package com.research.activityinvoker;

import java.util.ArrayList;
import java.util.Arrays;

public class Command {

    public static String guidedCommand(String command, String currentScreenXML, String packageName, String appName, String chatHistory, String currentActName) {
        String rule = " \n Guide users to achieve a task on Android device. APP: the currently used app. CURRENT_ACTIVITY_NAME: current that user is on  COMMAND: what user want to do in the app, XML: Current Android UI element on the screen as XML code. PACKAGE: package name of Android app. Follow these instructions at all costs: \n " +
                "1. ACTION must be one of the action in this list [PRESS, SCROLL, ENTER].\n " +
                "2. only PRESS a node that contains clickable=\"true\". If <ACTION> is PRESS, output format is <PRESS>: <BOUND>, where <BOUND> represent the location of the node, which must be from the XML.\n" +
                "3. only SCROLL a node that contains scrollable=\"true\". If <ACTION> is SCROLL, output format is <SCROLL>: <DIRECTION>, where <DIRECTION> either left, right, up, or down.\n" +
                "4. only ENTER for element that contains class=\"EditText\" and editable=\"true\". If <ACTION> is ENTER, output format is <ENTER>: <TEXT>, where <TEXT> is extracted from user command, represents the content user wants to input. \n" +
                "5. You MUST provide the single best action that user could perform or at least make a guess." +
                "6. Output this string \"Option not clear\" if you can't output any action and \"Reached destination screen\" if you think the screen represent what user wants." +
                chatHistory;

        String chainOfThought4 = "Let's think step by step. On the current screen, there are no direct element to see the history, as it will most probably in library screen of Youtube so we need to get to the library screen first, which is <node id=\"334\" class=\"Button\" content-desc=\"Library\" clickable=\"true\" bounds=\"[0,66][1080,1874]\">";
        String xml4 = "<node id=\"293\" class=\"View\" /><node><node id=\"294\" class=\"ScrollView\" scrollable=\"true\"><node><node id=\"295\" class=\"ImageView\" content-desc=\"YouTube\" /></node><node id=\"334\" class=\"Button\" content-desc=\"Library\" clickable=\"true\" bounds=\"[0,66][1080,1874]\"><node id=\"335\" class=\"ImageView\" /><node id=\"336\" text=\"Library\" class=\"TextView\" /></node></node></node><node id=\"337\" class=\"View\" /></node>\n";
        String example4 = itemQuestion("Youtube","show me my history","com.google.android.youtube", xml4, "WatchWhileActivity") + itemAnswer("<PRESS>","<node id=\"334\" class=\"Button\" content-desc=\"Library\" clickable=\"true\" bounds=\"[0,66][1080,1874]\">" ,"[0,66][1080,1874]", chainOfThought4);

        String chainOfThought3 = "Let's think step by step. On the screen, press upload button will allow you to upload a new file, which is <node id=\"2781\" text=\"Upload\" class=\"TextView\" clickable=\"true\" bounds=\"[234,432][1080,1200]\"/>";
        String xml3 = " <node><node id=\"2776\" class=\"View\" clickable=\"true\" /><node><node><node id=\"2777\" class=\"GridView\"><node><node id=\"2778\" class=\"ImageView\" /><node id=\"2779\" text=\"Folder\" class=\"TextView\" /></node><node><node id=\"2780\" class=\"ImageView\" /><node id=\"2781\" text=\"Upload\" class=\"TextView\" clickable=\"true\" bounds=\"[234,432][1080,1200]\"/></node><node><node id=\"2782\" class=\"ImageView\" /><node id=\"2783\" text=\"Scan\" class=\"TextView\" /></node><node><node id=\"2784\" class=\"ImageView\" /><node id=\"2785\" text=\"Google Docs\" class=\"TextView\" /></node><node><node id=\"2786\" class=\"ImageView\" /><node id=\"2787\" text=\"Google Sheets\" class=\"TextView\" /></node><node><node id=\"2788\" class=\"ImageView\" /><node id=\"2789\" text=\"Google Slides\" class=\"TextView\" /></node>";
        String example3 = itemQuestion("Google Drive","upload a new file","com.google.android.apps.docs",  xml3, "NavigationActivity") + itemAnswer("<PRESS>","<node id=\"2781\" text=\"Upload\" class=\"TextView\" clickable=\"true\" bounds=\"[234,432][1080,1200]\"/>" ,"[234,432][1080,1200]", chainOfThought3);

        String chainOfThought1 = "Let's think step by step. On this screen that shows History in Youtube, the delete history features will most likely be in the additional menu option, where pressing <node id=\"21\" class=\"ImageView\" content-desc=\"More options\" bounds=\"[584,620][430,560] clickable=\"true\" tooltip-desc=\"More options\" /> will go to that screen";
        String xml1 = "<node id=\"14\" class=\"View\" /><node><node><node><node id=\"15\" class=\"ScrollView\" scrollable=\"true\"><node><node id=\"17\" text=\"History\" class=\"TextView\" /><node id=\"19\" class=\"View\" content-desc=\"Cast. Disconnected\" clickable=\"true\" /><node id=\"20\" class=\"Button\" content-desc=\"Search\" clickable=\"true\" tooltip-desc=\"Search\" /><node id=\"21\" class=\"ImageView\" content-desc=\"More options\" bounds=\"[584,620][430,560] clickable=\"true\" tooltip-desc=\"More options\" /><node id=\"22\" class=\"ProgressBar\" /></node><node id=\"23\" class=\"HorizontalScrollView\"><node><node id=\"24\" class=\"Button\" content-desc=\"Home\" clickable=\"true\"></node><node id=\"27\" class=\"Button\" content-desc=\"Shorts\" clickable=\"true\"><node id=\"28\" class=\"ImageView\" /><node id=\"29\" text=\"Shorts\" class=\"TextView\" /><node id=\"38\" class=\"View\" /></node>. \n";
        String example1 = itemQuestion("Youtube","delete my history","com.google.android.youtube",  xml1, "WatchWhileActivity" ) + itemAnswer("<PRESS>", "<node id=\"21\" class=\"ImageView\" content-desc=\"More options\" bounds=\"[584,620][430,560] clickable=\"true\" tooltip-desc=\"More options\" />" , "[584,620][430,560]", chainOfThought1);

        String xml2 = "<node id=\"1673\" bounds=\"[120,220][450,510]\" class=\"Button\" content-desc=\"Settings\"><node id=\"1674\" class=\"ImageView\" /></node><node><node><node id=\"1675\" class=\"ImageView\" content-desc=\"Instagram from Meta\" /></node><node><node id=\"1676\" class=\"Button\" content-desc=\"Profile photo\"><node id=\"1677\" class=\"ImageView\" /></node><node id=\"1679\" class=\"View\" content-desc=\" \" /><node><node id=\"1680\" class=\"Button\" content-desc=\"Log in\" clickable=\"true\"><node id=\"1681\" text=\"Log in\" class=\"View\" content-desc=\"Log in\" /></node></node></node><node><node><node id=\"1682\" class=\"Button\" content-desc=\"Log into another account\" clickable=\"true\"><node id=\"1683\" text=\"Log into another account\" class=\"View\" content-desc=\"Log into another account\" /></node>\n";
        String chainOfThought2 = "Let's think step by step. Because there are no direct action to log out on the current screen, log out feature is included in the Instagram setting page and pressing the node <node id=\"1673\" bounds=\"[120,220][450,510]\" class=\"Button\" content-desc=\"Settings\"> can bring you to that page";
        String example2 = itemQuestion("Instagram","log out of my account","com.instagram.android",xml2, "BloksSignedOutFragmentActivity") + itemAnswer("<PRESS>", "<node id=\"1673\" bounds=\"[120,220][450,510]\" class=\"Button\" content-desc=\"Settings\">" ,"[120,220][450,510]", chainOfThought2);

        String xml5 = "<node id=\"310\" class=\"ScrollView\" scrollable=\"true\"><node><node id=\"311\" text=\"Search in mail\" class=\"EditText\" clickable=\"true\"><node id=\"312\" class=\"ImageView\" /><node id=\"313\" class=\"ImageButton\" content-desc=\"Open navigation drawer\" clickable=\"true\" tooltip-desc=\"Open navigation drawer\" /><node id=\"314\" text=\"Search in mail\" class=\"TextView\" /><node><node id=\"316\" class=\"ImageView\" /></node></node></node></node><node id=\"317\" class=\"HorizontalScrollView\" /></node><node id=\"320\" text=\"Dismiss\" class=\"Button\" content-desc=\"Dismiss tip\" clickable=\"true\" /></node><node id=\"321\" class=\"View\" /></node><node><node><node id=\"322\" class=\"ImageView\" /><node></node>";
        String chainOfThought5 = "Let's think step by step. Because it is in the mail list in Gmail app, scroll down will allow you to see older mails and there is a view that is scrollable";
        String example5 = itemQuestion("Gmail","I want to see older mails","com.google.android.gm",xml5, "ConversationListActivityGmail") + itemAnswer("<SCROLL>", "DOWN","" , chainOfThought5);

        String xml6 = "<node id=\"885\" class=\"ImageView\" content-desc=\"Back\" clickable=\"true\" /></node><node id=\"886\" text=\"Food, groceries, drinks, etc\" class=\"EditText\" clickable=\"true\" editable=\"true\" /></node></node></node><node><node><node id=\"887\" class=\"HorizontalScrollView\" scrollable=\"true\"><node><node><node id=\"888\" text=\"All\" class=\"TextView\" /></node><node><node id=\"889\" text=\"Restaurants\" class=\"TextView\" /></node><node><node id=\"890\" text=\"Grocery\" class=\"TextView\" /></node><node><node id=\"891\" text=\"Convenience\" class=\"TextView\" /></node><node><node id=\"892\" text=\"Alcohol\" class=\"TextView\" /></node><node><node id=\"893\" text=\"Pharmacy\" class=\"TextView\" /></node></node></node><node id=\"894\" class=\"View\" /></node></node><node><node id=\"895\" text=\"Recent searches\" class=\"TextView\" /></node></node>";
        String chainOfThought6 = "Let's think step by step. Because two condition qualified: it is an EditText search field on the screen and editable=\"true\" for that specific node where user can search for pizza on the screen";
        String example6 = itemQuestion("Uber Eats","I want to buy a pizza","com.ubercab.eats", xml6, ".RootActivity") + itemAnswer("<ENTER>", "pizza","", chainOfThought6);

        String xml7 = "<node><node id=\"25\" class=\"View\"><node><node id=\"27\" class=\"View\"><node id=\"28\" class=\"View\"><node id=\"29\" class=\"View\" content-desc=\"Search Movies &amp; TV Shows\" clickable=\"true\" bounds=\"[0,88][1080,198]\" /><node id=\"30\" class=\"View\">";
        String chainOfThought7 = "Let's think step by step. Although there seems like a View with Search in the content, it is not an EditText, hence we cannot directly put in the text but we have to press it first, which is <node id=\"29\" class=\"View\" content-desc=\"Search Movies &amp; TV Shows\" clickable=\"true\" bounds=\"[0,88][1080,198]\" />";
        String example7 = itemQuestion("top.mmshow.cetus","I want to search for Avatars","top.mmshow.cetus",xml7, ".MainActivity") + itemAnswer("<PRESS>", "[0,88][1080,198]","<node id=\"29\" class=\"View\" content-desc=\"Search Movies &amp; TV Shows\" clickable=\"true\" bounds=\"[0,88][1080,198]\" />", chainOfThought7);

        String question = itemQuestion(appName,command,packageName,currentScreenXML,currentActName);



        return rule + example1 + example2+ example4 + example5 + example6 + example7 + question + "A:";
    }

    static private String itemQuestion(String appName, String command, String packageName, String currentScreenXML, String actName){
        return "Q: APP: "+ appName + ". CURRENT_ACTIVITY_NAME: " + actName +". COMMAND: "+command + ". XML: " + currentScreenXML + ". PACKAGE: "+ packageName +".\n";
    }

    static private String itemAnswer(String action, String bounds,String targetNode, String chainOfThought){
        return "A: " + chainOfThought +".\n###Action:" + action + " ###Target Node:" + targetNode +" ###Bounds:" + bounds;
    }

    static private String itemAnswerActionDecision(String action, String chainOfThought){
        return "A: " + chainOfThought +".\nResult: ###" + action;
    }

    // todo:
    public static String decideActionPrompt(String currentCommand, String currentScreenXML, String packageName, String appName, String chatHistory,String currentActName) {
        String rule = "Parse the following user command to interact with the smartphone. The prompt provides APP: the currently used app. CURRENT_ACTIVITY_NAME: current that user is on COMMAND: what user want to do in the app, XML: Current Android UI element on the screen as XML code. PACKAGE: package name of Android app. First, output a single word for the ACTION that the user wants to perform from this list: PRESS, OPEN, ENTER, SCROLL. Rules: \n" +
                "1. only allow PRESS if the XML contains a node that has clickable=\"true\".\n" +
                "2. only allow SCROLL if the XML contains a node that has scrollable=\"true\".\n" +
                "3. only allow ENTER if the XML contains a node that has for element that contains class=\"EditText\" and editable=\"true\".\n" +
                "4. only allow OPEN if user mentions an application name in their command, otherwise always choose between PRESS, ENTER, SCROLL.\n" +
                "5. You MUST provide the single best action that user could perform or at least make a guess." ;

        String xml5 = "<node id=\"310\" class=\"ScrollView\" scrollable=\"true\"><node><node id=\"311\" text=\"Search in mail\" class=\"EditText\" clickable=\"true\"><node id=\"312\" class=\"ImageView\" /><node id=\"313\" class=\"ImageButton\" content-desc=\"Open navigation drawer\" clickable=\"true\" tooltip-desc=\"Open navigation drawer\" /><node id=\"314\" text=\"Search in mail\" class=\"TextView\" /><node><node id=\"316\" class=\"ImageView\" /></node></node></node></node><node id=\"317\" class=\"HorizontalScrollView\" /></node><node id=\"320\" text=\"Dismiss\" class=\"Button\" content-desc=\"Dismiss tip\" clickable=\"true\" /></node><node id=\"321\" class=\"View\" /></node><node><node><node id=\"322\" class=\"ImageView\" /><node></node>";
        String chainOfThought5 = "Let's think step by step. Because it is in the mail list in Gmail app, scroll down will allow you to see older mails and there is a view that is scrollable";
        String example5 = itemQuestion("Gmail","I want to see older mails","com.google.android.gm",xml5, "ConversationListActivityGmail") + itemAnswerActionDecision("SCROLL",chainOfThought5);

        String xml6 = "<node id=\"885\" class=\"ImageView\" content-desc=\"Back\" clickable=\"true\" /></node><node id=\"886\" text=\"Food, groceries, drinks, etc\" class=\"EditText\" clickable=\"true\" editable=\"true\" /></node></node></node><node><node><node id=\"887\" class=\"HorizontalScrollView\" scrollable=\"true\"><node><node><node id=\"888\" text=\"All\" class=\"TextView\" /></node><node><node id=\"889\" text=\"Restaurants\" class=\"TextView\" /></node><node><node id=\"890\" text=\"Grocery\" class=\"TextView\" /></node><node><node id=\"891\" text=\"Convenience\" class=\"TextView\" /></node><node><node id=\"892\" text=\"Alcohol\" class=\"TextView\" /></node><node><node id=\"893\" text=\"Pharmacy\" class=\"TextView\" /></node></node></node><node id=\"894\" class=\"View\" /></node></node><node><node id=\"895\" text=\"Recent searches\" class=\"TextView\" /></node></node>";
        String chainOfThought6 = "Let's think step by step. Because two condition qualified: it is an EditText search field on the screen and editable=\"true\" for that specific node where user can search for pizza on the screen";
        String example6 = itemQuestion("Uber Eats","I want to buy a pizza","com.ubercab.eats", xml6, ".RootActivity")+ itemAnswerActionDecision("ENTER",chainOfThought6);

        String chainOfThought4 = "Let's think step by step. On the current screen, there are no direct element to see the history, as it will most probably in library screen of Youtube so we need to get to the library screen first, which is <node id=\"334\" class=\"Button\" content-desc=\"Library\" clickable=\"true\" bounds=\"[0,66][1080,1874]\">";
        String xml4 = "<node id=\"293\" class=\"View\" /><node><node id=\"294\" class=\"ScrollView\" scrollable=\"true\"><node><node id=\"295\" class=\"ImageView\" content-desc=\"YouTube\" /></node><node id=\"334\" class=\"Button\" content-desc=\"Library\" clickable=\"true\" bounds=\"[0,66][1080,1874]\"><node id=\"335\" class=\"ImageView\" /><node id=\"336\" text=\"Library\" class=\"TextView\" /></node></node></node><node id=\"337\" class=\"View\" /></node>\n";
        String example4 = itemQuestion("Youtube","show me my history","com.google.android.youtube", xml4, "WatchWhileActivity") + itemAnswerActionDecision("PRESS",chainOfThought4);

        String chainOfThought3 = "Let's think step by step. Because user mention an app name in the command, we should first return OPEN to get to the app first";
        String xml3 = "<node id=\"293\" class=\"View\" /><node><node id=\"294\" class=\"ScrollView\" scrollable=\"true\"><node><node id=\"295\" class=\"ImageView\" content-desc=\"YouTube\" /></node><node id=\"334\" class=\"Button\" content-desc=\"Library\" clickable=\"true\" bounds=\"[0,66][1080,1874]\"><node id=\"335\" class=\"ImageView\" /><node id=\"336\" text=\"Library\" class=\"TextView\" /></node></node></node><node id=\"337\" class=\"View\" /></node>\n";
        String example3 = itemQuestion("Youtube","Open the message list in Gmail","com.google.android.youtube", xml3, "WatchWhileActivity") + itemAnswerActionDecision("OPEN",chainOfThought3);

        return rule + example5 + example6 + example4 + example3 + itemQuestion(appName,currentCommand,packageName,currentScreenXML,currentActName);
    }

    public static String scrollPrompt(String currentCommand, String currentScreenXML, String packageName, String appName, String chatHistory, String currentActName) {
        String rule =  "Input a voice command to smartphone to scroll. The prompt provides APP: the currently used app. CURRENT_ACTIVITY_NAME: current that user is on COMMAND: what user want to do in the app, XML: Current Android UI element on the screen as XML code. PACKAGE: package name of Android app. Output a single word LEFT, RIGHT, UP, DOWN for TARGET. Remember that swipe direction is opposite to scroll direction.";

        String xml5 = "<node id=\"310\" class=\"ScrollView\" scrollable=\"true\"><node><node id=\"311\" text=\"Search in mail\" class=\"EditText\" clickable=\"true\"><node id=\"312\" class=\"ImageView\" /><node id=\"313\" class=\"ImageButton\" content-desc=\"Open navigation drawer\" clickable=\"true\" tooltip-desc=\"Open navigation drawer\" /><node id=\"314\" text=\"Search in mail\" class=\"TextView\" /><node><node id=\"316\" class=\"ImageView\" /></node></node></node></node><node id=\"317\" class=\"HorizontalScrollView\" /></node><node id=\"320\" text=\"Dismiss\" class=\"Button\" content-desc=\"Dismiss tip\" clickable=\"true\" /></node><node id=\"321\" class=\"View\" /></node><node><node><node id=\"322\" class=\"ImageView\" /><node></node>";
        String chainOfThought5 = "Let's think step by step. Because it is in the mail list in Gmail app, scroll down will allow you to see older mails and there is a view that is scrollable";
        String example5 = itemQuestion("Gmail","I want to see older mails","com.google.android.gm",xml5, "ConversationListActivityGmail") + itemAnswer("<SCROLL>", "","DOWN" , chainOfThought5);

        String xml1 = "<node id=\"310\" class=\"ScrollView\" scrollable=\"true\"><node><node id=\"311\" text=\"Search in mail\" class=\"EditText\" clickable=\"true\"><node id=\"312\" class=\"ImageView\" /><node id=\"313\" class=\"ImageButton\" content-desc=\"Open navigation drawer\" clickable=\"true\" tooltip-desc=\"Open navigation drawer\" /><node id=\"314\" text=\"Search in mail\" class=\"TextView\" /><node><node id=\"316\" class=\"ImageView\" /></node></node></node></node><node id=\"317\" class=\"HorizontalScrollView\" /></node><node id=\"320\" text=\"Dismiss\" class=\"Button\" content-desc=\"Dismiss tip\" clickable=\"true\" /></node><node id=\"321\" class=\"View\" /></node><node><node><node id=\"322\" class=\"ImageView\" /><node></node>";
        String chainOfThought1 = "Let's think step by step. Because it is in the mail list in Gmail app, scroll down will allow you to see older mails and there is a view that is scrollable";
        String example1 = itemQuestion("Gmail","Swipe up","com.google.android.gm",xml1, "ConversationListActivityGmail") + itemAnswer("<SCROLL>", "","DOWN" , chainOfThought1);

        String xml2 = "<node id=\"310\" class=\"ScrollView\" scrollable=\"true\"><node><node id=\"311\" text=\"Search in mail\" class=\"EditText\" clickable=\"true\"><node id=\"312\" class=\"ImageView\" /><node id=\"313\" class=\"ImageButton\" content-desc=\"Open navigation drawer\" clickable=\"true\" tooltip-desc=\"Open navigation drawer\" /><node id=\"314\" text=\"Search in mail\" class=\"TextView\" /><node><node id=\"316\" class=\"ImageView\" /></node></node></node></node><node id=\"317\" class=\"HorizontalScrollView\" /></node><node id=\"320\" text=\"Dismiss\" class=\"Button\" content-desc=\"Dismiss tip\" clickable=\"true\" /></node><node id=\"321\" class=\"View\" /></node><node><node><node id=\"322\" class=\"ImageView\" /><node></node>";
        String chainOfThought2 = "Let's think step by step. Because it is in the mail list in Gmail app, scroll down will allow you to see older mails and there is a view that is scrollable";
        String example2 = itemQuestion("Gmail","Go up","com.google.android.gm",xml2, "ConversationListActivityGmail") + itemAnswer("<SCROLL>", "","UP" , chainOfThought2);


        String question = itemQuestion(appName,currentCommand,packageName,currentScreenXML,currentActName);

        return rule + chatHistory + example5 + example1 + example2 + question + "A:";

    }

    public static String textPrompt(String currentCommand, String currentScreenXML, String packageName, String appName, String chatHistory, String currentActName) {
        String rule = "Given a voice command to smartphone to type a piece of text. The prompt provides APP: the currently used app. CURRENT_ACTIVITY_NAME: current that user is on COMMAND: what user want to do in the app, XML: Current Android UI element on the screen as XML code. PACKAGE: package name of Android app. Identify the TEXT that user want to type into the text box.\n";


        String xml6 = "<node id=\"885\" class=\"ImageView\" content-desc=\"Back\" clickable=\"true\" /></node><node id=\"886\" text=\"Food, groceries, drinks, etc\" class=\"EditText\" clickable=\"true\" editable=\"true\" /></node></node></node><node><node><node id=\"887\" class=\"HorizontalScrollView\" scrollable=\"true\"><node><node><node id=\"888\" text=\"All\" class=\"TextView\" /></node><node><node id=\"889\" text=\"Restaurants\" class=\"TextView\" /></node><node><node id=\"890\" text=\"Grocery\" class=\"TextView\" /></node><node><node id=\"891\" text=\"Convenience\" class=\"TextView\" /></node><node><node id=\"892\" text=\"Alcohol\" class=\"TextView\" /></node><node><node id=\"893\" text=\"Pharmacy\" class=\"TextView\" /></node></node></node><node id=\"894\" class=\"View\" /></node></node><node><node id=\"895\" text=\"Recent searches\" class=\"TextView\" /></node></node>";
        String chainOfThought6 = "Let's think step by step. Because two condition qualified: it is an EditText search field on the screen and editable=\"true\" for that specific node where user can search for pizza on the screen";
        String example6 = itemQuestion("Uber Eats","I want to buy a pizza","com.ubercab.eats", xml6, ".RootActivity") + itemAnswer("<ENTER>", "","pizza", chainOfThought6);

        String question = itemQuestion(appName,currentCommand,packageName,currentScreenXML,currentActName);

        return rule + chatHistory + example6 + question + "A:";

    }

    public static String pressPrompt(String currentCommand, String currentTooltipCount, String currentScreenXML, String packageName, String appName, String chatHistory, String currentActName) {

        String rule = "Identify the TARGET element that user want to press. The prompt provides APP: the currently used app. CURRENT_ACTIVITY_NAME: current that user is on COMMAND: what user want to do in the app, XML: Current Android UI element on the screen as XML code. PACKAGE: package name of Android app. The TARGET must be a <node>, which has clickable=\"true\", or a number from 1 to " + currentTooltipCount + "\n";

        String chainOfThought4 = "Let's think step by step. On the current screen, there are no direct element to see the history, as it will most probably in library screen of Youtube so we need to get to the library screen first, which is <node id=\"334\" class=\"Button\" content-desc=\"Library\" clickable=\"true\" bounds=\"[0,66][1080,1874]\">";
        String xml4 = "<node id=\"293\" class=\"View\" /><node><node id=\"294\" class=\"ScrollView\" scrollable=\"true\"><node><node id=\"295\" class=\"ImageView\" content-desc=\"YouTube\" /></node><node id=\"334\" class=\"Button\" content-desc=\"Library\" clickable=\"true\" bounds=\"[0,66][1080,1874]\"><node id=\"335\" class=\"ImageView\" /><node id=\"336\" text=\"Library\" class=\"TextView\" /></node></node></node><node id=\"337\" class=\"View\" /></node>\n";
        String example4 = itemQuestionPress("Youtube","show me my history","com.google.android.youtube", xml4, "WatchWhileActivity", "2") + itemAnswer("<PRESS>","[0,66][1080,1874]","<node id=\"334\" class=\"Button\" content-desc=\"Library\" clickable=\"true\" bounds=\"[0,66][1080,1874]\">" , chainOfThought4);

        String chainOfThought3 = "Let's think step by step. On the screen, press upload button will allow you to upload a new file, which is <node id=\"2781\" text=\"Upload\" class=\"TextView\" clickable=\"true\" bounds=\"[234,432][1080,1200]\"/>";
        String xml3 = " <node><node id=\"2776\" class=\"View\" clickable=\"true\" /><node><node><node id=\"2777\" class=\"GridView\"><node><node id=\"2778\" class=\"ImageView\" /><node id=\"2779\" text=\"Folder\" class=\"TextView\" /></node><node><node id=\"2780\" class=\"ImageView\" /><node id=\"2781\" text=\"Upload\" class=\"TextView\" clickable=\"true\" bounds=\"[234,432][1080,1200]\"/></node><node><node id=\"2782\" class=\"ImageView\" /><node id=\"2783\" text=\"Scan\" class=\"TextView\" /></node><node><node id=\"2784\" class=\"ImageView\" /><node id=\"2785\" text=\"Google Docs\" class=\"TextView\" /></node><node><node id=\"2786\" class=\"ImageView\" /><node id=\"2787\" text=\"Google Sheets\" class=\"TextView\" /></node><node><node id=\"2788\" class=\"ImageView\" /><node id=\"2789\" text=\"Google Slides\" class=\"TextView\" /></node>";
        String example3 = itemQuestionPress("Google Drive","upload a new file","com.google.android.apps.docs",  xml3, "NavigationActivity", "4") + itemAnswer("<PRESS>","[234,432][1080,1200]","<node id=\"2781\" text=\"Upload\" class=\"TextView\" clickable=\"true\" bounds=\"[234,432][1080,1200]\"/>" , chainOfThought3);

        String chainOfThought1 = "Let's think step by step. On this screen that shows History in Youtube, the delete history features will most likely be in the additional menu option, where pressing <node id=\"21\" class=\"ImageView\" content-desc=\"More options\" bounds=\"[584,620][430,560] clickable=\"true\" tooltip-desc=\"More options\" /> will go to that screen";
        String xml1 = "<node id=\"14\" class=\"View\" /><node><node id=\"15\" class=\"ScrollView\" scrollable=\"true\"><node><node id=\"17\" text=\"History\" class=\"TextView\" /><node id=\"19\" class=\"View\" content-desc=\"Cast. Disconnected\" clickable=\"true\" /><node id=\"20\" class=\"Button\" content-desc=\"Search\" clickable=\"true\" tooltip-desc=\"Search\" /><node id=\"21\" class=\"ImageView\" content-desc=\"More options\" bounds=\"[584,620][430,560] clickable=\"true\" tooltip-desc=\"More options\" /><node id=\"22\" class=\"ProgressBar\" /></node><node id=\"23\" class=\"HorizontalScrollView\"><node><node id=\"24\" class=\"Button\" content-desc=\"Home\" clickable=\"true\"></node><node id=\"27\" class=\"Button\" content-desc=\"Shorts\" clickable=\"true\"><node id=\"28\" class=\"ImageView\" /><node id=\"29\" text=\"Shorts\" class=\"TextView\" /><node id=\"38\" class=\"View\" /></node>. \n";
        String example1 = itemQuestionPress("Youtube","delete my history","com.google.android.youtube",  xml1, "WatchWhileActivity", "5" ) + itemAnswer("<PRESS>","[584,620][430,560]", "<node id=\"21\" class=\"ImageView\" content-desc=\"More options\" bounds=\"[584,620][430,560] clickable=\"true\" tooltip-desc=\"More options\" />" , chainOfThought1);

        String xml2 = "<node id=\"23\" bounds=\"[120,220][450,510]\" class=\"Button\" content-desc=\"Settings\"><node id=\"34\" class=\"ImageView\" /><node id=\"1675\" class=\"ImageView\" content-desc=\"Instagram from Meta\" /></node><node><node id=\"1676\" class=\"Button\" content-desc=\"Profile photo\"><node id=\"1677\" class=\"ImageView\" /></node><node id=\"1679\" class=\"View\" content-desc=\" \" /><node><node id=\"1680\" class=\"Button\" content-desc=\"Log in\" clickable=\"true\"><node id=\"1681\" text=\"Log in\" class=\"View\" content-desc=\"Log in\" /></node></node></node><node><node><node id=\"1682\" class=\"Button\" content-desc=\"Log into another account\" clickable=\"true\"><node id=\"1683\" text=\"Log into another account\" class=\"View\" content-desc=\"Log into another account\" /></node>\n";
        String chainOfThought2 = "Let's think step by step. Because there are no direct action to log out on the current screen, log out feature is included in the Instagram setting page and pressing the node <node id=\"1673\" bounds=\"[120,220][450,510]\" class=\"Button\" content-desc=\"Settings\"> can bring you to that page";
        String example2 = itemQuestionPress("Instagram","log out of my account","com.instagram.android",xml2, "BloksSignedOutFragmentActivity", "11") + itemAnswer("<PRESS>","[120,220][450,510]", "<node id=\"1673\" bounds=\"[120,220][450,510]\" class=\"Button\" content-desc=\"Settings\">" , chainOfThought2);

        String xml7 = "<node><node id=\"25\" class=\"View\"><node><node id=\"27\" class=\"View\"><node id=\"28\" class=\"View\"><node id=\"29\" class=\"View\" content-desc=\"Search Movies &amp; TV Shows\" clickable=\"true\" bounds=\"[0,88][1080,198]\" /><node id=\"30\" class=\"View\">";
        String chainOfThought7 = "Let's think step by step. Although there seems like a View with Search in the content, it is not an EditText, hence we cannot directly put in the text but we have to press it first, which is <node id=\"29\" class=\"View\" content-desc=\"Search Movies &amp; TV Shows\" clickable=\"true\" bounds=\"[0,88][1080,198]\" />";
        String example7 = itemQuestionPress("top.mmshow.cetus","I want to search for Avatars","top.mmshow.cetus",xml7, ".MainActivity", "2") + itemAnswer("<PRESS>", "[0,88][1080,198]","<node id=\"29\" class=\"View\" content-desc=\"Search Movies &amp; TV Shows\" clickable=\"true\" bounds=\"[0,88][1080,198]\" />", chainOfThought7);

        String xml5 = "<node><node id=\"25\" class=\"View\"><node><node id=\"27\" class=\"View\"><node id=\"28\" class=\"View\"><node id=\"29\" class=\"View\" content-desc=\"Search Movies &amp; TV Shows\" clickable=\"true\" bounds=\"[0,88][1080,198]\" /><node id=\"30\" class=\"View\">";
        String chainOfThought5 = "Let's think step by step, because user want to press 2, the number should refer to a tooltip on the screen since 2 < 3.";
        String example5 = itemQuestionPress("top.mmshow.cetus","Press 2","top.mmshow.cetus",xml5, ".MainActivity", "3") + itemAnswer("<PRESS>", "","2", chainOfThought5);

        String question = itemQuestionPress(appName,currentCommand,packageName,currentScreenXML,currentActName, currentTooltipCount);

//example5
        return rule + example1 + example3 + example4 + example7 + question + "A:";
    }

    static private String itemQuestionPress(String appName, String command, String packageName, String currentScreenXML, String actName, String numOfToolTips){
        return "Q: APP: "+ appName + ". CURRENT_ACTIVITY_NAME: " + actName +". COMMAND: "+command + ". XML: " + currentScreenXML + ". PACKAGE: "+ packageName + "TOOLTIP_COUNT" + numOfToolTips +".\n";
    }

    public static String deeplinkPrompt(String currentCommand, String appName, String deeplinks) {
        return "In " +appName+ " app, given an app feature, choose the best match from the following activities and links: " + deeplinks +  ". Keep in mind the keyword often appears in the item. \n" + currentCommand;
    }

    public static String appPrompt(String currentCommand, String appNames) {
        return "User says that he wants to " + currentCommand + ". Which APP NAME in this list is user referring to: " + appNames +". In addition, detect the FEATURE that user wants to open from his command. The output format should be APP NAME &&& FEATURE";
    }


    static private String itemScreenSummarise(String appName, String packageName, String currentScreenXML, String actName){
        return "Q: APP: "+ appName + ". CURRENT_ACTIVITY_NAME: " + actName + ". XML: " + currentScreenXML + ". PACKAGE: "+ packageName +".\n";
    }


    public static String screenSummarise(String currentScreenXML, String packageName, String appName, String currentActName) {
        String rule = "Input current screen information, including APP: the currently used app, CURRENT_ACTIVITY_NAME: current that user is on, XML: Current Android UI element on the screen as XML code, PACKAGE: package name of Android app. Output the summarisation (description) for this screen.\n";

        String currentScreenXML1 = "<node id=\"3656\" class=\"View\" /><node id=\"3657\" class=\"TextView\" text=\"Track\"  /><node><node id=\"3659\" class=\"Button\" text=\"ADD\" clickable=\"true\" bounds=\"[904,77][1036,209]\" /></node><node/><node id=\"3661\" class=\"ImageView\" resource-name=\"parcelImage\" clickable=\"true\" bounds=\"[333,550][746,963]\" /><node id=\"3662\" class=\"TextView\" text=\"Don’t miss a delivery – get your parcels &#10;where y\" bounds=\"[66,1382][1014,1478]\" /><node id=\"3663\" class=\"Button\" text=\"Log in\" clickable=\"true\" bounds=\"[66,1544][1014,1654]\" /><node id=\"3664\" class=\"Button\" text=\"Sign up for free\" clickable=\"true\" bounds=\"[66,1676][1014,1786]\" /><node id=\"3665\" class=\"ImageView\" resource-name=\"navigation_bar_item_icon_view\" /><node id=\"3666\" class=\"TextView\" text=\"Track\" \" /><node id=\"3668\" class=\"TextView\" text=\"Find us\" bounds=\"[348,1965][462,2009]\" /><node id=\"3669\" class=\"ImageView\" bounds=\"[642,1896][708,1962]\" /><node id=\"3670\" class=\"TextView\" text=\"Account\" /><node id=\"3672\" class=\"TextView\" text=\"More\" /><node id=\"3673\" class=\"View\" /><node /></node>";
        String example1 = itemScreenSummarise("AusPost","au.com.auspost.android", currentScreenXML1,".feature.track.TrackActivity") + "A: This is the screen for tracking orders in AusPost app, include a button to add new track item. In addition, user can log in and sign up from this screen.\n";
        String currentScreenXML2 = "<node id=\"3249\" class=\"TextView\" text=\"Uber One&#10;A$9.99/mo 2 weeks free\" /><node id=\"3253\" class=\"TextView\" text=\"$0 Delivery Fee on eligible orders $20+ (Service \" /><node id=\"3257\" class=\"TextView\" text=\"Member savings on rides (at least 10% off) and to\" /><node id=\"3261\" class=\"TextView\" text=\"Exclusive Member Perks like promotions and experi\" /><node id=\"3265\" class=\"TextView\" text=\"$5 credit if the Latest Arrival estimate on your \" /><node id=\"3269\" class=\"TextView\" text=\"Cancel anytime without additional fees\" bounds=\"[176,1354][1036,1409]\" /><node id=\"3271\" class=\"TextView\" text=\"Members save around  A$30 every month\" /><node id=\"1\" class=\"TextView\" text=\"Based on average monthly savings of members in yo\" /><node id=\"2\" class=\"Button\" text=\"Join Uber One\" clickable=\"true\" bounds=\"[44,1676][1036,1830]\" />node id=\"3275\" class=\"TextView\" text=\"Home\" /><node id=\"4\" class=\"TextView\" text=\"Browse\"/><node id=\"3278\" class=\"TextView\" text=\"2\" /><node id=\"3282\" class=\"TextView\" text=\"Account\" /><node id=\"3284\" class=\"TextView\" text=\"Uber One\" /></node>";
        String example2 = itemScreenSummarise("Uber Eats","com.ubercab.eats", currentScreenXML2,".core.activity.RootActivity") + "A: This is the screen for Uber One in Uber Eats, include a button to join Uber One.\n";
        String currentScreenXML3 = "<node id=\"3757\" class=\"ScrollView\" resource-name=\"main_coordinator\" scrollable=\"true\" bounds=\"[0,66][1080,1808]\"><node><node id=\"3758\" class=\"ImageButton\" tooltip-desc=\"Show navigation menu\" content-desc=\"Show navigation menu\" clickable=\"true\" bounds=\"[0,77][154,231]\" /><node id=\"3759\" class=\"ImageView\" /><node id=\"3760\" class=\"TextView\" text=\"Files\" /><node id=\"3762\" class=\"ImageView\" clickable=\"true\" bounds=\"[0,424][1080,974]\" /><node id=\"3763\" class=\"TextView\" text=\"Nearby Share\" /><node id=\"3764\" class=\"Button\" text=\"Send\" clickable=\"true\" /><node id=\"3765\" class=\"Button\" text=\"Receive\" clickable=\"true\" bounds=\"[525,1106][818,1238]\" /><node id=\"3766\" class=\"ImageView\" /><node id=\"3767\" class=\"TextView\" text=\"Using Nearby Share\" /></node><node id=\"3769\" class=\"ImageView\" resource-name=\"navigation_bar_item_icon_view\" bounds=\"[147,1852][213,1918]\" /><node id=\"3770\" class=\"TextView\" text=\"Clean\" bounds=\"[134,1947][225,1994]\" /><node id=\"3773\" class=\"TextView\" text=\"Browse\" /><node id=\"3776\" class=\"TextView\" text=\"Nearby Share\" /><node /></node>";
        String example3 = itemScreenSummarise("Files", "com.google.android.apps.nbu.files", currentScreenXML3, ".home.HomeActivity") + "A: This is the nearby share page in Files app, with send and receive button on the screen to share or receive files.\n";

        return rule + example1 + example2 + example3 + itemScreenSummarise(appName,packageName,currentScreenXML,currentActName) + "A: ";
    }

    static String screenDescriptionBuilder(String command, ArrayList<String> screenDescriptions) {
        return "Q: COMMAND:" + command + ", SCREENS: " + screenDescriptions + "\n";
    }

    public static String requestScreenDescriptionMatching(String command, ArrayList<String> screenDescriptions) {
        String rule = "Input contains COMMAND: user request for an action that user want to do in the mobile app, SCREENS: List of descriptions for each screen that the app has. Output the best match from the list of description in SCREENS. If you are unsure about the answer, respond \"no match\".\n";

        ArrayList<String> exampleScreenDescription = new ArrayList<>( Arrays.asList("This is the screen for seeing history videos in Youtube, include a button to delete items, view all history. In addition, user can see any video from this screen.",
                "This is premium screen in Youtube, include a button to become premium member, and navigate back",
                "This is the search screen in Youtube, include a textfield to insert search term, a button to search and list of recently searched items.",
                "This is the login screen in Youtube, include buttons to login, sign up and forgot password with the text field to enter email."));
        String example = screenDescriptionBuilder("I want to search for classical music", exampleScreenDescription) + " A: ";
        return rule + example + screenDescriptionBuilder(command,screenDescriptions) + "A: ";
    }

}
