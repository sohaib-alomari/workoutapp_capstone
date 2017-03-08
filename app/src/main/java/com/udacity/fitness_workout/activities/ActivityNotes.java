package com.udacity.fitness_workout.activities;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.fitness_workout.R;
import com.udacity.fitness_workout.data.GymProvider;

public class ActivityNotes extends AppCompatActivity implements View.OnClickListener,LoaderManager.LoaderCallbacks<Cursor> {

    TextView Notes;
    EditText NotesEdit;
    Button AddNote;
    String nm;
    String addText="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        getLoaderManager().initLoader(1, null, this);

        Notes=(TextView)findViewById(R.id.noteText);
        NotesEdit=(EditText)findViewById(R.id.EditView);
        AddNote=(Button)findViewById(R.id.Addnote);
        AddNote.setOnClickListener(this);








    }

    @Override
    public void onClick(View v) {
        addText= NotesEdit.getText().toString();

        getApplicationContext().getContentResolver().delete(GymProvider.CONTENT_URI,null,null);

        ContentValues values = new ContentValues();
        values.put(GymProvider.Notes,addText);
        getApplicationContext().getContentResolver().insert(GymProvider.CONTENT_URI, values);




    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        if ( null != GymProvider.CONTENT_URI ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getApplicationContext(),
                    GymProvider.CONTENT_URI,
                    null,
                    null,
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
            nm = data.getString(6);
            Notes.setText(nm);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {


    }
}
