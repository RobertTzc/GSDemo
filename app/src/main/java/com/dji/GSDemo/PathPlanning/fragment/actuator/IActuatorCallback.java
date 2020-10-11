package com.dji.GSDemo.PathPlanning.fragment.actuator;

import dji.common.mission.waypointv2.Action.WaypointActuator;

public interface IActuatorCallback {
    WaypointActuator getActuator();
}
