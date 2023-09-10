package com.research.activityinvoker.graphdatabase;

import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.diff.Difference;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Utils {

    public static boolean isScreenSimilar(AccessibilityNodeInfo node1, AccessibilityNodeInfo node2) {
        if (node1 == null || node2 == null) {
            return false;
        }

        if (!node1.getClassName().equals(node2.getClassName())) {
            return false;
        }

        Rect node1Bounds = new Rect();
        node1.getBoundsInScreen(node1Bounds);
        Rect node2Bounds = new Rect();
        node2.getBoundsInScreen(node2Bounds);
        if (!node1Bounds.equals(node2Bounds)) {
            return false;
        }

        int node1ChildCount = node1.getChildCount();
        int node2ChildCount = node2.getChildCount();
        if (node1ChildCount != node2ChildCount) {
            return false;
        }

        for (int i = 0; i < node1.getChildCount(); i++) {
            AccessibilityNodeInfo child1 = node1.getChild(i);
            AccessibilityNodeInfo child2 = node2.getChild(i);
            boolean identical = isScreenSimilar(child1, child2);
            child1.recycle();
            child2.recycle();
            if (!identical) {
                return false;
            }
        }

        return true;
    }

    public static String clickableElementAsString (AccessibilityNodeInfo accessibilityNodeInfo) {
        String output = "Clickable element: ";
        List<AccessibilityNodeInfo> clickableNodes = new ArrayList<>();
        findClickableNodes(accessibilityNodeInfo, clickableNodes);

        for (AccessibilityNodeInfo node : clickableNodes) {
            boolean nodeHasContent = false;

            if (node.getTooltipText() != null && !node.getTooltipText().toString().equals("")) {
                output += node.getTooltipText().toString();
                nodeHasContent = true;
            }

            if(node.getText() != null && !node.getText().toString().equals("") && !nodeHasContent) {
                output += node.getText().toString();
                nodeHasContent = true;
            }

            if(node.getViewIdResourceName() != null && !node.getViewIdResourceName().equals("") && !nodeHasContent) {
                String[] content = node.getViewIdResourceName().split("id/");
                output += content[1];
                nodeHasContent = true;
            }



            if (node.getContentDescription() != null && !node.getContentDescription().toString().equals("") && !nodeHasContent){
                output += node.getContentDescription().toString();
                nodeHasContent = true;
            }
        }

        return output;
    }

    private static void findClickableNodes(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> clickableNodes) {
        if (node == null) {
            return;
        }

        if (node.isClickable()) {
            clickableNodes.add(node);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);

            if (child != null) {
                findClickableNodes(child, clickableNodes);
                child.recycle();
            }

        }
    }

    public static boolean calculateStringSimilarity(String str1, String str2) {
        str1 = str1.replaceAll(" id=\"[0-9]+\"", "");
        str2 = str2.replaceAll(" id=\"[0-9]+\"", "");
        int len1 = str1.length();
        int len2 = str2.length();
        int[][] distance = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            distance[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            distance[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;
                distance[i][j] = Math.min(Math.min(distance[i - 1][j] + 1, distance[i][j - 1] + 1), distance[i - 1][j - 1] + cost);
            }
        }

        double sim = 1.0 - ((double) distance[len1][len2] / Math.max(len1, len2));

        return sim > 0.7;
    }

    public static boolean calculateDescSimilarity(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();
        int[][] distance = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            distance[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            distance[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;
                distance[i][j] = Math.min(Math.min(distance[i - 1][j] + 1, distance[i][j - 1] + 1), distance[i - 1][j - 1] + cost);
            }
        }

        double sim = 1.0 - ((double) distance[len1][len2] / Math.max(len1, len2));

        return sim > 0.7;
    }

    public static int generateID() {
        Random r = new Random();
        return (r.nextInt(999 - 100) + 100);
    }





}
