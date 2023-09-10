package com.research.activityinvoker.model;

import java.util.ArrayList;

public class ChatHistory {
    ArrayList<Message> messages;

    public ChatHistory() {
        messages = new ArrayList<>();
    }
    public class Message {
        String userPrompt;
        String response;

        public Message(String userPrompt, String response) {
            this.userPrompt = userPrompt;
            this.response = response;
        }
    }

    public void addMessage(String userPrompt, String response) {
        Message newMessage = new Message(userPrompt,response);
        messages.add(newMessage);
        if(messages.size() > 3) {
            messages.remove(0);
        }
    }

    public void resetChatMessage() {
        messages.clear();
    }

    public String getChatHistory() {

       if (messages.isEmpty()) {
           return "";
       }

       StringBuilder output = new StringBuilder("I have done the following steps, do not repeat: \n");

       for(Message message: messages) {
           String messageContent = "assistant: " + message.response + "\n";
           output.append(messageContent);
       }

       return output.toString();
    }

}
