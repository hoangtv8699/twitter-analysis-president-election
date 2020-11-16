package com.hust.lazyyy.filter;

import com.hust.lazyyy.config.GlobalConfig;
import com.hust.lazyyy.model.ResultScore;
import com.hust.lazyyy.model.TweetScore;
import org.apache.spark.api.java.function.Function;

public class ScoreResultFilter implements Function<TweetScore, ResultScore> {
    @Override
    public ResultScore call(TweetScore tweetScore) throws Exception {
        ResultScore resultScore = new ResultScore();
        resultScore.setId(tweetScore.getId());
        resultScore.setCreatedAt(tweetScore.getCreatedAt());
        resultScore.setText(tweetScore.getText());
        resultScore.setTag(tweetScore.getTag());
        resultScore.setNegativeScore(tweetScore.getNegativeScore());
        resultScore.setPositiveScore(tweetScore.getPositiveScore());
        if(tweetScore.getNegativeScore() > tweetScore.getPositiveScore()){
            resultScore.setResult(GlobalConfig.NEGATIVE);
        }else if(tweetScore.getPositiveScore() > tweetScore.getNegativeScore()){
            resultScore.setResult(GlobalConfig.POSITIVE);
        }else{
            resultScore.setResult(GlobalConfig.NEUTRAL);
        }
        return resultScore;
    }
}
