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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteContentProvider extends ContentProvider {

    private static final String DATABASE_NAME = "newsfeed.db";

    private static final int DATABASE_VERSION = 1;

    private static final String MIME_DIR = "vnd.android.cursor.dir/";

    private static final Map<String, SQLiteTable> SCHEMA = new ConcurrentHashMap<>();

    static {
        SCHEMA.put(Feed.TABLE_NAME, new Feed());
        SCHEMA.put(News.TABLE_NAME, new News());
    }

    private SQLiteOpenHelper mHelper;

    @Override
    public boolean onCreate() {
        mHelper = new SQLiteOpenHelperImpl(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] columns, String where, String[] whereArgs, String orderBy) {
        final SQLiteTable table = SCHEMA.get(uri.getPathSegments().get(0));
        if (table != null) {
            final Cursor cursor = table.query(mHelper.getReadableDatabase(), columns, where, whereArgs, orderBy);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }
        throw new SQLiteException("Unknown uri " + uri);
    }

    @Override
    public String getType(Uri uri) {
        return MIME_DIR + uri.getPathSegments().get(0);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteTable table = SCHEMA.get(uri.getPathSegments().get(0));
        if (table != null) {
            final long lastId = table.insert(mHelper.getWritableDatabase(), values);
            getContext().getContentResolver().notifyChange(uri, null);
            table.onContentChanged(SQLiteOperation.INSERT, getContext());
            return ContentUris.withAppendedId(uri, lastId);
        }
        throw new SQLiteException("Unknown uri " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        final SQLiteTable table = SCHEMA.get(uri.getPathSegments().get(0));
        if (table != null) {
            final int affectedRows = table.delete(mHelper.getWritableDatabase(), where, whereArgs);
            if (affectedRows > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
                table.onContentChanged(SQLiteOperation.DELETE, getContext());
            }
            return affectedRows;
        }
        throw new SQLiteException("Unknown uri " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        final SQLiteTable table = SCHEMA.get(uri.getPathSegments().get(0));
        if (table != null) {
            final int affectedRows = table.update(mHelper.getWritableDatabase(), values, where, whereArgs);
            if (affectedRows > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
                table.onContentChanged(SQLiteOperation.UPDATE, getContext());
            }
            return affectedRows;
        }
        throw new SQLiteException("Unknown uri " + uri);
    }

    private static final class SQLiteOpenHelperImpl extends SQLiteOpenHelper {

        public SQLiteOpenHelperImpl(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransactionNonExclusive();
            try {
                for (final SQLiteTable table : SCHEMA.values()) {
                    table.onCreate(db);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.beginTransactionNonExclusive();
            try {
                for (final SQLiteTable table : SCHEMA.values()) {
                    table.onUpgrade(db, oldVersion, newVersion);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

    }

}
