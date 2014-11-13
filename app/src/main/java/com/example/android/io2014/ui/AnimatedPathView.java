/**
 * Copyright 2014 Google
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.io2014.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.android.io2014.R;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class AnimatedPathView extends View {
    private static final String LOG_TAG = "AnimatedPathView";

    private final Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final SvgHelper mSvg = new SvgHelper(mStrokePaint);
    private int mSvgResource;

    private final Object mSvgLock = new Object();
    private List<SvgHelper.SvgPath> mPaths = new ArrayList<SvgHelper.SvgPath>(0);
    private Thread mLoader;
    private final Path mRenderPath = new Path();

    private float mPhase;
    private float mFillAlpha;
    private float mFadeFactor;
    private int mDuration;
    private int mFillDuration;
    private int mFillOffset;

    public AnimatedPathView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatedPathView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mStrokePaint.setStyle(Paint.Style.STROKE);
        mFillPaint.setStyle(Paint.Style.FILL);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimatedPathView, defStyle, 0);
        try {
            if (a != null) {
                mStrokePaint.setStrokeWidth(
                        a.getDimensionPixelSize(R.styleable.AnimatedPathView_strokeWidth, 1));
                mStrokePaint.setColor(
                        a.getColor(R.styleable.AnimatedPathView_strokeColor, 0xff000000));
                mFillPaint.setColor(
                        a.getColor(R.styleable.AnimatedPathView_fillColor, 0xff000000));
                mPhase = a.getFloat(R.styleable.AnimatedPathView_phase, 0.0f);
                mDuration = a.getInt(R.styleable.AnimatedPathView_duration, 4000);
                mFillDuration = a.getInt(R.styleable.AnimatedPathView_fillDuration, 4000);
                mFillOffset = a.getInt(R.styleable.AnimatedPathView_fillOffset, 2000);
                mFadeFactor = a.getFloat(R.styleable.AnimatedPathView_fadeFactor, 10.0f);
                mSvgResource = a.getResourceId(R.styleable.AnimatedPathView_svgPath, 0);
            }
        } finally {
            if (a != null) a.recycle();
        }
    }

    public int getFillColor() {
        return mFillPaint.getColor();
    }

    public void setFillColor(int color) {
        mFillPaint.setColor(color);
    }

    public int getStrokeColor() {
        return mStrokePaint.getColor();
    }

    public void setStrokeColor(int color) {
        mStrokePaint.setColor(color);
    }

    public float getPhase() {
        return mPhase;
    }

    public void setPhase(float phase) {
        mPhase = phase;
        synchronized (mSvgLock) {
            updatePathsPhaseLocked();
        }
        invalidate();
    }

    public float getFillAlpha() {
        return mFillAlpha;
    }

    public void setFillAlpha(float fillAlpha) {
        mFillAlpha = fillAlpha;
        invalidate();
    }

    public int getSvgResource() {
        return mSvgResource;
    }

    public void setSvgResource(int svgResource) {
        mSvgResource = svgResource;
    }

    public void reveal() {
        ObjectAnimator svgAnimator = ObjectAnimator.ofFloat(this, "phase", 0.0f, 1.0f);
        svgAnimator.setDuration(mDuration);
        svgAnimator.start();

        setFillAlpha(0.0f);

        ObjectAnimator fillAnimator = ObjectAnimator.ofFloat(this, "fillAlpha", 0.0f, 1.0f);
        fillAnimator.setDuration(mFillDuration);
        fillAnimator.setStartDelay(mFillOffset);
        fillAnimator.start();
    }

    private void updatePathsPhaseLocked() {
        for (SvgHelper.SvgPath path : mPaths) {
            path.renderPath.reset();
            path.measure.getSegment(0.0f, mPhase * path.length, path.renderPath, true);
        }
    }

    @Override
    protected void onSizeChanged(final int w, final int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mLoader != null) {
            try {
                mLoader.join();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Unexpected error", e);
            }
        }

        mLoader = new Thread(new Runnable() {
            @Override
            public void run() {
                mSvg.load(getContext(), mSvgResource);
                synchronized (mSvgLock) {
                    mPaths = mSvg.getPathsForViewport(
                            w - getPaddingLeft() - getPaddingRight(),
                            h - getPaddingTop() - getPaddingBottom());
                    updatePathsPhaseLocked();
                }
            }
        }, "SVG Loader");
        mLoader.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (mSvgLock) {
            canvas.save();
            canvas.translate(getPaddingLeft(), getPaddingTop());
            mFillPaint.setAlpha((int) (mFillAlpha * 255.0f));
            for (SvgHelper.SvgPath path : mPaths) {
                int alpha = (int) (Math.min(mPhase * mFadeFactor, 1.0f) * 255.0f);
                path.paint.setAlpha(alpha);

                canvas.drawPath(path.path, mFillPaint);
                canvas.drawPath(path.renderPath, path.paint);
            }
            canvas.restore();
        }
    }
}
