package com.dimas.android.locatr.FavoritePlace;

import java.io.Serializable;
import java.util.Date;

public class PlaceInfo implements Serializable {
    private String title;
    private String body;
    private int temperature;
    private String weather;
    private Date date;

    public PlaceInfo() {
        date = new Date();
    }

    public Date getDate() {
        return date;
    }

    public PlaceInfo(String title, String address) {
        this.title = title;
        this.body = address;
        date = new Date();

    }

    public String getBody() {
        return body;
    }

    public void setBody(String address) {
        this.body = address;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public int getTemperature() {
        return temperature;
    }
    public String getTemperatureToString(){
        return "Температура на улице "+String.valueOf(getTemperature())+" по цельсию";
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }
}
