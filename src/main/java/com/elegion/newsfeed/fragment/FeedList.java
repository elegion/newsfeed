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

package com.elegion.newsfeed.fragment;

import android.accounts.Account;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;

import com.elegion.newsfeed.AppDelegate;
import com.elegion.newsfeed.R;
import com.elegion.newsfeed.activity.NewsActivity;
import com.elegion.newsfeed.sqlite.Feed;
import com.elegion.newsfeed.widget.CursorBinderAdapter;

/**
 * @author Daniel Serdyukov
 */
public class FeedList extends SwipeToRefreshList implements LoaderManager.LoaderCallbacks<Cursor> {

    private CursorAdapter mListAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListAdapter = new CursorBinderAdapter(getActivity(), R.layout.li_feed);
        setListAdapter(mListAdapter);
        getLoaderManager().initLoader(R.id.feeds_loader, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == R.id.feeds_loader) {
            return new CursorLoader(
                    getActivity().getApplicationContext(),
                    Feed.URI, null, null, null, null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == R.id.feeds_loader) {
            mListAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == R.id.feeds_loader) {
            mListAdapter.swapCursor(null);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Intent intent = new Intent(getActivity(), NewsActivity.class);
        intent.putExtra(NewsActivity.EXTRA_FEED_ID, id);
        startActivity(intent);
    }

    @Override
    public boolean canDismissView(View view, int position) {
        return true;
    }

    @Override
    public void dismissView(View view, int position) {
        final Cursor feed = mListAdapter.getCursor();
        if (feed.moveToPosition(position)) {
            getActivity().getContentResolver().delete(
                    Feed.URI, Feed.Columns._ID + "=?",
                    new String[]{String.valueOf(Feed.getId(feed))}
            );
        }
    }

    @Override
    protected void onRefresh(Account account) {
        ContentResolver.requestSync(account, AppDelegate.CONTENT_AUTHORITY, new Bundle());
    }

    @Override
    protected void onSyncStatusChanged(Account account, boolean isSyncActive) {
        setRefreshing(isSyncActive);
    }

}
