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
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;

import com.elegion.newsfeed.AppDelegate;
import com.elegion.newsfeed.R;
import com.elegion.newsfeed.sqlite.News;
import com.elegion.newsfeed.sync.SyncAdapter;
import com.elegion.newsfeed.widget.CursorBinderAdapter;

/**
 * @author Daniel Serdyukov
 */
public class NewsList extends SwipeToRefreshList implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String KEY_FEED_ID = "com.elegion.newsfeed.KEY_FEED_ID";

    private long mFeedId;

    private CursorAdapter mListAdapter;

    public static NewsList newInstance(long feedId) {
        final NewsList fragment = new NewsList();
        final Bundle args = new Bundle();
        args.putLong(KEY_FEED_ID, feedId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFeedId = getArguments().getLong(KEY_FEED_ID, -1);
        mListAdapter = new CursorBinderAdapter(getActivity(), R.layout.li_news);
        setListAdapter(mListAdapter);
        getLoaderManager().initLoader(R.id.news_loader, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == R.id.news_loader) {
            return new CursorLoader(
                    getActivity().getApplicationContext(),
                    News.URI, null,
                    News.Columns.FEED_ID + "=?",
                    new String[]{String.valueOf(mFeedId)},
                    News.Columns.PUB_DATE + " DESC"
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == R.id.news_loader) {
            mListAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == R.id.news_loader) {
            mListAdapter.swapCursor(null);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Cursor news = mListAdapter.getCursor();
        if (news.moveToPosition(position)) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(News.getLink(news))));
        }
    }

    @Override
    protected void onRefresh(Account account) {
        final Bundle extras = new Bundle();
        extras.putLong(SyncAdapter.KEY_FEED_ID, mFeedId);
        ContentResolver.requestSync(account, AppDelegate.CONTENT_AUTHORITY, extras);
    }

    @Override
    protected void onSyncStatusChanged(Account account, boolean isSyncActive) {
        setRefreshing(isSyncActive);
    }

}
