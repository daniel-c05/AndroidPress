package com.thoughts.apps.reader.ui;

import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Daniel on 8/22/13.
 * This ViewPager only scrolls sideways, and not vertically
 */
public class HorizontalViewPager extends ViewPager {

    public HorizontalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean ret = super.onInterceptTouchEvent(ev);
        if (ret)
            getParent().requestDisallowInterceptTouchEvent(true);
        return ret;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean ret = super.onTouchEvent(ev);
        if (ret)
            getParent().requestDisallowInterceptTouchEvent(true);
        return ret;
    }
}