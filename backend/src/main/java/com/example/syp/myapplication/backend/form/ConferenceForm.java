package com.example.syp.myapplication.backend.form;

import com.google.common.collect.ImmutableList;

import java.util.Date;
import java.util.List;

/**
 * A simple Java object (POJO) representing a Conference form sent from the client.
 */
public class ConferenceForm {
    /**
     * The name of the conference.
     */
    private String name;

    /**
     * The description of the conference.
     */
    private String description;

    /**
     * Topics that are discussed in this conference.
     */
    private List<String> topics;

    /**
     * The city where the conference will take place.
     */
    private String city;

    /**
     * The capacity of the conference.
     */
    private int maxAttendees;

    /**
     * Url photo cover chat
     */
    private String urlPhotoCover;

    /**
     * Default consutrctor
     */
    private ConferenceForm() {}

    /**
     * Public constructor is solely for Unit Test.
     * @param name
     * @param description
     * @param topics
     * @param city
     * @param maxAttendees
     */
    public ConferenceForm(String name, String description, List<String> topics, String city,
                          int maxAttendees, String urlPhotoCover) {
        this.name = name;
        this.description = description;
        this.topics = topics == null ? null : ImmutableList.copyOf(topics);
        this.city = city;
        this.maxAttendees = maxAttendees;
        this.urlPhotoCover = urlPhotoCover;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTopics() {
        return topics;
    }

    public String getCity() {
        return city;
    }

    public int getMaxAttendees() {
        return maxAttendees;
    }

    public String getUrlPhotoCover() { return urlPhotoCover; }
}

