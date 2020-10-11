package com.dji.GSDemo.PathPlanning;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;


import edu.missouri.frame.GePoint;
import edu.missouri.frame.ReadFlightParameters;

import static edu.missouri.frame.ReadFlightParameters.splitPointString;


public class SelfPathPlanning extends FragmentActivity implements View.OnClickListener, GoogleMap.OnMapClickListener, OnMapReadyCallback, SeekBar.OnSeekBarChangeListener {

        protected static final String TAG = "GSDemoActivity";

        private GoogleMap gMap;
        private Button locate, clear,generate,upload;
        private Button  start, stop;
        private EditText Altitude;
        private TextView tv_overlapratio;
        private CheckBox show;
        private SeekBar sb_overlapratio;

        private boolean isAdd = true;
        public ArrayList<GePoint> cornerListGeo = new ArrayList<GePoint>();
        private double droneLocationLat = 181, droneLocationLng = 181;;
        private Marker droneMarker = null;

        private float altitude = 100.0f;
        private float mSpeed = 10.0f;
        private  int overlapratio=20;

        ArrayList<Marker> markerList = new ArrayList<>();
        private List<Waypoint> waypointList = new ArrayList<>();

        public static WaypointMission.Builder waypointMissionBuilder;
        private FlightController mFlightController;
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
            start = (Button) findViewById(R.id.bt_start);
            stop = (Button) findViewById(R.id.bt_stop);
            generate = (Button)findViewById(R.id.bt_generate_path);
            upload = (Button)findViewById(R.id.bt_upload);
            show = findViewById(R.id.cb_show);
            sb_overlapratio = findViewById(R.id.sb_overlapratio);
            tv_overlapratio = findViewById(R.id.tv_overlapratio);


            locate.setOnClickListener(this);
            clear.setOnClickListener(this);
            start.setOnClickListener(this);
            stop.setOnClickListener(this);
            generate.setOnClickListener(this);
            upload.setOnClickListener(this);
            sb_overlapratio.setOnSeekBarChangeListener(this);
            show.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
            if (product != null && product.isConnected()) {
                if (product instanceof Aircraft) {
                    mFlightController = ((Aircraft) product).getFlightController();
                }
            }

            if (mFlightController != null) {
                mFlightController.setStateCallback(new FlightControllerState.Callback() {

                    @Override
                    public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
                        droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                        droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                        updateDroneLocation();
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
                try {
                    altitude = Integer.parseInt(alt);
                } catch (Exception e) {
                    altitude = 100;
                }
                edgeAltitudeList.add(altitude);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(point);
                Marker marker = gMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_icon)).draggable(true).title(String.valueOf(markerList.size()+1)+'_'+String.valueOf(altitude)));
                markerList.add(marker);
            }
        }


        public static boolean checkGpsCoordination(double latitude, double longitude) {
            return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
        }

        // Update the drone location based on states from MCU.
        private void updateDroneLocation(){

            LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
            //Create MarkerOptions object
            final MarkerOptions droneMarkerOptions = new MarkerOptions();
            droneMarkerOptions.position(pos);
            droneMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (droneMarker != null) {
                        droneMarker.remove();
                    }

                    if (checkGpsCoordination(droneLocationLat, droneLocationLng)) {
                        droneMarker = gMap.addMarker(droneMarkerOptions);
                    }
                }
            });
        }




        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_locate:{
                    updateDroneLocation();
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
                    show.setChecked(false);
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
                    updateDroneLocation();
                    break;
                }
                case R.id.bt_generate_path:{
                    if (show.isChecked()==false){
                        setResultToToast("please generate area first");
                        break;
                    }
                    setResultToToast("Generating Waypoint");

                    try {
                        pathCalculation.UpdateBounds(cornerListGeo, cornerListGeo.get(0), edgeAltitudeList.get(0), 20);
                    } catch(Exception e)
                    {
                        setResultToToast("Error "+e.toString());
                        break;
                    }
                    wpGeo = (ArrayList<GePoint>) pathCalculation.getWaypoints();
                    wpIsTurn = (ArrayList<Boolean>) pathCalculation.getIsTurning();
                    wpAltitude = (ArrayList<Double>) pathCalculation.getAltitudes();
                    DisplayWaypoint();
                    setResultToToast("Num_waypoint:"+String.valueOf(wpGeo.size()));
                    PolylineOptions wpTrace = new PolylineOptions();
                    for (int i = 0;i<wpGeo.size();i++) {
                        if (wpIsTurn.get(i) == false) {
                            Waypoint mWaypoint = new Waypoint(wpGeo.get(i).latitude, wpGeo.get(i).longtitude, edgeAltitudeList.get(0));
                            if (waypointMissionBuilder != null) {
                                waypointList.add(mWaypoint);
                                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
                            } else {
                                waypointMissionBuilder = new WaypointMission.Builder();
                                waypointList.add(mWaypoint);
                                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
                            }
                        }else{
                            continue;
                            //take pictures only
                            }
                    }
                    mSpeed = 5.0f;
                    mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
                    mHeadingMode = WaypointMissionHeadingMode.AUTO;
                    cornerListGeo.clear();
                    setResultToToast("Generate done");
                    //after the output from the code generate waypoint

                    break;
                }
                case R.id.bt_upload:{
                    configWayPointMission();
                    uploadWayPointMission();
                    break;
                }
                case R.id.bt_start:{
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
            LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
            float zoomlevel = (float) 18.0;
            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoomlevel);
            gMap.moveCamera(cu);
        }

    public void DisplayWaypoint(){
            PolylineOptions generatedPath = new PolylineOptions();
            for (int i =0;i<wpGeo.size();i++){
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(wpGeo.get(i).latitude,wpGeo.get(i).longtitude));
                Marker marker = null;
                if (wpIsTurn.get(i))
                    marker= gMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.turn_icon)));
                else
                    marker = gMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_icon)));
                generatedPath.add(new LatLng(wpGeo.get(i).latitude,wpGeo.get(i).longtitude));
            }
            pathPoly = gMap.addPolyline(generatedPath);

    }

    String nulltoIntegerDefalt(String value){
            if(!isIntValue(value)) value="0";
            return value;
        }

        boolean isIntValue(String val)
        {
            try {
                val=val.replace(" ","");
                Integer.parseInt(val);
            } catch (Exception e) {return false;}
            return true;
        }

        private void configWayPointMission(){

            if (waypointMissionBuilder == null){

                waypointMissionBuilder = new WaypointMission.Builder().finishedAction(mFinishedAction)
                        .headingMode(mHeadingMode)
                        .autoFlightSpeed(mSpeed)
                        .maxFlightSpeed(mSpeed)
                        .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

            }else
            {
                waypointMissionBuilder.finishedAction(mFinishedAction)
                        .headingMode(mHeadingMode)
                        .autoFlightSpeed(mSpeed)
                        .maxFlightSpeed(mSpeed)
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

            LatLng MU = new LatLng(38.946766, -92.331883);
            //gMap.addMarker(new MarkerOptions().position(MU).title("University of Missouri"));
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MU,15));
            gMap.moveCamera(CameraUpdateFactory.newLatLng(MU));
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (seekBar.getId() == R.id.sb_overlapratio){
                overlapratio = i;
                tv_overlapratio.setText("overlap: "+String.valueOf(i)+"%");
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
