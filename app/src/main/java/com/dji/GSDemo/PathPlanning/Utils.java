package com.dji.GSDemo.PathPlanning;

public class Utils {
	
	public static void addLineToSB(StringBuffer sb, String name, Object value) {
        if (sb == null) return;
        sb.
        append((name == null || "".equals(name)) ? "" : name + ": ").
        append(value == null ? "" : value + "").
        append("\n");
    }
	
}
