package com.research.activityinvoker;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.accessibility.AccessibilityNodeInfo;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class AccessibilityNodeInfoDumper {
    private static final String[] NAF_EXCLUDED_CLASSES = new String[] {
            android.widget.GridView.class.getName(),
            android.widget.GridLayout.class.getName(),
            android.widget.ListView.class.getName(),
            android.widget.TableLayout.class.getName(),
            android.widget.TableRow.class.getName(),
    };

    /**
     * Returns a string representation of the hierarchy starting at the given node.
     */
    public static int currentIndex = 0;

    public static void dumpNodeRec(AccessibilityNodeInfo node, XmlSerializer serializer, int index, boolean isPreviousContainer, int width, int height, boolean isParentClickable) throws IOException {
        ArrayList<String> viewGroups = new ArrayList<>(Arrays.asList("android.support.v7.widget.LinearLayoutCompat","android.widget.HorizontalScrollView","android.widget.GridView","androidx.drawerlayout.widget.DrawerLayout","android.widget.RelativeLayout","androidx.recyclerview.widget.RecyclerView","com.google.android.material.card.MaterialCardView","android.view.ViewGroup","android.widget.FrameLayout","android.widget.LinearLayout","android.support.v7.widget.RecyclerView"));


        if (node == null || serializer == null) {
            return;
        }


        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        boolean isOutOfScreen = !(bounds.right <= width && bounds.bottom <= height && bounds.left >= 0 && bounds.top >= 0);
        boolean willChildBeClickable = isParentClickable;

        boolean isContainer = node.getClassName() != null && viewGroups.contains( node.getClassName().toString());
        if (node.getChildCount() == 0 && isContainer ) {
            return;
        }

        if (!(isContainer && isPreviousContainer)){
            serializer.startTag(null, "node");
        }



        if (!(isContainer || isOutOfScreen)) {
            serializer.attribute(null, "id", currentIndex  + "");
            currentIndex ++;
            boolean nodeHasContent = false;


            if (node.getClassName() != null && !node.getClassName().toString().equals("")) {
                serializer.attribute(null, "class",node.getClassName().toString().substring(node.getClassName().toString().lastIndexOf(".") + 1));
            }

            if(node.getText() != null && !node.getText().toString().equals("")) {
                if (node.getText().toString().length() > 50){
                    serializer.attribute(null, "text", node.getText().toString().substring(0,49));

                } else {
                    serializer.attribute(null, "text", node.getText().toString());

                }
                nodeHasContent = true;
            }



            if (node.getTooltipText() != null && !node.getTooltipText().toString().equals(""))
                serializer.attribute(null, "tooltip-desc", node.getTooltipText().toString());

            if (node.getContentDescription() != null && !node.getContentDescription().toString().equals("")){
                serializer.attribute(null, "content-desc", node.getContentDescription().toString());
                nodeHasContent = true;
            }
            if(node.getViewIdResourceName() != null && !node.getViewIdResourceName().equals("") && !nodeHasContent) {
                String[] content = node.getViewIdResourceName().split("id/");
                serializer.attribute(null, "resource-name", content[1]);
                nodeHasContent = true;
            }


            if (node.isClickable() || node.isCheckable() || isParentClickable) {

                serializer.attribute(null, "clickable", String.valueOf(true));
                willChildBeClickable = true;
            }



            if(node.isScrollable())
            serializer.attribute(null, "scrollable", String.valueOf(true));

            if(node.isEditable()) {
                serializer.attribute(null, "editable", Boolean.toString(node.isEditable()));
            }




            if((node.isClickable() || node.isCheckable() || node.isEditable() || nodeHasContent) && (!node.getClassName().equals("TextView") && !node.getClassName().equals("View")))  {

                serializer.attribute("", "bounds", bounds.toShortString());
            }



        }


//        Rect rect = new Rect();
//        node.getBoundsInScreen(rect);
//        serializer.attribute(null, "bounds", rect.toShortString());

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                dumpNodeRec(child, serializer, i, isContainer, width, height, willChildBeClickable);
                child.recycle();
            }
        }
        if (!(isContainer && isPreviousContainer)){
            serializer.endTag(null, "node");
        }

    }


}