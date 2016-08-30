package FooDoNetSQLClasses;

import DataModel.FCPublication;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Asher on 14.07.2015.
 */
public class FCPublicationsTable {

    public static final String FCPUBLICATIONS_TABLE_NAME = "FCPUBLICATIONS";

    private static String GetCreateTableCommandText() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(FCPUBLICATIONS_TABLE_NAME);
        sb.append("(");
        sb.append(FCPublication.PUBLICATION_UNIQUE_ID_KEY);
        sb.append(" integer primary key, "); // removed autoincrement
        sb.append(FCPublication.PUBLICATION_PUBLISHER_UUID_KEY);
        sb.append(" text not null, ");
        sb.append(FCPublication.PUBLICATION_VERSION_KEY);
        sb.append(" integer not null, ");
        sb.append(FCPublication.PUBLICATION_TITLE_KEY);
        sb.append(" text not null, ");
        sb.append(FCPublication.PUBLICATION_SUBTITLE_KEY);
        sb.append(" text null, ");
        sb.append(FCPublication.PUBLICATION_ADDRESS_KEY);
        sb.append(" text null, ");
        sb.append(FCPublication.PUBLICATION_TYPE_OF_COLLECTION_KEY);
        sb.append(" integer not null, ");
        sb.append(FCPublication.PUBLICATION_LATITUDE_KEY);
        sb.append(" real null, ");
        sb.append(FCPublication.PUBLICATION_LONGITUDE_KEY);
        sb.append(" real null, ");
        sb.append(FCPublication.PUBLICATION_STARTING_DATE_KEY);
        sb.append(" long not null, ");
        sb.append(FCPublication.PUBLICATION_ENDING_DATE_KEY);
        sb.append(" long not null, ");
        sb.append(FCPublication.PUBLICATION_CONTACT_INFO_KEY);
        sb.append(" text null, ");
        sb.append(FCPublication.PUBLICATION_PHOTO_URL);
        sb.append(" text null, ");
        sb.append(FCPublication.PUBLICATION_COUNT_OF_REGISTER_USERS_KEY);
        sb.append(" int not null, ");
        sb.append(FCPublication.PUBLICATION_IS_ON_AIR_KEY);
        sb.append(" int not null, ");
        sb.append(FCPublication.PUBLICATION_PUBLISHER_ID_KEY);
        sb.append(" int not null, ");
        sb.append(FCPublication.PUBLICATION_PUBLISHER_USER_NAME_KEY);
        sb.append(" text not null, ");
        sb.append(FCPublication.PUBLICATION_PRICE_KEY);
        sb.append(" int null, ");
        sb.append(FCPublication.PUBLICATION_PRICE_DESCRIPTION_KEY);
        sb.append(" text null, ");
        sb.append(FCPublication.PUBLICATION_USER_RATING_KEY);
        sb.append(" int not null, ");
        sb.append(FCPublication.PUBLICATION_AUDIENCE_KEY);
        sb.append(" int not null);");
        return sb.toString();
    }

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(GetCreateTableCommandText());
    }

    public static void onUpgrade(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + FCPUBLICATIONS_TABLE_NAME);
        onCreate(db);
    }


}













