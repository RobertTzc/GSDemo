
package com.dji.GSDemo.PathPlanning;

import static java.lang.Double.parseDouble;
import static java.lang.Math.sqrt;
import static edu.missouri.frame.GPStoCord.getCord;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import dji.common.battery.BatteryState;
import dji.common.camera.SettingsDefinitions;
import dji.common.camera.StorageState;
import dji.common.error.DJICameraError;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecuteState;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.keysdk.CameraKey;
import dji.sdk.base.BaseProduct;
import dji.sdk.battery.Battery;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;
import edu.missouri.frame.GePoint;
import edu.missouri.frame.ReadFlightParameters;


public class SelfPathPlanning extends FragmentActivity implements View.OnClickListener, GoogleMap.OnMapClickListener, OnMapReadyCallback, SeekBar.OnSeekBarChangeListener {

    protected static final String TAG = "GSDemoActivity";

    private GoogleMap gMap;

    private  TextView tv_OverLapRatio;
    private Marker droneMarker = null;
    private Marker homeMarker = null;

    private int overlapratio;
    public SeekBar sb_overlapratio;
    private  EditText tv_plannedSpeed,tv_plannedAltitude;
    ArrayList<Marker> markerList = new ArrayList<>();
    private final List<Waypoint> waypointList = new ArrayList<>();

    public static WaypointMission.Builder waypointMissionBuilder;
    private FlightController mFlightController;
    private Battery mBatteryStatus;
    private Camera mCamera;
    private  StorageState mstorage;
    private WaypointMissionOperator instance;
    private WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;
    private Polygon polygon=null;
    private final String rootPath = Environment.getExternalStorageDirectory().toString()+"/DJI_MIZZOU_APP";
    private final String settingFilePath = rootPath + "/DJI_settings/";
    private final String wayPointFilePath = rootPath+"/DJI_waypointLogs/";
    public final ReadFlightParameters pathCalculation = new ReadFlightParameters();
    private  final ArrayList<Integer> wayPointStack = new ArrayList<>();
    private  Integer startOffset = 0;
    Polyline pathPoly;
    ArrayList<LatLng> edgeLatLngList = new ArrayList<>();
    ArrayList<GePoint> wpGeo =  new ArrayList<>();
    ArrayList<Boolean> wpIsTurn = new ArrayList<>();
    ArrayList<Double> wpAltitude = new ArrayList<>();
    String fileName;
    String energyfileName,energyfilePath;
    String TimeStampString;
    Tools tool = new Tools();
    DroneStatus droneStatus = new DroneStatus();
    SettingsDefinitions.ExposureMode cameraCurrentState;
    SettingsDefinitions.ShutterSpeed currentShutterSpeed;
    SettingsDefinitions.ISO currentISO;
    SettingsDefinitions.Aperture currentAperture;
    SettingsDefinitions.ExposureCompensation currentExposure;
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private Float spaceRemainPrecent = 0f;
    SettingsDefinitions.StorageLocation storageLocation;
    int TotalAvaSpace;
    boolean SD_detected= false;
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
        SelfPathPlanning.this.runOnUiThread(() -> Toast.makeText(SelfPathPlanning.this, string, Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("SetTextI18n")
    private void initUI() {
        //register buttons and other components, set listener
        Button locate = findViewById(R.id.bt_locate);
        Button clear = findViewById(R.id.bt_clear);
        Button start = findViewById(R.id.bt_start);
        Button recover_wp = findViewById(R.id.bt_recover_WP);
        Button generate = findViewById(R.id.bt_generate_path);
        Button upload = findViewById(R.id.bt_upload);
        Button rewind = findViewById(R.id.bt_rewind);
        Button resume = findViewById(R.id.bt_resume);
        Button camera = findViewById(R.id.bt_camera);
        Button recover = findViewById(R.id.bt_recover);
        sb_overlapratio = findViewById(R.id.sb_overlapratio);
        tv_plannedSpeed = findViewById(R.id.et_speed);
        tv_plannedAltitude = findViewById(R.id.et_altitude);
        tv_OverLapRatio = findViewById(R.id.tv_overlapratio);


        tv_plannedSpeed.setText("10");
        tv_plannedAltitude.setText("60");
        overlapratio = 70;
        locate.setOnClickListener(this);
        clear.setOnClickListener(this);
        start.setOnClickListener(this);
        recover_wp.setOnClickListener(this);
        generate.setOnClickListener(this);
        upload.setOnClickListener(this);
        rewind.setOnClickListener(this);
        resume.setOnClickListener(this);
        recover.setOnClickListener(this);
        camera.setOnClickListener(clickListener);
        sb_overlapratio.setOnSeekBarChangeListener(this);
    }

    private static void startActivity(Context context) {
        Intent intent = new Intent(context, cameraView.class);
        context.startActivity(intent);
    }
    private final View.OnClickListener clickListener = v -> {
        if (v.getId() == R.id.bt_camera) {//setResultToToast("camera pressed");
            try {
                startActivity(SelfPathPlanning.this);
            } catch (Exception e) {
                setResultToToast(e.toString());
            }
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
        assert mapFragment != null;
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
                mBatteryStatus = product.getBattery();

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
                public void onUpdate(@NonNull FlightControllerState djiFlightControllerCurrentState) {
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
                        if (SD_detected==true)
                            droneStatus.storage = TotalAvaSpace;
                        else
                            droneStatus.storage = -1;
                        droneStatus.cameraExposureMode = getCameraMode().toString();
                        droneStatus.cameraISO = getISO().toString();
                        droneStatus.cameraAperture = getAperture().toString();
                        droneStatus.cameraShutter = getShutterSpeed().toString();
                        droneStatus.cameraExposureCompensation = getExposure().toString();
                    }
                    catch(Exception ignored){}
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

    private final WaypointMissionOperatorListener eventNotificationListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent downloadEvent) {

        }

        @Override
        public void onUploadUpdate(@NonNull WaypointMissionUploadEvent uploadEvent) {

        }

        @Override
        public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent executionEvent) {
            wayPointStatusUpdate(executionEvent);
        }
        private void wayPointStatusUpdate(WaypointMissionExecutionEvent executionEvent)
        {
            assert executionEvent.getProgress() != null;
            int index = executionEvent.getProgress().targetWaypointIndex;
            WaypointMissionExecuteState state = executionEvent.getProgress().executeState;
            boolean isReached = executionEvent.getProgress().isWaypointReached;
            if (isReached && state.equals(WaypointMissionExecuteState.FINISHED_ACTION)) {
                setResultToToast("Waypoint " + (index+startOffset) + " finished"+state);
                //here we need to mark the waypoint as finished
                wayPointStack.add(index+startOffset);
            }
            else if (!isReached) {
                setResultToToast("Heading to waypoint " + (index+startOffset)+state);
            }
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
        gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        gMap.setOnMapClickListener(this);// add the listener for click for map object
        gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                renderPolygon();
            }
        });
    }
    public void renderPolygon(){
        try{
            polygon.remove();
        }
        catch(Exception ignored){

        }
        PolygonOptions areaOfInterest = new PolygonOptions().clickable(true);
        for (Marker m:markerList){
            areaOfInterest.add(m.getPosition());
        }
        polygon = gMap.addPolygon(areaOfInterest);
        polygon.setStrokeColor(Color.argb(100,71,227,58));
        polygon.setFillColor(Color.argb(100,55,201,43));
    }

    @Override
    public void onMapClick(LatLng point) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        Marker marker = gMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_icon)).draggable(true).title(String.valueOf(markerList.size()+1)));
        markerList.add(marker);
        renderPolygon();
    }


    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    // Update the drone location based on states from MCU.
    public void displayDroneStatus(){
        TextView tv_droneInfo;
        if (SD_detected==true)
            droneStatus.storage = TotalAvaSpace;
        else
            droneStatus.storage = -1;
        tv_droneInfo = findViewById(R.id.tv_droneInfo);
        tv_droneInfo.setText("Battery_info: "+ droneStatus.batteryPercentage +"%" +
                "\nSatellite count: "+ droneStatus.satelliteCount +
                "\nSpeed_info: "+ df.format(droneStatus.droneSpeed) +"m/s"+
                "\nSpeed set: "+ droneStatus.plannedSpeed +"m/s"+
                "\nHeight: "+ df.format(droneStatus.droneHeight) +"m"+
                "\nOverlap set: "+ droneStatus.overlapRatio +"%"+
                "\nDrone heading : "+ droneStatus.droneHeading +"deg"+
                "\nbattery estimate: "+ droneStatus.batteryPrecentageRemian +"%"+
                "\ndrone Mission Status: "+ droneStatus.droneMissionState.toString()+
                "\ndrone Storage (GB): "+ droneStatus.storage+
                "\ncamera Mode: "+droneStatus.cameraExposureMode +
                "\ncamera shutter: "+droneStatus.cameraShutter +
                "\ncamera ISO: " + droneStatus.cameraISO +
                "\ncamera Aperture: " + droneStatus.cameraAperture+
                "\ncamera ExposureCompensation:" + droneStatus.cameraExposureCompensation+
                "\nDrone battery current:" + droneStatus.batteryCurrent +
                "\nDrone battery voltage:" + droneStatus.batteryVoltage
        );
        if (droneStatus.isFly){
            TimeStampString = java.text.DateFormat.getDateTimeInstance().format(new Date());
            tool.writeTxtToFile(TimeStampString+"\t"+droneStatus.droneLatitude+"\t"+droneStatus.droneLongtitude+
                    "\t"+ df.format(droneStatus.droneSpeed) +
                    "\t"+ df.format(droneStatus.droneVerticalSpeed) +
                    "\t"+ droneStatus.droneHeading +
                    "\t"+ droneStatus.batteryPercentage +
                    "\t"+ droneStatus.batteryCurrent +
                    "\t"+ droneStatus.batteryVoltage
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
        runOnUiThread(() -> {
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
        });
    }

    public SettingsDefinitions.StorageLocation get_storage_location(){
        mCamera.getStorageLocation(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.StorageLocation>() {
            @Override
            public void onSuccess(SettingsDefinitions.StorageLocation storageLocation) {
                storageLocation = storageLocation;
            }
            @Override
            public void onFailure(DJIError djiError) {}
        });
        return storageLocation;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_locate:{
                //drawDroneInfo();
                cameraUpdate(); // Locate the drone's place
                //getSDStorage_state();
                set_storage();
                break;
            }
            case R.id.bt_clear:{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gMap.clear();
                    }

                });

                if (waypointList!=null)
                    waypointList.clear();
                if (waypointMissionBuilder!=null)
                    waypointMissionBuilder.waypointList(waypointList);
                if (polygon!=null)
                    polygon.remove();
                if (edgeLatLngList!=null)
                    edgeLatLngList.clear();
                if (markerList!=null)
                    markerList.clear();
                drawDroneInfo();
                break;
            }
            case R.id.bt_generate_path:{
                droneStatus.plannedSpeed = Math.round(Float.valueOf(tv_plannedSpeed.getText().toString()));
                drawDroneInfo();
                wayPointStack.clear(); //clear the stack of the wayPoint log
                startOffset = 0;
                generateWayPoint();
                break;
            }
            case R.id.bt_upload:{
                //set_storage();
                log_project();
                saveBoundary();
                saveWP();
                uploadMission(  true);
                break;
            }
            case R.id.bt_start:{
                if (SD_detected == false)
                    setResultToToast("Warning the SD card is not detected, proceed with caution");
                else
                    setResultToToast("Execute current flight plan");
                startWaypointMission();
                break;
            }
            case R.id.bt_recover:{
                setResultToToast("Resume the boundary of lst plan");
                loadBoundary();
                break;
            }
            case R.id.bt_resume:{
                setResultToToast("Resume the previous unfinished flight");
                uploadMission(false);
                break;
            }
            case R.id.bt_recover_WP:{
                setResultToToast("Load previous calculated WP");
                loadWP();
                constructWPList();
                DisplayWaypoint();
                break;
            }
            case R.id.bt_rewind:{
                withDrawMarker();
                break;
            }
            default:
                break;
        }
    }
    private void generateWayPoint(){
        setResultToToast("Generating Waypoint: "+ overlapratio / 100.0 +"%");
        ArrayList<GePoint> cornerListGeo = new ArrayList<>();
        try {
            for (Marker m:markerList)
                cornerListGeo.add(new GePoint(m.getPosition().latitude,m.getPosition().longitude));
            setResultToToast("Generate heights: "+ tv_plannedAltitude.getText().toString()+" Speed:"+(tv_plannedSpeed.getText().toString()));
            pathCalculation.UpdateBounds(cornerListGeo, Float.valueOf(tv_plannedAltitude.getText().toString()),droneStatus);
        } catch(Exception e)
        {
            setResultToToast("Error "+ e);
        }
        wpGeo = (ArrayList<GePoint>) pathCalculation.getWaypoints();
        wpIsTurn = (ArrayList<Boolean>) pathCalculation.getIsTurning();
        wpAltitude = (ArrayList<Double>) pathCalculation.getAltitudes();
        droneStatus.batteryPrecentageRemian =pathCalculation.getEnergyPercentRemainingAfterPlan();
        constructWPList();


        DisplayWaypoint();
    }
    private void constructWPList(){
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
        double distance=0;
        for (int i = 0;i<wpGeo.size();i++)
        {
            if(!wpIsTurn.get(i) && !wpIsTurn.get(i + 1)) {
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
                if(i+1<wpGeo.size() && !wpIsTurn.get(i + 1)){
                    mWaypoint.shootPhotoDistanceInterval= Float.parseFloat(String.valueOf(distance));
                }
                else
                    mWaypoint.shootPhotoDistanceInterval= 0;
                mWaypoint.speed = droneStatus.plannedSpeed;
                waypointList.add(mWaypoint);
            }

        }
        setResultToToast("Interval distance is: "+distance+"\nGenerate done");
    }
    private void log_project(){
        TimeStampString = java.text.DateFormat.getDateTimeInstance().format(new Date());
        fileName = "Project_"+java.text.DateFormat.getDateTimeInstance().format(new Date())+".txt";
        setResultToToast("Writing flight logs");
        String energyData = "TimeStamp"+"\tLatitude"+"\tLongtitude"+
                        "\tdroneSpeed"+
                        "\tdroneVerticalSpeed"+
                        "\tdroneHeading"+
                        "\tbatteryPercentage"+
                        "\tbatteryCurrent"+
                        "\tbatteryVoltage";

        String settingData ="Start project time: " + TimeStampString;
        settingData += "\nDrone setting info:\n";
        settingData +="\nBattery_info: "+ droneStatus.batteryPercentage +
                        "\nEstimate battery left: "+ droneStatus.batteryPrecentageRemian +
                        "\nSatellite count: "+ droneStatus.satelliteCount +
                        "\nSpeed_info: "+ df.format(droneStatus.droneSpeed) +
                        "\nSpeed set: "+ droneStatus.plannedSpeed +
                        "\nprePlannedSpeed set: "+ droneStatus.prePlannedSpeed +
                        "\nDrone current location: "+ droneStatus.droneLatitude +","+ droneStatus.droneLongtitude +
                        "\nDrone current Height: "+ df.format(droneStatus.droneHeight) +
                        "\nDrone heading : "+ droneStatus.droneHeading +
                        "\nDrone home location: "+ droneStatus.homeLatitude +","+ droneStatus.homeLongtitude +
                        "\nOverlap set: "+ droneStatus.overlapRatio;
        for (int i = 0;i<waypointList.size();i++) {
            settingData +="\nWaypoint_" + i + ": " + waypointList.get(i).coordinate.getLatitude() + "\t" + waypointList.get(i).coordinate.getLongitude() + "\t" + waypointList.get(i).altitude;
        }
        writeFiles(settingData,fileName,settingFilePath);
        writeFiles(energyData,fileName,energyfilePath);
        setResultToToast("Done writing");
    }
    private void withDrawMarker() {
        if (markerList.size() == 0) {
            setResultToToast("There is no points to rewind");
            return;
        }
        setResultToToast("rewind last point");
        Marker lst_marker = markerList.get(markerList.size() - 1);
        markerList.remove(markerList.size() - 1);
        lst_marker.remove();
        if (markerList.size() > 0) {
            renderPolygon();
        }
    }
    private void cameraUpdate()
    {
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
                gMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.turn_icon)).anchor(0.5f,0.5f).title(String.valueOf(i+1)+'_'+ wpAltitude.get(i)));
                generatedPath.add(new LatLng(wpGeo.get(i).latitude, wpGeo.get(i).longtitude));
            }
            else
                gMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_icon)).anchor(0.5f,0.5f).alpha(0.5f).title(String.valueOf(i+1)+'_'+ wpAltitude.get(i)));
        }
        pathPoly = gMap.addPolyline(generatedPath);

    }

    private void uploadMission(boolean newMission){
        if (waypointMissionBuilder == null) {
            waypointMissionBuilder = new WaypointMission.Builder();
        }
        if (newMission){
            waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
        }
        else{
            List<Waypoint> remainWaypoint = new ArrayList<>();
            startOffset = Collections.max(wayPointStack);
            for (int i = startOffset; i<waypointList.size(); i++){
                remainWaypoint.add(waypointList.get(i));
            }
            setResultToToast("Resuming waypoints "+(waypointList.size()-wayPointStack.size())+" out of "+waypointList.size());
            waypointMissionBuilder.waypointList(remainWaypoint).waypointCount(remainWaypoint.size());
        }
        mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
        mHeadingMode = WaypointMissionHeadingMode.AUTO;
        configWayPointMission(newMission);
        uploadWayPointMission();
    }

    private void configWayPointMission(boolean newMission){

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

//        if (waypointMissionBuilder.getWaypointList().size() > 0){
//
//            for (int i=0; i< waypointMissionBuilder.getWaypointList().size(); i++){
//                waypointMissionBuilder.getWaypointList().get(i).altitude = altitude;
//            }
//            setResultToToast("Set Waypoint attitude successfully");
//        }

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
                }
                else {
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
            droneStatus.overlapRatio = i;
            overlapratio = i;
            tv_OverLapRatio.setText("overlap: "+i+"%");
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
    public void set_storage() {
        DJIDemoApplication.getCameraInstance().setStorageStateCallBack(new StorageState.Callback() {
                @Override
                public void onUpdate(@NonNull StorageState storageState) {
                    if(storageState.isInserted()) {
                        storageLocation = SettingsDefinitions.StorageLocation.SDCARD;
                        SD_detected = true;

                        DJIDemoApplication.getCameraInstance().setStorageLocation(SettingsDefinitions.StorageLocation.SDCARD, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                            }
                        });
                        if(storageState.getStorageLocation()==storageLocation)
                            TotalAvaSpace = storageState.getTotalSpaceInMB()/1000;
                    } else {
                        SD_detected = false;
                        TotalAvaSpace = -1;
                       setResultToToast("SD card not detected, proceed with caution");
                    }
                }
            });
    }


    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }
    private void writeFiles(String inputText,String filename,String filepath) {
        File myExternalFile = new File(getExternalFilesDir(filepath), filename);
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            setResultToToast("Not able to write logs");
        } else {
            try {
                FileOutputStream fos = new FileOutputStream(myExternalFile);
                fos.write(inputText.getBytes());
                fos.close();
            } catch (IOException e) {
                setResultToToast(Arrays.toString(e.getStackTrace()));
            }
        }
    }
    private void saveWP(){
        String wpGeo_str="";
        String wpIsTurn_str = "";
        String wpAltitude_str = "";
        for (GePoint p:wpGeo){
            wpGeo_str+=p.latitude+"/"+p.longtitude+",";
        }
        for (Boolean p:wpIsTurn){
            wpIsTurn_str+=p+",";
        }
        for (Double a:wpAltitude){
            wpAltitude_str+=a+",";
        }
        writeFiles(wpGeo_str,"Last_wpGeo.txt",wayPointFilePath);
        writeFiles(wpIsTurn_str,"Last_wpIsTurn.txt",wayPointFilePath);
        writeFiles(wpAltitude_str,"Last_wpAltitude.txt",wayPointFilePath);
    }
    private void saveBoundary(){
        String points = "";
        for (Marker m:markerList){
            points = points+ m.getPosition().latitude +"/"+ m.getPosition().longitude +",";
        }
        writeFiles(points,"Last_boundary.txt",wayPointFilePath);
    }
    private String readFiles(String filename,String filepath){
        String outData = "";
        File myExternalFile = new File(getExternalFilesDir(filepath), filename);
        try {
            FileInputStream fis = new FileInputStream(myExternalFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                outData = outData + strLine;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outData;
    }
    private void loadBoundary(){
        markerList.clear();
        gMap.clear();
        String data = readFiles("last_boundary.txt",wayPointFilePath);
        for (String value:data.split(",")){
            double lat = parseDouble(value.split("/")[0]);
            double lot = parseDouble(value.split("/")[1]);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(lat,lot));
            Marker marker = gMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_icon)).draggable(true).title(String.valueOf(markerList.size()+1)));
            markerList.add(marker);
            renderPolygon();
        }

    }
    private void loadWP(){
        wpGeo.clear();
        wpIsTurn.clear();
        wpAltitude.clear();

        String data = readFiles("last_wpGeo.txt",wayPointFilePath);
        for (String value:data.split(",")){
            double lat = parseDouble(value.split("/")[0]);
            double lot = parseDouble(value.split("/")[1]);
            GePoint p = new GePoint(lat,lot);
            wpGeo.add(p);
        }

        data = readFiles("last_wpIsTurn.txt",wayPointFilePath);
        for (String value:data.split(",")){
            boolean d = Boolean.parseBoolean(value);
            wpIsTurn.add(d);
        }

        data = readFiles("last_wpAltitude.txt",wayPointFilePath);
        for (String value:data.split(",")){
            double alt = parseDouble(value);
            wpAltitude.add(alt);
        }

    }
}