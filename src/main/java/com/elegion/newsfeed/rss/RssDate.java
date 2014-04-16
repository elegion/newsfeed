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

import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Daniel Serdyukov
 */
public final class RssDate {

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            final DateFormat rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
            rfc1123.setTimeZone(TimeZone.getTimeZone("UTC"));
            return rfc1123;
        }
    };

    private static final String[] COMPATIBLE_DATE_FORMATS = new String[]{
            "EEEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM d HH:mm:ss yyyy",
            "EEE, dd-MMM-yyyy HH:mm:ss z",
            "EEE, dd-MMM-yyyy HH-mm-ss z",
            "EEE, dd MMM yy HH:mm:ss z",
            "EEE dd-MMM-yyyy HH:mm:ss z",
            "EEE dd MMM yyyy HH:mm:ss z",
            "EEE dd-MMM-yyyy HH-mm-ss z",
            "EEE dd-MMM-yy HH:mm:ss z",
            "EEE dd MMM yy HH:mm:ss z",
            "EEE,dd-MMM-yy HH:mm:ss z",
            "EEE,dd-MMM-yyyy HH:mm:ss z",
            "EEE, dd-MM-yyyy HH:mm:ss z",
            "EEE MMM d yyyy HH:mm:ss z"
    };

    private RssDate() {
    }

    public static Date parse(String value) {
        if (!TextUtils.isEmpty(value)) {
            try {
                return DATE_FORMAT.get().parse(value);
            } catch (ParseException e) {
                Log.e(RssDate.class.getSimpleName(), e.getMessage(), e);
            }
            for (String formatString : COMPATIBLE_DATE_FORMATS) {
                try {
                    return new SimpleDateFormat(formatString, Locale.US).parse(value);
                } catch (ParseException e) {
                    Log.e(RssDate.class.getSimpleName(), e.getMessage(), e);
                }
            }
        }
        return new Date(0);
    }

    public static String format(Date date) {
        return DATE_FORMAT.get().format(date);
    }

    public static String format(long time) {
        return format(new Date(time));
    }

}
