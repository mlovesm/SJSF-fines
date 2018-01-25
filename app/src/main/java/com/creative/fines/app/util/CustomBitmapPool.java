package com.creative.fines.app.util;

import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

/**
 * Created by GS on 2016-10-19.
 */
public class CustomBitmapPool implements BitmapPool {
    @Override
    public int getMaxSize() {
        return 0;
    }

    @Override
    public void setSizeMultiplier(float sizeMultiplier) {

    }

    @Override
    public boolean put(Bitmap bitmap) {
        return false;
    }

    @Override
    public Bitmap get(int width, int height, Bitmap.Config config) {
        return null;
    }

    @Override
    public Bitmap getDirty(int width, int height, Bitmap.Config config) {
        return null;
    }

    @Override
    public void clearMemory() {

    }

    @Override
    public void trimMemory(int level) {

    }
}
