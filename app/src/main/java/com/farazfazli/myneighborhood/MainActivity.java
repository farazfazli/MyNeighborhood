package com.farazfazli.myneighborhood;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "[" + MainActivity.class.getCanonicalName() + "]";
    private ListView mPlacesListView;
    private CursorAdapter mCursorAdapter;

    private boolean mShowOnlyFavorited = true;

    private DatabaseHelper helper;

    @Override
    protected void onResume() {

        // I moved this to here so it updates the color if I go back
        // to the activity, as onResume is called again but not onCreate

        Cursor cursor = helper.getAllRows(); // Get all rows from SQLite Database
        mCursorAdapter = new CursorAdapter(MainActivity.this, cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflating custom list layout
                return LayoutInflater.from(MainActivity.this).inflate(R.layout.neighborhood_list_layout, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Here cursor values for particular column are being set to the view
                TextView place = (TextView) view.findViewById(R.id.place);
                TextView city = (TextView) view.findViewById(R.id.city);
                TextView state = (TextView) view.findViewById(R.id.state);
                ImageView image = (ImageView) view.findViewById(R.id.image);
                place.setText(cursor.getString(cursor.getColumnIndex("place")));
                city.setText(cursor.getString(cursor.getColumnIndex("city")));
                state.setText(cursor.getString(cursor.getColumnIndex("state")));
                String mStarred = cursor.getString(cursor.getColumnIndex("favorite"));
                if (mStarred.equals("NOTSTARRED")) { // if not starred
                    place.setTextColor(Color.BLACK); // Black if not starred
                } else { // if starred
                    place.setTextColor(Color.GREEN); // Else green
                }
                new DownloadImageTask((ImageView) view.findViewById(R.id.image))
                        .execute(cursor.getString(cursor.getColumnIndex("image")));
            }
        };
        mPlacesListView.setAdapter(mCursorAdapter); // set ListView to use CursorAdapter
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = DatabaseHelper.getInstance(this); // Initialize new helper class

        mPlacesListView = (ListView) findViewById(R.id.placesListView);

        mPlacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("position", position); // pass position to DetailActivity
                startActivity(intent); // start DetailActivity
            }
        });
    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
            menu.add(0,1,menu.NONE,"Toggle Favorites");
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true; // don't close -- smooth :)
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mShowOnlyFavorited) {
            Cursor cursor = helper.getFavorited(true); // get all favorites
            mCursorAdapter.swapCursor(cursor);
            mShowOnlyFavorited = false; // flip
        } else {
            Cursor cursor = helper.getFavorited(false); // get all not favorites
            mCursorAdapter.swapCursor(cursor);
            mShowOnlyFavorited = true; // flip
        }
        return super.onOptionsItemSelected(item);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        // http://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
        // This downloads an image and sets it to the ImageView
        // TODO Lrucache

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
}
