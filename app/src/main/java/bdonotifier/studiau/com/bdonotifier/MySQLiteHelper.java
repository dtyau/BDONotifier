package bdonotifier.studiau.com.bdonotifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Daniel Au on 4/27/2016.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "characterDB";

    // Characters table name
    private static final String TABLE_CHARACTERS = "characters";
    // Characters table column names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_ENERGY = "energy";
    private static final String KEY_TIMESTAMP = "timestamp";

    private static final String[] COLUMNS = {KEY_ID, KEY_NAME, KEY_ENERGY, KEY_TIMESTAMP};

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement ot create character table.
        String CREATE_CHARACTER_TABLE = "CREATE TABLE characters ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "energy FLOAT, " +
                "timestamp LONG )";

        // Create characters table.
        db.execSQL(CREATE_CHARACTER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older characters table if existing.
        db.execSQL("DROP TABLE IF EXISTS characters");

        // Create fresh characters table.
        this.onCreate(db);
    }

    public void addCharacter(Character character) {

        // 1. Get reference to writable DB.
        SQLiteDatabase db = this.getWritableDatabase();
        // 2. Create ContentValues to add key "column"/value.
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NAME, character.getName());
        contentValues.put(KEY_ENERGY, character.getEnergy());
        contentValues.put(KEY_TIMESTAMP, character.getLastTimeStamp());
        // 3. Insert into the database.
        db.insert(TABLE_CHARACTERS, null, contentValues); //null is some kind of nullColumnHack
        // 4. Close the db.
        db.close();
    }

    public Character getCharacter(int id) {
        // 1. Get reference to readable DB.
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. Build the query.
        Cursor cursor = db.query(TABLE_CHARACTERS, COLUMNS,
                " id = ?", // Selections
                new String[] { String.valueOf(id) }, // Selection arguments
                null, // Group by
                null, // Having
                null, // Order by
                null); // Limit
        // 3. If we get results, get the first one.
        if (cursor != null) {
            cursor.moveToFirst();
        }
        // 4. Build the book object.
        Character character = new Character();
        character.setId(Integer.parseInt(cursor.getString(0)));
        character.setName(cursor.getString(1));
        character.setEnergy(Integer.parseInt(cursor.getString(2)));
        character.setLastTimeStamp(Long.parseLong(cursor.getString(3)));

        cursor.close();
        // 5. Close the db.
        db.close();

        // 6. Return character.
        return character;
    }

    public List<Character> getAllCharacters() {
        List<Character> characters = new LinkedList<Character>();

        // 1. Build the query.
        String query = "Select * FROM " + TABLE_CHARACTERS;
        // 2. Get a reference to writable DB.
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        // 3. Go over each row, build book and add it to the list.
        Character character = null;
        if (cursor.moveToFirst()) {
            do {
                character = new Character();
                character.setId(Integer.parseInt(cursor.getString(0)));
                character.setName(cursor.getString(1));
                character.setEnergy(Float.parseFloat(cursor.getString(2)));
                character.setLastTimeStamp(Long.parseLong(cursor.getString(3)));

                // Add character to characters.
                characters.add(character);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // 4. Close the db.
        db.close();

        // Return characters.
        return characters;
    }

    public int updateCharacter(Character character) {
        // 1. Get reference to writable DB.
        SQLiteDatabase db = this.getWritableDatabase();
        // 2. Create ContentValues to add key "column"/value.
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NAME, character.getName());
        contentValues.put(KEY_ENERGY, character.getEnergy());
        contentValues.put(KEY_TIMESTAMP, character.getLastTimeStamp());
        // 3. Updating row.
        int i = db.update(TABLE_CHARACTERS, contentValues, KEY_ID + " = ?",
                new String[] { String.valueOf(character.getId()) }); // Select arguments
        // 4. Close the DB.
        db.close();

        return i;
    }

    public int updateCharacterEnergy(String characterName, float characterEnergy, long characterTimeStamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ENERGY, characterEnergy);
        contentValues.put(KEY_TIMESTAMP, characterTimeStamp);
        int i = db.update(TABLE_CHARACTERS, contentValues, KEY_NAME + " = ?",
                new String[] { characterName });
        db.close();
        return i;
    }

    public void deleteCharacter(Character character) {
        // 1. Get reference to writable DB.
        SQLiteDatabase db = this.getWritableDatabase();
        // 2. Delete the character.
        db.delete(TABLE_CHARACTERS, KEY_ID + " = ?",
                new String[]{String.valueOf(character.getId())}); // Select arguments
        // 3. Close the DB.
        db.close();
    }

    public void deleteCharacter(String characterName) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_CHARACTERS, KEY_NAME + " = ?",
                new String[]{characterName});

        db.close();
    }

    public Character findCharacter(String characterName) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CHARACTERS, COLUMNS,
                " name = ?", // Selections
                new String[] { String.valueOf(characterName) }, // Selection arguments
                null, // Group by
                null, // Having
                null, // Order by
                null); // Limit
        // 3. If we get results, get the first one.
        if (cursor != null ) {
            cursor.moveToFirst();
        }
        // 4. Build the book object.
        Character character = new Character();
        character.setId(Integer.parseInt(cursor.getString(0)));
        character.setName(cursor.getString(1));
        character.setEnergy(Integer.parseInt(cursor.getString(2)));
        character.setLastTimeStamp(Long.parseLong(cursor.getString(3)));

        cursor.close();
        // 5. Close the db.
        db.close();

        // 6. Return character.
        return character;
    }

}
