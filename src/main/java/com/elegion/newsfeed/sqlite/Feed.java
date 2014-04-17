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

package com.elegion.newsfeed.sqlite;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;

import com.elegion.newsfeed.AppDelegate;
import com.elegion.newsfeed.R;
import com.elegion.newsfeed.sync.SyncAdapter;

/**
 * @author Daniel Serdyukov
 */
public class Feed extends SQLiteTable {

    public static final String TABLE_NAME = "feeds";

    public static final Uri URI = Uri.parse("content://com.elegion.newsfeed/" + TABLE_NAME);

    public Feed() {
        super(TABLE_NAME);
    }

    public static long getId(Cursor c) {
        return c.getLong(c.getColumnIndex(Columns._ID));
    }

    public static String getIconUrl(Cursor c) {
        return c.getString(c.getColumnIndex(Columns.IMAGE_URL));
    }

    public static String getTitle(Cursor c) {
        return c.getString(c.getColumnIndex(Columns.TITLE));
    }

    public static String getLink(Cursor c) {
        return c.getString(c.getColumnIndex(Columns.LINK));
    }

    public static long getPubDate(Cursor c) {
        return c.getLong(c.getColumnIndex(Columns.PUB_DATE));
    }

    public static String getRssLink(Cursor c) {
        return c.getString(c.getColumnIndex(Columns.RSS_LINK));
    }

    @Override
    public void onContentChanged(Context context, int operation, Bundle extras) {
        if (operation == INSERT) {
            extras.keySet();
            final Bundle syncExtras = new Bundle();
            syncExtras.putLong(SyncAdapter.KEY_FEED_ID, extras.getLong(KEY_LAST_ID, -1));
            ContentResolver.requestSync(new Account(
                    context.getString(R.string.app_name),
                    AppDelegate.ACCOUNT_TYPE
            ), AppDelegate.CONTENT_AUTHORITY, syncExtras);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_NAME +
                "(" + Columns._ID + " integer primary key on conflict replace, "
                + Columns.TITLE + " text, "
                + Columns.LINK + " text, "
                + Columns.IMAGE_URL + " text, "
                + Columns.LANGUAGE + " text, "
                + Columns.PUB_DATE + " integer, "
                + Columns.RSS_LINK + " text unique on conflict ignore)");
        db.execSQL("create trigger if not exists after delete on " + TABLE_NAME +
                " begin " +
                " delete from " + News.TABLE_NAME + " where " + News.Columns.FEED_ID + "=old" + Columns._ID + ";" +
                " end;");
    }

    public interface Columns extends BaseColumns {
        String TITLE = "title";
        String LINK = "link";
        String IMAGE_URL = "imageUrl";
        String LANGUAGE = "language";
        String PUB_DATE = "pubDate";
        String RSS_LINK = "rssLink";
    }

}
