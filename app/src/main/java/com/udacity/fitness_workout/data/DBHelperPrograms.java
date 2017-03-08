package com.udacity.fitness_workout.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.udacity.fitness_workout.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 *
 * DBHelperPrograms is created to handle program database operation that used within application.
 * Created using SQLiteOpenHelper.
 */
public class DBHelperPrograms extends SQLiteOpenHelper {

    // Path of database when app installed on device
    private static String DB_PATH = Utils.ARG_DATABASE_PATH;

    // Create database name and version
    final static String DB_NAME = "db_programs";
    public final static int DB_VERSION = 1;
    public static SQLiteDatabase db;

    private final Context context;

    // Create table names and fields
    private final String DAY_ID             = "day_id";
    private final String TABLE_PROGRAMS     = "tbl_programs";
    private final String PROGRAM_ID         = "program_id";
    private final String PROGRAM_WORKOUT_ID = "workout_id";
    private final String PROGRAM_NAME       = "name";
    private final String PROGRAM_IMAGE      = "image";
    private final String PROGRAM_TIME       = "time";
    private final String PROGRAM_STEP       = "steps";

    private final String TABLE_DAYS         = "tbl_days";
    private final String DAY_NAME           = "day_name";

    public DBHelperPrograms(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    // Method to create database
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();
        SQLiteDatabase db_Read = null;

        // If database exist delete database and copy the new one
        if(dbExist){
            // Do nothing - database already exist
        }else{
            db_Read = this.getReadableDatabase();
            db_Read.close();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }

    }

    // Method to check database on path
    private boolean checkDataBase(){
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    // Method to copy database from app to db path
    private void copyDataBase() throws IOException{

        InputStream myInput = context.getAssets().open(DB_NAME);

        String outFileName = DB_PATH + DB_NAME;

        OutputStream myOutput = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    // Method to open database and read it
    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Method to get days from database
    public ArrayList<ArrayList<Object>> getAllDays() {
        ArrayList<ArrayList<Object>> dataArrays = new ArrayList<>();

        Cursor cursor;
        try {
            cursor = db.query(
                    TABLE_DAYS,
                    new String[]{DAY_ID, DAY_NAME},
                    null, null, null, null, null);

            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                do {
                    ArrayList<Object> dataList = new ArrayList<>();
                    long id = countWorkouts(cursor.getLong(0));

                    dataList.add(cursor.getLong(0));
                    dataList.add(cursor.getString(1));
                    dataList.add(id);

                    dataArrays.add(dataList);
                }

                while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLException e) {
            Log.e("DB getallDays", e.toString());
            e.printStackTrace();
        }

        return dataArrays;
    }

    // Method to get total workout in day program
    public int countWorkouts(long id) {
        Cursor dataCount = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PROGRAMS + " WHERE " +
                DAY_ID + " = " + id, null);
        dataCount.moveToFirst();
        int count = dataCount.getInt(0);
        dataCount.close();
        return count;
    }


    // Method to get all workouts by day from database
    public ArrayList<ArrayList<Object>> getAllWorkoutsByDay(String selectedID) {
        ArrayList<ArrayList<Object>> dataArrays = new ArrayList<>();

        Cursor cursor;
        try {
            cursor = db.query(
                    TABLE_PROGRAMS,
                    new String[]{PROGRAM_ID, PROGRAM_WORKOUT_ID, PROGRAM_NAME, PROGRAM_IMAGE, PROGRAM_TIME},
                    DAY_ID + " = " + selectedID, null, null, null, null);

            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                do {
                    ArrayList<Object> dataList = new ArrayList<>();
                    dataList.add(cursor.getLong(0));
                    dataList.add(cursor.getLong(1));
                    dataList.add(cursor.getString(2));
                    dataList.add(cursor.getString(3));
                    dataList.add(cursor.getString(4));

                    dataArrays.add(dataList);
                }

                while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLException e) {
            Log.e("DB getWorkoutListByDay", e.toString());
            e.printStackTrace();
        }


        return dataArrays;
    }

    // Method to check whether selected day contain workouts or not
    public boolean isDataAvailable(int dayID, String workoutID) {
        boolean isAvailable = false;

        Cursor cursor;

        try {
            cursor = db.query(
                    TABLE_PROGRAMS,
                    new String[]{PROGRAM_WORKOUT_ID},
                    DAY_ID + " = " + dayID + " AND " + PROGRAM_WORKOUT_ID + " = " + workoutID,
                    null, null, null, null, null);


            if (cursor.getCount() > 0) {
                isAvailable = true;
            }

            cursor.close();
        } catch (SQLException e) {
            Log.e("DBProg isDataAvailable", e.toString());

            e.printStackTrace();
        }

        return isAvailable;
    }

    // Method to add workout to selected day
    public void addData(int workoutID, String name, int dayID, String image, String time, String steps) {
        ContentValues values = new ContentValues();
        values.put(PROGRAM_WORKOUT_ID, workoutID);
        values.put(PROGRAM_NAME, name);
        values.put(DAY_ID, dayID);
        values.put(PROGRAM_IMAGE, image);
        values.put(PROGRAM_TIME, time);
        values.put(PROGRAM_STEP, steps);

        try {
            db.insert(TABLE_PROGRAMS, null, values);
        } catch (Exception e) {
            Log.e("DB addData", e.toString());
            e.printStackTrace();
        }
    }

    // Method to delete workout from selected day
    public boolean deleteWorkoutFromDay(String programID){
        // ask the database manager to delete the row of given id
        try {
            db.delete(TABLE_PROGRAMS, PROGRAM_ID + "=" + programID, null);
        }
        catch (Exception e)
        {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
