package com.sergio.example.owngcm.sync;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.sergio.example.owngcm.model.DecoratedConference;
import com.sergio.example.owngcm.utils.ConferenceException;
import com.sergio.example.owngcm.utils.ConferenceUtils;
import com.sergio.example.owngcm.utils.UIUtils;

import java.io.IOException;
import java.util.List;

import static com.sergio.example.owngcm.utils.LogUtils.makeLogTag;

/**
 * Created by syp on 18/08/15.
 */
public class AttendedConferenceLoader extends AsyncTaskLoader<List<DecoratedConference>> {

    private static final String TAG = makeLogTag(AttendedConferenceLoader.class);
    private Exception mException;


    private Context mContext;
    public AttendedConferenceLoader(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public List<DecoratedConference> loadInBackground() {
        try {
            ConferenceUtils.build(mContext);
            return ConferenceUtils.getConferencesToAttend();
        } catch (IOException e) {
            Log.e(TAG, "Failed to get conferences", e);
            mException = e;
        } catch (ConferenceException e) {
            // logged
        }
        return null;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        cancelLoad();
        if (mException != null) {
            UIUtils.displayNetworkErrorMessage(getContext());
        }
    }

    public Exception getException() {
        return mException;
    }

}