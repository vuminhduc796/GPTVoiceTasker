package com.research.activityinvoker.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.research.activityinvoker.AccessibilityNodeInfoDumper;
import com.research.activityinvoker.Command;
import com.research.activityinvoker.JsonApi;
import com.research.activityinvoker.R;
import com.research.activityinvoker.graphdatabase.Edge;
import com.research.activityinvoker.graphdatabase.Graph;
import com.research.activityinvoker.graphdatabase.Node;
import com.research.activityinvoker.graphdatabase.Utils;
import com.research.activityinvoker.model.ChatHistory;
import com.research.activityinvoker.model.LabelFoundNode;
import com.research.activityinvoker.model.PackageDataObject;
import com.research.activityinvoker.model.RequestBody;
import com.research.activityinvoker.model.ResponseObject;
import com.research.activityinvoker.model.TooltipRequiredNode;

import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@RequiresApi(api = Build.VERSION_CODES.R)
public class ActionFulfilment extends AccessibilityService implements View.OnTouchListener {
    final String FILE_NAME = "voicify";
    int width, height;
    Button listenButton;
    Button resetBtn;

    Retrofit retrofit;
    String currentActivityName;
    JsonApi jsonApi;
    ChatHistory chatHistory;
    ArrayList<String> uiElements = new ArrayList<String>();
    ArrayList<String> appNames = new ArrayList<String>();
    SharedPreferences mPrefs;

    AccessibilityNodeInfo currentSource;
    AccessibilityNodeInfo previousSource;
    ArrayList<AccessibilityNodeInfo> scrollableNodes = new ArrayList<AccessibilityNodeInfo>();
    String componentAsString = "";
    FrameLayout mLayout;
    ArrayList<LabelFoundNode> foundLabeledNodes = new ArrayList<>();
    ArrayList<TooltipRequiredNode> tooltipRequiredNodes = new ArrayList<>();

    boolean isVoiceCommandConnected = false;
    String currentCommand = "";
    int noOfLabels = 0;
    SpeechRecognizer speechRecognizer;                      // declaring speech recognition var
    Intent speechRecognizerIntent;
    String debugLogTag = "FIT4003_VOICIFY";                  // use this tag for all log tags.
    ArrayList<String> launchTriggers = new ArrayList<String>(Arrays.asList("load", "launch", "execute", "open"));

    // Defining window manager for overlay elements and switchBar
    WindowManager wm;
    WindowManager.LayoutParams switchBar; // stores layout parameters for movable switchBar

    long currentTime;

    // variable for switch bar coordinates
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;

    private int currentTooltipCount = 1;

    // new code
    ArrayList<PackageDataObject> packageDataObjects = new ArrayList<>();
    int currentIntentIndex = 0;
    String currentOpeningPackage;
    String currentOpeningFeature;
    String currentAppName;

    ArrayList<String> matchedIntents = new ArrayList<>();

    String[] tooltipColorSpinnerItems = new String[]{"#64b5f6", "#2b2b2b", "#ff4040"};
    int[] tooltipSizeSpinnerItems = new int[]{14, 18, 22};
    int[] tooltipOpacitySpinnerItems = new int[]{250, 220, 170, 120};

    int tooltipColor = 0;
    int tooltipSize = 0;
    int tooltipOpacity = 0;

    String previousEventCode = "";

    String currentScreenXML = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        /**
         * This function will be invoked when defined type of event occurs
         * param: event is an instance that capture every information about the event
         */

        // basic checks for null safety

        setCurrentActName(event);
        AccessibilityNodeInfo source = event.getSource();

        if ((source == null)

            //  || (previousEventCode.equals("32") && previousSource!= null && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
        ) {

            return;
        }
//        if ((previousSource!= null && (previousSource.hashCode() == source.hashCode() || previousSource.equals(source))) && !currentCommand.equals("")) {
//            Log.d("GG", "confirmed");
//            guidedTaskExecutor();
//        }

        previousSource = source;
        previousEventCode = event.getEventType() + "";

        if (isNotBlockedEvent()) {
            currentSource = getRootInActiveWindow(); // update the current root node
            updateToolTip();

        }
    }


    public void updateToolTip() {
        uiElements.clear();
        noOfLabels = 0;
        removeAllTooltips();    // remove all old  tooltip when screen changed
        if (isVoiceCommandConnected && currentSource != null) {
            printOutAllClickableElement(getRootInActiveWindow(), 0); // call function for root node
            updateScreenXML();
            uiElements.remove("blocked numbers storage");
        }
    }

    private void updateScreenXML() {
        XmlSerializer serializer = Xml.newSerializer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            serializer.setOutput(outputStream, "UTF-8");
            serializer.startDocument(null, true);
            AccessibilityNodeInfoDumper.dumpNodeRec(getRootInActiveWindow(), serializer, 0, false, width, height, false);
            serializer.endDocument();
            currentScreenXML = outputStream.toString("UTF-8");

            //Log.d("CURRENT SCREEN XML", currentScreenXML);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    final Handler autoReloadHandler = new Handler();
    final Runnable runnable = new Runnable() {
        public void run() {
            autoReload();
            autoReloadHandler.postDelayed(this, 3000);

        }
    };

    public boolean isNotBlockedEvent() {
        Date date = new Date();
        long time = date.getTime();
        if (time - currentTime > 1000) {
            currentTime = time;
            return true;
        }

        return false;
    }

    public void autoReload() {
        if (currentSource != null) {
            currentSource.refresh();

            if (isNotBlockedEvent())
                updateToolTip();
        }


    }

    public void printOutAllClickableElement(AccessibilityNodeInfo nodeInfo, int depth) {
        /**
         * This function will print out all clickable -element, storing the data it has or number for those
         * clickable elements.
         *
         */

        if (nodeInfo == null) {
            return;
        }
        if (nodeInfo.isClickable()) {
            String label = "";
            Rect rectTest = new Rect();                     //  to get the coordinate of the UI element
            nodeInfo.getBoundsInScreen(rectTest);           //  store data of the node
            if (rectTest.right - 100 < width && rectTest.bottom - 100 < height && rectTest.left + 100 > 0 && rectTest.top + 100 > 0) {
                if (nodeInfo.getText() != null) {   // check if node has a corresponding text
                    label += nodeInfo.getText();
                    String[] wordInLabel = label.split(" ");
                    String filteredLabel = "";

                    for (String word : wordInLabel) {
                        if (filteredLabel.contains(word))
                            filteredLabel += word + " ";
                    }

                    uiElements.add(label.toLowerCase());
                    return;
                } else {
                    // no information about node or event (Tags to be assigned!)
                    String foundLabel = searchForTextView(nodeInfo, "");
                    String[] texts = foundLabel.split(" ");
                    int end = texts.length;
                    Set<String> cleanTexts = new HashSet<String>();

                    for (int i = 0; i < end; i++) {
                        cleanTexts.add(texts[i]);
                    }
                    String finalText = "";
                    for (String cleanText : cleanTexts) {
                        finalText += cleanText + " ";
                    }

                    if (!foundLabel.equals("") && noOfLabels < 15 && cleanTexts.size() < 10) {
                        foundLabeledNodes.add(new LabelFoundNode(nodeInfo, foundLabel.toLowerCase()));
                        uiElements.add(foundLabel.toLowerCase());

                        noOfLabels += 1;
                    } else if (currentTooltipCount < 20) {
                        inflateTooltip((rectTest.right + rectTest.left + 15) / 2, (rectTest.bottom + rectTest.top - 70) / 2, nodeInfo);    // call function to create number tooltips
                    }
                }
            }

            //clickableNodes.add(new Pair<>(label,nodeInfo));
            //Log.d(debugLogTag,"Available commands: " + label);
        }
        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
            printOutAllClickableElement(nodeInfo.getChild(i), depth + 1);    // recursive call
        }
    }


    private void inflateTooltip(int x, int y, AccessibilityNodeInfo nodeInfo) {
        /**
         * This function will configure each of the tooltip on the screen, so this function will be
         * called for each of the tooltip on the screen.
         * param: x is the location in x axis
         * param: y is the location in y axis
         */
        FrameLayout tooltipLayout = new FrameLayout(this);      // create new layout for each tooltip
        WindowManager.LayoutParams tooltipLayoutParams = new WindowManager.LayoutParams();
        tooltipLayoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        tooltipLayoutParams.format = PixelFormat.TRANSLUCENT;
        tooltipLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        tooltipLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        tooltipLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        tooltipLayoutParams.gravity = Gravity.TOP | Gravity.START;     // reset the (0,0) to the top left screen
        tooltipLayoutParams.x = x + 15;       // x location
        tooltipLayoutParams.y = y + 40;       // y location
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.tooltip_number, tooltipLayout);   // inflate the view to the screen
        wm.addView(tooltipLayout, tooltipLayoutParams);

        TextView tooltip = tooltipLayout.findViewById(R.id.tooltip);    // set the count based on current count
        tooltip.setText(currentTooltipCount + "");
        tooltip.setTextSize(tooltipSizeSpinnerItems[tooltipSize]);
        tooltip.setBackgroundResource(R.drawable.tooltip_shape);  //drawable id
        GradientDrawable gd = (GradientDrawable) tooltip.getBackground().getCurrent();
        gd.setColor(Color.parseColor(tooltipColorSpinnerItems[tooltipColor])); //set color
        gd.setAlpha(tooltipOpacitySpinnerItems[tooltipOpacity]);        // add to the list to retrieve later
        gd.setSize(tooltipSizeSpinnerItems[tooltipSize] + 40, tooltipSizeSpinnerItems[tooltipSize] + 5);
        tooltipRequiredNodes.add(new TooltipRequiredNode(nodeInfo, currentTooltipCount, tooltipLayout));
        //change
        //uiElements.add(Integer.toString(currentTooltipCount));
        currentTooltipCount += 1;

    }


    private void initializeSpeechRecognition() {
        /**
         * This function performs all the steps required for speech recognition initialisation
         */

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // also available: LANGUAGE_MODEL_WEB_SEARCH
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        // setting the limit for the service to listen as an requirement for API30
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 100000);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                //    Log.d("ss", "onReady");
                // textMsg.setText("Ready...");
                // textMsg.setBackgroundResource(R.color.green);
                // Called when the endpointer is ready for the user to start speaking.
            }

            @Override
            public void onBeginningOfSpeech() {
                //    Log.d("ss", "onBeginning");
                // The user has started to speak.
                // textMsg.setText("Listening...");
                // textMsg.setBackgroundResource(R.color.green);
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // The sound level in the audio stream has changed.
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                //Log.d("ss", "buffer");
                // More sound has been received.
            }

            @Override
            public void onEndOfSpeech() {
                // Log.d("ss", "onEndOfSpeech");
                // Called after the user stops speaking
            }

            @Override
            public void onError(int error) {
                //Log.d("ss", "onError: " + error);
                Log.d("hihi", error + " error");
                //Toast.makeText(MainActivity.this, "An error has occurred. Code: " + Integer.toString(error), Toast.LENGTH_SHORT).show();
                if (error == 8 || error == 7) {
                    speechRecognizer.cancel();
                    if (isVoiceCommandConnected) {
                        speechRecognizer.startListening(speechRecognizerIntent);
                    }
                }
            }

            @Override
            public void onResults(Bundle results) {
                // Called when recognition results are ready.

                //   textMsg.setText("Processing.");
                //   textMsg.setBackgroundResource(R.color.yellow);
                if (!isVoiceCommandConnected) {
                    return;
                }
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && matches.size() > 0) {
                    String match = matches.get(0);
                    //                  inputTxt.setText(match);
                    Log.d("USER_COMMAND", match);
                    if (!match.contains("continue")) {
                        currentCommand = match;
                    }

                    if (isRecording) {
                        guidedTaskExecutor();

                    } else {
                        screenPrediction();
                    }

                }
                speechRecognizer.startListening(speechRecognizerIntent);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && matches.size() > 0) {
                    String match = matches.get(0);
//                    inputTxt.setText(match);
                    Log.d("USER_COMMAND", match);
                    if (!match.contains("continue")) {
                        currentCommand = match;
                    }

                    if (isRecording) {
                        guidedTaskExecutor();

                    } else {
                        screenPrediction();
                    }
                    //Log.d("GRAPH",graph.toString());

                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // reserved by android for future events
            }
        });
    }

    boolean isRecording = false;

    private String formatNumberInCommand(String sentence) {
        String[] numbers = {" zero", " one", " two", " three", " four", " five", " six", " seven", " eight", " nine", " ten", " eleven", " twelve", " thirteen", " fourteen", " fifteen", " sixteen", " seventeen", " eighteen", " nineteen"};
//        sentence = sentence.replaceAll(" number","").replaceAll("click to","click two").replaceAll("press to","click two").replaceAll("for", "four").replaceAll("want", "one").replaceAll("sex","six");
//        sentence = sentence.replaceAll("pressed","press").replaceAll("clique","click").replaceAll("quick","click").replaceAll("Preston", "press");
        for (int i = 0; i < numbers.length; i++) {
            if (sentence.contains(numbers[i])) {
                sentence = sentence.replaceAll(numbers[i], " " + String.valueOf(i));
            }
        }
        return sentence;
    }

    private String listToString(ArrayList<String> list) {
        StringBuilder output = new StringBuilder();
        for (String ui : list) {
            output.append(ui);
            output.append(", ");
        }
        String outputAsString = output.toString();
        if (outputAsString.length() > 2) {
            return outputAsString.substring(0, outputAsString.length() - 2);
        } else {
            return outputAsString;
        }
    }

    String getAppComponentAsString() {

        StringBuilder output = new StringBuilder();
        ArrayList<String> systemApp = new ArrayList<>(Arrays.asList("voicify", "meet", "google services framework", "speech services by google", "home", "keep notes", "google vr services", "pixel ambient services", "google connectivity services", "default print service", "live transcribe & sound notifications", "pixel wallpapers 18", "nfc service", "android accessibility suite", "pixel ambient service", "127", "android system intelligence", "select hour", "start date – %1$s", "sim manager", "call management", "markup", "android system webview", "true", "package installer", "work setup", "pixel launcher", "google wallet", "settings services", "bluetooth", "150", "220", "styles & wallpapers", "android auto", "adaptive connectivity services", "device health services", "carrier setup", "live wallpaper picker", "android auto", "clear text", "pixel buds", "google wi-fi provisioner", "system ui", "system tracing", "gboard", "smart storage", "wireless emergency alerts", "playground", "media storage", "false", "device setup", "google tv", "android system", "pixel setup", "nfc", "personal safety", "current selection: %1$s", "extreme battery saver"));
        for (PackageDataObject packageDataObject : packageDataObjects) {
            if (packageDataObject.name.contains(".") || systemApp.contains(packageDataObject.name)) {
                continue;
            }
            ArrayList<String> commonWords = new ArrayList<>(Arrays.asList("app", "apps", "application", "http", "https", "deeplink", "com", "android", "intent", "action", "google", "VIEW", "MAIN"));
            List<String> appWords = Arrays.asList(packageDataObject.packageName.split("\\."));
            ArrayList<String> components = new ArrayList<>();
            for (String deeplink : packageDataObject.deepLinks) {
                String[] words = deeplink.split("[^\\w']+");
                String prefix = words[words.length - 1].toLowerCase(Locale.ROOT);
                if (!appWords.contains(prefix) && !commonWords.contains(prefix) && !components.contains(prefix) && prefix.length() > 1) { //&& dictionary.contains(prefix)
                    components.add(prefix);
                }
            }
            for (String intent : packageDataObject.getQuerySearch("")) {
                String[] words = intent.split("[^\\w']+");
                ArrayList<String> keywords = new ArrayList<>();
                for (String word : words) {
                    if (!appWords.contains(word) && !commonWords.contains(word)) {
                        if (word.endsWith("Activity")) {

                            keywords.add(word.replace("Activity", ""));
                        } else if (word.endsWith("Launcher")) {

                            keywords.add(word.replace("Launcher", ""));
                        } else if (word.contains("_")) {

                            word.replace("_", " ");
                        } else {
                            keywords.add(word);
                        }

                    }
                }
                ArrayList<String> finalSet = new ArrayList<>();
                for (String keyword : keywords) {
                    String[] singleWords = keyword.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
                    String element = String.join(" ", singleWords);
                    element = element.replace("MAIN", "").replace("VIEW", "");

                    if (!finalSet.contains(element.toLowerCase(Locale.ROOT))) { //&& dictionary.contains(element)
                        finalSet.add(element.toLowerCase(Locale.ROOT));
                    }
                }
                String componentName = String.join(" ", finalSet);
                if (!components.contains(componentName.toLowerCase(Locale.ROOT))) {
                    components.add(componentName.toLowerCase(Locale.ROOT));

                }

            }
            String outputStr = packageDataObject.name + " APP has COMPONENT: " + listToString(components) + ".\n";
            output.append(outputStr);
        }

        return output.toString();
    }

    public String getAppNameFromPackageName(String packageName) {
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : "<unk>");

    }

    Graph graph;

    public void setCurrentActName(AccessibilityEvent event) {

        String packageName = String.valueOf(event.getPackageName());
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            ComponentName componentName = intent.getComponent();
            if (!componentName.getClassName().contains("ReadManifest"))

                currentActivityName = componentName.getClassName();
        }

        if (currentSource != null && currentSource.getPackageName() != null && !currentSource.getPackageName().toString().equals(currentPackageName)) {
            Log.d("PACKAGE_NAME_CHANGE", currentPackageName + "->" + currentSource.getPackageName());
            currentPackageName = currentSource.getPackageName().toString();

            graph = new Graph(currentPackageName, getApplicationContext());
        }


    }

    String currentPackageName = "";


    Node currentNodeGraph;
    boolean isExecuting = false;

    public void screenPrediction() {
        String screenDescriptionMatching = Command.requestScreenDescriptionMatching(currentCommand, graph.getListOfDescriptions());
        Call<ResponseObject> call = jsonApi.getData(
                new RequestBody(screenDescriptionMatching)
        );
        call.enqueue(new Callback<ResponseObject>() {
            @Override
            public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                if (!response.isSuccessful()) {
                    assert response.errorBody() != null;
                    return;
                }
                ResponseObject data = response.body();
                if (data != null && data.choices != null && data.choices.size() > 0) {
                    String descriptionMatch = data.choices.get(0).text.trim();
                    Log.d("Description Match", descriptionMatch);

                    if (descriptionMatch.toLowerCase(Locale.ROOT).contains("no match")) {
                        Log.d("PATH", "not found mate");
                        return;
                    }
                    Node currentNode = graph.getNodeByXML(currentScreenXML);
                    Node destinationNode = graph.getNodeByDescription(descriptionMatch);

                    if (currentNode != null && destinationNode != null) {
                        Log.d("PATH", "src: " + currentNode.nodeID + ",dest: " + destinationNode.nodeID);
                        List<Edge> edges = graph.shortestPath(currentNode, destinationNode);
                        loopPressByBounds(edges);
                        for (Edge edge : edges) {
                            Log.d("Path", String.valueOf(edge));
                        }
                    } else {
                        Log.d("PATH", "not found mate");
                        speakerTask("cannot find path");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseObject> call, Throwable t) {
                Log.e("myErrTag", t.getMessage());

            }
        });
    }


    public void guidedTaskExecutor() {

        //TODO: Check for screen changed
        if (currentNodeGraph == null) {
            currentNodeGraph = new Node(currentActivityName, Utils.clickableElementAsString(currentSource), "This is has: " + foundLabeledNodes, currentScreenXML);
        }
        isExecuting = true;
        Log.d("execute", currentCommand);
        //Log.d("PROMPT",prompt);
        requestPressAction();
//        String decideActionPrompt = Command.decideActionPrompt(currentCommand,currentScreenXML,currentSource.getPackageName().toString(), getAppNameFromPackageName(currentSource.getPackageName().toString()), chatHistory.getChatHistory(), currentActivityName);
//        Call<ResponseObject> call = jsonApi.getData(
//                new RequestBody(decideActionPrompt)
//        );
//        call.enqueue(new Callback<ResponseObject>() {
//            @Override
//            public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
//                if (!response.isSuccessful()) {
//                    assert response.errorBody() != null;
//                    return;
//                }
//                ResponseObject data = response.body();
//                if (data != null && data.choices != null && data.choices.size() > 0) {
//                    Log.d("RETURN_ACTION",data.choices.get(0).text.toUpperCase(Locale.ROOT).trim());
//                    String text = "";
//                    try {
//                        text = data.choices.get(0).text.toUpperCase(Locale.ROOT).trim().split("###")[1];
//
//                    } catch (Exception e) {
//                        Log.e("ERROR", "Cannot get the action");
//                    }
//
//                    if (text.equals("")) {
//                        speakerTask("action not found, please try again");
//                        return;
//                    }
//
//                    if (text.contains("ENTER") ||text.contains("PRESS") ||text.contains("SCROLL") || text.contains("OPEN") ) {
//
//                        switch (text) {
//                        case "PRESS":
//
//                            requestPressAction();
//                            break;
//                        case "OPEN":
//
//                            requestAppName();
//                            break;
//
//                        case "ENTER":
//                            requestTextInput();
//                            break;
//
//                        case "SCROLL":
//                            requestScrollInput();
//                            break;
//
//                        default:
//                            break;
//                        }
//
//
//                    } else {
//                        String[] elements = text.split(":");
//                        String action = elements[elements.length -1];
//                        speakerTask(action);
//
//                    }
//
//                }
//            }
//            @Override
//            public void onFailure(Call<ResponseObject> call, Throwable t) {
//                Log.e("myErrTag", t.getMessage());
//
//            }
//        });
        isExecuting = false;
    }

    private void guidedExploreSingle(String prompt) {
        //String prompt = Command.guidedCommand(currentCommand,currentScreenXML,currentSource.getPackageName().toString(), getAppNameFromPackageName(currentSource.getPackageName().toString()), chatHistory.getChatHistory(), currentActivityName);

        Call<ResponseObject> call1 = jsonApi.getData(
                new RequestBody(prompt)
        );
        call1.enqueue(new Callback<ResponseObject>() {
            @Override
            public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                if (!response.isSuccessful()) {
                    assert response.errorBody() != null;
                    Log.d("GPT-3 ERROR", response.toString());


                    return;
                }

                ResponseObject data = response.body();
                if (data != null && data.choices != null && data.choices.size() > 0) {
                    String text = data.choices.get(0).text;
                    Log.d("GPT-3", text);


                    if (text.contains("ENTER") || text.contains("PRESS") || text.contains("SCROLL")) {
                        String[] elements = text.split("###");

                        String action = elements[1].split(":")[1].trim();
                        String bounds = "down";
                        if (elements[2].split(":").length > 1) {
                            bounds = elements[2].split(":")[1].trim();
                        }


                        String target = elements[3].split(":")[1].trim();
                        //Log.d("ACTION",action + target);


                        switch (action) {
                            case "<PRESS>":

                                String[] arr = bounds.substring(1, bounds.length() - 1).replaceAll("\\]\\s*\\[", ",").split(","); // split into pairs
                                int[] arrInt = new int[4]; // create array of appropriate size
                                int index = 0;
                                for (String str : arr) {

                                    arrInt[index++] = Integer.parseInt(str); // add first number to array

                                }

                                int xPoint = (arrInt[0] + arrInt[2]) / 2;
                                int yPoint = (arrInt[1] + arrInt[3]) / 2;
                                touchTo(xPoint, yPoint);

                                break;
                            case "<SCROLL>":
                                scrollingActivity(target);
                                break;
                            case "<ENTER>":
                                setTextForAllSubNode(currentSource, 0, target);
                                break;
                            default:
                                Log.d("duc", action + "/" + target);
                        }
                        currentSource.refresh();
                        updateToolTip();
                        guidedTaskExecutor();

                    } else {
                        String[] elements = text.split(":");
                        String action = elements[elements.length - 1];
                        speakerTask(action);
                    }

                }

                Log.d("hehe", "execute new command");
            }

            @Override
            public void onFailure(Call<ResponseObject> call, Throwable t) {
                Log.e("myErrTag", t.getMessage());
            }
        });
    }

    //Chat GPT

//
//        Call<ChatResponseObject> call = jsonApi.getDataChat(
//                new ChatRequestBody(prompt)
//        );
//        call.enqueue(new Callback<ChatResponseObject>() {
//            @Override
//            public void onResponse(Call<ChatResponseObject> call, Response<ChatResponseObject> response) {
//                if (!response.isSuccessful()) {
//                    assert response.errorBody() != null;
//
//
//                        Log.e("myErrTag", response.toString());
//
//                    return;
//                }
//
//                ChatResponseObject data = response.body();
//                if (data != null && data.choices != null && data.choices.size() > 0) {
//                    String text = data.choices.get(0).message.content;
//                    Log.d("ChatGPT",text);
//
//                    chatHistory.addMessage(currentCommand, text);
////                    if (text.contains("ENTER") ||text.contains("PRESS") ||text.contains("SCROLL") ) {
////
////                        String action = text.substring(text.lastIndexOf("<") + 1,text.lastIndexOf(">"));
////                        String target = text.substring(text.lastIndexOf(":") + 1, text.length());
////                        Log.d("ACTION",action + target);
////
////                        switch (action) {
////                            case "PRESS":
////
////                                target = target.substring(1, target.lastIndexOf("]")); // remove brackets
////                                String[] arr  = target.replaceAll("\\]\\s*\\[", ",").split(","); // split into pairs
////                                int[] arrInt = new int[4]; // create array of appropriate size
////                                int index = 0;
////                                for (String str : arr) {
////
////                                    arrInt[index++] = Integer.parseInt(str); // add first number to array
////
////                                }
////
////                                int xPoint = (arrInt[0] + arrInt[2])/2;
////                                int yPoint = (arrInt[1] + arrInt[3])/2;
////                                touchTo(xPoint,yPoint);
////                            break;
////                            case "SCROLL":
////                                scrollingActivity(target);
////                                break;
////                            case "ENTER":
////                                setTextForAllSubNode(currentSource,0, target);
////                                break;
////                            default:
////                                Log.d("duc", action + "/" + target);
////                        }
////                    }
//
//                }
//            }
//            @Override
//            public void onFailure(Call<ChatResponseObject> call, Throwable t) {
//                Log.e("myErrTag", t.getMessage());
//            }
//        });

    public void requestScrollInput() {
        String scrollPrompt = Command.scrollPrompt(currentCommand, currentScreenXML, getAppNameFromPackageName(currentSource.getPackageName().toString()), currentSource.getPackageName().toString(), chatHistory.getChatHistory(), currentActivityName);
        Log.d("ddd", scrollPrompt);
        Call<ResponseObject> call = jsonApi.getData(
                new RequestBody(scrollPrompt)
        );
        call.enqueue(new Callback<ResponseObject>() {
            @Override
            public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                if (!response.isSuccessful()) {
                    assert response.errorBody() != null;


                    Log.e("myErrTag", response.toString());

                    return;
                }

                ResponseObject data = response.body();
                if (data != null && data.choices != null && data.choices.size() > 0) {
                    String text = data.choices.get(0).text;
                    Log.d("SCROLL", text);
//                    String[] elements = text.split("###");
//                    String target = elements[3].split(":")[1].trim();
                    scrollingActivity(text);
                    speakerTask("Say continue to get to the next task");


                }
            }

            @Override
            public void onFailure(Call<ResponseObject> call, Throwable t) {
                Log.e("myErrTag", t.getMessage());
            }
        });

    }

    public void requestTextInput() {

        String textPrompt = Command.textPrompt(currentCommand, currentScreenXML, getAppNameFromPackageName(currentSource.getPackageName().toString()), currentSource.getPackageName().toString(), chatHistory.getChatHistory(), currentActivityName);
        Call<ResponseObject> call = jsonApi.getData(
                new RequestBody(textPrompt)
        );
        call.enqueue(new Callback<ResponseObject>() {
            @Override
            public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                if (!response.isSuccessful()) {
                    assert response.errorBody() != null;

                    try {
                        Log.e("myErrTag", response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                ResponseObject data = response.body();
                if (data != null && data.choices != null && data.choices.size() > 0) {
                    String text = data.choices.get(0).text;
                    Log.d("ENTER", text);
                    String[] elements = text.split("###");
                    String target = elements[3].split(":")[1].trim();
                    setTextForAllSubNode(currentSource, 0, target);
                    speakerTask("Say continue to get to the next task");

                }
            }

            @Override
            public void onFailure(Call<ResponseObject> call, Throwable t) {
                Log.e("myErrTag", t.getMessage());
            }
        });

    }

    public void requestPressAction() {

        String pressPrompt = Command.pressPrompt(currentCommand, currentTooltipCount + "", currentScreenXML, getAppNameFromPackageName(currentSource.getPackageName().toString()), currentSource.getPackageName().toString(), chatHistory.getChatHistory(), currentActivityName);
        //Log.d("prompt",pressPrompt);
        Call<ResponseObject> call = jsonApi.getData(
                new RequestBody(pressPrompt)
        );
        call.enqueue(new Callback<ResponseObject>() {
            @Override
            public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                if (!response.isSuccessful()) {
                    assert response.errorBody() != null;

                    try {
                        Log.e("myErrTag", response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                ResponseObject data = response.body();
                if (data != null && data.choices != null && data.choices.size() > 0) {
                    String text = data.choices.get(0).text;
                    Log.d("CLICK", text);

                    String[] elements = text.split("###");

                    String target = elements[2].split(":")[1].trim();

                    if (elements[3].split(":").length > 1) {
                        String bounds = elements[3].split(":")[1].trim();
                        String[] arr = bounds.substring(1, bounds.length() - 1).replaceAll("\\]\\s*\\[", ",").split(","); // split into pairs
                        int[] arrInt = new int[4]; // create array of appropriate size
                        int index = 0;
                        for (String str : arr) {

                            arrInt[index++] = Integer.parseInt(str); // add first number to array

                        }

                        int xPoint = (arrInt[0] + arrInt[2]) / 2;
                        int yPoint = (arrInt[1] + arrInt[3]) / 2;
                        touchTo(xPoint, yPoint);
                        updateGraphDB(target, "PRESS", bounds);
                    } else {
                        clickButtonByText(target);
                        updateGraphDB(target, "PRESS", " ");
                    }


                }
            }

            @Override
            public void onFailure(Call<ResponseObject> call, Throwable t) {
                Log.e("myErrTag", t.getMessage());
            }
        });
    }

    private void loopPressByBounds(List<Edge> edges) {
        for(Edge edge : edges) {
            String bounds = edge.bounds;
            String[] arr = bounds.substring(1,bounds.length() - 1).replaceAll("\\]\\s*\\[", ",").split(","); // split into pairs
            int[] arrInt = new int[4]; // create array of appropriate size
            int index = 0;
            for (String str : arr) {

                arrInt[index++] = Integer.parseInt(str); // add first number to array

            }

            int xPoint = (arrInt[0] + arrInt[2]) / 2;
            int yPoint = (arrInt[1] + arrInt[3]) / 2;
            touchTo(xPoint, yPoint);
            wait(2);
        }


    }

    private void updateGraphDB(String target, String action, String bounds) {

        String cacheXML = currentScreenXML;
        while (currentScreenXML.equals(cacheXML)) {
            wait(1);
            autoReload();
        }
        autoReload();
        speakerTask("Say continue to get to the next task");
        getAppDescription(target, action, bounds);

    }

    public static void wait(int second) {
        try {
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException e) {
            Log.e("ERROR", "sleep failed");
        }
    }

    public void getAppDescription(String target, String action, String bounds) {

        String summarisePrompt = Command.screenSummarise(currentScreenXML, getAppNameFromPackageName(currentSource.getPackageName().toString()), currentSource.getPackageName().toString(), currentActivityName);
        Log.d("summarise_prompt", summarisePrompt);
        Call<ResponseObject> call = jsonApi.getData(
                new RequestBody(summarisePrompt)
        );
        call.enqueue(new Callback<ResponseObject>() {
            @Override
            public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                if (!response.isSuccessful()) {
                    assert response.errorBody() != null;

                    try {
                        Log.e("myErrTag", response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                ResponseObject data = response.body();
                if (data != null && data.choices != null && data.choices.size() > 0) {
                    String text = data.choices.get(0).text;
                    Log.d("SUMMARISE", text);

                    Node newNode = graph.addNode(currentActivityName, Utils.clickableElementAsString(currentSource), text, currentScreenXML);
                    Edge edge = new Edge(currentNodeGraph, newNode, action, target, bounds);
                    currentNodeGraph = newNode;
                    graph.addEdge(edge);
                    //Log.d("GRAPH", graph.toString());

                }
            }

            @Override
            public void onFailure(Call<ResponseObject> call, Throwable t) {
                Log.e("myErrTag", t.getMessage());
            }
        });
    }

    public void requestAppName() {
        ArrayList<String> appNames = new ArrayList<>();
        ArrayList<String> systemApp = new ArrayList<>(Arrays.asList("voicify", "meet", "google services framework", "speech services by google", "home", "keep notes", "google vr services", "pixel ambient services", "google connectivity services", "default print service", "live transcribe & sound notifications", "pixel wallpapers 18", "nfc service", "android accessibility suite", "pixel ambient service", "127", "android system intelligence", "select hour", "start date – %1$s", "sim manager", "call management", "markup", "android system webview", "true", "package installer", "work setup", "pixel launcher", "google wallet", "settings services", "bluetooth", "150", "220", "styles & wallpapers", "android auto", "adaptive connectivity services", "device health services", "carrier setup", "live wallpaper picker", "android auto", "clear text", "pixel buds", "google wi-fi provisioner", "system ui", "system tracing", "gboard", "smart storage", "wireless emergency alerts", "playground", "media storage", "false", "device setup", "google tv", "android system", "pixel setup", "nfc", "personal safety", "current selection: %1$s", "extreme battery saver"));

        for (PackageDataObject packageDataObject : packageDataObjects) {
            if (!systemApp.contains(packageDataObject.name))
                appNames.add(packageDataObject.name);
        }
        String appPrompt = Command.appPrompt(currentCommand, listToString(appNames));
        //Log.d("prompt", appPrompt);
        Call<ResponseObject> call = jsonApi.getData(
                new RequestBody(appPrompt)
        );
        call.enqueue(new Callback<ResponseObject>() {
            @Override
            public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                if (!response.isSuccessful()) {
                    assert response.errorBody() != null;

                    try {
                        Log.e("myErrTag", response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                ResponseObject data = response.body();
                if (data != null && data.choices != null && data.choices.size() > 0) {
                    String[] command = data.choices.get(0).text.split("&&&");
                    String app = command[0].toLowerCase(Locale.ROOT).trim();
                    String feature = command[1].toLowerCase(Locale.ROOT).trim();
                    Log.d("ggg", data.choices.get(0).text);

                    for (PackageDataObject packageDataObject : packageDataObjects) {

                        String packageAppName = packageDataObject.name.toLowerCase(Locale.ROOT).trim();
                        String searchingAppName = app.toLowerCase(Locale.ROOT).trim();
                        if (packageAppName.contains(searchingAppName)) {
                            Log.d("GET_APP", "Found app");
                            requestIntentDeepLinkName(packageDataObject);
                            break;

                        }

                    }


                }
            }

            @Override
            public void onFailure(Call<ResponseObject> call, Throwable t) {
                Log.e("myErrTag", t.getMessage());
            }
        });
    }

    public void requestIntentDeepLinkName(PackageDataObject currentPackageDataObject) {


        String appPrompt = Command.deeplinkPrompt(currentPackageDataObject.name, currentPackageDataObject.getGPT3Context(), currentCommand);
        //Log.d("prompt", appPrompt);
        Call<ResponseObject> call = jsonApi.getData(
                new RequestBody(appPrompt)
        );
        call.enqueue(new Callback<ResponseObject>() {
            @Override
            public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                if (!response.isSuccessful()) {
                    assert response.errorBody() != null;

                    try {
                        Log.e("myErrTag", response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                ResponseObject data = response.body();
                if (data != null && data.choices != null && data.choices.size() > 0) {
                    String result = data.choices.get(0).text.substring(2);
                    Log.d("GET_DEEPLINK", result);

                    if (currentPackageDataObject.getDeepLinks().contains(result)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(result));
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            // Define what your app should do if no activity can handle the intent.

                            Log.d("Invocation Errors", e.getMessage());
                        }
                    } else if (currentPackageDataObject.getAllIntent().contains(result)) {
                        String[] intents = result.split("  ");
                        Log.d("ggg", intents.toString());

                        Intent intent = new Intent(intents[0]);
                        intent.setComponent(new ComponentName(currentPackageDataObject.packageName, intents[1]));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        PackageManager packageManager = getPackageManager();

                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent);
                        } else {
                            Intent mIntent = getPackageManager().getLaunchIntentForPackage(
                                    currentPackageDataObject.packageName);

                            if (mIntent != null) {
                                try {
                                    Log.d("Duc", "Open app launcher screen instead");
                                    startActivity(mIntent);

                                } catch (ActivityNotFoundException err) {

                                    Toast t = Toast.makeText(getApplicationContext(),
                                            "APP NOT FOUND", Toast.LENGTH_SHORT);
                                    t.show();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseObject> call, Throwable t) {
                Log.e("myErrTag", t.getMessage());
            }
        });
        speakerTask("Say continue to get to the next task");

    }

    public void getScrollableNode(AccessibilityNodeInfo currentNode) {
        /**
         * Get all the scrollable node in the current screen.
         * @param: currentNode: the current node that is being checked ( start from root node and recursively for all node)
         */
        if (currentNode == null) return;
        if (currentNode.isClickable()) {
            scrollableNodes.add(currentNode);
        }
        for (int i = 0; i < currentNode.getChildCount(); ++i) {
            getScrollableNode(currentNode.getChild(i));    // recursive call
        }
    }

    public String searchForTextView(AccessibilityNodeInfo currentNode, String allTexts) {
        String concatenatedString = allTexts;
        if (currentNode == null || concatenatedString.split(" ").length > 5)
            return concatenatedString;

        if (currentNode.getClassName() != null && currentNode.getClassName().equals("android.widget.TextView") && currentNode.getText() != null) {
            concatenatedString += currentNode.getText().toString() + " ";
        } else {
            for (int i = 0; i < currentNode.getChildCount(); ++i) {
                concatenatedString += searchForTextView(currentNode.getChild(i), concatenatedString);    // recursive call
            }
        }

        return concatenatedString;
    }


    public void setTextForAllSubNode(AccessibilityNodeInfo nodeInfo, int depth, String text) {
        /**
         * This function will set text for all sub-node ( all element on the screen)
         * @param: nodeInfo : current node that this function is called on ( will start from root node)
         * @param: depth : the current level of leaf
         * @param: text: the passed in text for writing in the edit text field.
         */
        if (nodeInfo == null) return;   // null check
        if (nodeInfo.isEditable()) {      // check if the node has editable field
            setGivenText(nodeInfo, text);       // call a method to put in the text
            Log.d("duc", nodeInfo + "");
        }
        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {    // recursive call to reach all nested nodes/leaves
            setTextForAllSubNode(nodeInfo.getChild(i), depth + 1, text);
        }
    }

    public boolean scrollingActivity(String command) {
        /**
         * This function will work as scrolling the screen for user on invocation.
         * @param: a string - can be up or down specifying the scrolling direction.
         */
        boolean returnVal = false;
        getScrollableNode(currentSource);   // get all scrollable not within current screen
        if (scrollableNodes.size() == 0) {
            Log.d(debugLogTag, "Can't find item to scroll");
            return false;
        } else {      // if there exist item to be scrolled.
            for (AccessibilityNodeInfo node : scrollableNodes) {
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                // scrolling using gesture builder.
                final int height = displayMetrics.heightPixels;
                final int top = (int) (height * .25);
                final int mid = (int) (height * .5);
                final int bottom = (int) (height * .75);
                final int midX = displayMetrics.widthPixels / 2;
                final int width = displayMetrics.widthPixels;
                final int left = (int) (width * 0.25);
                final int right = (int) (width * 0.75);

                GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
                Path path = new Path();
                command = command.toLowerCase().trim();

                // Scroll up
                if (command.contains("up")) {
                    path.moveTo(midX, mid);
                    path.lineTo(midX, bottom);
                    returnVal = true;
                    // Scroll down
                } else if (command.contains("down")) {
                    path.moveTo(midX, mid);
                    path.lineTo(midX, top);
                    returnVal = true;
                } else if (command.contains("right")) {
                    path.moveTo(right, mid);
                    path.lineTo(left, mid);
                    returnVal = true;
                } else if (command.contains("left")) {
                    path.moveTo(left, mid);
                    path.lineTo(right, mid);
                    returnVal = true;
                } else {
                    path.moveTo(midX, mid);
                    path.lineTo(midX, top);
                    returnVal = true;
                }

                gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 100, 300));
                dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) { // gesture execution
                        //Log.d(debugLogTag,"Gesture Completed");
                        super.onCompleted(gestureDescription);
                    }
                }, null);
            }
        }
        return returnVal;
    }

    @Override
    public void onInterrupt() {
        Log.d("Service Test", "Service Disconnected");
    }

    @Override
    protected void onServiceConnected() {
        /**
         * This function is invoked after the accessibility service has been stared by the user. this
         * function inflates the layout and draws the floating UI for the service. It also initialises
         * speech recognition & checks audio permissions.
         *
         * @param: None
         * @return: None
         * @post-cond: A button floating on top of the screen can be used to control the service
         *             by the user if the app have all the permissions it needs. Else opens settings
         *             page with the app's details.
         * */

        super.onServiceConnected();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Date date = new Date();
        currentTime = date.getTime();
//        try {
//            dictionary = new Dictionary();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        chatHistory = new ChatHistory();
        createSwitch();
        initializeSpeechRecognition();                      // Checking permissions & initialising speech recognition
        loadData();
        loadAPIConnection();
        getDisplayMetrics();
        graph = new Graph("", getApplicationContext());
        //Log.d("GRAPH", graph.toString());
        Log.d(debugLogTag, "Service Connected");
        loadAppNames();
        //createText2VecModel();

        autoReloadHandler.post(runnable);
    }

    void loadAPIConnection() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        jsonApi = retrofit.create(JsonApi.class);
    }

    void loadData() {

        mPrefs = getSharedPreferences(FILE_NAME, 0);
        Gson gson = new Gson();
        String json = mPrefs.getString("packageDataObjects", "");
        Type type = new TypeToken<ArrayList<PackageDataObject>>() {
        }.getType();
        packageDataObjects = gson.fromJson(json, type);
        componentAsString = getAppComponentAsString();
    }

    void deleteIntent() {
        Log.d("duc", currentOpeningFeature + "// " + currentOpeningPackage);
        if (packageDataObjects.size() > 0) {
            for (PackageDataObject packageDataObject : packageDataObjects) {
                if (packageDataObject.packageName.equals(currentOpeningPackage)) {
                    String[] dataArrIntent = new String[matchedIntents.size()];
                    dataArrIntent = matchedIntents.toArray(dataArrIntent);
                    String currentConcatenatedIntent = dataArrIntent[currentIntentIndex];
                    String[] intentComponent = currentConcatenatedIntent.split("  ");

                    ArrayList<String> intentActionList = packageDataObject.intentsByActivity.get(intentComponent[1]);
                    assert intentActionList != null;
                    boolean remove = intentActionList.remove(intentComponent[0]);
                    Log.d("duc", currentConcatenatedIntent);

                    packageDataObject.intentsByActivity.put(intentComponent[1], intentActionList);
                    if (remove) {
                        Log.d("duc", "Removed from the intent database");
                    }
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(packageDataObjects);
                    prefsEditor.putString("packageDataObjects", json);
                    prefsEditor.commit();
                    invokeComponent(currentAppName, currentOpeningFeature);
                    break;
                }
            }
        }

    }

    private void getDisplayMetrics() {
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;
    }


    private void createSwitch() {
        /**
         * This code will create a layout for the switch. This code is called whenever service is
         * connected and will be gone when service is shutdown
         *
         */

        // Check for permissions
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayout = new FrameLayout(this);

        // Create layout for switchBar
        switchBar = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        switchBar.gravity = Gravity.TOP;  // stick it to the top
        //WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |


        LayoutInflater inflater = LayoutInflater.from(this);
        View actionBar = inflater.inflate(R.layout.action_bar, mLayout);
        wm.addView(mLayout, switchBar);       // add it to the screen


        listenButton = mLayout.findViewById(R.id.listenBtn);
        resetBtn = mLayout.findViewById(R.id.resetBtn);
        // textMsg = mLayout.findViewById(R.id.msg);
        //  inputTxt = mLayout.findViewById(R.id.inputTxt);
        //  inputTxt.setBackgroundResource(R.color.black);
        listenButton.setBackgroundResource(R.drawable.start_btn);
        configureListenButton();
        configureResetButton();
    }


    // This method is responsible for updating the switchBar coordniates upon touch and updating the view
    @Override
    public boolean onTouch(View view1, MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {

            case MotionEvent.ACTION_DOWN:
                initialX = switchBar.x;
                initialY = switchBar.y;
                initialTouchX = motionEvent.getRawX();
                initialTouchY = motionEvent.getRawY();
                break;

            case MotionEvent.ACTION_UP:
                break;

            case MotionEvent.ACTION_MOVE:
                switchBar.x = initialX + (int) (motionEvent.getRawX() - initialTouchX);
                switchBar.y = initialY + (int) (motionEvent.getRawY() - initialTouchY);
                wm.updateViewLayout(mLayout, switchBar);
                break;
        }
        return false;
    }

    private void removeAllTooltips() {
        /**
         * This function will be called when something changed on the screen, reset all tooltips.
         *
         */
        for (TooltipRequiredNode tooltip : tooltipRequiredNodes) {    // remove the list of current tooltips
            if (tooltip.tooltipLayout != null)
                wm.removeView(tooltip.tooltipLayout);   // remove them from the screen
        }
        // reset all variables when changing to new screen.
        currentTooltipCount = 1;
        tooltipRequiredNodes.clear();
        foundLabeledNodes.clear();
    }

    private void configureListenButton() {
        /**
         * This function is called after the service has been connected. This function binds
         * functionality to the master button which can be used to turn on/off the tool.
         *
         * @param: None
         * @return: None
         * @post-cond: functionality has been added to the inflated button
         * */

        listenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listenButton.getText().toString().equalsIgnoreCase("start")) {
                    listenButton.setText("Stop");
                    listenButton.setBackgroundResource(R.drawable.stop_btn);
                    isVoiceCommandConnected = true;
                    speechRecognizer.startListening(speechRecognizerIntent);       // on click listener to start listening audio
                } else {
                    listenButton.setText("Start");

                    listenButton.setBackgroundResource(R.color.transparent);
                    isVoiceCommandConnected = false;
                    listenButton.setBackgroundResource(R.drawable.start_btn);
                    speechRecognizer.stopListening();           // on click listener to stop listening & processing data
                    //         textMsg.setText("");
                }
            }
        });
    }


    private void configureResetButton() {
        /**
         * This function is called after the service has been connected. This function binds
         * functionality to the master button which can be used to turn on/off the tool.
         *
         * @param: None
         * @return: None
         * @post-cond: functionality has been added to the inflated button
         * */

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String summarisePrompt = Command.screenSummarise(currentScreenXML, getAppNameFromPackageName(currentSource.getPackageName().toString()), currentSource.getPackageName().toString(), currentActivityName);
                Log.d("summarise_prompt", summarisePrompt);
                Call<ResponseObject> call = jsonApi.getData(
                        new RequestBody(summarisePrompt)
                );
                call.enqueue(new Callback<ResponseObject>() {
                    @Override
                    public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                        if (!response.isSuccessful()) {
                            assert response.errorBody() != null;

                            try {
                                Log.e("myErrTag", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                        ResponseObject data = response.body();
                        if (data != null && data.choices != null && data.choices.size() > 0) {
                            String text = data.choices.get(0).text;
                            Log.d("SUMMARISE", text);

                            Node newNode = graph.addNode(currentActivityName, Utils.clickableElementAsString(currentSource), text, currentScreenXML);
                            currentNodeGraph = newNode;
                            //Log.d("GRAPH", graph.toString());

                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseObject> call, Throwable t) {
                        Log.e("myErrTag", t.getMessage());
                    }
                });
            }
        });
    }

    private boolean openApp(String inputName) {
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

        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA); // getting meta data of all installed apps

        for (ApplicationInfo packageInfo : packages) {          // checking if the input has a match with app name
            try {
                ApplicationInfo info = pm.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA);
                String appName = (String) pm.getApplicationLabel(info).toString().toLowerCase();
                if (appName.equals(inputName)) {
                    Intent mIntent = getPackageManager().getLaunchIntentForPackage(
                            packageInfo.packageName);
                    if (mIntent != null) {
                        try {
                            startActivity(mIntent);
                            return true;
                            // Adding some text-to-speech feedback for opening apps based on input
                            // Text-to-speech feedback if app not found);
//                            speakerTask(speechPrompt.get("open") + inputName);

                        } catch (ActivityNotFoundException err) {
                            // Text-to-speech feedback if app not found
//                            speakerTask(speechPrompt.get("noMatch") + inputName);

                            // Render toast message on screen
                            Toast t = Toast.makeText(getApplicationContext(),
                                    "APP NOT FOUND", Toast.LENGTH_SHORT);
                            t.show();
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();                // handling app not found exception
//                speakerTask(speechPrompt.get("noMatch") + inputName);
            }
        }

        return false;
    }

    private void loadAppNames() {
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA); // getting meta data of all installed apps

        for (ApplicationInfo packageInfo : packages) {          // checking if the input has a match with app name
            try {
                ApplicationInfo info = pm.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA);
                String appName = (String) pm.getApplicationLabel(info).toString().toLowerCase();
                this.appNames.add(appName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();                // handling app not found exception
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            String appName = intent.getStringExtra("app_name");
            String componentName = intent.getStringExtra("component_name");
            Log.d("duc", appName + " " + componentName);
            invokeComponent(appName, componentName);

        }

        return result;
    }

    public void speakerTask(String toSpeak) {
        /**
         * Use this method to call out to TTSService (Text-To-speech service) to speak out message
         * param: a string to be spoken by the Text-to-speech service
         */
        Intent i = new Intent(this, TTSService.class);
        i.putExtra("message", toSpeak);
        // starts service for intent
        startService(i);
    }


    public void touchTo(int x, int y) {
        Path swipePath = new Path();
        swipePath.moveTo(x, y);
        swipePath.lineTo(x, y);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 50));
        dispatchGesture(gestureBuilder.build(), null, null);
    }

    private void clickButtonByLocation(int x, int y) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId("android:id/button1");
            if (!nodes.isEmpty()) {
                AccessibilityNodeInfo node = nodes.get(0);
                Rect rect = new Rect();
                node.getBoundsInScreen(rect);
                if (rect.contains(x, y)) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
            rootNode.recycle();
        }
    }

    boolean clickButtonByText(String word) {
        /**
         * This function will click a button (anything thats clickable) with provided information
         * param: word: a string to store data about what to click
         */
        // Processes input first to determine if number label was called
        // More efficient number label processing? Skips iterating through array of numbers and assumes the array is numerical order if input is a Digit
        word = word.trim().toLowerCase(Locale.ROOT);
        if (word.equals("")) {
            return false;
        }

        // check


        if (TextUtils.isDigitsOnly(word)) {
            //Log.d(debugLogTag,word);
            if (Integer.parseInt(word) <= currentTooltipCount - 1) {
                if (tooltipRequiredNodes.size() >= Integer.parseInt(word) && tooltipRequiredNodes.get(Integer.parseInt(word) - 1).nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    //Log.d(debugLogTag, "Clicked number: " + word);    // log the information
                    return true;
                }

            }
        }


        if (currentSource == null) {
            return false;
        }

        for (LabelFoundNode foundLabeledNode : foundLabeledNodes) {
            Log.d("hehe", foundLabeledNode.label);
            if (foundLabeledNode.label.contains(word.toLowerCase(Locale.ROOT))) {

                if (foundLabeledNode.nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    //Log.d(debugLogTag, "Clicked on description:" + word);
                    return true;
                }
                // return once clicked
            }
        }

        //Find ALL of the nodes that match the "text" argument.
        List<AccessibilityNodeInfo> list = currentSource.findAccessibilityNodeInfosByText(word);    // find the node by text
        for (final AccessibilityNodeInfo node : list) { // go through each node to see if action can be performed
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                return true;     // return once clicked
            }

        }
        // for some element that named with first capital word
        String camelCaseWord = word.substring(0, 1).toUpperCase() + word.substring(1);
        list = currentSource.findAccessibilityNodeInfosByText(camelCaseWord);    // find the node by text
        for (final AccessibilityNodeInfo node : list) { // go through each node to see if action can be performed
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                return true;     // return once clicked
            }

        }


        return false;
    }

    // Enter action
    public void setGivenText(AccessibilityNodeInfo currentNode, String text) {
        /**
         * This function will set the text for a given node
         * @param: currentNode: the node to store information about object that will be inserted the text.
         * @param: text: the customized passed in text to be written in the field.
         */
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo
                .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        currentNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
    }

    // new code
    String getAppComponent(PackageDataObject packageDataObject, String referencedName) {
        ArrayList<String> commonWords = new ArrayList<>(Arrays.asList("http", "deeplink", "com", "android", "intent", "action", "google"));
        List<String> appWords = Arrays.asList(packageDataObject.packageName.split("\\."));
        ArrayList<String> components = new ArrayList<>();
        for (String deeplink : packageDataObject.deepLinks) {
            String[] words = deeplink.split("[^\\w']+");
            String prefix = words[words.length - 1];
            if (!appWords.contains(prefix) && !commonWords.contains(prefix)) {
                components.add(prefix);
                if (referencedName.toLowerCase(Locale.ROOT).equals(prefix)) {
                    return deeplink;
                }
            }
        }
        for (String intent : packageDataObject.getQuerySearch("")) {
            String[] words = intent.split("[^\\w']+");
            ArrayList<String> keywords = new ArrayList<>();
            for (String word : words) {
                if (!appWords.contains(word) && !commonWords.contains(word)) {
                    if (word.endsWith("Activity")) {
                        //Log.d("nani", word);
                        keywords.add(word.replace("Activity", ""));
                    } else if (word.endsWith("Launcher")) {
                        //Log.d("nani", word);
                        keywords.add(word.replace("Launcher", ""));
                    } else if (word.contains("_")) {
                        //Log.d("nani", word);
                        word.replace("_", " ");
                    } else {
                        keywords.add(word);
                    }

                }
            }
            ArrayList<String> finalSet = new ArrayList<>();
            for (String keyword : keywords) {
                String[] singleWords = keyword.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
                String element = String.join(" ", singleWords);
                finalSet.add(element);
            }
            String componentName = String.join(" ", finalSet);
            components.add(componentName);
            if (componentName.toLowerCase(Locale.ROOT).equals(referencedName)) {
                return intent;
            }
        }
        Log.d("Test", packageDataObject.name + ": " + components);
        return "!@#!@#@!#!@#";
    }

    public void invokeComponent(String appSearchString, String featureSearchString) {
        appSearchString = appSearchString.replace("_", " ");
        featureSearchString = featureSearchString.replace("_", " ");
        boolean isAppFound = false;
        String componentOutput = "";
        PackageDataObject currentPackage = null;
        for (PackageDataObject packageDataObject : packageDataObjects) {

            if (packageDataObject.name.replace("!", "").equals(appSearchString)) {
                componentOutput = getAppComponent(packageDataObject, featureSearchString);
                isAppFound = true;
                currentPackage = packageDataObject;
                break;
            }


        }
        if (isAppFound) {
            if (currentPackage.getDeepLinks().contains(componentOutput)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(componentOutput));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Define what your app should do if no activity can handle the intent.

                    Log.d("Invocation Errors", e.getMessage());
                }
            } else if (currentPackage.getQuerySearch("").contains(componentOutput)) {
                String[] intents = componentOutput.split("  ");
                Intent intent = new Intent(intents[0]);
                intent.setComponent(new ComponentName(currentPackage.packageName, intents[1]));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PackageManager packageManager = getPackageManager();

                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent);
                } else {
                    Intent mIntent = getPackageManager().getLaunchIntentForPackage(
                            currentPackage.packageName);

                    if (mIntent != null) {
                        try {
                            Log.d("Duc", "Open app launcher screen instead");
                            startActivity(mIntent);
                            speakerTask("Component not found, opened the app instead");

                        } catch (ActivityNotFoundException err) {

                            Toast t = Toast.makeText(getApplicationContext(),
                                    "APP NOT FOUND", Toast.LENGTH_SHORT);
                            speakerTask("Cannot open the app");
                            t.show();
                        }
                    }
                }
            }
        }


    }

    public boolean isTransitionToNewApp(String packageName) {

        Log.d("duc", packageName + '/' + getRootInActiveWindow().getPackageName());
        return packageName == currentSource.getPackageName();

    }


    Handler intentValidationHandler = new Handler();
    final Runnable intentExecutionValidation = new Runnable() {
        public void run() {
            Log.d("duc", "Handler " + getRootInActiveWindow().getPackageName());
            Log.d("duc", "Handler " + currentOpeningPackage);
            if (currentOpeningPackage.equals(getRootInActiveWindow().getPackageName())) {

                Log.d("duc", "success");
                speakerTask("Opened the component");
            } else {
                currentIntentIndex += 1;
                Log.d("duc", "handler reinvoke");
                invokeComponent(currentAppName, currentOpeningFeature);
            }

        }
    };


}