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
 * Created by Asher on 30.08.2015.
 */
public class PublicationReport implements Serializable, ICanWriteSelfToJSONWriter {

    private static final String MY_TAG = "food_PublicationReport";

    public static final String PUBLICATION_REPORT_FIELD_KEY_ID = "_id";
    public static final String PUBLICATION_REPORT_FIELD_KEY_ID_SERVER = "id";
    public static final String PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_ID = "publication_id";
    public static final String PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_VERSION = "publication_version";
    public static final String PUBLICATION_REPORT_FIELD_KEY_REPORT = "report";
    public static final String PUBLICATION_REPORT_FIELD_KEY_DATE = "date_of_report";
    public static final String PUBLICATION_REPORT_FIELD_KEY_DEVICE_UID = "active_device_dev_uuid";
    public static final String PUBLICATION_REPORT_FIELD_KEY_REPORT_USER_NAME = "report_user_name";
    public static final String PUBLICATION_REPORT_FIELD_KEY_REPORT_CONTACT_INFO = "report_contact_info";
    public static final String PUBLICATION_REPORT_FIELD_KEY_REPORT_USER_ID = "reporter_user_id";
    public static final String PUBLICATION_REPORT_FIELD_KEY_REPORT_RATING = "rating";

    public static final String PUBLICATION_REPORT_FIELD_KEY_NEG_ID = "neg_id";

    public PublicationReport(int id, int pub_id, int pub_version, int report, Date date, String dev_UID) {
        setId(id);
        setPublication_id(pub_id);
        setPublication_version(pub_version);
        setReport(report);
        setDate_reported(date);
        setDevice_uuid(dev_UID);
    }

    public PublicationReport() {
    }

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int val) {
        id = val;
    }

    private int publication_id;

    public int getPublication_id() {
        return publication_id;
    }

    public void setPublication_id(int val) {
        publication_id = val;
    }

    private int publication_version;

    public int getPublication_version() {
        return publication_version;
    }

    public void setPublication_version(int val) {
        publication_version = val;
    }

    private Date date_reported;

    public Date getDate_reported() {
        return date_reported;
    }

    public long getDate_reported_unix_time() {
        return date_reported.getTime() / 1000;
    }

    public void setDate_reported(Date val) {
        date_reported = val;
    }

    public void setDate_reported(long val) {
        date_reported = new Date(val * 1000);
    }

    private String device_uuid;

    public String getDevice_uuid() {
        return device_uuid;
    }

    public void setDevice_uuid(String val) {
        device_uuid = val;
    }

    private int report;

    public int getReport() {
        return report;
    }

    public void setReport(int val) {
        report = val;
    }

    private String report_user_name;

    public String getReportUserName() {
        return report_user_name;
    }

    public void setReportUserName(String value) {
        report_user_name = value;
    }

    private String report_contact_info;

    public String getReportContactInfo() {
        return report_contact_info;
    }

    public void setReportContactInfo(String value) {
        report_contact_info = value;
    }

    private int rating;

    public int getRating() {
        return rating;
    }

    public void setRating(int r) {
        rating = r;
    }

    private int report_userID;

    public int getReport_userID() {
        return report_userID;
    }

    public void setReport_userID(int user_id) {
        report_userID = user_id;
    }

    public static String[] GetColumnNamesArray() {
        return
                new String[]{
                        PUBLICATION_REPORT_FIELD_KEY_ID,
                        PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_ID,
                        PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_VERSION,
                        PUBLICATION_REPORT_FIELD_KEY_DATE,
                        PUBLICATION_REPORT_FIELD_KEY_REPORT,
                        PUBLICATION_REPORT_FIELD_KEY_DEVICE_UID,
                        PUBLICATION_REPORT_FIELD_KEY_REPORT_USER_NAME,
                        PUBLICATION_REPORT_FIELD_KEY_REPORT_CONTACT_INFO,
                        PUBLICATION_REPORT_FIELD_KEY_REPORT_RATING,
                        PUBLICATION_REPORT_FIELD_KEY_REPORT_USER_ID
                };
    }

    public static ArrayList<PublicationReport> GetArrayListOfPublicationReportsFromCursor(Cursor cursor) {
        ArrayList<PublicationReport> result = new ArrayList<PublicationReport>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                PublicationReport pr = new PublicationReport();
                pr.setId(cursor.getInt(cursor.getColumnIndex(PUBLICATION_REPORT_FIELD_KEY_ID)));
                pr.setPublication_id(cursor.getInt(cursor.getColumnIndex(PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_ID)));
                pr.setPublication_version(cursor.getInt(cursor.getColumnIndex(PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_VERSION)));
                pr.setDate_reported(cursor.getLong(cursor.getColumnIndex(PUBLICATION_REPORT_FIELD_KEY_DATE)));
                pr.setDevice_uuid(cursor.getString(cursor.getColumnIndex(PUBLICATION_REPORT_FIELD_KEY_DEVICE_UID)));
                pr.setReport(cursor.getInt(cursor.getColumnIndex(PUBLICATION_REPORT_FIELD_KEY_REPORT)));
                pr.setReportUserName(cursor.getString(cursor.getColumnIndex(PUBLICATION_REPORT_FIELD_KEY_REPORT_USER_NAME)));
                pr.setReportContactInfo(cursor.getString(cursor.getColumnIndex(PUBLICATION_REPORT_FIELD_KEY_REPORT_CONTACT_INFO)));
                pr.setRating(cursor.getInt(cursor.getColumnIndex(PUBLICATION_REPORT_FIELD_KEY_REPORT_RATING)));
                pr.setReport_userID(cursor.getInt(cursor.getColumnIndex(PUBLICATION_REPORT_FIELD_KEY_REPORT_USER_ID)));
                result.add(pr);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public static ArrayList<PublicationReport> GetArrayListOfPublicationReportsFromJSON(JSONArray ja) {
        ArrayList<PublicationReport> result = new ArrayList<PublicationReport>();
        for (int i = 0; i < ja.length(); i++) {
            try {
                //Log.i(MY_TAG, ja.getJSONObject(i).toString());
                result.add(ParseSinglePublicationReportFromJSON(ja.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static PublicationReport ParseSinglePublicationReportFromJSON(JSONObject jo) {
        if (jo == null) return null;
        PublicationReport pr = new PublicationReport();
        try {
            pr.setId(jo.getInt(PUBLICATION_REPORT_FIELD_KEY_ID_SERVER));
            pr.setPublication_id(jo.getInt(PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_ID));
            pr.setPublication_version(jo.getInt(PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_VERSION));
            pr.setDate_reported(jo.getLong(PUBLICATION_REPORT_FIELD_KEY_DATE));
            pr.setDevice_uuid(jo.getString(PUBLICATION_REPORT_FIELD_KEY_DEVICE_UID));
            pr.setReport(jo.getInt(PUBLICATION_REPORT_FIELD_KEY_REPORT));
            pr.setReportUserName(jo.getString(PUBLICATION_REPORT_FIELD_KEY_REPORT_USER_NAME));
            pr.setReportContactInfo(jo.getString(PUBLICATION_REPORT_FIELD_KEY_REPORT_CONTACT_INFO));
            pr.setRating(jo.getInt(PUBLICATION_REPORT_FIELD_KEY_REPORT_RATING));
            if(!jo.isNull(PUBLICATION_REPORT_FIELD_KEY_REPORT_USER_ID))
                pr.setReport_userID(jo.getInt(PUBLICATION_REPORT_FIELD_KEY_REPORT_USER_ID));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(MY_TAG, e.getMessage());
            return null;
        }
        return pr;
    }

    public ContentValues GetContentValuesRow() {
        ContentValues cv = new ContentValues();
        cv.put(PUBLICATION_REPORT_FIELD_KEY_ID, getId());
        cv.put(PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_ID, getPublication_id());
        cv.put(PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_VERSION, getPublication_version());
        cv.put(PUBLICATION_REPORT_FIELD_KEY_DATE, getDate_reported_unix_time());
        cv.put(PUBLICATION_REPORT_FIELD_KEY_DEVICE_UID, getDevice_uuid());
        cv.put(PUBLICATION_REPORT_FIELD_KEY_REPORT, getReport());
        cv.put(PUBLICATION_REPORT_FIELD_KEY_REPORT_USER_NAME, getReportUserName());
        cv.put(PUBLICATION_REPORT_FIELD_KEY_REPORT_CONTACT_INFO, getReportContactInfo());
        cv.put(PUBLICATION_REPORT_FIELD_KEY_REPORT_RATING, getRating());
        cv.put(PUBLICATION_REPORT_FIELD_KEY_REPORT_USER_ID, getReport_userID());
        return cv;
    }

    @Override
    public org.json.simple.JSONObject GetJsonObjectForPost() {
        Map<String, Object> deviceData = new HashMap<String, Object>();
        deviceData.put("date_of_report", getDate_reported_unix_time());
        deviceData.put("publication_id", getPublication_id());
        deviceData.put("active_device_dev_uuid", getDevice_uuid());
        deviceData.put("report", getReport());
        deviceData.put("publication_version", getPublication_version());
        deviceData.put(PUBLICATION_REPORT_FIELD_KEY_REPORT_USER_NAME, getReportUserName());
        deviceData.put(PUBLICATION_REPORT_FIELD_KEY_REPORT_CONTACT_INFO, getReportContactInfo());
        deviceData.put(PUBLICATION_REPORT_FIELD_KEY_REPORT_RATING, getRating());
        deviceData.put(PUBLICATION_REPORT_FIELD_KEY_REPORT_USER_ID, getReport_userID());
        Map<String, Object> dataToSend = new HashMap<String, Object>();
        dataToSend.put("publication_report", deviceData);

        org.json.simple.JSONObject json = new org.json.simple.JSONObject(dataToSend);
        return json;
    }
}
