package upp.foodonet.material;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.IPleaseRegisterDialogCallback;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;

public class SettingsSelectActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener, AlertDialog.OnClickListener {

    private static final String MY_TAG = "food_settSelect";

    public static final int RESULT_CODE_LOGOUT_MADE = 100;

    Button btn_profile_settings;
    Button btn_settings_notifications;
    Button btn_logout;

    AlertDialog dialogIfLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_settings_select);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        btn_profile_settings = (Button) findViewById(R.id.btn_profile_settings);
        btn_profile_settings.setOnClickListener(this);

        btn_settings_notifications = (Button) findViewById(R.id.btn_notifications_settings);
        btn_settings_notifications.setOnClickListener(this);

        btn_logout = (Button) findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(this);


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_profile_settings:
                Intent intent = new Intent(this, ProfileViewAndEditActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_notifications_settings:
                Intent intentNo = new Intent(this, NotificationSettings.class);
                startActivity(intentNo);
                break;
            case R.id.btn_logout:
                ShowDialogAskIfSureToLogout();
                break;
            default:
                break;
        }
    }

    public void ShowDialogAskIfSureToLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.settings_ask_if_sure_to_logout));
        String positiveText = getString(R.string.yes);
        builder.setPositiveButton(positiveText, this);
        String negativeText = getString(R.string.no);
        builder.setNegativeButton(negativeText, this);
        dialogIfLogout = builder.create();
        dialogIfLogout.show();
    }


    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case AlertDialog.BUTTON_POSITIVE:
                ProgressDialog pDialogLogout = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_logout));
                CommonUtil.ClearUserDataOnLogOut(this, false);
                pDialogLogout.dismiss();
                dialog.dismiss();
                setResult(RESULT_CODE_LOGOUT_MADE);
                finish();
                break;
            case AlertDialog.BUTTON_NEGATIVE:
                dialog.dismiss();
                break;
        }
    }
}
