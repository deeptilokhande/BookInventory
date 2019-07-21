package com.example.prash.bookinventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prash.bookinventory.data.BookContract;
import com.example.prash.bookinventory.data.BookContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {


    //New constructor for Book Cursor Adapter
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /*flags*/);
    }

    //Make a new blank List View
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    //This method binds the book data (in the current row pointed to by cursor) to the given
    //list item layout.
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find individual text views we need to modify in list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.book_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.book_price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.book_quantity);
        Button saleBooks = (Button) view.findViewById(R.id.sale_button);


        //Find the columns of book attributes we are interested in
        int nameColIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int priceColIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int qtyColIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);

        //Read book details from cursor for current book
        String bookName = cursor.getString(nameColIndex);
        int bookPrice = cursor.getInt(priceColIndex);
        final int bookQty = cursor.getInt(qtyColIndex);

        //Update TextViews with values of current book
        nameTextView.setText(bookName);
        priceTextView.setText(String.valueOf(bookPrice));
        quantityTextView.setText(String.valueOf(bookQty));


        //When Sale books button is clicked, reduce book qty by 1 and update the list itemview
        int currentId = cursor.getInt(cursor.getColumnIndex(BookEntry._ID));
        final Uri contentUri = Uri.withAppendedPath(BookEntry.CONTENT_URI, Integer.toString(currentId));

        saleBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int quantity = Integer.valueOf(quantityTextView.getText().toString());
                if (quantity > 0) {
                    quantity -= 1;
                } else {
                    Toast.makeText(context, R.string.neg_quantity_error_msg, Toast.LENGTH_SHORT).show();
                    quantity = 0;
                }
                // Updates the data with the new quantity value
                ContentValues values = new ContentValues();
                values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
                context.getContentResolver().update(contentUri, values, null, null);
                String stringValue = String.valueOf(quantity);
                quantityTextView.setText(stringValue);
            }
        });


    }


}
