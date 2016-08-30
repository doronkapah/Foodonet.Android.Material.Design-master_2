package upp.foodonet.material;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import Adapters.AllPublicationsListRecyclerViewAdapter;
import Adapters.IOnPublicationFromListSelected;
import Adapters.MapMarkerInfoWindowAdapter;
import CommonUtilPackage.AmazonImageUploader;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.IAmazonFinishedCallback;
import CommonUtilPackage.INewGroupNameEnter;
import CommonUtilPackage.IPleaseRegisterDialogCallback;
import CommonUtilPackage.ImageDictionarySyncronized;
import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import DataModel.PublicationReport;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.FooDoNetSQLHelper;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServerClasses.ImageDownloader;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;
import UIUtil.RoundedImageView;
import upp.foodonet.material.R;

public class EntarenceMapAndListActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMyLocationChangeListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        IOnPublicationFromListSelected,
        TabLayout.OnTabSelectedListener,
        TextWatcher,
        IPleaseRegisterDialogCallback,
        IFooDoNetServerCallback, IAmazonFinishedCallback, IFooDoNetSQLCallback {

    private static final String MY_TAG = "food_mapAndList";

    private static final int MODE_MAP = 0;
    private static final int MODE_LIST = 1;
    private int currentMode;

    private static final int LIST_MODE_ALL = 0;
    private static final int LIST_MODE_MY = 1;
    private int currentListMode;

    private static final int REQUEST_CODE_NEW_PUB = 0;
    private static final int REQUEST_CODE_SETTINGS = 1;
    private static final int REQUEST_CODE_PUBLICATION_DETAILS = 2;

    //region Map variables
    SupportMapFragment mapFragment;
    GoogleMap googleMap;
    boolean isMapLoaded;
    HashMap<Marker, Integer> myMarkers;
    double maxDistance;
    int width, height;
    LatLng average, myLocation;
    float kilometer_for_map;
    int myLocationRefreshRate;
    ImageButton btn_focus_on_my_location;
    Date lastLocationUpdateDate;
    RelativeLayout ll_map_and_gallery;
    CoordinatorLayout.LayoutParams fabLayoutParams;
    FloatingActionButton fab;

    //endregion

    //region Gallery

    HorizontalScrollView hsv_gallery;
    LinearLayout gallery_pubs;
    ImageDownloader imageDownloader;
    ImageDictionarySyncronized imageDictionary;

    //endregion

    //region Publications list variables

    RecyclerView rv_all_publications_list;
    AllPublicationsListRecyclerViewAdapter adapter;
    FrameLayout fl_search_and_list;
    int currentFilterID;
    Toolbar tb_search;
    TextView tv_toolbar_label;
    EditText et_search;
    private Toolbar toolbar;
    TabLayout tl_list_filter_buttons;

//    TabLayout.Tab tab_all_all;
//    TabLayout.Tab tab_all_new;
//    TabLayout.Tab tab_all_closest;
//    TabLayout.Tab tab_my_all;
//    TabLayout.Tab tab_my_active;
//    TabLayout.Tab tab_my_ended;

    //endregion

    //region Nav menu variables

    DrawerLayout drawerLayout;
    boolean isSideMenuOpened;
    RoundedImageView riv_user_portrait;
    TextView tv_user_name;
    TextView tv_user_email;
    RelativeLayout btn_nav_menu_my_pubs;
    TextView tv_side_menu_list_mode;
    RelativeLayout btn_nav_menu_subscriptions;
    RelativeLayout btn_nav_menu_groups;
    RelativeLayout btn_nav_menu_settings;
    RelativeLayout btn_nav_menu_contact_us;
    RelativeLayout btn_nav_menu_terms;
    RelativeLayout btn_nav_menu_notifications;


    //endregion

    //region Registration for using features

    AlertDialog dialog;
    final int DO_AFTER_REGISTRATION_CODE_NOTHING = 10;
    final int DO_AFTER_REGISTRATION_CODE_ADD_PUBLICATION = 11;
    final int DO_AFTER_REGISTRATION_CODE_GROUPS = 12;
    final int DO_AFTER_REGISTRATION_CODE_SETTING = 13;
    final int DO_AFTER_REGISTRATION_CODE_MY_PUBS = 14;

    //endregion

    //region feedback

    Dialog feedbackDialog;
    EditText et_feedbackText;

    //endregion

    public Activity getActivity() {
        return this;
    }

    //region Activity overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppDefault);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_and_list);

        currentMode = MODE_LIST;

        initToolBar();
        initNavVew();

        fab = (FloatingActionButton) findViewById(R.id.fab_map_and_list);
        if (fab != null) fab.setOnClickListener(this);
        fabLayoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();

        tv_toolbar_label = (TextView) findViewById(R.id.tv_main_activity_title);

        fl_search_and_list = (FrameLayout) findViewById(R.id.fl_all_publications_list);
        fl_search_and_list.setVisibility(View.GONE);
        currentListMode = LIST_MODE_ALL;
        tl_list_filter_buttons = (TabLayout) findViewById(R.id.tl_list_filter_buttons);
        tl_list_filter_buttons.setOnTabSelectedListener(this);
        SetTabsVisibility(currentListMode);
        //SetupFilterTabButtons();
        tl_list_filter_buttons.setVisibility(View.GONE);
        tb_search = (Toolbar) findViewById(R.id.tb_search_pub_in_list);
        et_search = (EditText) findViewById(R.id.et_publication_list_search);
        et_search.addTextChangedListener(this);
        //tb_search.setVisibility(View.GONE);
/*
        View tab1view = LayoutInflater.from(tl_list_filter_buttons.getContext()).inflate(R.layout.tab_button_list_filter, tl_list_filter_buttons, false);
        TextView tv_tab1_title = (TextView)tab1view.findViewById(R.id.tv_tab_button_filter_title);
        tv_tab1_title.setText("tab1");
*/

//        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)fab.getLayoutParams();
//        lp.setBehavior(new FrameSwitchFABBehavior(this, null));
//        fab.setLayoutParams(lp);

        ll_map_and_gallery = (RelativeLayout) findViewById(R.id.ll_map_and_gallery);
        rv_all_publications_list = (RecyclerView) findViewById(R.id.rv_all_publications_list);

        btn_focus_on_my_location = (ImageButton) findViewById(R.id.btn_center_on_my_location_map);
        btn_focus_on_my_location.setOnClickListener(this);
        gallery_pubs = (LinearLayout) findViewById(R.id.ll_image_btns_gallery);
        hsv_gallery = (HorizontalScrollView) findViewById(R.id.hsv_image_gallery);

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.map_one_kilometer_for_calculation, typedValue, true);
        kilometer_for_map = typedValue.getFloat();
        myLocationRefreshRate = getResources().getInteger(R.integer.map_refresh_my_location_frequency_milliseconds);
        Point size = CommonUtil.GetScreenSize(this);
        width = size.x;
        height = size.y;


        imageDictionary = new ImageDictionarySyncronized();
        imageDownloader = new ImageDownloader(this, imageDictionary);

        currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_CLOSEST;
        //SetupRecyclerViewPublications();
        //   initTabs();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SetupMode();
        //SetTabsVisibility(currentListMode);
/*
        switch (ll_map_and_gallery.getVisibility()) {
            case View.VISIBLE:
                if (googleMap != null)
                    StartLoadingForMarkers();
                break;
            case View.GONE:
                StartLoadingForPublicationsList();
                break;
        }
*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        int publicationIdFromBroadcast = intent.getIntExtra(FooDoNetGcmListenerService.PUBLICATION_NUMBER, -1);
        intent.removeExtra(FooDoNetGcmListenerService.PUBLICATION_NUMBER);
        if(publicationIdFromBroadcast != -1)
            OnPublicationFromListClicked(publicationIdFromBroadcast);
    }

    @Override
    public void onBackPressed() {
        if (isSideMenuOpened) {
            drawerLayout.closeDrawers();
            return;
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        super.onBackPressed();
    }

    public void SetupMode() {
        switch (currentMode) {
            case MODE_LIST:
                SetFrameList();
                if (adapter == null)
                    SetupRecyclerViewPublications();
                StartLoadingForPublicationsList();
                break;
            case MODE_MAP:
                SetFrameMap();
                if (googleMap == null) {
                    mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    if (mapFragment != null) mapFragment.getMapAsync(this);
                } else
                    StartLoadingForMarkers();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_NEW_PUB:
                if (data == null) return;
                switch (resultCode) {
                    case AddEditPublicationActivity.RESULT_OK:
                        int action = data.getIntExtra(AddEditPublicationActivity.DETAILS_ACTIVITY_RESULT_KEY, -1);
                        switch (action) {
                            case InternalRequest.ACTION_POST_NEW_PUBLICATION:
                                FCPublication publication
                                        = (FCPublication) data.getExtras().get(AddEditPublicationActivity.PUBLICATION_KEY);
                                if (publication == null) {
                                    Log.i(MY_TAG, "got no pub from AddNew");
                                    return;
                                }
                                progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_saving_publication));
                                AddEditPublicationService.StartSaveNewPublication(this, publication);
                                break;
                        }
                        break;
                }
                break;
            case DO_AFTER_REGISTRATION_CODE_GROUPS:
            case DO_AFTER_REGISTRATION_CODE_ADD_PUBLICATION:
            case DO_AFTER_REGISTRATION_CODE_SETTING:
            case DO_AFTER_REGISTRATION_CODE_MY_PUBS:
                switch (resultCode) {
                    case RESULT_OK:
                        SetupSideMenuHeader();
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
            case REQUEST_CODE_SETTINGS:
                switch (resultCode) {
                    case SettingsSelectActivity.RESULT_CODE_LOGOUT_MADE:
                        RestartLoadingForMarkers();
                        currentListMode = LIST_MODE_MY;
                        onClick(btn_nav_menu_my_pubs);
                        riv_user_portrait.setImageDrawable(null);
                        tv_user_name.setText(null);
                        tv_user_email.setText(null);
                        break;
                    default:
                        return;
                }
                break;
            case REQUEST_CODE_PUBLICATION_DETAILS:
                switch (resultCode){
                    case RESULT_OK:
                        RestartLoadingForMarkers();
                        RestartLoadingForPublicationsList();
                        break;
                    default:
                        return;
                }
            default:
                break;
        }
    }

    //endregion

    //region CLICK LISTENERS

    @Override
    public void onClick(View v) {
        drawerLayout.closeDrawers();
        switch (v.getId()) {
            case R.id.fab_map_and_list:
                if (!CommonUtil.GetFromPreferencesIsRegisteredToGoogleFacebook(this))
                    dialog = CommonUtil.ShowDialogNeedToRegister(this, DO_AFTER_REGISTRATION_CODE_ADD_PUBLICATION, this);
                else {
                    InternalRequest ir = new InternalRequest(InternalRequest.ACTION_POST_NEW_USER);
                    ir.Status = InternalRequest.STATUS_OK;
                    ir.DoAfterRegistrationActionID = DO_AFTER_REGISTRATION_CODE_ADD_PUBLICATION;
                    OnServerRespondedCallback(ir);
                }
                break;
            case R.id.btn_center_on_my_location_map:
                if (myLocation == null)
                    return;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(CommonUtil.GetBoundsByCenterLatLng(myLocation, maxDistance), width, height, 0);
                googleMap.animateCamera(cu);
                break;
            case R.id.rl_btn_my_publications_list:
                if (currentListMode == LIST_MODE_ALL && !CommonUtil.GetFromPreferencesIsRegisteredToGoogleFacebook(this)) {
                    dialog = CommonUtil.ShowDialogNeedToRegister(this, DO_AFTER_REGISTRATION_CODE_ADD_PUBLICATION, this);
                    return;
                }
                switch (currentListMode) {
                    case LIST_MODE_ALL:
                        currentListMode = LIST_MODE_MY;
                        tv_side_menu_list_mode.setText(R.string.all_shares_toolbar_title);
                        currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_MY_ACTIVE_ID_DESC;
                        break;
                    case LIST_MODE_MY:
                        currentListMode = LIST_MODE_ALL;
                        tv_side_menu_list_mode.setText(R.string.my_shares_toolbar_title);
                        currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_CLOSEST;
                        break;
                }
                if (adapter != null)
                    adapter.UpdatePublicationsList(new ArrayList<FCPublication>(), currentListMode == LIST_MODE_MY);
                SetTabsVisibility(currentListMode);
                RestartLoadingForPublicationsList();
                if(progressDialog != null)
                    progressDialog.dismiss();
                progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_load));
//                Intent intent = new Intent(getApplicationContext(), MyPublicationsActivity.class);
//                startActivity(intent);
                break;
            case R.id.rl_btn_subscriptions:
                break;
            case R.id.rl_btn_groups:
                if (!CommonUtil.GetFromPreferencesIsRegisteredToGoogleFacebook(this))
                    dialog = CommonUtil.ShowDialogNeedToRegister(this, DO_AFTER_REGISTRATION_CODE_GROUPS, this);
                else {
                    InternalRequest ir = new InternalRequest(InternalRequest.ACTION_POST_NEW_USER);
                    ir.Status = InternalRequest.STATUS_OK;
                    ir.DoAfterRegistrationActionID = DO_AFTER_REGISTRATION_CODE_GROUPS;
                    OnServerRespondedCallback(ir);
                }
                break;
            case R.id.rl_btn_settings:
                if (!CommonUtil.GetFromPreferencesIsRegisteredToGoogleFacebook(this))
                    dialog = CommonUtil.ShowDialogNeedToRegister(this, DO_AFTER_REGISTRATION_CODE_SETTING, this);
                else {
                    Intent intentSettings = new Intent(this, SettingsSelectActivity.class);
                    startActivityForResult(intentSettings, REQUEST_CODE_SETTINGS);
                }
                break;
            case R.id.rl_btn_contact_us:
                feedbackDialog = new Dialog(this);
                feedbackDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                feedbackDialog.setContentView(R.layout.dialog_send_feedback);
                final Button btn_dialog_ok = (Button)feedbackDialog.findViewById(R.id.btn_feedback_ok);
                final Button btn_dialog_cancel = (Button)feedbackDialog.findViewById(R.id.btn_feedback_cancel);
                btn_dialog_ok.setOnClickListener(this);
                btn_dialog_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(feedbackDialog != null)
                            feedbackDialog.dismiss();
                    }
                });
                et_feedbackText = (EditText)feedbackDialog.findViewById(R.id.et_feedback_text);
                et_feedbackText.setText("");
                feedbackDialog.show();
                break;
            case R.id.btn_feedback_ok:
                if(et_feedbackText.getText().toString().length() == 0){
                    CommonUtil.SetEditTextIsValid(this, et_feedbackText, false);
                    Toast.makeText(this, getString(R.string.feedback_validation_text_null), Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_feedback_sending));
                HttpServerConnectorAsync serverConnector = new HttpServerConnectorAsync(getString(R.string.server_base_url), (IFooDoNetServerCallback)this);
                InternalRequest ir = new InternalRequest(InternalRequest.ACTION_POST_FEEDBACK);
                ir.publicationReport = new PublicationReport();
                ir.publicationReport.setReportContactInfo(et_feedbackText.getText().toString());
                ir.publicationReport.setReportUserName(
                        getSharedPreferences(getString(R.string.shared_preferences_contact_info), MODE_PRIVATE)
                                .getString(getString(R.string.shared_preferences_contact_info_name), ""));
                ir.publicationReport.setDevice_uuid(CommonUtil.GetIMEI(this));
                ir.ServerSubPath = getString(R.string.server_post_report_feedback);
                serverConnector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
                break;
            case R.id.rl_btn_terms_and_conditions:
                Intent aboutUsIntent = new Intent(this, AboutUsActivity.class);
                startActivity(aboutUsIntent);
                break;
            case R.id.rl_btn_notifications:
                Intent notificationsIntent = new Intent(this, NotificationsActivity.class);
                startActivity(notificationsIntent);
                break;
        }

        //CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)fab.getLayoutParams();
        //new CoordinatorLayout.LayoutParams(fab.getWidth(), fab.getHeight());//
    }

    //endregion

    //region MAP AND MARKERS METHODS

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (this.googleMap != null) {
            isMapLoaded = true;
        } else {
            myMarkers = new HashMap<>();
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setOnMyLocationChangeListener(this);
        googleMap.setInfoWindowAdapter(new MapMarkerInfoWindowAdapter(getLayoutInflater()));
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        StartLoadingForMarkers();

        if (btn_focus_on_my_location != null && googleMap != null)
            btn_focus_on_my_location.setVisibility(View.VISIBLE);
        hsv_gallery.setVisibility(View.VISIBLE);

        if (progressDialog != null)
            progressDialog.dismiss();

        SetCamera();
    }

    private Marker AddMarker(float latitude, float longtitude, String title, BitmapDescriptor icon) {
        MarkerOptions newMarker = new MarkerOptions().position(new LatLng(latitude, longtitude)).title(title).draggable(false);
        if (icon != null)
            newMarker.icon(icon);
        return googleMap.addMarker(newMarker);
    }

    private void AnimateCameraFocusOnLatLng(LatLng latLng) {
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        //CameraUpdateFactory.newLatLngBounds(CommonUtil.GetBoundsByCenterLatLng(latLng, maxDistance), width, height, 0);
        googleMap.animateCamera(cu);
    }

    private void SetCamera() {
        if (myLocation == null) {
            Log.i(MY_TAG, "SetCamera starts getting location");
            StartGetMyLocation();
        } else {
            OnReadyToUpdateCamera();
        }
    }

    private void OnReadyToUpdateCamera() {
        if (myMarkers != null && myMarkers.size() != 0) {
            double latitude = 0;
            double longtitude = 0;
            int counter = 0;
            if (myLocation != null) {
                if (average != null && CommonUtil.GetDistance(average, myLocation) < maxDistance)
                    return;

                latitude += myLocation.latitude;
                longtitude += myLocation.longitude;
                counter++;
                maxDistance = getResources().getInteger(R.integer.map_max_distance_if_location_available) * kilometer_for_map;
            } else {
                for (Marker m : myMarkers.keySet()) {
                    latitude += m.getPosition().latitude;
                    longtitude += m.getPosition().longitude;
                    counter++;
                }
                maxDistance = getResources().getInteger(R.integer.map_max_distance_if_location_not_available) * kilometer_for_map;
            }

            average = new LatLng(latitude / counter, longtitude / counter);
            Log.i(MY_TAG, "center coordinades: " + average.latitude + ":" + average.longitude);

/*
            if (myLocation != null && GetDistance(average, myLocation) < maxDistance)
                maxDistance = GetDistance(average, myLocation);
*/

/*
            for (Marker m : myMarkers.keySet()) {
                if (GetDistance(average, m.getPosition()) > maxDistance)
                    maxDistance = GetDistance(average, m.getPosition());
            }
*/

        } else {
            average = myLocation;
            Log.i(MY_TAG, "center coordinades (by my location): " + average.latitude + ":" + average.longitude);
        }
        AnimateCameraFocusOnLatLng(average);
    }

    @Override
    public void onMyLocationChange(Location location) {
        Log.i(MY_TAG, "got location update from map");
        if (location == null)
            return;
        if (lastLocationUpdateDate == null)
            lastLocationUpdateDate = new Date();
        else {
            long millisPassed = new Date().getTime() - lastLocationUpdateDate.getTime();
            if (millisPassed < myLocationRefreshRate) {
                Log.i(MY_TAG, millisPassed + " after last update, not updating");
                return;
            } else {
                Log.i(MY_TAG, "updating location! lat: " + location.getLatitude()
                        + "; long: " + location.getLongitude());
                lastLocationUpdateDate = new Date();
            }
        }
        if (myLocation != null && myLocation.latitude == location.getLatitude() && myLocation.longitude == location.getLongitude())
            return;
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        SetCamera();
        UpdateMyLocationPreferences(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void OnGotMyLocationCallback(Location location) {
        Log.i(MY_TAG, "got location callback from task");
        if (location != null)
            myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (isMapLoaded)
            OnReadyToUpdateCamera();
    }

    public void ResetMarkers() {
        if (myMarkers == null)
            myMarkers = new HashMap<>();
        else {
            for (Marker m : myMarkers.keySet())
                m.remove();
            myMarkers.clear();
        }
    }

    public void SetMarkers(ArrayList<FCPublication> publications) {
        for (FCPublication publication : publications) {
            Bitmap markerIcon;
            BitmapDescriptor icon = null;

            markerIcon = CommonUtil.decodeScaledBitmapFromDrawableResource(
                    getResources(), R.drawable.map_marker, 13, 13);
            icon = BitmapDescriptorFactory.fromBitmap(markerIcon);

            if (isMapLoaded)
                myMarkers.put(AddMarker(publication.getLatitude().floatValue(),
                        publication.getLongitude().floatValue(),
                        publication.getTitle(), icon), publication.getUniqueId());

            AddImageToGallery(publication);
        }

        if (isMapLoaded)
            SetCamera();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    private void showNotifyTab(int TAB) {
        //viewPager.setCurrentItem(TAB);
        //todo: implement
    }

    //endregion MAP AND REGIONS METHODS

    //region PUBS GALLERY

    public void AddImageToGallery(final FCPublication publication) {

        int screenLayout = this.getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;

        int size = getResources().getDimensionPixelSize(R.dimen.gallery_image_btn_height);
        ImageButton imageButton = new ImageButton(getApplicationContext());
        imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.setMargins(15, 30, 15, 30);

        if (screenLayout == Configuration.SCREENLAYOUT_SIZE_SMALL) lp.setMargins(5, 10, 5, 10);

        imageButton.setLayoutParams(lp);
        imageButton.setBackgroundResource(R.drawable.map_gallery_border);

        SetPublicationImage(publication, imageButton);

/*
        Drawable drawable
                = CommonUtil.GetBitmapDrawableFromFile(
                publication.GetImageFileName(), getString(R.string.image_folder_path), size, size);
        if (drawable == null)
            drawable = getResources().getDrawable(R.drawable.foodonet_logo_200_200);
        imageButton.setImageDrawable(drawable);
*/
        imageButton.setScaleType(ImageView.ScaleType.FIT_XY);
        imageButton.setOnClickListener(new View.OnClickListener() {
            int id = publication.getUniqueId();

            @Override
            public void onClick(View v) {
                ImageBtnFromGallerySelected(id);
                CommonUtil.PostGoogleAnalyticsUIEvent(getApplicationContext(), "Map and list", "Gallery item", "item pressed");
            }
        });
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.drawable.map_my_location_button_pressed));
        states.addState(new int[]{}, getResources().getDrawable(R.drawable.map_my_location_button_normal));
        imageButton.setBackground(states);
        gallery_pubs.addView(imageButton);
    }

    public void ImageBtnFromGallerySelected(int id) {
        for (Map.Entry<Marker, Integer> e : myMarkers.entrySet()) {
            if (e.getValue().intValue() == id) {
                AnimateCameraFocusOnLatLng(e.getKey().getPosition());
                e.getKey().showInfoWindow();
            }
        }
        //Toast.makeText(this, "selected image id: " + String.valueOf(id), Toast.LENGTH_SHORT).show();
    }

    private void SetPublicationImage(FCPublication publication, ImageView publicationImage) {
        final int id = publication.getUniqueId();
        final int version = publication.getVersion();
        Drawable imageDrawable;
        imageDrawable = imageDictionary.Get(id);
        if (imageDrawable == null) {
            imageDownloader.Download(id, version, publicationImage);
        } else
            publicationImage.setImageDrawable(imageDrawable);
    }

    private void SetGalleryAndMarkers(ArrayList<FCPublication> publications) {
        //gallery_pubs.setVisibility(View.GONE);
        gallery_pubs.removeAllViews();
        ResetMarkers();
        SetMarkers(publications);
        //gallery_pubs.setVisibility(View.VISIBLE);
    }

    //endregion PUBS GALLERY

    //region loading data

    private void StartLoadingForMarkers() {
        getSupportLoaderManager().initLoader(-1, null, this);
    }

    private void RestartLoadingForMarkers() {
        getSupportLoaderManager().restartLoader(-1, null, this);
    }

    private void StartLoadingForPublicationsList() {
        getSupportLoaderManager().initLoader(currentFilterID, null, this);
    }

    private void RestartLoadingForPublicationsList() {
        getSupportLoaderManager().restartLoader(currentFilterID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        android.support.v4.content.CursorLoader cursorLoader = null;
        String[] projection;
        switch (id) {
            case -1:
                projection = FCPublication.GetColumnNamesArray();
                cursorLoader = new android.support.v4.content.CursorLoader(this, FooDoNetSQLProvider.URI_GET_PUBS_FOR_MAP_MARKERS,
                        projection, null, null, null);
                break;
            default:
                projection = FCPublication.GetColumnNamesForListArray();
                cursorLoader = new android.support.v4.content.CursorLoader(
                        this, Uri.parse(FooDoNetSQLProvider.URI_GET_PUBS_FOR_LIST_BY_FILTER_ID + "/" + id),
                        projection, null, null, null);
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArrayList<FCPublication> publications = null;
        if (data != null) {
            publications = loader.getId() == -1
                    ? FCPublication.GetArrayListOfPublicationsForMapFromCursor(data)
                    : FCPublication.GetArrayListOfPublicationsFromCursor(data, true);
            if (publications == null || publications.size() == 0) {
                Log.e(MY_TAG, "no publications got from sql");
                adapter.UpdatePublicationsList(new ArrayList<FCPublication>(), currentListMode == LIST_MODE_MY);
            }
            switch (loader.getId()) {
                case -1:
                    SetGalleryAndMarkers(publications);
                    break;
                case FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_CLOSEST:
                case FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_LESS_REGS:
                case FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_NEWEST:
                case FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_TEXT_FILTER:
                    SetPublicationsListToAdapter(publications, false);
                    break;
                case FooDoNetSQLHelper.FILTER_ID_LIST_MY_ACTIVE_ID_DESC:
                case FooDoNetSQLHelper.FILTER_ID_LIST_MY_BY_ENDING_SOON:
                case FooDoNetSQLHelper.FILTER_ID_LIST_MY_BY_TEXT_FILTER:
                case FooDoNetSQLHelper.FILTER_ID_LIST_MY_NOT_ACTIVE_ID_ASC:
                    SetPublicationsListToAdapter(publications, true);
                    break;
                default:
                    break;
            }
        }
        if(progressDialog != null)
            progressDialog.dismiss();
    }

    //endregion

    //region Navigation and toolbar

    private void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.tb_map_and_list);
        if (toolbar != null) //toolbar.setTitle(R.string.app_name);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getTitle().toString().compareToIgnoreCase("list") == 0) {
                        switch (ll_map_and_gallery.getVisibility()) {
                            case View.VISIBLE:
                                currentMode = MODE_LIST;
                                tv_toolbar_label.setText(currentListMode == LIST_MODE_ALL
                                        ? R.string.all_shares_toolbar_title
                                        : R.string.my_shares_toolbar_title);
                                SetupMode();
                                item.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.map_menu_icon));
                                break;
                            case View.GONE:
                                currentMode = MODE_MAP;
                                tv_toolbar_label.setText(R.string.toolbar_title_map);
                                SetupMode();
                                item.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.list_xxh));
                                break;
                        }
                    }
                    return true;
                }
            });

        toolbar.inflateMenu(R.menu.menu);
    }

    private void SetFrameMap() {
        ll_map_and_gallery.setVisibility(View.VISIBLE);
        hsv_gallery.setVisibility(View.VISIBLE);
        tl_list_filter_buttons.setVisibility(View.GONE);
        //rv_all_publications_list.setVisibility(View.GONE);
        fl_search_and_list.setVisibility(View.GONE);

        fab.setLayoutParams(fabLayoutParams);
    }

    private void SetFrameList() {
        ll_map_and_gallery.setVisibility(View.GONE);
        tl_list_filter_buttons.setVisibility(View.VISIBLE);
        //rv_all_publications_list.setVisibility(View.VISIBLE);
        fl_search_and_list.setVisibility(View.VISIBLE);
        StartLoadingForPublicationsList();

        CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(fabLayoutParams);
        layoutParams.setAnchorId(View.NO_ID);
        layoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        layoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        layoutParams.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        fab.setLayoutParams(layoutParams);
    }

    private void initNavVew() {
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_main);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                isSideMenuOpened = false;
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                et_search.clearFocus();
                CommonUtil.HideSoftKeyboard(getActivity());
                isSideMenuOpened = true;
                super.onDrawerOpened(drawerView);
                CommonUtil.PostGoogleAnalyticsUIEvent(getApplicationContext(), "Map and list", "Side menu", "Open menu");
            }
        };

        if (drawerLayout != null) drawerLayout.addDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();


        //NavigationView navView = (NavigationView) findViewById(R.id.nv_main);
        //View navHeader = navView.getHeaderView(0);
        riv_user_portrait = (RoundedImageView) findViewById(R.id.riv_nav_header_user_portrait);
        tv_user_name = (TextView) findViewById(R.id.tv_nav_header_user_name);
        tv_user_email = (TextView) findViewById(R.id.tv_nav_header_user_email);
        SetupSideMenuHeader();

        btn_nav_menu_my_pubs = (RelativeLayout) findViewById(R.id.rl_btn_my_publications_list);
        btn_nav_menu_my_pubs.setOnClickListener(this);
        btn_nav_menu_subscriptions = (RelativeLayout) findViewById(R.id.rl_btn_subscriptions);
        btn_nav_menu_subscriptions.setOnClickListener(this);
        btn_nav_menu_groups = (RelativeLayout) findViewById(R.id.rl_btn_groups);
        btn_nav_menu_groups.setOnClickListener(this);
        btn_nav_menu_settings = (RelativeLayout) findViewById(R.id.rl_btn_settings);
        btn_nav_menu_settings.setOnClickListener(this);
        btn_nav_menu_contact_us = (RelativeLayout) findViewById(R.id.rl_btn_contact_us);
        btn_nav_menu_contact_us.setOnClickListener(this);
        btn_nav_menu_terms = (RelativeLayout) findViewById(R.id.rl_btn_terms_and_conditions);
        btn_nav_menu_terms.setOnClickListener(this);
        btn_nav_menu_notifications = (RelativeLayout)findViewById(R.id.rl_btn_notifications);
        btn_nav_menu_notifications.setOnClickListener(this);

        tv_side_menu_list_mode = (TextView) findViewById(R.id.tv_side_menu_list_mode);

//        Menu menuNav = v.getMenu();
//        menuNav.clear();
//        menuNav.add(0,0,0,"mySharings");
//        MenuItem menuItemMyList = menuNav.findItem(0);
//        View view = View.inflate(this, R.layout.nav_menu_item, null);
//        menuItemMyList.setActionView(view);
        //MenuItemCompat.setActionView(menuItemMyList, view);

        //View view1 = MenuItemCompat.getActionView(menuItemMyList);

/*
        if (navView != null) {
            navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    drawerLayout.closeDrawers();

                    switch (item.getItemId()) {
                        case R.id.nav_item_sharings:
                            Intent intent = new Intent(getApplicationContext(), MyPublicationsActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.nav_item_subscriptions:

                            break;
                        case R.id.nav_item_groups:
                            Intent intentGroups = new Intent(getApplicationContext(), GroupsListActivity.class);
                            startActivity(intentGroups);
                            break;
                        case R.id.nav_item_settings:

                            break;

                        case R.id.nav_item_contact_us:

                            break;

                        case R.id.nav_item_terms:

                            break;

                    }
                    return true;
                }
            });
        }
*/
    }

    //endregion

    //region All publications list

    private void SetupSideMenuHeader(){
        riv_user_portrait.setImageDrawable(
                CommonUtil.GetBitmapDrawableFromFile(getString(R.string.user_avatar_file_name),
                        getString(R.string.image_folder_path), 90, 90));
        tv_user_name.setText(CommonUtil.GetMyUserNameFromPreferences(this));
        tv_user_email.setText(CommonUtil.GetMyEmailFromPreferences(this));
    }

    private void SetupRecyclerViewPublications() {
        rv_all_publications_list.setLayoutManager(new LinearLayoutManager(rv_all_publications_list.getContext()));
        adapter = new AllPublicationsListRecyclerViewAdapter(this, new ArrayList<FCPublication>(), this);
        rv_all_publications_list.setAdapter(adapter);
        rv_all_publications_list.addOnScrollListener(new HidingScrollListener(this) {
            @Override
            public void onMoved(int distance) {
                tb_search.setTranslationY(-distance);
            }

            @Override
            public void onShow() {
                tb_search.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            }

            @Override
            public void onHide() {
                tb_search.animate().translationY(-mToolbarHeight).setInterpolator(new AccelerateInterpolator(2)).start();
            }
        });
    }

    private void SetPublicationsListToAdapter(ArrayList<FCPublication> publications, boolean isMine) {
        if (adapter != null) {
            adapter.UpdatePublicationsList(publications, isMine);
        }
    }

    @Override
    public void NotifyToBListenerAboutEvent(int eventCode) {

    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        switch (request.ActionCommand) {
            case InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID:
                FCPublication loadedPublication = request.publicationForDetails;
                Intent intent = new Intent(this, ExistingPublicationActivity.class);
                intent.putExtra(ExistingPublicationActivity.PUBLICATION_EXTRA_KEY, loadedPublication);
                startActivityForResult(intent, REQUEST_CODE_PUBLICATION_DETAILS);
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                break;
            default:
                Log.w(MY_TAG, "unexpected sql async task result!");
                break;
        }
    }

    public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {
        private static final float HIDE_THRESHOLD = 10;
        private static final float SHOW_THRESHOLD = 70;
        private int mToolbarOffset = 0;
        private boolean mControlsVisible = true;
        public int mToolbarHeight;

        public HidingScrollListener(Context context) {
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                mToolbarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (mControlsVisible) {
                    if (mToolbarOffset > HIDE_THRESHOLD) {
                        setInvisible();
                    } else {
                        setVisible();
                    }
                } else {
                    if ((mToolbarHeight - mToolbarOffset) > SHOW_THRESHOLD) {
                        setVisible();
                    } else {
                        setInvisible();
                    }
                }
            }
        }

        private void setVisible() {
            if (mToolbarOffset > 0) {
                onShow();
                mToolbarOffset = 0;
            }
            mControlsVisible = true;
        }

        private void setInvisible() {
            if (mToolbarOffset < mToolbarHeight) {
                onHide();
                mToolbarOffset = mToolbarHeight;
            }
            mControlsVisible = false;
        }


        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            clipToolbarOffset();
            onMoved(mToolbarOffset);

            if ((mToolbarOffset < mToolbarHeight && dy > 0) || (mToolbarOffset > 0 && dy < 0)) {
                mToolbarOffset += dy;
            }
        }

        private void clipToolbarOffset() {
            if (mToolbarOffset > mToolbarHeight) {
                mToolbarOffset = mToolbarHeight;
            } else if (mToolbarOffset < 0) {
                mToolbarOffset = 0;
            }
        }

        public abstract void onMoved(int distance);

        public abstract void onShow();

        public abstract void onHide();
    }

    //endregion

    //region callback methods


    @Override
    public void onBroadcastReceived(Intent intent) {
        super.onBroadcastReceived(intent);
        int actionCode = intent.getIntExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, -1);
        switch (actionCode) {
//            case ServicesBroadcastReceiver.ACTION_CODE_GET_LOCATION_SUCCESS:
//                Location location = (Location) intent.getParcelableExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_LOCATION_KEY);
//                if (location == null) {
//                    Log.e(MY_TAG, "got null location extra from broadcast");
//                    return;
//                }
//                break;
//            case ServicesBroadcastReceiver.ACTION_CODE_GET_LOCATION_FAIL:
//                break;
//            case ServicesBroadcastReceiver.ACTION_CODE_SAVE_NEW_PUB_FAIL:
//                Toast.makeText(this, getString(R.string.failed_to_save_new_pub), Toast.LENGTH_SHORT).show();
//                CommonUtil.PostGoogleAnalyticsUIEvent(getApplicationContext(),
//                        "my publications list", "save pub btn", "saving new pub fail");
            case ServicesBroadcastReceiver.ACTION_CODE_SAVE_NEW_PUB_SUCCESS:
            case ServicesBroadcastReceiver.ACTION_CODE_SAVE_NEW_PUB_SQL_SUCCESS:
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            case ServicesBroadcastReceiver.ACTION_CODE_NOTIFICATION_RECEIVED_PUBLICATION_DELETED:
            case ServicesBroadcastReceiver.ACTION_CODE_NOTIFICATION_RECEIVED_NEW_PUBLICATION:
            default:
                if (adapter != null) {
                    RestartLoadingForPublicationsList();
                }
                if (isMapLoaded) {
                    RestartLoadingForMarkers();
                }
                break;
        }
    }

    @Override
    public void OnPublicationFromListClicked(int publicationID) {
        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_loading_publication));
        FooDoNetSQLExecuterAsync sqlGetPubAsync = new FooDoNetSQLExecuterAsync(this, this);
        InternalRequest ir = new InternalRequest(InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID);
        ir.PublicationID = publicationID;
        sqlGetPubAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
    }

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

    //endregion

    //region Tab filter buttons

//    private void SetupFilterTabButtons() {
//        tl_list_filter_buttons.addTab(tl_list_filter_buttons.newTab().setText(getString(R.string.filter_closest_btn_text)), 0);
//        tl_list_filter_buttons.addTab(tl_list_filter_buttons.newTab().setText(getString(R.string.filter_new_btn_text)), 1);
//        tl_list_filter_buttons.addTab(tl_list_filter_buttons.newTab().setText(getString(R.string.filter_all_btn_text)), 2);
//
//
////        tab_all_closest = tl_list_filter_buttons.newTab().setText(getString(R.string.filter_closest_btn_text));
////        tl_list_filter_buttons.addTab(tab_all_closest, 0);
////        tab_all_new = tl_list_filter_buttons.newTab().setText(getString(R.string.filter_new_btn_text));
////        tl_list_filter_buttons.addTab(tab_all_new, 1);
////        tab_all_all = tl_list_filter_buttons.newTab().setText(getString(R.string.filter_all_btn_text));
////        tl_list_filter_buttons.addTab(tab_all_all, 2);
////        tab_my_all = tl_list_filter_buttons.newTab().setText(getString(R.string.filter_all_my_pubs));
////        tl_list_filter_buttons.addTab(tab_my_all, 3);
////        tab_my_active = tl_list_filter_buttons.newTab().setText(getString(R.string.filter_active_my_pubs));
////        tl_list_filter_buttons.addTab(tab_my_active, 4);
////        tab_my_ended = tl_list_filter_buttons.newTab().setText(getString(R.string.filter_ended_my_pubs));
////        tl_list_filter_buttons.addTab(tab_my_ended, 5);
//
//        tl_list_filter_buttons.setOnTabSelectedListener(this);
//    }

    public void SetTabsVisibility(int listMode) {
        tl_list_filter_buttons.setOnTabSelectedListener(null);
        while (tl_list_filter_buttons.getTabCount() > 0) {
            tl_list_filter_buttons.removeTabAt(0);
        }

        switch (listMode) {
            case LIST_MODE_ALL:
                tv_toolbar_label.setText(R.string.all_shares_toolbar_title);
                tl_list_filter_buttons.addTab(tl_list_filter_buttons.newTab()
                        .setText(getString(R.string.filter_closest_btn_text))
                        .setTag(FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_CLOSEST));
                tl_list_filter_buttons.addTab(tl_list_filter_buttons.newTab()
                        .setText(getString(R.string.filter_new_btn_text))
                        .setTag(FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_NEWEST));
                tl_list_filter_buttons.addTab(tl_list_filter_buttons.newTab()
                        .setText(getString(R.string.filter_all_btn_text))
                        .setTag(FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_LESS_REGS));
                break;
            case LIST_MODE_MY:
                tv_toolbar_label.setText(R.string.my_shares_toolbar_title);
                tl_list_filter_buttons.addTab(tl_list_filter_buttons.newTab()
                        .setText(getString(R.string.filter_active_my_pubs))
                        .setTag(FooDoNetSQLHelper.FILTER_ID_LIST_MY_ACTIVE_ID_DESC));
                tl_list_filter_buttons.addTab(tl_list_filter_buttons.newTab()
                        .setText(getString(R.string.filter_ended_my_pubs))
                        .setTag(FooDoNetSQLHelper.FILTER_ID_LIST_MY_NOT_ACTIVE_ID_ASC));
                tl_list_filter_buttons.addTab(tl_list_filter_buttons.newTab()
                        .setText(getString(R.string.filter_all_my_pubs))
                        .setTag(FooDoNetSQLHelper.FILTER_ID_LIST_MY_BY_ENDING_SOON));
                break;
        }
        tl_list_filter_buttons.setOnTabSelectedListener(this);

//        tab_all_all.getCustomView().setVisibility(mode == LIST_MODE_ALL? View.VISIBLE: View.GONE);
//        tab_all_new.getCustomView().setVisibility(mode == LIST_MODE_ALL? View.VISIBLE: View.GONE);
//        tab_all_closest.getCustomView().setVisibility(mode == LIST_MODE_ALL? View.VISIBLE: View.GONE);
//        tab_my_ended.getCustomView().setVisibility(mode == LIST_MODE_ALL? View.GONE: View.VISIBLE);
//        tab_my_active.getCustomView().setVisibility(mode == LIST_MODE_ALL? View.GONE: View.VISIBLE);
//        tab_my_all.getCustomView().setVisibility(mode == LIST_MODE_ALL? View.GONE: View.VISIBLE);

//        tl_list_filter_buttons.getChildAt(0).setVisibility(mode == LIST_MODE_ALL? View.VISIBLE: View.GONE);
//        tl_list_filter_buttons.getChildAt(1).setVisibility(mode == LIST_MODE_ALL? View.VISIBLE: View.GONE);
//        tl_list_filter_buttons.getChildAt(2).setVisibility(mode == LIST_MODE_ALL? View.VISIBLE: View.GONE);
//        tl_list_filter_buttons.getChildAt(3).setVisibility(mode == LIST_MODE_ALL? View.GONE: View.VISIBLE);
//        tl_list_filter_buttons.getChildAt(4).setVisibility(mode == LIST_MODE_ALL? View.GONE: View.VISIBLE);
//        tl_list_filter_buttons.getChildAt(5).setVisibility(mode == LIST_MODE_ALL? View.GONE: View.VISIBLE);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
//        switch (tab.getPosition()) {
//            case 0:
//                currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_CLOSEST;
//                break;
//            case 1:
//                currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_NEWEST;
//                break;
//            case 2:
//                currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_LESS_REGS;
//                break;
//            case 3:
//                currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_MY_BY_ENDING_SOON;
//                break;
//            case 4:
//                currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_MY_ACTIVE_ID_DESC;
//                break;
//            case 5:
//                currentFilterID = FooDoNetSQLHelper.FILTER_ID_LIST_MY_NOT_ACTIVE_ID_ASC;
//                break;        }
        if (et_search != null) {
            et_search.clearFocus();
            CommonUtil.HideSoftKeyboard(this);
        }


        if (tab.getTag() == null || !(tab.getTag() instanceof Integer))
                //|| currentFilterID == (int)tab.getTag())
            return;

        currentFilterID = (int) tab.getTag();
        if(progressDialog != null)
            progressDialog.dismiss();
        progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_load));
        RestartLoadingForPublicationsList();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        boolean isRestart = !TextUtils.isEmpty(et_search.getText().toString())
                && currentFilterID == FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_TEXT_FILTER;
        if (TextUtils.isEmpty(et_search.getText().toString())) {
            currentFilterID = (currentListMode == LIST_MODE_ALL)
                    ? FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_CLOSEST
                    : FooDoNetSQLHelper.FILTER_ID_LIST_MY_ACTIVE_ID_DESC;
        } else {
            FooDoNetCustomActivityConnectedToService.UpdateFilterTextPreferences(this, et_search.getText().toString());
            currentFilterID = (currentListMode == LIST_MODE_ALL)
                    ? FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_TEXT_FILTER
                    : FooDoNetSQLHelper.FILTER_ID_LIST_MY_BY_TEXT_FILTER;
        }
        if (isRestart)
            RestartLoadingForPublicationsList();
        else
            StartLoadingForPublicationsList();
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().length() > 0) {
            et_search.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            //Assign your image again to the view, otherwise it will always be gone even if the text is 0 again.
            et_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.toolbar_find, 0, 0, 0);
        }
    }

    //endregion

    //region Registration and after registration

    @Override
    public void YesRegisterNow(int code) {
        if (code == DO_AFTER_REGISTRATION_CODE_NOTHING) return;
        Intent signInIntent = new Intent(this, SignInActivity.class);
        startActivityForResult(signInIntent, code);
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        if (response == null) return;
        switch (response.ActionCommand){
            case InternalRequest.ACTION_POST_NEW_USER:
                switch (response.Status) {
                    case InternalRequest.STATUS_OK:
                        if (response.newUserID > 0) {
                            CommonUtil.SaveMyUserID(this, response.newUserID);
                            File avatarFile = new File(Environment.getExternalStorageDirectory()
                                    + getResources().getString(R.string.image_folder_path), getString(R.string.user_avatar_file_name));
                            if (avatarFile.exists()) {
                                AmazonImageUploader uploader = new AmazonImageUploader(this, this);
                                uploader.UploadUserAvatarToAmazon(avatarFile);
                            }
                        }
                        switch (response.DoAfterRegistrationActionID) {
                            case DO_AFTER_REGISTRATION_CODE_ADD_PUBLICATION:
                                Intent addPub = new Intent(this, AddEditPublicationActivity.class);
                                startActivityForResult(addPub, REQUEST_CODE_NEW_PUB);
                                break;
                            case DO_AFTER_REGISTRATION_CODE_GROUPS:
                                Intent intentGroups = new Intent(getApplicationContext(), GroupsListActivity.class);
                                startActivity(intentGroups);
                                break;
                            case DO_AFTER_REGISTRATION_CODE_SETTING:
                                Intent intentSettings = new Intent(this, SettingsSelectActivity.class);
                                startActivityForResult(intentSettings, REQUEST_CODE_SETTINGS);
                                break;
                            case DO_AFTER_REGISTRATION_CODE_MY_PUBS:
                                onClick(btn_nav_menu_my_pubs);
                                break;
                            case DO_AFTER_REGISTRATION_CODE_NOTHING:
                                break;
                        }
                        break;
                    case InternalRequest.STATUS_FAIL:
                        CommonUtil.ClearUserDataOnLogOut(this, true);
                        SetupSideMenuHeader();
                        Snackbar.make(fab, getString(R.string.failed_to_login_try_later), Snackbar.LENGTH_SHORT).show();
                        break;
                }
                break;
            case InternalRequest.ACTION_POST_FEEDBACK:
                if(progressDialog != null)
                    progressDialog.dismiss();
                break;
        }
    }

    //endregion

/*
    class FrameSwitchFABBehavior extends CoordinatorLayout.Behavior<FloatingActionButton>{
        Context context;

        public FrameSwitchFABBehavior(Context context, AttributeSet attrs){
            super();
            this.context = context;
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent,
                                       FloatingActionButton child, View dependency) {
            // We're dependent on all SnackbarLayouts (if enabled)
            return dependency instanceof Snackbar.SnackbarLayout || dependency instanceof LinearLayout;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency){
            return true;
        }
    }
*/
}
