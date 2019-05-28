package com.example.knocker.model;

public class PhoneLog {
    private String num;
    private String name;
    private String duration;
    private String call_type;
    private String date;

    public PhoneLog(String num, String name, String duration, String call_type, String date) {
        this.num = num;
        this.name = name;
        this.duration = duration;
        this.call_type = call_type;
        this.date = date;
    }

    public String getNum() {
        return num;
    }

    public String getName() {
        return name;
    }

    public String getDuration() {
        return duration;
    }

    public String getCall_type() {
        return call_type;
    }

    public String getDate() {
        return date;
    }
}
