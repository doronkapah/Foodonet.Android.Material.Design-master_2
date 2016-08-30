package FooDoNetSQLClasses;

import android.database.sqlite.SQLiteDatabase;

import DataModel.Group;
import DataModel.GroupMember;

/**
 * Created by Asher on 06.03.2016.
 */
public class GroupTable {

    public static final String GROUP_TABLE_NAME = "GROUPTABLE";

    private static String GetCreateTableCommandText() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(GROUP_TABLE_NAME);
        sb.append("(");
        sb.append(Group.GROUP_ID_KEY);
        sb.append(" integer primary key, ");
        sb.append(Group.GROUP_ADMIN_ID_KEY);
        sb.append(" integer not null, ");
        sb.append(Group.GROUP_NAME_KEY);
        sb.append(" text not null);");
        return sb.toString();
    }

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(GetCreateTableCommandText());
    }

    public static void onUpgrade(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + GROUP_TABLE_NAME);
        onCreate(db);
    }

    public static String GetRawSelectGroupsForList(){
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT G." + Group.GROUP_ID_KEY);
        sb.append(", G." + Group.GROUP_ADMIN_ID_KEY);
        sb.append(", G." + Group.GROUP_NAME_KEY);
        sb.append(", COUNT(GM." + GroupMember.GROUP_MEMBER_ID_KEY + ") AS " + Group.GROUP_MEMBERS_COUNT_KEY);
        sb.append(" FROM " + GROUP_TABLE_NAME + " AS G ");
        sb.append(" INNER JOIN " + GroupMemberTable.GROUP_MEMBER_TABLE_NAME + " AS GM ON G." + Group.GROUP_ID_KEY + " = GM." + GroupMember.GROUP_MEMBER_GROUP_ID_KEY);
        sb.append(" GROUP BY G." + Group.GROUP_ID_KEY + ", G." + Group.GROUP_NAME_KEY + ", G." + Group.GROUP_ADMIN_ID_KEY);
        return sb.toString();
    }
}
