

package com.srima.bb;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;



public class CheckableLinearLayout extends LinearLayout implements Checkable {
    private boolean mChecked;

    private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    public void toggle() {
        setChecked(!mChecked);
    }
    
    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            int s = getChildCount();
            for (int i = 0; i != s; ++i) {
                View v = getChildAt(i);
                if (v instanceof Checkable) {
                    Checkable c = (Checkable)v;
                    c.setChecked(mChecked);
                }
            }
            refreshDrawableState();
        }
    }
}

