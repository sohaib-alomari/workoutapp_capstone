package com.udacity.fitness_workout.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.udacity.fitness_workout.R;
import com.udacity.fitness_workout.adapters.AdapterWorkouts;
import com.udacity.fitness_workout.data.GymProvider;
import com.udacity.fitness_workout.listeners.OnTapListener;
import com.udacity.fitness_workout.data.DBHelperPrograms;
import com.udacity.fitness_workout.data.DBHelperWorkouts;
import com.udacity.fitness_workout.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * ActivityWorkouts is created to display workouts list.
 * Created using AppCompatActivity.
 */
public class ActivityWorkouts extends AppCompatActivity implements View.OnClickListener,LoaderManager.LoaderCallbacks<Cursor>{



    private static final String[] DETAIL_COLUMNS = {
            GymProvider.GYM_TABLE_NAME + "." + GymProvider.PROGRAM_ID,
            GymProvider.PROGRAM_WORKOUT_ID,
            GymProvider.PROGRAM_NAME,
            GymProvider.PROGRAM_IMAGE,
            GymProvider.PROGRAM_TIME,
            GymProvider.PROGRAM_STEP,

    };

    public static final int COL_GYM_ID = 0;
    public static final int COL_WORKOUT_ID = 1;
    public static final int COL_NAME = 2;
    public static final int COL_IMAGE = 3;
    public static final int COL_TIME = 4;
    public static final int COL_STEPS = 5;



    //Loader ID to get data from DB
    private static final int DETAIL_LOADER = 0;

   static String nm;

    // Create object views
    private Toolbar mToolbar;
    private CircleProgressBar mPrgLoading;
    private AppCompatButton mRaisedStart;

    public static RecyclerView sList;
    public static TextView sTxtAlert;
    public static RelativeLayout sLytSubHeader;

    // Create LayoutManager object
    private RecyclerView.LayoutManager mLayoutManager;


    // Create variables to store data that passed from previous activity
    private String mProgramName;
    private String mSelectedId;
    private String mParentPage;

    // Create adapter object
    private AdapterWorkouts mAdapterWorkouts;

    // Create object of database helper class
    private DBHelperWorkouts mDbHelperWorkouts;
    private DBHelperPrograms mDbHelperPrograms;

    // Create arraylist variables to store data
    private ArrayList<String> mProgramIds     = new ArrayList<>();
    private ArrayList<String> mWorkoutIds     = new ArrayList<>();
    private ArrayList<String> mWorkoutNames   = new ArrayList<>();
    private ArrayList<String> mWorkoutImages  = new ArrayList<>();
    private ArrayList<String> mWorkoutTimes   = new ArrayList<>();
    private ArrayList<String> mWorkoutSteps   = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workouts);

            getLoaderManager().initLoader(DETAIL_LOADER, null, this);



        // Get data that passed from previous activity
        Intent i        = getIntent();
        mSelectedId     = i.getStringExtra(Utils.ARG_WORKOUT_ID);
        mProgramName    = i.getStringExtra(Utils.ARG_WORKOUT_NAME);
        mParentPage     = i.getStringExtra(Utils.ARG_PARENT_PAGE);

        // connect view objects with view ids in xml
        mToolbar        = (Toolbar) findViewById(R.id.toolbar);
        mPrgLoading     = (CircleProgressBar) findViewById(R.id.prgLoading);
        mRaisedStart    = (AppCompatButton) findViewById(R.id.raisedStart);
        sList           = (RecyclerView) findViewById(R.id.list);
        sTxtAlert       = (TextView) findViewById(R.id.txtAlert);
        sLytSubHeader       = (RelativeLayout) findViewById(R.id.lytSubHeaderLayout);

        // Set listener to the views
        mRaisedStart.setOnClickListener(this);



        // If ActivityWorkouts open via workouts tab hide header,
        // otherwise display header
        if(mParentPage.equals(Utils.ARG_WORKOUTS)){
            sLytSubHeader.setVisibility(View.GONE);
        }else{
            sLytSubHeader.setVisibility(View.VISIBLE);
        }

        // Set toolbar name with workout category name and set toolbar as actionbar
        mToolbar.setTitle(mProgramName);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configure recyclerview object
        sList.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        sList.setLayoutManager(mLayoutManager);

        // Set progress circle loading color
        mPrgLoading.setColorSchemeResources(R.color.accent_color);

        // Create object of database helpers.
        mDbHelperWorkouts = new DBHelperWorkouts(this);
        mDbHelperPrograms = new DBHelperPrograms(this);

        // Create workout and program database
        try {
            mDbHelperWorkouts.createDataBase();
            mDbHelperPrograms.createDataBase();
        }catch(IOException ioe){
            throw new Error("Unable to create database");
        }

        // Open workout and program database
        mDbHelperWorkouts.openDataBase();
        mDbHelperPrograms.openDataBase();

        // Set adapter object
        mAdapterWorkouts = new AdapterWorkouts(this, mSelectedId, mParentPage, mDbHelperPrograms);

        // Call asynctask class to load ad in background

        new AsyncGetWorkoutList().execute();

        mAdapterWorkouts.setOnTapListener(new OnTapListener() {
            @Override
            public void onTapView(String id, String Name) {
                // On workout data selected open ActivityDetail and pass selected
                // data to that activity
                Intent i = new Intent(getApplicationContext(), ActivityDetail.class);
                i.putExtra(Utils.ARG_WORKOUT_ID, id);
                i.putExtra(Utils.ARG_PARENT_PAGE, mParentPage);
                startActivity(i);

            }
        });

    }






    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.raisedStart:
                Log.d("dataa",String.valueOf(nm));
                // When raisedStart button click, open ActivityStopWatch and pass
                // all workout data in selected day program to that activity
                Intent detailIntent = new Intent(this, ActivityStopWatch.class);
                mWorkoutIds = mAdapterWorkouts.getData(0);
                mWorkoutNames = mAdapterWorkouts.getData(1);
                mWorkoutImages = mAdapterWorkouts.getData(2);
                mWorkoutTimes = mAdapterWorkouts.getData(3);
                detailIntent.putExtra(Utils.ARG_WORKOUT_IDS, mWorkoutIds);
                detailIntent.putExtra(Utils.ARG_WORKOUT_NAMES, mWorkoutNames);
                detailIntent.putExtra(Utils.ARG_WORKOUT_IMAGES, mWorkoutImages);
                detailIntent.putExtra(Utils.ARG_WORKOUT_TIMES, mWorkoutTimes);
                detailIntent.putExtra(Utils.ARG_WORKOUT_NAME, mProgramName);
                startActivity(detailIntent);

                break;
        }
    }






    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != GymProvider.CONTENT_URI ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getApplicationContext(),
                    GymProvider.CONTENT_URI,
                    DETAIL_COLUMNS,
                    "1",
                    null,
                    null
            );
        }
        return null;



    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Read weather condition ID from cursor
             nm = data.getString(COL_NAME);






        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    // Asynctask class that is used to fetch data from database in background
    private class AsyncGetWorkoutList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            // When data still retrieve from database display loading view
            // and hide other view
            super.onPreExecute();
            mPrgLoading.setVisibility(View.VISIBLE);
            sList.setVisibility(View.GONE);
            sLytSubHeader.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Get workout data from database
            getWorkoutListDataFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // TODO Auto-generated method stub
            super.onPostExecute(aVoid);

            // When finishing fetching data, close progress dialog and display data
            // to the recyclerview. If data is not available display no result text
            mPrgLoading.setVisibility(View.GONE);
            if(mWorkoutIds.isEmpty()){
                sTxtAlert.setVisibility(View.VISIBLE);
            } else {
                sTxtAlert.setVisibility(View.GONE);
                sList.setVisibility(View.VISIBLE);
                mAdapterWorkouts.updateList(mProgramIds, mWorkoutIds, mWorkoutNames,
                        mWorkoutImages, mWorkoutTimes, mWorkoutSteps);
            }

            // If data is available display header, otherwise hide it
            if(!mWorkoutIds.isEmpty() && mParentPage.equals(Utils.ARG_PROGRAMS)){
                sLytSubHeader.setVisibility(View.VISIBLE);
            } else {
                sLytSubHeader.setVisibility(View.GONE);
            }

            // Set adapter to recyclerview object
            sList.setAdapter(mAdapterWorkouts);

        }
    }

    // Method to fetch workout list from database
    private void getWorkoutListDataFromDatabase() {
        ArrayList<ArrayList<Object>> data;

        // If ActivityWorkouts open via workouts tab then get workout list from workout database
        if(mParentPage.equals(Utils.ARG_WORKOUTS)){
            data = mDbHelperWorkouts.getAllWorkoutsByCategory(mSelectedId);

            for (int i = 0; i < data.size(); i++) {
                ArrayList<Object> row = data.get(i);

                mWorkoutIds.add(row.get(0).toString());
                mWorkoutNames.add(row.get(1).toString());
                mWorkoutImages.add(row.get(2).toString());
                mWorkoutTimes.add(row.get(3).toString());
                mWorkoutSteps.add(row.get(4).toString());
            }
        // If ActivityWorkouts open via programs tab then get workout list from program database
        } else {
            data = mDbHelperPrograms.getAllWorkoutsByDay(mSelectedId);
            for (int i = 0; i < data.size(); i++) {
                ArrayList<Object> row = data.get(i);



                mProgramIds.add(row.get(0).toString());
                mWorkoutIds.add(row.get(1).toString());
                mWorkoutNames.add(row.get(2).toString());
                mWorkoutImages.add(row.get(3).toString());
                mWorkoutTimes.add(row.get(4).toString());

            }
        }

    }

    // Method to handle physical back button with transition
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}


