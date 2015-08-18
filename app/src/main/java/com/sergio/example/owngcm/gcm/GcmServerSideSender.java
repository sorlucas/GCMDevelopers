/*
Copyright 2015 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.sergio.example.owngcm.gcm;

import android.util.Log;

import com.sergio.example.owngcm.model.Message;
import com.sergio.example.owngcm.service.LoggingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import static com.sergio.example.owngcm.gcm.HttpRequest.CONTENT_TYPE_JSON;
import static com.sergio.example.owngcm.gcm.HttpRequest.HEADER_AUTHORIZATION;
import static com.sergio.example.owngcm.gcm.HttpRequest.HEADER_CONTENT_TYPE;

import static com.sergio.example.owngcm.model.Message.PARAM_DELAY_WHILE_IDLE;
import static com.sergio.example.owngcm.model.Message.PARAM_DRY_RUN;
import static com.sergio.example.owngcm.model.Message.PARAM_JSON_NOTIFICATION_PARAMS;
import static com.sergio.example.owngcm.model.Message.PARAM_JSON_PAYLOAD;
import static com.sergio.example.owngcm.model.Message.PARAM_TIME_TO_LIVE;
import static com.sergio.example.owngcm.model.Message.PARAM_USER;
import static com.sergio.example.owngcm.model.Message.PARAM_MESSAGE;
/**
 * This class is used to send GCM downstream messages in the same way a server would.
 */
public class GcmServerSideSender {

    private static final String GCM_SEND_ENDPOINT = "https://gcm-http.googleapis.com/gcm/send";

    public static final String PARAM_TO = "to";

    private final String key;
    private final LoggingService.Logger logger;

    /**
     * @param key    The API key used to authorize calls to Google
     */
    public GcmServerSideSender(String key, LoggingService.Logger logger) {
        this.key = key;
        this.logger = logger;
    }


    /**
     * Send a downstream message via HTTP JSON.
     *
     * @param destination the registration id of the recipient app.
     * @param message        the message to be sent
     * @throws IOException
     */
    public void sendHttpJsonDownstreamMessage(String destination, Message message) throws IOException {

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put(PARAM_TO, destination);
            jsonBody.putOpt(PARAM_USER, message.getUserName());
            jsonBody.putOpt(PARAM_MESSAGE, message.getUserMessage());
            jsonBody.putOpt(PARAM_TIME_TO_LIVE, message.getTimeToLive());
            jsonBody.putOpt(PARAM_DELAY_WHILE_IDLE, message.isDelayWhileIdle());
            jsonBody.putOpt(PARAM_DRY_RUN, message.isDryRun());
            if (message.getData().size() > 0) {
                JSONObject jsonPayload = new JSONObject(message.getData());
                jsonBody.put(PARAM_JSON_PAYLOAD, jsonPayload);
            }
            if (message.getNotificationParams().size() > 0) {
                JSONObject jsonNotificationParams = new JSONObject(message.getNotificationParams());
                jsonBody.put(PARAM_JSON_NOTIFICATION_PARAMS, jsonNotificationParams);
            }
        } catch (JSONException e) {
            logger.log(Log.ERROR, "Failed to build JSON body");
            Log.e("GcmServerSideSender", e.toString());
            return;
        }

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
        httpRequest.setHeader(HEADER_AUTHORIZATION, "key=" + key);
        httpRequest.doPost(GCM_SEND_ENDPOINT, jsonBody.toString());

        if (httpRequest.getResponseCode() != 200) {
            throw new IOException("Invalid request."
                    + " status: " + httpRequest.getResponseCode()
                    + " response: " + httpRequest.getResponseBody());
        }

        Map<String, String> userData = message.getData();
        //TODO: Alguna implementacion para la response, al enviar un mensaje GCM
        JSONObject jsonResponse;
        try {
            jsonResponse = new JSONObject(httpRequest.getResponseBody());
        } catch (JSONException e) {
            logger.log(Log.ERROR, "Failed to parse server response:\n" + httpRequest.getResponseBody());
        }
    }

    /**
     * Adds a new parameter to the HTTP POST body without doing any encoding.
     *
     * @param body  HTTP POST body.
     * @param name  parameter's name.
     * @param value parameter's value.
     */
    private static void addOptParameter(StringBuilder body, String name, Object value) {
        if (value != null) {
            String encodedValue = value.toString();
            if (value instanceof Boolean) {
                encodedValue = ((Boolean) value) ? "1" : "0";
            }
            body.append('&').append(name).append('=').append(encodedValue);
        }
    }
}