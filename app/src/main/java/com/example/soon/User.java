package com.example.soon;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String name;
    private String betrag;
    private String grund;

    private String date;

    public User() {
    }

    public String getName() {
        return name;
    }

    public String getBetrag() {
        return betrag;
    }

    public String getGrund() {
        return grund;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setBetrag(String betrag) {
        this.betrag = betrag;
    }

    public void setGrund(String grund) {
        this.grund = grund;
    }

    public String getDate() {
        return date;
    }

    public User(String name, String betrag, String grund, String date) {
        this.name = name;
        this.betrag = betrag;
        this.grund = grund;
        this.date = date;
    }
}
