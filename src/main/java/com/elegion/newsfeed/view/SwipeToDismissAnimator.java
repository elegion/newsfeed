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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

/**
 * @author Daniel Serdyukov
 */
class SwipeToDismissAnimator {

    public static final float NO_FLING = 0.0f;

    private final FlingAnimationListener mFlingAnimationListener = new FlingAnimationListener();

    private final DismissAnimationListener mDismissAnimationListener = new DismissAnimationListener();

    private final SwipeToDismissHelper mHelper;

    private final SwipeToDismissCallback mCallback;

    private boolean mEnabled = true;

    private View mHitChild;

    private int mHitPosition;

    private int mHitChildHeight;

    private boolean mIsDrag;

    SwipeToDismissAnimator(SwipeToDismissHelper helper, SwipeToDismissCallback callback) {
        mHelper = helper;
        mCallback = callback;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setHitChild(View hitChild, int hitPosition) {
        mHitChild = hitChild;
        mHitPosition = hitPosition;
        mHitChildHeight = hitChild.getHeight();
    }

    public void setDrag(boolean drag) {
        mIsDrag = drag;
    }

    public boolean isDrag() {
        return mIsDrag;
    }

    public void dragHitChild(float deltaX) {
        if (mHitChild != null) {
            mHitChild.setTranslationX(deltaX);
            mHitChild.setAlpha(Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / mHelper.getListViewWidth())));
        }
    }

    public void flingHitChild(float velocityX, float deltaX) {
        if (mHitChild != null) {
            final float translationX = getFlingTranslationX(velocityX, deltaX);
            if (translationX != NO_FLING && mHitPosition != AbsListView.INVALID_POSITION) {
                mHitChild.animate()
                        .translationX(translationX)
                        .alpha(0)
                        .setDuration(mHelper.getAnimationTime())
                        .setListener(mFlingAnimationListener);
            } else {
                mHitChild.animate()
                        .translationX(0)
                        .alpha(1)
                        .setDuration(mHelper.getAnimationTime())
                        .setListener(null);
            }
        }
    }

    public void recycle() {
        mHitChild = null;
        mHitPosition = AbsListView.INVALID_POSITION;
        mIsDrag = false;
    }

    public void restoreHitChild() {
        if (mHitChild != null) {
            mHitChild.setTranslationX(0f);
            mHitChild.setAlpha(1f);
            final ViewGroup.LayoutParams lp = mHitChild.getLayoutParams();
            lp.height = mHitChildHeight;
            mHitChild.setLayoutParams(lp);
        }
    }

    private float getFlingTranslationX(float velocityX, float deltaX) {
        final int listViewWidth = mHelper.getListViewWidth();
        final float absVelocityX = Math.abs(velocityX);
        if (Math.abs(deltaX) > (float) listViewWidth / 2) {
            return deltaX > 0 ? listViewWidth : -listViewWidth;
        } else if (mHelper.getMinFlingVelocity() <= absVelocityX
                && absVelocityX <= mHelper.getMaxFlingVelocity()) {
            return velocityX > 0 ? listViewWidth : -listViewWidth;
        }
        return NO_FLING;
    }

    private final class FlingAnimationListener extends AnimatorListenerAdapter {
        @Override
        public void onAnimationStart(Animator animation) {
            setEnabled(false);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            animation.removeAllListeners();
            final ValueAnimator heightAnimator = ValueAnimator.ofInt(mHitChildHeight, 1)
                    .setDuration(mHelper.getAnimationTime());
            heightAnimator.addUpdateListener(mDismissAnimationListener);
            heightAnimator.addListener(mDismissAnimationListener);
            heightAnimator.start();
        }
    }

    private final class DismissAnimationListener extends AnimatorListenerAdapter implements
            ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            final ViewGroup.LayoutParams lp = mHitChild.getLayoutParams();
            lp.height = (int) animation.getAnimatedValue();
            mHitChild.setLayoutParams(lp);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            animation.removeAllListeners();
            mCallback.dismissView(mHitChild, mHitPosition);
            setEnabled(true);
        }

    }

}
