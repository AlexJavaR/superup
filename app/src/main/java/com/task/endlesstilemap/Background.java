package com.task.endlesstilemap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Background extends SurfaceView implements SurfaceHolder.Callback {
    private static final int INVALID_POINTER_ID = -1;

    private BitmapDrawable mDrawable;
    private float mPosX;
    private float mPosY;

    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId = INVALID_POINTER_ID;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    private DrawThread drawThread;

    public Background(Context context) {
        this(context, null, 0);
        mDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.tile_single);
        SurfaceHolder holder = getHolder();
        getHolder().addCallback(this);
        drawThread = new DrawThread(holder);
    }

    public Background(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
//        SurfaceHolder holder = getHolder();
//        getHolder().addCallback(this);
//        mDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.tile_single);
//        mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
//        drawThread = new DrawThread(holder);
    }

    public Background(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(getHolder());
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        drawThread.setRunning(false);
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    class DrawThread extends Thread {

        private boolean running = false;
        private SurfaceHolder surfaceHolder;

        public DrawThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            Canvas canvas;
            while (running) {
                canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                    if (canvas == null)
                        continue;
                    synchronized (surfaceHolder) {
                        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                        draw(canvas);
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        public void draw(Canvas canvas) {
            Display display = getDisplay();
            Point size = new Point();
            display.getSize(size);
            int displayX = size.x;
            int displayY = size.y;
            mDrawable.setBounds(0, 0, displayX, displayY);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(mDrawable.getBitmap(), displayX, displayY, false);
            //canvas.save();
            Log.d("DEBUG", "X: " + mPosX + " Y: " + mPosY);
            Rect rect = mDrawable.getBounds();
            int tileX = rect.width();
            int tileY = rect.height();
            //Rect rect = mImage.getBounds();
            Log.d("myDEBUG", "tileX: " + String.valueOf(tileX) + " tileY: " + String.valueOf(tileY));
            Log.d("myDEBUG", "LastTouchX: " + mLastTouchX + " mLastTouchY: " + mLastTouchY);
            Log.d("displayCoordinates", "displayX: " + displayX + " displayY: " + displayY);

            int i = 1;
            int j = 1;

            if (mPosX <= displayX && mPosY <= displayY) {
                i = -1;
                j = -1;
            }

            if (mPosX > displayX && mPosY > displayY) {
                i = 1;
                j = 1;
            }

            if (mPosX <= displayX && mPosY >= displayY) {
                i = -1;
                j = 1;
            }

            if (mPosX > displayX && mPosY < displayY) {
                i = 1;
                j = -1;
            }

            for (float startX = i*displayX; startX <= displayX + i*mPosX; startX += displayX) {
                for (float startY = j*displayY; startY <= displayY + j*mPosY; startY += displayY) {
                    canvas.save();
                    canvas.scale(mScaleFactor, mScaleFactor);
                    canvas.drawBitmap(resizedBitmap, mPosX - i*startX, mPosY - j*startY, null);
                    canvas.restore();
                }
            }


        }
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

                    //invalidate();
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


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 10.0f));

            //invalidate();
            return true;
        }
    }
}
