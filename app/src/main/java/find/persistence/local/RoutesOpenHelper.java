package find.persistence.local;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class RoutesOpenHelper extends SQLiteOpenHelper {

    // section
    public static final String SECTION_TABLE_NAME = "section";
    // occurrences
    public static final String OCCURRENCE_TABLE_NAME = "occurrence";
    // risk
    public static final String RISK_TABLE_NAME = "risk";
    // route
    public static final String ROUTE_TABLE_NAME = "route";
    // collection
    public static final String COLLECTION_TABLE_NAME = "collection";
    // last server access
    public static final String SERVER_TABLE_NAME = "server";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FIND-Routes";
    private static final String SECTION_TABLE_CREATE = "create table "
            + SECTION_TABLE_NAME
            + " (_id integer primary key, startLat double, startLng double, endLat double, endLng double, state integer, timestamp double);";
    private static final String OCCURRENCE_TABLE_CREATE = "create table "
            + OCCURRENCE_TABLE_NAME + " (_id integer primary key, name text);";
    private static final String RISK_TABLE_CREATE = "create table "
            + RISK_TABLE_NAME
            + " (section_id integer, occurrence_id integer, level integer, primary key (section_id, occurrence_id));";
    private static final String ROUTE_TABLE_CREATE = "create table "
            + ROUTE_TABLE_NAME
            + " (_id integer primary key, name text, description text);";
    private static final String COLLECTION_TABLE_CREATE = "create table "
            + COLLECTION_TABLE_NAME
            + " (route_id integer, section_id integer, primary key (route_id, section_id));";
    private static final String SERVER_TABLE_CREATE = "create table "
            + SERVER_TABLE_NAME + " (lastAccess double primary key);";

    public RoutesOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SECTION_TABLE_CREATE);
        db.execSQL(OCCURRENCE_TABLE_CREATE);
        db.execSQL(RISK_TABLE_CREATE);
        db.execSQL(ROUTE_TABLE_CREATE);
        db.execSQL(COLLECTION_TABLE_CREATE);
        db.execSQL(SERVER_TABLE_CREATE);

        db.execSQL("insert into occurrence values (1, 'terramoto');");
        db.execSQL("insert into occurrence values (2, 'tsunami');");
        db.execSQL("insert into occurrence values (3, 'incendio');");
        db.execSQL("insert into occurrence values (4, 'desabamento');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SECTION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RISK_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + OCCURRENCE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ROUTE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + COLLECTION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SERVER_TABLE_NAME);
        onCreate(db);
    }

    // TODO: to view the db
    public ArrayList<Cursor> getData(String Query) {
        // get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[]{"mesage"};
        // an array list of cursor to save two cursors one has results from the
        // query
        // other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<>(2);
        MatrixCursor Cursor2 = new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try {
            String maxQuery = Query;
            // execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            // add value to cursor2
            Cursor2.addRow(new Object[]{"Success"});

            alc.set(1, Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0, c);
                c.moveToFirst();

                return alc;
            }
            return alc;
        } catch (SQLException sqlEx) {
            Log.d("printing exception", sqlEx.getMessage());
            // if any exceptions are triggered save the error message to cursor
            // an return the arraylist
            Cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        } catch (Exception ex) {

            Log.d("printing exception", ex.getMessage());

            // if any exceptions are triggered save the error message to cursor
            // an return the arraylist
            Cursor2.addRow(new Object[]{"" + ex.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        }

    }

}
