package layout;

import android.app.LoaderManager;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.widget.RemoteViews;

import com.udacity.fitness_workout.R;
import com.udacity.fitness_workout.data.DBHelperPrograms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * Implementation of App Widget functionality.
 */
public class Widget extends AppWidgetProvider implements LoaderManager.LoaderCallbacks<Cursor> {

    public static String dayOfTheWeek;
    public static String AllWorkouts = " ";
    private static final int DETAIL_LOADER = 0;
    public static DBHelperPrograms mDbHelperPrograms;
    private static ArrayList<String> mWorkoutNames = new ArrayList<>();
    public static Context contextOfApplication;
    int dayOfWeek;


    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {


        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

        //Intent intent = new Intent(context, ActivitySplash.class);
        // PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        //views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
        // appWidgetManager.updateAppWidget(appWidgetId, views);
        //RemoteViews viewss = new RemoteViews(context.getPackageName(), R.layout.widget);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        dayOfTheWeek = sdf.format(d);

        Calendar c = Calendar.getInstance();
        dayOfWeek = c.get(Calendar.DAY_OF_WEEK);


        views.setTextViewText(R.id.appwidget_text, dayOfTheWeek);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        try {

                mDbHelperPrograms = new DBHelperPrograms(context.getApplicationContext());
                mDbHelperPrograms.openDataBase();
                ArrayList<ArrayList<Object>> data;
                AllWorkouts=" ";
                mWorkoutNames.clear();
                data = mDbHelperPrograms.getAllWorkoutsByDay(String.valueOf(dayOfWeek));
                for (int i = 0; i < data.size(); i++) {
                    ArrayList<Object> row = data.get(i);

                    mWorkoutNames.add(row.get(2).toString());

                    AllWorkouts= AllWorkouts+mWorkoutNames.get(i)+"\n";

                    views.setTextViewText(R.id.Today,AllWorkouts);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }



        } catch (Exception e) {

            views.setTextViewText(R.id.Today,Widget.contextOfApplication.getString(R.string.no_workouts));
            appWidgetManager.updateAppWidget(appWidgetId, views);


        }


    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        for (int appWidgetId : appWidgetIds) {


            try {
                mDbHelperPrograms = new DBHelperPrograms(context.getApplicationContext());
                mDbHelperPrograms.openDataBase();
                ArrayList<ArrayList<Object>> data;
                AllWorkouts=" ";
                mWorkoutNames.clear();
                data = mDbHelperPrograms.getAllWorkoutsByDay(String.valueOf(dayOfWeek));
                for (int i = 0; i < data.size(); i++) {
                    ArrayList<Object> row = data.get(i);

                    mWorkoutNames.add(row.get(2).toString());

                    AllWorkouts= AllWorkouts+mWorkoutNames.get(i)+"\n";

                    views.setTextViewText(R.id.Today,AllWorkouts);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }



            } catch (Exception e) {
                views.setTextViewText(R.id.Today,Widget.contextOfApplication.getString(R.string.no_workouts));
                appWidgetManager.updateAppWidget(appWidgetId, views);


            }


            updateAppWidget(context, appWidgetManager, appWidgetId);


        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    public static String getDay(String day) {
        if (day.equals("Sunday"))
            day = "1";
        if (day.equals("Monday"))
            day = "2";
        if (day.equals("Tuesday"))
            day = "3";
        if (day.equals("Wednsday"))
            day = "4";
        if (day.equals("Thursday"))
            day = "5";
        if (day.equals("Friday"))
            day = "6";
        if (day.equals("Saturday"))
            day = "7";


        return day;
    }


    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }
}

