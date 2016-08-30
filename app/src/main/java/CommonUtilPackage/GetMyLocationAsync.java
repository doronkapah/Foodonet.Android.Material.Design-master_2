package CommonUtilPackage;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Messenger;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;
import upp.foodonet.material.R;

/**
 * Created by Asher on 04.09.2015.
 */
public class GetMyLocationAsync extends AsyncTask<Void, Void, Void> {

    public static final int ACTION_GET_MY_LOCATION = 3;
    Messenger handler;
    Location location;
    LocationManager locationManager;
    boolean gotLocation;
    Context context;
    IGotMyLocationCallback callback;
    boolean isReportLocationMode = false;
    String imei;

    private static final String MY_TAG = "food_myLocationAsync";

    public GetMyLocationAsync(LocationManager lManager, Context context) {
        locationManager = lManager;
        this.context = context;
    }

    public void setGotLocationCallback(IGotMyLocationCallback gotLocationCallback) {
        callback = gotLocationCallback;
    }

    public void switchToReportLocationMode(boolean flag) {
        isReportLocationMode = flag;
    }

    public void setIMEI(String imei) {
        this.imei = imei;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null)
        {
            location = locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER);
            if (location == null)
            {
                Log.e(MY_TAG, "could not get location!");
                return null;
            }
        }
        Log.i(MY_TAG, "got location! " + location.getLatitude() + ":" + location.getLongitude());
        gotLocation = true;
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        CommonUtil.UpdateFilterMyLocationPreferences(
                context, new LatLng(this.location.getLatitude(), this.location.getLongitude()));
        if(isReportLocationMode){
            HttpServerConnectorAsync serverAsync
                    = new HttpServerConnectorAsync(context.getString(R.string.server_base_url), context);
            InternalRequest ir = new InternalRequest(InternalRequest.ACTION_REPORT_LOCATION,
                    "/ActiveDevices/");
            ir.location = this.location;
            ir.imei = this.imei;
            serverAsync.execute(ir);
        } else {
            if(context != null) {
                Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
                if (gotLocation) {
                    intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, ServicesBroadcastReceiver.ACTION_CODE_GET_LOCATION_SUCCESS);
                    intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_LOCATION_KEY, location);
                } else
                    intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, ServicesBroadcastReceiver.ACTION_CODE_GET_LOCATION_FAIL);
                context.sendBroadcast(intent);
            }
            if(callback != null)
                callback.OnGotMyLocationCallback(location);
        }
    }
}
