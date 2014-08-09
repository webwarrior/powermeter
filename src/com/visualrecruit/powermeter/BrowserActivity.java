package com.visualrecruit.powermeter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Shaun on 26/07/2014.
 */
public class BrowserActivity extends Activity {
    /** Called when the activity is first created. */
    Camera _camera;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new CameraPreview(this));

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.d("camera", "onTouchEvent");

            Camera.Parameters param = _camera.getParameters();
            int imgformat = param.getPreviewFormat();
            int bitsperpixel = ImageFormat.getBitsPerPixel(imgformat);
            int byteperpixel = bitsperpixel / 8;
            Camera.Size camerasize = param.getPreviewSize();

            int frame_size = ((camerasize.width * camerasize.height) * bitsperpixel) / 8;

            Log.d("camera", "width=" + camerasize.width + " height=" + camerasize.height + " bitsperpixel=" + bitsperpixel + " byteperpixel=" + byteperpixel + " size= " + frame_size);

            byte[] frame = new byte[frame_size];
            _camera.addCallbackBuffer(frame);

        }

        return super.onTouchEvent(event);
    }

    private Camera.PreviewCallback mPreviewListener = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Log.d("camera", "onPreviewFrame size=" + data.length);

            outputFile(data);
            //_camera.addCallbackBuffer(data);
        }
    };
    private void outputFile(byte[] data) {
        File recFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cap.raw");
        try {
            recFile.delete();
            recFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("camera", "error1");
        }

        FileOutputStream out = null;
        try {
            out =  new FileOutputStream(recFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("camera", "error2");
        }
        Log.d("camera", "start write " + data.length);
        try {
            out.write(data);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            Log.d("camera", "err ");
            e1.printStackTrace();
        }
        Log.d("camera", "stop write " + data.length);


        try {
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder holder;

        CameraPreview(Context context) {
            super(context);
            holder = getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            configure( format,  width, height) ;
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            _camera.stopPreview();
            _camera.release();
        }

        public void surfaceCreated(SurfaceHolder holder) {
            _camera = Camera.open();
            try {
                _camera.setPreviewDisplay(holder);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            _camera.setPreviewCallbackWithBuffer(mPreviewListener);
            _camera.startPreview();


        }
        protected void setPictureFormat(int format) {
            Camera.Parameters params = _camera.getParameters();
            List<Integer> supported = params.getSupportedPictureFormats();
            if (supported != null) {
                for (int f : supported) {
                    if (f == format) {
                        params.setPreviewFormat(format);
                        _camera.setParameters(params);
                        break;
                    }
                }
            }
        }

        protected void setPreviewSize(int width, int height) {
            Camera.Parameters params = _camera.getParameters();
            List<Camera.Size> supported = params.getSupportedPreviewSizes();
            if (supported != null) {
                for (Camera.Size size : supported) {
                    if (size.width <= width && size.height <= height) {
                        params.setPreviewSize(size.width, size.height);
                        _camera.setParameters(params);
                        break;
                    }
                }
            }
        }
        public void configure(int format, int width, int height) {
            _camera.stopPreview();
            setPictureFormat(format);
            setPreviewSize(width, height);
            _camera.startPreview();
        }
    }

}
