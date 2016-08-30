package upp.foodonet.material;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;
import org.json.JSONException;

import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import DataModel.FNotification;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServerClasses.DownloadImageTask;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IDownloadImageCallBack;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Service listenining for  push notifications
public class FooDoNetGcmListenerService extends GcmListenerService implements IFooDoNetServerCallback, IFooDoNetSQLCallback {
    private static final String TAG = "food_gcmListener";

    private FNotification pushNotification;
    public static final String PUSH_OBJECT_MSG = "message";
    public static final String PUBLICATION_NUMBER = "pubnumber";

    private FCPublication publication;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        if(from.startsWith(getString(R.string.push_notification_prefix)) || from.compareTo(getString(R.string.notifications_server_id)) == 0){
            String msg = data.getString(PUSH_OBJECT_MSG);
            JSONObject jo = new JSONObject();
            try {
                jo = new JSONObject(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pushNotification = FNotification.ParseSingleNotificationFromJSON(jo);//PushObject.DecodePushObject(data);
            HandleMessage(pushNotification);
        }
    }

    private void HandleMessage(FNotification notification) {
        if(notification == null)
            return;
        switch (notification.get_type()){
            case FNotification.FNOTIFICATION_TYPE_NEW_PUBLICATION:
                if(CommonUtil.GetDistanceInKM(CommonUtil.GetMyLocationFromPreferences(this),
                        new LatLng(notification.get_latitude(), notification.get_longitude()))
                        > CommonUtil.GetNotificationsSettingsRadius(this))
                    return;
                getContentResolver().insert(FooDoNetSQLProvider.URI_NOTIFICATIONS, notification.GetContentValuesRow());
                String basePath = getString(R.string.server_base_url);
                String subPath = getString(R.string.server_edit_publication_path).replace("{0}", String.valueOf(notification.get_publication_or_group_id()));
                HttpServerConnectorAsync connectorAsync = new HttpServerConnectorAsync(basePath, (IFooDoNetServerCallback) this);
                connectorAsync.execute(new InternalRequest(InternalRequest.ACTION_PUSH_NEW_PUB, subPath),
                        new InternalRequest(InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION,
                                getResources().getString(R.string.server_get_registered_for_publications)),
                        new InternalRequest(InternalRequest.ACTION_GET_PUBLICATION_REPORTS,
                                getResources().getString(R.string.server_get_publication_report)));
                break;
            case FNotification.FNOTIFICATION_TYPE_DELETED_PUBLICATION:
                getContentResolver().insert(FooDoNetSQLProvider.URI_NOTIFICATIONS, notification.GetContentValuesRow());
                ContentResolver resolver = getContentResolver();
                resolver.delete(FooDoNetSQLProvider.CONTENT_URI,
                        FCPublication.PUBLICATION_UNIQUE_ID_KEY + " = " + notification.get_publication_or_group_id(), null);
                resolver.delete(FooDoNetSQLProvider.URI_GET_ALL_REGS,
                        RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_ID + " = " + notification.get_publication_or_group_id(), null);
                resolver.delete(FooDoNetSQLProvider.URI_GET_ALL_REPORTS,
                        PublicationReport.PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_ID + " = " + notification.get_publication_or_group_id(), null);
                resolver.delete(FooDoNetSQLProvider.URI_NOTIFICATIONS,
                        FNotification.FNOTIFICATION_KEY_PUBLICATION_OR_GROUP_ID + " = " + notification.get_publication_or_group_id()
                        + " AND (" + FNotification.FNOTIFICATION_KEY_TYPE + " = " + FNotification.FNOTIFICATION_TYPE_NEW_PUBLICATION
                        + " OR " + FNotification.FNOTIFICATION_KEY_TYPE + " = " + FNotification.FNOTIFICATION_TYPE_EDITED_PUBLICATION
                        + " OR " + FNotification.FNOTIFICATION_KEY_TYPE + " = " + FNotification.FNOTIFICATION_TYPE_NEW_REGISTRATION
                        + " OR " + FNotification.FNOTIFICATION_KEY_TYPE + " = " + FNotification.FNOTIFICATION_TYPE_NEW_REPORT + ")", null);
                Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
                intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                        ServicesBroadcastReceiver.ACTION_CODE_NOTIFICATION_RECEIVED_PUBLICATION_DELETED);
                intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_NOTIFICATION_KEY, notification);
                CommonUtil.SavePendingBroadcastToSharedPreferences(this, intent);
                sendBroadcast(intent);
                FCPublication deletedPublication = new FCPublication();
                deletedPublication.setTitle(notification.get_publication_or_group_title());
                SendNotification(deletedPublication, InternalRequest.ACTION_PUSH_PUB_DELETED);
                break;
            case FNotification.FNOTIFICATION_TYPE_EDITED_PUBLICATION:
                // no such notification for now
                getContentResolver().insert(FooDoNetSQLProvider.URI_NOTIFICATIONS, notification.GetContentValuesRow());
                break;
            case FNotification.FNOTIFICATION_TYPE_NEW_REGISTRATION:
                getContentResolver().insert(FooDoNetSQLProvider.URI_NOTIFICATIONS, notification.GetContentValuesRow());
                HttpServerConnectorAsync connector = new HttpServerConnectorAsync(getString(R.string.server_base_url), (IFooDoNetServerCallback) this);
                InternalRequest registeredRequest = new InternalRequest(InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION,
                        getResources().getString(R.string.server_get_registered_for_publications)
                                .replace("{0}", String.valueOf(notification.get_publication_or_group_id())));
                registeredRequest.PublicationID = notification.get_publication_or_group_id();
                connector.execute(registeredRequest);
                break;

        }
/*
        switch (pushObject.PushObjectType) {
            case PushObject.PUSH_OBJECT_VALUE_NEW:
                String basePath = getString(R.string.server_base_url);
                String subPath = getString(R.string.server_edit_publication_path).replace("{0}", String.valueOf(pushObject.ID));
                HttpServerConnectorAsync connectorAsync = new HttpServerConnectorAsync(basePath, (IFooDoNetServerCallback) this);
                connectorAsync.execute(new InternalRequest(InternalRequest.ACTION_PUSH_NEW_PUB, subPath),
                        new InternalRequest(InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION,
                                getResources().getString(R.string.server_get_registered_for_publications)),
                        new InternalRequest(InternalRequest.ACTION_GET_PUBLICATION_REPORTS,
                                getResources().getString(R.string.server_get_publication_report)));
                break;
            case PushObject.PUSH_OBJECT_VALUE_DELETE:
                FooDoNetSQLExecuterAsync sqlExecuterAsync = new FooDoNetSQLExecuterAsync(this, this);
                InternalRequest irPubIdToDelete = new InternalRequest(InternalRequest.ACTION_PUSH_PUB_DELETED);
                irPubIdToDelete.PublicationID = pushObject.ID;
                sqlExecuterAsync.execute(irPubIdToDelete);
                break;
            case PushObject.PUSH_OBJECT_VALUE_REPORT:
                PublicationReport publicationReport
                        = new PublicationReport(pushObject.ID,
                        pushObject.PublicationID,
                        pushObject.PublicationVersion,
                        pushObject.Report,
                        pushObject.DateOfReport, "");//// TODO: 31.10.2015 also need publisher uuid
                InternalRequest irReportFromPush = new InternalRequest(InternalRequest.ACTION_PUSH_REPORT_FOR_PUB, publicationReport);
                irReportFromPush.PublicationID = pushObject.PublicationID;
                FooDoNetSQLExecuterAsync sqlExecuterAsync1 = new FooDoNetSQLExecuterAsync(this, this);
                sqlExecuterAsync1.execute(irReportFromPush);
                break;
            case PushObject.PUSH_OBJECT_VALUE_REG:
                String basePath1 = getString(R.string.server_base_url);
                String subPath1 = getString(R.string.server_edit_publication_path).replace("{0}", String.valueOf(pushObject.ID));
                long publicationID = (long)pushObject.ID;
                HttpServerConnectorAsync connectorAsync1 = new HttpServerConnectorAsync(basePath1, (IFooDoNetServerCallback) this);
                connectorAsync1.execute(new InternalRequest(InternalRequest.ACTION_PUSH_REG, subPath1),
                        new InternalRequest(InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION,
                                getResources().getString(R.string.server_get_registered_for_publications)),
                        new InternalRequest(InternalRequest.ACTION_PUSH_REG, publicationID));
                break;
        }
*/
    }


    @Override
    public void OnServerRespondedCallback(InternalRequest response) {
        switch (response.ActionCommand){
            case InternalRequest.ACTION_PUSH_NEW_PUB:
                if(response.Status == InternalRequest.STATUS_OK
                        && response.publicationForSaving != null){
                    if(response.publicationForSaving.getPublisherUID().compareTo(CommonUtil.GetIMEI(this)) == 0)
                        return;

                    FooDoNetSQLExecuterAsync sqlExecuterAsync = new FooDoNetSQLExecuterAsync(this, this);
                    sqlExecuterAsync.execute(response);
                }
                break;
            case InternalRequest.ACTION_PUSH_REG:
                if(response.Status == InternalRequest.STATUS_OK){
                    FooDoNetSQLExecuterAsync sqlExecuterAsync = new FooDoNetSQLExecuterAsync(this, this);
                    sqlExecuterAsync.execute(response);
                }
                break;
            case InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION:
                if(response.Status == InternalRequest.STATUS_OK){
                    ContentResolver resolver = getContentResolver();
                    resolver.delete(Uri.parse(FooDoNetSQLProvider.URI_DELETE_REGISTERED_USER_BY_PUBLICATION_ID + "/" + response.PublicationID), null, null);
                    if(response.registeredUsers != null && response.registeredUsers.size() != 0){
                        for(RegisteredUserForPublication reg : response.registeredUsers)
                            resolver.insert(FooDoNetSQLProvider.URI_INSERT_REGISTERED_FOR_PUBLICATION, reg.GetContentValuesRow());
                    }
                    Cursor cursor = resolver.query(Uri.parse(FooDoNetSQLProvider.CONTENT_URI + "/" + response.PublicationID),
                            FCPublication.GetColumnNamesArray(), null, null, null);
                    FCPublication publication = FCPublication.GetArrayListOfPublicationsFromCursor(cursor, false).get(0);
                    Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
                    intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                            ServicesBroadcastReceiver.ACTION_CODE_NOTIFICATION_RECEIVED_NEW_REGISTERED_USER);
                    sendBroadcast(intent);
                    SendNotification(publication, InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION);
                }
                break;
        }
    }

    @Override
    public void OnSQLTaskComplete(InternalRequest request) {
        switch (request.ActionCommand){
            case InternalRequest.ACTION_PUSH_NEW_PUB:
                if(request.Status == InternalRequest.STATUS_OK){

                    publication = request.publicationForSaving;
//                    String imageName = getString(R.string.publication_picture_file_name_format)
//                            .replace("{0}", String.valueOf(publication.getUniqueId()))
//                            .replace("{1}", String.valueOf(publication.getVersion()));
//                    int maxImageWidthHeight = getResources().getInteger(R.integer.max_image_width_height);
//                    CommonUtil.LoadAndSavePicture(null, getResources().getString(R.string.amazon_base_url_for_images) + "/"
//                            + imageName, maxImageWidthHeight, getString(R.string.image_folder_path), imageName);
                    Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
                    intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                            ServicesBroadcastReceiver.ACTION_CODE_NOTIFICATION_RECEIVED_NEW_PUBLICATION);

                    sendBroadcast(intent);
                    SendNotification(publication, request.ActionCommand);
/*
                    DownloadImageTask imageTask
                            = new DownloadImageTask(this,
                            getResources().getString(R.string.amazon_base_url_for_images), maxImageWidthHeight,
                            getResources().getString(R.string.image_folder_path));
                    Map<Integer,Integer> map = new HashMap<>();
                    map.put(request.publicationForSaving.getUniqueId(), request.publicationForSaving.getVersion());
                    imageTask.setRequestHashMap(map);
                    imageTask.execute();
*/
                }
                break;
            case InternalRequest.ACTION_PUSH_PUB_DELETED:
                if(request.Status == InternalRequest.STATUS_OK)
                    //SendNotification(request.publicationForSaving, request.ActionCommand);
                //PublishNotificationPublicationDeleted(request.publicationForSaving.getTitle());
                break;
            case InternalRequest.ACTION_PUSH_REPORT_FOR_PUB:
                if(request.Status == InternalRequest.STATUS_OK)
                    //SendNotification(request.publicationForSaving, request.ActionCommand);
                //PublishNotificationForNewReport(request.publicationForSaving.getTitle(), pushObject.Report);
                break;
            case InternalRequest.ACTION_PUSH_REG:
                if(request.Status == InternalRequest.STATUS_OK)
                    //SendNotification(request.publicationForSaving, request.ActionCommand);
                //PublishNot
                break;
        }
    }

    private void SendNotification(FCPublication publication, int action) {
        if(CommonUtil.GetIsApplicationRunningInForeground(this))
            return;
        switch (action){
            case InternalRequest.ACTION_PUSH_NEW_PUB:
            case InternalRequest.ACTION_PUSH_PUB_DELETED:
            case InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION:
                Intent intent = new Intent();
                String title = publication.getTitle();
                String message = "";
                switch (action){
                    case InternalRequest.ACTION_PUSH_NEW_PUB:
                        intent = new Intent(this, EntarenceMapAndListActivity.class);
                        intent.putExtra(PUBLICATION_NUMBER, publication.getUniqueId());
                        message = getString(R.string.notification_title_new_event_near_you) + " " + publication.getTitle();
                        break;
                    case InternalRequest.ACTION_PUSH_PUB_DELETED:
                        intent = new Intent(this, EntarenceMapAndListActivity.class);
                        message = getString(R.string.notification_publication_has_been_removed_by_owner) + " " + publication.getTitle();
                        break;
                    case InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION:
                        intent = new Intent(this, EntarenceMapAndListActivity.class);
                        intent.putExtra(PUBLICATION_NUMBER, publication.getUniqueId());
                        message = getString(R.string.notification_text_new_registration).replace("{0}", publication.getTitle());
                        break;

                }
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.app_logo_for_icon)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
                break;
        }
    }

/*
    private void SendNotification(FCPublication publication, int action) {
        Intent intent = new Intent();
        String title = publication.getTitle();
        String message = "";
        switch (action){
            case InternalRequest.ACTION_PUSH_NEW_PUB:
                intent = new Intent(this, MapAndListActivity.class);
                intent.putExtra(PUBLICATION_NUMBER, publication.getUniqueId());
                message = getString(R.string.notif_new_pub);

                break;
            case InternalRequest.ACTION_PUSH_PUB_DELETED:
                intent = new Intent(this, SplashScreenActivity.class);
                message = getString(R.string.notif_was_deleted);
                break;
            case InternalRequest.ACTION_PUSH_REPORT_FOR_PUB:
                intent = new Intent(this, MapAndListActivity.class);
                intent.putExtra(PUBLICATION_NUMBER, publication.getUniqueId());
                intent.putExtra(PUBLICATION_NUMBER, pushObject.Report);
                message = getString(R.string.notif_report);
            case InternalRequest.ACTION_PUSH_REG:
                intent = new Intent(this, MapAndListActivity.class);
                intent.putExtra(PUBLICATION_NUMBER, publication.getUniqueId());
                message = getString(R.string.notif_new_registered_user);
                break;
        }
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 */
/* Request code *//*
, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.side_menu_collect_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 */
/* ID of notification *//*
, notificationBuilder.build());
    }
*/


}
