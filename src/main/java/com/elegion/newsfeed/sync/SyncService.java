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

package com.elegion.newsfeed.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author =Troy= <Daniel Serdyukov>
 * @version 1.0
 */
public class SyncService extends Service {

    private static final AtomicReference<SyncAdapter> SYNC_ADAPTER = new AtomicReference<SyncAdapter>();

    @Override
    public void onCreate() {
        super.onCreate();
        if (SYNC_ADAPTER.get() == null) {
            SYNC_ADAPTER.compareAndSet(null, new SyncAdapter(getApplicationContext(), true));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return SYNC_ADAPTER.get().getSyncAdapterBinder();
    }

}
