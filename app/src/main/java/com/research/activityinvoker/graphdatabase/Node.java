package com.research.activityinvoker.graphdatabase;

import android.view.accessibility.AccessibilityNodeInfo;

public class Node {
    public static int currentNodeCount;
    public String activityName;
    public String clickableElements;
    public String description;
    public String screenDumperXML;
    public int nodeID;

    public Node(String activityName, String clickableElements, String description, String screenDumperXML) {
        this.activityName = activityName;
        this.clickableElements = clickableElements;
        this.description = description;
        this.screenDumperXML = screenDumperXML;
        currentNodeCount += 1;
        nodeID = Utils.generateID();
    }

    public Node( String activityName, String clickableElements, String description, String screenDumperXML, int nodeID) {
        this.activityName = activityName;
        this.clickableElements = clickableElements;
        this.description = description;
        this.screenDumperXML = screenDumperXML;
        this.nodeID = nodeID;
    }
}
