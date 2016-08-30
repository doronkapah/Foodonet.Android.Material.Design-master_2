package upp.foodonet.material;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

import Adapters.GroupsListRecyclerViewAdapter;
import Adapters.IOnGroupSelecterFromListListener;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.INewGroupNameEnter;
import DataModel.Group;
import DataModel.GroupMember;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;

public class GroupsListActivity
        extends FooDoNetCustomActivityConnectedToService
        implements  View.OnClickListener,
                    IOnGroupSelecterFromListListener,
                    INewGroupNameEnter {

    private static final String MY_TAG = "food_groupsList";

    public static final String GROUP_NAME_EXTRA_KEY = "group_name";

    public static final int requestCodeNewGroup = 0;
    public static final int requestCodeExistingGroup = 1;

    private FloatingActionButton fab_add_group;
    private RecyclerView rv_groups_list;
    ProgressDialog pd_loadingGroup;

    GroupsListRecyclerViewAdapter groupsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_groups_list);
        //setSupportActionBar(toolbar);

        fab_add_group = (FloatingActionButton) findViewById(R.id.fab_groups);
        fab_add_group.setOnClickListener(this);

        rv_groups_list = (RecyclerView)findViewById(R.id.rv_groups_list);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(pd_loadingGroup != null)
            pd_loadingGroup.dismiss();
        setupRecyclerView();
    }

    private void setupRecyclerView(){
        rv_groups_list.setLayoutManager(new LinearLayoutManager(rv_groups_list.getContext()));
        ArrayList<Group> groupsList = LoadGroups();
        groupsListAdapter = new GroupsListRecyclerViewAdapter(groupsList, CommonUtil.GetMyUserID(this), getString(R.string.group_admin_subtitle), this);
        rv_groups_list.setAdapter(groupsListAdapter);
    }

    private ArrayList<Group> LoadGroups(){
        Cursor groupsCursor = getContentResolver().query(FooDoNetSQLProvider.URI_GROUPS_LIST, Group.GetColumnNamesForListArray(), null, null, null);
        return Group.GetGroupsFromCursorForList(groupsCursor);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_groups:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(this,
                            new String[]{
                                    Manifest.permission.READ_CONTACTS
                            },
                            SplashScreenActivity.REQUEST_CODE_ASK_PERMISSION);
                else ShowDialogEnterNewGroupName(this, this);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ShowDialogEnterNewGroupName(this, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if(groupsListAdapter != null){
                    ArrayList<Group> groupsList = LoadGroups();
                    groupsListAdapter.UpdateGroupsList(groupsList);
                }
                break;
        }
    }

    @Override
    public void OnGroupSelected(int groupID) {
        pd_loadingGroup = CommonUtil.ShowProgressDialog(this, getString(R.string.loading_group));
        ExistingGroupGetter groupGetter = new ExistingGroupGetter(this);
        groupGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, groupID);
    }

    public void OnGroupFetchedForOpening(Group group){
        Intent intent = new Intent(this, NewAndExistingGroupActivity.class);
        intent.putExtra(NewAndExistingGroupActivity.extra_key_is_new_group, false);
        intent.putExtra(NewAndExistingGroupActivity.extra_key_existing_group, group);
        startActivityForResult(intent, 1);
        if(pd_loadingGroup != null)
            pd_loadingGroup.dismiss();
    }

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

/*
    new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
*/

    //region Create group

    public void ShowDialogEnterNewGroupName(final Context context, final INewGroupNameEnter callback) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.enter_new_group_name_dialog);
        final EditText et_group_name = (EditText) dialog.findViewById(R.id.et_new_group_name);
        final Button btn_group_name_ok = (Button) dialog.findViewById(R.id.btn_new_group_ok);
        Button btn_group_name_cancel = (Button) dialog.findViewById(R.id.btn_new_group_cancel);

        btn_group_name_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_group_name != null && et_group_name.getText().toString().length() > 0) {
                    callback.OnNewGroupNameSelected(et_group_name.getText().toString());
                    dialog.dismiss();
                }
            }
        });
        btn_group_name_ok.setEnabled(false);

        btn_group_name_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        et_group_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                btn_group_name_ok.setEnabled(editable.length() > 0);
            }
        });

        dialog.show();
    }

    @Override
    public void OnNewGroupNameSelected(String newGroupName) {
        Intent newGroupIntent = new Intent(this, NewAndExistingGroupActivity.class);
        newGroupIntent.putExtra(GROUP_NAME_EXTRA_KEY, newGroupName);
        startActivityForResult(newGroupIntent, requestCodeNewGroup);
    }

    //endregion


    @Override
    public void onBroadcastReceived(Intent intent){
        int actionCode = intent.getIntExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, -1);
        switch (actionCode) {
            case ServicesBroadcastReceiver.ACTION_CODE_RELOAD_DATA_SUCCESS:
                ArrayList<Group> groupList = LoadGroups();
                if(groupsListAdapter == null)
                    setupRecyclerView();
                else {
                    groupsListAdapter.UpdateGroupsList(groupList);
                }
                break;
            default:
                super.onBroadcastReceived(intent);
                break;
        }
    }


    private class ExistingGroupGetter extends AsyncTask<Integer, Void, Void>{
        Group group;
        Context context;

        public ExistingGroupGetter(Context ctx){
            context = ctx;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            group = Group.GetGroupsFromCursor(getContentResolver()
                    .query(Uri.parse(FooDoNetSQLProvider.URI_GROUP + "/" + params[0]),
                            Group.GetColumnNamesArray(), null, null, null)).get(0);
            ArrayList<GroupMember> members = GroupMember.GetGroupMembersFromCursor(getContentResolver()
                    .query(Uri.parse(FooDoNetSQLProvider.URI_GROUP_MEMBERS_BY_GROUP + "/" + params[0]),
                            GroupMember.GetColumnNamesArray(), null, null, null));
            ArrayList<GroupMember> result = new ArrayList<>();
            int myUserID = CommonUtil.GetMyUserID(context);
            for(GroupMember member: members)
                if(member.get_user_id() != myUserID)
                    result.add(member);
            group.set_group_members(result);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(group != null)
                OnGroupFetchedForOpening(group);
        }

    }

}
