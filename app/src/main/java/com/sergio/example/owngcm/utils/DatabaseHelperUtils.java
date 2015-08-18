package com.sergio.example.owngcm.utils;

import android.content.ContentValues;
import android.content.Context;

import com.example.syp.myapplication.backend.domain.conference.model.Conference;
import com.sergio.example.owngcm.model.DecoratedConference;
import com.sergio.example.owngcm.provider.RouteContract;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import static com.sergio.example.owngcm.utils.LogUtils.makeLogTag;

/**
 * Created by syp on 15/08/15.
 */
public class DatabaseHelperUtils {

    public final String LOG_TAG =  makeLogTag(DatabaseHelperUtils.class);

    public static int addRoutesToSQLite (Context context, List<DecoratedConference> decoratedConferences){

        // Insert the new weather information into the database
        Vector<ContentValues> cVVector = new Vector<>(decoratedConferences.size());
        ContentValues[] cvArray = new ContentValues[cVVector.size()];

        Iterator<DecoratedConference> decoratedConferenceIterator = decoratedConferences.iterator();

        while (decoratedConferenceIterator.hasNext()){

            // Create a new map of values, where column names are the keys
            ContentValues routeValues = new ContentValues();

            //Cojo Decorate conference, register + conference
            DecoratedConference decoratedConference = decoratedConferenceIterator.next();
            // ¿Registrado?
            routeValues.put(RouteContract.RouteEntry.COLUMN_REGISTERED, decoratedConference.isRegistered());

            // Cojo los datos de la conferencia
            Conference conference = decoratedConference.getConference();
            routeValues.put(RouteContract.RouteEntry._ID, conference.getId());
            routeValues.put(RouteContract.RouteEntry.COLUMN_NAME_ROUTE, conference.getName());
            routeValues.put(RouteContract.RouteEntry.COLUMN_DESCRIPTION,conference.getDescription() );
            routeValues.put(RouteContract.RouteEntry.COLUMN_TOPICS, conference.getTopics().toString());
            routeValues.put(RouteContract.RouteEntry.COLUMN_CITY_NAME_INIT, conference.getCity());
            routeValues.put(RouteContract.RouteEntry.COLUMN_START_DATE, conference.getStartDate().toString());
            routeValues.put(RouteContract.RouteEntry.COLUMN_MAX_ATTENDEES, conference.getMaxAttendees());
            routeValues.put(RouteContract.RouteEntry.COLUMN_SEATS_AVAILABLE, conference.getSeatsAvailable());
            routeValues.put(RouteContract.RouteEntry.COLUMN_URL_CHAT_COVER, conference.getPhotoUrlRouteCover());
            routeValues.put(RouteContract.RouteEntry.COLUMN_TOPIC_GCM, conference.getTopicGcm());
            routeValues.put(RouteContract.RouteEntry.COLUMN_WEBSAFE_KEY, conference.getWebsafeKey());
            routeValues.put(RouteContract.RouteEntry.COLUMN_ORGANIZER_DISPLAY_NAME, conference.getOrganizerDisplayName());

            cVVector.add(routeValues);
        }

        /** Insert weather data into database */
        return insertWeatherIntoDatabase(context, cVVector);
    }

    public static int insertWeatherIntoDatabase(Context context, Vector<ContentValues> CVVector) {
        int rowsInserted = 0;
        if (CVVector.size() > 0) {
            ContentValues[] contentValuesArray = new ContentValues[CVVector.size()];
            CVVector.toArray(contentValuesArray);

            rowsInserted = context.getContentResolver().bulkInsert(RouteContract.RouteEntry.CONTENT_URI, contentValuesArray);

        }
        return rowsInserted;
    }
}
