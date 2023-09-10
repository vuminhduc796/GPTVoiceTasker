package com.research.activityinvoker.model;

import android.view.accessibility.AccessibilityNodeInfo;

public class LabelFoundNode {
    public String label;
    public AccessibilityNodeInfo nodeInfo;

    public LabelFoundNode(AccessibilityNodeInfo nodeInfo, String label) {
        this.label = label;
        this.nodeInfo = nodeInfo;
    }
}
