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
import java.util.Arrays;
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

import static dji.common.camera.SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1000;
import static dji.common.camera.SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1600;
import static dji.common.camera.SettingsDefinitions.ShutterSpeed.find;
import static java.lang.Enum.valueOf;
import static java.lang.Math.sqrt;

public class cameraView extends AppCompatActivity implements TextureView.SurfaceTextureListener, NumberPicker.OnValueChangeListener, CommonCallbacks.CompletionCallback {

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
    int option = 1;
    DroneStatus droneStatus = new DroneStatus();
    NumberPicker np_shutter,np_ISO,np_aperture,np_exposureCompensation;

    String[] ISOValue = new String[]{"AUTO","50","100","200","400","800","1600","3200","6400","12800","25600"};
    String[] ShutterValue = new String[]{"1/20000","1/16000","1/12800","1/10000","1/8000","1/6400","1/6000","1/5000","1/4000","1/3200","1/3000","1/2500","1/2000","1/1600","1/1500",
            "1/1250","1/1000","1/800","1/725","1/640","1/500","1/400","1/350","1/320","1/250","1/240","1/200","1/180","1/160","1/125","1/120","1/100","1/90",
            "1/80","1/60","1/50","1/40","1/30","1/25","1/20","1/15","1/12.5","1/10","1/8","1/6.25","1/5","1/4","1/3","1/2.5","1/2",
            "1/1.67","1/1.25","1","1/3","1/6","2","2/5","3","3/2","4","5","6","7","8","9","10","13","15","20","25","3"};
    String[] ApertureValue = new String[]{"F_1.7","F_1.8","F_2","F_2.2","F_2.5","F_2.6","F_2.8","F_3.2","F_3.4","F_3.5","F_4","F_4.5","F_4.8","F_5","F_5.6",
            "F_6.3","F_6.8","F_7.1","F_8","F_9","F_9.6","F_10","F_11","F_13","F_14","F_16","F_18","F_19","F_20","F_22"};
    String[] ExposureValue = new String[]{"-5.0","-4.7","-4.3","-4.0","-3.7","-3.3","-3.0","-2.7","-2.3","-2.0","-1.7","-1.3","-1.0","-0.7","-0.3","0.0",
            "+0.3","+0.7","+1.0","+1.3","+1.7","+2.0","+2.3","+2.7","+3.0","+3.3","+3.7","+4.0","+4.3","+4.7","+5.0"};
    String[] ISOValuePro = new String[]{"AUTO","50","100","200","400","800","1600","3200","6400","12800","25600"};
    String[] ShutterValuePro = new String[]{"1/8000","1/6400","1/5000","1/4000","1/3200","1/2500","1/2000","1/1600","1/1250","1/1000","1/800","1/640","1/500","1/400","1/320","1/240","1/200","1/160",
            "1/120","1/100","1/80","1/60","1/50","1/40","1/30","1/25","1/20","1/15","1/12.5","1/10","1/8","1/6.25","1/5","1/4","1/3","1/2.5","1/2","1/1.67","1/1.25","1","1.3","1.6","2","2.5","3","3.2",
            "4","5","6","7","8"};
    String[] ApertureValuePro = new String[]{"F_2.8","F_3.2","F_3.4","F_3.5","F_4","F_4.5","F_4.8","F_5","F_5.6",
            "F_6.3","F_6.8","F_7.1","F_8","F_9","F_9.6","F_10","F_11"};
    String[] ExposureValuePro = new String[]{"-5.0","-4.7","-4.3","-4.0","-3.7","-3.3","-3.0","-2.7","-2.3","-2.0","-1.7","-1.3","-1.0","-0.7","-0.3","0.0",
            "+0.3","+0.7","+1.0","+1.3","+1.7","+2.0","+2.3","+2.7","+3.0","+3.3","+3.7","+4.0","+4.3","+4.7","+5.0"};

    int ShutterSet,ISOSet,ApertureSet,ExposureSet;
    SettingsDefinitions.ExposureMode cameraCurrentState;
    SettingsDefinitions.ShutterSpeed currentShutterSpeed;
    SettingsDefinitions.ISO currentISO;
    SettingsDefinitions.Aperture currentAperture;
    SettingsDefinitions.ExposureCompensation currentExposure;
    private static DecimalFormat df = new DecimalFormat("0.00");
    Tools tool = new Tools();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);

        handler = new Handler();
        camera = DJIDemoApplication.getCameraInstance();
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

        if (camera != null) {

            camera.setSystemStateCallback(new SystemState.Callback() {
                @Override
                public void onUpdate(SystemState cameraSystemState) {
                    if (null != cameraSystemState) {

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
        tv_droneInfo.setText("Battery_info: "+String.valueOf(droneStatus.batteryPercentage)+"%" +
                "\nSatellite count: "+String.valueOf(droneStatus.satelliteCount)+
                "\nSpeed_info: "+String.valueOf(df.format(droneStatus.droneSpeed))+"m/s"+
                "\nSpeed set: "+String.valueOf(droneStatus.plannedSpeed)+"m/s"+
                "\nHeight: "+String.valueOf(df.format(droneStatus.droneHeight))+"m"+
                "\nOverlap set: "+ String.valueOf(droneStatus.overlapRatio)+"%"+
                "\nDrone heading : "+ String.valueOf(droneStatus.droneHeading)+"deg"+
                "\nbattery estimate: "+String.valueOf(droneStatus.batteryPrecentageRemian)+"%"+
                "\ncamera Mode: "+droneStatus.cameraExposureMode +
                "\ncamera shutter: "+droneStatus.cameraShutter +
                "\ncamera ISO: " + droneStatus.cameraISO +
                "\ncamera Aperture: " + droneStatus.cameraAperture+
                "\ncamera ExposureCompensation:" + droneStatus.cameraExposureCompensation);
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
                    try {
                        droneStatus.cameraExposureMode = getCameraMode().toString();
                        droneStatus.cameraISO = getISO().toString();
                        droneStatus.cameraAperture = getAperture().toString();
                        droneStatus.cameraShutter = getShutterSpeed().toString();
                        droneStatus.cameraExposureCompensation = getExposure().toString();
                    }
                    catch(Exception e){}
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
        np_shutter.setDisplayedValues(ShutterValuePro);
        np_shutter.setMaxValue(ShutterValuePro.length-1);
        np_ISO.setDisplayedValues(ISOValuePro);
        np_ISO.setMaxValue(ISOValuePro.length-1);
        np_exposureCompensation.setDisplayedValues(ExposureValuePro);
        np_exposureCompensation.setMaxValue((ExposureValuePro.length-1));
        np_exposureCompensation.setValue((ExposureValuePro.length-1)/2);
        np_aperture.setDisplayedValues(ApertureValuePro);
        np_aperture.setMaxValue(ApertureValuePro.length-1);
        np_shutter.setWrapSelectorWheel(false);
        np_ISO.setWrapSelectorWheel(false);
        np_aperture.setWrapSelectorWheel(false);
        np_exposureCompensation.setWrapSelectorWheel(false);
        np_shutter.setEnabled(false);
        np_ISO.setEnabled(false);
        np_aperture.setEnabled(false);
        np_exposureCompensation.setEnabled(false);
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
                        option = 1;
                        break;
                    }
                    case R.id.rb_shutter:{
                        option = 2;
                        break;
                    }
                    case R.id.rb_manual:{
                        option = 4;
                        break;
                    }
                }
                setCameraMode();
            }
        });
        findViewById(R.id.bt_pre_set_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.setExposureMode(SettingsDefinitions.ExposureMode.MANUAL, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError!=null)
                            setResultToToast("set camera Manual failed");
                    }
                });
                camera.setShutterSpeed(SHUTTER_SPEED_1_1600, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError!=null)
                            setResultToToast("set camera shutter 1/1000 failed");
                    }
                });
                camera.setISO(SettingsDefinitions.ISO.ISO_400, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError!=null)
                            setResultToToast("set camera ISO 200 failed");
                    }
                });
                camera.setAperture(SettingsDefinitions.Aperture.F_6_DOT_3, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError!=null)
                            setResultToToast("set Aperture 3.2 failed");
                    }
                });
            }
        });
        findViewById(R.id.bt_pre_set_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.setExposureMode(SettingsDefinitions.ExposureMode.MANUAL, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError!=null)
                            setResultToToast("set camera Manual failed");
                    }
                });
                camera.setShutterSpeed(SHUTTER_SPEED_1_1000, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError!=null)
                            setResultToToast("set camera shutter 1/1000 failed");
                    }
                });
                camera.setISO(SettingsDefinitions.ISO.ISO_800, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError!=null)
                            setResultToToast("set camera ISO 800 failed");
                    }
                });
                camera.setAperture(SettingsDefinitions.Aperture.F_5_DOT_6, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError!=null)
                            setResultToToast("set Aperture 5.6 failed");
                    }
                });
            }
        });
        findViewById(R.id.bt_pre_set_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.setExposureMode(SettingsDefinitions.ExposureMode.SHUTTER_PRIORITY, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError!=null)
                            setResultToToast("set camera shutter priority failed");
                    }
                });
                camera.setShutterSpeed(SHUTTER_SPEED_1_1600, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError!=null)
                            setResultToToast("set camera shutter 1/1600 failed");
                    }
                });
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
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });
        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }
        findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setISO();
                setAperture();
                setExposure();
                setShutter();
            }
        });
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
                ShutterSet = Arrays.asList(ShutterValue).indexOf(ShutterValuePro[i1]);
                setShutter();
                break;
            }
            case R.id.np_ISO: {
                ISOSet = Arrays.asList(ISOValue).indexOf(ISOValuePro[i1]);
                setISO();
                break;
            }
            case R.id.np_Aperture:{
                ApertureSet = Arrays.asList(ApertureValue).indexOf(ApertureValuePro[i1])+2;
                setAperture();
                break;
            }
            case R.id.np_exposure:{
                ExposureSet = Arrays.asList(ExposureValue).indexOf(ExposureValuePro[i1]);
                setExposure();
                break;
            }
            default:
                break;
        }
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
    public void onResult(DJIError djiError) {

    }
    public void setShutter(){
        camera.setShutterSpeed(SettingsDefinitions.ShutterSpeed.values()[ShutterSet],this::onResult);
    }
    public SettingsDefinitions.ShutterSpeed getShutterSpeed(){
        camera.getShutterSpeed(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ShutterSpeed>() {
            @Override
            public void onSuccess(SettingsDefinitions.ShutterSpeed shutterSpeed) {
                currentShutterSpeed = shutterSpeed;
            }
            @Override
            public void onFailure(DJIError djiError) {}
        });
        return currentShutterSpeed;
    }

    public void setISO(){
        camera.setISO(SettingsDefinitions.ISO.values()[ISOSet],this::onResult);
    }
    public SettingsDefinitions.ISO getISO() {
        camera.getISO(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ISO>() {
            @Override
            public void onSuccess(SettingsDefinitions.ISO ISO) {
                currentISO = ISO;
            }

            @Override
            public void onFailure(DJIError djiError) {}
        });
        return currentISO;
    }
    public void setAperture(){
        camera.setAperture(SettingsDefinitions.Aperture.values()[ApertureSet],this::onResult);
    }
    public SettingsDefinitions.Aperture getAperture(){
        camera.getAperture(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.Aperture>() {
            @Override
            public void onSuccess(SettingsDefinitions.Aperture Aperture) {
                currentAperture = Aperture;
            }
            @Override
            public void onFailure(DJIError djiError) {}
        });
        return currentAperture;
    }
    public void setExposure(){
        camera.setExposureCompensation(SettingsDefinitions.ExposureCompensation.values()[ExposureSet],this::onResult);
    }
    public SettingsDefinitions.ExposureCompensation getExposure() {
        camera.getExposureCompensation(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ExposureCompensation>() {
            @Override
            public void onSuccess(SettingsDefinitions.ExposureCompensation Exposure) {
                currentExposure = Exposure;
            }

            @Override
            public void onFailure(DJIError djiError) {
            }
        });
        return currentExposure;
    }
    public void setCameraMode(){
        //PROGRAM(1),SHUTTER_PRIORITY(2),APERTURE_PRIORITY(3),MANUAL(4),CINE(7),

        if (option == 1) {
            camera.setExposureMode(SettingsDefinitions.ExposureMode.PROGRAM,new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError!=null)
                        setResultToToast("AUTO mode set failed");
                }
            });
            np_shutter.setEnabled(false);
            np_ISO.setEnabled(false);
            np_aperture.setEnabled(false);
            np_exposureCompensation.setEnabled(true);
        }
        else if (option==2){
            camera.setExposureMode(SettingsDefinitions.ExposureMode.SHUTTER_PRIORITY, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError!=null)
                        setResultToToast("Shutter priority set failed");
                }
            });;
            np_shutter.setEnabled(true);
            np_ISO.setEnabled(false);
            np_aperture.setEnabled(false);
            np_exposureCompensation.setEnabled(true);
        }
        else{
            camera.setExposureMode(SettingsDefinitions.ExposureMode.MANUAL, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError!=null)
                        setResultToToast("Manual mode set failed");
                }
            });;
            np_shutter.setEnabled(true);
            np_ISO.setEnabled(true);
            np_aperture.setEnabled(true);
            np_exposureCompensation.setEnabled(false);
        }
    }
    public SettingsDefinitions.ExposureMode getCameraMode(){
        camera.getExposureMode(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ExposureMode>() {
            @Override
            public void onSuccess(SettingsDefinitions.ExposureMode exposureMode) {
                cameraCurrentState = exposureMode;
            }
            @Override
            public void onFailure(DJIError djiError) {}
        });
        return cameraCurrentState;
    }

    public void setCameraSetting(){
        cameraView.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (option==1){
                    camera.setExposureMode(SettingsDefinitions.ExposureMode.PROGRAM, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError!=null)
                                setResultToToast("AUTO mode set failde");
                        }
                    });
                    camera.setExposureCompensation(SettingsDefinitions.ExposureCompensation.values()[ExposureSet], new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError!=null)
                                setResultToToast("set compensation failed");
                        }
                    });
                }
                else if (option==2){
                    camera.setExposureMode(SettingsDefinitions.ExposureMode.SHUTTER_PRIORITY, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError!=null)
                                setResultToToast("Shutter mode set failed");
                        }
                    });
                    camera.setShutterSpeed(SettingsDefinitions.ShutterSpeed.values()[ShutterSet], new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError!=null)
                                setResultToToast("shutter speed set failed");
                        }
                    });
                    camera.setExposureCompensation(SettingsDefinitions.ExposureCompensation.values()[ExposureSet], new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError!=null)
                                setResultToToast("exposure failed");
                        }
                    });
                }
                else if (option==4){
                    camera.setExposureMode(SettingsDefinitions.ExposureMode.MANUAL, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError!=null)
                                setResultToToast("MANUAL mode failed");
                        }
                    });
                    camera.setShutterSpeed(SettingsDefinitions.ShutterSpeed.values()[ShutterSet], new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError!=null)
                                setResultToToast("shutter failed");
                        }
                    });
                    camera.setISO(SettingsDefinitions.ISO.values()[ISOSet], new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError!=null)
                                setResultToToast("ISO failed");
                        }
                    });
                    camera.setAperture(SettingsDefinitions.Aperture.values()[ApertureSet], new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError!=null)
                                setResultToToast("APERTURe failed");
                        }
                    });
                }
            }
        });
    }
}