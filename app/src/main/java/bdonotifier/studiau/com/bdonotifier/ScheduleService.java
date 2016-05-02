package bdonotifier.studiau.com.bdonotifier;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Daniel Au on 4/24/2016.
 */
public class ScheduleService extends Service {

    // Class for clients to access
    public class ServiceBinder extends Binder {
        ScheduleService getService() {
            return ScheduleService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ScheduleService", "Received start id " + startId + ": " + intent);

        // We want this service to continue running until it is explicitly stopped.
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new ServiceBinder();

    // Show an alarm for a certain date. When the alarm is called it will pop a notification.
    public void setAlarm(Calendar calendar, int characterId, String characterName) {
        // This starts a new thread to set the alarm.
        // You want to push off your tasks onto a new thread to free up the UI to carry on responding.
        new AlarmTask(this, calendar, characterId, characterName).run();

    }
}
