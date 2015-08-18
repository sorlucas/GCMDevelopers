package com.sergio.example.owngcm.utils;

/**
 * Created by syp on 17/08/15.
 */
public class StringUtils {


    /*
     * Get the display name from the user's email. For example, if the email is
     * lemoncake@example.com, then the display name becomes "lemoncake."
     */
    public static String extractDefaultDisplayNameFromEmail(String email) {
        return email == null ? null : email.substring(0, email.indexOf("@"));
    }
}
