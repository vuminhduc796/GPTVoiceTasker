package com.research.activityinvoker.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class PackageDataObject {
    public String name;
    public String xmlContent;
    public ArrayList<String> deepLinks;
    public HashMap<String,ArrayList<String>> intentsByActivity;
    public String packageName;
    public ArrayList<String> referencedName;

    public PackageDataObject(String name, String xmlContent, ArrayList<String> deepLinks, HashMap<String, ArrayList<String>> intentsByActivity, String packageName) {
        this.name = name;
        this.xmlContent = xmlContent;
        this.deepLinks = deepLinks;
        this.intentsByActivity = intentsByActivity;
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public String getGPT3Context() {
        return  deepLinks + ", " + getAllIntent();

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getXmlContent() {
        return xmlContent;
    }

    public void setXmlContent(String xmlContent) {
        this.xmlContent = xmlContent;
    }

    public ArrayList<String> getDeepLinks() {
        return deepLinks;
    }

    public void setDeepLinks(ArrayList<String> deepLinks) {
        this.deepLinks = deepLinks;
    }

    public ArrayList<String> getAllActivity(){

        return new ArrayList<String>(intentsByActivity.keySet());
    }

    public ArrayList<String> getAllIntent() {

        ArrayList<String> allActivityIntents = new ArrayList<String>();
        ArrayList<String> activityKeys = getAllActivity();
        for(String activity : activityKeys){
            ArrayList<String> allIntents = intentsByActivity.get(activity);
            assert allIntents != null;
            for(String intent : allIntents){

                allActivityIntents.add(intent +"  "+activity);
            }
        }

        return allActivityIntents;

    }

    public ArrayList<String> getQuerySearch(String keyword){
        ArrayList<String> allActivityIntents = new ArrayList<String>();
        ArrayList<String> activityKeys = getAllActivity();
        for(String activity : activityKeys){
            ArrayList<String> allIntents = intentsByActivity.get(activity);
            assert allIntents != null;
            for(String intent : allIntents){

                if(activity.toLowerCase().contains(keyword.replace(" ","")) || intent.toLowerCase().contains(keyword.replace(" ","")))
                allActivityIntents.add(intent +"  "+activity);
            }
        }

        return allActivityIntents;
    }

    public ArrayList<String> getDeeplinkSearch(String deeplinkKeyword){
        ArrayList<String> deeplinksList = new ArrayList<>();
        for(String deeplink: deepLinks){
            if(deeplink.contains(deeplinkKeyword)){
                deeplinksList.add(deeplink);
            }
        }
        return deeplinksList;
    }
}
