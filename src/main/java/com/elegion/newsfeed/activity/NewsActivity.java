/*
 * Copyright 2012-2014 Daniel Serdyukov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elegion.newsfeed.activity;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;

import com.elegion.newsfeed.R;
import com.elegion.newsfeed.fragment.NewsList;
import com.elegion.newsfeed.sqlite.Feed;

/**
 * @author Daniel Serdyukov
 */
public class NewsActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_FEED_ID = "com.elegion.newsfeed.EXTRA_FEED_ID";

    private long mFeedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_single_frame);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mFeedId = getIntent().getLongExtra(EXTRA_FEED_ID, -1);
        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.frame1, NewsList.newInstance(mFeedId))
                    .commit();
        }
        getLoaderManager().initLoader(R.id.feeds_loader, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == R.id.feeds_loader) {
            return new CursorLoader(
                    getApplicationContext(), Feed.URI, null,
                    Feed.Columns._ID + "=?",
                    new String[]{String.valueOf(mFeedId)},
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == R.id.feeds_loader && data.moveToFirst()) {
            getActionBar().setTitle(Feed.getTitle(data));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
