# GSDemo
## TL;DR
This App is modified from the Dji GSO Demo project developed for fully automatic self-path planning across a region of interest (ROI). User can use this APP on a mobile device or smart controller to draw the region and configure the drones’ altitude, flying speed, camera settings, and other parameters. After configuration, the drones can complete the mission automatically without any further instruction or inputs.
## APP requirements & Compatibility  
This APP is currently run on Android platform and may be installed on mobile devices (Android phone) or DJI smart controller (https://www.dji.com/smart-controller) (Out of Date). The compatibility to the newer version DJI RC Pro (https://www.dji.com/rc-pro?site=brandsite&from=nav) hasn't been tested - please proceed with caution if using this platform.

This system was developed for the  Mavic 2 Pro (https://www.dji.com/mavic-2). The Mavic mini/Mavic 2 zoom is not supported.


## Before using the APP
Example images and videos are taken from the DJI smart controller (the procedure of using an Android phone is mostly the same). Please make sure the device is connected with the internet through mobile data_stream/WIFI. And confirm that the APP is installed correctly.

Make sure the controller is properly connected to the drone. For help, refer to the video here (https://www.youtube.com/watch?v=c8J4pEyzt2A).


## Using the APP 
### Registering the APP

Open the APP and a  registration screen will appear (see  image below). At this stage the APP will go through all necessary pre-requisites, including registering to the DJI API and checking the connection between the drone and the controller. **Please make sure the controller is connected to the internet and drone and internet is available before opening the APP.** The 'Open' button should be available in less than 10 seconds at this screen.

![loginscreen](https://user-images.githubusercontent.com/71574752/205713830-cd0797b6-00a3-4c30-816b-82aeeb3bfa41.JPG)

### Configure the path planning plan

Once the register process succeeds, the app will open to a satellite map where the drone is located. Use two fingers to pinch to zoom in/out and pan with one finger just like on the Google maps app. The satellite may take time to load depending on internet quality and speed. **If the map loads and the  drone or controller location marks don’t appear,  please click the ‘locate’ button to be taken to the location of the drone on the map.**


![satellitemap_drone](https://user-images.githubusercontent.com/71574752/205713919-668d273b-0056-4922-ac26-04f741937396.JPG)

### Draw the ROI(region of interest)

To mark the ROI, use a single finger to tap/click on the map in a corner of the ROI to place an anchor (red mark) on the map. Continue taping/clicking in a clockwise or counterclockwise order around the map to generate the ROI.  To successfully create a ROI, at least 3 marks shall be placed, and a polygon in light green will demonstrate the region selected. If needed, each anchor can be adjusted with single finger long press on the marker and then dragging the marker to adjust. If extra or unwanted markers are created, the last marker created may be deleted  by using the ‘Rewind’ button, and the last marker created will be removed.

![ROI](https://user-images.githubusercontent.com/71574752/205713955-487b552e-c1c0-40c1-9998-ceeaa7d947c5.JPG)


### Configure the path settings

Once the ROI is defined (light green polygon on map), configure the altitude, flight speed, and overlaps between images using the settings at the bottom. Altitude is set in meters above takeoff and flight speed is set in m/s. For vegetation surveys or if pix4D post-processing is desired, we recommend setting the overlap to be over 70%. Some recommended settings to start with are listed below:
```
Altitude:15m Speed:4m/s
Altitude:30m Speed:5m/s
Altitude:45m Speed:8m/s
Altitude:60m Speed:10m/s
Altitude:90m Speed:10m/s
```

![configurethepathsettings](https://user-images.githubusercontent.com/71574752/205713989-0a5d251f-4ab1-4410-8e52-7c95eaf461cb.JPG)


### Generate the path planning trajectory and upload mission

Once the flight configuration is complete, click the ‘Generate' button and the system shall automatically calculate the optimal energy efficient trajectory to survey the area. The trajectory will appear on screen showing the actual flying path of the drone with black lines and camera symbols to indicate image locations. If the trajectory is not satisfactory, use the ‘Clear’ to start over with the defining ROI step. Once the user is satisfied with the trajectory, click 'UPLOAD' and the status bar of the drone (shown on the right side  next to 'drone Mission Status') will change to 'Uploading' and then show 'READY TO EXECUTE' once the mission has been uploaded and the drone is ready for the mission.

![generatepath](https://user-images.githubusercontent.com/71574752/205714035-cd340b5e-1314-4b50-ae8c-249c8a6b1614.JPG)


###Camera settings

To adjust the camera settings for the flight, there is a 'CAMERA' button. This button brings up a new page showing the current view of the drone camera and available camera settings that can be tuned. Three preset camera settings are provided for starting points for the surveys, depending on survey type and weather conditions.
```
Preset 1 – For Sunny -> iso: 400, f-stop: 6.3, shutter speed: 1/1000
Preset 2 – For Cloudy -> iso: 800, f-stop: 5.6, shutter speed: 1/1000
Preset 3 – For vegetation/mapping -> shutter priority: 1/1600
```

![camerasettings](https://user-images.githubusercontent.com/71574752/205714055-be575c7f-fe46-4035-9e09-5b759ba13a1c.JPG)


### Execute the mission

After making sure the drone status is 'READY TO EXECUTE' and the pre-flight checklist has been completed, click 'EXECUTE' and the drone will begin the mission (immediate take-off). During flight, drone status may be monitored using the information panel on the right side of the screen showing current information from the drone.

### Drone status
On the right side of the map screen, the system will list all the available drone status parameters.
```
Battery info: Current drone battery percentage
Satellite count: Measure the GPS signal quality
Speed info: Drone current actual ground speed
Speed set: Speed set by the user
Height: Drone height above takeoff location 
Overlap set: Percentage of overlap set by the user
Drone heading: Current heading 0-360 degrees (0 is north)
Battery estimate: Estimate of the amount of current battery that will be remaining upon completion of the flight
drone Mission Status: current drone status
drone Storage (GB): Available storage on the SD card inserted (-1.0 GB means NO SD CARD INSERTED)
Camera Mode: Camera settings, either shutter priority/Manual/Auto
Camera shutter/ISO/Aperture/ExposureCompensation: Current camera parameters set for the mission
Drone battery current: The instant current draw by the drone
Drone battery voltage: The instant voltage the drone battery provides
```

![dronestatus_missioninprogress](https://user-images.githubusercontent.com/71574752/205714108-9a0d55c3-d4ba-4f1d-97a6-830e68a8da3b.JPG)


### Mission Resuming

If the drone cannot finish the entire trajectory in one battery, the system can automatically resume the unfinished mission after exchange of batteries using  the following procedure:
- Step1: When the drone detects a low battery during flight, the controller will begin emitting a low battery warning sound. The user can either manually bring drone back using the return-to-home button on the controller or the user may wait and the drone will automatically stop the current mission and return to the home location at a certain calculated battery level.
- Step2: Once the drone has landed, turn off the done (keep the controller open/on), replace the battery, and turn on the drone.
- Step3: Once the drone has restarted and reconnected to the remote, click the 'RESUME_LST_MISSION' button to upload the remaining portion of the mission to the drone. Once the drone status has changed to 'READY_TO_EXECUTE' that indicates the resumed mission is uploaded successfully.
- Step4: Click the 'EXECUTE_MISSION' button and the drone will take off and resume to the last waypoint it stopped from the previous flight and continue the mission.

### In case APP crash

During the flight, if the controller APP crashes or turns off, it will not affect the mission execution. To recover the current mission polygon and waypoints, use the two buttons 'RECOVER_POLYGON' and 'REGENERATE_WP' to recover polygon and trajectory that was calculated previously to restore the mission details for monitoring. This process will not affect the drone currently in the mission, however, use with caution as if the current mission is stopped and then resumed (such as to replace the drone battery), bugs may appear that require you to restart the entire mission.

# Emergency operation

While in flight operations, it is possible to override the current mission procedure in case of hazardous situations. All physical buttons on the controller have higher privilege than the APP, so the user may  use these physical button to **PAUSE/STOP** the drone (red pause button) which will pause the drone where it is, and the "RETURN TO HOME" Button to bring the drone back to where it was launched from (at the preset altitude set for the drone). More details can be found with here:  https://dl.djicdn.com/downloads/smart+controller/20190110-2/DJI_Smart_Controller_User_Manual_EN_V1.0_0110.pdf


**Pause** can be done by single short press on the red pause button. While the mission was paused the drone will hover in the sky and wait for further instruictions (drone status will show execution paused), joystick input will not be taken at this stage. Possible following operations are return to home button, resuming mission.

**Stop** can be done by single short click follow with a long press on the red pause button, the drone will hover in the air and terminate the current mission(wont able to resume), and at this stage the joystick input will be taken as manual control. Possible following operations are return to home button, or Joystick overwrite.

<!-- 
the details of the button are listed below:
```
locate: locate the current drone location and home position of the drone. It will be show on the mark with blue drone icon and black house icon.
generate: Generate automatic path planning trajectory after the ROI(region of interest) is drawed and drone configuration is complete.
```





(Given a specific area generate a round trip path to cover the areas'
For testment, generate your own Dji API key and Google map API keys and paste them into the manifest.xml file
Work to do:
   Bugs to be fixed: cannot locate the drone before apply the path planning algorithm
   
#Usage(Updated on Sep 4th 2022): 
#Normal flight plan generation:
   Phase1:
      Click corners on the map and circle out the ROI for the current flight plan
      Each mark is clickable and the polygon shall be generated in counter clock wise based on the order of click
      Polygon area will auto generate based on marker
   Phase2:
      Input key parameters, such as height/altitude/overlaps
      Click generate, this shall generate the calculated path, at this stage the polygon will be stored as tmp file
      Click upload, this will upload the flight plans to the drone also create a local tmp file stores the flight plan in case lost data
   Phase3:
      Click execute_mission this will allow the drone to automatic finish the job
#In case the battery cannot complete the flight plan.
   Phase1: 
      Either manulally bring back the drone or let the low battery procedure take control to land the drone
      Dont close app,you can turn off the drone and replace battery
      Once drone is back online, click resume_lst_mission, this will upload unfinished part of the mission into the drone
   Phase2:
      Once the drone is in read to execute mode, Click Execute_mission and the program should take care of it.
#In case the APP crush:
   Phase 1:
      Click recover polygon will retrieve the last polygon you drawed
   Phase 2.1:
      Click the regenerate WP will retrieve the last wp you generated, plz be noticed if the polygon and wp doesnt match, please clear the map and redo phase 1, and then skip to phase 2.2
    Phase 2.2:
      If the wp doenst match with polygon in phase2.1, after redo phase1, click generate, this will regenerate a WP based on new calculation
    Phase 3:
      Click upload/resume_lst_mission(current version is not yet support resume unfinished flight after app crash)

ChangeLog:
This APP is developed for University of Missouri Computer Science Lab under Dr Shang, it solo purpose is to collect aerial image data with DJI Mavic 2.
Change Log
2022 Sep_01 Update:
    Added map polygon dynamic generation, replace the current check box options
    Added rewind button, user can use that easily to withdraw the last point that they don't want
    Changed the speed and height settings, these settings will be taken only when the flight plan is executed.
2022 Sep_03-Sep_04 Update:
    Fixed current BUGS of recording flight plans on external storage and add an tmp file stores last flight plan for resuming
    Added the resume button for last flight polygon resuming, wp can be recalculated when the polygon is resumed
    Added monitor on wp status, drones can continue last wp mission once the battery is changed(if the APP is terminated this func will not work)
    Added 15 exceptional handle func for stable purposes
TODO:
    Need a field test, currently pass all simulator test
    Add exceptional handle func for APP crush to restore all necessary data -->
