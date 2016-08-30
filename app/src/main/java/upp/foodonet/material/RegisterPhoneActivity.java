package upp.foodonet.material;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.w3c.dom.Text;

import CommonUtilPackage.CommonUtil;
import FooDoNetServerClasses.DownloadImageTask;
import UIUtil.RoundedImageView;



public class RegisterPhoneActivity extends Activity implements View.OnClickListener{

    private EditText et_phone_number;
    private Button btn_register;
    private TextView tv_profile_name;
    public static final String AVATAR_KEY = "avatar";
    public static final String USER_NAME = "user_name";
    public static final String NETWORKTYPE_KEY = "networktype";
    public static final String FACEBOOK_KEY = "facebook";
    public static final String GOOGLE_KEY = "google";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone);

        et_phone_number = (EditText) findViewById(R.id.et_phone);
        et_phone_number.setInputType(InputType.TYPE_CLASS_NUMBER);

        btn_register = (Button) findViewById(R.id.btn_register_phone);
        btn_register.setOnClickListener(this);

        tv_profile_name = (TextView)findViewById(R.id.user_name);

        Intent i = this.getIntent();
        String photoURL = i.getStringExtra(AVATAR_KEY);
        if(photoURL != null){
            String socialNetworkType = i.getStringExtra(NETWORKTYPE_KEY);
            String baseUrl = "";
            if(socialNetworkType.equals(FACEBOOK_KEY))
                baseUrl = getString(R.string.facebook_url);
            ImageView userProfileImage = (ImageView) findViewById(R.id.iv_user_profile_img);
            String networkUrl = CommonUtil.GetNetworkUrl(baseUrl, photoURL);
            DownloadImageTask imageTask = new DownloadImageTask(networkUrl,
                    150, getString(R.string.image_folder_path), userProfileImage, getString(R.string.user_avatar_file_name));
            imageTask.execute();
        }

        tv_profile_name.setText(i.getStringExtra(USER_NAME));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        ForceReturn();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        return;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirmExit))
                .setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    private void ForceReturn(){
        setResult(RESULT_CANCELED);
        finish();
//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_HOME);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
        //finish();
    }

    private boolean ValidatePhoneField() {
        if (et_phone_number.getText().length() == 0) {
            CommonUtil.SetEditTextIsValid(this, et_phone_number, false);
            Toast.makeText(this, getString(R.string.validation_phone_number_empty), Toast.LENGTH_LONG).show();
            return false;
        }
        if (!CommonUtil.CheckPhoneNumberString(this, et_phone_number.getText().toString())) {
            CommonUtil.SetEditTextIsValid(this, et_phone_number, false);
            Toast.makeText(this, getString(R.string.validation_phone_number_invalid), Toast.LENGTH_LONG).show();
            return false;
        }
        CommonUtil.SetEditTextIsValid(this, et_phone_number, true);
        return true;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_register_phone:
                if (!ValidatePhoneField()) return;
                CommonUtil.SaveMyPhoneNumberToPreferences(this, et_phone_number.getText().toString());
                Intent phoneIntent = new Intent();
                phoneIntent.putExtra(SignInActivity.PHONE_KEY, et_phone_number.getText().toString());
                // return data Intent and finish
                setResult(RESULT_OK, phoneIntent);
                finish();
        }
    }
}
