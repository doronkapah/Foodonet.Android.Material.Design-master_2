package FooDoNetSQLClasses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import DataModel.FCPublication;
import DataModel.Group;
import DataModel.GroupMember;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;

/**
 * Created by Asher on 14.07.2015.
 */
public class FooDoNetSQLHelper extends SQLiteOpenHelper {

    private static final String MY_TAG = "food_SQLHelper";

    public static final String FC_DATABASE_NAME = "FoodCollector.db";
    public static final int FC_DATABASE_VERSION = 21;

    public static final int FILTER_ID_LIST_ALL_BY_CLOSEST = 0;
    public static final int FILTER_ID_LIST_ALL_BY_NEWEST = 1;
    public static final int FILTER_ID_LIST_ALL_BY_LESS_REGS = 2;
    public static final int FILTER_ID_LIST_ALL_BY_TEXT_FILTER = 3;

    public static final int FILTER_ID_LIST_MY_BY_ENDING_SOON = 10;
    public static final int FILTER_ID_LIST_MY_ACTIVE_ID_DESC = 11;
    public static final int FILTER_ID_LIST_MY_NOT_ACTIVE_ID_ASC = 12;
    public static final int FILTER_ID_LIST_MY_BY_TEXT_FILTER = 13;

    public static final int FILTER_ID_SIDEMENU_MY_ACTIVE = 20;
    public static final int FILTER_ID_SIDEMENU_OTHERS_I_REGISTERED = 21;

    public FooDoNetSQLHelper(Context context) {
        super(context, FC_DATABASE_NAME, null, FC_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        FCPublicationsTable.onCreate(db);
        RegisteredForPublicationTable.onCreate(db);
        PublicationReportsTable.onCreate(db);
        GroupTable.onCreate(db);
        GroupMemberTable.onCreate(db);
        FNotificationsTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        FCPublicationsTable.onUpgrade(db);
        RegisteredForPublicationTable.onUpgrade(db);
        PublicationReportsTable.onUpgrade(db);
        GroupTable.onUpgrade(db);
        GroupMemberTable.onUpgrade(db);
        FNotificationsTable.onUpgrade(db);
    }

    public static String GetRawSelectPublicationsForListByFilterID(int filterID, String... params) {
        String androidId = "";
        Log.i(MY_TAG, "selected filter: " + filterID);
        switch (filterID) {
            //region other's list
            case FILTER_ID_LIST_ALL_BY_CLOSEST:
                androidId = params[0];
                String latitude = params[1];
                String longitude = params[2];
                return RAW_FOR_LIST_SELECT_OTHERS + RAW_FOR_LIST_FROM_OTHERS
                        + getRawForListWhere("PUBS", FCPublication.PUBLICATION_PUBLISHER_UUID_KEY, "!=", androidId, false)
                        + RAW_FOR_LIST_GROUP + getRawForListOrderByDistance(latitude, longitude);
            case FILTER_ID_LIST_ALL_BY_NEWEST:
                return RAW_FOR_LIST_SELECT_OTHERS + RAW_FOR_LIST_FROM_OTHERS
                        + getRawForListWhere("PUBS", FCPublication.PUBLICATION_PUBLISHER_UUID_KEY, "!=", params[0], false)
                        + RAW_FOR_LIST_GROUP + getRawForListOrderBy("PUBS", FCPublication.PUBLICATION_STARTING_DATE_KEY, true);
            case FILTER_ID_LIST_ALL_BY_LESS_REGS:
                return RAW_FOR_LIST_SELECT_OTHERS + RAW_FOR_LIST_FROM_OTHERS
                        + getRawForListWhere("PUBS", FCPublication.PUBLICATION_PUBLISHER_UUID_KEY, "!=", params[0], false)
                        + RAW_FOR_LIST_GROUP + getRawForListOrderByLessRegs();
            case FILTER_ID_LIST_ALL_BY_TEXT_FILTER:
                androidId = params[0];
                String filterString = params[1];
                if (TextUtils.isEmpty(filterString))
                    return GetRawSelectPublicationsForListByFilterID(FILTER_ID_LIST_ALL_BY_NEWEST);
                return RAW_FOR_LIST_SELECT_OTHERS + RAW_FOR_LIST_FROM_OTHERS
                        + getRawForListWhere("PUBS", FCPublication.PUBLICATION_PUBLISHER_UUID_KEY, "!=", androidId, false)
                        + " AND ("
                        + getRawForListWhere("PUBS", FCPublication.PUBLICATION_TITLE_KEY, " LIKE ", "%" + filterString + "%", true)
                        + " OR "
                        + getRawForListWhere("PUBS", FCPublication.PUBLICATION_SUBTITLE_KEY, " LIKE ", "%" + filterString + "%", true)
                        + ") "
                        + RAW_FOR_LIST_GROUP + getRawForListOrderBy("PUBS", FCPublication.PUBLICATION_UNIQUE_ID_KEY, true);
            //endregion
            //region my publication
            case FILTER_ID_LIST_MY_BY_ENDING_SOON:
                return RAW_FOR_LIST_SELECT_MY + RAW_FOR_LIST_FROM_MY
                        + getRawForListWhere("PUBS", FCPublication.PUBLICATION_PUBLISHER_UUID_KEY, "=", params[0], false)
                        + " AND PUBS." + FCPublication.PUBLICATION_STARTING_DATE_KEY + " < " + UNIX_TIME_NOW
                        + " AND PUBS." + FCPublication.PUBLICATION_ENDING_DATE_KEY + " > " + UNIX_TIME_NOW
                        + RAW_FOR_MY_LIST_GROUP
                        + getRawForListOrderBy("PUBS", FCPublication.PUBLICATION_ENDING_DATE_KEY, true);
            case FILTER_ID_LIST_MY_ACTIVE_ID_DESC:
                return RAW_FOR_LIST_SELECT_MY + RAW_FOR_LIST_FROM_MY
                        + getRawForListWhere("PUBS", FCPublication.PUBLICATION_PUBLISHER_UUID_KEY, "=", params[0], false)
                        + " AND PUBS." + FCPublication.PUBLICATION_IS_ON_AIR_KEY + " = 1 "
                        + " AND PUBS." + FCPublication.PUBLICATION_STARTING_DATE_KEY + " < " + UNIX_TIME_NOW
                        + " AND PUBS." + FCPublication.PUBLICATION_ENDING_DATE_KEY + " > " + UNIX_TIME_NOW + " "
                        + RAW_FOR_MY_LIST_GROUP
                        + getRawForListOrderBy("PUBS", FCPublication.PUBLICATION_UNIQUE_ID_KEY, true);
            case FILTER_ID_LIST_MY_NOT_ACTIVE_ID_ASC:
                return RAW_FOR_LIST_SELECT_MY + RAW_FOR_LIST_FROM_MY
                        + getRawForListWhere("PUBS", FCPublication.PUBLICATION_PUBLISHER_UUID_KEY, "=", params[0], false)
                        + " AND (PUBS." + FCPublication.PUBLICATION_IS_ON_AIR_KEY + " = 0 "
                        + " OR PUBS." + FCPublication.PUBLICATION_STARTING_DATE_KEY + " > " + UNIX_TIME_NOW
                        + " OR PUBS." + FCPublication.PUBLICATION_ENDING_DATE_KEY + " < " + UNIX_TIME_NOW + ") "
                        + RAW_FOR_MY_LIST_GROUP
                        + getRawForListOrderBy("PUBS", FCPublication.PUBLICATION_UNIQUE_ID_KEY, false);
            //endregion
            //region sidemenu
            case FILTER_ID_SIDEMENU_OTHERS_I_REGISTERED:
                return RAW_FOR_LIST_SELECT_OTHERS + RAW_FOR_LIST_FROM_OTHERS
                        + " WHERE REGS."
                        + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_DEVICE_UUID
                        + " = " + "'" + params[0] + "'"
                        + " AND PUBS." + FCPublication.PUBLICATION_IS_ON_AIR_KEY + " = 1 "
                        + RAW_FOR_LIST_GROUP + getRawForListOrderBy("PUBS", FCPublication.PUBLICATION_ENDING_DATE_KEY, true);
            case FILTER_ID_SIDEMENU_MY_ACTIVE:
                return RAW_FOR_LIST_SELECT_OTHERS + RAW_FOR_LIST_FROM_OTHERS
                        + getRawForListWhere("PUBS", FCPublication.PUBLICATION_PUBLISHER_UUID_KEY, "=", params[0], false)
                        + " AND PUBS." + FCPublication.PUBLICATION_IS_ON_AIR_KEY + " = 1 "
                        + RAW_FOR_LIST_GROUP + getRawForListOrderBy("PUBS", FCPublication.PUBLICATION_UNIQUE_ID_KEY, true);
            //endregion
            default:
                Log.e(MY_TAG, "unexpected filter id = " + filterID);
                return "";
        }
    }

    private static final String RAW_FOR_LIST_SELECT_OTHERS
            = "SELECT "
            + "PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_TITLE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_VERSION_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_ADDRESS_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LATITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LONGITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_ENDING_DATE_KEY + ", "
            //+ "PUBS." + FCPublication.PUBLICATION_PHOTO_URL + ", "
            //+ "PUBS." + FCPublication.PUBLICATION_IMAGE_BYTEARRAY_KEY + ", "
            + "COUNT (REGS." + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_ID + ") "
            + FCPublication.PUBLICATION_NUMBER_OF_REGISTERED + ", "
            + "CASE WHEN GROUPS." + Group.GROUP_NAME_KEY + " IS NULL THEN '0' ELSE GROUPS."
            + Group.GROUP_NAME_KEY + " END " + FCPublication.PUBLICATION_GROUP_NAME;

    private static final String RAW_FOR_LIST_SELECT_MY
            = "SELECT "
            + "PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_TITLE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_VERSION_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_ENDING_DATE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_PHOTO_URL + ", "
            + "PUBS." + FCPublication.PUBLICATION_IS_ON_AIR_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_ADDRESS_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LATITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LONGITUDE_KEY + ", "
//            + "1 " + FCPublication.PUBLICATION_NUMBER_OF_REGISTERED + ", "
//            + "1 " + FCPublication.PUBLICATION_GROUP_NAME;
            + "COUNT (REGS." + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_ID + ") "
            + FCPublication.PUBLICATION_NUMBER_OF_REGISTERED + ", "
            + "CASE WHEN GROUPS." + Group.GROUP_NAME_KEY + " IS NULL THEN '0' ELSE GROUPS."
            + Group.GROUP_NAME_KEY + " END " + FCPublication.PUBLICATION_GROUP_NAME;

    private static final String RAW_FOR_MY_LIST_GROUP
            = " GROUP BY "
            + "PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_TITLE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_VERSION_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_ENDING_DATE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_PHOTO_URL + ", "
            + "PUBS." + FCPublication.PUBLICATION_IS_ON_AIR_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_ADDRESS_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LATITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LONGITUDE_KEY + ", "
            + "GROUPS." + Group.GROUP_NAME_KEY;


    private static final String RAW_FOR_LIST_FROM_OTHERS
            = " FROM " + FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME + " PUBS "
            + "LEFT JOIN " + RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME + " REGS "
            + "ON PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY
            + " = REGS." + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID
            + " LEFT JOIN " + GroupTable.GROUP_TABLE_NAME + " GROUPS "
            + "ON PUBS." + FCPublication.PUBLICATION_AUDIENCE_KEY
            + " = GROUPS." + Group.GROUP_ID_KEY;

    private static final String RAW_FOR_LIST_FROM_MY
            = " FROM " + FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME + " PUBS "
            + " LEFT JOIN " + GroupTable.GROUP_TABLE_NAME + " GROUPS "
            + "ON PUBS." + FCPublication.PUBLICATION_AUDIENCE_KEY
            + " = GROUPS." + Group.GROUP_ID_KEY
            + " LEFT JOIN " + RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME + " REGS "
            + "ON PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY
            + " = REGS." + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID;

    private static final String RAW_FOR_LIST_GROUP
            = " GROUP BY "
            + "PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_ADDRESS_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LATITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LONGITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_VERSION_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_ENDING_DATE_KEY + ", "
            //+ "PUBS." + FCPublication.PUBLICATION_IMAGE_BYTEARRAY_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_PHOTO_URL;/**/

    private static final String getRawForListOrderBy(String tableName, String fieldName, boolean isDesc) {
        return " ORDER BY " + tableName + "." + fieldName + (isDesc ? " DESC" : " ASC");
    }

    private static final String getRawForListOrderByDistance(String latitude, String longitude) {
        return " ORDER BY (PUBS." + FCPublication.PUBLICATION_LATITUDE_KEY + " - " + latitude
                + ")*(PUBS." + FCPublication.PUBLICATION_LATITUDE_KEY + " - " + latitude
                + ") + (PUBS." + FCPublication.PUBLICATION_LONGITUDE_KEY + " - " + longitude
                + ")*(PUBS." + FCPublication.PUBLICATION_LONGITUDE_KEY + " - " + longitude
                + ") ASC";
    }

    private static final String getRawForListOrderByLessRegs() {
        return " ORDER BY COUNT (REGS." + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_ID + ") ASC ";
    }

    private static final String getRawForListWhere(String tableName, String fieldName, String operator, String value, boolean isAdditional) {
        return (isAdditional ? " " : " WHERE ") + tableName + "." + fieldName + " " + operator + " '" + value + "'";
    }

    public static final String RAW_SELECT_NEW_NEGATIVE_ID
            = " SELECT " + FCPublication.PUBLICATION_UNIQUE_ID_KEY
            + " - 1 AS " + FCPublication.PUBLICATION_NEW_NEGATIVE_ID
            + " FROM " + FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME
            + " ORDER BY " + FCPublication.PUBLICATION_UNIQUE_ID_KEY + " LIMIT 1";

    public static final String RAW_SELECT_NEW_NEGATIVE_ID_REGISTRATION_FOR_PUBLICATION
            = "SELECT " + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_ID
            + " - 1 AS " + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_NEW_NEGATIVE_ID
            + " FROM " + RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME
            + " ORDER BY " + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_ID + " LIMIT 1";

    public static final String RAW_SELECT_NEW_NEGATIVE_ID_REPORT_FOR_PUBLICATION
            = "SELECT " + PublicationReport.PUBLICATION_REPORT_FIELD_KEY_ID
            + " - 1 AS " + PublicationReport.PUBLICATION_REPORT_FIELD_KEY_NEG_ID
            + " FROM " + PublicationReportsTable.PUBLICATION_REPORTS_TABLE_NAME
            + " ORDER BY " + PublicationReport.PUBLICATION_REPORT_FIELD_KEY_ID + " LIMIT 1";

    public static final String RAW_SELECT_PREVIOUS_ADDRESSES
            = "SELECT " + FCPublication.PUBLICATION_ADDRESS_KEY + ", "
            + FCPublication.PUBLICATION_LATITUDE_KEY + ", "
            + FCPublication.PUBLICATION_LONGITUDE_KEY
            + " FROM " + FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME
            + " WHERE " + FCPublication.PUBLICATION_PUBLISHER_UUID_KEY + " = '{0}' "
            + " ORDER BY " + FCPublication.PUBLICATION_UNIQUE_ID_KEY + " DESC ";

    private static final String UNIX_TIME_NOW = "STRFTIME('%s','now')";

    public static final String RAW_SELECT_ALL_PUBS_FOR_MAP_MARKERS
            = "SELECT "
            + " PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY + ", "
            + " PUBS." + FCPublication.PUBLICATION_TITLE_KEY + ", "
            + " PUBS." + FCPublication.PUBLICATION_LATITUDE_KEY + ", "
            + " PUBS." + FCPublication.PUBLICATION_LONGITUDE_KEY + ", "
            + " PUBS." + FCPublication.PUBLICATION_VERSION_KEY + ", "
            + "COUNT (REGS." + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_ID + ") "
            + FCPublication.PUBLICATION_NUMBER_OF_REGISTERED
            + " FROM " + FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME + " PUBS "
            + "LEFT JOIN " + RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME + " REGS "
            + "ON PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY
            + " = REGS." + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID
            + " WHERE PUBS." + FCPublication.PUBLICATION_IS_ON_AIR_KEY + " = 1 "
            + " AND PUBS." + FCPublication.PUBLICATION_STARTING_DATE_KEY + " < " + UNIX_TIME_NOW
            + " AND PUBS." + FCPublication.PUBLICATION_ENDING_DATE_KEY + " > " + UNIX_TIME_NOW
            + " GROUP BY "
            + "PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_TITLE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LATITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LONGITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_VERSION_KEY;


//    public static final String RAW_SELECT_REPORTS_FOR_PUB_DETAILS
//            = "SELECT " + PublicationReport.PUBLICATION_REPORT_FIELD_KEY_REPORT + ", "
//            + PublicationReport.PUBLICATION_REPORT_FIELD_KEY_DATE
//            + " FROM " + PublicationReportsTable.PUBLICATION_REPORTS_TABLE_NAME
//            + " WHERE " + PublicationReport.PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_ID + " = {0} "
//            + " ORDER BY " + PublicationReport.PUBLICATION_REPORT_FIELD_KEY_ID + " DESC";

/*
    public static final String RAW_SELECT_FOR_LIST_ALL_PUBS_ID_DESC
            = "SELECT "
            + "PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_TITLE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_ADDRESS_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LATITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LONGITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_PHOTO_URL + ", "
            + "COUNT (REGS." + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_ID + ") "
            + FCPublication.PUBLICATION_NUMBER_OF_REGISTERED
            + " FROM " + FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME + " PUBS "
            + "LEFT JOIN " + RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME + " REGS "
            + "ON PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY
            + " = REGS." + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID
            + " GROUP BY "
            + "PUBS." + FCPublication.PUBLICATION_UNIQUE_ID_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_ADDRESS_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LATITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_LONGITUDE_KEY + ", "
            + "PUBS." + FCPublication.PUBLICATION_PHOTO_URL
            + " ORDER BY PUBS." +FCPublication.PUBLICATION_UNIQUE_ID_KEY + " DESC";
*/

}
