package com.farazfazli.myneighborhood;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "[" + MainActivity.class.getCanonicalName() + "]";
    private ListView mPlacesListView;
    private CursorAdapter mCursorAdapter;

    private DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = DatabaseHelper.getInstance(this);

        mPlacesListView = (ListView) findViewById(R.id.placesListView);

        helper.insertData();

        Cursor cursor = helper.getAllRows();

        mCursorAdapter = new CursorAdapter(MainActivity.this, cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(MainActivity.this).inflate(R.layout.neighborhood_list_layout, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView place = (TextView) view.findViewById(R.id.place);
                TextView city = (TextView) view.findViewById(R.id.city);
                TextView state = (TextView) view.findViewById(R.id.state);
                ImageView image = (ImageView) view.findViewById(R.id.image);
                place.setText(cursor.getString(cursor.getColumnIndex("place")));
                city.setText(cursor.getString(cursor.getColumnIndex("city")));
                state.setText(cursor.getString(cursor.getColumnIndex("state")));
                new DownloadImageTask((ImageView) view.findViewById(R.id.image))
                        .execute(cursor.getString(cursor.getColumnIndex("image")));
            }
        };

        mPlacesListView.setAdapter(mCursorAdapter);

        mPlacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        // Who needs intents and activity jumping when we can just listen live?
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true; // don't close -- smooth ;)
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // This will ALWAYS work, because an empty query is a select all query
                Cursor cursor = helper.search(query);
                mCursorAdapter.swapCursor(cursor);
                return false;
            }
        });
        return true;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        // http://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public class getData extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;

        @Override
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL("https://api.github.com/users/dmnugent80/repos");
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }


            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {

            //Do something with the JSON string

        }

    }
}
