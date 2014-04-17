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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elegion.newsfeed.R;
import com.elegion.newsfeed.sqlite.Feed;
import com.elegion.newsfeed.widget.CursorBinder;
import com.elegion.newsfeed.widget.FeedIconView;

import java.text.DateFormat;
import java.util.Date;

/**
 * @author Daniel Serdyukov
 */
public class FeedListItem extends LinearLayout implements CursorBinder {

    private FeedIconView mIcon;

    private TextView mTitle;

    private TextView mLink;

    private TextView mPubDate;

    public FeedListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    @SuppressLint("StringFormatMatches")
    public void bindCursor(Cursor c) {
        mIcon.loadIcon(Feed.getIconUrl(c));
        final String title = Feed.getTitle(c);
        if (!TextUtils.isEmpty(title)) {
            mTitle.setText(title);
        } else {
            mTitle.setText(getResources().getString(R.string.feed_p, Feed.getId(c)));
        }
        final String link = Feed.getLink(c);
        if (!TextUtils.isEmpty(link)) {
            mLink.setText(link);
        } else {
            mLink.setText(Feed.getRssLink(c));
        }
        final long pubDate = Feed.getPubDate(c);
        if (pubDate > 0) {
            mPubDate.setText(DateFormat.getDateTimeInstance().format(new Date(pubDate)));
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIcon = (FeedIconView) findViewById(R.id.feed_icon);
        mTitle = (TextView) findViewById(R.id.title);
        mLink = (TextView) findViewById(R.id.link);
        mPubDate = (TextView) findViewById(R.id.pub_date);
    }

}
