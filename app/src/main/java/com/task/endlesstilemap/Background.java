package com.task.endlesstilemap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class Background extends View  {
    private static final int INVALID_POINTER_ID = -1;

    private Drawable mImage;
    private Bitmap mBitmap;
    private BitmapDrawable mDrawable;
    private float mPosX;
    private float mPosY;

    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId = INVALID_POINTER_ID;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    public Background(Context context) {
        this(context, null, 0);
        //mBitmap = loadBitmap();
        mDrawable = new BitmapDrawable(context.getResources(), mBitmap);
        mDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.tile_single);
        mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
        //mImage = getResources().getDrawable(R.drawable.tilemap);
        //mImage.setBounds(0, 0, mImage.getIntrinsicWidth(), mImage.getIntrinsicHeight());
    }

    public Background(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Background(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();

                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                    invalidate();
                }

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Display display = getDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayX = size.x;
        int displayY = size.y;

        //canvas.save();
        Log.d("DEBUG", "X: " + mPosX + " Y: " + mPosY);
        Rect rect = mDrawable.getBounds();
        int tileX = rect.width();
        int tileY = rect.height();
        //Rect rect = mImage.getBounds();
        Log.d("myDEBUG", "tileX: " + String.valueOf(tileX) + " tileY: " + String.valueOf(tileY));
        Log.d("myDEBUG", "LastTouchX: " + mLastTouchX + " mLastTouchY: " + mLastTouchY);
        Log.d("displayCoordinates", "displayX: " + displayX + " displayY: " + displayY);
        //if (mPosX <= 0 && mPosY <=0) {
            for (int startX = 0; startX < displayX - mPosX; startX += tileX) {
                for (int startY = 0; startY < displayY - mPosY; startY += tileY) {
                    canvas.save();
                    canvas.translate(mPosX + startX, mPosY + startY);
                    mDrawable.draw(canvas);
                    canvas.restore();
                }
            }
        //}

        //if (mPosX >= 0 && mPosY >=0) {
            for (int startX = 0; startX < displayX + mPosX; startX += tileX) {
                for (int startY = 0; startY < displayY + mPosY; startY += tileY) {
                    canvas.save();
                    canvas.translate(mPosX - startX, mPosY - startY);
                    mDrawable.draw(canvas);
                    canvas.restore();
                }
            }
        //}

//            int startX = 0;
//            int startY = 0;
//            while (startX < displayX - mPosX || startY < displayY - mPosY) {
//                int plusTileY = 0;
//                int plusTileX = 0;
//                while (startX < displayX - mPosX) {
//                    canvas.save();
//                    canvas.translate(mPosX + startX, mPosY);
//                    mDrawable.draw(canvas);
//                    canvas.restore();
//                    canvas.save();
//                    canvas.translate(mPosX + startX, mPosY + plusTileY);
//                    mDrawable.draw(canvas);
//                    canvas.restore();
//                    startX += tileX;
//                    plusTileY += tileY;
//                }
//                while (startY < displayY - mPosY) {
//                    canvas.save();
//                    canvas.translate(mPosX, mPosY + startY);
//                    mDrawable.draw(canvas);
//                    canvas.restore();
//                    canvas.save();
//                    canvas.translate(mPosX + plusTileX, mPosY + startY);
//                    mDrawable.draw(canvas);
//                    canvas.restore();
//                    startY += tileY;
//                    plusTileX +=tileX;
//                }
//            }


        if (mPosX > 0 && mPosY > 0) {
            int startX = 0;
            int startY = 0;
            while (startX > displayX - mPosX || startY < mPosY - displayY) {
                if (startX < mPosX - displayX) {
                    canvas.save();
                    canvas.translate(mPosX + startX, mPosY);
                    mDrawable.draw(canvas);
                    canvas.restore();
                    startX += rect.width();
                }
                if (startY < mPosY - displayX) {
                    canvas.save();
                    canvas.translate(mPosX, mPosY + startY);
                    mDrawable.draw(canvas);
                    canvas.restore();
                    startY += rect.height();
                }
            }
        }

//        canvas.translate(mPosX, mPosY);
//        //canvas.scale(mScaleFactor, mScaleFactor);
//        mDrawable.draw(canvas);
//        canvas.restore();
//
//        canvas.save();
//        canvas.translate(mPosX, mPosY + rect.centerY()*2);
//        //canvas.scale(mScaleFactor, mScaleFactor);
//        mDrawable.draw(canvas);
//        canvas.restore();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            //mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            invalidate();
            return true;
        }
    }
}
