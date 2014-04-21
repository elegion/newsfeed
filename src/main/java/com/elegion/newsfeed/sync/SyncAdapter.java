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

package com.elegion.newsfeed.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.elegion.newsfeed.rss.RssFeedParser;
import com.elegion.newsfeed.sqlite.FeedProvider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String KEY_FEED_ID = "com.elegion.newsfeed.sync.KEY_FEED_ID";

    public SyncAdapter(Context context) {
        super(context, true);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
                              SyncResult syncResult) {
        final long feedId = extras.getLong(KEY_FEED_ID, -1);
        if (feedId > 0) {
            syncFeeds(provider, syncResult, FeedProvider.Columns._ID + "=?", new String[]{String.valueOf(feedId)});
        } else {
            syncFeeds(provider, syncResult, null, null);
        }
    }

    private void syncFeeds(ContentProviderClient provider, SyncResult syncResult, String where, String[] whereArgs) {
        try {
            final Cursor feeds = provider.query(
                    FeedProvider.URI, new String[]{
                            FeedProvider.Columns._ID,
                            FeedProvider.Columns.RSS_LINK
                    }, where, whereArgs, null
            );
            try {
                if (feeds.moveToFirst()) {
                    do {
                        syncFeed(feeds.getString(0), feeds.getString(1), provider, syncResult);
                    } while (feeds.moveToNext());
                }
            } finally {
                feeds.close();
            }
        } catch (RemoteException e) {
            Log.e(SyncAdapter.class.getName(), e.getMessage(), e);
            ++syncResult.stats.numIoExceptions;
        }
    }

    private void syncFeed(String feedId, String feedUrl, ContentProviderClient provider, SyncResult syncResult) {
        try {
            final HttpURLConnection cn = (HttpURLConnection) new URL(feedUrl).openConnection();
            try {
                final RssFeedParser parser = new RssFeedParser(cn.getInputStream());
                try {
                    parser.parse(feedId, provider, syncResult);
                } finally {
                    parser.close();
                }
            } finally {
                cn.disconnect();
            }
        } catch (IOException e) {
            Log.e(SyncAdapter.class.getName(), e.getMessage(), e);
            ++syncResult.stats.numIoExceptions;
        }
    }

}
