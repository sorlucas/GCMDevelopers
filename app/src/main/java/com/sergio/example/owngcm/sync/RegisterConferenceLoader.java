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
public class RegisterConferenceLoader extends AsyncTaskLoader<Boolean> {

    private static final String TAG = makeLogTag(RegisterConferenceLoader.class);
    private Exception mException;

    private int mIsRegisterd;
    private String mWebsafeKey;

    private Context mContext;

    public RegisterConferenceLoader(Context context, String websafeKey, int isRegistered) {
        super(context);
        mContext = context;
        mIsRegisterd = isRegistered;
        mWebsafeKey = websafeKey;
    }

    @Override
    public Boolean loadInBackground() {

        //TODO: creo que tenemos demasiados build
        ConferenceUtils.build(mContext);
        try {
            try {
                if (mIsRegisterd == 1) {
                    boolean success = ConferenceUtils
                            .unregisterFromConference(mWebsafeKey);
                    return success;
                } else {
                    boolean success = ConferenceUtils.registerForConference(mWebsafeKey);
                    return success;
                }
            } catch (IOException e) {
                mException = e;
            }
        } catch (ConferenceException e) {
            //logged
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