package upp.foodonet.material;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;

import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import DataModel.FNotification;
import DataModel.Group;
import DataModel.GroupMember;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;
import FooDoNetSQLClasses.FCPublicationsTable;
import FooDoNetSQLClasses.FNotificationsTable;
import FooDoNetSQLClasses.FooDoNetSQLHelper;
import FooDoNetSQLClasses.GroupMemberTable;
import FooDoNetSQLClasses.GroupTable;
import FooDoNetSQLClasses.PublicationReportsTable;
import FooDoNetSQLClasses.RegisteredForPublicationTable;

/**
 * Created by Asher on 14.07.2015.
 */
public class FooDoNetSQLProvider extends ContentProvider {

    private FooDoNetSQLHelper database;

    private static final String MY_TAG = "food_contentProvider";

    private static final int PUBLICATIONS = 10;
    private static final int PUBLICATION_DELETE_COMPLETELY = 11;
    private static final int PUBLICATION_ID = 20;
    private static final int PUBLICATION_ID_NEGATIVE = 21;
    private static final int PUBLICATIONS_ALL_FOR_LIST_SORTED_ID_DESC = 30;
    private static final int PUBLICATIONS_MY_FOR_LIST_SORTED_ID_DESC = 31;
    private static final int PUBLICATIONS_FOR_LIST_BY_FILTER_ID = 32;
    private static final int PUBLICATIONS_FOR_MAP_MARKERS = 33;
    private static final int REGS_FOR_PUBLICATION = 40;
    private static final int INSERT_REG_FOR_PUBLICATION = 41;
    private static final int DELETE_REG_FOR_PUBLICATION = 42;
    private static final int GET_NEW_NEGATIVE_ID_CODE = 43;
    private static final int REGS_FOR_PUBLICATION_BY_PUB_ID = 44;
    private static final int REGS_FOR_PUBLICATION_BY_PUB_NEG_ID = 45;
    private static final int REGS_FOR_PUBLICATION_NEW_NEGATIVE_ID = 46;
    private static final int REGS_FOR_PUBLICATION_REMOVE_MYSELF = 47;
    private static final int DELETE_REG_USER_BY_PUB_ID = 48;
    private static final int PUBLICATION_REPORT = 50;
    private static final int PUBLICATION_REPORT_ID = 51;
    private static final int PUBLICATION_REPORTS_BY_ID = 52;
    private static final int PUBLICATION_REPORTS_BY_NEG_ID = 53;
    private static final int UPDATE_IMAGES_FOR_PUBLICATIONS = 60;
    private static final int REPORT_NEW_NEGATIVE_ID = 70;
    private static final int PREVIOUS_ADDRESSES = 80;
    private static final int REPORTS_LIST_FOR_PUBLICATION = 90;
    private static final int GROUP = 100;
    private static final int GROUP_BY_ID = 101;
    private static final int GROUPS_LIST = 102;
    private static final int GROUP_MEMBERS_BY_GROUP_ID = 105;
    private static final int GROUP_MEMBER_BY_MEMBER_ID = 110;
    private static final int GROUP_MEMBER = 111;
    private static final int NOTIFICATIONS = 120;
    private static final int NOTIFICATION_BY_ID = 121;

    private static final String AUTHORITY = "foodonet.foodcollector.sqlprovider";

    private static final String BASE_PATH = "foodonet";

    private static final String EXT_NEGATIVE_ID = "/neg_id";

    private static final String EXT_ALL_PUBS_FOR_LIST_ID_DESC_PATH = "/Pubs_ALL_for_list_id_desc";
    private static final String EXT_MY_PUBS_FOR_LIST_ID_DESC_PATH = "/Pubs_MY_for_list_id_desc";

    private static final String EXT_PUBS_FOR_LIST_BY_FILTER_ID = "/Pubs_for_list_by_filter";

    private static final String EXT_PUBS_FOR_MAP_MARKERS = "/Pubs_for_map_markers";

    private static final String EXT_ALL_REGS = "/AllRegisteredForPublications";

    private static final String EXT_GET_NEW_NEGATIVE_ID = "/GetNegativeID";

    private static final String EXT_INSERT_REG = "/InsertRegisteredForPublication";

    private static final String EXT_DELETE_REG = "/DeleteRegisteredForPublication";

    private static final String EXT_DELETE_REG_USER_BY_PUB_ID = "/DeleteRegisteredUserByPubId";

    private static final String EXT_REGS_FOR_PUBLICATION_BY_ID = "/RegisteredByPublicationID";

    private static final String EXT_REGS_FOR_PUBLICATION_NEW_NEGATIVE_ID = "/RegForPubNewNegID";

    private static final String EXT_REGS_FOR_PUBLICATION_REMOVE_MYSELF = "/UnregMyselfFromPub";

    private static final String EXT_PUBLICATION_REPORT = "/PublicationReport";

    private static final String EXT_PUBLICATION_REPORTS_BY_PUB_ID = "/PublicationReportsByPubID";

    private static final String EXT_UPDATE_PUBLICATION_IMAGES = "/UpdateImages";

    private static final String EXT_REPORT_NEW_NEGATIVE_ID = "/ReportNewNegativeID";

    private static final String EXT_PREVIOUS_ADDRESSES = "/PrevAddresses";

    private static final String EXT_REPORTS_LIST_FOR_PUB = "/ReportsListForPublication";

    private static final String EXT_PUBLICATION_REMOVE_COMPLETELY = "/RemovePublicationCompletely";

    private static final String EXT_GROUP = "/Group";

    private static final String EXT_GROUPS_LIST = "/GroupsList";

    private static final String EXT_GROUP_MEMBER = "/GroupMembers";

    private static final String EXT_GROUP_MEMBERS_BY_GROUP = "GroupMembersByGroup";

    private static final String EXT_NOTIFICATIONS = "/Notifications";

    public static final String BASE_STRING_FOR_URI = "content://" + AUTHORITY + "/" + BASE_PATH;

    public static final Uri CONTENT_URI = Uri.parse(BASE_STRING_FOR_URI);

    public static final Uri URI_PUBLICATION_ID_NEGATIVE
            = Uri.parse(BASE_STRING_FOR_URI + EXT_NEGATIVE_ID);

    public static final Uri URI_GET_MY_PUBS_FOR_LIST_ID_DESC
            = Uri.parse(BASE_STRING_FOR_URI + EXT_MY_PUBS_FOR_LIST_ID_DESC_PATH);
    public static final Uri URI_GET_ALL_PUBS_FOR_LIST_ID_DESC
            = Uri.parse(BASE_STRING_FOR_URI + EXT_ALL_PUBS_FOR_LIST_ID_DESC_PATH);

    public static final Uri URI_GET_PUBS_FOR_LIST_BY_FILTER_ID
            = Uri.parse(BASE_STRING_FOR_URI + EXT_PUBS_FOR_LIST_BY_FILTER_ID);

    public static final Uri URI_GET_PUBS_FOR_MAP_MARKERS
            = Uri.parse(BASE_STRING_FOR_URI + EXT_PUBS_FOR_MAP_MARKERS);

    public static final Uri URI_GET_ALL_REGS = Uri.parse(BASE_STRING_FOR_URI + EXT_ALL_REGS);
    public static final Uri URI_INSERT_REGISTERED_FOR_PUBLICATION
            = Uri.parse(BASE_STRING_FOR_URI + EXT_INSERT_REG);
    public static final Uri URI_DELETE_REGISTERED_FOR_PUBLICATION
            = Uri.parse(BASE_STRING_FOR_URI + EXT_DELETE_REG);

    public static final Uri URI_DELETE_REGISTERED_USER_BY_PUBLICATION_ID
            = Uri.parse(BASE_STRING_FOR_URI + EXT_DELETE_REG_USER_BY_PUB_ID);

    public static final Uri URI_GET_NEW_NEGATIVE_ID
            = Uri.parse(BASE_STRING_FOR_URI + EXT_GET_NEW_NEGATIVE_ID);
    public static final Uri URI_GET_REGISTERED_BY_PUBLICATION_ID
            = Uri.parse(BASE_STRING_FOR_URI + EXT_REGS_FOR_PUBLICATION_BY_ID);
    public static final Uri URI_GET_REGISTERED_BY_PUBLICATION_NEG_ID
            = Uri.parse(BASE_STRING_FOR_URI + EXT_REGS_FOR_PUBLICATION_BY_ID + EXT_NEGATIVE_ID);
    public static final Uri URI_GET_REG_FOR_PUB_NEW_NEG_ID
            = Uri.parse(BASE_STRING_FOR_URI + EXT_REGS_FOR_PUBLICATION_NEW_NEGATIVE_ID);
    public static final Uri URI_REMOVE_MYSELF_FROM_REGS_FOR_PUBLICATION
            = Uri.parse(BASE_STRING_FOR_URI + EXT_REGS_FOR_PUBLICATION_REMOVE_MYSELF);
    public static final Uri URI_GET_ALL_REPORTS
            = Uri.parse(BASE_STRING_FOR_URI + EXT_PUBLICATION_REPORT);
    public static final Uri URI_GET_ALL_REPORTS_BY_PUB_ID
            = Uri.parse(BASE_STRING_FOR_URI + EXT_PUBLICATION_REPORTS_BY_PUB_ID);
    public static final Uri URI_GET_ALL_REPORTS_BY_PUB_NEG_ID
            = Uri.parse(URI_GET_ALL_REPORTS_BY_PUB_ID + EXT_NEGATIVE_ID);
    public static final Uri URI_REPORT_GET_NEW_NEGATIVE_ID
            = Uri.parse(BASE_STRING_FOR_URI + EXT_REPORT_NEW_NEGATIVE_ID);
    public static final Uri URI_UPDATE_IMAGES
            = Uri.parse(BASE_STRING_FOR_URI + EXT_UPDATE_PUBLICATION_IMAGES);
    public static final Uri URI_PREVIOUS_ADDRESSES
            = Uri.parse(BASE_STRING_FOR_URI + EXT_PREVIOUS_ADDRESSES);
    public static final Uri URI_REPORTS_LIST_FOR_PUB_DETAILS
            = Uri.parse(BASE_STRING_FOR_URI + EXT_REPORTS_LIST_FOR_PUB);
    public static final Uri URI_REMOVE_PUBLICATION_COMPLETELY
            = Uri.parse(BASE_STRING_FOR_URI + EXT_PUBLICATION_REMOVE_COMPLETELY);
    public static final Uri URI_GROUP
            = Uri.parse(BASE_STRING_FOR_URI + EXT_GROUP);
    public static final Uri URI_GROUPS_LIST
            = Uri.parse(BASE_STRING_FOR_URI + EXT_GROUPS_LIST);
    public static final Uri URI_GROUP_MEMBERS
            = Uri.parse(BASE_STRING_FOR_URI + EXT_GROUP_MEMBER);
    public static final Uri URI_GROUP_MEMBERS_BY_GROUP
            = Uri.parse(BASE_STRING_FOR_URI + EXT_GROUP_MEMBERS_BY_GROUP);
    public static final Uri URI_NOTIFICATIONS
            = Uri.parse(BASE_STRING_FOR_URI + EXT_NOTIFICATIONS);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/publications";

    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/publication";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, PUBLICATIONS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", PUBLICATION_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_NEGATIVE_ID + "/#", PUBLICATION_ID_NEGATIVE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_ALL_PUBS_FOR_LIST_ID_DESC_PATH, PUBLICATIONS_ALL_FOR_LIST_SORTED_ID_DESC);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_MY_PUBS_FOR_LIST_ID_DESC_PATH, PUBLICATIONS_MY_FOR_LIST_SORTED_ID_DESC);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_PUBS_FOR_LIST_BY_FILTER_ID + "/#", PUBLICATIONS_FOR_LIST_BY_FILTER_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_ALL_REGS, REGS_FOR_PUBLICATION);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_INSERT_REG, INSERT_REG_FOR_PUBLICATION);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_DELETE_REG + "/#", DELETE_REG_FOR_PUBLICATION);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_DELETE_REG_USER_BY_PUB_ID + "/#", DELETE_REG_USER_BY_PUB_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_GET_NEW_NEGATIVE_ID, GET_NEW_NEGATIVE_ID_CODE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_REGS_FOR_PUBLICATION_BY_ID + "/#", REGS_FOR_PUBLICATION_BY_PUB_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_REGS_FOR_PUBLICATION_BY_ID + EXT_NEGATIVE_ID + "/#", REGS_FOR_PUBLICATION_BY_PUB_NEG_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_PUBLICATION_REPORT, PUBLICATION_REPORT);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_PUBLICATION_REPORT + "/#", PUBLICATION_REPORT_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_PUBLICATION_REPORTS_BY_PUB_ID, PUBLICATION_REPORTS_BY_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_PUBLICATION_REPORTS_BY_PUB_ID + "/#", PUBLICATION_REPORTS_BY_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_PUBLICATION_REPORTS_BY_PUB_ID + EXT_NEGATIVE_ID + "/#", PUBLICATION_REPORTS_BY_NEG_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_UPDATE_PUBLICATION_IMAGES, UPDATE_IMAGES_FOR_PUBLICATIONS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_REGS_FOR_PUBLICATION_NEW_NEGATIVE_ID, REGS_FOR_PUBLICATION_NEW_NEGATIVE_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_REGS_FOR_PUBLICATION_REMOVE_MYSELF, REGS_FOR_PUBLICATION_REMOVE_MYSELF);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_REPORT_NEW_NEGATIVE_ID, REPORT_NEW_NEGATIVE_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_PREVIOUS_ADDRESSES, PREVIOUS_ADDRESSES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_REPORTS_LIST_FOR_PUB + "/#", REPORTS_LIST_FOR_PUBLICATION);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_PUBLICATION_REMOVE_COMPLETELY + "/#", PUBLICATION_DELETE_COMPLETELY);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_PUBS_FOR_MAP_MARKERS, PUBLICATIONS_FOR_MAP_MARKERS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_GROUP, GROUP);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_GROUP + "/#", GROUP_BY_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_GROUP_MEMBER, GROUP_MEMBER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_GROUP_MEMBER + "/#", GROUP_MEMBER_BY_MEMBER_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_GROUP_MEMBERS_BY_GROUP + "/#", GROUP_MEMBERS_BY_GROUP_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_GROUPS_LIST, GROUPS_LIST);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_NOTIFICATIONS, NOTIFICATIONS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + EXT_NOTIFICATIONS + "/#", NOTIFICATION_BY_ID);
    }

    @Override
    public boolean onCreate() {
        database = new FooDoNetSQLHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        int uriType = sURIMatcher.match(uri);
        checkColumns(projection, uriType);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String imei;

        String rawQuery;

        switch (uriType) {
            case PUBLICATIONS:
                queryBuilder.setTables(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME);// + "," + RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME
                break;
            case PUBLICATION_ID:
                queryBuilder.setTables(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME);// + "," + RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME
                queryBuilder.appendWhere(FCPublication.PUBLICATION_UNIQUE_ID_KEY + "=" + uri.getLastPathSegment());
                break;
            case PUBLICATION_ID_NEGATIVE:
                queryBuilder.setTables(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME);
                queryBuilder.appendWhere(FCPublication.PUBLICATION_UNIQUE_ID_KEY + "=-" + uri.getLastPathSegment());
                break;
            case PUBLICATIONS_ALL_FOR_LIST_SORTED_ID_DESC:
                imei = CommonUtil.GetIMEI(this.getContext());
                rawQuery = FooDoNetSQLHelper.GetRawSelectPublicationsForListByFilterID(
                        FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_NEWEST, imei);
                Log.i(MY_TAG, rawQuery);
                return database.getReadableDatabase()
                        .rawQuery(rawQuery, null);
            case PUBLICATIONS_MY_FOR_LIST_SORTED_ID_DESC:
                imei = CommonUtil.GetIMEI(this.getContext());
                rawQuery = FooDoNetSQLHelper.GetRawSelectPublicationsForListByFilterID(
                        FooDoNetSQLHelper.FILTER_ID_LIST_MY_BY_ENDING_SOON, imei);
                Log.i(MY_TAG, rawQuery);
                return database.getReadableDatabase()
                        .rawQuery(rawQuery, null);
            case PUBLICATIONS_FOR_LIST_BY_FILTER_ID:
                String filterIDStr = uri.getLastPathSegment();
                int filterID = Integer.parseInt(filterIDStr);
                String stringFilter = "";
                imei = CommonUtil.GetIMEI(this.getContext());
                if (filterID == FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_TEXT_FILTER
                        || filterID == FooDoNetSQLHelper.FILTER_ID_LIST_MY_BY_TEXT_FILTER)
                    stringFilter = CommonUtil.GetFilterStringFromPreferences(this.getContext());
                if (filterID == FooDoNetSQLHelper.FILTER_ID_LIST_ALL_BY_CLOSEST) {
                    LatLng myLocation = CommonUtil.GetMyLocationFromPreferences(this.getContext());
                    rawQuery = FooDoNetSQLHelper.GetRawSelectPublicationsForListByFilterID(
                            filterID, imei, String.valueOf(myLocation.latitude),
                            String.valueOf(myLocation.longitude));
                    Log.i(MY_TAG, rawQuery);
                    return database.getReadableDatabase()
                            .rawQuery(rawQuery, null);
                }
                rawQuery = FooDoNetSQLHelper.GetRawSelectPublicationsForListByFilterID(
                        filterID, imei, stringFilter);
                Log.i(MY_TAG, rawQuery);
                return database.getReadableDatabase()
                        .rawQuery(rawQuery, null);
            case REGS_FOR_PUBLICATION:
                queryBuilder.setTables(RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME);
                break;
            case REGS_FOR_PUBLICATION_BY_PUB_ID:
                queryBuilder.setTables(RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME);
                queryBuilder.appendWhere(RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID
                        + " = " + uri.getLastPathSegment());
                break;
            case REGS_FOR_PUBLICATION_BY_PUB_NEG_ID:
                queryBuilder.setTables(RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME);
                queryBuilder.appendWhere(RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID
                        + " = -" + uri.getLastPathSegment());
                break;
            case GET_NEW_NEGATIVE_ID_CODE:
                return database.getReadableDatabase().rawQuery(FooDoNetSQLHelper.RAW_SELECT_NEW_NEGATIVE_ID, null);
            case PUBLICATION_REPORT:
                queryBuilder.setTables(PublicationReportsTable.PUBLICATION_REPORTS_TABLE_NAME);
                break;
            case PUBLICATION_REPORTS_BY_ID:
                queryBuilder.setTables(PublicationReportsTable.PUBLICATION_REPORTS_TABLE_NAME);
                queryBuilder.appendWhere(PublicationReport.PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_ID
                        + " = " + uri.getLastPathSegment());
                break;
            case PUBLICATION_REPORTS_BY_NEG_ID:
                queryBuilder.setTables(PublicationReportsTable.PUBLICATION_REPORTS_TABLE_NAME);
                queryBuilder.appendWhere(PublicationReport.PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_ID
                        + " = -" + uri.getLastPathSegment());
                break;
            case REGS_FOR_PUBLICATION_NEW_NEGATIVE_ID:
                return database.getReadableDatabase()
                        .rawQuery(FooDoNetSQLHelper.RAW_SELECT_NEW_NEGATIVE_ID_REGISTRATION_FOR_PUBLICATION, null);
            case REPORT_NEW_NEGATIVE_ID:
                return database.getReadableDatabase()
                        .rawQuery(FooDoNetSQLHelper.RAW_SELECT_NEW_NEGATIVE_ID_REPORT_FOR_PUBLICATION, null);
            case PREVIOUS_ADDRESSES:
                return database.getReadableDatabase()
                        .rawQuery(FooDoNetSQLHelper.RAW_SELECT_PREVIOUS_ADDRESSES.replace("{0}", CommonUtil.GetIMEI(getContext())), null);
            case PUBLICATIONS_FOR_MAP_MARKERS:
                return database.getReadableDatabase()
                        .rawQuery(FooDoNetSQLHelper.RAW_SELECT_ALL_PUBS_FOR_MAP_MARKERS, null);
            case GROUP:
                queryBuilder.setTables(GroupTable.GROUP_TABLE_NAME);
                break;
            case GROUP_BY_ID:
                queryBuilder.setTables(GroupTable.GROUP_TABLE_NAME);
                queryBuilder.appendWhere(Group.GROUP_ID_KEY + " = " + uri.getLastPathSegment());
                break;
            case GROUP_MEMBERS_BY_GROUP_ID:
                queryBuilder.setTables(GroupMemberTable.GROUP_MEMBER_TABLE_NAME);
                queryBuilder.appendWhere(GroupMember.GROUP_MEMBER_GROUP_ID_KEY + " = " + uri.getLastPathSegment());
                break;
            case GROUP_MEMBER:
                queryBuilder.setTables(GroupMemberTable.GROUP_MEMBER_TABLE_NAME);
                break;
            case GROUP_MEMBER_BY_MEMBER_ID:
                queryBuilder.setTables(GroupMemberTable.GROUP_MEMBER_TABLE_NAME);
                queryBuilder.appendWhere(GroupMember.GROUP_MEMBER_ID_KEY + " = " + uri.getLastPathSegment());
                break;
            case GROUPS_LIST:
                Cursor cGroupsList = database.getReadableDatabase().rawQuery(GroupTable.GetRawSelectGroupsForList(), null);
                cGroupsList.setNotificationUri(getContext().getContentResolver(), uri);
                return cGroupsList;
            case NOTIFICATIONS:
                queryBuilder.setTables(FNotificationsTable.FNOTIFICATIONSS_TABLE_NAME);
                break;
//            case REPORTS_LIST_FOR_PUBLICATION:
//                return database.getReadableDatabase()
//                        .rawQuery(FooDoNetSQLHelper.RAW_SELECT_REPORTS_FOR_PUB_DETAILS.replace("{0}", String.valueOf(uri.getLastPathSegment())), null);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case PUBLICATIONS:
                id = db.insert(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME, null, values);
                break;
            case INSERT_REG_FOR_PUBLICATION:
                id = db.insert(RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME, null, values);
                break;
            case PUBLICATION_REPORT:
                id = db.insert(PublicationReportsTable.PUBLICATION_REPORTS_TABLE_NAME, null, values);
                break;
            case GROUP:
                id = db.insert(GroupTable.GROUP_TABLE_NAME, null, values);
                break;
            case GROUP_MEMBER:
                id = db.insert(GroupMemberTable.GROUP_MEMBER_TABLE_NAME, null, values);
                break;
            case NOTIFICATIONS:
                id = db.insert(FNotificationsTable.FNOTIFICATIONSS_TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
//        if (id == -1)
//            Log.e(MY_TAG, "failed inserting: " + uri);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = database.getWritableDatabase();
        int rowsDeleted = 0;
        String id;
        switch (uriType) {
            case PUBLICATIONS:
                rowsDeleted = db.delete(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME, selection, selectionArgs);
                break;
            case PUBLICATION_ID:
            case PUBLICATION_ID_NEGATIVE:
                id = uri.getLastPathSegment();
                if (uriType == PUBLICATION_ID_NEGATIVE)
                    id = "-" + id;
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME,
                            FCPublication.PUBLICATION_UNIQUE_ID_KEY + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME,
                            FCPublication.PUBLICATION_UNIQUE_ID_KEY + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case REGS_FOR_PUBLICATION:
                rowsDeleted = db.delete(RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME,
                        selection, selectionArgs);
                break;
            case DELETE_REG_FOR_PUBLICATION:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted
                            = db.delete(RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME,
                            RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME,
                            RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_ID + "=" + id + selection, selectionArgs);
                }
                break;
            case DELETE_REG_USER_BY_PUB_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted
                            = db.delete(RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME,
                            RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME,
                            RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID + "=" + id + selection, selectionArgs);
                }
                break;
            case PUBLICATION_REPORT:
                rowsDeleted = db.delete(PublicationReportsTable.PUBLICATION_REPORTS_TABLE_NAME,
                        selection, selectionArgs);
                break;
            case PUBLICATION_REPORTS_BY_ID:
                id = uri.getLastPathSegment();
                    rowsDeleted = db.delete(PublicationReportsTable.PUBLICATION_REPORTS_TABLE_NAME,
                        PublicationReport.PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_ID + "=" + id, null);
                break;
            case PUBLICATION_REPORT_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(PublicationReportsTable.PUBLICATION_REPORTS_TABLE_NAME,
                            PublicationReport.PUBLICATION_REPORT_FIELD_KEY_ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(PublicationReportsTable.PUBLICATION_REPORTS_TABLE_NAME,
                            PublicationReport.PUBLICATION_REPORT_FIELD_KEY_ID + "=" + id + selection, null);
                }
                break;
            case REGS_FOR_PUBLICATION_REMOVE_MYSELF:
                rowsDeleted = db.delete(RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME,
                        selection, null);
                break;
            case PUBLICATION_DELETE_COMPLETELY:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(id)) {
                    Log.e(MY_TAG, "empty id");
                    return -1;
                }
                rowsDeleted = 0;
                rowsDeleted += db.delete(PublicationReportsTable.PUBLICATION_REPORTS_TABLE_NAME,
                        PublicationReport.PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_ID + "=" + id, null);
                rowsDeleted += db.delete(RegisteredForPublicationTable.REGISTERED_FOR_PUBLICATION_TABLE_NAME,
                        RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID + "=" + id, null);
                rowsDeleted += db.delete(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME,
                        FCPublication.PUBLICATION_UNIQUE_ID_KEY + "=" + id, null);
                return rowsDeleted;
            case GROUP_BY_ID:
                rowsDeleted = 0;
                rowsDeleted += db.delete(GroupMemberTable.GROUP_MEMBER_TABLE_NAME,
                        GroupMember.GROUP_MEMBER_GROUP_ID_KEY + " = " + uri.getLastPathSegment(), null);
                rowsDeleted += db.delete(GroupTable.GROUP_TABLE_NAME,
                        Group.GROUP_ID_KEY + " = " + uri.getLastPathSegment(), null);
                return rowsDeleted;
            case GROUP_MEMBER_BY_MEMBER_ID:
                rowsDeleted = 0;
                rowsDeleted += db.delete(GroupMemberTable.GROUP_MEMBER_TABLE_NAME,
                        GroupMember.GROUP_MEMBER_ID_KEY + " = " + uri.getLastPathSegment(), null);
                return rowsDeleted;
            case GROUP:
                rowsDeleted = 0;
                rowsDeleted += db.delete(GroupTable.GROUP_TABLE_NAME, selection, selectionArgs);
                return rowsDeleted;
            case GROUP_MEMBER:
                rowsDeleted = 0;
                rowsDeleted += db.delete(GroupMemberTable.GROUP_MEMBER_TABLE_NAME, null, null);
                return rowsDeleted;
            case NOTIFICATIONS:
                rowsDeleted = 0;
                rowsDeleted += db.delete(FNotificationsTable.FNOTIFICATIONSS_TABLE_NAME,
                        selection, selectionArgs);
                return rowsDeleted;
            case NOTIFICATION_BY_ID:
                rowsDeleted = 0;
                rowsDeleted += db.delete(FNotificationsTable.FNOTIFICATIONSS_TABLE_NAME,
                        FNotification.FNOTIFICATION_KEY_ID + " = " + uri.getLastPathSegment(), null);
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = database.getWritableDatabase();
        int rowsUpdated = 0;

        switch (uriType) {
            case PUBLICATIONS:
                rowsUpdated = db.update(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            case PUBLICATION_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME,
                            values, FCPublication.PUBLICATION_UNIQUE_ID_KEY + "=" + id, null);
                } else {
                    rowsUpdated = db.update(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME,
                            values, FCPublication.PUBLICATION_UNIQUE_ID_KEY + "=" + id
                                    + " and " + selection, selectionArgs);
                }
                break;
            case PUBLICATION_ID_NEGATIVE:
                String idToUpdate = uri.getLastPathSegment();
                rowsUpdated = db.update(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME,
                        values, FCPublication.PUBLICATION_UNIQUE_ID_KEY + "= -" + idToUpdate, null);
                break;
            case UPDATE_IMAGES_FOR_PUBLICATIONS:
/*
                for(String pubIdString : values.keySet()){
                    int pubId = Integer.parseInt(pubIdString);
                    ContentValues cv = new ContentValues();
                    cv.put(FCPublication.PUBLICATION_IMAGE_BYTEARRAY_KEY, values.getAsByteArray(pubIdString));
                    rowsUpdated += db.update(FCPublicationsTable.FCPUBLICATIONS_TABLE_NAME,
                            cv, FCPublication.PUBLICATION_UNIQUE_ID_KEY + " = " + pubIdString, null);
                }
*/
                break;
            case GROUP_BY_ID:
                rowsUpdated = db.update(GroupTable.GROUP_TABLE_NAME,
                        values, Group.GROUP_ID_KEY + "=" + uri.getLastPathSegment(), null);
                break;
            case GROUP_MEMBER_BY_MEMBER_ID:
                rowsUpdated = db.update(GroupMemberTable.GROUP_MEMBER_TABLE_NAME,
                        values, GroupMember.GROUP_MEMBER_ID_KEY + " = " + uri.getLastPathSegment(), null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection, int action) {
        String[] available;
        switch (action) {
            case PUBLICATIONS:
            case PUBLICATION_ID:
            case PUBLICATION_ID_NEGATIVE:
            case UPDATE_IMAGES_FOR_PUBLICATIONS:
            case PUBLICATIONS_FOR_MAP_MARKERS:
                available = FCPublication.GetColumnNamesArray();
                break;
            case PUBLICATIONS_ALL_FOR_LIST_SORTED_ID_DESC:
            case PUBLICATIONS_MY_FOR_LIST_SORTED_ID_DESC:
            case PUBLICATIONS_FOR_LIST_BY_FILTER_ID:
                available = FCPublication.GetColumnNamesForListArray();
                break;
            case REGS_FOR_PUBLICATION:
            case REGS_FOR_PUBLICATION_BY_PUB_ID:
            case REGS_FOR_PUBLICATION_BY_PUB_NEG_ID:
            case REGS_FOR_PUBLICATION_REMOVE_MYSELF:
                available = RegisteredUserForPublication.GetColumnNamesArray();
                break;
            case GET_NEW_NEGATIVE_ID_CODE:
                available = new String[]{FCPublication.PUBLICATION_NEW_NEGATIVE_ID};
                break;
            case REGS_FOR_PUBLICATION_NEW_NEGATIVE_ID:
                available = new String[]{RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_NEW_NEGATIVE_ID};
                break;
            case REPORT_NEW_NEGATIVE_ID:
                available = new String[]{PublicationReport.PUBLICATION_REPORT_FIELD_KEY_NEG_ID};
                break;
            case PUBLICATION_REPORT:
            case PUBLICATION_REPORT_ID:
            case PUBLICATION_REPORTS_BY_ID:
            case PUBLICATION_REPORTS_BY_NEG_ID:
                available = PublicationReport.GetColumnNamesArray();
                break;
            case PREVIOUS_ADDRESSES:
                available = new String[]
                        {FCPublication.PUBLICATION_ADDRESS_KEY,
                                FCPublication.PUBLICATION_LATITUDE_KEY,
                                FCPublication.PUBLICATION_LONGITUDE_KEY};
                break;
            case GROUP:
            case GROUP_BY_ID:
                available = Group.GetColumnNamesArray();
                break;
            case GROUP_MEMBERS_BY_GROUP_ID:
            case GROUP_MEMBER:
            case GROUP_MEMBER_BY_MEMBER_ID:
                available = GroupMember.GetColumnNamesArray();
                break;
            case GROUPS_LIST:
                available = Group.GetColumnNamesForListArray();
                break;
//            case REPORTS_LIST_FOR_PUBLICATION:
//                available = new String[]
//                        {PublicationReport.PUBLICATION_REPORT_FIELD_KEY_REPORT,
//                        PublicationReport.PUBLICATION_REPORT_FIELD_KEY_DATE};
//                break;
            case NOTIFICATIONS:
            case NOTIFICATION_BY_ID:
                available = FNotification.GetColumnNamesArray();
                break;
            default:
                Log.e(MY_TAG, "checkColumns got bad parameter action");
                available = new String[]{};
                break;
        }
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection - action " + action);
            }
        }
    }
}
