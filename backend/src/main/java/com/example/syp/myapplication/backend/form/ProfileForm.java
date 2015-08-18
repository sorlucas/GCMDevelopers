package com.example.syp.myapplication.backend.form;

/**
 * Pojo representing a profile form on the client side.
 */
public class ProfileForm {
    /**
     * Any string user wants us to display him/her on this system.
     */
    private String displayName;

    /**
     * TGender user
     */
    private Gender gender;

    private ProfileForm() {}

    /**
     * Constructor for ProfileForm, solely for unit test.
     * @param displayName A String for displaying the user on this system.
     * @param gender A gender user.
     */
    public ProfileForm(String displayName, Gender gender) {
        this.displayName = displayName;
        this.gender = gender;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Gender getGender() {
        return gender;
    }

    public enum Gender {
        NOT_SPECIFIED,
        MALE,
        FEMALE
    }
}
