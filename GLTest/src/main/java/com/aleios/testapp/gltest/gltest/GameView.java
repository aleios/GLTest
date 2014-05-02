package com.aleios.testapp.gltest.gltest;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.view.Window;
import android.content.res.AssetManager;
import android.view.WindowManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameView extends Activity
{
    private GLSurfaceView renderSurface = null;

    private GLRenderer renderer = null;

    public static AssetManager assetManager;

    MediaPlayer mp = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        assetManager = getAssets();

        // Remove window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if(Build.VERSION.SDK_INT < 16)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        renderSurface = new GLSurfaceView(this);
        renderSurface.setEGLContextClientVersion(2);
        renderSurface.setEGLConfigChooser(new MultisampleConfigChooser());

        final ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configInfo = activityManager.getDeviceConfigurationInfo();

        boolean isSupported = false;
        if(configInfo != null)
            isSupported = configInfo.reqGlEsVersion >= 0x20000;
        else // Unexpected failure as configInfo is null.
            return;

        Log.w("GameView", "Booting Opengl");

        if(isSupported)
        {

            renderer = new GLRenderer();
            renderSurface.setRenderer(renderer);

            Log.w("GameView", "Renderer Loaded");
        }
        else
        {
            Log.w("GameView", "!! GLES 2.0 not found! !!");
            return;
        }

        try
        {
            AssetFileDescriptor afd = getAssets().openFd("ftest.mp3");

            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
            mp.start();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        setContentView(renderSurface);

        /*
        AlertDialog.Builder dlgBuild = new AlertDialog.Builder(this);
        dlgBuild.setMessage("Nope").setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog dlg = dlgBuild.create();
        dlg.show();
        */
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        int action = ev.getAction();

        if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE)
        {
            int size = ev.getHistorySize();

            /*
            for(int i = 0; i < size; i++)
            {
                int[] coords = new int[2];
                renderSurface.getLocationInWindow(coords);

                renderer.MoveSquare(ev.getHistoricalX(i) - coords[0], ev.getHistoricalY(i) - coords[1]);
            }*/
            int[] coords = new int[2];
            renderSurface.getLocationInWindow(coords);
            renderer.MoveSquare(ev.getRawX() - coords[0], ev.getRawY() - coords[1]);
            return true;
        }
        return false;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(renderSurface != null && renderer != null)
            renderSurface.onResume();
        mp.start();

        if(renderer != null)
            renderer.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(renderSurface != null && renderer != null)
            renderSurface.onPause();
        mp.pause();
    }
}
