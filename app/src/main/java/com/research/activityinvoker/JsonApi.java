package com.research.activityinvoker;

import com.research.activityinvoker.model.ChatRequestBody;
import com.research.activityinvoker.model.ChatResponseObject;
import com.research.activityinvoker.model.RequestBody;
import com.research.activityinvoker.model.ResponseObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.OPTIONS;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface JsonApi {


    @Headers({"Content-Type: application/json", "Authorization: Bearer YOUR_API_KEY"})
    @POST("v1/completions")
    Call<ResponseObject> getData(@Body RequestBody requestBody);

    @Headers({"Content-Type: application/json", "Authorization: Bearer YOUR_API_KEY"})
    @POST("v1/chat/completions")
    Call<ChatResponseObject> getDataChat(@Body ChatRequestBody requestBody);


}
