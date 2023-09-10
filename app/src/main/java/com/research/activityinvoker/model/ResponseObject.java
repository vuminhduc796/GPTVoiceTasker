package com.research.activityinvoker.model;

import java.util.ArrayList;

public class ResponseObject {

    public ArrayList<Choice> choices;

    public ResponseObject(ArrayList<Choice> choices) {
        this.choices = choices;
    }


        public class Choice {
            public String text;
            public double index;

            public Choice(String text, double index) {
                this.text = text;
                this.index = index;
            }
        }
    }



