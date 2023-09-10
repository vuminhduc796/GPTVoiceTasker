package com.research.activityinvoker.services;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class WordChecker {
    public static boolean check_for_word(String word) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(
                    "/usr/share/dict/american-english"));
            String str;
            while ((str = in.readLine()) != null) {
                Log.d("dddddd", str);
                if (str.contains(word)) {
                    return true;
                }
            }
            in.close();
        } catch (IOException e) {
        }

        return false;
    }
}
