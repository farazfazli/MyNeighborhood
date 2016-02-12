package com.farazfazli.myneighborhood;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.Random;

public class DetailActivity extends AppCompatActivity {

    private String mPlace;
    private String mCity;
    private String mState;
    private String mStarred;
    private int position;

    private Toolbar mToolbar;
    private DatabaseHelper mHelper;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        TextView mDetailsTextView = (TextView) findViewById(R.id.details);

        position = getIntent().getExtras().getInt("position", -9); // get position
        position++;

        if (position >= 0) { // if position is valid
            mHelper = DatabaseHelper.getInstance(this);
            Cursor cursor = mHelper.getById(position);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                do {
                    mPlace = cursor.getString(cursor.getColumnIndex("place"));
                    mCity = cursor.getString(cursor.getColumnIndex("city"));
                    mState = cursor.getString(cursor.getColumnIndex("state"));
                    mStarred = cursor.getString(cursor.getColumnIndex("favorite"));
                    if (mStarred.equals("NOTSTARRED")) { // if unstarred
                        fab.setAlpha(.5f); // change transparency to 50% transparent
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_sentiment_neutral_white_24dp));
                    } else { // if starred
                        fab.setAlpha(1f); // change transparency to 0% transparent
                        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_sentiment_very_satisfied_white_24dp));
                    }
                    Random random = new Random();
                    mDetailsTextView.setText(mPlace + "\n" + mCity + ", " + mState + "\n" + (random.nextInt(15) + 2) + " blocks away");
                    // set imageview to image from the internet
                    new DownloadImageTask((ImageView) findViewById(R.id.background))
                            .execute(cursor.getString(cursor.getColumnIndex("image")));
                } while (cursor.moveToNext());
            }
            cursor.close();

            mToolbar.setTitle(mPlace);

        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mStarred.equals("NOTSTARRED")) {
                    mHelper.modifyFavorite(position, true);
                    fab.setAlpha(1f);
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_sentiment_very_satisfied_white_24dp));
                    mStarred = "STARRED";
                    Snackbar.make(view, "Starred " + mPlace + "!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    mHelper.modifyFavorite(position, false);
                    fab.setAlpha(.5f);
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_sentiment_neutral_white_24dp));
                    mStarred = "NOTSTARRED";
                    Snackbar.make(view, "Unstarred " + mPlace + " from favorites!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
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
