# GSDemo
This is a demo app modified from Dji GSO Demo, adding functions of self-path planning(Given a specific area generate a round trip path to cover the areas'
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
      Click generate, this shall generate the calculated path, at this stage the polygon will be stroed as tmp file
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
    Add exceptional handle func for APP crush to restore all necessary data
