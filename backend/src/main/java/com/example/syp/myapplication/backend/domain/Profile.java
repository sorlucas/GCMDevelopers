package com.example.syp.myapplication.backend.domain;

import static com.example.syp.myapplication.backend.form.ProfileForm.Gender;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Entity
@Cache
public class Profile {

    @Id
    String userId;

    String displayName;
    String mainEmail;
    Gender gender;

    // Add conferenceKeysToAttend
    private List<String> conferenceKeysToAttend = new ArrayList<>(0);

    public List<String> getConferenceKeysToAttend() {
        return ImmutableList.copyOf(conferenceKeysToAttend);
    }

    public void addToConferenceKeysToAttend(String conferenceKey){
        conferenceKeysToAttend.add(conferenceKey);
    }
    // END

    /**
     * Remove the conferenceId from conferenceIdsToAttend.
     *
     * @param conferenceKey a websafe String representation of the Conference Key.
     */
    public void unregisterFromConference(String conferenceKey){
        if (conferenceKeysToAttend.contains(conferenceKey)){
            conferenceKeysToAttend.remove(conferenceKey);
        } else {
            throw new IllegalArgumentException("Invalid conferenceKey: " + conferenceKey);
        }
    }

    /**
     * Just making the default constructor private.
     */
    private Profile() {}

    /**
     * Public constructor for Profile.
     * @param userId The user id, obtained from the email
     * @param displayName Any string user wants us to display him/her on this system.
     * @param mainEmail User's main e-mail address.
     * @param gender The User's tee shirt size
     *
     */
    public Profile(String userId, String displayName, String mainEmail, Gender gender) {
        this.userId = userId;
        this.displayName = displayName;
        this.mainEmail = mainEmail;
        this.gender = gender;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMainEmail() {
        return mainEmail;
    }

    public Gender getGender() {
        return gender;
    }

    public String getUserId() {
        return userId;
    }

    /**
     *  Update the Profile with the given displayName and teeShirtSize
     *
     *  @param displayName Any string user wants us to display him/her on this system.
     *  @param gender The User's tee shirt size
     */
    public  void update(String displayName, Gender gender){
        if (displayName != null){
            this.displayName = displayName;
        }
        if (gender != null){
            this.gender = gender;
        }
    }
}