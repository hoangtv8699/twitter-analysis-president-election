package com.hust.lazyyy.filter;

import com.hust.lazyyy.config.NegativeWords;
import com.hust.lazyyy.config.PositiveWords;
import com.hust.lazyyy.model.Tweet;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;
import java.util.Set;

public class ScoreFilter implements PairFunction<Tweet, Tweet, Tuple2<Double, Double>> {

    @Override
    public Tuple2<Tweet, Tuple2<Double, Double>> call(Tweet tweet) throws Exception {
        String text = tweet.getText();
        int type = analyse(text);
//        Set<String> posWords = PositiveWords.getWords();
//        Set<String> negWords = NegativeWords.getWords();
//        String[] words = text.split(" ");
//        int numWords = words.length;
//        int numPosWords = 0;
//        int numNegWords = 0;
//        for (String word : words) {
//            if (posWords.contains(word))
//                numPosWords++;
//            if(negWords.contains(word)){
//                numNegWords++;
//            }
//        }
        double posPoint = 1;
        double negPoint = 1;

        if(type > 2){
            posPoint = 2;
        }

        if(type < 2){
            negPoint = 2;
        }

        return new Tuple2<>(
                tweet,
                new Tuple2<>(
                        posPoint,
                        negPoint)
        );
    }

    public int analyse(String tweet) {

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation = pipeline.process(tweet);
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            return RNNCoreAnnotations.getPredictedClass(tree);
        }
        return 2;
    }
}
