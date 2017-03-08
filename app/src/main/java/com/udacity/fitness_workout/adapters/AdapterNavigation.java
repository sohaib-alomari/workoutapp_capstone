package com.udacity.fitness_workout.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;
import com.udacity.fitness_workout.R;
import com.udacity.fitness_workout.fragments.FragmentPrograms;
import com.udacity.fitness_workout.fragments.FragmentCategories;

/**

 *
 * AdapterNavigation is created to create tab view of workout category and program.
 * Created using CacheFragmentStatePagerAdapter.
 */
public class AdapterNavigation extends CacheFragmentStatePagerAdapter {

    // Create variables to store page titles
    private  String[] sPagerTitles;

    private Context mContext;

    // Constructor to set context and page titles data
    public AdapterNavigation(Context c, FragmentManager fm) {
        super(fm);

        mContext = c;

        // Get page titles from strings.xml
        sPagerTitles = mContext.getResources().getStringArray(R.array.home_pager_titles);
    }

    @Override
    protected Fragment createItem(int position) {
        // Initialize fragments.
        // Please be sure to pass scroll position to each fragments using setArguments.
        Fragment f;
        switch (position) {
            case 0: {
                // Set first tab with workout category
                f = new FragmentCategories();
                break;
            }
            case 1:
            default: {
                // set second tab with day program
                f = new FragmentPrograms();
                break;
            }
        }
        return f;
    }

    @Override
    public int getCount() {
        return sPagerTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return sPagerTitles[position];
    }
}