package upp.foodonet.material;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import CommonUtilPackage.AmazonImageUploader;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.IAmazonFinishedCallback;
import CommonUtilPackage.InternalRequest;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServiceUtil.FooDoNetCustomActivityConnectedToService;
import UIUtil.RoundedImageView;

public class ProfileViewAndEditActivity
        extends FooDoNetCustomActivityConnectedToService
        implements View.OnClickListener,
                    IFooDoNetServerCallback,
                    IAmazonFinishedCallback {

    RoundedImageView riv_user_avatar;
    EditText et_user_name;
    EditText et_phone_number;
    Button btn_edit_save_profile;

    boolean isEditModeOn;
    boolean userAvatarEdited;
    boolean isNewImageShot;
    String prevName;
    boolean isNameEdited;

    boolean nameAndPhoneUpdateFinished;
    boolean avatarUpdateFinished;

    String prevPhone;
    boolean isPhoneEdited;

    ProgressDialog progressDialog;

    byte[] imageBytes;
    String imageFilePath;

    public static final int REQUEST_CAMERA = 1;
    public static final int SELECT_FILE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view_and_edit);

        riv_user_avatar = (RoundedImageView)findViewById(R.id.riv_user_profile_image);
        riv_user_avatar.setOnClickListener(this);

        et_user_name = (EditText)findViewById(R.id.et_profile_user_name);
        et_user_name.setTag(et_user_name.getKeyListener());
        et_user_name.setKeyListener(null);

        et_phone_number = (EditText)findViewById(R.id.et_profile_phone_number);
        et_phone_number.setTag(et_phone_number.getKeyListener());
        et_phone_number.setKeyListener(null);

        btn_edit_save_profile = (Button)findViewById(R.id.btn_update_profile);
        btn_edit_save_profile.setOnClickListener(this);

        LoadProfileData();

        et_user_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                if(isEditModeOn)
                    setNameEdited(prevName.compareTo(editable.toString()) != 0);
            }
        });
        et_phone_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                if(isEditModeOn)
                    setPhoneEdited(prevPhone.compareTo(editable.toString()) != 0);
            }
        });

        btn_edit_save_profile.setText(getString(R.string.edit_button_text));
        isEditModeOn = false;
        userAvatarEdited = false;

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    private void LoadProfileData(){
        riv_user_avatar.setImageDrawable(
                CommonUtil.GetBitmapDrawableFromFile(getString(R.string.user_avatar_file_name),
                        getString(R.string.image_folder_path), 90, 90));
        et_user_name.setText(CommonUtil.GetMyUserNameFromPreferences(this));
        et_phone_number.setText(CommonUtil.GetMyPhoneNumberFromPreferences(this));
    }

    @Override
    public void OnGooglePlayServicesCheckError() {

    }

    @Override
    public void OnInternetNotConnected() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.riv_user_profile_image:
                if(!isEditModeOn)
                    return;
                selectImage();
                //todo selecting new avatar or making photo
                break;
            case R.id.btn_update_profile:
                if(!isEditModeOn){
                    isEditModeOn = true;
                    prevName = et_user_name.getText().toString();
                    et_user_name.setKeyListener((KeyListener)et_user_name.getTag());
                    prevPhone = et_phone_number.getText().toString();
                    et_phone_number.setKeyListener((KeyListener)et_phone_number.getTag());
                    userAvatarEdited = false;
                    btn_edit_save_profile.setText(R.string.update_button_text);
                    btn_edit_save_profile.setEnabled(false);
                    return;
                }
                progressDialog = CommonUtil.ShowProgressDialog(this, getString(R.string.progress_saving_profile));
                //todo: check what was updated, cause image is updated apart from profile on server
                avatarUpdateFinished = true;
                nameAndPhoneUpdateFinished = true;
                if(userAvatarEdited){
                    avatarUpdateFinished = false;
                    AmazonImageUploader uploader = new AmazonImageUploader(this, this);
                    File imgFile = new File(//Environment.getExternalStorageDirectory() + getString(R.string.image_folder_path),
                            imageFilePath);
                    uploader.UploadUserAvatarToAmazon(imgFile);
                } else { avatarUpdateFinished = true; }
                if(isNameEdited || isPhoneEdited) {
                    nameAndPhoneUpdateFinished = false;
                    SendUpdatedProfileDetails();
                }
                else { nameAndPhoneUpdateFinished = true; }
                break;
        }
    }

    public void setNameEdited(boolean nameEdited) {
        isNameEdited = nameEdited;
        EnableSaveButtonIfEditedAndValid();
    }

    public void setPhoneEdited(boolean phoneEdited) {
        isPhoneEdited = phoneEdited;
        EnableSaveButtonIfEditedAndValid();
    }

    public void setUserAvatarEdited(){
        userAvatarEdited = true;
        EnableSaveButtonIfEditedAndValid();
    }

    private boolean isAnythingEdited(){
        return isNameEdited || isPhoneEdited || userAvatarEdited;
    }

    private void EnableSaveButtonIfEditedAndValid(){
        btn_edit_save_profile.setEnabled(isAnythingEdited() && ValidateNameField() && ValidatePhoneField());
    }

    private void SendUpdatedProfileDetails(){
        InternalRequest irProfile = new InternalRequest(InternalRequest.ACTION_PUT_EDIT_USER);
        irProfile.SocialNetworkType = CommonUtil.GetSocialAccountTypeFromPreferences(this);
        irProfile.SocialNetworkToken = "token1";
        irProfile.SocialNetworkID = CommonUtil.GetSocialAccountIDFromPreferences(this);
        irProfile.PhoneNumber = et_phone_number.getText().toString();
        irProfile.UserName = et_user_name.getText().toString();
        irProfile.Email = CommonUtil.GetMyEmailFromPreferences(this);
        irProfile.DeviceUUID = CommonUtil.GetIMEI(this);
        irProfile.ServerSubPath = getString(R.string.server_post_register_user) + "/" + String.valueOf(CommonUtil.GetMyUserID(this));
        HttpServerConnectorAsync connector = new HttpServerConnectorAsync(getString(R.string.server_base_url), (IFooDoNetServerCallback)this);
        connector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, irProfile);
    }


    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        switch (response.Status){
            case InternalRequest.STATUS_OK:
                //todo: if image also was updated  - check if updating completed
                prevName = et_user_name.getText().toString();
                CommonUtil.SaveMyUserNameToPreferences(this, prevName);
                isNameEdited = false;
                et_user_name.setTag(et_user_name.getKeyListener());
                et_user_name.setKeyListener(null);
                CommonUtil.RemoveValidationFromEditText(this, et_user_name);

                prevPhone = et_phone_number.getText().toString();
                CommonUtil.SaveMyPhoneNumberToPreferences(this, prevPhone);
                isPhoneEdited = false;
                et_phone_number.setTag(et_phone_number.getKeyListener());
                et_phone_number.setKeyListener(null);
                CommonUtil.RemoveValidationFromEditText(this, et_phone_number);

                nameAndPhoneUpdateFinished = true;
                ContinueIfFinishedUpdates();

                break;
            case InternalRequest.STATUS_FAIL:
                Toast.makeText(this, "failed to update profile", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //todo add callback from avatar update, checks if profile was updated and finished and returns screen to readonly state

    private boolean ValidatePhoneField() {
        if (et_phone_number.getText().length() == 0) {
            CommonUtil.SetEditTextIsValid(this, et_phone_number, false);
            //Toast.makeText(this, getString(R.string.validation_phone_number_empty), Toast.LENGTH_LONG).show();
            return false;
        }
        if (!CommonUtil.CheckPhoneNumberString(this, et_phone_number.getText().toString())) {
            CommonUtil.SetEditTextIsValid(this, et_phone_number, false);
            //Toast.makeText(this, getString(R.string.validation_phone_number_invalid), Toast.LENGTH_LONG).show();
            return false;
        }
        CommonUtil.SetEditTextIsValid(this, et_phone_number, true);
        return true;
    }

    private boolean ValidateNameField(){
        if(et_user_name.getText().length() == 0){
            CommonUtil.SetEditTextIsValid(this, et_user_name, false);
            return false;
        }
        CommonUtil.SetEditTextIsValid(this, et_user_name, true);
        return true;
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileViewAndEditActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CAMERA:
            case SELECT_FILE:
                if (resultCode == RESULT_OK) {
                    isNewImageShot = false;
                    setUserAvatarEdited();
                    if (requestCode == REQUEST_CAMERA) {
                        isNewImageShot = true;
                        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                        thumbnail = CommonUtil.CompressBitmapByMaxSize(thumbnail,
                                getResources().getInteger(R.integer.max_image_width_height));
                        imageBytes = CommonUtil.BitmapToBytes(thumbnail); // todo: do I need this?
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        File destination = new File(Environment.getExternalStorageDirectory()
                                + getResources().getString(R.string.image_folder_path),
                                System.currentTimeMillis() + getString(R.string.file_name_part_just_shot) + ".jpg");
                        FileOutputStream fo;
                        try {
                            destination.createNewFile();
                            fo = new FileOutputStream(destination);
                            fo.write(bytes.toByteArray());
                            fo.close();
                            imageFilePath = destination.getAbsolutePath();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        riv_user_avatar.setImageBitmap(thumbnail);
                    } else if (requestCode == SELECT_FILE) {
                        Uri selectedImageUri = data.getData();
                        String[] projection = {MediaStore.MediaColumns.DATA};
                        Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                                null);
                        if (cursor == null)
                            throw new NullPointerException("can't get picture cursor, critical error");
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                        cursor.moveToFirst();
                        imageFilePath = cursor.getString(column_index);
                        Bitmap bm = CommonUtil.decodeScaledBitmapFromSdCard(imageFilePath, 200, 200);
                        riv_user_avatar.setImageBitmap(bm);
                        imageBytes = CommonUtil.BitmapToBytes(bm);
                    }
                }
                if (resultCode == RESULT_CANCELED) {

                }
                break;
        }
    }

    @Override
    public void NotifyToBListenerAboutEvent(int eventCode) {
        avatarUpdateFinished = true;
        File sourceFile = new File(imageFilePath);
        File destinationFile = new File(Environment.getExternalStorageDirectory()
                + getResources().getString(R.string.image_folder_path), getString(R.string.user_avatar_file_name));
        try {
            CommonUtil.CopyFile(sourceFile, destinationFile);
            if(isNewImageShot){
                sourceFile.delete();
                isNewImageShot = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ContinueIfFinishedUpdates();
    }

    private void ContinueIfFinishedUpdates(){
        if(!nameAndPhoneUpdateFinished || !avatarUpdateFinished)
            return;

        btn_edit_save_profile.setText(getString(R.string.edit_button_text));
        btn_edit_save_profile.setEnabled(true);

        isEditModeOn = false;
        if(progressDialog != null)
            progressDialog.dismiss();
    }
}
