package bdonotifier.studiau.com.bdonotifier;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by Daniel Au on 4/24/2016.
 *
 * Set an alarm for the date passed intot he constructor.
 * When the alarm is raised, it will start the NotifyService.
 *
 * This uses the android built-in alarm manager.
 * If the phone is restarted, the alarm will be cancelled.
 *
 * This will run on its' own thread.
 */
public class AlarmTask implements Runnable  {
    // The date selected for the alarm.
    private final Calendar calendar;
    // The android system alarm manager.
    private final AlarmManager alarmManager;
    // Your context to retrieve the alarm manager from.
    private final Context context;
    private final int characterId;

    public AlarmTask(Context context, Calendar calendar, int characterId) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.calendar = calendar;
        this.characterId = characterId;
    }

    @Override
    public void run() {
        // Request to start our service when the alarm date is upon us.
        // We don't start an activity as we just want to pop up a notification into the system bar..
        Intent intent = new Intent(context, NotifyService.class);
        intent.putExtra(NotifyService.INTENT_NOTIFY, true);
        PendingIntent pendingIntent = PendingIntent.getService(context, characterId, intent, 0);

        // Sets an alarm - note this alarm is lost if the phone is turned off.
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
    }

}
