package com.visualrecruit.powermeter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.widget.Toast;


public class LoginActivity extends Activity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button okButton;
    private Button button_capture;
    private String _username;
    private String _password;
    private static final String PREFS_NAME = "powermeter.dat";
    private SurfaceView preview=null;
    private SurfaceHolder previewHolder=null;
    private Camera camera=null;
    private boolean inPreview=false;
    private boolean cameraConfigured=false;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        preview=(SurfaceView)findViewById(R.id.preview);
        previewHolder=preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        camera=Camera.open();
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
                startPreview();
            }
        });


    }

    @Override
    public void onPause() {
        if (inPreview) {
            camera.stopPreview();
        }

        camera.release();
        camera=null;
        inPreview=false;

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
        if (cameraConfigured && camera!=null) {
            camera.startPreview();
            inPreview=true;
        }
    }

    SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            // no-op -- wait until surfaceChanged()
        }

        public void surfaceChanged(SurfaceHolder holder,
                                   int format, int width,
                                   int height) {
            initPreview(width, height);
            startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };


}
