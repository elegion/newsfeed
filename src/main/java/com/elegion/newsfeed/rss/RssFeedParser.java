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

package com.elegion.newsfeed.rss;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.SyncResult;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.elegion.newsfeed.sqlite.FeedProvider;
import com.elegion.newsfeed.sqlite.NewsProvider;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
public class RssFeedParser implements Closeable {

    private static final String CHANNEL = "channel";

    private static final String TITLE = "title";

    private static final String LINK = "link";

    private static final String LANGUAGE = "language";

    private static final String PUB_DATE = "pubDate";

    private static final String IMAGE = "image";

    private static final String URL = "url";

    private static final String ITEM = "item";

    private static final String AUTHOR = "author";

    private static final int CHANNEL_DEPTH = 2;

    private static final int CHANNEL_INFO_DEPTH = 3;

    private static final int IMAGE_INFO_DEPTH = 4;

    private static final int ITEM_INFO_DEPTH = 4;

    private final InputStream mInputStream;

    public RssFeedParser(InputStream content) {
        mInputStream = content;
    }

    public void parse(String feedId, ContentProviderClient provider, SyncResult syncResult) {
        if (mInputStream != null) {
            try {
                final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                final XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(mInputStream, "UTF-8");
                parseFeed(xpp, feedId, provider, syncResult);
            } catch (XmlPullParserException e) {
                Log.e(RssFeedParser.class.getSimpleName(), e.getMessage(), e);
                ++syncResult.stats.numParseExceptions;
            }
        }
    }

    @Override
    public void close() {
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                Log.e(RssFeedParser.class.getSimpleName(), e.getMessage(), e);
            }
        }
    }

    protected void parseFeed(XmlPullParser xpp, String feedId, ContentProviderClient provider, SyncResult syncResult) {
        try {
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getDepth() == CHANNEL_DEPTH
                            && TextUtils.equals(CHANNEL, xpp.getName())) {
                        onChannelNode(xpp, feedId, provider, syncResult);
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException | IOException | RemoteException e) {
            Log.e(RssFeedParser.class.getSimpleName(), e.getMessage(), e);
            ++syncResult.stats.numParseExceptions;
        }
    }

    protected void onChannelNode(XmlPullParser xpp, String feedId, ContentProviderClient provider,
                                 SyncResult syncResult) throws IOException, XmlPullParserException, RemoteException {
        final ContentValues channelValues = new ContentValues();
        final List<ContentValues> channelItemValuesList = new ArrayList<>();
        int eventType = xpp.nextTag();
        while (!(eventType == XmlPullParser.END_TAG
                && xpp.getDepth() == CHANNEL_DEPTH
                && TextUtils.equals(CHANNEL, xpp.getName()))) {
            if (eventType == XmlPullParser.START_TAG && xpp.getDepth() == CHANNEL_INFO_DEPTH) {
                final String nodeName = xpp.getName();
                if (TextUtils.equals(TITLE, nodeName)) {
                    channelValues.put(FeedProvider.Columns.TITLE, xpp.nextText());
                } else if (TextUtils.equals(LINK, nodeName)) {
                    channelValues.put(FeedProvider.Columns.LINK, xpp.nextText());
                } else if (TextUtils.equals(PUB_DATE, nodeName)) {
                    channelValues.put(FeedProvider.Columns.PUB_DATE, RssDate.parse(xpp.nextText()).getTime());
                } else if (TextUtils.equals(LANGUAGE, nodeName)) {
                    channelValues.put(FeedProvider.Columns.LANGUAGE, xpp.nextText());
                } else if (TextUtils.equals(IMAGE, nodeName)) {
                    onChannelImageNode(xpp, channelValues);
                } else if (TextUtils.equals(ITEM, nodeName)) {
                    final ContentValues itemValues = new ContentValues();
                    itemValues.put(NewsProvider.Columns.FEED_ID, feedId);
                    onChannelItemNode(xpp, itemValues);
                    channelItemValuesList.add(itemValues);
                }
            }
            eventType = xpp.next();
        }
        syncResult.stats.numUpdates += provider
                .update(FeedProvider.URI, channelValues, FeedProvider.Columns._ID + "=?", new String[]{feedId});
        syncResult.stats.numDeletes += provider
                .delete(NewsProvider.URI, NewsProvider.Columns.FEED_ID + "=?", new String[]{feedId});
        syncResult.stats.numUpdates += provider
                .bulkInsert(NewsProvider.URI, channelItemValuesList.toArray(new ContentValues[channelItemValuesList.size()]));
    }

    protected void onChannelImageNode(XmlPullParser xpp, ContentValues channelValues)
            throws IOException, XmlPullParserException {
        int eventType = xpp.nextTag();
        while (!(eventType == XmlPullParser.END_TAG
                && xpp.getDepth() == CHANNEL_INFO_DEPTH
                && TextUtils.equals(IMAGE, xpp.getName()))) {
            if (eventType == XmlPullParser.START_TAG
                    && xpp.getDepth() == IMAGE_INFO_DEPTH
                    && TextUtils.equals(URL, xpp.getName())) {
                channelValues.put(FeedProvider.Columns.IMAGE_URL, xpp.nextText());
            }
            eventType = xpp.next();
        }
    }

    protected void onChannelItemNode(XmlPullParser xpp, ContentValues itemValues)
            throws IOException, XmlPullParserException {
        int eventType = xpp.nextTag();
        while (!(eventType == XmlPullParser.END_TAG
                && xpp.getDepth() == CHANNEL_INFO_DEPTH
                && TextUtils.equals(ITEM, xpp.getName()))) {
            if (eventType == XmlPullParser.START_TAG && xpp.getDepth() == ITEM_INFO_DEPTH) {
                final String nodeName = xpp.getName();
                if (TextUtils.equals(TITLE, nodeName)) {
                    itemValues.put(NewsProvider.Columns.TITLE, xpp.nextText());
                } else if (TextUtils.equals(LINK, nodeName)) {
                    itemValues.put(NewsProvider.Columns.LINK, xpp.nextText());
                } else if (TextUtils.equals(PUB_DATE, nodeName)) {
                    itemValues.put(NewsProvider.Columns.PUB_DATE, RssDate.parse(xpp.nextText()).getTime());
                } else if (TextUtils.equals(AUTHOR, nodeName)) {
                    itemValues.put(NewsProvider.Columns.AUTHOR, xpp.nextText());
                }
            }
            eventType = xpp.next();
        }
    }

}
