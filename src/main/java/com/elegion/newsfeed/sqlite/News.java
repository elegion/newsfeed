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

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author Daniel Serdyukov
 */
public class News extends SQLiteTable {

    public static final String TABLE_NAME = "news";

    public static final Uri URI = Uri.parse("content://com.elegion.newsfeed/" + TABLE_NAME);

    public News() {
        super(TABLE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_NAME +
                "(" + Columns._ID + " integer primary key on conflict replace, "
                + Columns.TITLE + " text, "
                + Columns.LINK + " text, "
                + Columns.AUTHOR + " text, "
                + Columns.PUB_DATE + " integer, "
                + Columns.FEED_ID + " integer);");
        db.execSQL("create index if not exists " +
                TABLE_NAME + "_" + Columns.FEED_ID + "_index" +
                " on " + TABLE_NAME + "(" + Columns.FEED_ID + ");");
    }

    public interface Columns extends BaseColumns {
        String TITLE = "title";
        String LINK = "link";
        String PUB_DATE = "pubDate";
        String AUTHOR = "author";
        String FEED_ID = "feedId";
    }

}
