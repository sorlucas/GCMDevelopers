package com.sergio.example.owngcm.utils;

import android.content.Context;
import android.util.Log;

import com.example.syp.myapplication.backend.domain.conference.model.Conference;
import com.example.syp.myapplication.backend.domain.conference.model.ConferenceCollection;
import com.example.syp.myapplication.backend.domain.conference.model.ConferenceForm;
import com.example.syp.myapplication.backend.domain.conference.model.ConferenceQueryForm;
import com.example.syp.myapplication.backend.domain.conference.model.Profile;
import com.example.syp.myapplication.backend.domain.conference.model.WrappedBoolean;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.sergio.example.owngcm.Config;
import com.sergio.example.owngcm.R;
import com.sergio.example.owngcm.model.DecoratedConference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A utility class for communication with the Cloud Endpoint.
 */
public class ConferenceUtils {

    private final static String TAG = "ConferenceUtils";

    private static com.example.syp.myapplication.backend.domain.conference.Conference sApiServiceHandler;

    public static void build(Context context) {
        String email = AccountUtils.getActiveAccountName(context);
        sApiServiceHandler = buildServiceHandler(context, email);
    }

    /**
     * Returns the user {@link Profile}. Can be used to find out what conferences user is registered for.
     *
     * @return profile user
     * @throws ConferenceException
     */
    public static Profile getProfile() throws ConferenceException, IOException {
        if (null == sApiServiceHandler) {
            Log.e(TAG, "getProfile(): no service handler was built");
            throw new ConferenceException();
        }

        com.example.syp.myapplication.backend.domain.conference.Conference.GetProfile getProfile =
                sApiServiceHandler.getProfile();
        return getProfile.execute();
    }

    public static Conference createConference(ConferenceForm conferenceForm)
            throws ConferenceException, IOException {
        if (null == sApiServiceHandler) {
            Log.e(TAG, "createConference(): no service handler was built");
            throw new ConferenceException();
        }
        com.example.syp.myapplication.backend.domain.conference.Conference.CreateConference createConference =
                sApiServiceHandler.createConference(conferenceForm);

        return createConference.execute();
    }

    /**
     * Returns a list of {@link com.sergio.example.owngcm.model.DecoratedConference}s.
     * This list includes information about what {@link Conference}s
     * user has registered for.
     *
     * @return List of DecoratedConferences
     * @throws ConferenceException
     * @see <code>getProfile</code>
     */
    public static List<DecoratedConference> getConferences(ConferenceQueryForm conferenceQueryForm)
            throws ConferenceException, IOException {
        if (null == sApiServiceHandler) {
            Log.e(TAG, "getConferences(): no service handler was built");
            throw new ConferenceException();
        }

        com.example.syp.myapplication.backend.domain.conference.Conference.QueryConferences
                queryConferences = sApiServiceHandler.queryConferences(conferenceQueryForm);
        ConferenceCollection conferenceCollection = queryConferences.execute();

        if (conferenceCollection != null && conferenceCollection.getItems() != null) {
            List<Conference> conferences = conferenceCollection.getItems();
            List<DecoratedConference> decoratedList = null;
            if (null == conferences || conferences.isEmpty()) {
                return decoratedList;
            }
            decoratedList = new ArrayList<>();
            Profile profile = getProfile();
            List<String> registeredConfKeys = null;
            if (null != profile) {
                registeredConfKeys = profile.getConferenceKeysToAttend();
            }
            if (null == registeredConfKeys) {
                registeredConfKeys = new ArrayList<>();
            }
            for (Conference conference : conferences) {
                DecoratedConference decorated = new DecoratedConference(conference,
                        registeredConfKeys.contains(conference.getWebsafeKey()));
                decoratedList.add(decorated);
            }
            return decoratedList;
        }
        return null;
    }

    /**
     * Returns a list of {@link com.sergio.example.owngcm.model.DecoratedConference}s that user created
     * This list includes information about what {@link Conference}s
     * user has registered for.
     *
     * @return List of DecoratedConferences
     * @throws ConferenceException
     * @see <code>getProfile</code>
     */
    public static List<DecoratedConference> getConferencesCreated()
            throws ConferenceException, IOException {
        if (null == sApiServiceHandler) {
            Log.e(TAG, "getConferences(): no service handler was built");
            throw new ConferenceException();
        }

        com.example.syp.myapplication.backend.domain.conference.Conference.GetConferencesCreated
                queryConferences = sApiServiceHandler.getConferencesCreated();
        ConferenceCollection conferenceCollection = queryConferences.execute();

        if (conferenceCollection != null && conferenceCollection.getItems() != null) {
            List<Conference> conferences = conferenceCollection.getItems();
            List<DecoratedConference> decoratedList = null;
            if (null == conferences || conferences.isEmpty()) {
                return decoratedList;
            }
            decoratedList = new ArrayList<>();
            Profile profile = getProfile();
            List<String> registeredConfKeys = null;
            if (null != profile) {
                registeredConfKeys = profile.getConferenceKeysToAttend();
            }
            if (null == registeredConfKeys) {
                registeredConfKeys = new ArrayList<>();
            }
            for (Conference conference : conferences) {
                DecoratedConference decorated = new DecoratedConference(conference,
                        registeredConfKeys.contains(conference.getWebsafeKey()));
                decoratedList.add(decorated);
            }
            return decoratedList;
        }
        return null;
    }

    /**
     * Registers user for a {@link com.example.syp.myapplication.backend.domain.conference.Conference}
     *
     * @param websafeKey
     * @return
     * @throws ConferenceException
     */
    public static boolean registerForConference(String websafeKey)
            throws ConferenceException, IOException {
        if (null == sApiServiceHandler) {
            Log.e(TAG, "registerForConference(): no service handler was built");
            throw new ConferenceException();
        }

        com.example.syp.myapplication.backend.domain.conference.Conference.RegisterForConference
                registerForConference = sApiServiceHandler.registerForConference(
                websafeKey);
        WrappedBoolean result = registerForConference.execute();
        return result.getResult();
    }

    /**
     * Unregisters user from a {@link com.example.syp.myapplication.backend.domain.conference.Conference}.
     *
     * @param websafeKey
     * @return
     * @throws ConferenceException
     */
    public static boolean unregisterFromConference(String websafeKey)
            throws ConferenceException, IOException {
        if (null == sApiServiceHandler) {
            Log.e(TAG, "unregisterFromConference(): no service handler was built");
            throw new ConferenceException();
        }

        com.example.syp.myapplication.backend.domain.conference.Conference.UnregisterFromConference
                unregisterFromConference = sApiServiceHandler.unregisterFromConference(
                websafeKey);
        WrappedBoolean result = unregisterFromConference.execute();
        return result.getResult();
    }

    public static List<DecoratedConference> getConferencesToAttend()
            throws ConferenceException, IOException {
        if (null == sApiServiceHandler) {
            Log.e(TAG, "unregisterFromConference(): no service handler was built");
            throw new ConferenceException();
        }

        com.example.syp.myapplication.backend.domain.conference.Conference.GetConferencesToAttend
                conferencesToAttend = sApiServiceHandler.getConferencesToAttend();
        ConferenceCollection conferenceCollection = conferencesToAttend.execute();

        if (conferenceCollection != null && conferenceCollection.getItems() != null) {
            List<Conference> conferences = conferenceCollection.getItems();
            List<DecoratedConference> decoratedList = null;
            if (null == conferences || conferences.isEmpty()) {
                return decoratedList;
            }
            decoratedList = new ArrayList<>();
            for (Conference conference : conferences) {
                //En todas estas rutas el usuario esta registrado
                DecoratedConference decorated = new DecoratedConference(conference, true);
                decoratedList.add(decorated);
            }
            return decoratedList;
        }
        return null;
    }
    /**
     * Build and returns an instance of {@link com.example.syp.myapplication.backend.domain.conference.Conference}
     *
     * @param context context of running application
     * @param email email user
     * @return
     */
    public static com.example.syp.myapplication.backend.domain.conference.Conference buildServiceHandler(
            Context context, String email) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(
                context, Config.AUDIENCE);
        credential.setSelectedAccountName(email);

        com.example.syp.myapplication.backend.domain.conference.Conference.Builder builder
                = new com.example.syp.myapplication.backend.domain.conference.Conference.Builder(
                Config.HTTP_TRANSPORT,
                Config.JSON_FACTORY,
                credential);
        builder.setApplicationName(context.getString(R.string.app_name));

        return builder.build();
    }

}
