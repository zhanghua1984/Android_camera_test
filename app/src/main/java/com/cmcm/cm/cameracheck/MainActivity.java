package com.cmcm.cm.cameracheck;


import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private AutoFitTextureView mCamView;
    private Camera mCamera;
    private static final int PREVIEW_W = 640;
    private static final int PREVIEW_H = 480;


    private boolean mIsFirstFrameAvailable;
    private long mStartTime;

    private SurfaceTexture mTexture;

    private TextView mTextView;


    private int mCameraId = 0;

    private boolean mHasTwoCam;

    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCamView = (AutoFitTextureView) findViewById(R.id.cam_view);
        mCamView.setSurfaceTextureListener(this);

        btn = (Button) findViewById(R.id.button);

        mCamView.setAspectRatio(PREVIEW_W, PREVIEW_H);

        mTextView = (TextView) findViewById(R.id.tv_cam_nums);

        mHasTwoCam = Camera.getNumberOfCameras() > 1;

        if (!mHasTwoCam) {
            btn.setText("只有一个摄像头");
            btn.setEnabled(false);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public void switchCam(View v) {
        mCameraId++;
        try {
            closeCamera();
            openCamera(mCameraId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mCameraId >= 1) {
            mCameraId = -1;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    private void openCamera(int camId) throws IOException {
        if (mCamera == null) {
            mCamera = Camera.open(camId);
        }
        Camera.Parameters parameters = mCamera.getParameters();

        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : sizes) {
            Log.d("", "SupportedPreviewSize->" + size.width + "x" + size.height);
        }

        parameters.setPreviewSize(PREVIEW_W, PREVIEW_H);
        mCamera.setParameters(parameters);
        mCamera.setPreviewTexture(mTexture);
        mCamera.startPreview();
        if(mCameraId==1)
        {
            mTextView.setText("摄像头ID:" + mCameraId + "秤盘");
        }
        else
        {
            mTextView.setText("摄像头ID:" + mCameraId + "人脸");
        }
        mTextView.setTextColor(Color.RED);
        mTextView.setTextSize(150);
        mTextView.setAlpha((float) 0.5);
    }

    private void closeCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d(TAG, "onSurfaceTextureAvailable:" + surfaceTexture.toString());

        if (mTexture == null) {
            mTexture = surfaceTexture;
            Log.d(TAG, "the first surfaceTexture");
            try {
                openCamera(mCameraId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        closeCamera();
        return true;
    }


    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        /*synchronized (mFrameLock){
            mFrameLock.notify();
        }*/
        if (!mIsFirstFrameAvailable) {
            mIsFirstFrameAvailable = true;
            long openCamCostT = System.currentTimeMillis() - mStartTime;
            Log.d(TAG, "open first camera cost :" + (openCamCostT / 1000f) + "s");
        }
    }


}
