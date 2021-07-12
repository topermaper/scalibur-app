package com.itene.scalibur.models;

public class Stretch {

    private int from;
    private int to;
    private String to_name;
    private String from_name;
    private double time;
    private double distance;

    public Stretch() {}

    public Stretch(String from_name, String to_name, double time, double distance) {
        this.from_name = from_name;
        this.to_name = to_name;
        this.time = time;
        this.distance = distance;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public String getTo_name() {
        return to_name;
    }

    public void setTo_name(String to_name) {
        this.to_name = to_name;
    }

    public String getFrom_name() {
        return from_name;
    }

    public void setFrom_name(String from_name) {
        this.from_name = from_name;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
