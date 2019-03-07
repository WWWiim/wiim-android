/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * License AGPLv3/Commercial, see LICENSE file for more details
 */

package br.com.joseafga.wiim.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This class represents Timeline Model
 */
public class Timeline {

    @SerializedName("tag")
    @Expose
    private Tag tag = null;
    @SerializedName("record")
    @Expose
    private Record record = null;

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }
}