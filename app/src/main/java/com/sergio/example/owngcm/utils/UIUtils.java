package com.sergio.example.owngcm.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.text.format.DateUtils;
import android.view.View;

import com.sergio.example.owngcm.R;

import java.util.Calendar;

/**
 * Created by syp on 14/08/15.
 */
public class UIUtils {

    /**
     * Display custom SnackBar in CoordinatorLayout Activity
     * @param activity activity to find snackbar
     * @param message Message will be display.
     * @param messageAction Message action or null
     * @param mOnClickListener Funcion action or null
     */
    public static void showSnackBar(Activity activity, String message, String messageAction, View.OnClickListener mOnClickListener) {
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) activity.findViewById(R.id.snackbarlocation);
        Snackbar snackbar =  Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE);
        if (messageAction == null) {
            snackbar.setDuration(Snackbar.LENGTH_SHORT);
        } else {
            snackbar.setAction(messageAction, mOnClickListener);
            snackbar.setActionTextColor(Color.parseColor("#9c27b0"));
        }
        snackbar.show();
    }

    /**
     * Displays error dialog when a network error occurs. Exits application when user confirms
     * dialog.
     *
     * @param context context of application running
     */
    public static void displayNetworkErrorMessage(Context context) {
        new AlertDialog.Builder(
                context).setTitle(R.string.api_error_title)
                .setMessage(R.string.api_error_message)
                .setCancelable(false)
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                System.exit(0);
                            }
                        }
                ).create().show();
    }

    /**
     * Returns the date of a conference.
     *
     * @param context context of running application
     * @param startDate data conference date
     * @return date in String
     */
    public static String getConferenceDate(Context context, Long startDate) {

        StringBuffer sb = new StringBuffer();
        if(null != startDate) {
            sb.append(getFormattedDate(context, startDate));
        }

        // TODO. TRUCO PARA CONCATENAR DATOS PARA MOSTRAR POR PANTALLA
        /* original

        if (null != conference.getStartDate() && null != conference.getEndDate()) {
            sb.append(getFormattedDateRange(context, conference.getStartDate(),
                    conference.getEndDate()));
        } else if (null != conference.getStartDate()) {
            sb.append(getFormattedDate(context, conference.getStartDate()));
        }
        */
        return sb.toString();
    }

    /**
     * Returns a user-friendly localized date.
     *
     * @param context context of running application
     * @param dateLong date in string
     * @return
     */
    public static String getFormattedDate(Context context, Long dateLong) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateLong);
        return DateUtils
                .formatDateTime(context, cal.getTimeInMillis(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
    }
}
