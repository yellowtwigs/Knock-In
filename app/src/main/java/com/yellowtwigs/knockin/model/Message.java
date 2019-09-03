package com.yellowtwigs.knockin.model;

public class Message {
    private String messageContent;
    private boolean fromMe;
    private String number;
    private int profilePicture;
    private String date;
    private String hour;

    public Message(String messageContent, boolean fromMe, String number, int profilePicture, String date, String hour) {
        this.messageContent = messageContent;
        this.fromMe = fromMe;
        this.profilePicture = profilePicture;
        this.date = date;
        this.hour = hour;
        this.number = number;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public boolean getFromMe() {
        return fromMe;
    }

    public int getProfilePicture() {
        return profilePicture;
    }

    public String getDate() {
        return date;
    }

    public String getHour() {
        return hour;
    }

    public String getNumber() {
        return number;
    }
}
