package com.udacity.fitness_workout.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.udacity.fitness_workout.R;
import com.udacity.fitness_workout.activities.ActivityDetail;
import com.udacity.fitness_workout.activities.ActivityHome;
import com.udacity.fitness_workout.activities.ActivityNotes;
import com.udacity.fitness_workout.adapters.AdapterCategories;
import com.udacity.fitness_workout.listeners.OnTapListener;
import com.udacity.fitness_workout.data.DBHelperWorkouts;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;

/**
 * FragmentCategories is created to display workout category and add it to tab.
 * Created using Fragment.
 */
public class FragmentCategories extends Fragment implements View.OnClickListener {

    // Create listener object
    private OnSelectedCategoryListener mCallback;

    // Create view objects
    private RecyclerView mList;
    private CircleProgressBar mPrgLoading;
    FloatingActionButton AddNoteFab;




    // Create adapter object
    private AdapterCategories mAdapterCategories;

    // Create object of database helper class
    private DBHelperWorkouts mDbHelperWorkouts;

    // Create arraylist variable to store data from database helper object
    private ArrayList<ArrayList<Object>> data;

    // Create arraylist variables to store data
    private ArrayList<String> mCategoryIds = new ArrayList<>();
    private ArrayList<String> mCategoryNames = new ArrayList<>();
    private ArrayList<String> mCategoryImages = new ArrayList<>();
    private ArrayList<String> mTotalWorkouts = new ArrayList<>();

    @Override
    public void onClick(View v) {

            Intent i= new Intent(getActivity(),ActivityNotes.class);
            startActivity(i);


    }

    // Create interface for listener
    public interface OnSelectedCategoryListener {
        public void onSelectedCategory(String selectedID, String selectedName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_list, container, false);

        // Connect view objects with view ids in xml
        mPrgLoading     = (CircleProgressBar) v.findViewById(R.id.prgLoading);
        mList           = (RecyclerView) v.findViewById(R.id.list);

        // Configure recyclerview
        mList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mList.setHasFixedSize(false);

        AddNoteFab=(FloatingActionButton)v.findViewById(R.id.fabAddNote);
        AddNoteFab.setOnClickListener(this);

        // Set view for header
        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.padding, null);



        // Set progress circle loading color
        mPrgLoading.setColorSchemeResources(R.color.accent_color);



        // Create object of database helpers class
        mDbHelperWorkouts = new DBHelperWorkouts(getActivity());

        // Create workout database
        try {
            mDbHelperWorkouts.createDataBase();
        }catch(IOException ioe){
            throw new Error("Unable to create database");
        }

        // Open workout database
        mDbHelperWorkouts.openDataBase();

        // Call asynctask class to get data from database
        new AsyncGetWorkoutCategories().execute();

        // Set adapter object
        mAdapterCategories = new AdapterCategories(getActivity(), headerView);

        mAdapterCategories.setOnTapListener(new OnTapListener() {
            @Override
            public void onTapView(String id, String name) {
                // When item on recyclerview clicked, send selected data to the activity
                // that implement this fragment
                mCallback.onSelectedCategory(id, name);
            }
        });

        return v;
    }







    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnSelectedCategoryListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSelectedCategoryListener");
        }
    }

    // Asynctask class that is used to fetch data from database in background
    private class AsyncGetWorkoutCategories extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            // When data still retrieve from database display loading view
            // and hide other view
            mPrgLoading.setVisibility(View.VISIBLE);
            mList.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Get workout category data from database
            getWorkoutCategoryFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // TODO Auto-generated method stub
            super.onPostExecute(aVoid);
            // When finishing fetching data, close progress dialog and display data
            // to the recyclerview. If data is not available display no result text
            mPrgLoading.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);

            // Send workout categories to adapter
            mAdapterCategories.updateList(mCategoryIds, mCategoryNames,
                    mCategoryImages, mTotalWorkouts);
            // And set it to recyclerview object if data not empty
            if(mCategoryIds.size() != 0) {
                mList.setAdapter(mAdapterCategories);
            }

        }
    }

    // Method to fetch workout category data from database
    public void getWorkoutCategoryFromDatabase() {
        data = mDbHelperWorkouts.getAllCategories();

        for (int i = 0; i < data.size(); i++) {
            ArrayList<Object> row = data.get(i);

            mCategoryIds.add(row.get(0).toString());
            mCategoryNames.add(row.get(1).toString());
            mCategoryImages.add(row.get(2).toString());
            mTotalWorkouts.add(row.get(3).toString());
        }

    }

    // Close database before activity destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
        mDbHelperWorkouts.close();
    }
}
