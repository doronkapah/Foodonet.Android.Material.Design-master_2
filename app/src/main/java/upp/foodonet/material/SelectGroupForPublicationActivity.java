package upp.foodonet.material;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;

import Adapters.IOnGroupForPublicationSelectedListener;
import Adapters.SelectGroupForPublicationRecyclerViewAdapter;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.InternalRequest;
import DataModel.Group;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;

public class SelectGroupForPublicationActivity extends AppCompatActivity implements IOnGroupForPublicationSelectedListener, IFooDoNetSQLCallback {

    private static final String MY_TAG = "food_groupForPub";
    public static final String EXTRA_KEY_GROUP_ID = "group_id";
    public static final String EXTRA_KEY_GROUP_NAME = "group_name";

    RecyclerView rv_groups_for_selection;
    SelectGroupForPublicationRecyclerViewAdapter adapter;
    ProgressDialog pd_loadingGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group_for_publication);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_select_group_for_pub);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setupGroupRecyclerView();
        pd_loadingGroups = CommonUtil.ShowProgressDialog(this, getString(R.string.loading_groups));
        FooDoNetSQLExecuterAsync sqlExecuter = new FooDoNetSQLExecuterAsync(this, this);
        InternalRequest irGroups = new InternalRequest(InternalRequest.ACTION_GET_GROUPS_FROM_SQL);
        sqlExecuter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, irGroups);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(pd_loadingGroups != null)
            pd_loadingGroups.dismiss();
    }

    private void setupGroupRecyclerView(){
        rv_groups_for_selection = (RecyclerView)findViewById(R.id.rv_select_group_for_pub);
        rv_groups_for_selection.setLayoutManager(new LinearLayoutManager(rv_groups_for_selection.getContext()));
        adapter = new SelectGroupForPublicationRecyclerViewAdapter(new ArrayList<Group>(), this,
                getResources().getDrawable(R.drawable.globe_icon_list),
                getResources().getDrawable(R.drawable.group_icon_list),
                getString(R.string.public_share_group_name));
        rv_groups_for_selection.setAdapter(adapter);
    }

    @Override
    public void OnGroupForPublicationSelected(int groupID, String groupName) {
        Intent intent = getIntent();
        //intent.putExtra(EXTRA_KEY_GROUP_ID, groupID);
        intent.putExtra(EXTRA_KEY_GROUP_NAME, groupName);
        setResult(groupID, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(-1);
        finish();
        //super.onBackPressed();
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        if(request.Status == InternalRequest.STATUS_OK && request.groups != null || request.groups.size() > 0)
            adapter.UpdateGroupsList(request.groups);
        if(pd_loadingGroups != null)
            pd_loadingGroups.dismiss();
    }
}
