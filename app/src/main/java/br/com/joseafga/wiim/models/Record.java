/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package br.com.joseafga.wiim.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This class represents Record Model
 */
public class Record {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("time_opc")
    @Expose
    private String timeOpc;
    @SerializedName("time_db")
    @Expose
    private String timeDb;
    @SerializedName("value")
    @Expose
    private Double value;
    @SerializedName("quality")
    @Expose
    private String quality;
    @SerializedName("tag")
    @Expose
    private Integer tag;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTimeOpc() {
        return timeOpc;
    }

    public void setTimeOpc(String timeOpc) {
        this.timeOpc = timeOpc;
    }

    public String getTimeDb() {
        return timeDb;
    }

    public void setTimeDb(String timeDb) {
        this.timeDb = timeDb;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public Integer getTag() {
        return tag;
    }

    public void setTag(Integer tag) {
        this.tag = tag;
    }
}