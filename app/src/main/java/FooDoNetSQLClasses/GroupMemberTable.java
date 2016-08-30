package FooDoNetSQLClasses;

import android.database.sqlite.SQLiteDatabase;

import DataModel.GroupMember;

/**
 * Created by Asher on 06.03.2016.
 */
public class GroupMemberTable {

    public static final String GROUP_MEMBER_TABLE_NAME = "GROUPMEMBER";

    private static String GetCreateTableCommandText() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(GROUP_MEMBER_TABLE_NAME);
        sb.append("(");
        sb.append(GroupMember.GROUP_MEMBER_ID_KEY);
        sb.append(" integer primary key, ");
        sb.append(GroupMember.GROUP_MEMBER_USER_ID_KEY);
        sb.append(" integer not null, ");
        sb.append(GroupMember.GROUP_MEMBER_GROUP_ID_KEY);
        sb.append(" integer not null, ");
        sb.append(GroupMember.GROUP_MEMBER_IS_ADMIN_KEY);
        sb.append(" integer not null, ");
        sb.append(GroupMember.GROUP_MEMBER_NAME_KEY);
        sb.append(" text not null, ");
        sb.append(GroupMember.GROUP_MEMBER_PHONE_NUMBER_KEY);
        sb.append(" text null);");
        return sb.toString();
    }

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(GetCreateTableCommandText());
    }

    public static void onUpgrade(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + GROUP_MEMBER_TABLE_NAME);
        onCreate(db);
    }
}
