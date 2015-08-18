package com.example.syp.myapplication.backend;

import com.google.api.server.spi.Constant;

/**
 * Contains the client IDs and scopes for allowed clients consuming the conference API.
 */
public class Constants {
    public static final String WEB_CLIENT_ID = "916882531293-0ka738h5fv4mu1ht22unp43hfeb19q38.apps.googleusercontent.com";
    public static final String ANDROID_CLIENT_ID_DEBUG = "916882531293-4d29ivijks1gc0asc1j79q24pmp29luq.apps.googleusercontent.com";
    public static final String ANDROID_CLIENT_ID_RELEASE = "916882531293-vcs7vtjue6hp39tfk2o74o03dla9fbne.apps.googleusercontent.com";
    public static final String IOS_CLIENT_ID = "replace this with your iOS client ID";
    public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;
    public static final String EMAIL_SCOPE = Constant.API_EMAIL_SCOPE;
    public static final String API_EXPLORER_CLIENT_ID = Constant.API_EXPLORER_CLIENT_ID;

    public static final String MEMCACHE_ANNOUNCEMENTS_KEY = "RECENT_ANNOUNCEMENTS";
}