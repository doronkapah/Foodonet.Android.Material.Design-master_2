package FooDoNetSQLClasses;

import android.database.sqlite.SQLiteDatabase;

import DataModel.FCPublication;
import DataModel.RegisteredUserForPublication;

/**
 * Created by Asher on 21.08.2015.
 */
public class RegisteredForPublicationTable {
    public static final String REGISTERED_FOR_PUBLICATION_TABLE_NAME = "REGISTEREDFORPUBLICATION";

    private static String GetCreateTableCommandText() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(REGISTERED_FOR_PUBLICATION_TABLE_NAME);
        sb.append("(");
        sb.append(RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_ID);
        sb.append(" integer primary key, ");
        sb.append(RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID);
        sb.append(" integer not null, ");
        sb.append(RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_VERSION);
        sb.append(" integer not null, ");
        sb.append(RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_USER_ID);
        sb.append(" integer not null, ");
        sb.append(RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_USER_NAME);
        sb.append(" text not null, ");
        sb.append(RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_USER_CONTACT_INFO);
        sb.append(" text not null, ");
        sb.append(RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_DATE);
        sb.append(" long not null, ");
        sb.append(RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_DEVICE_UUID);
        sb.append(" text not null);");
        return sb.toString();
    }

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(GetCreateTableCommandText());
    }

    public static void onUpgrade(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + REGISTERED_FOR_PUBLICATION_TABLE_NAME);
        onCreate(db);
    }

}
