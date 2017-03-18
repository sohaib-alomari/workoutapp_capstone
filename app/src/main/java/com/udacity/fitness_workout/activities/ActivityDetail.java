package com.udacity.fitness_workout.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.OrangeGangsters.circularbarpager.library.CircularBarPager;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.mrengineer13.snackbar.SnackBar;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.udacity.fitness_workout.R;
import com.udacity.fitness_workout.adapters.AdapterPagerWorkout;
import com.udacity.fitness_workout.data.GymProvider;
import com.udacity.fitness_workout.data.DBHelperPrograms;
import com.udacity.fitness_workout.data.DBHelperWorkouts;
import com.udacity.fitness_workout.utils.Utils;
import com.udacity.fitness_workout.views.ViewSteps;
import com.udacity.fitness_workout.views.ViewWorkout;
import com.viewpagerindicator.CirclePageIndicator;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;

/**
 *Sohaib Alomari
 * ActivityDetail is created to display workout detail.
 * Created using AppCompatActivity.
 */
public class ActivityDetail extends AppCompatActivity implements
        View.OnClickListener {

    // Create view objects
    private Toolbar mToolbar;
    private FloatingActionButton mFabAdd;

    private CircleProgressBar mPrgLoading;
    private CircularBarPager mCircularBarPager;
    private TextView mTxtTitle, mTxtSubTitle;
    private LinearLayout lytTitleLayout;
    private CirclePageIndicator mCirclePageIndicator;



    // Create database helper class object
    private DBHelperPrograms mDbHelperPrograms;
    private DBHelperWorkouts mDbHelperWorkouts;

    // Create arraylist variables to store data
    private String mWorkoutId;
    private String mWorkoutName;
    private String mWorkoutImage;
    private String mWorkoutTime;
    private String mWorkoutSteps;
    private ArrayList<String> mWorkoutGalleries    = new ArrayList<>();

    // Create array variable to store days
    private String[] mDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Get days from strings.xml
        mDays               = getResources().getStringArray(R.array.day_names);

        // Get data from previous activity
        // Get data that passed from previous activity
        Intent iGet         = getIntent();
        mWorkoutId          = iGet.getStringExtra(Utils.ARG_WORKOUT_ID);

        // Connect view objects with view ids in xml
        mToolbar            = (Toolbar) findViewById(R.id.toolbar);
        mFabAdd             = (FloatingActionButton) findViewById(R.id.fabAdd);

        mPrgLoading         = (CircleProgressBar) findViewById(R.id.prgLoading);
        mCircularBarPager   = (CircularBarPager) findViewById(R.id.circularBarPager);
        mTxtTitle           = (TextView) findViewById(R.id.txtTitle);
        mTxtSubTitle        = (TextView) findViewById(R.id.txtSubTitle);
        lytTitleLayout      = (LinearLayout) findViewById(R.id.lytTitleLayout);

        // Set click listener to fab button
        mFabAdd.setOnClickListener(this);

        // Hide circle page indicator as we do not need it
        mCirclePageIndicator = mCircularBarPager.getCirclePageIndicator();
        mCirclePageIndicator.setFillColor(ContextCompat.getColor(this, R.color.accent_color));
        mCirclePageIndicator.setStrokeColor(ContextCompat.getColor(this, R.color.divider_color));



        // Set progress circle loading color
        mPrgLoading.setColorSchemeResources(R.color.accent_color);

        // Set toolbar as actionbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set toolbar background to transparent at the beginning
        mToolbar.setBackgroundColor(ScrollUtils.getColorWithAlpha(0,
                getResources().getColor(R.color.primary_color)));

        // Call asynctask class to get data from database
        new AsyncGetWorkoutDetail().execute();


        // Create object of database helpers class
        mDbHelperPrograms = new DBHelperPrograms(getApplicationContext());
        mDbHelperWorkouts = new DBHelperWorkouts(getApplicationContext());

        // Create program and workout databases
        try {
            mDbHelperPrograms.createDataBase();
            mDbHelperWorkouts.createDataBase();
        }catch(IOException ioe){
            throw new Error("Unable to create database");
        }

        // Open program and workout databases
        mDbHelperPrograms.openDataBase();
        mDbHelperWorkouts.openDataBase();
    }



    // Asynctask class to load data from database in background
    private class AsyncGetWorkoutDetail extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            // When data still retrieve from database display loading view
            // and hide other view
            super.onPreExecute();
            mPrgLoading.setVisibility(View.VISIBLE);
            mToolbar.setVisibility(View.GONE);
            mCircularBarPager.setVisibility(View.GONE);
            mFabAdd.setVisibility(View.GONE);
            lytTitleLayout.setVisibility(View.GONE);

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
            getWorkoutDetailFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // TODO Auto-generated method stub
            super.onPostExecute(aVoid);
            // Call asynctask class to get workout galleries in background
            new AsyncGetWorkoutGalleryImages().execute();


        }
    }

    // Asynctask class to load data from database in background
    private class AsyncGetWorkoutGalleryImages extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Get workout galleries from database
            getWorkoutGalleryImagesFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // TODO Auto-generated method stub
            // When finishing fetching data, close progress dialog and display data to the views
            super.onPostExecute(aVoid);

            mTxtTitle.setText(mWorkoutName);
            mTxtSubTitle.setText(mWorkoutTime);

            // Add gallery images and steps to view pager
            startViewPagerThread();


        }
    }

    // Method to add gallery images and steps to view pager in UI thread
    private void startViewPagerThread() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                View[] viewFlippers = new View[2];
                // View for gallery images
                viewFlippers[0] = new ViewWorkout(ActivityDetail.this,
                        mWorkoutGalleries);
                // View for steps
                viewFlippers[1] = new ViewSteps(ActivityDetail.this,
                        mWorkoutSteps);
                mCircularBarPager.setViewPagerAdapter(new AdapterPagerWorkout(
                        ActivityDetail.this, viewFlippers));

                mPrgLoading.setVisibility(View.GONE);
                mToolbar.setVisibility(View.VISIBLE);
                mCircularBarPager.setVisibility(View.VISIBLE);
                mFabAdd.setVisibility(View.VISIBLE);
                lytTitleLayout.setVisibility(View.VISIBLE);
            }
        });

    }

    // Method to fetch workout detail from database
    public void getWorkoutDetailFromDatabase() {

        ArrayList<Object> data;
        data = mDbHelperWorkouts.getWorkoutDetail(mWorkoutId);

        mWorkoutId     = data.get(0).toString();
        mWorkoutName   = data.get(1).toString();
        mWorkoutImage  = data.get(2).toString();
        mWorkoutTime   = data.get(3).toString();
        mWorkoutSteps  = data.get(4).toString();

    }

    // Method to get workout gallery images from database
    private void getWorkoutGalleryImagesFromDatabase(){
        ArrayList<ArrayList<Object>> data;
        data = mDbHelperWorkouts.getImages(mWorkoutId);

        if(data.size() > 0) {
            // If gallery is available then store data to variables
            for (int i = 0; i < data.size(); i++) {
                ArrayList<Object> row = data.get(i);
                mWorkoutGalleries.add(row.get(0).toString());
            }
        }else {
            mWorkoutGalleries.add(mWorkoutImage);
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabAdd:
                // When add fab click show days dialog
                showDayListDialog();
            default:
                break;
        }
    }

    // Method to display day list dialog
    public void showDayListDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.days)
                .items(R.array.day_names)
                .positiveText(R.string.add)
                .negativeText(R.string.cancel)
                .cancelable(false)
                .positiveColorRes(R.color.primary_color)
                .negativeColorRes(R.color.primary_color)
                        // When positive button clicked
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view,
                                                       int selectedIndex, CharSequence text) {
                                // Check workout whether it is already available
                                // in selected day program
                                if (mDbHelperPrograms.isDataAvailable((selectedIndex + 1),
                                        mWorkoutId)) {

                                    // If workout has already added to selected day program
                                    // inform user with snackbar
                                    showSnackbar(getString(R.string.workout_already_added) + " " +
                                            mDays[selectedIndex] + " " +
                                            getString(R.string.program) + ".");
                                } else {
                                    // If it has not added yet add it to programs database
                                    mDbHelperPrograms.addData(
                                            Integer.valueOf(mWorkoutId),
                                            mWorkoutName,
                                            (selectedIndex + 1),
                                            mWorkoutImage,
                                            mWorkoutTime,
                                            mWorkoutSteps);

                                    ContentValues values = new ContentValues();
                                    values.put(GymProvider.PROGRAM_NAME,mWorkoutName);
                                    values.put(GymProvider.PROGRAM_WORKOUT_ID,mWorkoutId);
                                    values.put(GymProvider.PROGRAM_IMAGE,mWorkoutImage);
                                    values.put(GymProvider.PROGRAM_TIME,mWorkoutTime);
                                    values.put(GymProvider.PROGRAM_STEP,mWorkoutSteps);


                                    getApplicationContext().getContentResolver().insert(GymProvider.CONTENT_URI, values);


                                    // After that inform user that workout successfully
                                    // added to database with snackbar
                                    showSnackbar(getString(R.string.workout_successfully_added) +
                                            " " + mDays[selectedIndex] + " " +
                                            getString(R.string.program) + ".");
                                }
                                return true;
                            }
                        }
                )
                        // When negative button clicked
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        // Close days dialog
                        dialog.dismiss();
                    }
                })
                .show();
    }


    // Method to show snackbar message
    public void showSnackbar(String message){
        new SnackBar.Builder(this)
                .withMessage(message)
                .show();
    }

    // Method to handle physical back button with animation
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
