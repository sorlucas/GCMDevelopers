package com.sergio.example.owngcm;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

/**
 * Created by syp on 13/08/15.
 */
public class Config {

    // Config Endpoint   //
    // Your WEB CLIENT ID from the API Access screen of the Developer Console for your project.
    // This is NOT the Android client id from that screen.
    public static final String WEB_CLIENT_ID = "916882531293-0ka738h5fv4mu1ht22unp43hfeb19q38.apps.googleusercontent.com";
    // The audience is defined by the web client id, not the Android client id.
    public static final String AUDIENCE = "server:client_id:" + WEB_CLIENT_ID;
    // Class instance of the JSON factory.
    public static final JsonFactory JSON_FACTORY = new AndroidJsonFactory();
    // Class instance of the HTTP transport.
    public static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

    // Email Address from Service account
    public static final String SERVICE_ACCOUNT_EMAIL = "916882531293-2j5uso8c086adba1ite0ir8lpsojtbgc@developer.gserviceaccount.com";

    // Name buckets
    public static final String BUCKET_PHOTOS_COVER = "photos_cover_chat";

}
