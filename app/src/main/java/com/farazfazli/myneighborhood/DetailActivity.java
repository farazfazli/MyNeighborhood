package com.farazfazli.myneighborhood;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class DetailActivity extends AppCompatActivity {

    private String mPlace;
    private String mCity;
    private String mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int position = getIntent().getExtras().getInt("position", -1);

        if (position >= 0) {
            Log.e("TAG", "WOAH");
            DatabaseHelper helper = DatabaseHelper.getInstance(this);
            Cursor cursor = helper.getById(position);
            cursor.moveToFirst();
            if (cursor.moveToFirst()) {
                do {
                    mPlace = cursor.getString(cursor.getColumnIndex("place"));
                    mCity = cursor.getString(cursor.getColumnIndex("city"));
                    mState = cursor.getString(cursor.getColumnIndex("state"));
//                    image += cursor.getString(cursor.getColumnIndex("image"));
                } while (cursor.moveToNext());
            }
            cursor.close();

            if (mPlace != null && mCity != null && mState != null) {
                toolbar.setTitle(mPlace);
            }

        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}
