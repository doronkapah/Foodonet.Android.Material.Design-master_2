package upp.foodonet.material;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import CommonUtilPackage.AmazonImageUploader;
import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.IAmazonFinishedCallback;
import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;

/**
 * An {@link android.app.IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AddEditPublicationService extends IntentService implements IFooDoNetSQLCallback, IFooDoNetServerCallback, IAmazonFinishedCallback {

    private static final String MY_TAG = "food_serviceAddEditPub";

    private static final String ACTION_SAVE_NEW_PUBLICATION = "upp.foodonet.action.save.new.pub";
    private static final String ACTION_SAVE_EDITED_PUBLICATION = "upp.foodonet.action.save.edited.pub";


    private static final String EXTRA_PARAM_PUBLICATION = "upp.foodonet.new.pub.param";

    private CognitoCachingCredentialsProvider credentialsProvider;

    private FCPublication editedPublicationForSaving;

    public static void StartSaveNewPublication(Context context, FCPublication pubToSave) {
        Intent intent = new Intent(context, AddEditPublicationService.class);
        intent.setAction(ACTION_SAVE_NEW_PUBLICATION);
        intent.putExtra(EXTRA_PARAM_PUBLICATION, pubToSave);
        context.startService(intent);
    }

    public static void StartSaveEditedPublication(Context context, FCPublication pubToSave) {
        Intent intent = new Intent(context, AddEditPublicationService.class);
        intent.setAction(ACTION_SAVE_EDITED_PUBLICATION);
        intent.putExtra(EXTRA_PARAM_PUBLICATION, pubToSave);
        context.startService(intent);
    }

    public AddEditPublicationService() {
        super("SaveNewPublicationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_SAVE_NEW_PUBLICATION:
                    FCPublication publicationToSave = (FCPublication) intent.getSerializableExtra(EXTRA_PARAM_PUBLICATION);
                    if (publicationToSave == null) {
                        Log.e(MY_TAG, "got no publication to save (onHandleIntent)");
                        return;
                    }
                    handleActionSaveNewPublication(publicationToSave);
                    break;
                case ACTION_SAVE_EDITED_PUBLICATION:
                    FCPublication editedPubToSave = (FCPublication) intent.getSerializableExtra(EXTRA_PARAM_PUBLICATION);
                    if (editedPubToSave == null) {
                        Log.e(MY_TAG, "got no publication to save (onHandleIntent)");
                        return;
                    }
                    editedPublicationForSaving = editedPubToSave;
                    handleActionSaveEditedPublication(editedPubToSave);
                    break;
            }
        }
    }

    private void handleActionSaveEditedPublication(FCPublication publication) {
        if (publication.getPhotoUrl() != null && publication.getPhotoUrl().length() > 0) {
            File sourceFile = new File(publication.getPhotoUrl());
            if (sourceFile.exists()) {
                String imgFileName
                        = String.valueOf(publication.getUniqueId()) + "." + String.valueOf(publication.getVersion() + 1) + ".jpg";
                File destinationFile
                        = new File(Environment.getExternalStorageDirectory()
                        + getString(R.string.image_folder_path), imgFileName);
                CommonUtil.CopyImageFileWithCompressionBySize(sourceFile, destinationFile, getResources().getInteger(R.integer.max_image_width_height));
                if (publication.getPhotoUrl().contains(getString(R.string.file_name_part_just_shot)))
                    sourceFile.delete();
                publication.setPhotoUrl(null);
            }
        } else {
            String fileName
                    = String.valueOf(publication.getUniqueId()) + "."
                    + String.valueOf(publication.getVersion()) + ".jpg";
            File oldVersionSource = new File(
                    Environment.getExternalStorageDirectory()
                            + getString(R.string.image_folder_path), fileName);
            if (oldVersionSource.exists()) {
                String newFileName
                        = String.valueOf(publication.getUniqueId()) + "."
                        + String.valueOf(publication.getVersion() + 1) + ".jpg";
                File newVersionDestination = new File(
                        Environment.getExternalStorageDirectory() + getString(R.string.image_folder_path), newFileName);
                try {
                    CommonUtil.CopyFile(oldVersionSource, newVersionDestination);
                    oldVersionSource.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        HttpServerConnectorAsync connector1
                = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), (IFooDoNetServerCallback) this);
        String subPath = getString(R.string.server_edit_publication_path);
        subPath = subPath.replace("{0}", String.valueOf(publication.getUniqueId()));
        InternalRequest ir1
                = new InternalRequest(InternalRequest.ACTION_PUT_EDIT_PUBLICATION,
                subPath, publication);
        ir1.publicationForSaving = publication;
        connector1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir1);
/*
        FooDoNetSQLExecuterAsync saveExecuter
                = new FooDoNetSQLExecuterAsync(this, getContentResolver());
        saveExecuter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new InternalRequest(InternalRequest.ACTION_SQL_SAVE_EDITED_PUBLICATION, publication));
*/
    }

    private void handleActionSaveNewPublication(FCPublication publication) {
        if (publication.getUniqueId() == 0) {
            int newNegativeID = 0;
            Cursor negIdCursor = getContentResolver()
                    .query(FooDoNetSQLProvider.URI_GET_NEW_NEGATIVE_ID,
                            new String[]{FCPublication.PUBLICATION_NEW_NEGATIVE_ID}, null, null, null);
            if (negIdCursor != null && negIdCursor.moveToFirst()) {
                newNegativeID = negIdCursor.getInt(
                        negIdCursor.getColumnIndex(FCPublication.PUBLICATION_NEW_NEGATIVE_ID));
                newNegativeID = newNegativeID >= 0 ? -1 : newNegativeID;
                publication.setUniqueId(newNegativeID);
            }
        }
        if (publication.getPhotoUrl() != null && publication.getPhotoUrl().length() > 0) {
            File sourceFile = new File(publication.getPhotoUrl());
            if (sourceFile.exists()) {
                String imgFileName
                        = CommonUtil.GetFileNameByPublication(publication);
                File destinationFile
                        = new File(Environment.getExternalStorageDirectory()
                        + getString(R.string.image_folder_path), imgFileName);
                CommonUtil.CopyImageFileWithCompressionBySize(sourceFile, destinationFile, getResources().getInteger(R.integer.max_image_width_height));
                if (publication.getPhotoUrl().contains(getString(R.string.file_name_part_just_shot)))
                    sourceFile.delete();
                publication.setPhotoUrl(null);
            }
        }
        FooDoNetSQLExecuterAsync saveExecuter
                = new FooDoNetSQLExecuterAsync(this, this);
        saveExecuter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new InternalRequest(
                        InternalRequest.ACTION_SQL_SAVE_NEW_PUBLICATION, publication));
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        switch (request.ActionCommand) {
            case InternalRequest.ACTION_SQL_SAVE_NEW_PUBLICATION:
                if (request.Status == InternalRequest.STATUS_FAIL) {
                    Log.i(MY_TAG, "cant save new pub in sql");
                    return;
                }
/*
                if(TextUtils.isEmpty(request.publicationForSaving.getPhotoUrl())){
                    File imageToSave = new File(request.publicationForSaving.getPhotoUrl());
                    File imageToSaveOnDevice=new File(Environment.getExternalStorageDirectory(),
                            request.publicationForSaving.getUniqueId() + "." + request.publicationForSaving.getVersion() + ".jpg");
                    if (imageToSaveOnDevice.exists()) {
                        imageToSaveOnDevice.delete();
                    }
                    try {
                        CommonUtil.CopyFile(imageToSave, imageToSaveOnDevice);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
*/


                Log.i(MY_TAG, "new pub successfully saved in db, sending to server");
                NotifyToBListenerAboutEvent(ServicesBroadcastReceiver.ACTION_CODE_SAVE_NEW_PUB_SQL_SUCCESS);
                HttpServerConnectorAsync connector
                        = new HttpServerConnectorAsync(getResources().getString(R.string.server_base_url), (IFooDoNetServerCallback) this);
                InternalRequest ir
                        = new InternalRequest(InternalRequest.ACTION_POST_NEW_PUBLICATION,
                        getResources().getString(R.string.server_add_new_publication_path),
                        request.publicationForSaving);
                ir.publicationForSaving = request.publicationForSaving;
                connector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ir);
                break;
            case InternalRequest.ACTION_SQL_UPDATE_ID_OF_PUB_AFTER_SAVING_ON_SERVER:
                if (request.Status == InternalRequest.STATUS_FAIL) {
                    Log.i(MY_TAG, "cant update new pub's id in sql");
                    return;
                }
                File imgFile = new File(Environment.getExternalStorageDirectory() + getString(R.string.image_folder_path),
                        CommonUtil.GetFileNameByPublication(request.publicationForSaving));
                if (imgFile.exists()) {
                    AmazonImageUploader imageUploader = new AmazonImageUploader(getApplicationContext(), this);
                    imageUploader.UploadPublicationImageToAmazon(imgFile, false);
                }
                else
                    NotifyToBListenerAboutEvent(ServicesBroadcastReceiver.ACTION_CODE_SAVE_NEW_PUB_SUCCESS);
                //NotifyToBListenerAboutPubSavedInDB(ServicesBroadcastReceiver.ACTION_CODE_SAVE_NEW_PUB_COMPLETE);
                break;
            case InternalRequest.ACTION_SQL_SAVE_EDITED_PUBLICATION:
                File imgFile1 = new File(Environment.getExternalStorageDirectory() + getString(R.string.image_folder_path),
                        CommonUtil.GetFileNameByPublication(request.publicationForSaving));
                if (imgFile1.exists()) {
                    AmazonImageUploader imageUploader = new AmazonImageUploader(getApplicationContext(), this);
                    imageUploader.UploadPublicationImageToAmazon(imgFile1, true);
                }
                else
                    NotifyToBListenerAboutEvent(ServicesBroadcastReceiver.ACTION_CODE_SAVE_EDITED_PUB_SUCCESS);
                break;
        }
    }

    public void NotifyToBListenerAboutEvent(int eventCode) {
        Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
        intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, eventCode);
        sendBroadcast(intent);
    }

    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        switch (response.ActionCommand) {
            case InternalRequest.ACTION_POST_NEW_PUBLICATION:
                if (response.Status == InternalRequest.STATUS_FAIL) {
                    int id = response.publicationForSaving.getUniqueId();
                    Uri deleteUri = Uri.parse(id >= 0 ?
                            FooDoNetSQLProvider.CONTENT_URI + "/" + String.valueOf(id) :
                            FooDoNetSQLProvider.URI_PUBLICATION_ID_NEGATIVE + "/" + String.valueOf(id * -1));
                    getContentResolver().delete(deleteUri, null, null);
                    NotifyToBListenerAboutEvent(ServicesBroadcastReceiver.ACTION_CODE_SAVE_NEW_PUB_FAIL);
                    return;
                }
                Log.i(MY_TAG, "succeeded saving pub to server, new id: "
                        + response.publicationForSaving.getNewIdFromServer());
                File existingImageFile
                        = new File(Environment.getExternalStorageDirectory()
                        + getString(R.string.image_folder_path),
                        CommonUtil.GetFileNameByPublication(response.publicationForSaving));
                if (existingImageFile.exists()) {
                    String fileName
                            = String.valueOf(response.publicationForSaving.getNewIdFromServer()) + "."
                            + String.valueOf(response.publicationForSaving.getVersionFromServer()) + ".jpg";
                    File destinationFile = new File(Environment.getExternalStorageDirectory()
                            + getString(R.string.image_folder_path), fileName);
                    try {
                        CommonUtil.CopyFile(existingImageFile, destinationFile);
                        existingImageFile.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                FooDoNetSQLExecuterAsync executerAsync = new FooDoNetSQLExecuterAsync(this, this);
                executerAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new InternalRequest(
                        InternalRequest.ACTION_SQL_UPDATE_ID_OF_PUB_AFTER_SAVING_ON_SERVER,
                        response.publicationForSaving));
                break;
            case InternalRequest.ACTION_PUT_EDIT_PUBLICATION:
                FooDoNetSQLExecuterAsync saveExecuter
                        = new FooDoNetSQLExecuterAsync(this, this);
                saveExecuter.execute(new InternalRequest(InternalRequest.ACTION_SQL_SAVE_EDITED_PUBLICATION, response.publicationForSaving));
                break;
        }
    }
}
