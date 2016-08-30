package upp.foodonet.material;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import DataModel.FCPublication;
import FooDoNetSQLClasses.FooDoNetSQLExecuterAsync;
import FooDoNetSQLClasses.IFooDoNetSQLCallback;
import FooDoNetServerClasses.HttpServerConnectorAsync;
import FooDoNetServerClasses.IFooDoNetServerCallback;
import CommonUtilPackage.InternalRequest;
//import FooDoNetServiceUtil.IFooDoNetCustomServiceBinder;
//import FooDoNetServiceUtil.IFooDoNetServiceCallback;

public class FooDoNetService
        extends Service {

    private static boolean isStarted = false;
    private static int cycleCounter = 0;

    private boolean mustRun = false;

    private int secondsToWait;

    WaiterForScheduler waiter;


    private final String MY_TAG = "food_SchedulerService";

    public FooDoNetService() {
        mustRun = true;

    }

    @Override
    public void onDestroy() {
        Log.i(MY_TAG, "destroing scheduler");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        secondsToWait = getResources().getInteger(R.integer.fetch_data_scheduler_repeat_time);
        StartScheduler();
        return START_STICKY;
    }

    private void StartScheduler(){
        Log.i(MY_TAG, "running scheduler, cycle " + ++cycleCounter);
        isStarted = true;
        ReloadDataIntentService.startActionReloadData(getBaseContext());
        waiter = new WaiterForScheduler(finishedSleepingHandler);
        waiter.execute(secondsToWait);
    }

    Handler finishedSleepingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    StartScheduler();
                    break;
                default:
                    Log.e(MY_TAG, "Handler got unexpected msg.what");
                    break;
            }
        }
    };

    private class WaiterForScheduler extends AsyncTask<Integer, Void, Void> {

        Handler callbackHandler;

        public WaiterForScheduler(Handler handler) {
            callbackHandler = handler;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            if (params.length == 0 || params[0] <= 0)
                throw new IllegalArgumentException("service scheduler got no time param");
            final int secsToSleep = params[0];
            Log.i("food", "scheduler sleeps...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(secsToSleep);
                        callbackHandler.sendEmptyMessage(0);
                        Log.i("food", "finished sleeping");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
        }
    }
}
