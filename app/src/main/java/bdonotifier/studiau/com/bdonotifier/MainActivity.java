package bdonotifier.studiau.com.bdonotifier;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_MAX_ENERGY = "lels";
    private static final long CONSTANT_TIME = 30 * 60 * 1000; // minutes*seconds*milliseconds

    private MySQLiteHelper characterDB;
    // This is a handle so that we can call methods on our service.
    private ScheduleClient scheduleClient;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;
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

        characterDB = new MySQLiteHelper(this);

        // Create a new service client and bind our activity to this service.
        scheduleClient = new ScheduleClient(this);
        scheduleClient.doBindService();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferencesEditor = sharedPreferences.edit();

        maxEnergyButton = (Button) findViewById(R.id.maxEnergyButton);

        loadValues();
        loadCharacters();
        showCharacters();
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
        characterTable.removeAllViews();
        characterTable.invalidate();
        characterTable.refreshDrawableState();
        showCharacters();
    }

    private void showCharacters() {
        characterTable = (TableLayout) findViewById(R.id.characterTable);
        characterTable.setStretchAllColumns(true);
        TableRow.LayoutParams rightGravityParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        rightGravityParams.gravity = Gravity.RIGHT;
        for (Character character : characterList) {
            TableRow row = new TableRow(this);

            final TextView characterName = new TextView(this);
            characterName.setText(character.getName());
            characterName.setTextAppearance(this, R.style.MyNormalText);
            characterName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    deleteCharacterDialog(characterName.getText().toString());
                    return false;
                }
            });
            row.addView(characterName);

            final Button characterEnergy = new Button(this);
            characterEnergy.setText(String.valueOf(character.getEnergy()));
            characterEnergy.setTextAppearance(this, R.style.MyNormalText);
            characterEnergy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateEnergyDialog(characterName.getText().toString());
                }
            });
            row.addView(characterEnergy, rightGravityParams);

            characterTable.addView(row);
        }
    }

    private void createCharacterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
                String characterName = inputName.getText().toString();
                int characterEnergy = Integer.valueOf(inputEnergy.getText().toString());
                if (!characterName.equals("") && !String.valueOf(characterEnergy).equals("")
                        && characterEnergy < maxEnergy) {
                    Character character = new Character(characterName,
                            Integer.valueOf(inputEnergy.getText().toString()),
                            System.currentTimeMillis());
                    characterDB.addCharacter(character);
                }
                refreshCharacters();
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void deleteCharacterDialog(final String characterName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Delete character?");

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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Current Energy");

        // Character energy input field.
        final EditText inputEnergy = new EditText(this);
        inputEnergy.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputEnergy.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        builder.setView(inputEnergy);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int newCharacterEnergy = Integer.valueOf(inputEnergy.getText().toString());
                if (!inputEnergy.getText().toString().equals("") && newCharacterEnergy < maxEnergy) {
                    Long characterTimeStamp = System.currentTimeMillis();
                    characterDB.updateCharacterEnergy(characterName, newCharacterEnergy, characterTimeStamp);
                    refreshCharacters();
                    setTimer(newCharacterEnergy);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
                    int maxEnergy = Integer.valueOf(value);
                    sharedPreferencesEditor.putInt(KEY_MAX_ENERGY, maxEnergy);
                    sharedPreferencesEditor.commit();
                    maxEnergyButton.setText(String.valueOf(maxEnergy));
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
    public void setTimer(int newCharacterEnergy) {
        long addTime = (long) (maxEnergy - newCharacterEnergy) * CONSTANT_TIME;
        long newTime = System.currentTimeMillis() + addTime;
        // Set the time to the specified number of minutes ahead of the current time.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(newTime);
        //calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + timer);

        // Ask our service to set an alarm for that day and time, this activity
        // talks to the client that talks to the service.
        scheduleClient.setAlarmForNotification(calendar);
        // Notify the user what they just did.
        Toast.makeText(this, calendar.get(Calendar.YEAR) + "-" +
                calendar.get(Calendar.MONTH) + "-" +
                calendar.get(Calendar.DAY_OF_MONTH) + " " +
                calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                calendar.get(Calendar.MINUTE), Toast.LENGTH_LONG).show();
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
