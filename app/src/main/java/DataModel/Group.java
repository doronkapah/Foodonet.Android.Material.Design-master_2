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
public class Group implements ICanWriteSelfToJSONWriter, Serializable {

    private static final String MY_TAG = "food_group";

    public static final String GROUP_ID_KEY = "_id";
    public static final String GROUP_NAME_KEY = "name";
    public static final String GROUP_ADMIN_ID_KEY = "user_id";
    public static final String GROUP_MEMBERS_KEY = "members";

    public static final String GROUP_MEMBERS_COUNT_KEY = "members_count";

    public static final String GROUP_ITEM_JSON_KEY = "group";

    public static final String GROUP_ID_FOR_JSON_KEY = "group_id";
    public static final String GROUP_NAME_KEY_FOR_INCOMING_JSON = "group_name";

    public Group() {
    }

    public Group(String name, int adminId) {
        Set_name(name);
        Set_admin_id(adminId);
    }

    public Group(int id, String name, int admin_id) {
        this(name, admin_id);
        Set_id(id);
    }

    private int _id;

    public int Get_id() {
        return _id;
    }

    public void Set_id(int id) {
        this._id = id;
    }

    private String _name;

    public String Get_name() {
        return _name;
    }

    public void Set_name(String name) {
        this._name = name;
    }

    private int _admin_id;

    public int Get_admin_id() {
        return _admin_id;
    }

    public void Set_admin_id(int admin_id) {
        this._admin_id = admin_id;
    }

    private int _members_count;

    public int get_members_count() {
        return _members_count;
    }

    public void set_members_count(int count) {
        _members_count = count;
    }

    private ArrayList<GroupMember> _group_members;

    public ArrayList<GroupMember> get_group_members() {
        if (_group_members == null)
            _group_members = new ArrayList<>();
        return _group_members;
    }

    public void set_group_members(ArrayList<GroupMember> group_members) {
        if (_group_members == null)
            _group_members = new ArrayList<>();
        _group_members.addAll(group_members);
    }

    public void add_group_member(GroupMember groupMember) {
        if (_group_members == null)
            _group_members = new ArrayList<>();
        _group_members.add(groupMember);
    }

    public void add_group_members(ArrayList<GroupMember> members){
        if (_group_members == null)
            _group_members = new ArrayList<>();
        _group_members.addAll(members);
    }

    private String groupOwnerName;

    public String getGroupOwnerName() {
        return groupOwnerName;
    }

    public void setGroupOwnerName(String groupOwnerName) {
        this.groupOwnerName = groupOwnerName;
    }

    public static String[] GetColumnNamesArray() {
        return
                new String[]{
                        GROUP_ID_KEY,
                        GROUP_NAME_KEY,
                        GROUP_ADMIN_ID_KEY
                };
    }

    public static String[] GetColumnNamesForListArray(){
        return new String[]{ GROUP_ID_KEY, GROUP_ADMIN_ID_KEY , GROUP_NAME_KEY, GROUP_MEMBERS_COUNT_KEY };
    }

    public ContentValues GetContentValuesRow() {
        ContentValues cv = new ContentValues();
        cv.put(GROUP_ID_KEY, Get_id());
        cv.put(GROUP_NAME_KEY, Get_name());
        cv.put(GROUP_ADMIN_ID_KEY, Get_admin_id());
        return cv;
    }

    public static ArrayList<Group> GetGroupsFromCursor(Cursor cursor) {
        ArrayList<Group> result = new ArrayList<Group>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(GROUP_ID_KEY));
                String name = cursor.getString(cursor.getColumnIndex(GROUP_NAME_KEY));
                int admin_id = cursor.getInt(cursor.getColumnIndex(GROUP_ADMIN_ID_KEY));
                Group group = new Group(id, name, admin_id);
                result.add(group);
            } while (cursor.moveToNext());
        }
        return result;
    }

    public static ArrayList<Group> GetGroupsFromCursorForList(Cursor cursor) {
        ArrayList<Group> result = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Group g = new Group();
                g.Set_id(cursor.getInt(cursor.getColumnIndex(GROUP_ID_KEY)));
                g.Set_name(cursor.getString(cursor.getColumnIndex(GROUP_NAME_KEY)));
                g.set_members_count(cursor.getInt(cursor.getColumnIndex(GROUP_MEMBERS_COUNT_KEY)));
                g.Set_admin_id(cursor.getInt(cursor.getColumnIndex(GROUP_ADMIN_ID_KEY)));
                result.add(g);
            } while (cursor.moveToNext());
        }
        return result;
    }

    public static ArrayList<Group> GetArrayListOfGroupsFromJSON(JSONArray ja) {
        ArrayList<Group> result = new ArrayList<Group>();
        for (int i = 0; i < ja.length(); i++) {
            try {
                result.add(ParseSingleGroupFromJSON(ja.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Group ParseSingleGroupFromJSON(org.json.JSONObject jo) {
        if (jo == null) return null;
        Group group = new Group();
        try {
            group.Set_id(jo.getInt(GROUP_ID_FOR_JSON_KEY));
            group.Set_name(jo.getString(GROUP_NAME_KEY_FOR_INCOMING_JSON));
            group.Set_admin_id(jo.getInt(GROUP_ADMIN_ID_KEY));
            JSONArray jsonArrayMembers = jo.getJSONArray(GROUP_MEMBERS_KEY);
            for (int i = 0; i < jsonArrayMembers.length(); i++){
                org.json.JSONObject jsonObjectMember = jsonArrayMembers.getJSONObject(i);
                GroupMember member = GroupMember.ParseSingleGroupMemberFromJSON(jsonObjectMember);
                group.add_group_member(member);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(MY_TAG, e.getMessage());
            return null;
        }
        return group;
    }

    @Override
    public JSONObject GetJsonObjectForPost() {
        Map<String, Object> groupData = new HashMap<String, Object>();
        groupData.put(GROUP_NAME_KEY, Get_name());
        groupData.put(GROUP_ADMIN_ID_KEY, Get_admin_id());
        Map<String, Object> dataToSend = new HashMap<String, Object>();
        dataToSend.put(GROUP_ITEM_JSON_KEY, groupData);
        org.json.simple.JSONObject json = new org.json.simple.JSONObject();
        json.putAll(dataToSend);
        return json;
    }
}
