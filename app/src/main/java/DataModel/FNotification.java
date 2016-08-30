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

/**
 * Created by Asher on 22-Jul-16.
 */
public class FNotification implements Serializable {
    private static final String MY_TAG = "food_fnotification";

    public static final String FNOTIFICATION_KEY_ID = "_id";
    public static final String FNOTIFICATION_KEY_DATE_ARRIVED = "date_arrived";
    public static final String FNOTIFICATION_KEY_DATA = "data";

    public static final String FNOTIFICATION_KEY_TYPE = "type";
    public static final String FNOTIFICATION_KEY_PUBLICATION_OR_GROUP_ID = "id";
    public static final String FNOTIFICATION_KEY_PUBLICATION_OR_GROUP_TITLE = "title";
    public static final String FNOTIFICATION_KEY_PUBLICATION_VERSION = "version";
    public static final String FNOTIFICATION_KEY_LATITUDE = "latitude";
    public static final String FNOTIFICATION_KEY_LONGITUDE = "longitude";

    public static final String FNOTIFICATION_TYPE_KEY_NEW_PUBLICATION = "new_publication";
    public static final String FNOTIFICATION_TYPE_KEY_DELETED_PUBLICATION = "deleted_publication";
    public static final String FNOTIFICATION_TYPE_KEY_PUBLICATION_REPORT = "publication_report";
    public static final String FNOTIFICATION_TYPE_KEY_REGISTRATION_FOR_PUBLICATION = "registration_for_publication";
    public static final String FNOTIFICATION_TYPE_KEY_GROUP_MEMBERS = "group_members";

    public static final int FNOTIFICATION_TYPE_NEW_PUBLICATION = 0;
    public static final int FNOTIFICATION_TYPE_EDITED_PUBLICATION = 1;
    public static final int FNOTIFICATION_TYPE_DELETED_PUBLICATION = 2;
    public static final int FNOTIFICATION_TYPE_NEW_REPORT = 3;
    public static final int FNOTIFICATION_TYPE_NEW_REGISTRATION = 4;
    public static final int FNOTIFICATION_TYPE_GROUP_MEMBER_ADD = 5;
    public static final int FNOTIFICATION_TYPE_GROUP_MEMBER_DELETE = 6;

    private int _id;
    private int _type;
    private Date _date_arrived;
    private int _publication_or_group_id;
    private String _publication_or_group_title;
    private int _publication_version;
    private double _latitude;
    private double _longitude;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int get_type() {
        return _type;
    }

    public void set_type(int _type) {
        this._type = _type;
    }

    public Date get_date_arrived() {
        return _date_arrived;
    }

    public void set_date_arrived(Date _date_arrived) {
        this._date_arrived = _date_arrived;
    }

    public long getDateArrivedUnixTime() {
        return _date_arrived.getTime() / 1000;
    }

    public void setDateArrivedUnixTime(long _date_arrived) {
        this._date_arrived = new Date(_date_arrived * 1000);
    }

    public int get_publication_or_group_id() {
        return _publication_or_group_id;
    }

    public void set_publication_or_group_id(int _publication_or_group_id) {
        this._publication_or_group_id = _publication_or_group_id;
    }

    public String get_publication_or_group_title() {
        return _publication_or_group_title;
    }

    public void set_publication_or_group_title(String _publication_or_group_title) {
        this._publication_or_group_title = _publication_or_group_title;
    }

    public int get_publication_version() {
        return _publication_version;
    }

    public void set_publication_version(int _publication_version) {
        this._publication_version = _publication_version;
    }

    public double get_latitude() {
        return _latitude;
    }

    public void set_latitude(double _latitude) {
        this._latitude = _latitude;
    }

    public double get_longitude() {
        return _longitude;
    }

    public void set_longitude(double _longitude) {
        this._longitude = _longitude;
    }

    public static String[] GetColumnNamesArray() {
        return
                new String[]{
                        FNOTIFICATION_KEY_ID,
                        FNOTIFICATION_KEY_TYPE,
                        FNOTIFICATION_KEY_DATE_ARRIVED,
                        FNOTIFICATION_KEY_PUBLICATION_OR_GROUP_ID,
                        FNOTIFICATION_KEY_PUBLICATION_OR_GROUP_TITLE,
                        FNOTIFICATION_KEY_PUBLICATION_VERSION,
                        FNOTIFICATION_KEY_LATITUDE,
                        FNOTIFICATION_KEY_LONGITUDE
                };
    }

    public ContentValues GetContentValuesRow() {
        ContentValues cv = new ContentValues();
        //cv.put(FNOTIFICATION_KEY_ID, "NULL");
        cv.put(FNOTIFICATION_KEY_TYPE, get_type());
        cv.put(FNOTIFICATION_KEY_DATE_ARRIVED, getDateArrivedUnixTime());
        cv.put(FNOTIFICATION_KEY_PUBLICATION_OR_GROUP_ID, get_publication_or_group_id());
        cv.put(FNOTIFICATION_KEY_PUBLICATION_OR_GROUP_TITLE, get_publication_or_group_title());
        cv.put(FNOTIFICATION_KEY_PUBLICATION_VERSION, get_publication_version());
        cv.put(FNOTIFICATION_KEY_LATITUDE, get_latitude());
        cv.put(FNOTIFICATION_KEY_LONGITUDE, get_longitude());
        return cv;
    }

    public static ArrayList<FNotification> GetNotificationsFromCursor(Cursor cursor) {
        ArrayList<FNotification> result = new ArrayList<FNotification>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                FNotification notification = new FNotification();
                notification.set_id(cursor.getInt(cursor.getColumnIndex(FNOTIFICATION_KEY_ID)));
                notification.set_type(cursor.getInt(cursor.getColumnIndex(FNOTIFICATION_KEY_TYPE)));
                notification.setDateArrivedUnixTime(cursor.getLong(cursor.getColumnIndex(FNOTIFICATION_KEY_DATE_ARRIVED)));
                notification.set_publication_or_group_id(cursor.getInt(cursor.getColumnIndex(FNOTIFICATION_KEY_PUBLICATION_OR_GROUP_ID)));
                notification.set_publication_or_group_title(cursor.getString(cursor.getColumnIndex(FNOTIFICATION_KEY_PUBLICATION_OR_GROUP_TITLE)));
                notification.set_publication_version(cursor.getInt(cursor.getColumnIndex(FNOTIFICATION_KEY_PUBLICATION_VERSION)));
                notification.set_latitude(cursor.getDouble(cursor.getColumnIndex(FNOTIFICATION_KEY_LATITUDE)));
                notification.set_longitude(cursor.getDouble(cursor.getColumnIndex(FNOTIFICATION_KEY_LONGITUDE)));
                result.add(notification);
            } while (cursor.moveToNext());
        }
        return result;
    }

    public static FNotification ParseSingleNotificationFromJSON(org.json.JSONObject jo) {
        if (jo == null) return null;
        FNotification notification = new FNotification();
        try {
            notification.set_date_arrived(new Date());
            if(jo.isNull(FNOTIFICATION_KEY_TYPE))
                return null;

            String type_got_from_json = jo.getString(FNOTIFICATION_KEY_TYPE);
            //org.json.JSONObject innerJson = jo.getJSONObject(FNOTIFICATION_KEY_DATA);
            notification.set_publication_or_group_id(jo.getInt(FNOTIFICATION_KEY_PUBLICATION_OR_GROUP_ID));

            if (type_got_from_json.compareTo(FNOTIFICATION_TYPE_KEY_GROUP_MEMBERS) == 0) {
                notification.set_type(FNOTIFICATION_TYPE_GROUP_MEMBER_ADD);
            } else {
                notification.set_publication_version(jo.getInt(FNOTIFICATION_KEY_PUBLICATION_VERSION));
                switch (type_got_from_json) {
                    case FNOTIFICATION_TYPE_KEY_NEW_PUBLICATION:
                        notification.set_type((notification.get_publication_version() == 1)
                                ? FNOTIFICATION_TYPE_NEW_PUBLICATION
                                : FNOTIFICATION_TYPE_EDITED_PUBLICATION);
                        notification.set_publication_or_group_title(jo.getString(FNOTIFICATION_KEY_PUBLICATION_OR_GROUP_TITLE));
                        notification.set_latitude(jo.getDouble(FNOTIFICATION_KEY_LATITUDE));
                        notification.set_longitude(jo.getDouble(FNOTIFICATION_KEY_LONGITUDE));
                        break;
                    case FNOTIFICATION_TYPE_KEY_DELETED_PUBLICATION:
                        notification.set_type(FNOTIFICATION_TYPE_DELETED_PUBLICATION);
                        notification.set_publication_or_group_title(jo.getString(FNOTIFICATION_KEY_PUBLICATION_OR_GROUP_TITLE));
                        notification.set_latitude(jo.getDouble(FNOTIFICATION_KEY_LATITUDE));
                        notification.set_longitude(jo.getDouble(FNOTIFICATION_KEY_LONGITUDE));
                        break;
                    case FNOTIFICATION_TYPE_KEY_PUBLICATION_REPORT:
                        notification.set_type(FNOTIFICATION_TYPE_NEW_REPORT);
                        break;
                    case FNOTIFICATION_TYPE_KEY_REGISTRATION_FOR_PUBLICATION:
                        notification.set_type(FNOTIFICATION_TYPE_NEW_REGISTRATION);
                        break;
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(MY_TAG, e.getMessage());
            return null;
        }
        return notification;
    }

}
