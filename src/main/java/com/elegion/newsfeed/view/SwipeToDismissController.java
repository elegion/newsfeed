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

import android.database.DataSetObserver;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.AbsListView;

/**
 * @author Daniel Serdyukov
 */
public class SwipeToDismissController implements View.OnTouchListener, AbsListView.OnScrollListener {

    private final DataSetObserver mDataSetObserver = new DataSetObserverImpl();

    private final SwipeToDismissHelper mHelper;

    private final SwipeToDismissAnimator mAnimator;

    private final SwipeToDismissCallback mCallback;

    private final AbsListView.OnScrollListener mOnScrollListener;

    private VelocityTracker mVelocityTracker;

    private MotionEvent mLastMotion;

    private boolean mEnabled = true;

    public SwipeToDismissController(AbsListView listView, SwipeToDismissCallback callback) {
        this(listView, callback, null);
    }

    public SwipeToDismissController(AbsListView listView, SwipeToDismissCallback callback,
                                    AbsListView.OnScrollListener onScrollListener) {
        mHelper = new SwipeToDismissHelper(listView);
        mAnimator = new SwipeToDismissAnimator(mHelper, callback);
        mCallback = callback;
        mOnScrollListener = onScrollListener;
    }

    public DataSetObserver getDataSetObserver() {
        return mDataSetObserver;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (mHelper.isValidTouch(view, event) && isEnabled()) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    return onDownEvent(event);
                case MotionEvent.ACTION_CANCEL:
                    return onCancelEvent(event);
                case MotionEvent.ACTION_MOVE:
                    return onMoveEvent(event);
                case MotionEvent.ACTION_UP:
                    return onUpEvent(event);
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mEnabled = scrollState == SCROLL_STATE_IDLE;
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    protected void onDataSetChanged() {
        mAnimator.restoreHitChild();
    }

    protected void onDataSetInvalidated() {
        mAnimator.recycle();
        recycle();
    }

    private boolean isEnabled() {
        return mEnabled && mAnimator.isEnabled();
    }

    private boolean onDownEvent(MotionEvent event) {
        final View hitChild = mHelper.getHitChild(event);
        if (hitChild != null) {
            mLastMotion = MotionEvent.obtainNoHistory(event);
            final int hitPosition = mHelper.getHitPosition(hitChild);
            if (mCallback.canDismissView(hitChild, hitPosition)) {
                mAnimator.setHitChild(hitChild, hitPosition);
                mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.addMovement(event);
            } else {
                mAnimator.recycle();
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    private boolean onCancelEvent(MotionEvent event) {
        if (mVelocityTracker != null) {
            mAnimator.recycle();
            recycle();
        }
        return false;
    }

    private boolean onMoveEvent(MotionEvent event) {
        if (mVelocityTracker != null && mLastMotion != null) {
            mVelocityTracker.addMovement(event);
            final float deltaX = event.getRawX() - mLastMotion.getRawX();
            final float deltaY = event.getRawY() - mLastMotion.getRawY();
            if (Math.abs(deltaX) > mHelper.getTouchSlop() && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                mAnimator.setDrag(true);
                mHelper.requestDisallowInterceptTouchEvent(true);
                final MotionEvent cancelEvent = MotionEvent.obtain(event);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL
                        | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                mHelper.dispatchTouchEvent(cancelEvent);
                cancelEvent.recycle();
            }
            if (mAnimator.isDrag()) {
                mAnimator.dragHitChild(deltaX);
                return true;
            }
        }
        return false;
    }

    private boolean onUpEvent(MotionEvent event) {
        if (mVelocityTracker != null && mLastMotion != null) {
            if (mAnimator.isDrag()) {
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);
                mAnimator.flingHitChild(mVelocityTracker.getXVelocity(), event.getRawX() - mLastMotion.getRawX());
            }
            recycle();
        }
        return false;
    }

    private void recycle() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        mAnimator.setDrag(false);
        if (mLastMotion != null) {
            mLastMotion.recycle();
        }
        mVelocityTracker = null;
        mLastMotion = null;
    }

    private final class DataSetObserverImpl extends DataSetObserver {
        @Override
        public void onChanged() {
            onDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            onDataSetInvalidated();
        }
    }

}
