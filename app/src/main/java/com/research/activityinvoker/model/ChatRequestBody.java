package com.research.activityinvoker.model;

public class ChatRequestBody {
     String model =  "gpt-3.5-turbo";
     Message[] messages;
     double temperature = 0.5;


    public ChatRequestBody(String message) {
        Message message0 = new Message("system", "You are guiding me to an Android App, you give me action on the screen based on what I want to do. You reply with one action which is brief, to-the-point answer.");
        Message message1 = new Message(message);

        this.messages = new Message[]{message0,message1};
    }

    class Message {
        String role = "user";
        String content;

        public Message(String content) {
            this.content = content;
        }

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
