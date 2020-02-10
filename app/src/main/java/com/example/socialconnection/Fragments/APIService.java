package com.example.socialconnection.Fragments;

import com.example.socialconnection.Notifications.MyResponse;
import com.example.socialconnection.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
@Headers({
        "Content-Type:application/json",
        "Authorization:key=AAAA733WKxM:APA91bFhvf9L1efIrTG8Z4lVU08sQhpbNDqPucLleksqTZarqiCwRRtY7_PtHqit2FJDlAg8Zezw-SBcFWc9gAhbjlqq8N8NS-XWP4tgk3qrUCpoqKlzT3qC_pyX0sZQnHH7vFnZrwN2"

})
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
