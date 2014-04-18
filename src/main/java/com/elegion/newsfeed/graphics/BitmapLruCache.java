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

package com.elegion.newsfeed.graphics;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * @author Daniel Serdyukov
 */
public final class BitmapLruCache extends LruCache<String, Bitmap> {

    private BitmapLruCache() {
        super((int) (Runtime.getRuntime().freeMemory() / 4));
    }

    public static BitmapLruCache getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getByteCount();
    }

    private static final class Holder {
        public static final BitmapLruCache INSTANCE = new BitmapLruCache();
    }

}
