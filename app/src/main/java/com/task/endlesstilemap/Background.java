package com.task.endlesstilemap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class Background extends View {
    private static final int INVALID_POINTER_ID = -1;

    private BitmapDrawable mDrawable;
    private float mPosX;
    private float mPosY;

    private float tileWidth;
    private float tileHeight;

    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId = INVALID_POINTER_ID;

    private int displayX;
    private int displayY;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    private Matrix mImageMatrix = new Matrix();
    /* Last Rotation Angle */
    private int mLastAngle = 0;
    private int degrees = 0;
    private int angle = 0;

    public Background(Context context) {
        this(context, null, 0);
        mDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.tile_single);
    }

    public Background(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Background(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */

    @Override
    public void draw(Canvas canvas) {
        float scale;
        Display display = getDisplay();
        Point size = new Point();
        display.getSize(size);
        displayX = size.x;
        displayY = size.y;
        mDrawable.setBounds(0, 0, displayX, displayY);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(mDrawable.getBitmap(), displayX, displayY, false);
        Rect rect = mDrawable.getBounds();
        tileWidth = rect.width();
        tileHeight = rect.height();
        Log.d("DEBUG", "X: " + mPosX + " Y: " + mPosY);
        Log.d("myDEBUG", "tileX: " + tileWidth + " tileY: " + tileHeight);
        Log.d("myDEBUG", "LastTouchX: " + mLastTouchX + " mLastTouchY: " + mLastTouchY);
        Log.d("displayCoordinates", "displayX: " + displayX + " displayY: " + displayY);
        Log.d("mScaleFactor", ": " + mScaleFactor);

        int i = 1;
        int j = 1;

        if (mPosX <= displayX && mPosY <= displayY) {
            i = -1;
            j = -1;
        }

        if (mPosX < displayX && mPosY > displayY) {
            i = -1;
            j = 1;
        }

        if (mPosX > displayX && mPosY > displayY) {
            i = 1;
            j = 1;
        }

        if (mPosX > displayX && mPosY < displayY) {
            i = 1;
            j = -1;
        }

        if (mScaleFactor <= 1) {
             scale = mScaleFactor*mScaleFactor;
        } else {
            scale = 1/mScaleFactor;
        }


        for (float startX = -tileWidth/scale; startX <= 2*tileWidth/scale + i*mPosX; startX += tileWidth) {
            for (float startY = -tileHeight/scale; startY <= 2*tileHeight/scale + j*mPosY; startY += tileHeight) {
                canvas.save();
                canvas.setMatrix(mImageMatrix);
                canvas.scale(mScaleFactor, mScaleFactor);
                canvas.drawBitmap(resizedBitmap, mPosX - i*startX, mPosY - j*startY, null);
                canvas.restore();
                //mImageMatrix.reset();
            }
        }
    }

    /**
     * Handle touch screen motion events
     *
     * @param ev The motion event.
     * @return True if the event was handled.
     *
     */

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Display display = getDisplay();
        Point size = new Point();
        display.getSize(size);
        displayX = size.x;
        displayY = size.y;

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

                    if (degrees != 0) {
                        mPosX += dx/mScaleFactor * Math.cos(angle);
                        mPosY += dy/mScaleFactor * Math.sin(angle);
                    } else {
                        mPosX += dx/mScaleFactor;
                        mPosY += dy/mScaleFactor;
                    }

                    invalidate();
                }

                mLastTouchX = x;
                mLastTouchY = y;
                if (ev.getPointerCount() >= 2) {
                    doRotationEvent(ev);
//                    //Calculate the angle between the two fingers
//                    float deltaX = ev.getX(0) - ev.getX(1);
//                    float deltaY = ev.getY(0) - ev.getY(1);
//                    double radians = Math.atan(deltaY / deltaX);
//                    //Convert to degrees
//                    int degrees = (int)(radians * 180 / Math.PI);
//                    if ((degrees - mLastAngle) > 45) {
//                        //Going CCW across the boundary
//                        mImageMatrix.postRotate(-5, tileWeight / 2, tileHeight / 2);
//                    } else if ((degrees - mLastAngle) < -45) {
//                        //Going CW across the boundary
//                        mImageMatrix.postRotate(5, tileWeight / 2, tileHeight / 2);
//                    } else {
//                        //Normal rotation, rotate the difference
//                        mImageMatrix.postRotate(degrees - mLastAngle, tileWeight / 2, tileHeight / 2);
//                    }
//                    //Post the rotation to the image
//
//                    //Save the current angle
//                    mLastAngle = degrees;
               }
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


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 2.0f));

            invalidate();
            return true;
        }
    }

    private void doRotationEvent(MotionEvent event) {
        //Calculate the angle between the two fingers
        float deltaX = event.getX(0) - event.getX(1);
        float deltaY = event.getY(0) - event.getY(1);
        double radians = Math.atan(deltaY / deltaX);
        Log.d("radians", ": " + radians);
        //Convert to degrees
        degrees = (int)(radians * 180 / Math.PI);
        Log.d("Degrees1111111111 ", ": " + degrees);
        /*
         * Must use getActionMasked() for switching to pick up pointer events.
         * These events have the pointer index encoded in them so the return
         * from getAction() won't match the exact action constant.
         */
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                //Mark the initial angle
                mLastAngle = degrees;
                break;
            case MotionEvent.ACTION_MOVE:
                // ATAN returns a converted value between -90deg and +90deg
                // which creates a point when two fingers are vertical where the
                // angle flips sign.  We handle this case by rotating a small amount
                // (5 degrees) in the direction we were traveling
                Log.d("Angle", ": " + mLastAngle);
                Log.d("Degrees ", ": " + degrees);
                angle = degrees - mLastAngle;
                if ((degrees - mLastAngle) > 45) {
                    //Going CCW across the boundary
                    mImageMatrix.postRotate(-5, tileWidth / 2, tileHeight / 2);
                } else if ((degrees - mLastAngle) < -45) {
                    //Going CW across the boundary
                    mImageMatrix.postRotate(5, tileWidth / 2, tileHeight / 2);
                } else {
                    //Normal rotation, rotate the difference
                    mImageMatrix.postRotate(degrees - mLastAngle, tileWidth / 2, tileHeight / 2);
                }
                //Post the rotation to the image

                //Save the current angle
                mLastAngle = degrees;
                break;
        }
    }
}
