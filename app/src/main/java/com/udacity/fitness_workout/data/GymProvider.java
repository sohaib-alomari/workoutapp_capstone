package com.udacity.fitness_workout.data;


        import android.content.ContentProvider;
        import android.content.ContentUris;
        import android.content.ContentValues;
        import android.content.Context;
        import android.content.UriMatcher;
        import android.database.Cursor;
        import android.database.SQLException;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.database.sqlite.SQLiteQueryBuilder;
        import android.net.Uri;
        import android.text.TextUtils;
        import android.util.Log;

        import java.util.HashMap;


public class GymProvider extends ContentProvider {

    static final String PROVIDER_NAME = "com.udacity.fitness_workout.Gym_Workout";
    static final String URL = "content://" + PROVIDER_NAME + "/gym";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    private static HashMap<String, String> GYM_PROJECTION_MAP;

    // Create table names and fields
    private final String DAY_ID             = "day_id";
    private final String TABLE_PROGRAMS     = "tbl_programs";
    public static  String PROGRAM_ID         = "program_id";
    public static String PROGRAM_WORKOUT_ID = "workout_id";
    public static  String PROGRAM_NAME       = "name";
   public static String PROGRAM_IMAGE      = "image";
    public static String PROGRAM_TIME       = "time";
   public static String PROGRAM_STEP       = "steps";
    public static String Notes             = "notes";
    private final String TABLE_DAYS         = "tbl_days";
    private final String DAY_NAME           = "day_name";


    private static HashMap<String, String> MOVIES_PROJECTION_MAP;

    static final int Gym_Workout = 1;
    static final int Gym_Workout_Program_ID = 2;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "gym", Gym_Workout);
        uriMatcher.addURI(PROVIDER_NAME, "gym/#", Gym_Workout_Program_ID);
    }

    /**
     * Database specific constant declarations
     */
    private SQLiteDatabase db;
    static final String DATABASE_NAME = "Gym_WorkoutPrograms";
    public static  String GYM_TABLE_NAME = "TABLE_PROGRAMS";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + GYM_TABLE_NAME +
                    " (program_id, " +
                    " workout_id, " +
                    " name, " +
                    " image, " +
                    " time, " +"steps,"+
                    " notes);";

    /**
     * Helper class that actually creates and manages
     * the provider's underlying data repository.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  GYM_TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /**
         * Add a new record
         */
        long rowID = db.insert(GYM_TABLE_NAME, "", values);

        /**
         * If record is added successfully
         */

        if (rowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(GYM_TABLE_NAME);

        switch (uriMatcher.match(uri)) {


            case Gym_Workout:
                qb.setProjectionMap(GYM_PROJECTION_MAP);
                break;
      
            case Gym_Workout_Program_ID:
                qb.appendWhere( PROGRAM_ID + "=" + uri.getPathSegments().get(2));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (sortOrder == null || sortOrder == ""){
            /**
                       */
            sortOrder = PROGRAM_ID;
        }
        Cursor c = qb.query(db, projection,selection, selectionArgs,null, null, sortOrder);

        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case Gym_Workout:
                count = db.delete(GYM_TABLE_NAME, selection, selectionArgs);
                break;

            case Gym_Workout_Program_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( GYM_TABLE_NAME, PROGRAM_ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case Gym_Workout:
                count = db.update(GYM_TABLE_NAME, values, selection, selectionArgs);
                break;

            case Gym_Workout_Program_ID:
                count = db.update(GYM_TABLE_NAME, values, PROGRAM_ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        Log.v("DATABASE","IN UPDATE");

        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            /**
             * Get all GYM WORUKOUTS
             */
            case Gym_Workout:
                return "vnd.android.cursor.dir/vnd.example.gym";

            /**
             * Get a particular student
             */
            case Gym_Workout_Program_ID:
                return "vnd.android.cursor.item/vnd.example.gym";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
    public void clearTable()
    {
        db.execSQL("DELETE FROM Movies");
    }





}