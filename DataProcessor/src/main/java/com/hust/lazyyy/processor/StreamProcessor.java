package com.hust.lazyyy.processor;

import com.hust.lazyyy.config.GlobalConfig;
import com.hust.lazyyy.filter.*;
import com.hust.lazyyy.model.ResultScore;
import com.hust.lazyyy.model.Tweet;
import com.hust.lazyyy.model.TweetScore;
import com.hust.lazyyy.service.KafkaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function3;
import org.apache.spark.sql.*;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.*;
import scala.Tuple2;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class StreamProcessor implements Serializable {

    public static void main(String[] args) throws InterruptedException, ClassNotFoundException, IOException {
        StreamProcessor processor = new StreamProcessor();
        processor.start();
    }

    public void start() throws InterruptedException, ClassNotFoundException, IOException {
        // init
        SparkConf conf = GlobalConfig.getSparkConfig();

        JavaStreamingContext streamingContext = new JavaStreamingContext(conf, Durations.seconds(GlobalConfig.getStreamDuration()));
        final SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();
        sparkSession.sparkContext().setLogLevel("ERROR");

        // set checkpoint
        streamingContext.checkpoint(GlobalConfig.getCheckpointDir());

        // get latest offset
        Map<TopicPartition, Long> latestOffset = getLatestOffSet(sparkSession);

        // get stream data
        JavaInputDStream<ConsumerRecord<String, Tweet>> stream = getStream(streamingContext, latestOffset);

        // add meta data
        JavaDStream<Tweet> transformedStream = stream.transform(item ->
                getEnhancedObjWithKafkaInfo(item)
        );

        // process
        processStream(streamingContext, sparkSession, transformedStream);

        // commit offset
        commitOffset(stream);

        // start stream
        streamingContext.start();
        streamingContext.awaitTermination();
    }

    private void processStream(JavaStreamingContext streamingContext, SparkSession sparkSession, JavaDStream<Tweet> nonFilteredDataStream) throws IOException {
        appendDataToHDFS(sparkSession, nonFilteredDataStream);
        JavaDStream<Tweet> filteredTweetDataStream = getTweetNotProcessed(nonFilteredDataStream);

        // Remove words
        JavaDStream<Tweet> tweets = filteredTweetDataStream.map(new TextFilter());


        // Remove stop words
        JavaDStream<Tweet> tweetsFiltered = tweets.map(new StopWordsFilter());


        // Get postive score
        JavaPairDStream<Tweet, Tuple2<Double, Double>> _scoredTweets = tweetsFiltered.mapToPair(new ScoreFilter());

        // Get Score
        JavaDStream<TweetScore> scoredTweets = _scoredTweets.map(new JoinScoreFilter());

        // Get valid score
        //JavaDStream<TweetScore> filteredScoredTweets = scoredTweets.filter(new ValidScoreFilter());

        // Final result
        JavaDStream<ResultScore> results = scoredTweets.map(new ScoreResultFilter());
        results.cache();

        //results.print();
        JavaDStream<ResultScore> resultFlat = results.flatMap(new FlatMapFunction<ResultScore, ResultScore>() {
            @Override
            public Iterator<ResultScore> call(ResultScore resultScore) throws Exception {
                String tags = resultScore.getTag();
                tags = tags.substring(0, tags.length()-1);
                return Arrays.stream(tags.split(";")).map(tag -> {
                   ResultScore resultScore1 = resultScore.clone();
                   resultScore1.setTag(tag);
                   return resultScore1;
                }).iterator();
            }
        });

        System.out.println("======results=======");
        resultFlat.count().print();
        resultFlat.cache();

        // Write file
        resultFlat.foreachRDD((resultScoreJavaRDD, time) -> {
            Dataset<Row> resultDF = sparkSession.createDataFrame(resultScoreJavaRDD, ResultScore.class);

            Dataset<Row> negDF = resultDF.filter("negativeScore > positiveScore").groupBy("tag").count()
                    .selectExpr("tag", "count as neg");
            Dataset<Row> posDF = resultDF.filter("negativeScore < positiveScore").groupBy("tag").count()
                    .selectExpr("tag", "count as pos");
            Dataset<Row> neuDF = resultDF.filter("negativeScore == positiveScore").groupBy("tag").count()
                    .selectExpr("tag", "count as neu");

            Dataset<Row> rs = negDF.join(posDF, "tag").join(neuDF, "tag");

            rs.selectExpr("tag || ':' || pos || ':' || neg || ':' || neu as value")
                    .foreach(row -> {
                        KafkaService.getInstance().process(row);
                    });


            rs.write()
                    .format("jdbc")
                    .option("url", "jdbc:postgresql://localhost:5432/postgres")
                    .option("dbTable", "result")
                    .option("user", "POSTGRES")
                    .option("password", "123456a@")
                    .mode("append")
                    .save();

            resultDF.write()
                    .format("jdbc")
                    .option("url", "jdbc:postgresql://localhost:5432/postgres")
                    .option("dbTable", "result_score")
                    .option("user", "POSTGRES")
                    .option("password", "123456a@")
                    .mode("append")
                    .save();
        });
    }

    private JavaDStream<Tweet> getTweetNotProcessed(JavaDStream<Tweet> nonFilteredDataStream) {

        JavaPairDStream<String, Tweet> tweetDataPairStream = nonFilteredDataStream
                .mapToPair(tweet -> new Tuple2<>(tweet.getId(), tweet))
                .reduceByKey((a, b) -> a);

        // Check tweet Id is already processed
        JavaMapWithStateDStream<String, Tweet, Boolean, Tuple2<Tweet, Boolean>> tweetDStreamWithStatePairs =
                tweetDataPairStream
                        .mapWithState(
                                StateSpec.function(processedTweetFunc).timeout(Durations.seconds(3600))
                        );//maintain state for one hour

        // Filter processed tweet ids and keep un-processed
        JavaDStream<Tuple2<Tweet, Boolean>> filteredTweetDStreams = tweetDStreamWithStatePairs
                .filter(tuple -> tuple._2.equals(Boolean.FALSE));

        // Get stream of tweet data
        return filteredTweetDStreams.map(tuple -> tuple._1);
    }

    //Function to check processed tweet.
    private final Function3<String, Optional<Tweet>, State<Boolean>, Tuple2<Tweet, Boolean>> processedTweetFunc = (String, tweet, state) -> {
        Tuple2<Tweet, Boolean> tweets = new Tuple2<>(tweet.get(), false);
        if (state.exists()) {
            tweets = new Tuple2<>(tweet.get(), true);
        } else {
            state.update(Boolean.TRUE);
        }
        return tweets;
    };

    /**
     * write data to hdfs
     * @param sparkSession
     * @param nonFilteredDataStream
     */
    private void appendDataToHDFS(SparkSession sparkSession, JavaDStream<Tweet> nonFilteredDataStream) {
        nonFilteredDataStream.foreachRDD(rdd -> {
            if (!rdd.isEmpty()) {
                Dataset<Row> dataFrame = sparkSession.createDataFrame(rdd, Tweet.class);
                Dataset<Row> dfStore = dataFrame.selectExpr(
                        "id", "text", "tags", "createdAt",
                        "metaData.fromOffset as fromOffset",
                        "metaData.untilOffset as untilOffset",
                        "metaData.kafkaPartition as kafkaPartition",
                        "metaData.topic as topic",
                        "metaData.dayOfWeek as dayOfWeek"
                );

                dfStore.write()
                        .partitionBy("topic", "kafkaPartition", "dayOfWeek")
                        .mode(SaveMode.Append)
                        .parquet(GlobalConfig.getHDFSDir()+"tweets");
            }
        });
    }

    /**
     * Commit the ack to kafka some time later, after process have completed
     * @param stream
     */
    private void commitOffset(JavaInputDStream<ConsumerRecord<String, Tweet>> stream) {
        stream.foreachRDD((JavaRDD<ConsumerRecord<String, Tweet>> trafficRdd) -> {
            if (!trafficRdd.isEmpty()) {
                OffsetRange[] offsetRanges = ((HasOffsetRanges) trafficRdd.rdd()).offsetRanges();

                CanCommitOffsets canCommitOffsets = (CanCommitOffsets) stream.inputDStream();
                canCommitOffsets.commitAsync(offsetRanges, new TweetOffSetCommitCallBack());
            }
        });
    }

    /**
     * Add meta data
     * @param item
     * @return
     */
    private JavaRDD<Tweet> getEnhancedObjWithKafkaInfo(JavaRDD<ConsumerRecord<String, Tweet>> item) {
        OffsetRange[] offsetRanges = ((HasOffsetRanges) item.rdd()).offsetRanges();

        return item.mapPartitionsWithIndex((index, items) -> {
            HashMap<String, String> meta = new HashMap<String, String>() {{
                int partition = offsetRanges[index].partition();
                long from = offsetRanges[index].fromOffset();
                long until = offsetRanges[index].untilOffset();

                put("topic", offsetRanges[index].topic());
                put("fromOffset", "" + from);
                put("kafkaPartition", "" + partition);
                put("untilOffset", "" + until);
            }};
            List<Tweet> list = new ArrayList<>();
            while (items.hasNext()) {
                ConsumerRecord<String, Tweet> next = items.next();
                Tweet dataItem = next.value();
                meta.put("dayOfWeek", dataItem.getDayOfWeek());
                dataItem.setMetaData(meta);
                list.add(dataItem);
            }
            return list.iterator();
        }, true);
    }

    /**
     * Get stream data from kafka
     * @param streamingContext
     * @param fromOffsets
     * @return
     */
    private JavaInputDStream<ConsumerRecord<String, Tweet>> getStream(
            JavaStreamingContext streamingContext,
            Map<TopicPartition, Long> fromOffsets
    ) throws ClassNotFoundException {

        ConsumerStrategy<String, Tweet> subscribe;
        if (fromOffsets.isEmpty()) {
            subscribe = ConsumerStrategies.Subscribe(GlobalConfig.getTopics(), GlobalConfig.getKafkaConfig());
        } else {
            subscribe = ConsumerStrategies.Subscribe(GlobalConfig.getTopics(), GlobalConfig.getKafkaConfig(), fromOffsets);
        }

        return KafkaUtils.createDirectStream(
                streamingContext,
                LocationStrategies.PreferConsistent(),
                subscribe
        );
    }


    /**
     * Get latest offset for each partition kafka
     * @param sparkSession
     * @return
     */
    private Map<TopicPartition, Long> getLatestOffSet(SparkSession sparkSession) {
        Map<TopicPartition, Long> collect = Collections.emptyMap();
        try {
            Dataset<Row> parquet = sparkSession.read()
                    .parquet(GlobalConfig.getHDFSDir()+"tweets");

            parquet.createTempView("tweets");
            Dataset<Row> sql = parquet.sqlContext()
                    .sql("select max(untilOffset) as untilOffset, topic, kafkaPartition from tweets group by topic, kafkaPartition");

            collect = sql.javaRDD()
                    .collect()
                    .stream()
                    .map(row -> {
                        TopicPartition topicPartition = new TopicPartition(row.getString(row.fieldIndex("topic")), row.getInt(row.fieldIndex("kafkaPartition")));
                        Tuple2<TopicPartition, Long> key = new Tuple2<>(
                                topicPartition,
                                Long.valueOf(row.getString(row.fieldIndex("untilOffset")))
                        );
                        return key;
                    })
                    .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
        } catch (Exception e) {
            return collect;
        }
        return collect;
    }
}
