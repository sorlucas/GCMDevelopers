package com.sergio.example.owngcm.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import static com.sergio.example.owngcm.utils.LogUtils.makeLogTag;

/**
 * Created by syp on 14/08/15.
 */
public class SyncService extends Service {

    private static final String TAG = makeLogTag(SyncService.class);

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate - SyncService");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), false);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
