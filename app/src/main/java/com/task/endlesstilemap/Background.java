package com.task.endlesstilemap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class Background extends View {

    //Object for tile map
    private Bitmap[][] arrayTiles;

    //Coordinates of the map camera
    private float mPosX;
    private float mPosY;

    //Width and height of tile map
    private float tileWidth;
    private float tileHeight;

    //Coordinates of the last touch of the screen
    private float mLastTouchX;
    private float mLastTouchY;

    // The ‘active pointer’ is the one currently moving our object.
    private int mActivePointerId = INVALID_POINTER_ID;

    //Width, height and diagonal of display
    private int displayX;
    private int displayY;
    private float diagonalScreen;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    private Matrix mImageMatrix = new Matrix();

    //Last rotation angle
    private int mLastAngle = 0;

    Bitmap bitmap;

    public Background(Context context, Bitmap[][] arrayTiles, int tileWidth, int tileHeight, int startX, int startY) {
        this(context, null, 0);
        this.arrayTiles = arrayTiles;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        mPosX = startX;
        mPosY = startY;
        bitmap = getBitmapFromArray(arrayTiles, tileWidth, tileHeight);
    }

    public Background(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Background(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    //create Bitmap from array tiles
    public Bitmap getBitmapFromArray(Bitmap[][] arrayTiles, int tileWidth, int tileHeight) {
        int lengthArray = arrayTiles.length;
        Bitmap bitmap = Bitmap.createBitmap(lengthArray * tileWidth, lengthArray * tileHeight, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(bitmap);
        for (int i = 0; i < arrayTiles.length; i++) {
            for (int j = 0; j < arrayTiles.length; j++) {
                comboImage.drawBitmap(arrayTiles[i][j], i * tileWidth, j * tileHeight, null);
            }
        }
        diagonalScreen = (float) Math.sqrt(bitmap.getWidth()*bitmap.getWidth() + bitmap.getHeight()*bitmap.getHeight());
        return bitmap;
    }

    /**
     * Method of tiled map rendering.
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
        tileWidth = bitmap.getWidth();
        tileHeight = bitmap.getHeight();
        Log.d("myDEBUG", "X: " + mPosX + " Y: " + mPosY);
        Log.d("myDEBUG", "tileX: " + tileWidth + " tileY: " + tileHeight);
        Log.d("myDEBUG", "displayX: " + displayX + " displayY: " + displayY);
        Log.d("myDEBUG", "mScaleFactor: " + mScaleFactor);

        //Coefficients for different quadrants
        int i = 1;
        int j = 1;

        if (mPosX >= tileWidth && mPosY >= tileHeight) {
            i = 1;
            j = 1;
        }

        if (mPosX < tileWidth && mPosY < tileHeight) {
            i = -1;
            j = -1;
        }

        if (mPosX < tileWidth && mPosY > tileHeight) {
            i = -1;
            j = 1;
        }

        if (mPosX > tileWidth && mPosY < tileHeight) {
            i = 1;
            j = -1;
        }

        //The amount of tiles is changed if the scale changes
        if (mScaleFactor <= 1) {
            scale = mScaleFactor;
        } else {
            scale = 1/mScaleFactor;
        }

        //Tiled map rendering with provision for scale and rotation
        for (float startX = -tileWidth/scale; startX <= diagonalScreen/scale + i*mPosX; startX += tileWidth) {
            for (float startY = -tileHeight/scale; startY <= diagonalScreen/scale + j*mPosY; startY += tileHeight) {
                canvas.save();
                canvas.setMatrix(mImageMatrix);
                canvas.scale(mScaleFactor, mScaleFactor);
                canvas.drawBitmap(bitmap, mPosX - i*startX, mPosY - j*startY, null);
                canvas.restore();
            }
        }
    }

    /**
     * Handle touch screen motion events
     *
     * @param ev The motion event.
     * @return True if the event was handled.
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

                    mPosX += dx / mScaleFactor;// * Math.cos(mLastAngle + Math.atan2(tileHeight, tileWidth));
                    mPosY += dy / mScaleFactor;// * Math.sin(mLastAngle + Math.atan2(tileHeight, tileWidth));

                    invalidate();
                }

                if (ev.getPointerCount() >= 2) {
                    doRotationEvent(ev);
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

    /**
     * A class to listen for a subset of scaling-related events.
     */

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // ScaleGestureDetector calculates a scale factor based on whether
            // the fingers are moving apart or together
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.2f, Math.min(mScaleFactor, 2.0f));

            invalidate();
            return true;
        }
    }

    /**
     * Operate on two-finger events to rotate the image.
     * This method calculates the change in angle between the
     * pointers and rotates the image accordingly.  As the user
     * rotates their fingers, the image will follow.
     *
     * @param event The motion event.
     */

    private void doRotationEvent(MotionEvent event) {
        //Calculate the angle between the two fingers
        float deltaX = event.getX(0) - event.getX(1);
        float deltaY = event.getY(0) - event.getY(1);
        double radians = Math.atan(deltaY / deltaX);
        Log.d("myDEBUG", "radians: " + radians);
        //Convert to degrees
        int degrees = (int) (radians * 180 / Math.PI);
        Log.d("myDEBUG ", "degrees: " + degrees);

        // Returns a converted value between -90deg and +90deg
        // which creates a point when two fingers are vertical where the
        // angle flips sign.  We handle this case by rotating a small amount
        // (5 degrees) in the direction we were traveling
        Log.d("myDEBUG", "mLastAngle: " + mLastAngle);
        if ((degrees - mLastAngle) > 45) {
            //Going CCW across the boundary
            mImageMatrix.postRotate(-5, displayX / 2, displayY / 2);
        } else if ((degrees - mLastAngle) < -45) {
            //Going CW across the boundary
            mImageMatrix.postRotate(5, displayX / 2, displayY / 2);
        } else {
            //Normal rotation, rotate the difference
            mImageMatrix.postRotate(degrees - mLastAngle, displayX / 2, displayY / 2);
        }

        //Save the current angle
        mLastAngle = degrees;
    }
}
