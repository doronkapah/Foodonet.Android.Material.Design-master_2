package upp.foodonet.material;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.InternalRequest;
import DataModel.UserRegisterData;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;

public class FooDoNetInstanceIDListenerService extends IntentService implements IFooDoNetServerCallback {

    private static final String[] TOPICS = {"global"};

    private static final String MY_TAG = "food_intentService_ID";

    private static final String ACTION_REGISTER_TO_GCM = "1";

    public static void StartRegisterToGCM(Context context) {
        Intent intent = new Intent(context, FooDoNetInstanceIDListenerService.class);
        intent.setAction(ACTION_REGISTER_TO_GCM);
        context.startService(intent);
    }

    public FooDoNetInstanceIDListenerService() {
        super("FooDoNetInstanceIDListenerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_REGISTER_TO_GCM:
                    RegisterToGCM();
                    break;
                default:
                    return;
            }
        }
    }

    private void RegisterToGCM() {
        InstanceID instanceID = InstanceID.getInstance(this);
        String token = "";
        try {
            token = instanceID.getToken(getString(R.string.notifications_server_id),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            subscribeTopics(token);
            SharedPreferences sp = getSharedPreferences(getResources().getString(R.string.shared_preferences_token), MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(getResources().getString(R.string.shared_preferences_token_key), token);
            editor.commit();
            Log.w(MY_TAG, "Got token: " + token);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String imei = CommonUtil.GetIMEI(this);
        Log.w(MY_TAG, "Got imei: " + imei);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = null;
        double lat, lon;
        if (locationManager == null) {
            Log.e(MY_TAG, "could not get location!");
            return;
        } else {
            //int pCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (location == null)
        {
            Log.e(MY_TAG, "could not get location!");
            lat = 0;
            lon = 0;
        } else {
            lat = location.getLatitude();
            lon = location.getLongitude();
        }

        UserRegisterData userData = new UserRegisterData(imei, token, lat, lon);

        HttpServerConnectorAsync connector
                = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), (IFooDoNetServerCallback)this);
        connector.setContextForBroadcasting(this);
        connector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new InternalRequest(InternalRequest.ACTION_POST_REGISTER,
                        getResources().getString(R.string.register_new_device), userData));
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        switch (response.Status){
            case InternalRequest.STATUS_OK:
                Log.i(MY_TAG, "got server callback registration ok");
                CommonUtil.PutCommonPreferenceIsRegisteredDevice(this, true);
                break;
            case InternalRequest.STATUS_FAIL:
                Log.i(MY_TAG, "got server callback registration fail");
                CommonUtil.PutCommonPreferenceIsRegisteredDevice(this, false);
                break;
            default:
                Log.i(MY_TAG, "unexpected callback status from server!");
                return;
        }
    }

    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        pubSub.subscribe(token, getString(R.string.push_notification_prefix), null);
//        for (String topic : TOPICS) {
//
//        }
    }
}
