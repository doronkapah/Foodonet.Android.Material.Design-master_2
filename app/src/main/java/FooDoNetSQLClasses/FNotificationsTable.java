package FooDoNetSQLClasses;

import android.database.sqlite.SQLiteDatabase;

import DataModel.FNotification;

/**
 * Created by Asher on 22-Jul-16.
 */
public class FNotificationsTable {
    public static final String FNOTIFICATIONSS_TABLE_NAME = "FNOTIFICATIONS";

    private static String GetCreateTableCommandText() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(FNOTIFICATIONSS_TABLE_NAME);
        sb.append("(");
        sb.append(FNotification.FNOTIFICATION_KEY_ID);
        sb.append(" integer primary key AUTOINCREMENT not null, ");
        sb.append(FNotification.FNOTIFICATION_KEY_DATE_ARRIVED);
        sb.append(" long not null, ");
        sb.append(FNotification.FNOTIFICATION_KEY_TYPE);
        sb.append(" integer not null, ");
        sb.append(FNotification.FNOTIFICATION_KEY_PUBLICATION_OR_GROUP_ID);
        sb.append(" integer not null, ");
        sb.append(FNotification.FNOTIFICATION_KEY_PUBLICATION_OR_GROUP_TITLE);
        sb.append(" text null, ");
        sb.append(FNotification.FNOTIFICATION_KEY_PUBLICATION_VERSION);
        sb.append(" integer null, ");
        sb.append(FNotification.FNOTIFICATION_KEY_LATITUDE);
        sb.append(" real null, ");
        sb.append(FNotification.FNOTIFICATION_KEY_LONGITUDE);
        sb.append(" real null);");
        return sb.toString();
    }

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(GetCreateTableCommandText());
    }

    public static void onUpgrade(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + FNOTIFICATIONSS_TABLE_NAME);
        onCreate(db);
    }

}
