package upp.foodonet.material;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.Rating;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Adapters.IRegisteredUserSelectedCallback;
import Adapters.RegisteredUsersForCallOrSmsRecyclerViewAdapter;
import CommonUtilPackage.AmazonImageUploader;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.IAmazonFinishedCallback;
import CommonUtilPackage.IPleaseRegisterDialogCallback;
import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import DataModel.FNotification;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServerClasses.ConnectionDetector;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServerClasses.ImageDownloader;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;
import UIUtil.RoundedImageView;

public class ExistingPublicationActivity
        extends FooDoNetCustomActivityConnectedToService
        implements Toolbar.OnMenuItemClickListener,
                    View.OnClickListener,
                    IRegisteredUserSelectedCallback,
                    IPleaseRegisterDialogCallback,
                    IFooDoNetServerCallback, IAmazonFinishedCallback, IFooDoNetSQLCallback {

    private static final String MY_TAG = "food_existPub";

    private int existing_publication_mode;
    private static final int MODE_MY_PUBLICATION = 1;
    private static final int MODE_OTHERS_PUBLICATION = 2;

    private static final int REQUEST_CODE_REGISTER = 1;
    private static final int REQUEST_CODE_EDIT_PUBLICATION = 2;

    private boolean amIRegisteredToThisPublication;

    public static final String PUBLICATION_EXTRA_KEY = "publication";
    FCPublication currentPublication;
    FCPublication editedPublication;

    Toolbar toolbar;

    ImageView iv_group_icon;
    TextView tv_group_name;
    TextView tv_time_left;
    TextView tv_users_joined;

    ImageView iv_publication_image;
    TextView tv_title;
    TextView tv_subtitle;

    RoundedImageView riv_user_avatar;
    TextView tv_address;
    ImageView iv_rating_star;
    TextView tv_user_rating;
    TextView tv_user_name;

    TextView tv_price;

    TextView tv_reports_title;
    LinearLayout ll_reports;

    ImageDownloader imageDownloader;

    //region floating buttons

    LinearLayout ll_fab_panel_my;
    FloatingActionButton fab_facebook;
    FloatingActionButton fab_twitter;
    FloatingActionButton fab_sms_reg;
    FloatingActionButton fab_call_reg;
    LinearLayout ll_fab_panel_other;
    FloatingActionButton fab_reg_unreg;
    FloatingActionButton fab_sms_owner;
    FloatingActionButton fab_call_owner;
    FloatingActionButton fab_navigate;

    //endregion

    //region select registered user

    final int SELECT_USER_DIALOG_STARTED_FOR_SMS = 1;
    final int SELECT_USER_DIALOG_STARTED_FOR_CALL = 2;
    int select_user_dialog_started_for;
    Dialog select_reg_user_dialog;
    boolean is_select_user_dialog_started;
    RecyclerView rv_select_user;

    //endregion

    protected boolean isInternetAvailable = false;
    protected boolean isGoogleServiceAvailable = false;

    AlertDialog dialog;

    boolean waitingForActionFinish;

    boolean ifChangesMade;

    public Activity getActivity() { return ExistingPublicationActivity.this; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_publication);

        Intent thisIntent = getIntent();
        currentPublication = (FCPublication) thisIntent.getSerializableExtra(PUBLICATION_EXTRA_KEY);
        if (currentPublication == null) {
            Log.e(MY_TAG, "no publication got from intent");
            Toast.makeText(this, "Error: no publication", Toast.LENGTH_SHORT).show();
            finish();
        }
        existing_publication_mode
                = currentPublication.getPublisherID() == CommonUtil.GetMyUserID(this)
                ? MODE_MY_PUBLICATION : MODE_OTHERS_PUBLICATION;

        amIRegisteredToThisPublication = false;
        if (existing_publication_mode == MODE_OTHERS_PUBLICATION && currentPublication.getRegisteredForThisPublication() != null) {
            for (RegisteredUserForPublication reg : currentPublication.getRegisteredForThisPublication()) {
                if (reg.getDevice_registered_uuid().compareToIgnoreCase(CommonUtil.GetIMEI(this)) == 0) {
                    amIRegisteredToThisPublication = true;
                    break;
                }
            }
        }

        toolbar = (Toolbar) findViewById(R.id.tb_existing_publication);
        iv_group_icon = (ImageView) findViewById(R.id.iv_pub_det_group_icon);
        tv_group_name = (TextView) findViewById(R.id.tv_pub_det_group_name);
        tv_time_left = (TextView) findViewById(R.id.tv_time_left_pub_det);
        tv_users_joined = (TextView) findViewById(R.id.tv_users_joined_pub_det);
        iv_publication_image = (ImageView) findViewById(R.id.iv_pub_det_image);
        riv_user_avatar = (RoundedImageView) findViewById(R.id.riv_pub_det_user_avatar);
        tv_address = (TextView) findViewById(R.id.tv_pub_det_address);
        iv_rating_star = (ImageView) findViewById(R.id.iv_pub_det_rating_star);
        tv_user_rating = (TextView) findViewById(R.id.tv_pub_det_user_rating);
        tv_user_name = (TextView) findViewById(R.id.tv_pub_det_user_name);
        tv_price = (TextView) findViewById(R.id.tv_pub_det_price);
        tv_reports_title = (TextView) findViewById(R.id.tv_pub_det_reports_title);
        ll_reports = (LinearLayout) findViewById(R.id.ll_pub_det_reports);

        InitToolBar();
        InitTopInfoBar();
        SetPublicationImage();

        tv_title = (TextView) findViewById(R.id.tv_pub_det_title);
        tv_title.setText(currentPublication.getTitle());
        tv_subtitle = (TextView) findViewById(R.id.tv_pub_det_subtitle);
        if (TextUtils.isEmpty(currentPublication.getSubtitle()))
            tv_subtitle.setVisibility(View.GONE);
        else
            tv_subtitle.setText(currentPublication.getSubtitle());

        InitUserData();
        SetPrice();
        SetReports();
        InitFABPanel();

/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
    }

    @Override
    public void onBackPressed() {
        setResult(ifChangesMade ? RESULT_OK : RESULT_CANCELED);
        finish();
    }

    private void SetPrice(){
        if (currentPublication.getPrice() == null || currentPublication.getPrice() == 0)
            tv_price.setText(getString(R.string.publication_details_price_free));
        else tv_price.setText(getString(R.string.publication_details_price_format)
                .replace("{0}", String.valueOf(currentPublication.getPrice())));
    }

    @Override
    protected void onResume() {
        super.onResume();
        waitingForActionFinish = false;
        isInternetAvailable = CheckInternetConnection();
        if (!isInternetAvailable)
            OnInternetNotConnected();
        isGoogleServiceAvailable = CheckPlayServices();
        if (!isGoogleServiceAvailable)
            OnGooglePlayServicesCheckError();
        Cursor cursor = getContentResolver()
                .query(Uri.parse(FooDoNetSQLProvider.CONTENT_URI + "/" + currentPublication.getUniqueId()),
                        FCPublication.GetColumnNamesArray(), null, null, null);
        ArrayList<FCPublication> publications = FCPublication.GetArrayListOfPublicationsFromCursor(cursor, false);
        if(publications.size() == 0)
            ShowAlertPublicationDeletedAndCloseScreen();
        else if (progressDialog != null && progressDialog.isShowing()) {
            int pendingBroadcastTypeID = CommonUtil.GetPendingBroadcastTypeFromSharedPreferences(this);
            if (pendingBroadcastTypeID == -1) {
                Log.e(MY_TAG, "progress bar showing, but no pending broadcast");
                //progressDialog.dismiss();
            }
            else {
            Intent intent = new Intent();
            intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                    pendingBroadcastTypeID);
                onBroadcastReceived(intent);
            }
        }
    }

    private void InitToolBar() {
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(null);
        if (toolbar != null)
            toolbar.setOnMenuItemClickListener(this);
        toolbar.getMenu().clear();
        switch (existing_publication_mode) {
            case MODE_MY_PUBLICATION:
                toolbar.inflateMenu(currentPublication.IsActivePublication()
                        ? R.menu.existing_publication_my_active_menu
                        : R.menu.existing_publication_my_inactive_menu);
                break;
            case MODE_OTHERS_PUBLICATION:
                if (amIRegisteredToThisPublication)
                    toolbar.inflateMenu(R.menu.existing_publication_others_menu);
                break;
        }
        //toolbar.inflateMenu(R.menu.menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getTitle().toString().compareToIgnoreCase(getString(R.string.menu_item_edit)) == 0) {
            EditCurrentPublication();
        } else if (item.getTitle().toString().compareToIgnoreCase(getString(R.string.menu_item_delete)) == 0) {
            if (progressDialog != null)
                progressDialog.dismiss();
            progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_delete_pub));
            HttpServerConnectorAsync connector2
                    = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), (IFooDoNetServerCallback) this);
            String subPath1 = getString(R.string.server_edit_publication_path);
            subPath1 = subPath1.replace("{0}", String.valueOf(currentPublication.getUniqueId()));
            InternalRequest ir2 = new InternalRequest(InternalRequest.ACTION_DELETE_PUBLICATION, subPath1);
            connector2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir2);
        } else if (item.getTitle().toString().compareToIgnoreCase(getString(R.string.menu_item_stop_event)) == 0) {
            if (!CheckInternetForAction(getString(R.string.action_take_off_air)))
                return false;
            if (progressDialog != null)
                progressDialog.dismiss();
            progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_taking_pub_off_air));
            currentPublication.setIsOnAir(false);
            //currentPublication.setPublisherUserName();
            HttpServerConnectorAsync connector1
                    = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), (IFooDoNetServerCallback) this);
            String subPath = getString(R.string.server_edit_publication_path);
            subPath = subPath.replace("{0}", String.valueOf(currentPublication.getUniqueId()));
            InternalRequest ir1
                    = new InternalRequest(InternalRequest.ACTION_PUT_TAKE_PUBLICATION_OFF_AIR,
                    subPath, currentPublication);
            ir1.publicationForSaving = currentPublication;
            connector1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir1);
        } else if (item.getTitle().toString().compareToIgnoreCase(getString(R.string.menu_item_restart_event)) == 0) {
            if (!CheckInternetForAction(getString(R.string.action_take_off_air)))
                return false;
            if (progressDialog != null)
                progressDialog.dismiss();
            progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_activating_pub));
            currentPublication.setIsOnAir(true);
            HttpServerConnectorAsync connector1
                    = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), (IFooDoNetServerCallback) this);
            String subPath = getString(R.string.server_edit_publication_path);
            subPath = subPath.replace("{0}", String.valueOf(currentPublication.getUniqueId()));
            InternalRequest ir1
                    = new InternalRequest(InternalRequest.ACTION_PUT_REACTIVATE_PUBLICATION,
                    subPath, currentPublication);
            ir1.publicationForSaving = currentPublication;
            connector1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir1);
        } else if (item.getTitle().toString().compareToIgnoreCase(getString(R.string.menu_item_report)) == 0) {
            if(CheckIfPublicationHasMyReport()){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.additional_report_not_allowed_title));
                builder.setMessage(getString(R.string.additional_report_not_allowed_content));
                String positiveText = getString(R.string.address_dialog_btn_ok);
                builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }
            if(waitingForActionFinish) return false;
            waitingForActionFinish = true;
            CheckIfMyLocationAvailableAndAskReportConfirmation();
        }
        return true;
    }

    private boolean CheckIfPublicationHasMyReport(){
        for(PublicationReport report : currentPublication.getPublicationReports())
            if(report.getReport_userID() == CommonUtil.GetMyUserID(this))
                return true;
        return false;
    }

    private void EditCurrentPublication(){
        Intent editPubIntent = new Intent(this, AddEditPublicationActivity.class);
        editPubIntent.putExtra(AddEditPublicationActivity.PUBLICATION_KEY, currentPublication);
        startActivityForResult(editPubIntent, REQUEST_CODE_EDIT_PUBLICATION);
    }

    private void InitTopInfoBar() {
        if (currentPublication.getAudience() == 0) {
            iv_group_icon.setImageDrawable(getResources().getDrawable(R.drawable.public_group_pub_det_icon));
            tv_group_name.setText(getString(R.string.public_share_group_name));
        } else {
            iv_group_icon.setImageDrawable(getResources().getDrawable(R.drawable.group_pub_det_icon));
            tv_group_name.setText(currentPublication.get_group_name());
        }

        if (currentPublication.IsActivePublication())
            tv_time_left.setText(GetTimeLertTillPublicationEnds());
        else tv_time_left.setText(getString(R.string.time_left_ended));

        tv_users_joined.setText(getString(R.string.users_joined_format_for_list)
                .replace("{0}", String.valueOf(
                        currentPublication.getRegisteredForThisPublication() == null
                                ? 0 : currentPublication.getRegisteredForThisPublication().size())));
    }

    private void SetPublicationImage() {
        final int id = currentPublication.getUniqueId();
        final int version = currentPublication.getVersion();
        imageDownloader = new ImageDownloader(this, null);
        imageDownloader.Download(id, version, iv_publication_image);
    }

    private String GetTimeLertTillPublicationEnds() {
        return CommonUtil.GetTimeLeftString(this, new Date(), currentPublication.getEndingDate());
    }

    private void InitUserData() {
        imageDownloader.DownloadUserAvatar(
                getString(R.string.amazon_user_avatar_image_name).replace("{0}",
                        String.valueOf(currentPublication.getPublisherID())), riv_user_avatar);
        tv_address.setText(currentPublication.getAddress());
        iv_rating_star.setImageDrawable(getRatingStarByUserRating(currentPublication.getRating()));
        tv_user_rating.setText(getString(R.string.user_rating_format)
                .replace("{0}", String.valueOf((int) (currentPublication.getRating() / 1)))
                .replace("{1}", String.valueOf((int) (currentPublication.getRating() % 1))));
        tv_user_name.setText(currentPublication.getPublisherUserName());
    }

    private Drawable getRatingStarByUserRating(double rating) {
        if (rating <= 0)
            return getResources().getDrawable(R.drawable.rating_star_no_rating);
        if (rating <= 2)
            return getResources().getDrawable(R.drawable.rating_star_bad);
        if (rating <= 4)
            return getResources().getDrawable(R.drawable.rating_star_half);
        return getResources().getDrawable(R.drawable.rating_star_good);
    }

    private void SetReports() {
        if (currentPublication.getPublicationReports() == null
                || currentPublication.getPublicationReports().size() == 0) {
            tv_reports_title.setText(getString(R.string.publication_details_no_reports));
            return;
        } else {
            tv_reports_title.setText(getString(R.string.publication_details_reports));
        }
        for (PublicationReport report : currentPublication.getPublicationReports())
            AddReportToPanel(report);
    }

    private void ResetReports(){
        Cursor cursor = getContentResolver().query(
                Uri.parse(FooDoNetSQLProvider.URI_GET_ALL_REPORTS_BY_PUB_ID + "/" + currentPublication.getUniqueId()),
                PublicationReport.GetColumnNamesArray(), null, null, null);
        ArrayList<PublicationReport> reports = PublicationReport.GetArrayListOfPublicationReportsFromCursor(cursor);
        if (reports != null && reports.size() > 0) {
            currentPublication.getPublicationReports().clear();
            currentPublication.setPublicationReports(reports);
            ll_reports.removeAllViews();
            for(PublicationReport report : currentPublication.getPublicationReports())
                AddReportToPanel(report);
        }

    }

    private void AddReportToPanel(PublicationReport report){
        View reportView = getLayoutInflater().inflate(R.layout.publication_details_report_item, null);
        TextView tv_report_title = (TextView) reportView.findViewById(R.id.tv_report_details);
        tv_report_title.setText(getString(R.string.report_format)
                .replace("{0}", GetReportStringByCode(report.getReport()))
                .replace("{1}", CommonUtil.GetTimeLeftString(this, report.getDate_reported(), new Date())));
        ll_reports.addView(reportView);
    }

    private String GetReportStringByCode(int reportCode) {
        switch (reportCode) {
            case 1:
                return getString(R.string.report_has_more);
            case 3:
                return getString(R.string.report_took_all);
            case 5:
                return getString(R.string.report_nothing_found);
            default:
                return getString(R.string.report_error);
        }
    }

    private void InitFABPanel() {
        ll_fab_panel_my = (LinearLayout) findViewById(R.id.ll_pub_det_fab_panel_my);
        ll_fab_panel_other = (LinearLayout) findViewById(R.id.ll_pub_det_fab_panel_others);
        switch (existing_publication_mode) {
            case MODE_MY_PUBLICATION:
                ll_fab_panel_other.setVisibility(View.GONE);
                ll_fab_panel_my.setVisibility(View.VISIBLE);
                fab_facebook = (FloatingActionButton) findViewById(R.id.fab_pub_det_facebook);
                fab_facebook.setOnClickListener(this);
                fab_twitter = (FloatingActionButton) findViewById(R.id.fab_pub_det_twitter);
                fab_twitter.setOnClickListener(this);
                fab_call_reg = (FloatingActionButton) findViewById(R.id.fab_pub_det_call_reg);
                fab_call_reg.setOnClickListener(this);
                fab_sms_reg = (FloatingActionButton) findViewById(R.id.fab_pub_det_sms_reg);
                fab_sms_reg.setOnClickListener(this);
                if(currentPublication.getRegisteredForThisPublication() == null
                        || currentPublication.getRegisteredForThisPublication().size() == 0){
                    fab_call_reg.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_inactive_gray)));
                    fab_sms_reg.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_inactive_gray)));
                }
                break;
            case MODE_OTHERS_PUBLICATION:
                ll_fab_panel_other.setVisibility(View.VISIBLE);
                ll_fab_panel_my.setVisibility(View.GONE);
                fab_reg_unreg = (FloatingActionButton) findViewById(R.id.fab_pub_det_register_unregister);
                fab_reg_unreg.setOnClickListener(this);
                fab_sms_owner = (FloatingActionButton) findViewById(R.id.fab_pub_det_sms);
                fab_sms_owner.setOnClickListener(this);
                fab_call_owner = (FloatingActionButton) findViewById(R.id.fab_pub_det_call);
                fab_call_owner.setOnClickListener(this);
                fab_navigate = (FloatingActionButton) findViewById(R.id.fab_pub_det_navigate);
                fab_navigate.setOnClickListener(this);
                if (!amIRegisteredToThisPublication) {
                    fab_reg_unreg.setImageDrawable(getResources().getDrawable(R.drawable.fab_register));
                    fab_sms_owner.setVisibility(View.GONE);
                    fab_call_owner.setVisibility(View.GONE);
                    fab_navigate.setVisibility(View.GONE);
                } else {
                    fab_reg_unreg.setImageDrawable(getResources().getDrawable(R.drawable.fab_unregister));
                    fab_navigate.setVisibility(View.VISIBLE);
                    fab_sms_owner.setVisibility(View.VISIBLE);
                    fab_call_owner.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_pub_det_facebook:
                if (!CheckInternetForAction(getString(R.string.post_on_facebook_action)))
                    return;
                if (progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_loading));
                PostOnFacebook();
                break;
            case R.id.fab_pub_det_twitter:
                if (!CheckInternetForAction(getString(R.string.post_on_tweeter_action)))
                    return;
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_loading));
                SendTweet();
                break;
            case R.id.fab_pub_det_call_reg:
            case R.id.fab_pub_det_sms_reg:
                if(currentPublication.getRegisteredForThisPublication() == null
                        || currentPublication.getRegisteredForThisPublication().size() == 0) {
                    Toast.makeText(this, getString(R.string.no_registered_user_for_sms_or_call), Toast.LENGTH_SHORT).show();
                    return;
                }
                select_user_dialog_started_for = view.getId() == R.id.fab_pub_det_call_reg
                        ? SELECT_USER_DIALOG_STARTED_FOR_CALL
                        : SELECT_USER_DIALOG_STARTED_FOR_SMS;
                ShowSelectUserDialog();
                break;
            case R.id.fab_pub_det_register_unregister:
                if (!CommonUtil.GetFromPreferencesIsRegisteredToGoogleFacebook(this))
                    dialog = CommonUtil.ShowDialogNeedToRegister(this, REQUEST_CODE_REGISTER, this);
                else {
                    RegisterUnregister();
                }
                break;
            case R.id.fab_pub_det_sms:
                StartSMS(currentPublication.getContactInfo());
                break;
            case R.id.fab_pub_det_call:
                StartCall(currentPublication.getContactInfo());
                break;
            case R.id.fab_pub_det_navigate:
                try {
                    String url = "waze://?ll=" + currentPublication.getLatitude() + "," + currentPublication.getLongitude();
                    Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(navIntent);
                } catch (ActivityNotFoundException ex) {
                    Intent navIntent =
                            new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
                    startActivity(navIntent);
                }
                break;
        }
    }

    @Override
    public void YesRegisterNow(int code) {
        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_creating_account));
        Intent signInIntent = new Intent(this, SignInActivity.class);
        startActivityForResult(signInIntent, code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE_REGISTER:
                switch (resultCode) {
                    case 1:
                        InternalRequest ir = (InternalRequest) data.getSerializableExtra(InternalRequest.INTERNAL_REQUEST_EXTRA_KEY);
                        ir.DoAfterRegistrationActionID = requestCode;
                        if (ir != null) {
                            HttpServerConnectorAsync connectorAsync
                                    = new HttpServerConnectorAsync(getString(R.string.server_base_url), (IFooDoNetServerCallback) this);
                            connectorAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
                            return;
                        }
                        Log.e(MY_TAG, "InternalRequest extra null");
                        break;
                    default:
                        Log.i(MY_TAG, "User decided not to login with google/facebook");
                        OnServerRespondedCallback(null);
                        break;
                }
                break;
            case REQUEST_CODE_EDIT_PUBLICATION:
                switch (resultCode){
                    case RESULT_OK:
                        editedPublication = (FCPublication)data.getSerializableExtra(AddEditPublicationActivity.PUBLICATION_KEY);
                        if(editedPublication == null){
                            Log.e(MY_TAG, "unexpected result - no publication got back from edit");
                            return;
                        }
                        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_saving_publication));
                        AddEditPublicationService.StartSaveEditedPublication(this, editedPublication);
                        break;
                    case RESULT_CANCELED:
                        break;
                }

                break;
        }
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        if(response == null)
            return;
        switch (response.ActionCommand){
            case InternalRequest.ACTION_PUT_TAKE_PUBLICATION_OFF_AIR:
                if (response.Status == InternalRequest.STATUS_OK) {
                    currentPublication.setIsOnAir(response.Status == InternalRequest.STATUS_FAIL);
                    toolbar.getMenu().clear();
                    InitToolBar();
                    InitTopInfoBar();
                    getContentResolver().update(Uri.parse(
                            FooDoNetSQLProvider.CONTENT_URI + "/" + currentPublication.getUniqueId()),
                            currentPublication.GetContentValuesRow(), null, null);
                    ifChangesMade = true;
                } else
                    Snackbar.make(fab_facebook, getString(R.string.failed_to_save_changes), Snackbar.LENGTH_SHORT).show();
                break;
            case InternalRequest.ACTION_PUT_REACTIVATE_PUBLICATION:
                if (response.Status == InternalRequest.STATUS_OK) {
                    currentPublication.setIsOnAir(response.Status == InternalRequest.STATUS_OK);
                    toolbar.getMenu().clear();
                    InitToolBar();
                    InitTopInfoBar();
                    getContentResolver().update(Uri.parse(
                            FooDoNetSQLProvider.CONTENT_URI + "/" + currentPublication.getUniqueId()),
                            currentPublication.GetContentValuesRow(), null, null);
                    ifChangesMade = true;
                } else
                    Snackbar.make(fab_facebook, getString(R.string.failed_to_save_changes), Snackbar.LENGTH_SHORT).show();
                break;
            case InternalRequest.ACTION_DELETE_PUBLICATION:
                FooDoNetSQLExecuterAsync sqlExecutor
                        = new FooDoNetSQLExecuterAsync(this, this);
                sqlExecutor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        new InternalRequest(response.ActionCommand, currentPublication));
                break;
            case InternalRequest.ACTION_POST_NEW_USER:
                switch (response.Status) {
                    case InternalRequest.STATUS_OK:
                        if (response.newUserID > 0) {
                            CommonUtil.SaveMyUserID(this, response.newUserID);
                            File avatarFile = new File(Environment.getExternalStorageDirectory()
                                    + getResources().getString(R.string.image_folder_path), getString(R.string.user_avatar_file_name));
                            if(avatarFile.exists()){
                                AmazonImageUploader uploader = new AmazonImageUploader(this, this);
                                uploader.UploadUserAvatarToAmazon(avatarFile);
                            }
                        }
                        RegisterUnregister();
                        break;
                    case InternalRequest.STATUS_FAIL:
                        break;
                }
                break;
        }
        if(progressDialog != null)
            progressDialog.dismiss();
        progressDialog = null;
    }

    @Override
    public void NotifyToBListenerAboutEvent(int eventCode) { }

    private void RegisterUnregister(){
        if (!CheckInternetForAction(amIRegisteredToThisPublication
                ? getString(R.string.action_unregistering_from_publication)
                : getString(R.string.action_registering_for_publication)))
            return;
        if (!amIRegisteredToThisPublication) {
            progressDialog
                    = CommonUtil.ShowProgressDialog(this, getString(R.string.action_registering_for_publication));
            RegisteredUserForPublication newRegistrationForPub
                    = new RegisteredUserForPublication();
            newRegistrationForPub.setDate_registered(new Date());
            newRegistrationForPub.setDevice_registered_uuid(CommonUtil.GetIMEI(this));
            newRegistrationForPub.setPublication_id(currentPublication.getUniqueId());
            newRegistrationForPub.setPublication_version(currentPublication.getVersion());
            newRegistrationForPub.setCollectorName(CommonUtil.GetMyUserNameFromPreferences(this));
            newRegistrationForPub.setCollectorphone(CommonUtil.GetMyPhoneNumberFromPreferences(this));
            newRegistrationForPub.setUserID(CommonUtil.GetMyUserID(this));
            RegisterUnregisterReportService.startActionRegisterToPub(this, newRegistrationForPub);
        } else {
            progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.action_unregistering_from_publication));
            RegisteredUserForPublication unreg = new RegisteredUserForPublication();
            unreg.setDate_registered(new Date());
            unreg.setDevice_registered_uuid(CommonUtil.GetIMEI(this));
            unreg.setPublication_id(currentPublication.getUniqueId());
            unreg.setPublication_version(currentPublication.getVersion());
            unreg.setUserID(CommonUtil.GetMyUserID(this));
            unreg.setCollectorName("");
            unreg.setCollectorphone("");
            RegisterUnregisterReportService.startActionUnRegisterFromPub(this, unreg);
        }
    }

    private void PopButtonsAfterRegistration(){
        Animation expandIn = AnimationUtils.loadAnimation(this, R.anim.anim_fab_pop_appear);
        fab_sms_owner.setVisibility(View.VISIBLE);
        fab_sms_owner.startAnimation(expandIn);
        fab_call_owner.setVisibility(View.VISIBLE);
        fab_call_owner.startAnimation(expandIn);
        fab_navigate.setVisibility(View.VISIBLE);
        fab_navigate.startAnimation(expandIn);
        toolbar.inflateMenu(R.menu.existing_publication_others_menu);
    }

    private void CollapseButtonsAfterUnregister(){
        Animation collapseOut = AnimationUtils.loadAnimation(this, R.anim.anim_fab_collapse_disappear);
        fab_sms_owner.startAnimation(collapseOut);
        fab_sms_owner.setVisibility(View.GONE);
        fab_call_owner.startAnimation(collapseOut);
        fab_call_owner.setVisibility(View.GONE);
        fab_navigate.startAnimation(collapseOut);
        fab_navigate.setVisibility(View.GONE);
        toolbar.getMenu().clear();
    }

    protected boolean CheckInternetForAction(String action) {
        if (!isInternetAvailable) {
            isInternetAvailable = CheckInternetConnection();
            if (!isInternetAvailable) {
                Toast.makeText(this,
                        getString(R.string.error_cant_perform_this_action_without_internet).replace("{0}",
                                action), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

    protected boolean CheckPlayServices() {
        Log.i(MY_TAG, "checking isGooglePlayServicesAvailable...");
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Log.e(MY_TAG, "UserRecoverableError: " + resultCode);
            }
            Log.e(MY_TAG, "Google Play Services Error: " + resultCode);
            return false;
        }
        Log.w(MY_TAG, "Google Play Services available!");
        return true;
    }

    protected boolean CheckInternetConnection() {
        Log.i(MY_TAG, "Checking internet connection...");
        ConnectionDetector cd = new ConnectionDetector(getBaseContext());
        return cd.isConnectingToInternet();
    }

    //region Facebook method
    private void PostOnFacebook() {
        Intent facebookIntent = new Intent(Intent.ACTION_SEND);
        String msg = currentPublication.getTitle() + "\n " + getString(R.string.facebook_page_url) + "\n ";
        facebookIntent.putExtra(Intent.EXTRA_TEXT, msg);
        String fileName = currentPublication.getUniqueId() + "." + currentPublication.getVersion() + ".jpg";
        String imageSubFolder = getString(R.string.image_folder_path);
        File photo = new File(fileName);
        if (!photo.exists())
            photo = new File(Environment.getExternalStorageDirectory() + imageSubFolder, fileName);
        facebookIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photo));
        facebookIntent.setType("image/*");
        PackageManager packManager = getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(facebookIntent, PackageManager.MATCH_DEFAULT_ONLY);
        boolean resolved = false;
        for (ResolveInfo resolveInfo : resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.facebook.katana")) {
                facebookIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }
        if (resolved) {
            startActivity(facebookIntent);
        } else {
            Toast.makeText(this, "Facebook app isn't found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.facebook.katana"));
            startActivity(intent);
        }
        if (progressDialog != null)
            progressDialog.dismiss();
    }
    // endregion

    // region  Twitter method
    private void SendTweet() {
        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
        String msg = getString(R.string.hashtag) + " : " + currentPublication.getTitle() + "\n " +
                getString(R.string.facebook_page_url);
        tweetIntent.putExtra(Intent.EXTRA_TEXT, msg);
        String fileName = currentPublication.getUniqueId() + "." + currentPublication.getVersion() + ".jpg";
        String imageSubFolder = getString(R.string.image_folder_path);
        File photo = new File(fileName);
        if (!photo.exists())
            photo = new File(Environment.getExternalStorageDirectory() + imageSubFolder, fileName);
        tweetIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photo));
        tweetIntent.setType("text/plain");
        PackageManager packManager = getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);
        boolean resolved = false;
        for (ResolveInfo resolveInfo : resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                tweetIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }
        if (resolved) {
            startActivity(tweetIntent);
        } else {
            Toast.makeText(this, "Twitter app isn't found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.twitter.android"));
            startActivity(intent);
        }
        if (progressDialog != null)
            progressDialog.dismiss();
    }
    // endregion


    //region Select registered user dialog
    private void ShowSelectUserDialog() {
        is_select_user_dialog_started = true;
        select_reg_user_dialog = new Dialog(this);
        select_reg_user_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        select_reg_user_dialog.setContentView(R.layout.select_reg_user_dialog);
        select_reg_user_dialog.setCanceledOnTouchOutside(true);
        select_reg_user_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) { }
        });
        select_reg_user_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                RegisteredUserSelected(null);
            }
        });

        rv_select_user = (RecyclerView)select_reg_user_dialog.findViewById(R.id.rv_reged_users);
        rv_select_user.setLayoutManager(new LinearLayoutManager(rv_select_user.getContext()));
        RegisteredUsersForCallOrSmsRecyclerViewAdapter adapter
                = new RegisteredUsersForCallOrSmsRecyclerViewAdapter(currentPublication.getRegisteredForThisPublication(), this);
        rv_select_user.setAdapter(adapter);

        Button btn_cancel_select = (Button)select_reg_user_dialog.findViewById(R.id.btn_cancel_select_reg_user);
        btn_cancel_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisteredUserSelected(null);
            }
        });

        select_reg_user_dialog.show();
    }

    @Override
    public void RegisteredUserSelected(String phoneNumber) {
        if(select_reg_user_dialog != null)
            select_reg_user_dialog.dismiss();
        if(phoneNumber == null || TextUtils.isEmpty(phoneNumber))
            return;
        switch (select_user_dialog_started_for){
            case SELECT_USER_DIALOG_STARTED_FOR_CALL:
                StartCall(phoneNumber);
                break;
            case SELECT_USER_DIALOG_STARTED_FOR_SMS:
                StartSMS(phoneNumber);
                break;
            default:
                return;
        }
    }
    //endregion

    private void StartCall(String phoneNumber){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void StartSMS(String phoneNumber){
        Intent intentSMS = new Intent(Intent.ACTION_SENDTO);
        intentSMS.setType("text/plain");
        intentSMS.setData(Uri.parse("smsto:" + phoneNumber));
        intentSMS.putExtra("sms_body", getString(R.string.pub_det_sms_default_text) + ": " + currentPublication.getTitle());
        if (intentSMS.resolveActivity(getPackageManager()) != null) {
            startActivity(intentSMS);
        }
    }

    @Override
    public void onBroadcastReceived(Intent intent) {
        super.onBroadcastReceived(intent);
        int actionCode = intent.getIntExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, -1);
        switch (actionCode) {
            case ServicesBroadcastReceiver.ACTION_CODE_REGISTER_TO_PUBLICATION_SUCCESS:
                Log.i(MY_TAG, "successfully registered to publication " + currentPublication.getUniqueId());
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_REGISTER_TO_PUBLICATION_FAIL:
                Log.i(MY_TAG, "failed to register to publication");
                if (progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = null;
                Toast.makeText(getBaseContext(),
                        getResources().getString(R.string.pub_det_uimessage_failed_register_to_pub), Toast.LENGTH_LONG).show();
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_UNREGISTER_FROM_PUBLICATION_SUCCESS:
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_UNREGISTER_FROM_PUBLICATION_FAIL:
                if (progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = null;
                Toast.makeText(getBaseContext(),
                        getResources().getString(R.string.pub_det_uimessage_failed_unregister_from_pub), Toast.LENGTH_LONG).show();
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_ADD_MYSELF_TO_REGS_FOR_PUBLICATION:
                Log.i(MY_TAG, "successfully added myself to regs! refreshing number");
                amIRegisteredToThisPublication = true;
                PopButtonsAfterRegistration();
                fab_reg_unreg.setImageDrawable(getResources().getDrawable(R.drawable.fab_unregister));
                RefreshNumberOfJoinedUsers();
                ifChangesMade = true;
                if (progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = null;
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_REMOVE_MYSELF_FROM_REGS_FOR_PUBLICATION:
                Log.i(MY_TAG, "successfully removed myself from regs! refreshing number");
                amIRegisteredToThisPublication = false;
                CollapseButtonsAfterUnregister();
                fab_reg_unreg.setImageDrawable(getResources().getDrawable(R.drawable.fab_register));
                RefreshNumberOfJoinedUsers();
                ifChangesMade = true;
                if (progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = null;
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_REPORT_TO_PUBLICATION_SUCCESS:
                Log.i(MY_TAG, "successfully left report for publication!");
                amIRegisteredToThisPublication = false;
                CollapseButtonsAfterUnregister();
                RefreshNumberOfJoinedUsers();
                ResetReports();
                if(progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = null;
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_SAVE_EDITED_PUB_SUCCESS:
                if(editedPublication != null){
                    editedPublication.setPhotoUrl(null);
                    editedPublication.setVersion(editedPublication.getVersion() + 1);
                    currentPublication = editedPublication;
                    SetPublicationPropertiesToControls();
                    if(progressDialog != null)
                        progressDialog.dismiss();
                    ifChangesMade = true;
                }
            case ServicesBroadcastReceiver.ACTION_CODE_SAVE_EDITED_PUB_FAIL:
                if(progressDialog != null)
                    progressDialog.dismiss();
                Toast.makeText(this,
                        getString(actionCode == ServicesBroadcastReceiver.ACTION_CODE_SAVE_EDITED_PUB_FAIL
                                ? R.string.failed_to_save_edited_publication
                                : R.string.succeeded_to_save_edited_publication),
                        Toast.LENGTH_SHORT).show();
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_NOTIFICATION_RECEIVED_PUBLICATION_DELETED:
                if(progressDialog != null)
                    progressDialog.dismiss();
                Serializable sObj = intent.getSerializableExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_NOTIFICATION_KEY);
                if(sObj != null){
                    FNotification notification = (FNotification)sObj;
                    if(notification.get_publication_or_group_id() == currentPublication.getUniqueId()){
                        ShowAlertPublicationDeletedAndCloseScreen();
                    }
                }
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_NOTIFICATION_RECEIVED_NEW_REGISTERED_USER:
                RefreshNumberOfJoinedUsers();
                break;
        }
        CommonUtil.ClearPendingBroadcastFromSharedPreferences(this);
    }

    private void ShowAlertPublicationDeletedAndCloseScreen(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.notification_this_publication_has_been_removed_by_owner);
        builder.setPositiveButton(R.string.address_dialog_btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show().setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                getActivity().setResult(RESULT_OK);
                getActivity().finish();
            }
        });
    }

    private void SetPublicationPropertiesToControls(){
        tv_title.setText(currentPublication.getTitle());
        tv_subtitle.setText(currentPublication.getSubtitle());
        tv_address.setText(currentPublication.getAddress());
        SetPrice();
        SetPublicationImage();
        InitToolBar();
        InitTopInfoBar();
    }

    private void RefreshNumberOfJoinedUsers(){
        Cursor cursorReged = getContentResolver().query(Uri.parse(FooDoNetSQLProvider.URI_GET_REGISTERED_BY_PUBLICATION_ID + "/" + currentPublication.getUniqueId()),
                RegisteredUserForPublication.GetColumnNamesArray(), null, null, null);
        ArrayList<RegisteredUserForPublication> regs = RegisteredUserForPublication.GetArrayListOfRegisteredForPublicationsFromCursor(cursorReged);
        tv_users_joined.setText(
                getString(R.string.users_joined_format_for_list)
                        .replace("{0}", String.valueOf(regs != null ? regs.size() : 0)));
        cursorReged.close();
    }

    private boolean CheckIfMyLocationAvailableAndAskReportConfirmation() {
        LatLng myLocation = CommonUtil.GetMyLocationFromPreferences(this);
        if (myLocation.latitude == -1000 || myLocation.longitude == -1000)
            return true;
        double distance = CommonUtil.GetDistanceInKM(
                new LatLng(currentPublication.getLatitude(), currentPublication.getLongitude()), myLocation);
        if (distance < 2)
            return true;
        else {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            ShowReportDialog();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            waitingForActionFinish = false;
                            return;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.report_big_distance))
                    .setPositiveButton(getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(getString(R.string.no), dialogClickListener).show().setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    waitingForActionFinish = false;
                }
            });
        }
        return false;
    }

    private void ShowReportDialog(){
        final Dialog reportDialog = new Dialog(this);
        reportDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        reportDialog.setContentView(R.layout.report_dialog);
        final RadioGroup rg_report = (RadioGroup)reportDialog.findViewById(R.id.rg_report_dialog_options);
        final RatingBar rating_report = (RatingBar)reportDialog.findViewById(R.id.rating_bar_report_dialog);
        final TextView tv_report_dialog_title = (TextView)reportDialog.findViewById(R.id.tv_report_dialog_title);
        tv_report_dialog_title.setText(getString(R.string.report_dialog_title_format).replace("{0}", currentPublication.getTitle()));
        final Button btn_rating_dialog_no = (Button)reportDialog.findViewById(R.id.btn_report_result_no);
        btn_rating_dialog_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportDialog.dismiss();
                waitingForActionFinish = false;
            }
        });
        final Button btn_rating_dialog_yes = (Button)reportDialog.findViewById(R.id.btn_report_result_yes);
        btn_rating_dialog_yes.setEnabled(false);
        rg_report.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                btn_rating_dialog_yes.setEnabled(true);
            }
        });
        btn_rating_dialog_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton selectedRB = (RadioButton)rg_report.findViewById(rg_report.getCheckedRadioButtonId());
                int reportType = Integer.parseInt(selectedRB.getTag().toString());
                int rating = (int)rating_report.getRating();
                SendReport(reportType, rating);
                reportDialog.dismiss();
                waitingForActionFinish = false;
            }
        });
        reportDialog.show();
    }

    private void SendReport(int reportType, int rating){
        PublicationReport report = new PublicationReport();
        report.setReport(reportType);
        report.setRating(rating);
        report.setPublication_id(currentPublication.getUniqueId());
        report.setPublication_version(currentPublication.getVersion());
        report.setDevice_uuid(CommonUtil.GetIMEI(this));
        report.setDate_reported(new Date());
        report.setReport_userID(CommonUtil.GetMyUserID(this));
        report.setReportContactInfo(CommonUtil.GetMyUserNameFromPreferences(this));
        report.setReportContactInfo(CommonUtil.GetMyPhoneNumberFromPreferences(this));
        RegisterUnregisterReportService.startActionReportForPublication(this, report);
        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_leaving_report));
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        switch (request.ActionCommand) {
            case InternalRequest.ACTION_DELETE_PUBLICATION:
                CommonUtil.RemoveImageByPublication(currentPublication, this);
                if (progressDialog != null)
                    progressDialog.dismiss();
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
                break;
        }
    }
}
