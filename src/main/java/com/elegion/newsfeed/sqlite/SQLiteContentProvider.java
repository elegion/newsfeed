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

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteContentProvider extends ContentProvider {

    private static final String DATABASE_NAME = "newsfeed.db";

    private static final int DATABASE_VERSION = 1;

    private static final String MIME_DIR = "vnd.android.cursor.dir/";

    private static final String MIME_ITEM = "vnd.android.cursor.item/";

    private static final Map<String, SQLiteTableProvider> SCHEMA = new ConcurrentHashMap<>();

    static {
        SCHEMA.put(FeedProvider.TABLE_NAME, new FeedProvider());
        SCHEMA.put(NewsProvider.TABLE_NAME, new NewsProvider());
    }

    private final SQLiteUriMatcher mUriMatcher = new SQLiteUriMatcher();

    private SQLiteOpenHelper mHelper;

    private static ProviderInfo getProviderInfo(Context context, Class<? extends ContentProvider> provider, int flags)
            throws PackageManager.NameNotFoundException {
        return context.getPackageManager()
                .getProviderInfo(new ComponentName(context.getPackageName(), provider.getName()), flags);
    }

    private static String getTableName(Uri uri) {
        return uri.getPathSegments().get(0);
    }


    @Override
    public boolean onCreate() {
        try {
            final ProviderInfo pi = getProviderInfo(getContext(), getClass(), 0);
            final String[] authorities = TextUtils.split(pi.authority, ";");
            for (final String authority : authorities) {
                mUriMatcher.addAuthority(authority);
            }
            mHelper = new SQLiteOpenHelperImpl(getContext());
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            throw new SQLiteException(e.getMessage());
        }
    }

    @Override
    public Cursor query(Uri uri, String[] columns, String where, String[] whereArgs, String orderBy) {
        final int matchResult = mUriMatcher.match(uri);
        if (matchResult == SQLiteUriMatcher.NO_MATCH) {
            throw new SQLiteException("Unknown uri " + uri);
        }
        final String tableName = getTableName(uri);
        final SQLiteTableProvider tableProvider = SCHEMA.get(tableName);
        if (tableProvider == null) {
            throw new SQLiteException("No such table " + tableName);
        }
        if (matchResult == SQLiteUriMatcher.MATCH_ID) {
            where = BaseColumns._ID + "=?";
            whereArgs = new String[]{uri.getLastPathSegment()};
        }
        final Cursor cursor = tableProvider.query(mHelper.getReadableDatabase(), columns, where, whereArgs, orderBy);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int matchResult = mUriMatcher.match(uri);
        if (matchResult == SQLiteUriMatcher.NO_MATCH) {
            throw new SQLiteException("Unknown uri " + uri);
        } else if (matchResult == SQLiteUriMatcher.MATCH_ID) {
            return MIME_ITEM + getTableName(uri);
        }
        return MIME_DIR + getTableName(uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int matchResult = mUriMatcher.match(uri);
        if (matchResult == SQLiteUriMatcher.NO_MATCH) {
            throw new SQLiteException("Unknown uri " + uri);
        }
        final String tableName = getTableName(uri);
        final SQLiteTableProvider tableProvider = SCHEMA.get(tableName);
        if (tableProvider == null) {
            throw new SQLiteException("No such table " + tableName);
        }
        if (matchResult == SQLiteUriMatcher.MATCH_ID) {
            final int affectedRows = updateInternal(
                    tableProvider.getBaseUri(), tableProvider,
                    values, BaseColumns._ID + "=?",
                    new String[]{uri.getLastPathSegment()}
            );
            if (affectedRows > 0) {
                return uri;
            }
        }
        final long lastId = tableProvider.insert(mHelper.getWritableDatabase(), values);
        getContext().getContentResolver().notifyChange(tableProvider.getBaseUri(), null);
        final Bundle extras = new Bundle();
        extras.putLong(SQLiteOperation.KEY_LAST_ID, lastId);
        tableProvider.onContentChanged(getContext(), SQLiteOperation.INSERT, extras);
        return uri;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        final int matchResult = mUriMatcher.match(uri);
        if (matchResult == SQLiteUriMatcher.NO_MATCH) {
            throw new SQLiteException("Unknown uri " + uri);
        }
        final String tableName = getTableName(uri);
        final SQLiteTableProvider tableProvider = SCHEMA.get(tableName);
        if (tableProvider == null) {
            throw new SQLiteException("No such table " + tableName);
        }
        if (matchResult == SQLiteUriMatcher.MATCH_ID) {
            where = BaseColumns._ID + "=?";
            whereArgs = new String[]{uri.getLastPathSegment()};
        }
        final int affectedRows = tableProvider.delete(mHelper.getWritableDatabase(), where, whereArgs);
        if (affectedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            final Bundle extras = new Bundle();
            extras.putLong(SQLiteOperation.KEY_AFFECTED_ROWS, affectedRows);
            tableProvider.onContentChanged(getContext(), SQLiteOperation.DELETE, extras);
        }
        return affectedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        final int matchResult = mUriMatcher.match(uri);
        if (matchResult == SQLiteUriMatcher.NO_MATCH) {
            throw new SQLiteException("Unknown uri " + uri);
        }
        final String tableName = getTableName(uri);
        final SQLiteTableProvider tableProvider = SCHEMA.get(tableName);
        if (tableProvider == null) {
            throw new SQLiteException("No such table " + tableName);
        }
        if (matchResult == SQLiteUriMatcher.MATCH_ID) {
            where = BaseColumns._ID + "=?";
            whereArgs = new String[]{uri.getLastPathSegment()};
        }
        return updateInternal(tableProvider.getBaseUri(), tableProvider, values, where, whereArgs);
    }

    private int updateInternal(Uri uri, SQLiteTableProvider provider,
                               ContentValues values, String where, String[] whereArgs) {
        final int affectedRows = provider.update(mHelper.getWritableDatabase(), values, where, whereArgs);
        if (affectedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            final Bundle extras = new Bundle();
            extras.putLong(SQLiteOperation.KEY_AFFECTED_ROWS, affectedRows);
            provider.onContentChanged(getContext(), SQLiteOperation.UPDATE, extras);
        }
        return affectedRows;
    }

    private static final class SQLiteOpenHelperImpl extends SQLiteOpenHelper {

        public SQLiteOpenHelperImpl(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransactionNonExclusive();
            try {
                for (final SQLiteTableProvider table : SCHEMA.values()) {
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
                for (final SQLiteTableProvider table : SCHEMA.values()) {
                    table.onUpgrade(db, oldVersion, newVersion);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

    }

}
