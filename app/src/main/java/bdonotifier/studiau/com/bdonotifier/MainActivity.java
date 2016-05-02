package bdonotifier.studiau.com.bdonotifier;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_MAX_ENERGY = "lels";
    private static final long CONSTANT_TIME_PER_ENERGY = 1 * 60 * 1000; // minutes*seconds*milliseconds

    private MySQLiteHelper characterDB;
    // This is a handle so that we can call methods on our service.
    private ScheduleClient scheduleClient;
    private SharedPreferences sharedPreferences;
    private Button maxEnergyButton;
    private TableLayout characterTable;
    private List<Character> characterList;
    private int maxEnergy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCharacterDialog();
            }
        });

        clearActiveNotifications();

        // Create a new service client and bind our activity to this service.
        scheduleClient = new ScheduleClient(this);
        scheduleClient.doBindService();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        maxEnergyButton = (Button) findViewById(R.id.maxEnergyButton);

        characterDB = new MySQLiteHelper(this);

        loadValues();
        loadCharacters();
        updateCharacterEnergies();
        showCharacters();
    }

    private void clearActiveNotifications() {
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private void loadValues() {
        maxEnergy = sharedPreferences.getInt(KEY_MAX_ENERGY, 30);
        maxEnergyButton.setText(String.valueOf(maxEnergy));
    }

    private void loadCharacters() {
        characterList = characterDB.getAllCharacters();
    }

    private void refreshCharacters() {
        characterList.clear();
        loadCharacters();
        updateCharacterEnergies();
        characterTable.removeAllViews();
        showCharacters();
    }

    private void showCharacters() {
        characterTable = (TableLayout) findViewById(R.id.characterTable);
        characterTable.setStretchAllColumns(true);

        for (Character character : characterList) {
            final String characterName = character.getName();

            final MyTextView characterNameTextView = new MyTextView(this, R.style.MyCharacterText);
            characterNameTextView.setText(characterName);

            // Energy recovered date and time.
            final MyTextView characterReadyTextView = new MyTextView(this, R.style.MyCharacterSubText);
            String characterReadyText = "Fully recovered!";
            if (character.getEnergy() < maxEnergy) {
                float characterEnergyDifference = maxEnergy - character.getEnergy();
                long characterRecoveryTime = System.currentTimeMillis() +
                        (long) (characterEnergyDifference * CONSTANT_TIME_PER_ENERGY);
                Calendar characterCalendar = Calendar.getInstance();
                characterCalendar.setTimeInMillis(characterRecoveryTime);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                        "EEEE 'at' h:mm a", Locale.getDefault());
                characterReadyText = "wait till " + simpleDateFormat.format(characterCalendar.getTime());
            }
            characterReadyTextView.setText(characterReadyText);

            // Energy Button
            final MyButton characterEnergyButton = new MyButton(this, R.style.MyButtonText);
            characterEnergyButton.setText(String.valueOf((int) Math.floor(character.getEnergy())));
            //characterEnergyButton.setText(String.valueOf(character.getEnergy()));
            characterEnergyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateEnergyDialog(characterName);
                }
            });

            // Prototype
            RelativeLayout characterLayout = new RelativeLayout(this);
            characterLayout.setPadding(0, 64, 0, 0);
            LinearLayout characterTextLayout = new LinearLayout(this);
            characterTextLayout.setOrientation(LinearLayout.VERTICAL);
            characterTextLayout.addView(characterNameTextView);
            characterTextLayout.addView(characterReadyTextView);
            characterTextLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    deleteCharacterDialog(characterName);
                    return false;
                }
            });
            characterLayout.addView(characterTextLayout);
            LinearLayout characterButtonLayout = new LinearLayout(this);
            characterButtonLayout.addView(characterEnergyButton);
            RelativeLayout.LayoutParams characterButtonLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            characterButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            characterLayout.addView(characterButtonLayout, characterButtonLayoutParams);
            characterTable.addView(characterLayout);
        }
    }

    private void updateCharacterEnergies() {
        long currentTime = System.currentTimeMillis();
        for ( Character character : characterList) {
            long characterTime = character.getLastTimeStamp();
            long timeDifference = currentTime - characterTime;
            float energyRecovered = (float) timeDifference / CONSTANT_TIME_PER_ENERGY;
            if (character.getEnergy() + energyRecovered >= maxEnergy) {
                character.setEnergy(maxEnergy);
            } else {
                character.setEnergy(character.getEnergy() + energyRecovered);
            }
            character.setLastTimeStamp(currentTime);
            characterDB.updateCharacterEnergy(character.getName(),
                    character.getEnergy(),
                    character.getLastTimeStamp());
        }
    }

    private void createCharacterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);

        builder.setTitle("Add Character");

        // Character name input field.
        final EditText inputName = new EditText(this);
        inputName.setHint("Character Name");
        inputName.setInputType(InputType.TYPE_CLASS_TEXT);
        inputName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        TableRow rowName = new TableRow(this);
        rowName.addView(inputName);
        // Character energy input field.
        final EditText inputEnergy = new EditText(this);
        inputEnergy.setHint("Current Energy");
        inputEnergy.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputEnergy.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        TableRow rowEnergy = new TableRow(this);
        rowEnergy.addView(inputEnergy);
        // Layout for dialog.
        final TableLayout inputTable = new TableLayout(this);
        inputTable.setStretchAllColumns(true);
        inputTable.addView(rowName);
        inputTable.addView(rowEnergy);

        builder.setView(inputTable);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (!inputName.getText().toString().equals("") &&
                        !String.valueOf(inputEnergy.getText().toString()).equals("")
                        && Float.valueOf(inputEnergy.getText().toString()) < maxEnergy) {
                    String characterName = inputName.getText().toString().trim();
                    float characterEnergy = Float.valueOf(inputEnergy.getText().toString());
                    Character character = new Character(characterName,
                            characterEnergy,
                            System.currentTimeMillis());
                    characterDB.addCharacter(character);
                    setTimer(character);
                    refreshCharacters();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void deleteCharacterDialog(final String characterName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);

        builder.setTitle("Delete " + characterName + "?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                characterDB.deleteCharacter(characterName);
                refreshCharacters();
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void updateEnergyDialog(final String characterName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        builder.setTitle("Current Energy");

        // Character energy input field.
        final EditText inputEnergy = new EditText(this);
        inputEnergy.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputEnergy.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        inputEnergy.setHint(characterName + "'s new energy");
        builder.setView(inputEnergy);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!inputEnergy.getText().toString().equals("") &&
                        Float.valueOf(inputEnergy.getText().toString()) < maxEnergy) {
                    float newCharacterEnergy = Float.valueOf(inputEnergy.getText().toString());
                    Long characterTimeStamp = System.currentTimeMillis();
                    characterDB.updateCharacterEnergy(characterName,
                            newCharacterEnergy,
                            characterTimeStamp);
                    setTimer(characterDB.findCharacter(characterName));
                    refreshCharacters();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * This is the onClick called from the XML to set a Max Energy.
     */
    public void setMaxEnergy(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);

        builder.setTitle("Max Energy");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        input.setHint("Family Max Energy");
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                if (!value.equals("")) {
                    maxEnergy = Integer.valueOf(value);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(KEY_MAX_ENERGY, maxEnergy);
                    editor.apply();
                    maxEnergyButton.setText(String.valueOf(maxEnergy));
                    resetCharacterTimers();
                }
                // Add method to reset all notifications.
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
        }

    /**
     * This is the onClick called from the XML to set a new notification.
     */
    private void setTimer(Character character) {
        long addTime = (long) (maxEnergy - character.getEnergy()) * CONSTANT_TIME_PER_ENERGY;
        long newTime = System.currentTimeMillis() + addTime;
        // Set the time to the specified number of minutes ahead of the current time.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(newTime);

        int characterId = character.getId();
        String characterName = character.getName();

        // Ask our service to set an alarm for that day and time, this activity
        // talks to the client that talks to the service.
        scheduleClient.setAlarmForNotification(calendar, characterId, characterName);
        // Notify the user what they just did.
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "EEEE 'at' h:mm a", Locale.getDefault());
        String snackbarMessage = "Notification set for " +
                simpleDateFormat.format(calendar.getTime());
        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout),
                snackbarMessage,
                Snackbar.LENGTH_LONG);
        snackbar.show();

    }

    private void resetCharacterTimers() {
        for (Character character : characterList) {
            if (character.getEnergy() < maxEnergy) {
                setTimer(character);
            }
        }
        refreshCharacters();
    }

    @Override
    protected void onStop() {
        // When our activity is stopped, ensure we also stop the connection to the service.
        // This stops us leaking our activity in the system!
        if(scheduleClient != null) {
            scheduleClient.doUnbindService();
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
}
