package FooDoNetSQLClasses;

import android.database.sqlite.SQLiteDatabase;

import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;

/**
 * Created by Asher on 30.08.2015.
 */
public class PublicationReportsTable {

    public static final String PUBLICATION_REPORTS_TABLE_NAME = "PUBLICATIONREPORTS";

    private static String GetCreateTableCommandText() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(PUBLICATION_REPORTS_TABLE_NAME);
        sb.append("(");
        sb.append(PublicationReport.PUBLICATION_REPORT_FIELD_KEY_ID);
        sb.append(" integer primary key, ");
        sb.append(PublicationReport.PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_ID);
        sb.append(" integer not null, ");
        sb.append(PublicationReport.PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_VERSION);
        sb.append(" integer not null, ");
        sb.append(PublicationReport.PUBLICATION_REPORT_FIELD_KEY_DATE);
        sb.append(" long not null, ");
        sb.append(PublicationReport.PUBLICATION_REPORT_FIELD_KEY_DEVICE_UID);
        sb.append(" text not null, ");
        sb.append(PublicationReport.PUBLICATION_REPORT_FIELD_KEY_REPORT_USER_NAME);
        sb.append(" text null, ");
        sb.append(PublicationReport.PUBLICATION_REPORT_FIELD_KEY_REPORT_CONTACT_INFO);
        sb.append(" text null, ");
        sb.append(PublicationReport.PUBLICATION_REPORT_FIELD_KEY_REPORT_USER_ID);
        sb.append(" integer not null, ");
        sb.append(PublicationReport.PUBLICATION_REPORT_FIELD_KEY_REPORT_RATING);
        sb.append(" integer not null, ");
        sb.append(PublicationReport.PUBLICATION_REPORT_FIELD_KEY_REPORT);
        sb.append(" integer not null);");
        return sb.toString();
    }

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(GetCreateTableCommandText());
    }

    public static void onUpgrade(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + PUBLICATION_REPORTS_TABLE_NAME);
        onCreate(db);
    }
}
