package com.srvraj311.Modal;

import java.util.Date;
import java.util.UUID;

public class Note {
    private String note_id;
    private String note_title;
    private String note_body;
    private String note_date;
    private String note_colour;
    private boolean pinned;

    public Note(String note_title, String note_body, String note_date, String note_colour, boolean pinned) {
        this.note_title = note_title;
        this.note_body = note_body;
        this.note_date = note_date;
        this.note_colour = note_colour;
        // Generate Random ID
        UUID uniqueKey = UUID.randomUUID();
        this.note_id = String.valueOf(uniqueKey);
        this.pinned = pinned;
    }
    //New note without Colour
    public Note(String note_title, String note_body, String note_date, boolean pinned) {
        this.pinned = pinned;
        this.note_title = note_title;
        this.note_body = note_body;
        this.note_date = note_date;
        this.note_colour = "#FFFFFF";
        // Generate Random ID
        UUID uniqueKey = UUID.randomUUID();
        this.note_id = String.valueOf(uniqueKey);
    }

    // To update the existing note
    public Note(String note_title, String note_body, String note_date,String note_colour, String note_id, boolean pinned) {
        this.note_title = note_title;
        this.note_body = note_body;
        this.note_date = note_date;
        this.note_colour = note_colour;
        this.note_id = note_id;
        this.pinned = pinned;
    }

    public String getNote_id() {
        return note_id;
    }

    public void setNote_id(String note_id) {
        this.note_id = note_id;
    }

    public String getNote_title() {
        return note_title;
    }

    public void setNote_title(String note_title) {
        this.note_title = note_title;
    }

    public String getNote_body() {
        return note_body;
    }

    public void setNote_body(String note_body) {
        this.note_body = note_body;
    }

    public String getNote_date() {
        return note_date;
    }

    public void setNote_date(String note_date) {
        this.note_date = note_date;
    }

    public String getNote_colour() {
        return note_colour;
    }

    public void setNote_colour(String note_colour) {
        this.note_colour = note_colour;
    }
    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

}
