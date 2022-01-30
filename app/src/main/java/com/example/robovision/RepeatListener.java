package com.example.robovision;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

public class RepeatListener implements OnTouchListener {

    private Handler mhandler = new Handler(); //Handler for this event

    private int mInitialInterval;
    private final int mNormalInterval;
    private final OnClickListener mClickListener;
    private View mTouchedView;

    public RepeatListener(int initialInterval, int normalInterval, OnClickListener clickListener){
        if(clickListener == null) {
            throw new IllegalArgumentException("Null runnable");
        }
        if(initialInterval < 0 || normalInterval < 0){
            throw new IllegalArgumentException("Negative interval passed");
        }
        mInitialInterval = initialInterval;
        mNormalInterval  = normalInterval;
        mClickListener   = clickListener;
    }

    private Runnable mHandlerRunnable = new Runnable() {
        @Override
        public void run() {
            if(mTouchedView.isEnabled()) {
                mhandler.postDelayed(this, mNormalInterval);
                mClickListener.onClick(mTouchedView);
            } else{
                mhandler.removeCallbacks(mHandlerRunnable);
                mTouchedView.setPressed(false);
                mTouchedView = null;
            }
        }
    };


    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mhandler.removeCallbacks(mHandlerRunnable);
                mhandler.postDelayed(mHandlerRunnable, mInitialInterval);
                mTouchedView = view;
                mTouchedView.setPressed(true);
                mClickListener.onClick(view);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mhandler.removeCallbacks(mHandlerRunnable);
                mTouchedView.setPressed(false);
                mTouchedView = null;
                return true;
        }
        return false;
    }
}
