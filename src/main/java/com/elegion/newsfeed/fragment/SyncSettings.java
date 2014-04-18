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

package com.elegion.newsfeed.fragment;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;

import com.elegion.newsfeed.AppDelegate;
import com.elegion.newsfeed.R;

/**
 * @author Daniel Serdyukov
 */
public class SyncSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String KEY_AUTO_SYNC = "com.elegion.newsfeed.KEY_AUTO_SYNC";

    private static final String KEY_AUTO_SYNC_INTERVAL = "com.elegion.newsfeed.KEY_AUTO_SYNC_INTERVAL";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sync_prefs);
        final ListPreference interval = (ListPreference) getPreferenceManager()
                .findPreference(KEY_AUTO_SYNC_INTERVAL);
        interval.setSummary(interval.getEntry());
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (TextUtils.equals(KEY_AUTO_SYNC, key)) {
            if (prefs.getBoolean(key, false)) {
                final long interval = Long.parseLong(prefs.getString(
                        KEY_AUTO_SYNC_INTERVAL,
                        getString(R.string.auto_sync_interval_default)
                ));
                ContentResolver.addPeriodicSync(AppDelegate.sAccount, AppDelegate.AUTHORITY, Bundle.EMPTY, interval);
            } else {
                ContentResolver.removePeriodicSync(AppDelegate.sAccount, AppDelegate.AUTHORITY, new Bundle());
            }
        } else if (TextUtils.equals(KEY_AUTO_SYNC_INTERVAL, key)) {
            final ListPreference interval = (ListPreference) getPreferenceManager().findPreference(key);
            interval.setSummary(interval.getEntry());
            ContentResolver.addPeriodicSync(
                    AppDelegate.sAccount, AppDelegate.AUTHORITY,
                    Bundle.EMPTY, Long.parseLong(interval.getValue())
            );
        }
    }

}
