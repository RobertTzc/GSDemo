package com.dji.GSDemo.PathPlanning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import static dji.common.camera.SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1600;
import static dji.common.camera.SettingsDefinitions.ShutterSpeed.find;
import static java.lang.Math.sqrt;

public class cameraView extends AppCompatActivity implements TextureView.SurfaceTextureListener, NumberPicker.OnValueChangeListener, CommonCallbacks.CompletionCallback, CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ShutterSpeed> {

    private static final String TAG = FpvActivity.class.getName();
    protected VideoFeeder.VideoDataListener mReceivedVideoDataListener = null;

    // Codec for video live view
    protected DJICodecManager mCodecManager = null;

    protected TextureView mVideoSurface = null;
    Camera camera;
    DrawerLayout drawerLayout;
    private Handler handler;
    private FlightController mFlightController;
    private Battery mBatteryStatus;
    private RadioGroup rg_cameraMode;
    DroneStatus droneStatus = new DroneStatus();
    NumberPicker np_shutter,np_ISO,np_aperture,np_exposureCompensation;
    String[] ISOValue = new String[]{"50","100","200","400","800","1600","3200","6400","12800","25600"};
    String[] ShutterValue = new String[]{"1_20000","1_16000","1_12800","1_10000","1_8000","1_6400","1_6000","1_5000","1_4000","1_3200","1_3000","1_2500","1_2000","1_1600","1_1500",
            "1_1250","1_1000","1_800","1_725","1_640","1_500","1_400","1_350","1_320","1_250","1_240","1_200","1_180","1_160","1_125","1_120","1_100","1_90",
            "1_80","1_60","1_50","1_40","1_30","1_25","1_20","1_15","1_12_DOT_5","1_10","1_8","1_6_DOT_25","1_5","1_4","1_3","1_2_DOT_5","1_2",
            "1_1_DOT_67","1_1_DOT_25","1","1_DOT_3","1_DOT_6","2","2_DOT_5","3","3_DOT_2","4","5","6","7","8","9","10","13","15","20","25","3"};
    String[] ApertureValue = new String[]{"F_1_DOT_7","F_1_DOT_8","F_2","F_2_DOT_2","F_2_DOT_5","F_2_DOT_6","F_2_DOT_8","F_3_DOT_2","F_3_DOT_4","F_3_DOT_5","F_4","F_4_DOT_5","F_4_DOT_8","F_5","F_5_DOT_6",
            "F_6_DOT_3","F_6_DOT_8","F_7_DOT_1","F_8","F_9","F_9_DOT_6","F_10","F_11","F_13","F_14","F_16","F_18","F_19","F_20","F_22"};
    String[] ExposureValue = new String[]{"N_5_0","N_4_7","N_4_3","N_4_0","N_3_7","N_3_3","N_3_0","N_2_7","N_2_3","N_2_0","N_1_7","N_1_3","N_1_0","N_0_7","N_0_3","N_0_0",
            "P_0_3","P_0_7","P_1_0","P_1_3","P_1_7","P_2_0","P_2_3","P_2_7","P_3_0","P_3_3","P_3_7","P_4_0","P_4_3","P_4_7","P_5_0"};
    int ShutterSet,ISOSet,ApertureSet,ExposureSet;

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

        camera = DJIDemoApplication.getCameraInstance();

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
    public void modifyCameraSetting(){
        camera.setShutterSpeed(SettingsDefinitions.ShutterSpeed.values()[ShutterSet],this);
        camera.setISO(SettingsDefinitions.ISO.values()[ISOSet],this);
        camera.setAperture(SettingsDefinitions.Aperture.values()[ApertureSet],this);
        camera.setExposureCompensation(SettingsDefinitions.ExposureCompensation.values()[ExposureSet],this);
    }
    public void updateCameraSetting(){
        camera.getShutterSpeed(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ShutterSpeed>() {
            @Override
            public void onSuccess(SettingsDefinitions.ShutterSpeed shutterSpeed) {
                for (int i = 0; i<SettingsDefinitions.ShutterSpeed.values().length;i++)
                   if (SettingsDefinitions.ShutterSpeed.values()[i]==shutterSpeed) {
                       ShutterSet = i;
                       break;
                   }
            }
            @Override
            public void onFailure(DJIError djiError) {
                setResultToToast(djiError.toString());
            }
        });
        camera.getISO(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ISO>() {
            @Override
            public void onSuccess(SettingsDefinitions.ISO iso) {
                for (int i = 0; i<SettingsDefinitions.ISO.values().length;i++)
                    if (SettingsDefinitions.ISO.values()[i]==iso) {
                        ISOSet = i;
                        break;
                    }
            }
            @Override
            public void onFailure(DJIError djiError) {
                    setResultToToast(djiError.toString());
            }
        });
        camera.getAperture(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.Aperture>() {
            @Override
            public void onSuccess(SettingsDefinitions.Aperture aperture) {
                for (int i = 0; i<SettingsDefinitions.Aperture.values().length;i++)
                    if (SettingsDefinitions.Aperture.values()[i]==aperture) {
                        ApertureSet = i;
                        break;
                    }
            }

            @Override
            public void onFailure(DJIError djiError) {
                setResultToToast(djiError.toString());
            }
        });
        camera.getExposureCompensation(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ExposureCompensation>() {
            @Override
            public void onSuccess(SettingsDefinitions.ExposureCompensation exposureCompensation) {
                for (int i = 0; i<SettingsDefinitions.ExposureCompensation.values().length;i++)
                    if (SettingsDefinitions.ExposureCompensation.values()[i]==exposureCompensation) {
                        ExposureSet = i;
                        break;
                    }
            }

            @Override
            public void onFailure(DJIError djiError) {
                setResultToToast(djiError.toString());
            }
        });
        np_shutter.setValue(ShutterSet);
        np_ISO.setValue(ISOSet);
        np_aperture.setValue(ApertureSet);
        np_exposureCompensation.setValue(ExposureSet);
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
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        np_shutter = findViewById(R.id.np_shutter);
        np_ISO = findViewById(R.id.np_ISO);
        np_aperture = findViewById(R.id.np_Aperture);
        np_exposureCompensation = findViewById(R.id.np_exposure);
        np_shutter.setDisplayedValues(ShutterValue);
        np_shutter.setMaxValue(ShutterValue.length-1);
        np_ISO.setDisplayedValues(ISOValue);
        np_ISO.setMaxValue(ISOValue.length-1);
        np_exposureCompensation.setDisplayedValues(ExposureValue);
        np_exposureCompensation.setMaxValue(ExposureValue.length-1);
        np_aperture.setDisplayedValues(ApertureValue);
        np_aperture.setMaxValue(ApertureValue.length-1);
        np_shutter.setOnValueChangedListener(this);
        np_ISO.setOnValueChangedListener(this);
        np_aperture.setOnValueChangedListener(this);
        np_exposureCompensation.setOnValueChangedListener(this);
        rg_cameraMode = findViewById(R.id.rg_mode);
        rg_cameraMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.rb_auto:{
                        camera.setExposureMode(SettingsDefinitions.ExposureMode.PROGRAM,cameraView.this::onResult);
                        updateCameraSetting();
                        setResultToToast("auto mode");
                        break;
                    }
                    case R.id.rb_shutter:{
                        camera.setExposureMode(SettingsDefinitions.ExposureMode.SHUTTER_PRIORITY,cameraView.this::onResult);
                        updateCameraSetting();
                        break;
                    }
                    case R.id.rb_manual:{
                        camera.setExposureMode(SettingsDefinitions.ExposureMode.MANUAL,cameraView.this::onResult);
                        updateCameraSetting();
                        break;
                    }
                }
            }
        });
        findViewById(R.id.bt_cameraSetting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
        findViewById(R.id.btn_close_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifyCameraSetting();
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });
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

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        switch (numberPicker.getId()){
            case R.id.np_shutter: {
                ShutterSet = i1;
                break;
            }
            case R.id.np_ISO: {
                ISOSet = i1;
                break;
            }
            case R.id.np_Aperture:{
                ApertureSet = i1;
                break;
            }
            case R.id.np_exposure:{
                ExposureSet = i1;
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onResult(DJIError djiError) {
        if (djiError!=null)
            setResultToToast(djiError.toString());
    }
    private void setResultToToast(final String string){
        cameraView.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(cameraView.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSuccess(SettingsDefinitions.ShutterSpeed shutterSpeed) {

    }

    @Override
    public void onFailure(DJIError djiError) {
        setResultToToast(djiError.toString());
    }
}