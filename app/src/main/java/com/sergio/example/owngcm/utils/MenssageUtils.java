package com.sergio.example.owngcm.utils;

import android.os.Bundle;

import com.sergio.example.owngcm.model.Message;

/**
 * Utility Class
 */
public class MenssageUtils {

    /**
     * Compose message "User => message.... \n" from bundle.
     * @param bundle bundle Message
     * @return compose message User => mesagge....
     */
    public static String getComposeMessage(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        String string;
        //TODO: futuras implemenaciones para el resto de parametros del message
        String user = bundle.getString(Message.PARAM_USER);
        String message = bundle.getString(Message.PARAM_MESSAGE);
        string = user + "\n => " + message + "\n";
        return string;
    }
}
