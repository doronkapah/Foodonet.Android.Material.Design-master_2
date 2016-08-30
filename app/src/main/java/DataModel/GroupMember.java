package DataModel;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Asher on 05.03.2016.
 */
public class GroupMember implements ICanWriteSelfToJSONWriter, Serializable {

    private static final String MY_TAG = "food_groupMember";

    public static final String GROUP_MEMBER_ID_KEY_JSON = "id";
    public static final String GROUP_MEMBER_ID_KEY = "_id";
    public static final String GROUP_MEMBER_USER_ID_KEY = "user_id";
    public static final String GROUP_MEMBER_GROUP_ID_KEY = "Group_id";
    public static final String GROUP_MEMBER_IS_ADMIN_KEY = "is_admin";
    public static final String GROUP_MEMBER_PHONE_NUMBER_KEY = "phone_number";
    public static final String GROUP_MEMBER_NAME_KEY = "name";

    public GroupMember(){}

    public GroupMember(int member_id, int user_id, int group_id, boolean is_admin, String phone_number, String name){
        set_id(member_id);
        set_user_id(user_id);
        set_group_id(group_id);
        set_is_admin(is_admin);
        set_phone_number(phone_number);
        set_name(name);
    }

    private int _id;
    public int get_id() {return _id;}
    public void set_id(int member_id){this._id = member_id;}

    private int _user_id;
    public int get_user_id() {return _user_id;}
    public void set_user_id(int user_id){_user_id = user_id;}

    private int _group_id;
    public int get_group_id(){return _group_id;}
    public void set_group_id(int group_id){_group_id = group_id;}

    private boolean _is_admin;
    public boolean get_is_admin(){return _is_admin;}
    public void set_is_admin(boolean is_admin){_is_admin = is_admin;}

    private String _phone_number;
    public String get_phone_number(){return _phone_number;}
    public void set_phone_number(String phone_number){_phone_number = phone_number;}

    private String _name;
    public String get_name(){return _name;}
    public void set_name(String name){_name = name;}

    public static String[] GetColumnNamesArray() {
        return
                new String[]{
                        GROUP_MEMBER_ID_KEY,
                        GROUP_MEMBER_NAME_KEY,
                        GROUP_MEMBER_USER_ID_KEY,
                        GROUP_MEMBER_PHONE_NUMBER_KEY,
                        GROUP_MEMBER_GROUP_ID_KEY,
                        GROUP_MEMBER_IS_ADMIN_KEY
                };
    }

    public ContentValues GetContentValuesRow() {
        ContentValues cv = new ContentValues();
        cv.put(GROUP_MEMBER_ID_KEY, get_id());
        cv.put(GROUP_MEMBER_NAME_KEY, get_name());
        cv.put(GROUP_MEMBER_USER_ID_KEY, get_user_id());
        cv.put(GROUP_MEMBER_PHONE_NUMBER_KEY, get_phone_number());
        cv.put(GROUP_MEMBER_GROUP_ID_KEY, get_group_id());
        cv.put(GROUP_MEMBER_IS_ADMIN_KEY, get_is_admin());
        return cv;
    }

    public static ArrayList<GroupMember> GetGroupMembersFromCursor(Cursor cursor) {
        ArrayList<GroupMember> result = new ArrayList<>();
        if(cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(GROUP_MEMBER_ID_KEY));
                String name = cursor.getString(cursor.getColumnIndex(GROUP_MEMBER_NAME_KEY));
                boolean is_admin = cursor.getInt(cursor.getColumnIndex(GROUP_MEMBER_IS_ADMIN_KEY)) == 1;
                int user_id = cursor.getInt(cursor.getColumnIndex(GROUP_MEMBER_USER_ID_KEY));
                String phone_number = cursor.getString(cursor.getColumnIndex(GROUP_MEMBER_PHONE_NUMBER_KEY));
                int group_id = cursor.getInt(cursor.getColumnIndex(GROUP_MEMBER_GROUP_ID_KEY));
                result.add(new GroupMember(id,user_id,group_id,is_admin,phone_number,name));
            } while (cursor.moveToNext());
        }
        return result;
    }

    public static ArrayList<GroupMember> GetArrayListOfGroupMembersFromJSON(JSONArray ja) {
        ArrayList<GroupMember> result = new ArrayList<>();
        for (int i = 0; i < ja.length(); i++) {
            try {
                result.add(ParseSingleGroupMemberFromJSON(ja.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static GroupMember ParseSingleGroupMemberFromJSON(org.json.JSONObject jo) {
        if (jo == null) return null;
        GroupMember groupMember = new GroupMember();
        try {
            groupMember.set_id(jo.getInt(GROUP_MEMBER_ID_KEY_JSON));
            groupMember.set_name(jo.getString(GROUP_MEMBER_NAME_KEY));
            groupMember.set_is_admin(jo.getBoolean(GROUP_MEMBER_IS_ADMIN_KEY));
            groupMember.set_user_id(jo.getInt(GROUP_MEMBER_USER_ID_KEY));
            groupMember.set_phone_number(jo.getString(GROUP_MEMBER_PHONE_NUMBER_KEY));
            groupMember.set_group_id(jo.getInt(GROUP_MEMBER_GROUP_ID_KEY));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(MY_TAG, e.getMessage());
            return null;
        }
        return groupMember;
    }


//    member_id: Int,
//    user_id: Int,
//    Group_id: Int,
//    is_admin: Boolean,
//    phone_number : String,
//    name: String

    @Override
    public JSONObject GetJsonObjectForPost() {
        Map<String,Object> memberMap = new HashMap<>();
        memberMap.put(GROUP_MEMBER_NAME_KEY, get_name());
        memberMap.put(GROUP_MEMBER_USER_ID_KEY, get_user_id());
        memberMap.put(GROUP_MEMBER_IS_ADMIN_KEY, get_is_admin());
        memberMap.put(GROUP_MEMBER_PHONE_NUMBER_KEY, get_phone_number());
        memberMap.put(GROUP_MEMBER_GROUP_ID_KEY, get_group_id());
        return new JSONObject(memberMap);
    }
}
