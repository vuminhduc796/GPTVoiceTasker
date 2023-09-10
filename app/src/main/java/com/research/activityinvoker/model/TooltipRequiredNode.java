package com.research.activityinvoker.model;

import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

public class TooltipRequiredNode {
    public AccessibilityNodeInfo nodeInfo;
    public int tooltipNumber;
    public FrameLayout tooltipLayout;

    public TooltipRequiredNode(AccessibilityNodeInfo nodeInfo, int tooltipNumber, FrameLayout tooltipLayout) {
        this.nodeInfo = nodeInfo;
        this.tooltipNumber = tooltipNumber;
        this.tooltipLayout = tooltipLayout;
    }

}
