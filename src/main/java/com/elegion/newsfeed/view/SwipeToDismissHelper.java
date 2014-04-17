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

import android.content.Context;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;

/**
 * @author Daniel Serdyukov
 */
class SwipeToDismissHelper {

    private final AbsListView mListView;

    private final int mTouchSlop;

    private final int mMinFlingVelocity;

    private final int mMaxFlingVelocity;

    private final long mAnimationTime;

    private int mListViewWidth;

    SwipeToDismissHelper(AbsListView listView) {
        mListView = listView;
        final Context context = listView.getContext();
        final ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    @SuppressWarnings("unused")
    public boolean isValidTouch(View view, MotionEvent event) {
        if (mListViewWidth <= 0 && view == mListView) {
            mListViewWidth = mListView.getWidth();
        }
        return mListViewWidth > 0 && view == mListView;
    }

    public int getListViewWidth() {
        return mListViewWidth;
    }

    public int getMinFlingVelocity() {
        return mMinFlingVelocity;
    }

    public int getMaxFlingVelocity() {
        return mMaxFlingVelocity;
    }

    public long getAnimationTime() {
        return mAnimationTime;
    }

    public int getTouchSlop() {
        return mTouchSlop;
    }

    public View getHitChild(MotionEvent event) {
        final Rect hitRect = new Rect();
        final int childCount = mListView.getChildCount();
        final int[] listViewCoords = new int[2];
        mListView.getLocationOnScreen(listViewCoords);
        final int x = (int) event.getRawX() - listViewCoords[0];
        final int y = (int) event.getRawY() - listViewCoords[1];
        for (int i = 0; i < childCount; i++) {
            final View child = mListView.getChildAt(i);
            child.getHitRect(hitRect);
            if (hitRect.contains(x, y)) {
                return child;
            }
        }
        return null;
    }

    public int getHitPosition(View view) {
        return mListView.getPositionForView(view);
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        mListView.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public void dispatchTouchEvent(MotionEvent event) {
        mListView.onTouchEvent(event);
    }

}
