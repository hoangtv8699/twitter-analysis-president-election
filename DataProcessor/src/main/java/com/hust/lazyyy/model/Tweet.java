package com.hust.lazyyy.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class Tweet implements Serializable {

    private TweetData data;
    private Tag[] matching_rules;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date_tweet;
    private Map<String, String> metaData;

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public TweetData getData() {
        return data;
    }

    public void setData(TweetData data) {
        this.data = data;
    }

    public Tag[] getMatching_rules() {
        return matching_rules;
    }

    public void setMatching_rules(Tag[] matching_rules) {
        this.matching_rules = matching_rules;
    }

    public Date getDate_tweet() {
        return date_tweet;
    }

    public void setDate_tweet(Date date_tweet) {
        this.date_tweet = date_tweet;
    }

    public String getDayOfWeek(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.date_tweet);
        return calendar.get(Calendar.DAY_OF_WEEK)+"";
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "data=" + data +
                ", matching_rules=" + Arrays.toString(matching_rules) +
                ", date_tweet=" + date_tweet +
                ", metaData=" + metaData +
                '}';
    }
}
