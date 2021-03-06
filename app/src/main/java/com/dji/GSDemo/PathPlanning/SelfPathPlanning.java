
package com.dji.GSDemo.PathPlanning;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.palette.graphics.Palette;

import com.dji.GSDemo.GoogleMap.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.barcode.Barcode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dji.common.battery.BatteryState;
import dji.common.camera.SettingsDefinitions;
import dji.common.camera.SystemState;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.product.Model;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.keysdk.BatteryKey;
import dji.sdk.base.BaseProduct;
import dji.sdk.battery.Battery;
import dji.sdk.camera.Camera;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;


import edu.missouri.frame.GePoint;
import edu.missouri.frame.ReadFlightParameters;

import static dji.common.camera.SettingsDefinitions.ShutterSpeed.SHUTTER_SPEED_1_1600;
import static edu.missouri.frame.GPStoCord.getCord;
import static edu.missouri.frame.ReadFlightParameters.splitPointString;
import static java.lang.Math.sqrt;


public class SelfPathPlanning extends FragmentActivity implements View.OnClickListener, GoogleMap.OnMapClickListener, OnMapReadyCallback, SeekBar.OnSeekBarChangeListener {

    protected static final String TAG = "GSDemoActivity";

    private GoogleMap gMap;
    private Button locate, clear,generate,upload,camera;
    private Button  start, stop;
    private EditText Altitude,Speed;
    private TextView tv_overlapratio;
    private CheckBox cb_show;
    private SeekBar sb_overlapratio;
    protected DJICodecManager mCodecManager = null;
    private Handler handler;


    private boolean isAdd = true;
    public ArrayList<GePoint> cornerListGeo = new ArrayList<GePoint>();
    private float droneYaw = 0;
    private Marker droneMarker = null;
    private Marker homeMarker = null;

    private float altitude = 100.0f;
    private float mSpeed = 5.0f;
    private  int overlapratio=50;

    ArrayList<Marker> markerList = new ArrayList<>();
    private List<Waypoint> waypointList = new ArrayList<>();

    public static WaypointMission.Builder waypointMissionBuilder;
    private FlightController mFlightController;
    private Battery mBatteryStatus;
    private Camera mCamera;
    private WaypointMissionOperator instance;
    private WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;
    private Polygon polygon=null;
    public final ReadFlightParameters pathCalculation = new ReadFlightParameters();
    Polyline pathPoly;
    ArrayList<LatLng> edgeLatLngList = new ArrayList<>();
    ArrayList<Float> edgeAltitudeList = new ArrayList<>();
    ArrayList<GePoint> wpGeo =  new ArrayList<>();
    ArrayList<Boolean> wpIsTurn = new ArrayList<>();
    ArrayList<Double> wpAltitude = new ArrayList<>();
    int batteryLevel = 0,satelliteCt = 0;
    String fileName,filePath;
    String energyfileName,energyfilePath;
    String TimeStampString;
    Tools tool = new Tools();
    DroneStatus droneStatus = new DroneStatus();
    SettingsDefinitions.ExposureMode cameraCurrentState;
    SettingsDefinitions.ShutterSpeed currentShutterSpeed;
    SettingsDefinitions.ISO currentISO;
    SettingsDefinitions.Aperture currentAperture;
    SettingsDefinitions.ExposureCompensation currentExposure;
    private static DecimalFormat df = new DecimalFormat("0.00");


    @Override
    protected void onResume(){
        super.onResume();
        initFlightController();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(mReceiver);
        removeListener();
        super.onDestroy();
    }

    /**
     * @Description : RETURN Button RESPONSE FUNCTION
     */
    public void onReturn(View view){
        Log.d(TAG, "onReturn");
        this.finish();
    }

    private void setResultToToast(final String string){
        SelfPathPlanning.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SelfPathPlanning.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initUI() {

        locate = (Button) findViewById(R.id.bt_locate);
        clear = (Button) findViewById(R.id.bt_clear);
        Altitude = (EditText) findViewById(R.id.et_altitude);
        Speed = (EditText)findViewById(R.id.et_speed);
        start = (Button) findViewById(R.id.bt_start);
        stop = (Button) findViewById(R.id.bt_stop);
        generate = (Button)findViewById(R.id.bt_generate_path);
        upload = (Button)findViewById(R.id.bt_upload);
        camera = (Button)findViewById(R.id.bt_camera);
        camera.setOnClickListener(clickListener);
        cb_show = findViewById(R.id.cb_show);
        sb_overlapratio = findViewById(R.id.sb_overlapratio);
        tv_overlapratio = findViewById(R.id.tv_overlapratio);
        locate.setOnClickListener(this);
        clear.setOnClickListener(this);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        generate.setOnClickListener(this);
        upload.setOnClickListener(this);
        sb_overlapratio.setOnSeekBarChangeListener(this);
        cb_show.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    isAdd = false;
                    edgeLatLngList.clear();
                    if (polygon !=null)
                        polygon.remove();
                    for (Marker m:markerList)
                        edgeLatLngList.add(new LatLng(m.getPosition().latitude,m.getPosition().longitude));
                    PolygonOptions polygonOptions = new PolygonOptions().addAll(edgeLatLngList)
                            .clickable(true);
                    polygon = gMap.addPolygon((polygonOptions));
                    polygon.setStrokeColor(Color.argb(100,71,227,58));
                    polygon.setFillColor(Color.argb(100,55,201,43));
                    for (LatLng p:edgeLatLngList) {
                        cornerListGeo.add(new GePoint(p.latitude,p.longitude));
                    }
                }
                else {
                    isAdd = true;
                    polygon.setFillColor(Color.TRANSPARENT);
                    if (polygon !=null)
                        polygon.remove();
                    cornerListGeo.clear();
                }
            }
        });
    }
    public static void startActivity(Context context, Class activity) {
        Intent intent = new Intent(context, activity);
        context.startActivity(intent);
    }
    private View.OnClickListener clickListener = v -> {
        switch (v.getId()) {
            case R.id.bt_camera:
                //setResultToToast("camera pressed");
                try {
                    startActivity(SelfPathPlanning.this, cameraView.class);
                }
                catch(Exception e) {
                    setResultToToast(e.toString());
                }
                break;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When the compile and target version is higher than 22, please request the
        // following permissions at runtime to ensure the
        // SDK work well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }

        setContentView(R.layout.activity_self_path_planning);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIDemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        initUI();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addListener();

    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    private void onProductConnectionChange()
    {
        initFlightController();
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
                        setResultToToast("Login Error:"
                                + error.getDescription());
                    }
                });
    }

    private void initFlightController() {
        BaseProduct product = DJIDemoApplication.getProductInstance();
        mCamera = DJIDemoApplication.getCameraInstance();
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
                    droneStatus.batteryCurrent = batteryState.getCurrent();
                    droneStatus.batteryVoltage = batteryState.getVoltage();
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
                    droneStatus.droneVerticalSpeed = djiFlightControllerCurrentState.getVelocityZ();
                    droneStatus.droneHeading = mFlightController.getCompass().getHeading();
                    droneStatus.satelliteCount= djiFlightControllerCurrentState.getSatelliteCount();
                    droneStatus.homeLatitude = djiFlightControllerCurrentState.getHomeLocation().getLatitude();
                    droneStatus.homeLongtitude = djiFlightControllerCurrentState.getHomeLocation().getLongitude();
                    try {
                        droneStatus.cameraExposureMode = getCameraMode().toString();
                        droneStatus.cameraISO = getISO().toString();
                        droneStatus.cameraAperture = getAperture().toString();
                        droneStatus.cameraShutter = getShutterSpeed().toString();
                        droneStatus.cameraExposureCompensation = getExposure().toString();
                    }
                    catch(Exception e){}
                    droneStatus.droneMissionState = getWaypointMissionOperator().getCurrentState();
                    droneStatus.isFly = djiFlightControllerCurrentState.isFlying();
                    drawDroneInfo();
                }
            });
        }
    }

    //Add Listener for WaypointMissionOperator
    private void addListener() {
        if (getWaypointMissionOperator() != null){
            getWaypointMissionOperator().addListener(eventNotificationListener);
        }
    }

    private void removeListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().removeListener(eventNotificationListener);
        }
    }

    private WaypointMissionOperatorListener eventNotificationListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(WaypointMissionDownloadEvent downloadEvent) {

        }

        @Override
        public void onUploadUpdate(WaypointMissionUploadEvent uploadEvent) {

        }

        @Override
        public void onExecutionUpdate(WaypointMissionExecutionEvent executionEvent) {

        }

        @Override
        public void onExecutionStart() {

        }

        @Override
        public void onExecutionFinish(@Nullable final DJIError error) {
            setResultToToast("Execution finished: " + (error == null ? "Success!" : error.getDescription()));
        }
    };

    public WaypointMissionOperator getWaypointMissionOperator() {
        if (instance == null) {
            if (DJISDKManager.getInstance().getMissionControl() != null){
                instance = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
            }
        }
        return instance;
    }

    private void setUpMap() {
        gMap.setMapType(gMap.MAP_TYPE_SATELLITE);
        gMap.setOnMapClickListener(this);// add the listener for click for map object
        gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                ToastUtil.toast(SelfPathPlanning.this,"Latitude"+marker.getPosition().latitude+"\nlongtitude"+marker.getPosition().longitude);
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }
        });
    }

    @Override
    public void onMapClick(LatLng point) {
        if (isAdd == true) {
            String alt = Altitude.getText().toString();
            String sd = Speed.getText().toString();
            try {
                altitude = Integer.parseInt(alt);
                mSpeed = Float.parseFloat(sd);

            } catch (Exception e) {
                altitude = 90;
                mSpeed=  5.0f;
            }
            setResultToToast("Creating edge with\nHeight: "+String.valueOf(df.format(altitude))+"\nSpeed: "+String.valueOf(df.format(mSpeed)));
            edgeAltitudeList.add(altitude);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(point);
            Marker marker = gMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_icon)).draggable(true).title(String.valueOf(markerList.size()+1)+'_'+String.valueOf(altitude)));
            markerList.add(marker);
        }
        else{
            setResultToToast("Not able to add points right now, clear polygon first");
        }
    }


    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    // Update the drone location based on states from MCU.
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
                "\ndrone Mission Status: "+ droneStatus.droneMissionState.toString()+
                "\ncamera Mode: "+droneStatus.cameraExposureMode +
                "\ncamera shutter: "+droneStatus.cameraShutter +
                "\ncamera ISO: " + droneStatus.cameraISO +
                "\ncamera Aperture: " + droneStatus.cameraAperture+
                "\ncamera ExposureCompensation:" + droneStatus.cameraExposureCompensation+
                "\nDrone battery current:" + String.valueOf(droneStatus.batteryCurrent)+
                "\nDrone battery voltage:" + String.valueOf(droneStatus.batteryVoltage)
        );
        if (droneStatus.isFly){
            TimeStampString = java.text.DateFormat.getDateTimeInstance().format(new Date());
            tool.writeTxtToFile(TimeStampString+"\t"+droneStatus.droneLatitude+"\t"+droneStatus.droneLongtitude+
                    "\t"+String.valueOf(df.format(droneStatus.droneSpeed))+
                    "\t"+String.valueOf(df.format(droneStatus.droneVerticalSpeed))+
                    "\t"+String.valueOf(droneStatus.droneHeading)+
                    "\t"+String.valueOf(droneStatus.batteryPercentage)+
                    "\t"+String.valueOf(droneStatus.batteryCurrent)+
                    "\t"+String.valueOf(droneStatus.batteryVoltage)
                    , energyfilePath, energyfileName);
        }

    }
    private void drawDroneInfo(){
        LatLng dronePos = new LatLng(droneStatus.droneLatitude, droneStatus.droneLongtitude);
        LatLng homePos = new LatLng(droneStatus.homeLatitude,droneStatus.homeLongtitude);
        //Create MarkerOptions object
        final MarkerOptions droneMarkerOptions = new MarkerOptions().position(dronePos);
        final MarkerOptions homeMarkerOptions = new MarkerOptions().position(homePos);
        droneMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.drone_icon)).rotation((droneStatus.droneHeading)).anchor(0.5f,0.5f);
        homeMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.drone_home_icon)).anchor(0.5f,0.5f);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayDroneStatus();
                if (homeMarker != null){
                    homeMarker.remove();
                }
                if (droneMarker != null) {
                    droneMarker.remove();
                }
                homeMarker = gMap.addMarker(homeMarkerOptions);
                if (checkGpsCoordination(droneStatus.droneLatitude, droneStatus.droneLongtitude)) {
                    droneMarker = gMap.addMarker(droneMarkerOptions);
                }
            }
        });
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_locate:{
                //drawDroneInfo();
                cameraUpdate(); // Locate the drone's place
                break;
            }
            case R.id.bt_clear:{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gMap.clear();
                    }

                });
                cb_show.setChecked(false);
                if (waypointList!=null)
                    waypointList.clear();
                if (waypointMissionBuilder!=null)
                    waypointMissionBuilder.waypointList(waypointList);
                if (polygon!=null)
                    polygon.remove();
                if (edgeAltitudeList!=null)
                    edgeAltitudeList.clear();
                if (edgeLatLngList!=null)
                    edgeLatLngList.clear();
                if (markerList!=null)
                    markerList.clear();
                drawDroneInfo();
                break;
            }
            case R.id.bt_generate_path:{

                droneStatus.plannedSpeed = Math.round(mSpeed);
                drawDroneInfo();
                if (cb_show.isChecked()==false){
                    setResultToToast("please generate area first");
                    break;
                }
                setResultToToast("Generating Waypoint: "+String.valueOf(overlapratio/100.0)+"%");

                try {
                    pathCalculation.UpdateBounds(cornerListGeo, edgeAltitudeList.get(0),droneStatus);
                } catch(Exception e)
                {
                    setResultToToast("Error "+e.toString());
                    break;
                }
                wpGeo = (ArrayList<GePoint>) pathCalculation.getWaypoints();
                wpIsTurn = (ArrayList<Boolean>) pathCalculation.getIsTurning();
                wpAltitude = (ArrayList<Double>) pathCalculation.getAltitudes();
                //add check if duplicate wp exist
                ArrayList<GePoint> pre_wp = new ArrayList<>();
                List<Integer> toremove = new ArrayList<>();
                for  (int i = 0;i<wpGeo.size();i++){
                    if (pre_wp.contains(wpGeo.get(i)))
                        toremove.add(i);
                    else
                        pre_wp.add(wpGeo.get(i));
                }
                for (int i= 0 ; i<toremove.size();i++){
                    wpGeo.remove(toremove.get(i));
                    wpIsTurn.remove(toremove.get(i));
                    wpAltitude.remove(toremove.get(i));
                }

                droneStatus.batteryPrecentageRemian =pathCalculation.getEnergyPercentRemainingAfterPlan();
                DisplayWaypoint();
                setResultToToast("Num_waypoint:"+String.valueOf(wpGeo.size()));
                PolylineOptions wpTrace = new PolylineOptions();
                double distance=0;
                for (int i = 0;i<wpGeo.size();i++)
                {
                    if(wpIsTurn.get(i)==false && wpIsTurn.get(i+1)==false) {
                        double[] result = getCord(wpGeo.get(i), wpGeo.get(i + 1));
                        distance = sqrt((result[0] * result[0]) + (result[1] * result[1]));
                        break;
                    }
                }
                //initial first waypoint to make faster to the mission spot
                Waypoint startWaypoint = new Waypoint(droneStatus.droneLatitude, droneStatus.droneLongtitude,Float.parseFloat(wpAltitude.get(0).toString()));
                startWaypoint.speed = droneStatus.prePlannedSpeed;
                waypointList.add(startWaypoint);
                for (int i = 0;i<wpGeo.size();i++) {
                    if (wpIsTurn.get(i)) {
                        Waypoint mWaypoint = new Waypoint(wpGeo.get(i).latitude, wpGeo.get(i).longtitude, Float.parseFloat(wpAltitude.get(i).toString()));
                        mWaypoint.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, -90));
                        mWaypoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 0));
                        if(i+1<wpGeo.size() && wpIsTurn.get(i+1)==false){
                            mWaypoint.shootPhotoDistanceInterval= Float.parseFloat(String.valueOf(distance));
                        }
                        else
                            mWaypoint.shootPhotoDistanceInterval= 0;
                        mWaypoint.speed = droneStatus.plannedSpeed;
                        waypointList.add(mWaypoint);
                    }

                }
                setResultToToast("Interval distance is: "+distance+"\nGenerate done");
                //after the output from the code generate waypoint

                break;
            }
            case R.id.bt_upload:{
                if (waypointMissionBuilder != null) {

                    waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
                } else {
                    waypointMissionBuilder = new WaypointMission.Builder();
                    waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
                }
                mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
                mHeadingMode = WaypointMissionHeadingMode.AUTO;
                cornerListGeo.clear();
                configWayPointMission();
                uploadWayPointMission();
                break;
            }
            case R.id.bt_start:{
                TimeStampString = java.text.DateFormat.getDateTimeInstance().format(new Date());
                filePath = Environment.getExternalStorageDirectory()
                        .getPath() + "/DJI_Log/";
                energyfilePath = Environment.getExternalStorageDirectory()
                        .getPath() + "/DJI_ENERGY/";
                fileName = "PathPlanning_Project_"+java.text.DateFormat.getDateTimeInstance().format(new Date())+".txt";
                energyfileName = "PathPlanning_Project_"+java.text.DateFormat.getDateTimeInstance().format(new Date())+".txt";
                tool.writeTxtToFile("TimeStamp"+"\tLatitude"+"\tLongtitude"+
                                "\tdroneSpeed"+
                                "\tdroneVerticalSpeed"+
                                "\tdroneHeading"+
                                "\tbatteryPercentage"+
                                "\tbatteryCurrent"+
                                "\tbatteryVoltage"
                        , energyfilePath, energyfileName);
                tool.writeTxtToFile("Start project time: " + TimeStampString, filePath, fileName);
                tool.writeTxtToFile("Drone setting info:\n",filePath,fileName);
                tool.writeTxtToFile("Battery_info: "+String.valueOf(droneStatus.batteryPercentage)+
                        "\nEstimate battery left: "+String.valueOf(droneStatus.batteryPrecentageRemian)+
                        "\nSatellite count: "+String.valueOf(droneStatus.satelliteCount)+
                        "\nSpeed_info: "+String.valueOf(df.format(droneStatus.droneSpeed))+
                        "\nSpeed set: "+String.valueOf(droneStatus.plannedSpeed)+
                        "\nprePlannedSpeed set: "+String.valueOf(droneStatus.prePlannedSpeed)+
                        "\nDrone current location: "+String.valueOf(droneStatus.droneLatitude)+","+String.valueOf(droneStatus.droneLongtitude)+
                        "\nDrone current Height: "+String.valueOf(df.format(droneStatus.droneHeight))+
                        "\nDrone heading : "+ String.valueOf(droneStatus.droneHeading)+
                        "\nDrone home location: "+String.valueOf(droneStatus.homeLatitude)+","+String.valueOf(droneStatus.homeLongtitude)+
                        "\nOverlap set: "+ String.valueOf(droneStatus.overlapRatio),
                        filePath, fileName);
                for (int i = 0;i<waypointList.size();i++) {
                    tool.writeTxtToFile("Waypoint_" + String.valueOf(i) + ": " + String.valueOf(waypointList.get(i).coordinate.getLatitude()) + "\t" + String.valueOf(waypointList.get(i).coordinate.getLongitude()) + "\t" + String.valueOf(waypointList.get(i).altitude),
                            filePath, fileName);
                }
                startWaypointMission();
                break;
            }
            case R.id.bt_stop:{
                stopWaypointMission();
                break;
            }
            default:
                break;
        }
    }

    private void cameraUpdate(){
        LatLng pos = new LatLng(droneStatus.droneLatitude, droneStatus.droneLongtitude);
        float zoomlevel = (float) 18.0;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoomlevel);
        gMap.moveCamera(cu);
    }

    public void DisplayWaypoint(){
        PolylineOptions generatedPath = new PolylineOptions();
        for (int i =0;i<wpGeo.size();i++){
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(wpGeo.get(i).latitude,wpGeo.get(i).longtitude));
            if (wpIsTurn.get(i)) {
                gMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.turn_icon)).anchor(0.5f,0.5f).title(String.valueOf(i+1)+'_'+String.valueOf(wpAltitude.get(i))));
                generatedPath.add(new LatLng(wpGeo.get(i).latitude, wpGeo.get(i).longtitude));
            }
            else
                gMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_icon)).anchor(0.5f,0.5f).alpha(0.5f).title(String.valueOf(i+1)+'_'+String.valueOf(wpAltitude.get(i))));
        }
        pathPoly = gMap.addPolyline(generatedPath);

    }

    private void configWayPointMission(){

        if (waypointMissionBuilder == null){

            waypointMissionBuilder = new WaypointMission.Builder().finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .maxFlightSpeed(10.0f)
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

        }else
        {
            waypointMissionBuilder.finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .maxFlightSpeed(10.0f)
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

        }

        if (waypointMissionBuilder.getWaypointList().size() > 0){

            for (int i=0; i< waypointMissionBuilder.getWaypointList().size(); i++){
                waypointMissionBuilder.getWaypointList().get(i).altitude = altitude;
            }

            setResultToToast("Set Waypoint attitude successfully");
        }

        DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());

        if (error == null) {
            setResultToToast("loadWaypoint succeeded");
        } else {
            setResultToToast("loadWaypoint failed " + error.getDescription());
        }

    }

    private void uploadWayPointMission(){

        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {

                if (error == null) {
                    setResultToToast("Mission upload successfully!");
                } else {
                    setResultToToast("Mission upload failed, error: " + error.getDescription() + " retrying...");
                    getWaypointMissionOperator().retryUploadMission(null);
                }
            }
        });

    }

    private void startWaypointMission(){
        getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                setResultToToast("Mission Start: " + (error == null ? "Successfully" : error.getDescription()));
            }
        });


    }

    private void stopWaypointMission(){

        getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                setResultToToast("Mission Stop: " + (error == null ? "Successfully" : error.getDescription()));
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (gMap == null) {
            gMap = googleMap;
            setUpMap();
        }

        LatLng MU = new LatLng(droneStatus.droneLatitude,droneStatus.droneLongtitude);
        //gMap.addMarker(new MarkerOptions().position(MU).title("University of Missouri"));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MU,15));
        gMap.moveCamera(CameraUpdateFactory.newLatLng(MU));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (seekBar.getId() == R.id.sb_overlapratio){
            overlapratio = i;
            droneStatus.overlapRatio = i;
            tv_overlapratio.setText("overlap: "+String.valueOf(i)+"%");
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
    public SettingsDefinitions.ShutterSpeed getShutterSpeed(){
        mCamera.getShutterSpeed(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ShutterSpeed>() {
            @Override
            public void onSuccess(SettingsDefinitions.ShutterSpeed shutterSpeed) {
                currentShutterSpeed = shutterSpeed;
            }
            @Override
            public void onFailure(DJIError djiError) {}
        });
        return currentShutterSpeed;
    }

    public SettingsDefinitions.ISO getISO() {
        mCamera.getISO(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ISO>() {
            @Override
            public void onSuccess(SettingsDefinitions.ISO ISO) {
                currentISO = ISO;
            }

            @Override
            public void onFailure(DJIError djiError) {}
        });
        return currentISO;
    }
    public SettingsDefinitions.Aperture getAperture(){
        mCamera.getAperture(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.Aperture>() {
            @Override
            public void onSuccess(SettingsDefinitions.Aperture Aperture) {
                currentAperture = Aperture;
            }
            @Override
            public void onFailure(DJIError djiError) {}
        });
        return currentAperture;
    }
    public SettingsDefinitions.ExposureCompensation getExposure() {
        mCamera.getExposureCompensation(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ExposureCompensation>() {
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
    public SettingsDefinitions.ExposureMode getCameraMode(){
        mCamera.getExposureMode(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ExposureMode>() {
            @Override
            public void onSuccess(SettingsDefinitions.ExposureMode exposureMode) {
                cameraCurrentState = exposureMode;
            }
            @Override
            public void onFailure(DJIError djiError) {}
        });
        return cameraCurrentState;
    }
}