package com.research.activityinvoker.model;

public class RequestBody {
    String model = "text-davinci-003";

    String prompt;
    double temperature = 0.5;
    int max_tokens = 200;

    public RequestBody(String prompt) {
        this.prompt = prompt;
    }
}
