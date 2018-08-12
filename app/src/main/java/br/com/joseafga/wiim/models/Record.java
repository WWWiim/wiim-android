/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * https://creativecommons.org/licenses/by/4.0/
 */
package br.com.joseafga.wiim.models;

/**
 * This class represents Records Model
 */
public class Record {
    private int id = 0;
    private String time_opc = "";
    private String time_db = "";
    private double value = 0;
    private String quality = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime_opc() {
        return time_opc;
    }

    public void setTime_opc(String time_opc) {
        this.time_opc = time_opc;
    }

    public String getTime_db() {
        return time_db;
    }

    public void setTime_db(String time_db) {
        this.time_db = time_db;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }
}
