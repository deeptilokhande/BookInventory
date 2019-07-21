package com.example.prash.bookinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prash.bookinventory.data.BookContract;
import com.example.prash.bookinventory.data.BookContract.BookEntry;
import com.example.prash.bookinventory.data.BookDbHelper;

import java.util.List;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BOOK_LOADER = 0;

    BookCursorAdapter mCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the book data
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        ListView bookListView = (ListView) findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);


        // Setup an Adapter to create a list item for each row of book data in the Cursor.
        mCursorAdapter = new BookCursorAdapter(this, null);

        // Attach the adapter to the ListView.
        bookListView.setAdapter(mCursorAdapter);

        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                Uri currentUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

                intent.setData(currentUri);
                startActivity(intent);
            }
        });


        //KickOff the Loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    //This shows the menu options on main screen
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;

    }

    //This adds dummy data to the table and displays it.
    void addDummyData() {

        ContentValues dummyData = new ContentValues();
        dummyData.put(BookEntry.COLUMN_BOOK_NAME, "Vajra");
        dummyData.put(BookEntry.COLUMN_BOOK_PRICE, 78);
        dummyData.put(BookEntry.COLUMN_BOOK_QUANTITY, 5);
        dummyData.put(BookEntry.COLUMN_SUPPLIER_NAME, "Mehta Publishers");
        dummyData.put(BookEntry.COLUMN_SUPPLIER_PHONE, "43423");

        Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, dummyData);
        if (newUri != null) {

            Toast.makeText(this, getString(R.string.successful_insert) + "for dummy data", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.unsuccessful_insert) + "for dummy data", Toast.LENGTH_SHORT).show();
        }

    }

    void deleteAllEntries() {
        //SQLiteDatabase db = mbookDbHelper.getWritableDatabase();
        // db.delete(BookEntry.TABLE_NAME, null, null);


        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg_all);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog

                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }

    void deleteBook() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
        if (rowsDeleted == 0) {
            Toast.makeText(this, "Delete unsuccessful!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Delete successful!", Toast.LENGTH_SHORT).show();
        }
        Log.v("CatalogActivity", "all rows deleted");
    }


    //Function to define actions to take when an menu option is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                addDummyData();
                return true;

            case R.id.action_delete_all_entries:
                deleteAllEntries();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define which columns from database are used in a projection
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY
        };


        //This loader will execute content provider's  query method on a background thread
        return new CursorLoader(this,
                BookEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Update with new Cursor containing updated book data
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }
}
