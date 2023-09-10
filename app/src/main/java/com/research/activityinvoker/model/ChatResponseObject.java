package com.research.activityinvoker.model;

import java.util.ArrayList;

public class ChatResponseObject {
    public ArrayList<Choice> choices;

    public ChatResponseObject(ArrayList<Choice> choices) {
        this.choices = choices;
    }


     public class Choice {

         public Message message;

         public Choice(Message message) {
             this.message = message;
         }

         public class Message {
            public String role;
            public String content;

            public Message(String role, String content) {
                this.role = role;
                this.content = content;
            }
        }
    }
}
