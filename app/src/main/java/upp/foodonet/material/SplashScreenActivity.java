package upp.foodonet.material;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmPubSub;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.GetMyLocationAsync;
import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import FooDoNetServerClasses.DownloadImageTask;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;


public class SplashScreenActivity
        extends FooDoNetCustomActivityConnectedToService
        implements IFooDoNetServerCallback {

    boolean flagWaitTaskFinished, registerTaskFinished;

    private final String MY_TAG = "food_splashscreen";

    public static final int REQUEST_CODE_ASK_PERMISSION = 10;

//    private boolean isGoogleFacebookChecked;
//    private boolean isLoadDataServiceStarted = false;

    private boolean AllLoaded() {
        return registerTaskFinished && flagWaitTaskFinished;
    }

    //controls
    TextView tv_load_status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        tv_load_status = (TextView) findViewById(R.id.tv_progress_text_splash_screen);
        tv_load_status.setText(R.string.progress_load);
        flagWaitTaskFinished = true;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE_ASK_PERMISSION);
        }
        else ContinueAfterGettingPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ContinueAfterGettingPermissions();
    }

    private void GetLocationFirstTime(){
        GetMyLocationAsync locationTask
                = new GetMyLocationAsync((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), this);
        locationTask.switchToReportLocationMode(false);
        locationTask.execute();
    }

    private void CheckIfPhoneRegisteredOnFoodonet() {
        if (CommonUtil.GetFromPreferencesIsDeviceRegistered(this)) {
            RegisterToGoogleFacebookIfNeeded();
        } else {
            File directory = new File(Environment.getExternalStorageDirectory()
                    + getResources().getString(R.string.image_folder_path));
            if(directory.exists() && !CommonUtil.GetFromPreferencesIsRegisteredToGoogleFacebook(this))
                if(directory.isDirectory()){
                    String[] children = directory.list();
                    for(int i = 0; i < children.length; i++)
                        new File(directory, children[i]).delete();
                }
            if (!directory.exists()) {
                directory.mkdirs();
            }
            FooDoNetInstanceIDListenerService.StartRegisterToGCM(this);
        }
    }

    private void ContinueAfterGettingPermissions(){
        CheckIfPhoneRegisteredOnFoodonet();
        GetLocationFirstTime();
    }

    private void RegisterToGoogleFacebookIfNeeded() {
//        if (CommonUtil.GetFromPreferencesIsRegisteredToGoogleFacebook(this)) {
        registerTaskFinished = true;
        RepairPushNotifications();
        startService(new Intent(this, FooDoNetService.class));
        if (CommonUtil.GetFromPreferencesIsDataLoaded(this)) {
            if (AllLoaded())
                StartNextActivity();
        } else {
            tv_load_status.setText(R.string.progress_first_load);
        }
//        } else {
//            Intent signInIntent = new Intent(this , SignInActivity.class);
//            startActivityForResult(signInIntent, 0);
//        }
    }

    private void StartNextActivity() {
        // and start next activity
        Intent intent = new Intent(this, EntarenceMapAndListActivity.class);
        this.startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //isGoogleFacebookChecked = true;
        switch (resultCode) {
//            case 1:
//                InternalRequest ir = (InternalRequest) data.getSerializableExtra(InternalRequest.INTERNAL_REQUEST_EXTRA_KEY);
//                if (ir != null) {
////                    if(ir.PhotoURL != null){
////                        DownloadImageTask imageTask = new DownloadImageTask(ir.PhotoURL, 100, getString(R.string.image_folder_path));
////                        imageTask.execute();
////                    }
//                    HttpServerConnectorAsync connectorAsync
//                            = new HttpServerConnectorAsync(getString(R.string.server_base_url), (IFooDoNetServerCallback) this);
//                    connectorAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
//                    return;
//                }
//                Log.e(MY_TAG, "InternalRequest extra null");
//                break;
//            case REQUEST_CODE_ASK_PERMISSION:
//                ContinueAfterGettingPermissions();
//                break;
            default:
                break;
        }
    }

    @Override
    public void onBroadcastReceived(Intent intent) {
        int regResult = 0;
        regResult = intent.getIntExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, 0);
        switch (regResult) {
            case ServicesBroadcastReceiver.ACTION_CODE_REGISTRATION_FAIL:
                Toast.makeText(getBaseContext(), "problem registering device!", Toast.LENGTH_LONG).show();
                // return;
            case ServicesBroadcastReceiver.ACTION_CODE_REGISTRATION_SUCCESS:
                // if register succeed
                // check registration google/
                RegisterToGoogleFacebookIfNeeded();
                break;
            case ServicesBroadcastReceiver.ACTION_CODE_RELOAD_DATA_SUCCESS:
                registerTaskFinished = true;
                if (AllLoaded())
                    StartNextActivity();
                break;
        }
    }

    private void RepairPushNotifications() {
        SharedPreferences sp = getSharedPreferences(getString(R.string.shared_preferences_token), MODE_PRIVATE);
        final String token = sp.getString(getString(R.string.shared_preferences_token_key), "");
        if (!TextUtils.isEmpty(token)) {
            final Context ctx = this;
            new AsyncTask<Void,Void,Void>(){
                @Override
                protected Void doInBackground(Void... voids) {
                    GcmPubSub pubSub = GcmPubSub.getInstance(ctx);
                    try {
                        pubSub.unsubscribe(token, "/topics/global");
                        pubSub.subscribe(token, getString(R.string.push_notification_prefix), null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        }
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        startService(new Intent(this, FooDoNetService.class));
        //isLoadDataServiceStarted = true;
        tv_load_status.setText(getString(R.string.progress_first_load));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }
}
