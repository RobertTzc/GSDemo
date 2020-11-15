package com.dji.GSDemo.PathPlanning;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dji.GSDemo.GoogleMap.R;

import java.text.DecimalFormat;
import java.util.Date;

import dji.common.battery.BatteryState;
import dji.common.camera.SettingsDefinitions;
import dji.common.camera.SystemState;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.product.Model;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.battery.Battery;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.useraccount.UserAccountManager;

import static java.lang.Math.sqrt;

public class cameraView extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private static final String TAG = FpvActivity.class.getName();
    protected VideoFeeder.VideoDataListener mReceivedVideoDataListener = null;

    // Codec for video live view
    protected DJICodecManager mCodecManager = null;

    protected TextureView mVideoSurface = null;

    private Handler handler;

    private FlightController mFlightController;
    private Battery mBatteryStatus;
    DroneStatus droneStatus = new DroneStatus();
    private static DecimalFormat df = new DecimalFormat("0.00");
    Tools tool = new Tools();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);

        handler = new Handler();

        initUI();

        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataListener = new VideoFeeder.VideoDataListener() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };

        Camera camera = DJIDemoApplication.getCameraInstance();

        if (camera != null) {

            camera.setSystemStateCallback(new SystemState.Callback() {
                @Override
                public void onUpdate(SystemState cameraSystemState) {
                    if (null != cameraSystemState) {

                        int recordTime = cameraSystemState.getCurrentVideoRecordingTimeInSeconds();
                        int minutes = (recordTime % 3600) / 60;
                        int seconds = recordTime % 60;

                        final String timeString = String.format("%02d:%02d", minutes, seconds);
                        final boolean isVideoRecording = cameraSystemState.isRecording();

                        cameraView.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                displayDroneStatus();
                            }
                        });
                    }
                }
            });

        }
    }
    public void displayDroneStatus(){
        TextView tv_droneInfo;
        tv_droneInfo = findViewById(R.id.tv_droneInfo);
        tv_droneInfo.setText("Battery_info: "+String.valueOf(droneStatus.batteryPercentage)+"" +
                "\nSatellite count: "+String.valueOf(droneStatus.satelliteCount)+
                "\nSpeed_info: "+String.valueOf(df.format(droneStatus.droneSpeed))+"m/s"+
                "\nHeight: "+String.valueOf(df.format(droneStatus.droneHeight))+"m"+
                "\nDrone heading : "+ String.valueOf(droneStatus.droneHeading));
    }
    private void initFlightController() {
        BaseProduct product = DJIDemoApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
                mBatteryStatus = ((Aircraft)product).getBattery();

            }
        }
        if (mBatteryStatus!=null){
            mBatteryStatus.setStateCallback(new BatteryState.Callback() {
                @Override
                public void onUpdate(BatteryState batteryState) {
                    droneStatus.batteryPercentage = batteryState.getChargeRemainingInPercent();
                }
            });
        }
        if (mFlightController != null) {
            mFlightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
                    droneStatus.droneLatitude = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                    droneStatus.droneLongtitude = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                    droneStatus.droneHeight = djiFlightControllerCurrentState.getAircraftLocation().getAltitude();
                    droneStatus.droneSpeed = sqrt(Math.pow(djiFlightControllerCurrentState.getVelocityX(),2)+Math.pow(djiFlightControllerCurrentState.getVelocityY(),2));
                    droneStatus.droneHeading = mFlightController.getCompass().getHeading();
                    droneStatus.satelliteCount= djiFlightControllerCurrentState.getSatelliteCount();
                }
            });
        }
    }
    protected void onProductChange() {
        initFlightController();
        initPreviewer();
        loginAccount();
    }

    private void loginAccount(){

        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Log.e(TAG, "Login Success");
                    }
                    @Override
                    public void onFailure(DJIError error) {
                        showToast("Login Error:"
                                + error.getDescription());
                    }
                });
    }
    private void initUI() {
        // init mVideoSurface
        mVideoSurface = (TextureView)findViewById(R.id.video_previewer_surface);
        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }

    }

    private void initPreviewer() {

        BaseProduct product = DJIDemoApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(mReceivedVideoDataListener);
            }
        }
    }

    private void uninitPreviewer() {
        Camera camera = DJIDemoApplication.getCameraInstance();
        if (camera != null){
            // Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG,"onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(cameraView.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();
        onProductChange();

        if(mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        uninitPreviewer();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        uninitPreviewer();
        super.onDestroy();
    }
}