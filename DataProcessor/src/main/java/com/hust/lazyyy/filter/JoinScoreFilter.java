package com.hust.lazyyy.filter;

import com.hust.lazyyy.model.Tweet;
import com.hust.lazyyy.model.TweetScore;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

public class JoinScoreFilter implements Function<Tuple2<Tweet, Tuple2<Double, Double>>, TweetScore> {

    @Override
    public TweetScore call(Tuple2<Tweet, Tuple2<Double, Double>> tweet) throws Exception {
        TweetScore tweetScore = new TweetScore();
        tweetScore.setId(tweet._1().getId());
        tweetScore.setText(tweet._1().getText());
        tweetScore.setTag(tweet._1().getTags());
        tweetScore.setCreatedAt(tweet._1().getCreatedAt());
        tweetScore.setPositiveScore(tweet._2()._1());
        tweetScore.setNegativeScore(tweet._2()._2());
        return tweetScore;
    }
}
