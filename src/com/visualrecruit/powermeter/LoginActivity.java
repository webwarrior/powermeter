package com.visualrecruit.powermeter;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class LoginActivity extends Activity implements SurfaceHolder.Callback{

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button okButton;
    private Button button_capture;
    private String _username;
    private String _password;
    private static final String PREFS_NAME = "powermeter.dat";
    private SurfaceView preview = null;
    private SurfaceHolder previewHolder=null;
    private Camera camera = null;
    private boolean inPreview=false;
    private boolean cameraConfigured=false;
    private MediaRecorder mMediaRecorder;
    private Boolean mInitSuccesful = Boolean.FALSE;
    private final String VIDEO_PATH_NAME = "/mnt/sdcard/VGA_30fps_512vbrate.mp4";
    private Boolean isRecording = Boolean.FALSE;
    private static final String TAG = "LoginActivity";
    private int[] pixels;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        preview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(this);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        //camera=Camera.open();
        //startPreview();

        usernameEditText = (EditText) findViewById(R.id.user_name);
        passwordEditText = (EditText) findViewById(R.id.password);

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        _username = settings.getString("username", "");
        _password = settings.getString("password", "");



//        if((_username != null) && (_username.length() >0 ) && (_password != null) && (_password.length() > 0))
//        {
//            Intent activity = new Intent(LoginActivity.this, BrowserActivity.class);
//            activity.putExtra("username", usernameEditText.getText().toString());
//            activity.putExtra("password", passwordEditText.getText().toString());
//            startActivity(activity);
//        }



        okButton = (Button)findViewById(R.id.ok_buton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // We need an Editor object to make preference changes.
                // All objects are from android.context.Context
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("username", usernameEditText.getText().toString());
                editor.putString("password", passwordEditText.getText().toString());

                // Commit the edits!
                editor.commit();

                //activity.putExtra("username", usernameEditText.getText().toString());
                //activity.putExtra("password", passwordEditText.getText().toString());

                //startActivity(activity);
            }
        });

        button_capture = (Button)findViewById(R.id.button_capture);
        button_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //camera=Camera.open();
                //startPreview();
                   //camera.startPreview();
//                camera.takePicture(null, null, new  Camera.PictureCallback() {
//                    @Override
//                    public void onPictureTaken(byte[] data, Camera camera) {
//
//                    }
//                });


                if(isRecording == Boolean.FALSE)
                {
                    mMediaRecorder.start();
//                    try {
//                        Thread.sleep(10 * 1000); // This will recode for 10 seconds, if you don't want then just remove it.
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    isRecording = Boolean.TRUE;
                    //camera.startPreview();


                    //finish();
                }else
                {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();

                    try
                    {
                        initRecorder(previewHolder.getSurface());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isRecording = Boolean.FALSE;
                }
            }
        });

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


    private void initRecorder(Surface surface) throws IOException
    {
        // It is very important to unlock the camera before doing setCamera
        // or it will results in a black preview
        if(camera == null) {
            camera = Camera.open(0);

            camera.setPreviewCallback(new Camera.PreviewCallback()
            {
                public void onPreviewFrame(byte[] _data, Camera _camera) {
                    Log.d("onPreviewFrame-surfaceChanged",String.format("Got %d bytes of camera data", _data.length));
                }
            });

            Camera.Parameters parameters = camera.getParameters();
            parameters.set("orientation", "portrait");
            parameters.setRotation(270);
            pixels = new int [(parameters.getPreviewSize().height * parameters.getPreviewSize().width)];
            int pixelBuffer = (parameters.getPreviewSize().height * parameters.getPreviewSize().width);
            camera.setParameters(parameters);

            camera.addCallbackBuffer(new byte[pixelBuffer]);
            camera.addCallbackBuffer(new byte[pixelBuffer]);
            camera.addCallbackBuffer(new byte[pixelBuffer]);
            camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                private long timestamp=0;
                public synchronized void onPreviewFrame(byte[] data, Camera camera) {
                    Log.v("CameraTest","Time Gap = "+(System.currentTimeMillis()-timestamp));
                    timestamp=System.currentTimeMillis();
                    try{
                        camera.addCallbackBuffer(data);
                    }catch (Exception e) {
                        Log.e("CameraTest", "addCallbackBuffer error");
                        return;
                    }
                    return;
                }
            });

            camera.setPreviewDisplay(previewHolder);
            camera.startPreview();

            camera.unlock();

        }

        if(mMediaRecorder == null)  mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setPreviewDisplay(surface);
        mMediaRecorder.setCamera(camera);

        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        //       mMediaRecorder.setOutputFormat(8);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setVideoEncodingBitRate(256 * 1000);
        mMediaRecorder.setVideoFrameRate(15);
        mMediaRecorder.setVideoSize(640, 480);
        mMediaRecorder.setOutputFile(VIDEO_PATH_NAME);
        mMediaRecorder.setOrientationHint(90);

        try
        {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e)
        {
            // This is thrown if the previous calls are not called with the
            // proper order
            e.printStackTrace();
        }

        mInitSuccesful = Boolean.TRUE;

    }


    private void shutdown() {
        // Release MediaRecorder and especially the Camera as it's a shared
        // object that can be used by other applications
        mMediaRecorder.reset();
        mMediaRecorder.release();
        camera.release();

        // once the objects have been released they can't be reused
        mMediaRecorder = null;
        camera = null;
    }

    @Override
    public void onPause() {
        if (inPreview) {
            camera.stopPreview();
        }

//        camera.release();
//        camera=null;
//        inPreview=false;

        super.onPause();
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width<=width && size.height<=height) {
                if (result==null) {
                    result=size;
                }
                else {
                    int resultArea=result.width*result.height;
                    int newArea=size.width*size.height;

                    if (newArea>resultArea) {
                        result=size;
                    }
                }
            }
        }

        return(result);
    }

    private void initPreview(int width, int height) {
        if (camera!=null && previewHolder.getSurface()!=null) {
            try {
                camera.setPreviewDisplay(previewHolder);
            }
            catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback",
                        "Exception in setPreviewDisplay()", t);
                Toast
                        .makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }

            if (!cameraConfigured) {
                Camera.Parameters parameters=camera.getParameters();
                Camera.Size size=getBestPreviewSize(width, height,
                        parameters);

                if (size!=null) {
                    parameters.setPreviewSize(size.width, size.height);
                    camera.setParameters(parameters);
                    cameraConfigured=true;
                }
            }
        }
    }

    private void startPreview() {
        //if (cameraConfigured && camera!=null) {
            camera.startPreview();
            inPreview=true;
        //}
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.d(TAG,"Created surface");
        try {
            if(!mInitSuccesful)
                initRecorder(previewHolder.getSurface());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG,"surfaceChanged");
         if(camera != null)
         {
             Log.d(TAG,"here I aam");
         }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        shutdown();
    }

//    SurfaceHolder.Callback surfaceCallback= new SurfaceHolder.Callback() {
//        public void surfaceCreated(SurfaceHolder holder) {
//            // no-op -- wait until surfaceChanged()
//        }
//
//        public void surfaceChanged(SurfaceHolder holder,
//                                   int format, int width,
//                                   int height) {
//            initPreview(width, height);
//            //startPreview();
//        }
//
//
//    };


}
