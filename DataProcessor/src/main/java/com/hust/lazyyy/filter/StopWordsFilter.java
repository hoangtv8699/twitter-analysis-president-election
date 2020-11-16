package com.hust.lazyyy.filter;

import com.hust.lazyyy.config.StopWords;
import com.hust.lazyyy.model.Tweet;
import org.apache.spark.api.java.function.Function;

import java.util.List;

public class StopWordsFilter implements Function<Tweet, Tweet> {

    @Override
    public Tweet call(Tweet tweet) throws Exception {
        String filterText = tweet.getText();

        List<String> stopWords = StopWords.getWords();
        for (String word : stopWords) {
            filterText = filterText.replaceAll("\\b" + word + "\\b", "");
        }
        tweet.setText(filterText);

        return tweet;
    }
}
