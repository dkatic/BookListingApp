package com.example.david.booklistingapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by david on 27.7.2017..
 */

public class BookAdapter extends ArrayAdapter<Book> {

    Context context;
    private ArrayList<Book> bookAdapter;
    //Tag for the log messages
    private static final String LOG_TAG = BookAdapter.class.getSimpleName();

    static class ViewHolder {
        private TextView title;
        private TextView authors;
        private TextView description;
    }

    public BookAdapter(Context context, ArrayList<Book> bookAdapter) {
        super(context, -1, bookAdapter);
        this.context = context;
        this.bookAdapter = bookAdapter;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = (TextView) view.findViewById(R.id.book_title);
            viewHolder.authors = (TextView) view.findViewById(R.id.book_author);
            viewHolder.description = (TextView) view.findViewById(R.id.book_description);
            view.setTag(viewHolder);
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.title.setText(bookAdapter.get(position).getTitle());

        String authorsString = "";
        int authorNr = 0;

        if (bookAdapter.get(position).getAuthors()!=null) {
            for (String author : bookAdapter.get(position).getAuthors()) {

                if (authorNr == 0) {
                    authorsString = "";
                }

                if (authorNr > 0) {
                    authorsString = authorsString + ", ";
                }
                authorsString = authorsString + author;
                authorNr++;
                Log.d(LOG_TAG, "Authors: " + authorsString);
            }
        }

        viewHolder.authors.setText(authorsString);
        viewHolder.description.setText(bookAdapter.get(position).getDescription());

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return bookAdapter.size();
    }
}
