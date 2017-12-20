package com.secuchat.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by michael on 10/24/14.
 */

// Custom linear layout to detect keyboard. We need to scroll if keyboard opens
public class ResizeView extends LinearLayout {
    public ResizeView(Context context) {
        super(context);
    }

    public ResizeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    ResizeListener resizeListener;
    public void setSizeListener(ResizeListener resizeListener){
        this.resizeListener = resizeListener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(resizeListener!=null) resizeListener.viewSizeChanged(w,h,oldw,oldh);
    }
}
