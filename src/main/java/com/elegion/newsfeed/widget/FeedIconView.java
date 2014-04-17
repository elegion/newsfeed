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

package com.elegion.newsfeed.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.elegion.newsfeed.graphics.Bitmaps;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Daniel Serdyukov
 */
public class FeedIconView extends ImageView {

    private static final LruCache<String, Bitmap> BITMAP_CACHE = new LruCacheImpl();

    private AsyncTask<String, Void, Bitmap> mLoadIconTask;

    private int mIconSize;

    public FeedIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void loadIcon(String url) {
        if (!TextUtils.isEmpty(url)) {
            mLoadIconTask = new AsyncTask<String, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(String... params) {
                    return loadIconBackground(params[0]);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    setImageBitmap(bitmap);
                }

            }.execute(url);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mIconSize = Math.max(w, h);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mLoadIconTask != null) {
            mLoadIconTask.cancel(false);
            mLoadIconTask = null;
        }
        super.onDetachedFromWindow();
    }

    private Bitmap loadIconBackground(String url) {
        Bitmap bitmap = BITMAP_CACHE.get(url);
        if (bitmap == null) {
            try {
                final HttpURLConnection cn = (HttpURLConnection) new URL(url).openConnection();
                try {
                    bitmap = Bitmaps.decodeStream(cn.getInputStream(), mIconSize);
                    if (bitmap != null) {
                        BITMAP_CACHE.put(url, bitmap);
                    }
                } finally {
                    cn.disconnect();
                }
            } catch (IOException e) {
                Log.e(FeedIconView.class.getSimpleName(), e.getMessage(), e);
            }
        }
        return bitmap;
    }

    private static final class LruCacheImpl extends LruCache<String, Bitmap> {

        public LruCacheImpl() {
            super((int) (Runtime.getRuntime().freeMemory() / 4));
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }

    }

}
