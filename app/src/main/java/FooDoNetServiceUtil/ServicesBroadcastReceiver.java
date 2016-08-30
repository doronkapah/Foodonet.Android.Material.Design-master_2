package FooDoNetServiceUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Asher on 09.09.2015.
 */
public class ServicesBroadcastReceiver extends BroadcastReceiver {

    private static final String MY_TAG = "food_broadcastRec";

    public static final String BROADCAST_REC_INTENT_FILTER = "upp.foodonet.broadcast.receiver.common";

    private IBroadcastReceiverCallback callbackListener;

    public static final String BROADCAST_REC_EXTRA_ACTION_KEY = "action_extra_key";
    public static final String BROADCAST_REC_EXTRA_LOCATION_KEY = "action_extra_location";
    public static final String BROADCAST_REC_EXTRA_NOTIFICATION_KEY = "action_extra_notification";

    public static final int ACTION_CODE_REGISTRATION_SUCCESS = 1;
    public static final int ACTION_CODE_REGISTRATION_FAIL = 2;
    public static final int ACTION_CODE_RELOAD_DATA_SUCCESS = 11;
    public static final int ACTION_CODE_RELOAD_DATA_FAIL = 12;
    public static final int ACTION_CODE_SAVE_NEW_PUB_SQL_SUCCESS = 21;
    public static final int ACTION_CODE_SAVE_NEW_PUB_SUCCESS = 22;
    public static final int ACTION_CODE_SAVE_NEW_PUB_FAIL = 23;
    public static final int ACTION_CODE_SAVE_EDITED_PUB_SUCCESS = 24;
    public static final int ACTION_CODE_SAVE_EDITED_PUB_FAIL = 25;
    public static final int ACTION_CODE_GET_LOCATION_SUCCESS = 31;
    public static final int ACTION_CODE_GET_LOCATION_FAIL = 32;
    public static final int ACTION_CODE_REGISTER_TO_PUBLICATION_SUCCESS = 41;
    public static final int ACTION_CODE_REGISTER_TO_PUBLICATION_FAIL = 42;
    public static final int ACTION_CODE_UNREGISTER_FROM_PUBLICATION_SUCCESS = 43;
    public static final int ACTION_CODE_UNREGISTER_FROM_PUBLICATION_FAIL = 44;
    public static final int ACTION_CODE_ADD_MYSELF_TO_REGS_FOR_PUBLICATION = 45;
    public static final int ACTION_CODE_REMOVE_MYSELF_FROM_REGS_FOR_PUBLICATION = 46;
    public static final int ACTION_CODE_REPORT_TO_PUBLICATION_SUCCESS = 51;
    public static final int ACTION_CODE_REPORT_TO_PUBLICATION_FAIL = 52;
    public static final int ACTION_CODE_NOTIFICATION_RECEIVED_NEW_PUBLICATION = 61;
    public static final int ACTION_CODE_NOTIFICATION_RECEIVED_PUBLICATION_DELETED = 62;
    public static final int ACTION_CODE_NOTIFICATION_RECEIVED_PUBLICATION_EDITED = 63;
    public static final int ACTION_CODE_NOTIFICATION_RECEIVED_NEW_REGISTERED_USER = 64;
    public static final int ACTION_CODE_NOTIFICATION_RECEIVED_NEW_REPORT = 65;
    public static final int ACTION_CODE_NOTIFICATION_RECEIVED_ADDED_TO_GROUP = 66;
    public static final int ACTION_CODE_NOTIFICATION_RECEIVED_REMOVED_FROM_GROUP = 67;


    public ServicesBroadcastReceiver(IBroadcastReceiverCallback callback){
        callbackListener = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(MY_TAG, "some broadcast received!");
        if(callbackListener != null)
            callbackListener.onBroadcastReceived(intent);
    }
}
