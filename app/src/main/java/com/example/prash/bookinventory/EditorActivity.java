package com.example.prash.bookinventory;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prash.bookinventory.data.BookContract;
import com.example.prash.bookinventory.data.BookContract.BookEntry;
import com.example.prash.bookinventory.data.BookDbHelper;

import java.util.List;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;

    private BookDbHelper mDbHelper;
    //Define EditTexts that will accept the user input
    private EditText mEditBookName;
    private EditText mEditBookPrice;
    private EditText mEditBookQuantity;
    private EditText mEditSupplierName;
    private EditText mEditSupplierPhone;
    private Uri mCurrentBookUri;

    private boolean mBookHasChanged = false;

    //OnTouch Listener that listens to any changes in the view and if changed, sets mBookHasChanged to true
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editor_book);

        //Get the intent to find out if its for editing or adding new book
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        //If uri is null, its for adding new book , else its for editing existing book.
        if (mCurrentBookUri == null) {
            setTitle(getString(R.string.editor_activity_title_add_book));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a book that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_book));
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        //Link the EditText with corresponding xml ids.
        mEditBookName = (EditText) findViewById(R.id.edit_book_name);
        mEditBookPrice = (EditText) findViewById(R.id.edit_book_price);
        mEditBookQuantity = (EditText) findViewById(R.id.edit_book_quantity);
        mEditSupplierName = (EditText) findViewById(R.id.edit_supplier_name);
        mEditSupplierPhone = (EditText) findViewById(R.id.edit_supplier_phone);
        //Increase and Decrease quantity of books buttons
        Button decreaseQuantity = (Button) findViewById(R.id.decrease_quantity);
        Button increaseQuantity = (Button) findViewById(R.id.increase_quantity);
        Button orderBooks = (Button) findViewById(R.id.order_button);

        //SetUp OnTouch Listener on all fields of layout
        mEditBookName.setOnTouchListener(mTouchListener);
        mEditBookPrice.setOnTouchListener(mTouchListener);
        mEditBookQuantity.setOnTouchListener(mTouchListener);
        mEditSupplierName.setOnTouchListener(mTouchListener);
        mEditSupplierPhone.setOnTouchListener(mTouchListener);
        decreaseQuantity.setOnTouchListener(mTouchListener);
        increaseQuantity.setOnTouchListener(mTouchListener);

        //HookUp ClickListeners for buttons to increase and decrease of quantity
        decreaseQuantity.setOnClickListener(mDecreaseQtyListener);
        increaseQuantity.setOnClickListener(mIncreaseQtyListener);
        orderBooks.setOnClickListener(mOrderBooksListener);


        mDbHelper = new BookDbHelper(this);


    }

    //When Order books button is clicked, open implicit intent to go to dial phone of Supplier
    private View.OnClickListener mOrderBooksListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
            String phoneNumber = mEditSupplierPhone.getText().toString();
            phoneIntent.setData(Uri.parse("tel:" + phoneNumber));
            PackageManager packageManager = view.getContext().getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(phoneIntent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            boolean isIntentSafe = activities.size() > 0;
            if (isIntentSafe) {
                getApplicationContext().startActivity(phoneIntent);
            }
        }
    };


    //To decrease quantity of books when - button is clicked
    private View.OnClickListener mDecreaseQtyListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String editbkqty = mEditBookQuantity.getText().toString();
            if (TextUtils.isEmpty(editbkqty)) {
                Toast.makeText(EditorActivity.this, R.string.null_value_decrement, Toast.LENGTH_SHORT).show();
            } else {
                int quantity = Integer.parseInt(mEditBookQuantity.getText().toString());
                String stringValue;
                if (quantity > 0) {
                    quantity -= 1;
                    stringValue = String.valueOf(quantity);
                    mEditBookQuantity.setText(stringValue);
                } else {
                    Toast.makeText(EditorActivity.this, R.string.neg_quantity_error_msg, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };


    //To increase quantity of books when + button is clicked
    private View.OnClickListener mIncreaseQtyListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String editbkqty = mEditBookQuantity.getText().toString();
            if (TextUtils.isEmpty(editbkqty)) {
                Toast.makeText(EditorActivity.this, R.string.null_value_increase, Toast.LENGTH_SHORT).show();
            } else {
                int quantity = Integer.parseInt(mEditBookQuantity.getText().toString());
                quantity += 1;
                String stringValue = String.valueOf(quantity);
                mEditBookQuantity.setText(stringValue);
            }

        }
    };


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    //Display the menu options on Editor screen
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edior_book_menu, menu);
        return true;
    }

    //Function to define actions to take when an menu option is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveABook();
                return true;

            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity) if any book details are not changed
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                //Otherwise show a dialog telling user about unsaved changes

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    private int validateItemstosave() {
        String bookName = mEditBookName.getText().toString().trim();
        String supplierName = mEditSupplierName.getText().toString().trim();
        String supplierPhone = mEditSupplierPhone.getText().toString();
        String bookPriceStr = mEditBookPrice.getText().toString().trim();
        String bookQtyStr = mEditBookQuantity.getText().toString().trim();
        int emptyfield = 1;


        return emptyfield;

    }


    //Add book details to database table from user input
    private void saveABook() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String bookName = mEditBookName.getText().toString().trim();
        int bookPrice;
        int bookQuantity;
        String supplierName = mEditSupplierName.getText().toString().trim();
        String supplierPhone = mEditSupplierPhone.getText().toString();
        String bookPriceStr = mEditBookPrice.getText().toString().trim();
        String bookQtyStr = mEditBookQuantity.getText().toString().trim();

        //Check for empty add , and avoid crash of app because of empty save
        if (mCurrentBookUri == null && TextUtils.isEmpty(bookName) && TextUtils.isEmpty(bookQtyStr) &&
                TextUtils.isEmpty(bookPriceStr) && TextUtils.isEmpty(supplierName) && TextUtils.isEmpty(supplierPhone)) {
            //return as no new book details are added
            Toast.makeText(EditorActivity.this, R.string.need_data_to_save, Toast.LENGTH_SHORT).show();
            return;

        }
        else if (TextUtils.isEmpty(bookName)) {

            mEditBookName.setError("Need book name!");

        }
        else if (TextUtils.isEmpty(bookPriceStr)) {

            mEditBookPrice.setError("Need book price!");

        }
        else if (TextUtils.isEmpty(bookQtyStr)) {

            mEditBookQuantity.setError("Need Book Qty!");

        }
        else if (TextUtils.isEmpty(supplierName)) {

            mEditSupplierName.setError("Need Supplier name!");

        }
        else if (TextUtils.isEmpty(supplierPhone)) {

            mEditSupplierPhone.setError("Need Supplier phone!");

        }//If all fields are filled in save the book.
        else {
            ContentValues bookToAdd = new ContentValues();
            // If the weight is not provided by the user, don't try to parse the string into an
            // integer value. Use 0 by default.
            bookPrice = 0;
            bookQuantity = 0;
            if (!TextUtils.isEmpty(bookPriceStr)) {
                bookPrice = Integer.parseInt(bookPriceStr);
            }
            if (!TextUtils.isEmpty(bookQtyStr)) {
                bookQuantity = Integer.parseInt(bookQtyStr);
            }
            bookToAdd.put(BookEntry.COLUMN_BOOK_NAME, bookName);
            bookToAdd.put(BookEntry.COLUMN_BOOK_QUANTITY, bookQuantity);
            bookToAdd.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
            bookToAdd.put(BookEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);
            bookToAdd.put(BookEntry.COLUMN_BOOK_PRICE, bookPrice);


            //if uri is null, that means its insertion of new book
            if (mCurrentBookUri == null) {
                Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, bookToAdd);
                if (newUri != null) {

                    Toast.makeText(this, R.string.successful_insert, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.unsuccessful_insert, Toast.LENGTH_SHORT).show();
                }

            }
            //Else if uri is not null, it means its update of existing book
            else {
                int rowsAffected = getContentResolver().update(mCurrentBookUri, bookToAdd, null, null);
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_book_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_book_successful),
                            Toast.LENGTH_SHORT).show();
                }

            }
            finish();
        }


    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE};


        //This loader will execute content provider's  query method on a background thread
        return new CursorLoader(this,
                mCurrentBookUri,
                projection,
                null,
                null,
                null);

    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {

            //Find the columns of book attributes we are interested in
            int nameColIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int priceColIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int qtyColIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int supplierColIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE);

            //Read book details from cursor for current book
            String bookName = cursor.getString(nameColIndex);
            int bookPrice = cursor.getInt(priceColIndex);
            int bookQty = cursor.getInt(qtyColIndex);
            String supplier = cursor.getString(supplierColIndex);
            String supplierPhone = cursor.getString(supplierPhoneColIndex);

            //Update TextViews with values of current book
            mEditBookName.setText(bookName);
            mEditBookPrice.setText(String.valueOf(bookPrice));
            mEditBookQuantity.setText(String.valueOf(bookQty));
            mEditSupplierName.setText(supplier);
            mEditSupplierPhone.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        mEditBookName.setText("");
        mEditBookPrice.setText("");
        mEditBookQuantity.setText("");
        mEditSupplierName.setText("");
        mEditSupplierPhone.setText("");

    }


    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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


    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
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

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
}










