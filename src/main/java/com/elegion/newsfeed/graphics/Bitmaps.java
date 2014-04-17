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
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Daniel Serdyukov
 */
public final class Bitmaps {

    private static final int BITMAP_HEAD = 1024;

    private static final double LN_2 = Math.log(2);

    private Bitmaps() {
    }

    public static Bitmap decodeFile(String filePath, int hwSize) {
        if (hwSize > 0) {
            final BitmapFactory.Options ops = new BitmapFactory.Options();
            ops.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, ops);
            ops.inSampleSize = calculateInSampleSize(ops, hwSize);
            ops.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(filePath, ops);
        }
        return BitmapFactory.decodeFile(filePath);
    }

    public static Bitmap decodeStream(InputStream stream, int hwSize) {
        return decodeStream(stream, null, hwSize);
    }

    public static Bitmap decodeStream(InputStream stream, Rect outPadding, int hwSize) {
        if (hwSize > 0) {
            final InputStream localIn = new BufferedInputStream(stream);
            try {
                try {
                    final BitmapFactory.Options ops = new BitmapFactory.Options();
                    ops.inJustDecodeBounds = true;
                    localIn.mark(BITMAP_HEAD);
                    BitmapFactory.decodeStream(localIn, outPadding, ops);
                    ops.inSampleSize = calculateInSampleSize(ops, hwSize);
                    ops.inJustDecodeBounds = false;
                    localIn.reset();
                    return BitmapFactory.decodeStream(localIn, outPadding, ops);
                } finally {
                    localIn.close();
                }
            } catch (IOException e) {
                Log.e(Bitmaps.class.getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
        return BitmapFactory.decodeStream(stream);
    }

    public static int calculateInSampleSize(BitmapFactory.Options ops, int hwSize) {
        final int outHeight = ops.outHeight;
        final int outWidth = ops.outWidth;
        if (outWidth > hwSize || outHeight > hwSize) {
            final double ratio = Math.max(
                    Math.round((double) outWidth / (double) hwSize),
                    Math.round((double) outHeight / (double) hwSize)
            );
            return ratio > 0 ? (int) Math.pow(2, Math.floor(Math.log(ratio) / LN_2)) : 1;
        }
        return 1;
    }

}
