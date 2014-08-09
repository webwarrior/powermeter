package com.visualrecruit.powermeter;

import android.app.Activity;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by Shaun on 26/07/2014.
 */
public class BrowserActivity extends Activity {
    private static final String TAG = "YUV420SP";
    private static final String FORMAT_FPS = "YUV420SP->ARGB %d fps\nAve. %.3fms\nmin %.3fms max %.3fms";
    private static int PREVIEW_WIDTH = 800;
    private static int PREVIEW_HEIGHT = 600;
    // private int mSurfaceWidth = 640;
    // private int mSurfaceHeight = 480;

    private SurfaceView mPreviewSurfaceView;
    private SurfaceView mFilterSurfaceView;
    private Camera mCamera;

    // for filter
    // YUV420SP→ARGB
    private int[] mRGBData = new int[PREVIEW_WIDTH * PREVIEW_HEIGHT];
    private Paint mPaint = new Paint();

    // for fps
    private TextView mFpsTextView;
    private long mSumEffectTime;
    private long mMinEffectTime = 0;
    private long mMaxEffectTime = 0;
    private long mFrames;
    private long mPrivFrames;
    private String mFpsString;
    private Timer mFpsTimer;

    /**
     * Camera Previewの流し込み先SurfaceViewのCallback
     */
    private SurfaceHolder.Callback mPreviewSurfaceListener = new SurfaceHolder.Callback() {

        public void surfaceCreated(SurfaceHolder holder) {
            // Back Camera
            // Back Camera Preview
            mCamera = Camera.open(0);

            if (mCamera != null) {
                mCamera.setDisplayOrientation(90);
                //mCamera.enableShutterSound(false);
                mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
                try {
                    mCamera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // mSurfaceWidth = width;
            // mSurfaceHeight = height;
            if (mCamera != null) {
                // init preview
                mCamera.stopPreview();
                mCamera.setDisplayOrientation(90);
                Camera.Parameters params = mCamera.getParameters();
                //params.set("orientation", "portrait");
                //params.setRotation(90);
                // VGA
                params.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
                mCamera.setParameters(params);
                mCamera.startPreview();

                // Android Camera
                mCamera.addCallbackBuffer(new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 3 / 2]);
            }

//            // start timer
//            mFrames = 0;
//            mPrivFrames = 0;
//            mSumEffectTime = 0;
//            mFpsTimer = new Timer();
//            mFpsTimer.scheduleAtFixedRate(new TimerTask() {
//                @Override
//                public void run() {
//                    // nano sec.
//                    if ((mPrivFrames > 0) && (mSumEffectTime > 0)) {
//                        long frames = mFrames - mPrivFrames;
//                        mFpsString = String.format(FORMAT_FPS, frames,
//                                ((double) mSumEffectTime) / (frames * 1000000.0),
//                                ((double) mMinEffectTime) / (1000000.0), ((double) mMaxEffectTime) / (1000000.0));
//                        mSumEffectTime = 0;
//                        mMinEffectTime = 0;
//                        mMaxEffectTime = 0;
//                        runOnUiThread(new Runnable() {
//                            public void run() {
//                                mFpsTextView.setText(mFpsString);
//                            }
//                        });
//                    }
//                    mPrivFrames = mFrames;
//                }
//            }, 0, 1000); // 1000ms periodic
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // stop timer
            if (mFpsTimer != null) {
                mFpsTimer.cancel();
                mFpsTimer = null;
            }

            // deinit preview
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewCallbackWithBuffer(null);
                mCamera.release();
                mCamera = null;
            }
        }

    };

    /**
     * Camera Previewの取得Callback。
     */
    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {

        public void onPreviewFrame(byte[] data, Camera camera) {
            if (camera != null) {
                camera.addCallbackBuffer(data);
            }

            // YUV420SP→ARGB
            long before = System.nanoTime();
            // JavaFilter.decodeYUV420SP(mRGBData, data, PREVIEW_WIDTH, PREVIEW_HEIGHT);
            // call JNI method.
            decodeYUV420SP(mRGBData, data, PREVIEW_WIDTH, PREVIEW_HEIGHT);
            long after = System.nanoTime();
            updateEffectTimes(after - before);

            // int[] ARGB
            if (mFilterSurfaceView != null) {
                SurfaceHolder holder = mFilterSurfaceView.getHolder();
                Canvas canvas = holder.lockCanvas();
                Bitmap _buffer;
                // canvas.save();
                // canvas.scale(mSurfaceWidth / PREVIEW_WIDTH, mSurfaceHeight / PREVIEW_HEIGHT);
                _buffer = Bitmap.createBitmap(mRGBData, PREVIEW_WIDTH, PREVIEW_HEIGHT, Bitmap.Config.RGB_565);
                int pixel =_buffer.getPixel(50,50);
                int a = Color.alpha(pixel);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                String color = String.format("#%02X%02X%02X%02X", a, r, g, b); //#FFFF0000 for RED color

                //mFpsTextView.setText(mFpsTextView.getText().toString() + "\n" + color);
                mFpsTextView.setText("Color: " + color);

                //canvas.drawBitmap(mRGBData, 0, PREVIEW_WIDTH, 0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT, false, mPaint);
                //canvas.rotate(90);
                // canvas.restore();
                holder.unlockCanvasAndPost(canvas);
                //holder.
                mFrames++;
            }
        }
    };

    /**
     *
     * @param elapsed time
     */
    private void updateEffectTimes(long elapsed) {
        if (elapsed <= 0) {
            return;
        }
        if ((mMinEffectTime == 0) || (elapsed < mMinEffectTime)) {
            mMinEffectTime = elapsed;
        }
        if ((mMaxEffectTime == 0) || (mMaxEffectTime < elapsed)) {
            mMaxEffectTime = elapsed;
        }
        mSumEffectTime += elapsed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_browser_activity);

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        deinit();
    }

    @SuppressWarnings("deprecation")
    private void init() {
        mPreviewSurfaceView = (SurfaceView) findViewById(R.id.preview_surface);
        SurfaceHolder holder = mPreviewSurfaceView.getHolder();
        holder.addCallback(mPreviewSurfaceListener);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        mFilterSurfaceView = (SurfaceView) findViewById(R.id.filter_surface);
        mFilterSurfaceView.setZOrderOnTop(true);
        mFpsTextView = (TextView) findViewById(R.id.fps_text);
    }

    private void deinit() {
        SurfaceHolder holder = mPreviewSurfaceView.getHolder();
        holder.removeCallback(mPreviewSurfaceListener);

        mPreviewSurfaceView = null;
        mFilterSurfaceView = null;
        mFpsTextView = null;
    }

    //Method from Ketai project! Not mine! See below...
    void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {       int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)                  r = 0;               else if (r > 262143)
                    r = 262143;
                if (g < 0)                  g = 0;               else if (g > 262143)
                    g = 262143;
                if (b < 0)                  b = 0;               else if (b > 262143)
                    b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

}
