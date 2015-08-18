package com.sergio.example.owngcm.model;


import com.example.syp.myapplication.backend.domain.conference.model.Conference;

/**
 * A wrapper around the {@link Conference}
 * to enable adding additional fields and operations.
 */
public class DecoratedConference {

    private Conference mConference;
    private boolean mRegistered;

    public DecoratedConference(Conference conference, boolean registered) {
        mConference = conference;

        /* keeps the status of user's attendance to this conference */
        mRegistered = registered;
    }

    public Conference getConference() {
        return mConference;
    }

    public void setConference(Conference conference) {
        mConference = conference;
    }

    public boolean isRegistered() {
        return mRegistered;
    }

    public void setRegistered(boolean registered) {
        mRegistered = registered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DecoratedConference that = (DecoratedConference) o;

        if (mConference != null ? !mConference.equals(that.mConference)
                : that.mConference != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return mConference != null ? mConference.hashCode() : 0;
    }
}