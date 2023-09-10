package com.research.activityinvoker.ViewActivity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.common.reflect.TypeToken;

import com.google.gson.Gson;
import com.research.activityinvoker.ListAdapter.MyPackageAdapter;
import com.research.activityinvoker.R;
import com.research.activityinvoker.model.PackageDataObject;
import com.research.activityinvoker.services.ActionFulfilment;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

//import org.apache.commons.io.IOUtils;
//
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class ReadManifest extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<PackageDataObject> packageDataObjects = new ArrayList<PackageDataObject>();
    MyPackageAdapter adapter;
    EditText appSearch;
    EditText keywordSearch;
    Button searchBtn;
    Button aapiBtn;
    String appSearchString;
    String featureSearchString;

    final String FILE_NAME = "voicify";
    Button resetBtn;
    Button settingBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manifest_main);


// Add a new document with a generated ID

        onNewIntent(getIntent());
        recyclerView = findViewById(R.id.recView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyPackageAdapter(this, packageDataObjects);
        loadData();
        recyclerView.setAdapter(adapter);

        resetBtn = findViewById(R.id.reset_button);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    fetchActivities();
                    storeIntoSharedPrefs();
                    adapter.setData(packageDataObjects);
                    printOutAppComponent();
                    adapter.notifyDataSetChanged();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

        });
        settingBtn = findViewById(R.id.settingBtn);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
            }
        });
        searchFeatures();
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ReadManifest.this);
        builder1.setTitle("Accessibility API Usage Prominent Disclosure");
        builder1.setMessage("In order to fully support all the declared features, our app uses accessibility API to: \n\n- Read element on the screen to know their label or content. \n- Inflating tooltips and control switch on the screen layout. \n- Performing actions such as clicking, scrolling, entering texts on user requests.\n\n By using the app, you have agreed to our Accessibility API usages.");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Accept",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "Reject and Exit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        System.exit(0);
                    }
                });

        AlertDialog alert11 = builder1.create();
        aapiBtn = findViewById(R.id.aapi_btn);
        aapiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert11.show();
            }
        });


        alert11.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
    // messages: ["Spam settings", "settings", "home", "smart settings", "conversation list", "rich cards settings"]
    void printOutAppComponent(){
        ArrayList<String> commonWords = new ArrayList<>(Arrays.asList("http", "deeplink", "com", "android","intent", "action", "google"));
        for( PackageDataObject packageDataObject: packageDataObjects){
            List<String> appWords = Arrays.asList(packageDataObject.packageName.split("\\."));
            ArrayList<String> components = new ArrayList<>();
            for(String deeplink: packageDataObject.deepLinks){
                String[] words = deeplink.split("[^\\w']+");
                String prefix = words[words.length -1];
                if(!appWords.contains(prefix) && !commonWords.contains(prefix)){
                    components.add(prefix);
                }
            }
            for(String intent: packageDataObject.getQuerySearch("")) {
                String[] words = intent.split("[^\\w']+");
                ArrayList<String> keywords = new ArrayList<>();
                for(String word: words){
                    if(!appWords.contains(word) && !commonWords.contains(word)){
                        if (word.endsWith("Activity")){
                            //Log.d("nani", word);
                            keywords.add(word.replace("Activity",""));
                        } else if (word.endsWith("Launcher")){
                            //Log.d("nani", word);
                            keywords.add(word.replace("Launcher",""));
                        } else if (word.contains("_")){
                            //Log.d("nani", word);
                            word.replace("_"," ");
                        } else {
                            keywords.add(word);
                        }

                    }
                }
                ArrayList<String> finalSet = new ArrayList<>();
                for (String keyword: keywords){
                    String[] singleWords = keyword.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
                    String element = String.join(" ",singleWords);
                    finalSet.add(element);
                }
                components.add(String.join(" ",finalSet));
            }
            String referencedName = String.join(" ",components).trim();
            Log.d("Test",packageDataObject.name +": " + components);
        }
    }

    void loadData(){

        SharedPreferences  mPrefs =getSharedPreferences(FILE_NAME,0);
        Gson gson = new Gson();
        String json = mPrefs.getString("packageDataObjects", "");
        Type type = new TypeToken<ArrayList<PackageDataObject>>(){}.getType();
        ArrayList<PackageDataObject> fetchedData = gson.fromJson(json, type);

        if(fetchedData != null && fetchedData.size() >0){
            packageDataObjects = fetchedData;
            adapter.setData(packageDataObjects);
            adapter.notifyDataSetChanged();

        }
      }
    void storeIntoSharedPrefs(){
        SharedPreferences  mPrefs = getSharedPreferences(FILE_NAME,0);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(packageDataObjects);
        prefsEditor.putString("packageDataObjects", json);
        prefsEditor.commit();
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String appName = intent.getStringExtra("app_name");
        String componentName = intent.getStringExtra("component_name");

        Log.d("11",appName + "//" + componentName);
        if(appName != null && componentName != null) {
            Intent serviceIntent = new Intent(getApplicationContext(), ActionFulfilment.class);
            serviceIntent.putExtra("app_name", appName.replaceAll("[!-]*",""));
            serviceIntent.putExtra("component_name", componentName.replaceAll("[!-]*",""));
            startService(serviceIntent);
        }

    }

    void searchFeatures() {

        appSearch = findViewById(R.id.searchApp);
        keywordSearch = findViewById(R.id.searchKeyword);
        searchBtn = findViewById(R.id.searchButton);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appSearchString = appSearch.getText().toString().trim();
                featureSearchString = keywordSearch.getText().toString().trim();
               // invokeComponent(appSearchString,featureSearchString);
                Intent i = new Intent(getApplicationContext(), ActionChooserActivity.class);
                ArrayList<String> matchedIntents = new ArrayList<>();
                String packageName = "";
                String manifest = "";
                ArrayList<String> matchedDeeplinks = new ArrayList<>();
                for(PackageDataObject packageDataObject: packageDataObjects){
                    if(packageDataObject.name.replace("!","").equals(appSearchString)){
                        matchedDeeplinks = packageDataObject.getDeeplinkSearch(featureSearchString);
                        matchedIntents = packageDataObject.getQuerySearch(featureSearchString);
                        packageName = packageDataObject.packageName;
                        manifest = packageDataObject.xmlContent;
                        break;
                    }
                }
                String[] dataArrDeeplink = new String[matchedDeeplinks.size()];
                dataArrDeeplink = matchedDeeplinks.toArray(dataArrDeeplink);
                String[] dataArrIntent = new String[matchedIntents.size()];
                dataArrIntent = matchedIntents.toArray(dataArrIntent);
                Log.d("testt",matchedDeeplinks +"");
                i.putExtra("intents", dataArrIntent);
                i.putExtra("deeplinks", dataArrDeeplink);
                i.putExtra("packageName",packageName );
                i.putExtra("manifest",manifest);
                startActivity(i);

            }
        });
    }

    public void invokeComponent(String appSearchString, String featureSearchString) {
        ArrayList<String> matchedIntents = new ArrayList<>();
        String packageName = "";
        ArrayList<String> matchedDeeplinks = new ArrayList<>();
        boolean isAppFound = false;
        for(PackageDataObject packageDataObject: packageDataObjects){
            if(packageDataObject.name.equals(appSearchString)){
                matchedDeeplinks = packageDataObject.getDeeplinkSearch(featureSearchString);
                matchedIntents = packageDataObject.getQuerySearch(featureSearchString);
                packageName = packageDataObject.packageName;
                isAppFound = true;
                break;
            }
        }
        if(!isAppFound){
//                Snackbar snackbar = Snackbar
//                        .make(recyclerView, "App not found", Snackbar.LENGTH_LONG);
//                snackbar.show();
//                return;
        }
        String[] dataArrDeeplink = new String[matchedDeeplinks.size()];
        dataArrDeeplink = matchedDeeplinks.toArray(dataArrDeeplink);
        String[] dataArrIntent = new String[matchedIntents.size()];
        dataArrIntent = matchedIntents.toArray(dataArrIntent);
//        if(dataArrDeeplink.length != 0){
//            Intent intent = new Intent (Intent.ACTION_VIEW);
//            intent.setData (Uri.parse( dataArrDeeplink[0]));
//            try {
//                startActivity(intent);
//            } catch (ActivityNotFoundException e) {
//                // Define what your app should do if no activity can handle the intent.
//
//                Log.d("Invocation Errors", e.getMessage());
//            }
//
//        } else
            if (dataArrIntent.length != 0) {
                int currentIndex = 0;
                while(currentIndex < dataArrIntent.length) {
                    String intentName = dataArrIntent[0];
                    String[] intents = intentName.split("  ");

                    Intent intent = new Intent (intents[0]);
                    intent.setComponent(new ComponentName(packageName,intents[1]));

                    if(intentName.equals("com.google.android.gms.actions.SEARCH_ACTION") ){
                        intent.putExtra("query","testing string");
                    }
                    try {
                        PackageManager packageManager = getPackageManager();
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent);

                            Log.d("success", intentName+ " / " + packageName +" / " + intents[1]);
                            break;
                        } else {
                            Log.d("Duc", "No Intent available to handle action");
                            currentIndex +=1;
                        }


                    } catch (Exception e) {
                        currentIndex +=1;
                        // Define what your app should do if no activity can handle the intent
                        Log.d("Duc", e.getMessage());
                    }
                }

        } else {
                Intent intent = new Intent (Intent.ACTION_VIEW);
                intent.setPackage(packageName);
                startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchActivities() throws PackageManager.NameNotFoundException {
        /**
         * This function is used to check if the given string matches with any applications that the
         * user may have installed. It launches apps that have matched. Current matching algorithm is
         * trivial. (WIP: Improved Matching Algorithm)
         *
         * @param: inputName — This is a String that is supposed to be checked for app name matching
         * @return: None
         * @post-cond: Apps that match with the given string are launched and presented on the
         *             foreground adding them to the system backstack if multiple apps are launched.
         * */
        packageDataObjects = new ArrayList<>();
        final PackageManager pm = getPackageManager();// getting meta data of all installed apps
           List<ApplicationInfo> apps = pm.getInstalledApplications(
                PackageManager.GET_META_DATA | PackageManager.GET_SHARED_LIBRARY_FILES
        );
//           Log.d("NumberOfApps",apps.size() + " in total" ); // 177
        for (ApplicationInfo applicationInfo : apps) {
            ApplicationInfo info = pm.getApplicationInfo(applicationInfo.packageName, PackageManager.GET_META_DATA);
            String appName = (String) pm.getApplicationLabel(info).toString().toLowerCase();



            //   Log.d("apps", appName);
//            if(appName.contains("map")){
            PackageDataObject packageDataObject = new PackageDataObject(appName,"", new ArrayList<>(),new HashMap<>(),applicationInfo.packageName);
      //      String loc = applicationInfo.publicSourceDir;
            String loc = applicationInfo.sourceDir;
            try {
                ZipFile apk = new ZipFile(loc);
                ZipEntry manifest = apk.getEntry("AndroidManifest.xml");
//                if(appName.contains("youtube")){
//                    Enumeration<? extends ZipEntry> entries = apk.entries();
//
//                    while (entries.hasMoreElements())
//                        Log.d("testt","" + entries.nextElement());
//                }

                if (manifest != null){
                    InputStream stream = apk.getInputStream(manifest);
                    byte[] xml = new byte[stream.available()];
                    int br = stream.read(xml);
                    //Tree tr = TrunkFactory.newTree();
                    Log.d("duc",applicationInfo.name + "/" + applicationInfo.packageName) ;
                    packageDataObject.xmlContent = decompressXML(xml,applicationInfo.name );

                    stream.close();
                }
                apk.close();
                packageDataObject.deepLinks = deepLinkExtractor(packageDataObject.xmlContent);
                packageDataObject.intentsByActivity = intentExtractor(packageDataObject);
                if(packageDataObject.deepLinks.size() > 0 || packageDataObject.intentsByActivity.size() > 0 ){
                    packageDataObjects.add(packageDataObject);
                }


            } catch (IOException e) {
                e.printStackTrace();
        //    }
           }
        }
    }



    public static int endDocTag = 0x00100101;
    public static int startTag =  0x00100102;
    public static int endTag =    0x00100103;
    public String decompressXML(byte[] xml, String filename) {

        String xmlString = "";
        StringBuilder stringBuilder = new StringBuilder();
        int numbStrings = LEW(xml, 4*4);


        int sitOff = 0x24;  // Offset of start of StringIndexTable

        int stOff = sitOff + numbStrings*4;  // StringTable follows StrIndexTable

        int xmlTagOff = LEW(xml, 3*4);  // Start from the offset in the 3rd word.

        for (int ii=xmlTagOff; ii<xml.length-4; ii+=4) {
            if (LEW(xml, ii) == startTag) {
                xmlTagOff = ii;  break;
            }
        } // end of hack, scanning for start of first start tag

// Step through the XML tree element tags and attributes
        int off = xmlTagOff;
        int indent = 0;
        int startTagLineNo = -2;
        while (off < xml.length) {
            int tag0 = LEW(xml, off);
            //int tag1 = LEW(xml, off+1*4);
            int lineNo = LEW(xml, off+2*4);
            //int tag3 = LEW(xml, off+3*4);
            int nameNsSi = LEW(xml, off+4*4);
            int nameSi = LEW(xml, off+5*4);

            if (tag0 == startTag) { // XML START TAG
                int tag6 = LEW(xml, off+6*4);  // Expected to be 14001400
                int numbAttrs = LEW(xml, off+7*4);  // Number of Attributes to follow
                //int tag8 = LEW(xml, off+8*4);  // Expected to be 00000000
                off += 9*4;  // Skip over 6+3 words of startTag data
                String name = compXmlString(xml, sitOff, stOff, nameSi);
                //tr.addSelect(name, null);
                startTagLineNo = lineNo;

                // Look for the Attributes
                StringBuffer sb = new StringBuffer();
                for (int ii=0; ii<numbAttrs; ii++) {
                    int attrNameNsSi = LEW(xml, off);  // AttrName Namespace Str Ind, or FFFFFFFF
                    int attrNameSi = LEW(xml, off+1*4);  // AttrName String Index
                    int attrValueSi = LEW(xml, off+2*4); // AttrValue Str Ind, or FFFFFFFF
                    int attrFlags = LEW(xml, off+3*4);
                    int attrResId = LEW(xml, off+4*4);  // AttrValue ResourceId or dup AttrValue StrInd
                    off += 5*4;  // Skip over the 5 words of an attribute

                    String attrName = compXmlString(xml, sitOff, stOff, attrNameSi);
                    String attrValue = attrValueSi!=-1
                            ? compXmlString(xml, sitOff, stOff, attrValueSi)
                            : "resourceID 0x"+Integer.toHexString(attrResId);
                    sb.append(" "+attrName+"=\""+attrValue+"\"");
                    //tr.add(attrName, attrValue);
                }
                prtIndent(indent, "<"+name+sb+">",filename);
                stringBuilder.append(spaces.substring(0, Math.min(indent*2, spaces.length()))+"<"+name+sb+">"+ "\n");
                indent++;

            } else if (tag0 == endTag) { // XML END TAG
                indent--;
                off += 6*4;  // Skip over 6 words of endTag data
                String name = compXmlString(xml, sitOff, stOff, nameSi);
                prtIndent(indent, "</"+name+"> ",filename);
                stringBuilder.append(spaces.substring(0, Math.min(indent*2, spaces.length()))+"</"+name+">  "+ "\n");
                //tr.parent();  // Step back up the NobTree

            } else if (tag0 == endDocTag) {  // END OF XML DOC TAG
                break;

            } else {
          //      Log.d("Issue","  Unrecognized tag code '"+Integer.toHexString(tag0)
           //             +"' at offset "+off);
                break;
            }
        } // end of while loop scanning tags and attributes of XML tree
       // Log.d("Issue","    end at offset "+off);
        xmlString = stringBuilder.toString();
//        String extractedName =  filename.substring(filename.lastIndexOf('.') + 1).trim();
//        Map<String, Object> dataObject = new HashMap<>();
//        dataObject.put("manifest", xmlString);
//        dataObject.put("package", extractedName);

//        db.collection("data")
//                .document(extractedName)
//                .set(dataObject);
        return xmlString;
    } // end of decompressXML


    public String compXmlString(byte[] xml, int sitOff, int stOff, int strInd) {
        if (strInd < 0) return null;
        int strOff = stOff + LEW(xml, sitOff+strInd*4);
        return compXmlStringAt(xml, strOff);
    }


    public static String spaces = "                                             ";

    public void prtIndent(int indent, String str, String filename) {

        // writeToFile(spaces.substring(0, Math.min(indent*2, spaces.length()))+str,filename);
       // Log.d("Data",spaces.substring(0, Math.min(indent*2, spaces.length()))+str);
        
    }


    // compXmlStringAt -- Return the string stored in StringTable format at
// offset strOff.  This offset points to the 16 bit string length, which
// is followed by that number of 16 bit (Unicode) chars.
    public String compXmlStringAt(byte[] arr, int strOff) {
        int strLen = arr[strOff+1]<<8&0xff00 | arr[strOff]&0xff;
        byte[] chars = new byte[strLen];
        for (int ii=0; ii<strLen; ii++) {
            try {
                chars[ii] = arr[strOff + 2 + ii * 2];
            } catch (Exception e) {
                Log.e("err", "Parsing error");
            }
        }
        return new String(chars);  // Hack, just use 8 byte chars
    } // end of compXmlStringAt


    // LEW -- Return value of a Little Endian 32 bit word from the byte array
//   at offset off.
    public int LEW(byte[] arr, int off) {
        return arr[off+3]<<24&0xff000000 | arr[off+2]<<16&0xff0000
                | arr[off+1]<<8&0xff00 | arr[off]&0xFF;
    } // end of LEW


    private ArrayList<String> deepLinkExtractor(String rawXMLData){
        ArrayList<String> output = new ArrayList<>();
        int totalCounter = 0;
        Pattern intentPattern = Pattern.compile("<intent-filter(.*?)</intent-filter>", Pattern.DOTALL);
        Matcher intentMatcher = intentPattern.matcher(rawXMLData);
        while (intentMatcher.find()) {
            String match = intentMatcher.group(1);
            if(match != null && match.contains("DEFAULT") && match.contains("BROWSABLE")){

                ArrayList<String> dataScheme = new ArrayList<>();
                ArrayList<String> dataHost = new ArrayList<>();
                ArrayList<String> dataPathPrefix = new ArrayList<>();
                Pattern dataPattern = Pattern.compile("<data (.*?)</data>| <data (.*?)/>", Pattern.DOTALL);
                Matcher dataMatcher = dataPattern.matcher(match);
                while (dataMatcher.find() && totalCounter < 60) {
                    String matchData = dataMatcher.group(1);
                    if(matchData != null){
                        Pattern patternScheme = Pattern.compile("scheme=\"(.*?)\"", Pattern.DOTALL);
                        Matcher matcherScheme = patternScheme.matcher(matchData);
                        int counterScheme = 0;
                        while (matcherScheme.find() && counterScheme < 2) {
                            String matchScheme = matcherScheme.group(1);
                            if(matchScheme != null && !matchScheme.contains("resource")){
                            dataScheme.add(matchScheme);
                    //        Log.d("scheme", matchScheme);
                            counterScheme += 1;
                        }}
                        if(dataScheme.size() == 0) {
                            continue;
                        }

                        Pattern patternHost = Pattern.compile("host=\"(.*?)\"", Pattern.DOTALL);
                        Matcher matcherHost= patternHost.matcher(matchData);
                        int counterHost = 0;
                        while (matcherHost.find() && counterHost < 10) {
                            String matchHost = matcherHost.group(1);
                            if(matchHost != null && !matchHost.contains("resource")){
                            dataHost.add(matchHost);
                     //       Log.d("host", matchHost);
                            counterHost += 1;
                        }}

                        Pattern patternPathPrefix = Pattern.compile("pathPrefix=\"(.*?)\"", Pattern.DOTALL);
                        Matcher matcherPathPrefix= patternPathPrefix.matcher(matchData);
                        int counterPathPrefix = 0;
                        while (matcherPathPrefix.find() && counterPathPrefix < 10) {
                            String matchPathPrefix = matcherPathPrefix.group(1);
                            if(matchPathPrefix != null && !matchPathPrefix.contains("resource")){
                                dataPathPrefix.add(matchPathPrefix);
                       //         Log.d("prefix", matchPathPrefix);
                                counterPathPrefix += 1;
                            }}
                        if (dataHost.size() == 0) dataHost.add("");
                        if (dataPathPrefix.size() == 0) dataPathPrefix.add("");
                        if (dataScheme.contains("https") && dataScheme.contains("http")){
                            dataScheme.remove("http");
                        }
                        for(String scheme : dataScheme){
                            for(String host : dataHost) {
                                for(String pathPrefix : dataPathPrefix){
                                    if (scheme.equals("") && host.equals("")){

                                    }else {
                                        String url = scheme+ "://" + host+ pathPrefix;
                                       // if (output.contains(url)) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse(url));
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            ActivityInfo info = intent.resolveActivityInfo(getApplicationContext().getPackageManager(),
                                                    PackageManager.MATCH_DEFAULT_ONLY);

                                            if (info != null && info.exported) {
                                                //Log.d("duc", url);
                                                output.add(url);
                                                totalCounter += 1;
                                            }
                                        }
                             //       }
                                }


                            }
                        }

                        //totalCounter += 1;
                    }
                }

//            for(String scheme : dataScheme){
//                for(String host : dataHost){
//                    String url = host + "://"+  scheme;
//                    output.add(url);
//                }
//            }

            }


        }
        Set<String> set = new HashSet<>(output);
        output.clear();
        output.addAll(set);
        return output;

    }


    private HashMap<String,ArrayList<String>>  intentExtractor(PackageDataObject packageDataObject){
        if(packageDataObject.name.equals("gmail")){
          //  Log.d("app",rawXMLData);
        }
        HashMap<String,ArrayList<String>> activityOutput = new HashMap<>();
        Pattern activityPattern = Pattern.compile("<activity(.*?)</activity", Pattern.DOTALL);
        Matcher activityMatcher = activityPattern.matcher(packageDataObject.xmlContent);
        while (activityMatcher.find()) {            // for each activity
            String activityName  = "";
            String activityMatch = activityMatcher.group(1);
//            Log.d("te11", " @@@ "+ activityMatch);

          // if (activityMatch != null  && activityMatch.contains("<intent-filter") && !activityMatch.contains("exported=\"resourceID 0x0\"")) {
                if(activityMatch != null  && !activityMatch.contains("exported=\"resourceID 0x0\"")){
                Pattern activityPropsPattern = Pattern.compile(" (.*?)>", Pattern.DOTALL);
                Matcher activityPropsMatcher = activityPropsPattern.matcher(activityMatch);
                while( activityPropsMatcher.find()){        // for
                    String activityPropsMatch = activityPropsMatcher.group(1);
                    if (activityPropsMatch != null && activityName.equals("")) {
                      //  Log.d("te11",activityPropsMatch + " @@@ "+ activityName);
                        Pattern namePattern = Pattern.compile("name=\"(.*?)\"", Pattern.DOTALL);
                        Matcher nameMatcher = namePattern.matcher(activityPropsMatch);
                        while (nameMatcher.find()) {
                            String match = nameMatcher.group(1);
                            if (match != null) {
                                activityName = match;
                        //        Log.d("testt",match);
                                break;
                            }
                        }

                    }
                }


                ArrayList<String> appWords = new ArrayList<>();
                appWords.addAll(Arrays.asList(packageDataObject.name.split(" ")));
                appWords.addAll(Arrays.asList(packageDataObject.packageName.split("\\.")));
                appWords.add("google");
                appWords.add("android");
                appWords.remove("example");
                appWords.remove("com");
                boolean isInternalActivity = false;
                for(String appWord: appWords){
                    if(activityName.contains(appWord)) {
                        isInternalActivity = true;
                        break;
                    } else {
                      //  Log.d("checks", activityName + " @@@ " + appWord);
                    }
                }
                if(!isInternalActivity) {
                 //   Log.d("checks",activityName + " / " + packageDataObject.packageName );
                    continue;
                }

                ArrayList<String> output = new ArrayList<>();
                Pattern intentPattern = Pattern.compile("<intent-filter(.*?)</intent-filter>", Pattern.DOTALL);
                Matcher intentMatcher = intentPattern.matcher(activityMatch);
                int count = 0;
                while (intentMatcher.find()) {
                    String match = intentMatcher.group(1);
                    if (match != null && match.contains("DEFAULT")) {
                        ArrayList<String> actionList = new ArrayList<>();
                        Pattern actionPattern = Pattern.compile("<action name=\"(.*?)\"", Pattern.DOTALL);
                        Matcher actionMatcher = actionPattern.matcher(match);

                        while (actionMatcher.find()) {
                            String matchAction = actionMatcher.group(1);
                            if (matchAction != null) {
                                if(!output.contains(matchAction)){
                                    Intent intent = new Intent (matchAction);
                                    intent.setComponent(new ComponentName(packageDataObject.packageName,activityName));
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    ActivityInfo info = intent.resolveActivityInfo(getApplicationContext().getPackageManager(),
                                            PackageManager.MATCH_DEFAULT_ONLY);
                                    if ( info != null && info.exported){
                                        output.add(matchAction);
                                    }

                                }

                                //Log.d("testt", matchAction);
                                count +=1;

                            }
                        }

                    }
                }
               // Log.d("totall",count + "");
                Set<String> set = new HashSet<>(output);
               Intent defaultIntent = new Intent ("android.intent.action.VIEW");
               defaultIntent.setComponent(new ComponentName(packageDataObject.packageName,activityName));
               defaultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               ActivityInfo defaultInfo = defaultIntent.resolveActivityInfo(getApplicationContext().getPackageManager(),
                       PackageManager.MATCH_DEFAULT_ONLY);
               if ( defaultInfo != null && defaultInfo.exported && set.size() == 0){
                   set.add("android.intent.action.VIEW");
               }
                output.clear();
                output.addAll(set);
                activityOutput.put(activityName,output);
            }
        }

        return activityOutput;

    }
}