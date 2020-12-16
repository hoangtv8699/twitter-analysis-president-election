package com.hust.lazyyy.processor;

import com.hust.lazyyy.config.GlobalConfig;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.api.java.JavaDStream;

import java.io.Serializable;

public class BatchProcessor implements Serializable {

    public static void main(String[] args) {
        BatchProcessor processor = new BatchProcessor();
        processor.start();
    }

    public void start(){
        SparkConf conf = GlobalConfig.getSparkConfig();
        final SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();
        Dataset<Row> tweetDF = sparkSession
                .read()
                .format("jdbc")
                .option("url", "jdbc:postgresql://localhost:5432/postgres")
                .option("dbTable", "tweet")
                .option("user", "POSTGRES")
                .option("password", "123456a@")
                .load();

        System.out.println(tweetDF.schema());
        System.out.println(tweetDF.first());

        RDD<Row> tweetRDD = tweetDF.rdd();
    }
}
