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

package com.elegion.newsfeed;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;
import android.os.Bundle;

/**
 * @author Daniel Serdyukov
 */
public class AppDelegate extends Application {

    public static final String ACCOUNT_TYPE = "com.elegion.newsfeed.account";

    public static final String AUTHORITY = "com.elegion.newsfeed";

    public static Account sAccount;

    @Override
    public void onCreate() {
        super.onCreate();
        final AccountManager am = AccountManager.get(this);
        if (sAccount == null) {
            sAccount = new Account(getString(R.string.news), ACCOUNT_TYPE);
        }
        if (am.addAccountExplicitly(sAccount, getPackageName(), new Bundle())) {
            ContentResolver.setSyncAutomatically(sAccount, AUTHORITY, true);
        }
    }

}
