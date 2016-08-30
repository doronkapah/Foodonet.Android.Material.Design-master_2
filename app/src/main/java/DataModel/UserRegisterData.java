package DataModel;

import android.util.JsonWriter;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Asher on 19.08.2015.
 */
public class UserRegisterData implements Serializable, ICanWriteSelfToJSONWriter {

    private static final String MY_TAG = "food_UserRegisterData";

    public static final String USER_DATA_DEV_UUID_FIELD_NAME = "dev_uuid";
    public static final String USER_DATA_PUSH_TOKEN = "remote_notiﬁcation_token";
    public static final String USER_DATA_LATITUDE = "last_location_latitude";
    public static final String USER_DATA_LONGITUDE = "last_location_longitude";
    public static final String USER_DATA_IS_IOS = "is_ios";

    public UserRegisterData(String imei, String token, double latitude, double longitude) {
        set_Imei(imei);
        set_Push_Token(token);
        set_Latitude(latitude);
        set_Longitude(longitude);
    }

    private String _imei;

    public void set_Imei(String imei) {
        _imei = imei;
    }

    public String get_Imei() {
        return _imei;
    }

    private String _push_token;

    public void set_Push_Token(String token) {
        _push_token = token;
    }

    public String get_Push_Token() {
        return _push_token;
    }

    private double _latitude;

    public void set_Latitude(double latitude) {
        _latitude = latitude;
    }

    public double get_Latitude() {
        return _latitude;
    }

    private double _longitude;

    public void set_Longitude(double longitude) {
        _longitude = longitude;
    }

    public double get_Longitude() {
        return _longitude;
    }

    private final static boolean _is_ios = false;

    public boolean get_Is_IOS() {
        return _is_ios;
    }

    public void WriteSelfToJSONWriter(JsonWriter writer) {
        try {
            writer.beginObject();
            writer.name("active_device");
            writer.beginObject();
            writer.name(USER_DATA_DEV_UUID_FIELD_NAME).value(get_Imei());
            writer.name(USER_DATA_PUSH_TOKEN).value("1234");//get_Push_Token());
            writer.name(USER_DATA_LATITUDE).value(String.valueOf(get_Latitude()));
            writer.name(USER_DATA_LONGITUDE).value(String.valueOf(get_Longitude()));
            writer.name(USER_DATA_IS_IOS).value(Boolean.toString(get_Is_IOS()));
            writer.endObject();
            writer.endObject();
        } catch (IOException e) {
            Log.e(MY_TAG, e.toString());
            e.printStackTrace();
        }
    }

    public Map<String, Object> GetJsonMapStringObject() {
        Map<String, Object> deviceData = new HashMap<String, Object>();
        deviceData.put(USER_DATA_DEV_UUID_FIELD_NAME, get_Imei());
        deviceData.put(USER_DATA_PUSH_TOKEN, get_Push_Token());
        deviceData.put(USER_DATA_IS_IOS, false);
        deviceData.put(USER_DATA_LATITUDE, get_Latitude());
        deviceData.put(USER_DATA_LONGITUDE, get_Longitude());
        Map<String, Object> dataToSend = new HashMap<String, Object>();
        dataToSend.put("active_device", deviceData);

        return dataToSend;
    }

    @Override
    public org.json.simple.JSONObject GetJsonObjectForPost() {

        Map<String, Object> deviceData = new HashMap<String, Object>();
        deviceData.put("dev_uuid", get_Imei());
        deviceData.put("remote_notification_token", get_Push_Token());
        deviceData.put("last_location_latitude", get_Latitude());
        deviceData.put("last_location_longitude", get_Longitude());
        deviceData.put("is_ios", false);
        Map<String, Object> dataToSend = new HashMap<String, Object>();
        dataToSend.put("active_device", deviceData);

        org.json.simple.JSONObject json = new org.json.simple.JSONObject(dataToSend);
        return json;
    }


}
