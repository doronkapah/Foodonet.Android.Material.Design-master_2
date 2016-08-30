package upp.foodonet.material;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Adapters.ContactsInGroupRecyclerViewAdapter;
import AsyncTasks.FetchContactsAsyncTask;
import AsyncTasks.IFetchContactsParent;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.ContactItem;
import CommonUtilPackage.IGroupMemberRemoved;
import CommonUtilPackage.InternalRequest;
import DataModel.Group;
import DataModel.GroupMember;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;

public class NewAndExistingGroupActivity
        extends AppCompatActivity
        implements View.OnClickListener,
        IFetchContactsParent,
        IFooDoNetServerCallback, IGroupMemberRemoved {

    private static final String MY_TAG = "food_editGroup";
    public static final String extra_key_contacts = "contacts";
    public static final String extra_key_is_new_group = "isNew";
    public static final String extra_key_existing_group = "group";

    int groupID;

    TextView tv_groupName;
    Button btn_addMembers;
    ProgressDialog pd_loadingContacts;
    RecyclerView rv_contacts_in_group;
    FloatingActionButton fab_saveGroup;
    FrameLayout fl_btn_leaveGroup;
    ProgressDialog pb_progress;

    ContactsInGroupRecyclerViewAdapter adapter;

    boolean IsNewGroup;
    ArrayList<ContactItem> groupContacts;
    Group existingGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_and_existing_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_new_existing_group);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        btn_addMembers = (Button) findViewById(R.id.btn_group_add_member);
        btn_addMembers.setOnClickListener(this);
        fab_saveGroup = (FloatingActionButton) findViewById(R.id.fab_save_group);
        fl_btn_leaveGroup = (FrameLayout)findViewById(R.id.fl_button_leave_group);
        //fab_saveGroup.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_inactive_gray)));
        tv_groupName = (TextView)findViewById(R.id.tv_group_name_title);

        Intent intent = getIntent();
        IsNewGroup = intent.getBooleanExtra(extra_key_is_new_group, true);

        if (IsNewGroup) {
            groupID = 0;
            tv_groupName.setText(getIntent().getStringExtra(GroupsListActivity.GROUP_NAME_EXTRA_KEY));
            fab_saveGroup.setVisibility(View.VISIBLE);
            fl_btn_leaveGroup.setVisibility(View.GONE);
            fab_saveGroup.setOnClickListener(this);
        } else {
            existingGroup = (Group)intent.getSerializableExtra(extra_key_existing_group);
            tv_groupName.setText(existingGroup.Get_name());
            fl_btn_leaveGroup.setVisibility(View.VISIBLE);
            fab_saveGroup.setVisibility(View.GONE);
            fl_btn_leaveGroup.setOnClickListener(this);
        }

        groupContacts = new ArrayList<>();
        rv_contacts_in_group = (RecyclerView) findViewById(R.id.rv_group_member_list);
        SetRecyclerView();
    }

    private void SetRecyclerView() {
        rv_contacts_in_group.setLayoutManager(new LinearLayoutManager(rv_contacts_in_group.getContext()));
        adapter = new ContactsInGroupRecyclerViewAdapter(this);
        if(!IsNewGroup){
            ArrayList<ContactItem> contactItems = new ArrayList<>();
            for(GroupMember member : existingGroup.get_group_members())
                contactItems.add(new ContactItem(member.get_name(), member.get_phone_number(), member));
            adapter.setContacts(contactItems);
        }
        rv_contacts_in_group.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_group_add_member:
                pd_loadingContacts = CommonUtil.ShowProgressDialog(this, getString(R.string.loading_contacts));
                FetchContactsAsyncTask contactsAsyncTask = new FetchContactsAsyncTask(getContentResolver(), this);
                contactsAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, groupContacts);
                break;
            case R.id.fab_save_group:
                pb_progress = CommonUtil.ShowProgressDialog(this, getString(R.string.saving_group));
                HttpServerConnectorAsync connector = new HttpServerConnectorAsync(getString(R.string.server_base_url), (IFooDoNetServerCallback) this);
                Group g = new Group(tv_groupName.getText().toString(), CommonUtil.GetMyUserID(this));
                GroupMember owner = new GroupMember(0, CommonUtil.GetMyUserID(this), 0, true,
                        CommonUtil.GetMyPhoneNumberFromPreferences(this),
                        CommonUtil.GetSocialAccountNameFromPreferences(this));
                InternalRequest ir = new InternalRequest(InternalRequest.ACTION_POST_NEW_GROUP, getString(R.string.server_post_new_group), g);
                ir.groupOwner = owner;
                ir.groupMembersToAdd = GetGroupMembers();
                ir.MembersServerSubPath = getString(R.string.server_post_add_members_to_group);
                connector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
                break;
            case R.id.fl_button_leave_group:
                int memberIDSelf = -1;
                int myUserID = CommonUtil.GetMyUserID(this);
                for(GroupMember member : existingGroup.get_group_members())
                    if(member.get_user_id() == myUserID)
                        memberIDSelf = member.get_id();
                if(memberIDSelf != -1){
                    pb_progress = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_leave_group));
                    HttpServerConnectorAsync connector1 = new HttpServerConnectorAsync(getString(R.string.server_post_add_members_to_group), (IFooDoNetServerCallback)this);
                    InternalRequest irLeaveGroup = new InternalRequest(InternalRequest.ACTION_DELETE_LEAVE_GROUP);
                    irLeaveGroup.GroupMemberToDeleteID = memberIDSelf;
                    connector1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, irLeaveGroup);
                }
                else {
                    Log.e(MY_TAG, "can't find group member self in this group");
                    return;
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (pd_loadingContacts != null) pd_loadingContacts.dismiss();
        if (resultCode == 0) return;
        HashMap<Integer, ContactItem> selectedContacts = (HashMap<Integer, ContactItem>) data.getSerializableExtra(extra_key_contacts);
        groupContacts.clear();
        groupContacts.addAll(selectedContacts.values());
        adapter.setContacts(groupContacts);
    }

    @Override
    public void OnContactsFetched(HashMap<Integer, ContactItem> contacts) {
        Intent addMembersIntent = new Intent(this, SelectContactsForGroupActivity.class);
        addMembersIntent.putExtra(extra_key_contacts, contacts);
        startActivityForResult(addMembersIntent, 0);
    }

    private ArrayList<GroupMember> GetGroupMembers() {
        ArrayList<GroupMember> result = new ArrayList<>();
        for (ContactItem item : groupContacts)
            result.add(new GroupMember(0, 0, groupID, false, item.getPhoneNumber(), item.getName()));
        return result;
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        switch (response.ActionCommand) {
            case InternalRequest.ACTION_POST_NEW_GROUP:
                switch (response.Status) {
                    case InternalRequest.STATUS_OK:
                        getContentResolver().insert(FooDoNetSQLProvider.URI_GROUP, response.group.GetContentValuesRow());
                        for(GroupMember member : response.group.get_group_members())
                            getContentResolver().insert(FooDoNetSQLProvider.URI_GROUP_MEMBERS, member.GetContentValuesRow());
                        Log.e(MY_TAG, "succeeded to save group");
                        finish();
                        break;
                    case InternalRequest.STATUS_FAIL:
                        Log.e(MY_TAG, "failed to save group");
                        break;
                }
                if (pb_progress != null) pb_progress.dismiss();
                break;
            case InternalRequest.ACTION_DELETE_GROUP_MEMBER:
                switch (response.Status){
                    case InternalRequest.STATUS_OK:
                        int rowDeleted = getContentResolver().delete(Uri.parse(FooDoNetSQLProvider.URI_GROUP_MEMBERS + "/" + String.valueOf(response.GroupMemberToDeleteID)), null, null);
                        Log.i(MY_TAG, "Delete group member from DB: " + (rowDeleted == 1 ? "success": "fail"));
                        break;
                }
                if(pb_progress != null) pb_progress.dismiss();
                break;
            case InternalRequest.ACTION_DELETE_LEAVE_GROUP:
                switch (response.Status){
                    case InternalRequest.STATUS_OK:
                        int rowsDeleted = getContentResolver().delete(Uri.parse(FooDoNetSQLProvider.URI_GROUP + "/" + String.valueOf(existingGroup.Get_id())), null, null);
                        Log.i(MY_TAG, "Leaving group: " + (rowsDeleted > 0 ? "success": "fail"));
                        if(rowsDeleted > 0) finish();
                        break;
                }
                if(pb_progress != null) pb_progress.dismiss();
                break;
        }
    }

    @Override
    public void OnGroupMemberRemoved(GroupMember groupMember) {
        pb_progress = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_delete_member));
        HttpServerConnectorAsync connector
                = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), (IFooDoNetServerCallback)this);
        InternalRequest irDeleteMember = new InternalRequest(InternalRequest.ACTION_DELETE_GROUP_MEMBER);
        irDeleteMember.ServerSubPath =  getString(R.string.server_post_add_members_to_group);
        irDeleteMember.GroupMemberToDeleteID = groupMember.get_id();
        connector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, irDeleteMember);
    }

    /*
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
        */

}
