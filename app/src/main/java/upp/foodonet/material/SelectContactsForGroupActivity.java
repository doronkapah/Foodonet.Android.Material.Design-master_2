package upp.foodonet.material;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Adapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.HashMap;
import java.util.Map;

import Adapters.ContactPhoneNumbersRecyclerViewAdapter;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.ContactItem;

public class SelectContactsForGroupActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String MY_TAG = "food_contactsList";

    FloatingActionButton fab_add_members;
    RecyclerView rv_contacts;
    ProgressDialog progressDialog;
    CheckBox cb_selectAll;
    ContactPhoneNumbersRecyclerViewAdapter contactsAdapter;

    HashMap<Integer, ContactItem> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts_for_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_select_contacts_for_group);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        fab_add_members = (FloatingActionButton) findViewById(R.id.fab_add_members_to_group);
        fab_add_members.setOnClickListener(this);

        cb_selectAll = (CheckBox)findViewById(R.id.cb_select_contacts_select_all);
        cb_selectAll.setOnCheckedChangeListener(this);

        rv_contacts = (RecyclerView)findViewById(R.id.rv_contacts_for_group);

        Intent intent = getIntent();
        contacts =  (HashMap<Integer, ContactItem>)intent.getSerializableExtra(NewAndExistingGroupActivity.extra_key_contacts);
        SetupRecyclerView();

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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add_members_to_group:
                Intent resultIntent = new Intent();
                resultIntent.putExtra(NewAndExistingGroupActivity.extra_key_contacts, contactsAdapter.getSelectedContacts());
                setResult(1, resultIntent);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(0);
        finish();
    }

    private void SetupRecyclerView(){
        rv_contacts.setLayoutManager(new LinearLayoutManager(rv_contacts.getContext()));
        contactsAdapter = new ContactPhoneNumbersRecyclerViewAdapter(contacts, this);
        rv_contacts.setAdapter(contactsAdapter);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(contactsAdapter != null){
            contactsAdapter.SetSelectAll(isChecked);
        }
    }

    public void setAllSelected(boolean isChecked){
        cb_selectAll.setOnCheckedChangeListener(null);
        cb_selectAll.setChecked(isChecked);
        cb_selectAll.setOnCheckedChangeListener(this);
    }
}
