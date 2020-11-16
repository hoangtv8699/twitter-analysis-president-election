package com.hust.lazyyy.model;

import java.io.Serializable;
import java.sql.Date;

public class ResultScore implements Serializable {

    private String id;
    private String text;
    private String tag;
    private Date createdAt;
    private Double negativeScore;
    private Double positiveScore;
    private Integer result;

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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Double getNegativeScore() {
        return negativeScore;
    }

    public void setNegativeScore(Double negativeScore) {
        this.negativeScore = negativeScore;
    }

    public Double getPositiveScore() {
        return positiveScore;
    }

    public void setPositiveScore(Double positiveScore) {
        this.positiveScore = positiveScore;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return this.id + " - "  + this.text + " - " + this.result;
    }
}
