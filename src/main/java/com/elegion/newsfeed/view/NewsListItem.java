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

package com.elegion.newsfeed.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elegion.newsfeed.R;
import com.elegion.newsfeed.sqlite.News;
import com.elegion.newsfeed.widget.CursorBinder;

import java.text.DateFormat;
import java.util.Date;

/**
 * @author Daniel Serdyukov
 */
public class NewsListItem extends LinearLayout implements CursorBinder {

    private TextView mTitle;

    private TextView mAuthor;

    private TextView mPubDate;

    public NewsListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    @SuppressLint("StringFormatMatches")
    public void bindCursor(Cursor c) {
        mTitle.setText(News.getTitle(c));
        mAuthor.setText(News.getAuthor(c));
        final long pubDate = News.getPubDate(c);
        if (pubDate > 0) {
            mPubDate.setText(DateFormat.getDateTimeInstance().format(new Date(pubDate)));
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitle = (TextView) findViewById(R.id.title);
        mAuthor = (TextView) findViewById(R.id.author);
        mPubDate = (TextView) findViewById(R.id.pub_date);
    }

}
