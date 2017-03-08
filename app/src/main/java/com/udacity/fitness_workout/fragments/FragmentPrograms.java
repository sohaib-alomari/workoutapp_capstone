package com.udacity.fitness_workout.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.udacity.fitness_workout.R;
import com.udacity.fitness_workout.adapters.AdapterPrograms;
import com.udacity.fitness_workout.listeners.OnTapListener;
import com.udacity.fitness_workout.data.DBHelperPrograms;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * FragmentPrograms is created to display days program and add it to tab.
 * Created using Fragment.
 */
public class FragmentPrograms extends Fragment {

    // Create listener object
    private OnSelectedDayListener mCallback;

    // Create view objects
    private RecyclerView mList;
    private CircleProgressBar mPrgLoading;




    // Create adapter object
    private AdapterPrograms mAdapterPrograms;

    // Create object of database helper class
    private DBHelperPrograms mDbHelperPrograms;

    // Create arraylist variable to store data from database helper object
    private ArrayList<ArrayList<Object>> data;

    // Create arraylist variables to store data
    private ArrayList<String> mDayIds = new ArrayList<>();
    private ArrayList<String> mDayNames = new ArrayList<>();
    private ArrayList<String> mTotalPrograms = new ArrayList<>();

    // Create variable to set first app launch status
    private boolean mIsFirstAppRun = true;

    // Create interface for listener
    public interface OnSelectedDayListener{
        public void onSelectedDay(String selectedID, String selectedName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_list, container, false);

        // Connect view objects with view ids in xml
        mPrgLoading     = (CircleProgressBar) v.findViewById(R.id.prgLoading);
        mList           = (RecyclerView) v.findViewById(R.id.list);

        // Configure recyclerview
        mList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mList.setHasFixedSize(false);

        // Set view for header
        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.padding, null);



        // Hide progress circle first
        mPrgLoading.setVisibility(View.GONE);

        // Set progress circle loading color
        mPrgLoading.setColorSchemeResources(R.color.accent_color);



        // Create object of database helpers class
        mDbHelperPrograms = new DBHelperPrograms(getActivity());

        // Create program database
        try {
            mDbHelperPrograms.createDataBase();
        }catch(IOException ioe){
            throw new Error("Unable to create database");
        }

        // Open program database
        mDbHelperPrograms.openDataBase();

        // Set adapter object
        mAdapterPrograms = new AdapterPrograms(getActivity(), headerView);

        // Call asynctask class to get data from database
        new AsyncGetDays().execute();

        mAdapterPrograms.setOnTapListener(new OnTapListener() {

            @Override
            public void onTapView(String id, String name) {

                // When item on recyclerview clicked, send selected data to the activity
                // that implement this fragment
                mCallback.onSelectedDay(id, name);
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
            mCallback = (OnSelectedDayListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement onSelectedDayListener");
        }
    }

    // Asynctask class that is used to fetch data from database in background
    private class AsyncGetDays extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            // Clear data before storing data
            clearData();
            // When data still retrieve from database display loading view
            // and hide other view
            mPrgLoading.setVisibility(View.VISIBLE);
            mList.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            // Get days data from database
            getDaysFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // TODO Auto-generated method stub
            super.onPostExecute(aVoid);
            // when finishing fetching data, close progress dialog and show data on listview
            // if available, otherwise show toast message
            super.onPostExecute(aVoid);
            mPrgLoading.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);
            // Send days to adapter
            mAdapterPrograms.updateList(mDayIds, mDayNames, mTotalPrograms);
            mAdapterPrograms.notifyDataSetChanged();
            mIsFirstAppRun = false;

            // And set it to recyclerview object if data not empty
            if(mDayIds.size() != 0){
                mList.setAdapter(mAdapterPrograms);
            }
        }
    }

    // Method to fetch days data from database
    public void getDaysFromDatabase(){
        data = mDbHelperPrograms.getAllDays();

        for(int i=0;i< data.size();i++){
            ArrayList<Object> row = data.get(i);

            mDayIds.add(row.get(0).toString());
            mDayNames.add(row.get(1).toString());
            mTotalPrograms.add(row.get(2).toString());
        }
    }

    // Method to clear arraylist variables
    private void clearData(){
        mDayIds.clear();
        mDayNames.clear();
        mTotalPrograms.clear();
    }

    // Close database before activity destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
        mDbHelperPrograms.close();
    }


    @Override
    public void onResume() {
        super.onResume();
        // As total workout data change dynamically, call Asynctask class to
        // load data from database via onResume().
        if(!mIsFirstAppRun) {
            clearData();
            getDaysFromDatabase();
            mAdapterPrograms.updateList(mDayIds, mDayNames, mTotalPrograms);
            mAdapterPrograms.notifyDataSetChanged();
        }
    }

}
