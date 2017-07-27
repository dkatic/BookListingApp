package com.example.david.booklistingapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //Tag for the log messages
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    ProgressBar loadingIndicator;
    SearchView searchView;
    ListView listView;
    TextView emptyListTextView;
    ArrayList<Book> bookAdapter = new ArrayList<>();
    BookAdapter bookListArrayAdapter;
    FloatingActionButton searchBookFab;
    boolean isSearchOpened = false;
    Bitmap fabIcon;

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(
                getString(R.string.book_activity_saved_instance_booklist_key),
                bookAdapter);
        savedInstanceState.putBoolean(
                getString(R.string.book_activity_saved_instance_book_search_key), isSearchOpened);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.app_name));

        loadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);
        searchView = (SearchView) findViewById(R.id.text_search);
        listView = (ListView) findViewById(R.id.list);
        emptyListTextView = (TextView) findViewById(R.id.no_results);
        searchBookFab = (FloatingActionButton) findViewById(R.id.search_button);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            listView.setNestedScrollingEnabled(true);
        }

        searchBookFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSearchOpened) {
                    hideSearchView();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                } else {
                    showSearchView();
                    //Focus on text and open keyboard
                    searchView.requestFocus();
                    searchView.setFocusable(true);
                    searchView.setFocusableInTouchMode(true);
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        });

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //At Startup hide the Book List
        listView.setEmptyView(emptyListTextView);
        //Loading indicator set as GONE
        loadingIndicator.setVisibility(View.GONE);
        //Welcome message
        emptyListTextView.setText(R.string.welcome_message);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(getString(R.string.book_activity_saved_instance_booklist_key))) {
                bookAdapter = savedInstanceState.getParcelableArrayList(getString(R.string.book_activity_saved_instance_booklist_key));
                updateBookList();
            }

            if (savedInstanceState.containsKey(getString(R.string.book_activity_saved_instance_book_search_key))) {
                isSearchOpened = savedInstanceState.getBoolean(getString(R.string.book_activity_saved_instance_book_search_key));
                if (isSearchOpened) {
                    showSearchView();
                }
            }
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(LOG_TAG, "setOnQueryTextListener: " + query);
                hideSearchView();
                isSearchOpened = false;
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Log.d(LOG_TAG, "Network connected...");
                    AsyncTask bookAsyncTask = new BooksHttpRequestAsyncTask();
                    String[] queryParam = {query};
                    bookAsyncTask.execute(queryParam);
                    loadingIndicator.setVisibility(View.VISIBLE);
                    emptyListTextView.setText("");
                } else {
                    Log.d(LOG_TAG, "Network offline");
                    View loadingIndicator = findViewById(R.id.loading_indicator);
                    loadingIndicator.setVisibility(View.GONE);
                    showShortToast(getString(R.string.network_unavailable_message));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void showShortToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("PrivateResource")
    private void hideSearchView() {
        searchView.setVisibility(View.GONE);
        fabIcon = BitmapFactory.decodeResource(getResources(), android.support.design.R.drawable.abc_ic_search_api_mtrl_alpha);
        searchBookFab.setImageBitmap(fabIcon);
        isSearchOpened = false;
    }

    @SuppressLint("PrivateResource")
    private void showSearchView() {
        searchView.setVisibility(View.VISIBLE);
        fabIcon = BitmapFactory.decodeResource(getResources(), android.support.design.R.drawable.abc_ic_clear_mtrl_alpha);
        searchBookFab.setImageBitmap(fabIcon);
        isSearchOpened = true;
    }

    public class BooksHttpRequestAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.d(LOG_TAG, "doInBackground: " + params[0]);
            try {
                bookHttpRequest(params[0]);
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error on Http Request");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Hide loading indicator because the data has been loaded
            loadingIndicator.setVisibility(View.GONE);
            emptyListTextView.setText(R.string.empty_list_view_message);
            updateBookList();
        }
    }

    private void updateBookList() {
        bookListArrayAdapter = new BookAdapter(getBaseContext(), bookAdapter);
        listView.setAdapter(bookListArrayAdapter);
        bookListArrayAdapter.notifyDataSetChanged();
    }

    private void bookHttpRequest(String query) throws IOException {
        Log.d(LOG_TAG, "bookHttpRequest: " + query);
        InputStream inputStream = null;

        try {
            String encodedQuery = URLEncoder.encode(query, "utf-8");
            URL url = new URL(getResources().getString(R.string.http_query_address) + encodedQuery +
                    getResources().getString(R.string.query_projection_key) +
                    getResources().getString(R.string.projection_lite_key));

            Log.d(LOG_TAG, "url: " + url.toString());
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(getResources().getInteger(R.integer.http_read_timeout));
            httpURLConnection.setConnectTimeout(getResources().getInteger(R.integer.http_connect_timeout));
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();

            int response = httpURLConnection.getResponseCode();
            Log.d(LOG_TAG, "Response code is: " + response);

            switch (response) {
                case HttpURLConnection.HTTP_OK:
                    inputStream = httpURLConnection.getInputStream();
                    String stringResponse = readIt(inputStream);
                    parseJson(stringResponse);
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    showShortToast(getString(R.string.http_url_not_found_message));
                    break;
                default:
                    showShortToast(getString(R.string.http_generic_error_message));
                    break;
            }

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public void parseJson(String stringFromInputStream) {
        try {
            bookAdapter.clear();
            JSONObject jsonObject = new JSONObject(stringFromInputStream);

            // Extract the JSONArray associated with the key called "items"
            JSONArray jArray = jsonObject.getJSONArray(getString(R.string.json_items_key));

            // For each book in the bookArray, create an {@link Book} object
            for (int i = 0; i < jArray.length(); i++) {

                ArrayList<String> authorList = new ArrayList<>();
                Book book = new Book();

                // For a given book, extract the JSONObject associated with the
                // key called "volumeInfo", which represents a list of all properties
                // for that book.
                JSONObject volumeInfo = jArray.getJSONObject(i).getJSONObject(getString(R.string.json_volume_info_key));

                // Extract the value for the key called "title"
                String title = volumeInfo.getString(getString(R.string.json_title_key));
                book.setTitle(title);

                // Extract the value for the key called "authors"
                JSONArray authors = null;
                if (volumeInfo.has(getString(R.string.json_authors_key))) {
                    authors = volumeInfo.getJSONArray(getString(R.string.json_authors_key));
                }
                // Extract the value for the key called "description"
                String description = null;
                if (volumeInfo.has(getString(R.string.json_description_key))) {
                    description = volumeInfo.getString(getString(R.string.json_description_key));
                }
                book.setDescription(description);

                if (authors!=null && authors.length() > 0) {
                    for (int j = 0; j < authors.length(); j++) {
                        String author = authors.getString(j);
                        authorList.add(author);
                    }
                }

                book.setAuthors(authorList);

                // Add the new {@link Book} to the list of books.
                bookAdapter.add(book);
            }

        } catch (JSONException e) {
            Log.d(LOG_TAG, "JSONException");
            e.printStackTrace();
        }
    }

    public String readIt(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(stream));
        String line = responseReader.readLine();

        while (line != null) {
            builder.append(line);
            line = responseReader.readLine();
        }

        return builder.toString();
    }
}