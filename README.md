# GSDemo
## TL;DR
This is a demo app modified from Dji GSO Demo project. Developed for fully automatic self-path planning/ROI(region of interest) coverage.
User can use this APP on mobile device to draw the region and configurative the drones with altitude/flying speed, camera settings etc. After the configuration the drone are capable of proceeding the mission automatically without any further interactions.
## APP requirements & Compatibility  
This APP is currently run on Android platform, it can be installed on mobile device(Android phone) or DJI smart controller(https://www.dji.com/smart-controller)(Out of Date), the compatibility to the newer version DJI RC Pro(https://www.dji.com/rc-pro?site=brandsite&from=nav) hasn't been tested, please proceed with caution.

The drone used with this system is Mavic 2 Pro(https://www.dji.com/mavic-2), please be notice that Mavic mini/Mavic 2 zoom is not supported.

## Before using the APP
Take the DJI smart controller as example (the procedure of using an Android phone is mostly same), please make sure the device is connect with the data_stream/WIFI. And confirm that the APP is installed correctly.

Make sure the controller is connected with the drone, you can refer to the video here(https://www.youtube.com/watch?v=c8J4pEyzt2A)

## Using the APP to deploy mission
After the login in process, you should be prompt to a new screen with satellite map, and this guide will provide step by step guide for deploy the mission.

### Registering the APP
Open the APP and you should be prompt with a register screen, see the attached image below, at this stage the APP will go through all necessary pre-requisites including registering to the DJI API and checking the connection between the drone and the controller. **Please make sure the internet is available before open the APP.**  You should be expecting to see the 'open' button activated in 10s at this screen.


>please attach an screen shoot here to demo the login screen

### Configure the path planning plan

Once the register process succeed, you will be prompted with a satellite map where the drone is located. You can zoom in/out with two fingers just like google map. Please allow 10-20s for the map to be loaded if didn't see the satellite map. **if you didn't see the drone marks please click the button 'locate'.** 

### Draw the ROI(region of interest)

When using single finger click on the map, you will be able to place an anchor(red mark) on the map, this will be used as a corner point of the region user is interested. In order to successfully create a ROI, at least 3 marks(clockwise/anti-clockwise) shall be placed, and a polygon in light green will demonstrate the region being selected. If needed, each anchor can be adjusted with finger long press the marker and drag to adjust.
If there are markers created unexpected, user can delete last marker by using the button 'REWIND', and last marker will be removed.

>please attach an screen shoot here to demo ROI process with drone mark in middle

### Configure the path settings
Once the ROI is defined (after previous step you shall see a light green polygon on map), user will be able to configure the altitude/speed/overlaps between images, these parameters can be easily inputted into the box/sliding bar on the bottom of the screen. We use meters in altitude and m/s for speed, and for pix4D post-process requirements we recommend to set overlap (sliding bar on the bottom) to be over 70%. Some common default settings are listed below:
```
Altitude: 15m ; Speed: 4m/s
Altitude: 30m ; Speed: 5m/s
Altitude: 45m ; Speed: 8m/s
Altitude: 60m ; Speed: 10m/s
Altitude: 90m ; Speed: 10m/s
```
>please attach an screen shoot here to demo drone configuration

### Generate the path planning trajectory and upload mission
Once the configuration is completed, click 'generate' button and the system shall automatically calculate the optimal trajectory based on energy efficiency, and a back-force black trajectory will pop on screen showing the actual flying path of the drone. Once the user is satisfied with the trajectory, simply click 'UPLOAD' and the status bar of the drone(showing on screen right side, named 'drone status') will change to 'uploading' and then showing 'READY TO EXECUTE' showing that the drone is ready for the mission.

>please attach an screen shoot here to demo drone status ready to execute.

### Execute the mission
After making sure the drone status is 'READY TO EXECUTE' and fullfil the pre-flight checklist, click button 'EXECUTE' and the drone shall be taken off immediately. Meanwhile user can check the drone status on the right side of the screen monitoring the status of the drone.


### Operations during the flight
When the drone is in air, it is possible to overwrite the current mission procedure in case hazard situation observed. Since all the physical buttons on the controller has higher privilege than the APP, user can use these physical button to **PAUSE** the drone with the red pause button and the "RETURN TO Home" Button to bring the drone back to where it departs. More details can be found with this link:https://dl.djicdn.com/downloads/smart+controller/20190110-2/DJI_Smart_Controller_User_Manual_EN_V1.0_0110.pdf

## Other functions
The system also contains some utility functions that can assist user gaining better results, they will be introduced in this section

### Camera settings

On the main screen there is a 'CAMERA' button, click it will brings up a new page showing the current view of the drone camera and available camera settings that can be tuned, the system also provides three preset settings for convenience:
```
Preset 1 – For Sunny -> iso: 400, f-stop: 6.3, shutter speed: 1/1600
Preset 2 – For Cloudy -> iso: 800, f-stop: 5.6, shutter speed: 1/1000
Preset 3 – For vegetation/mapping -> shutter priority: 1/1600
```
>please attatch an scrren shoot of camera settings


### Drone status 
On the right side of the screen, the system will list all the available drone status, some key elements are describes below:
```
Height/Lat/Lot: Drone GPS info including the current height of the drone
Speed_info: Drone current actual ground speed
Battery_info: Current drone battery percentage
Satellite count: Measure the GPS signal quality
drone Mission Status: current drone status.
drone Storage (GB): Available storage on the SD card inserted.
Camera Mode: Camera settings, either shutter priority/Manual/Auto
Drone battery current: The instant current draw by the drone
Drone battery voltage: The instant voltage the drone battery provides.
```
> please attatch a screen shot on the drone status, better in the air

### Mission Resuming
In some cases the drone cannot finish the entire trajectory as planned, in this case the system can automatically resume the unfinished mission by following the procedure as follows:

- Step1: When the drone detect battery lows, controller will have a warning sound user can either manually bring drone back or the drone will automatically stop current mission and return to home point

- Step2: Once the drone is landed, turn off the done(keep the controller open) replace the battery and turn on the drone

- Step3: Click the button 'RESUME_LST_MISSION', once the drone status changed to 'READY_TO_EXECUTE' that indicates the resumed mission is uploaded successfully.

- Step4: Click the button 'EXECUTE_MISSION' the drone will take off and resume to the last waypoint it stopped from previous flight


### In case APP crash
During the flight, if the controller side APP crashed/turn off wont affect the mission execution, user can use the two buttons 'RECOVER_POLYGON' and 'REGENERATE_WP' to recover polygon and trajectory that was calculated previously to restore the mission details for monitoring.(Note this process wont affect the drone that is currently in the mission.)


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
