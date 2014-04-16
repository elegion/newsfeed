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

import com.elegion.newsfeed.sqlite.Feed;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
                              SyncResult syncResult) {
        try {
            final Cursor feeds = provider.query(Feed.URI, new String[]{Feed.Columns.RSS_LINK}, null, null, null);
            try {
                if (feeds.moveToFirst()) {
                    do {
                        syncFeed(feeds.getString(0), provider, syncResult);
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

    private void syncFeed(String rssLink, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(SyncAdapter.class.getSimpleName(), "[" + Thread.currentThread().getName() + "] " + rssLink);
    }

}
