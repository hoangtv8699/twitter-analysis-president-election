package com.hust.lazyyy.filter;

import com.hust.lazyyy.model.Tweet;
import org.apache.spark.api.java.function.Function;

public class TextFilter implements Function<Tweet, Tweet> {

    @Override
    public Tweet call(Tweet tweet) throws Exception {
        String filterText = tweet.getText();
        filterText = filterText.replaceAll("[^a-zA-Z\\s]", "").trim().toLowerCase();
        filterText = filterText.replaceAll("(\\r\\n)", " ");
        filterText = filterText.replaceAll("(\\r|\\n)", " ");

        tweet.setText(filterText);

        return tweet;
    }
}
