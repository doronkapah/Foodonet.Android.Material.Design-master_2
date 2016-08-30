package DataModel;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Asher on 21.08.2015.
 */
public class RegisteredUserForPublication implements Serializable, ICanWriteSelfToJSONWriter {

    private static final String MY_TAG = "food_RegForPublication";

    public static final String REGISTERED_FOR_PUBLICATION_KEY_ID = "_id";
    public static final String REGISTERED_FOR_PUBLICATION_KEY_ID_SERVER = "id";
    public static final String REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID = "publication_id";
    public static final String REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_VERSION = "publication_version";
    public static final String REGISTERED_FOR_PUBLICATION_KEY_DATE = "date_of_registration";
    public static final String REGISTERED_FOR_PUBLICATION_KEY_DEVICE_UUID = "active_device_dev_uuid";
    public static final String REGISTERED_FOR_PUBLICATION_KEY_USER_ID = "collector_user_id";
    public static final String REGISTERED_FOR_PUBLICATION_KEY_USER_NAME = "collector_name";
    public static final String REGISTERED_FOR_PUBLICATION_KEY_USER_CONTACT_INFO = "collector_contact_info";


    public static final String REGISTERED_FOR_PUBLICATION_KEY_NEW_NEGATIVE_ID = "neg_id";

    private int id;
    public int getId(){
        return id;
    }
    public void setId(int val){
        id = val;
    }

    private int publication_id;
    public int getPublication_id(){
        return publication_id;
    }
    public void setPublication_id(int val){
        publication_id = val;
    }

    private int publication_version;
    public int getPublication_version(){return publication_version;}
    public void setPublication_version(int val){
        publication_version = val;
    }

    private Date date_registered;
    public Date getDate_registered(){return date_registered;}
    public long getDate_registered_unix_time(){return date_registered.getTime()/1000;}
    public void setDate_registered(Date val){
        date_registered = val;
    }
    public void setDate_registered(long val){
        date_registered = new Date(val * 1000);
    }

    private String device_registered_uuid;
    public String getDevice_registered_uuid(){
        return device_registered_uuid;
    }
    public void setDevice_registered_uuid(String val){
        device_registered_uuid = val;
    }

    private String collectorName;
    public String getCollectorName(){
        return collectorName;
    }
    public void setCollectorName(String name){
        collectorName = name;
    }

    private String collectorPhone;
    public String getCollectorphone(){
        return collectorPhone;
    }
    public void setCollectorphone(String phone){
        collectorPhone = phone;
    }

    private int collectorUserID;
    public int getUserID() { return collectorUserID; }
    public void setUserID(int userID) { collectorUserID = userID; }

    public static String[] GetColumnNamesArray() {
        return
                new String[]{
                        REGISTERED_FOR_PUBLICATION_KEY_ID,
                        REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID,
                        REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_VERSION,
                        REGISTERED_FOR_PUBLICATION_KEY_DATE,
                        REGISTERED_FOR_PUBLICATION_KEY_DEVICE_UUID,
                        REGISTERED_FOR_PUBLICATION_KEY_USER_NAME,
                        REGISTERED_FOR_PUBLICATION_KEY_USER_CONTACT_INFO,
                        REGISTERED_FOR_PUBLICATION_KEY_USER_ID
                };
    }

    public ContentValues GetContentValuesRow() {
        ContentValues cv = new ContentValues();
        cv.put(REGISTERED_FOR_PUBLICATION_KEY_ID, getId());
        cv.put(REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID, getPublication_id());
        cv.put(REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_VERSION, getPublication_version());
        cv.put(REGISTERED_FOR_PUBLICATION_KEY_DATE, getDate_registered_unix_time());
        cv.put(REGISTERED_FOR_PUBLICATION_KEY_DEVICE_UUID, getDevice_registered_uuid());
        cv.put(REGISTERED_FOR_PUBLICATION_KEY_USER_NAME, getCollectorName());
        cv.put(REGISTERED_FOR_PUBLICATION_KEY_USER_CONTACT_INFO, getCollectorphone());
        cv.put(REGISTERED_FOR_PUBLICATION_KEY_USER_ID, getUserID());
        return cv;
    }

    public static ArrayList<RegisteredUserForPublication> GetArrayListOfRegisteredForPublicationsFromCursor(Cursor cursor) {
        ArrayList<RegisteredUserForPublication> result = new ArrayList<RegisteredUserForPublication>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                RegisteredUserForPublication rufp = new RegisteredUserForPublication();
                rufp.setId(cursor.getInt(cursor.getColumnIndex(REGISTERED_FOR_PUBLICATION_KEY_ID)));
                rufp.setPublication_id(cursor.getInt(cursor.getColumnIndex(REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID)));
                rufp.setPublication_version(cursor.getInt(cursor.getColumnIndex(REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_VERSION)));
                rufp.setDate_registered(cursor.getLong(cursor.getColumnIndex(REGISTERED_FOR_PUBLICATION_KEY_DATE)));
                rufp.setDevice_registered_uuid(cursor.getString(cursor.getColumnIndex(REGISTERED_FOR_PUBLICATION_KEY_DEVICE_UUID)));
                rufp.setCollectorName(cursor.getString(cursor.getColumnIndex(REGISTERED_FOR_PUBLICATION_KEY_USER_NAME)));
                rufp.setCollectorphone(cursor.getString(cursor.getColumnIndex(REGISTERED_FOR_PUBLICATION_KEY_USER_CONTACT_INFO)));
                rufp.setUserID(cursor.getInt(cursor.getColumnIndex(REGISTERED_FOR_PUBLICATION_KEY_USER_ID)));
                result.add(rufp);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public static ArrayList<RegisteredUserForPublication> GetArrayListOfRegisteredForPublicationsFromJSON(JSONArray ja) {
        ArrayList<RegisteredUserForPublication> result = new ArrayList<RegisteredUserForPublication>();
        for (int i = 0; i < ja.length(); i++) {
            try {
                //Log.i(MY_TAG, ja.getJSONObject(i).toString());
                result.add(ParseSingleRegisteredForPublicationFromJSON(ja.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static RegisteredUserForPublication ParseSingleRegisteredForPublicationFromJSON(JSONObject jo) {
        if (jo == null) return null;
        RegisteredUserForPublication rufp = new RegisteredUserForPublication();
        try {
            rufp.setId(jo.getInt(REGISTERED_FOR_PUBLICATION_KEY_ID_SERVER));
            rufp.setPublication_id(jo.getInt(REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID));
            rufp.setPublication_version(jo.getInt(REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_VERSION));
            rufp.setDate_registered(jo.getLong(REGISTERED_FOR_PUBLICATION_KEY_DATE));
            rufp.setDevice_registered_uuid(jo.getString(REGISTERED_FOR_PUBLICATION_KEY_DEVICE_UUID));
            rufp.setCollectorName(jo.getString(REGISTERED_FOR_PUBLICATION_KEY_USER_NAME));
            rufp.setCollectorphone(jo.getString(REGISTERED_FOR_PUBLICATION_KEY_USER_CONTACT_INFO));
            if(!jo.isNull(REGISTERED_FOR_PUBLICATION_KEY_USER_ID))
                rufp.setUserID(jo.getInt(REGISTERED_FOR_PUBLICATION_KEY_USER_ID));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(MY_TAG, e.getMessage());
            return null;
        }
        return rufp;
    }

    @Override
    public org.json.simple.JSONObject GetJsonObjectForPost() {
        Map<String, Object> deviceData = new HashMap<String, Object>();
        deviceData.put(REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID, getPublication_id());
        deviceData.put(REGISTERED_FOR_PUBLICATION_KEY_DATE, getDate_registered_unix_time());
        deviceData.put(REGISTERED_FOR_PUBLICATION_KEY_DEVICE_UUID, getDevice_registered_uuid());
        deviceData.put(REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_VERSION, getPublication_version());
        deviceData.put(REGISTERED_FOR_PUBLICATION_KEY_USER_CONTACT_INFO, getCollectorphone());
        deviceData.put(REGISTERED_FOR_PUBLICATION_KEY_USER_NAME, getCollectorName());
        //deviceData.put(REGISTERED_FOR_PUBLICATION_KEY_USER_ID, getUserID());
        Map<String, Object> dataToSend = new HashMap<String, Object>();
        dataToSend.put("registered_user_for_publication", deviceData);

        org.json.simple.JSONObject json = new org.json.simple.JSONObject(dataToSend);
        return json;
    }

}
