package com.hust.lazyyy.filter;

import com.hust.lazyyy.config.PositiveWords;
import com.hust.lazyyy.model.Tweet;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.Set;

public class PositiveScoreFilter implements PairFunction<Tweet, Tweet, Double> {

    @Override
    public Tuple2<Tweet, Double> call(Tweet tweet) throws Exception {
        String text = tweet.getText();
        Set<String> posWords = PositiveWords.getWords();
        String[] words = text.split(" ");
        int numWords = words.length;
        int numPosWords = 0;
        for (String word : words) {
            if (posWords.contains(word))
                numPosWords++;
        }
        return new Tuple2<>(
                tweet,
                (double) numPosWords / numWords
        );
    }
}
