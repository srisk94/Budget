

package com.srima.bb;

public interface ColorFragment {

    public static interface OnColorChangeListener {
        public void onColorChange(int color);
    };

    abstract public int getColor();
};

