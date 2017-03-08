package com.udacity.fitness_workout.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mrengineer13.snackbar.SnackBar;
import com.udacity.fitness_workout.R;
import com.udacity.fitness_workout.activities.ActivityWorkouts;
import com.udacity.fitness_workout.listeners.OnTapListener;
import com.udacity.fitness_workout.data.DBHelperPrograms;
import com.udacity.fitness_workout.utils.ImageLoader;
import com.udacity.fitness_workout.utils.Utils;

import java.util.ArrayList;

/**
 *
 * AdapterWorkouts is created to display workout item in ActivityWorkouts.
 * Created using RecyclerView.Adapter.
 */
public class AdapterWorkouts extends RecyclerView.Adapter<AdapterWorkouts.ViewHolder>
{
    // Create arraylist variables to store data
    private final ArrayList<String> mProgramIds;
    private final ArrayList<String> mWorkoutIds;
    private final ArrayList<String> mWorkoutNames;
    private final ArrayList<String> mWorkoutImages;
    private final ArrayList<String> mWorkoutTimes;
    private final ArrayList<String> mWorkoutSteps;

    // Create listener object
    private OnTapListener onTapListener;

    private Activity mActivity;

    // Create object of database helper class
    private DBHelperPrograms mDbHelperPrograms;
    // Create ImageLoader class object
    private ImageLoader mImageLoader;

    // Create variables to store day data and dimension
    private String[] mDays;
    private String mSelectedDay;
    private static String sParentPage;
    private int mImageWidth, mImageHeight;

    // Constructor to set data and class objects
    public AdapterWorkouts(Activity activity, String selectedDay, String parentPage,
                           DBHelperPrograms dbHelperPrograms) {

        this.mProgramIds    = new ArrayList<>();
        this.mWorkoutIds    = new ArrayList<>();
        this.mWorkoutNames  = new ArrayList<>();
        this.mWorkoutImages = new ArrayList<>();
        this.mWorkoutTimes  = new ArrayList<>();
        this.mWorkoutSteps  = new ArrayList<>();

        mActivity = activity;

        // Get current activity
        sParentPage = parentPage;

        // Set database object
        mDbHelperPrograms = dbHelperPrograms;
        mSelectedDay = selectedDay;

        // Get day list from strings.xml
        mDays = mActivity.getResources().getStringArray(R.array.day_names);

        // Get image width and height sizes from dimens.xml
        mImageWidth = mActivity.getResources().getDimensionPixelSize(R.dimen.thumb_width);
        mImageHeight = mActivity.getResources().getDimensionPixelSize(R.dimen.thumb_height);

        // Set image loader object
        mImageLoader = new ImageLoader(mActivity, mImageWidth, mImageHeight);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_list, null);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position)
    {
        viewHolder.mItemContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (onTapListener != null)
                    onTapListener.onTapView(mWorkoutIds.get(position),mWorkoutNames.get(position));
            }
        });

        viewHolder.mBtnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If AdapterWorkouts generated from workout category tab, show showDayListDialog
                // when action button clicked.
                if(sParentPage.equals(Utils.ARG_WORKOUTS)){
                    showDayListDialog(position);
                // Else if AdapterWorkouts generated from day program tab, showAlertDialog
                // when action button clicked.
                }else if(sParentPage.equals(Utils.ARG_PROGRAMS)){
                    showAlertDialog(position, mSelectedDay);
                }
            }
        });

        // set data to text view
        viewHolder.mTxtTitle.setText(mWorkoutNames.get(position));
        viewHolder.mTxtTime.setText(mWorkoutTimes.get(position));

        // set image to image view
        int image = mActivity.getResources().getIdentifier(mWorkoutImages.get(position),
                "drawable", mActivity.getPackageName());

        // Load image lazily
        mImageLoader.loadBitmap(image, viewHolder.mImgThumbnail);
    }

    // Method to create alert dialog
    public void showAlertDialog(int i, String selectedDay){
        final int position = i;
        final String day = mDays[(Integer.valueOf(selectedDay) - 1)];
        String confirmMessage = mActivity.getString(R.string.confirm_message)+" "+
                day+" "+mActivity.getString(R.string.program) + "?";
        new MaterialDialog.Builder(mActivity)
                .title(R.string.confirm)
                .content(confirmMessage)
                .positiveText(R.string.remove)
                .negativeText(R.string.cancel)
                .cancelable(false)
                .positiveColorRes(R.color.primary_color)
                .negativeColorRes(R.color.primary_color)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        boolean result = mDbHelperPrograms.deleteWorkoutFromDay(mProgramIds.get(position));
                        if (result) {
                            // Remove data from recyclerview item
                            removeAt(position);

                            // If data is empty updateview. hide header and recycler view
                            // and display alert text
                            if (mProgramIds.size() == 0) {
                                updateViews();
                            }
                            notifyDataSetChanged();

                            // And inform user that workout has successfully remove using snackbar
                            new SnackBar.Builder(mActivity)
                                    .withMessage(mActivity.getString(R.string.success_remove) + " " +
                                            day + " " + mActivity.getString(R.string.program))
                                    .show();
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    // Method to display day list dialog
    public void showDayListDialog(int i) {
        final int position = i;
        new MaterialDialog.Builder(mActivity)
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
                            public boolean onSelection(MaterialDialog dialog, View view, int selectedIndex, CharSequence text) {
                                // Check workout whether it is already available
                                // in selected day program
                                if (mDbHelperPrograms.isDataAvailable((selectedIndex + 1),
                                        mWorkoutIds.get(position))) {

                                    // If workout has already added to selected day program
                                    // inform user with snackbar
                                    showSnackbar(
                                            mActivity.getString(R.string.workout_already_added) +
                                                    " " + mDays[selectedIndex] + " " +
                                                    mActivity.getString(R.string.program) + ".");
                                } else {
                                    // If it has not added yet add it to programs database
                                    mDbHelperPrograms.addData(
                                            Integer.valueOf(mWorkoutIds.get(position)),
                                            mWorkoutNames.get(position),
                                            (selectedIndex + 1),
                                            mWorkoutImages.get(position),
                                            mWorkoutTimes.get(position),
                                            mWorkoutSteps.get(position));
                                    showSnackbar(
                                            mActivity.getString(R.string.workout_successfully_added)
                                                    + " " + mDays[selectedIndex] + " " +
                                                    mActivity.getString(R.string.program) + ".");
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
        new SnackBar.Builder(mActivity)
                .withMessage(message)
                .show();
    }

    @Override
    public int getItemCount()
    {
        return mWorkoutIds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Create view objects
        private ImageView mImgThumbnail;
        private TextView mTxtTitle, mTxtTime;
        private RelativeLayout mBtnAction;
        private RelativeLayout mItemContainer;

        public ViewHolder(View v)
        {
            super(v);
            // connect views object with views id in xml
            mImgThumbnail= (ImageView) v.findViewById(R.id.imgThumbnail);
            mTxtTitle    = (TextView) v.findViewById(R.id.txtPrimaryText);
            mTxtTime     = (TextView) v.findViewById(R.id.txtSecondaryText);
            mBtnAction   = (RelativeLayout) v.findViewById(R.id.btnAction);
            mItemContainer   = (RelativeLayout) v.findViewById(R.id.item_container);
            mBtnAction.setVisibility(View.VISIBLE);
        }
    }

    // Method to set data to recyclerview item
    public void updateList(
            ArrayList<String> mProgramIds,
            ArrayList<String> mWorkoutIds,
            ArrayList<String> workoutNames,
            ArrayList<String> workoutImages,
            ArrayList<String> workoutTimes,
            ArrayList<String> workoutSteps)
    {
        this.mProgramIds.clear();
        this.mProgramIds.addAll(mProgramIds);

        this.mWorkoutIds.clear();
        this.mWorkoutIds.addAll(mWorkoutIds);

        this.mWorkoutNames.clear();
        this.mWorkoutNames.addAll(workoutNames);

        this.mWorkoutTimes.clear();
        this.mWorkoutTimes.addAll(workoutTimes);

        this.mWorkoutImages.clear();
        this.mWorkoutImages.addAll(workoutImages);

        this.mWorkoutSteps.clear();
        this.mWorkoutSteps.addAll(workoutSteps);

        this.notifyDataSetChanged();
    }

    // Method to remove data from arraylist variables
    public void removeAt(int position) {

        this.mProgramIds.remove(position);

        this.mWorkoutIds.remove(position);

        this.mWorkoutNames.remove(position);

        this.mWorkoutTimes.remove(position);

        this.mWorkoutImages.remove(position);
    }

    // Method to get data of arraylist variables
    public ArrayList<String> getData(int i){
        switch(i){
            case 0:
                return mWorkoutIds;
            case 1:
                return mWorkoutNames;
            case 2:
                return mWorkoutImages;
            case 3:
                return mWorkoutTimes;
        }
        return null;
    }

    // Method to update views
    public void updateViews(){
        ActivityWorkouts.sList.setVisibility(View.GONE);
        ActivityWorkouts.sLytSubHeader.setVisibility(View.GONE);
        ActivityWorkouts.sTxtAlert.setVisibility(View.VISIBLE);
    }

    // Method to set listener to handle item click
    public void setOnTapListener(OnTapListener onTapListener)
    {
        this.onTapListener = onTapListener;
    }
}