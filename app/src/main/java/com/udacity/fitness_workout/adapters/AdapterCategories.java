package com.udacity.fitness_workout.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.udacity.fitness_workout.R;
import com.udacity.fitness_workout.listeners.OnTapListener;
import com.udacity.fitness_workout.utils.ImageLoader;

import java.util.ArrayList;

/**

 *
 * AdapterCategories is created to display workout item under workout category tab.
 * Created using RecyclerView.Adapter.
 */
public class AdapterCategories extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    // Create variables to store data
    private final ArrayList<String> mCategoryIds;
    private final ArrayList<String> mCategoryNames;
    private final ArrayList<String> mCategoryImages;
    private final ArrayList<String> mTotalWorkouts;

    // Create listener object
    private OnTapListener onTapListener;

    // Create Context and ImageLoader objects
    private Context mContext;
    private ImageLoader mImageLoader;

    // Create view and LayoutInflater objects
    private View mHeaderView;
    private LayoutInflater mInflater;

    // Constructor to set classes objects
    public AdapterCategories(Context context, View headerView)
    {
        this.mCategoryIds = new ArrayList<>();
        this.mCategoryNames = new ArrayList<>();
        this.mCategoryImages = new ArrayList<>();
        this.mTotalWorkouts = new ArrayList<>();

        mContext = context;

        mHeaderView = headerView;

        mInflater = LayoutInflater.from(context);

        // Get image width and height sizes from dimens.xml
        int mImageWidth = mContext.getResources().getDimensionPixelSize(R.dimen.thumb_width);
        int mImageHeight = mContext.getResources().getDimensionPixelSize(R.dimen.thumb_height);

        mImageLoader = new ImageLoader(mContext, mImageWidth, mImageHeight);

    }

    @Override
    public int getItemCount() {
        // If header has been set, add +1 to arraylist variables size
        if (mHeaderView == null) {
            return mCategoryIds.size();
        } else {
            return mCategoryIds.size() + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(mHeaderView);
        } else {
            return new ItemViewHolder(mInflater.inflate(R.layout.adapter_list, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position)
    {
        if(viewHolder instanceof ItemViewHolder){
            ((ItemViewHolder) viewHolder).mItemContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onTapListener != null)
                        onTapListener.onTapView(mCategoryIds.get(position - 1), mCategoryNames.get(position - 1));
                }
            });

            ((ItemViewHolder) viewHolder).mTxtWorkoutName.setText(mCategoryNames.get(position - 1));

            // Get total workout
            int count = Integer.parseInt(mTotalWorkouts.get(position - 1));

            // If total workout is more than one then add 's'
            if(count > 1){
                ((ItemViewHolder) viewHolder).mTxtWorkoutNumber.setText(count+" "+mContext.getResources().getString(R.string.workouts));
            }else{
                ((ItemViewHolder) viewHolder).mTxtWorkoutNumber.setText(count+" "+mContext.getResources().getString(R.string.workout));
            }

            // Set image to ImageView
            int image = mContext.getResources().getIdentifier(mCategoryImages.get(position - 1), "drawable", mContext.getPackageName());

            // Load image lazily
            mImageLoader.loadBitmap(image, ((ItemViewHolder) viewHolder).mImgCategoryImage);
        }


    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View view) {
            super(view);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        // Create view objects
        private ImageView mImgCategoryImage;
        private TextView mTxtWorkoutName, mTxtWorkoutNumber;
        private RelativeLayout mItemContainer;

        public ItemViewHolder(View view) {
            super(view);
            // Connect view objects with view ids in xml
            mImgCategoryImage   = (ImageView) view.findViewById(R.id.imgThumbnail);
            mTxtWorkoutName     = (TextView) view.findViewById(R.id.txtPrimaryText);
            mTxtWorkoutNumber   = (TextView) view.findViewById(R.id.txtSecondaryText);
            mItemContainer      = (RelativeLayout) view.findViewById(R.id.item_container);
        }
    }

    // Method to set data to recyclerview item
    public void updateList(
            ArrayList<String> categoryIds,
            ArrayList<String> categoryNames,
            ArrayList<String> categoryImages,
            ArrayList<String> totalWorkouts) {

        this.mCategoryIds.clear();
        this.mCategoryIds.addAll(categoryIds);

        this.mCategoryNames.clear();
        this.mCategoryNames.addAll(categoryNames);

        this.mCategoryImages.clear();
        this.mCategoryImages.addAll(categoryImages);

        this.mTotalWorkouts.clear();
        this.mTotalWorkouts.addAll(totalWorkouts);

        this.notifyDataSetChanged();
    }

    // Method to set listener to handle item click
    public void setOnTapListener(OnTapListener onTapListener)
    {
        this.onTapListener = onTapListener;
    }


}