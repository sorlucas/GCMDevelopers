package com.sergio.example.owngcm.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.sergio.example.owngcm.R;
import com.sergio.example.owngcm.model.DecoratedConference;
import com.sergio.example.owngcm.navdrawer.mockedFragments.FragmentListChats;
import com.sergio.example.owngcm.provider.RouteContract;
import com.sergio.example.owngcm.utils.AccountUtils;
import com.sergio.example.owngcm.utils.DatabaseHelperUtils;
import com.sergio.example.owngcm.utils.StringUtils;
import com.sergio.example.owngcm.utils.UIUtils;

import java.util.IllegalFormatException;
import java.util.List;

import static com.sergio.example.owngcm.utils.LogUtils.LOGD;
import static com.sergio.example.owngcm.utils.LogUtils.LOGE;
import static com.sergio.example.owngcm.utils.LogUtils.makeLogTag;

/**
 * Created by syp on 14/08/15.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String TAG =  makeLogTag(SyncAdapter.class);

    public static final String SYNC_MODE_TYPE = "mode_sync";
    public static final String SYNC_MODE_SEARCH = "mode_search_chats";
    public static final String SYNC_MODE_ATTENDES_CHATS = "mode_attendes_chats";
    public static final String SYNC_MODE_REGISTRATION_CHAT = "mode_registration_chat";

    // Interval at which to sync  in seconds. 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int ROUTE_NOTIFICATION_ID = 3999;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "Starting sync");

        // TODO: FUTURO CAMBIO DE las conferencias que loader con GCM. mas eficiente.
        // Ahora sincronizamos el Sync Adapter con acciones del usuario: abrir applicacion....cuando
        // se ha subido la ruta, implementacion mecanica de sincronizacion. MAL
        String typeSync = extras.getString(SYNC_MODE_TYPE);

        if (typeSync != null) {
            List<DecoratedConference> decoratedConferences = null;

            // Download data from serverd dependen of  type sync
            switch (typeSync) {
                case SYNC_MODE_SEARCH:
                    SearchConferenceLoader searchLoader = new SearchConferenceLoader(getContext());
                    decoratedConferences = searchLoader.loadInBackground();
                    break;
                case SYNC_MODE_ATTENDES_CHATS:
                    AttendedConferenceLoader registerConferenceLoader = new AttendedConferenceLoader(getContext());
                    decoratedConferences = registerConferenceLoader.loadInBackground();
                    break;
                case SYNC_MODE_REGISTRATION_CHAT:
                    String websafeKey = extras.getString(RouteContract.RouteEntry.COLUMN_WEBSAFE_KEY);
                    int isRegistered = extras.getInt(RouteContract.RouteEntry.COLUMN_REGISTERED);
                    RegisterConferenceLoader registrationConferenceLoader = new RegisterConferenceLoader(getContext(),websafeKey,isRegistered);
                    Boolean sucefully = registrationConferenceLoader.loadInBackground();
                    break;
                default:

                    LOGE(TAG, "Error Type Sync Mode");
                    break;
            }

            // Solucion para soluciona problema de incomapitbilidad null&0 cuando no hay ninguna conferencia
            int size = (decoratedConferences == null ? 0 : decoratedConferences.size());
            if (size > 0){
                //Create route values today an insert in database
                int rowsInserted = DatabaseHelperUtils.addRoutesToSQLite(getContext(), decoratedConferences);
                if(rowsInserted > 0){
                    // notifyRoute();
                }
                Log.d(TAG, "Sync Complete. " + rowsInserted + " new Routes Inserted");
            }
        } else {
            LOGE(TAG, "No specifing any Type Sync");
        }    }

    // TODO: FUTURO: initializeSyncAdapter (Inicializacion!!)
    //Metodos para configuracion de la Sync
    public static void initializeSyncAdapter(Context context, String syncMode, Bundle extras) {
        Log.d(TAG, "initializeSyncAdapter. -> syncMode: " + syncMode);

        Bundle bundle = new Bundle();
        switch (syncMode){
            case SYNC_MODE_SEARCH:
                bundle.putString(SYNC_MODE_TYPE,SYNC_MODE_SEARCH);
                break;
            case SYNC_MODE_ATTENDES_CHATS:
                bundle.putString(SYNC_MODE_TYPE,SYNC_MODE_ATTENDES_CHATS);
                break;
            case SYNC_MODE_REGISTRATION_CHAT:
                bundle.putString(SYNC_MODE_TYPE, SYNC_MODE_REGISTRATION_CHAT);
                bundle.putString(RouteContract.RouteEntry.COLUMN_WEBSAFE_KEY,extras.getString(RouteContract.RouteEntry.COLUMN_WEBSAFE_KEY));
                bundle.putInt(RouteContract.RouteEntry.COLUMN_REGISTERED, extras.getInt(RouteContract.RouteEntry.COLUMN_REGISTERED));
                break;
            default: throw new IllegalArgumentException("unreachable SYNC_MODE_TYPE");

        }

        // TODO: Implement to initializeSyncAdapter
        //SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(
                AccountUtils.getActiveAccount(context),
                context.getString(R.string.content_authority),
                true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context, bundle);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {

        LOGD(TAG, "configurePeriodicSync");

        Account account = AccountUtils.getActiveAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context,Bundle bundle) {

        LOGD(TAG, "syncImmediately");

        Account account = AccountUtils.getActiveAccount(context);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(account,
                context.getString(R.string.content_authority), bundle);
    }
}
