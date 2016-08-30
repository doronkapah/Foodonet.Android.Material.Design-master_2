package FooDoNetServiceUtil;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;

import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.GetMyLocationAsync;
import FooDoNetServerClasses.ConnectionDetector;
//import com.nikoxes.foodonetwmd.FooDoNetService;
import upp.foodonet.material.R;

/**
 * Created by Asher on 31-Jul-15.
 */
public abstract class FooDoNetCustomActivityConnectedToService
        extends AppCompatActivity
        implements IBroadcastReceiverCallback {
    //implements IFooDoNetServiceCallback, IGotMyLocationCallback {

    //FooDoNetService fooDoNetService;
    //boolean isBoundedToService;
    //protected Messenger boundedService;
    //private static boolean isServiceRunning;
    //protected Intent serviceIntent;

    ServicesBroadcastReceiver servicesBroadcastReceiver;
    protected ProgressDialog progressDialog;

    protected boolean isInternetAvailable = false;
    protected boolean isGoogleServiceAvailable = false;

    private final String MY_TAG = "food_abstract_fActivity";

    //public IFooDoNetServiceCallback serviceCallback = this;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        //boundedService = getIntent().getExtras().getParcelable("service");
    }

    @Override
    protected void onStart() {
/*
        if(!isServiceRunning){
            serviceIntent = new Intent(this, FooDoNetService.class);
            bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
            isServiceRunning = true;
        }
        if (boundedService != null) {
            isBoundedToService = true;
            Message m = Message.obtain(null, FooDoNetService.ACTION_START);
            m.replyTo = callbackMessenger;
            try {
                boundedService.send(m);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
*/
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (servicesBroadcastReceiver != null) {
            unregisterReceiver(servicesBroadcastReceiver);
            servicesBroadcastReceiver = null;
        }
        CommonUtil.SetIsApplicationRunningInForeground(this, false);
//        if(progressDialog != null)
//            progressDialog.dismiss();
        super.onPause();
    }

    @Override
    protected void onResume() {
        isInternetAvailable = CheckInternetConnection();
        if (!isInternetAvailable)
            OnInternetNotConnected();
        isGoogleServiceAvailable = CheckPlayServices();
        if (!isGoogleServiceAvailable)
            OnGooglePlayServicesCheckError();
        CommonUtil.SetIsApplicationRunningInForeground(this, true);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        if (servicesBroadcastReceiver == null) {
            servicesBroadcastReceiver = new ServicesBroadcastReceiver(this);
            IntentFilter filter = new IntentFilter(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
            registerReceiver(servicesBroadcastReceiver, filter);
        }
        super.onResume();
    }

/*
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FooDoNetService.FooDoNetCustomServiceBinder mBinder = (FooDoNetService.FooDoNetCustomServiceBinder) service;
            fooDoNetService = mBinder.getService();
            fooDoNetService.StartScheduler(serviceCallback);
            isBoundedToService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBoundedToService = false;
        }
    };
*/

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

    protected boolean CheckInternetForAction(String action){
        if(!isInternetAvailable){
            isInternetAvailable = CheckInternetConnection();
            if(!isInternetAvailable){
                Toast.makeText(this,
                        getString(R.string.error_cant_perform_this_action_without_internet).replace("{0}",
                                action), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

/*
    @Override
    public abstract void OnNotifiedToFetchData();

    @Override
    public abstract void LoadUpdatedListOfPublications(ArrayList<FCPublication> updatedList);
*/

    @Override
    public void onBroadcastReceived(Intent intent) {
        int actionCode = intent.getIntExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, -1);
        switch (actionCode) {
            case ServicesBroadcastReceiver.ACTION_CODE_GET_LOCATION_SUCCESS:
                Location location = (Location)intent.getParcelableExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_LOCATION_KEY);
                OnGotMyLocationCallback(location);
                if(location != null)
                    UpdateMyLocationPreferences(new LatLng(location.getLatitude(), location.getLongitude()));
                break;
        }
    }

    public abstract void OnGooglePlayServicesCheckError();

    public abstract void OnInternetNotConnected();

    public void OnGotMyLocationCallback(Location location) {}

    /*
        protected ServiceConnection mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                boundedService = new Messenger(service);
                isBoundedToService = true;
                Message m = Message.obtain(null, FooDoNetService.ACTION_START);
                m.replyTo = callbackMessenger;
                try {
                    boundedService.send(m);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            public void onServiceDisconnected(ComponentName className) {
                boundedService = null;
                isBoundedToService = false;
            }
        };

    */
/*
    class IncomingHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GetMyLocationAsync.ACTION_GET_MY_LOCATION:
                    OnGotMyLocationCallback((Location) msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
*/

   // final Messenger callbackMessenger = new Messenger(new IncomingHandler());


    protected void StartGetMyLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        GetMyLocationAsync locationAsync = new GetMyLocationAsync(locationManager, this);
        locationAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected void UpdateMyLocationPreferences(LatLng myLocation){
        if(myLocation == null){
            Log.e(MY_TAG, "got null myLocation (updateMyLocationPreferences)");
            return;
        }
        SharedPreferences sp = getSharedPreferences(getString(R.string.shared_preferences_my_location_key), MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
/*
        if(sp.contains(getString(R.string.shared_preferences_my_latitude_key))){
            editor.remove(getString(R.string.shared_preferences_my_latitude_key));
            editor.commit();
        }
        if(sp.contains(getString(R.string.shared_preferences_my_longitude_key))){
            editor.remove(getString(R.string.shared_preferences_my_longitude_key));
            editor.commit();
        }
*/
        editor.putFloat(getString(R.string.shared_preferences_my_latitude_key), ((float) myLocation.latitude));
        editor.putFloat(getString(R.string.shared_preferences_my_longitude_key), ((float) myLocation.longitude));
        editor.commit();
    }

    protected void UpdateFilterTextPreferences(String filterText){
        UpdateFilterTextPreferences(this, filterText);
    }

    public static void UpdateFilterTextPreferences(Context ctx, String filterText){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.shared_preferences_text_filter_key), MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (sp.contains(ctx.getString(R.string.shared_preferences_text_filter_text_key))){
            editor.remove(ctx.getString(R.string.shared_preferences_text_filter_text_key));
            editor.commit();
        }
        editor.putString(ctx.getString(R.string.shared_preferences_text_filter_text_key), filterText);
        editor.commit();
    }

}
