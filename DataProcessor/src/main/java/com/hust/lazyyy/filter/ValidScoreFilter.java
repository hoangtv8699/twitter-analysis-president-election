package com.hust.lazyyy.filter;

import com.hust.lazyyy.model.TweetScore;
import org.apache.spark.api.java.function.Function;

public class ValidScoreFilter implements Function<TweetScore, Boolean> {
    @Override
    public Boolean call(TweetScore tweetScore) throws Exception {
        return (tweetScore.getNegativeScore() > tweetScore.getPositiveScore() ||
                tweetScore.getNegativeScore() < tweetScore.getPositiveScore() ||
                ((tweetScore.getNegativeScore() == tweetScore.getPositiveScore()) &&
                        (tweetScore.getNegativeScore() > 0.0 && tweetScore.getPositiveScore() > 0.0)));
    }
}
