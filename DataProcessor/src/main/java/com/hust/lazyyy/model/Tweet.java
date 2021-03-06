/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hust.lazyyy.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Calendar;
import java.sql.Date;
import java.util.HashMap;

/**
 *
 * @author hungpv
 */
public class Tweet implements Serializable {
    
    private String id;
    private String text;
    private String tags;
    @JsonProperty(value = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'")
    private Date createdAt;

    private Object withheld;

    private HashMap<String, String> metaData;

    public HashMap<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(HashMap<String, String> metaData) {
        this.metaData = metaData;
    }

    public Object getWithheld() {
        return withheld;
    }

    public void setWithheld(Object withheld) {
        this.withheld = withheld;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDayOfWeek(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.createdAt);
        return String.valueOf(cal.get(Calendar.DAY_OF_WEEK));
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", tags='" + tags + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
