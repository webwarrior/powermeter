package com.visualrecruit.powermeter;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.SharedPreferences;


//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.ActivityInfo;
//import android.graphics.ImageFormat;
//import android.media.MediaRecorder;
//import android.os.Bundle;
//import android.os.Environment;
//import android.util.Log;
//import android.view.*;
//import android.widget.Button;
//import android.widget.EditText;
//import android.content.SharedPreferences;
//import android.hardware.Camera;
//import android.widget.Toast;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.List;

public class LoginActivity extends Activity{

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button okButton;
    private Button button_capture;
    private String _username;
    private String _password;
    private static final String PREFS_NAME = "powermeter.dat";
    //private SurfaceView preview = null;
    //private SurfaceHolder previewHolder=null;
    //private Camera camera = null;
    //private boolean inPreview=false;
    //private boolean cameraConfigured=false;
    //private MediaRecorder mMediaRecorder;
    //private Boolean mInitSuccesful = Boolean.FALSE;
    //private final String VIDEO_PATH_NAME = "/mnt/sdcard/VGA_30fps_512vbrate.mp4";
    //private Boolean isRecording = Boolean.FALSE;
    private static final String TAG = "LoginActivity";
    //private int[] pixels;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
            }
        });

        button_capture = (Button)findViewById(R.id.button_capture);
        button_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //camera=Camera.open();
                //startPreview();

                Intent activity = new Intent(LoginActivity.this, BrowserActivity.class);

                activity.putExtra("username", usernameEditText.getText().toString());
                activity.putExtra("password", passwordEditText.getText().toString());

                startActivity(activity);

            }
        });
    }



}
