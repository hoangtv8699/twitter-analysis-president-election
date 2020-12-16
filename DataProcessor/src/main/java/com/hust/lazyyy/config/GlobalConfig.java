package com.hust.lazyyy.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class GlobalConfig {

    private static final Logger LOGGER = Logger.getLogger(GlobalConfig.class);
    private final static String CONFIG_PATH = "/home/tuhalang/Documents/HUST/4/Bigdata/twitter-analysis-president-election/DataProcessor/src/main/resources/application.properties";
    private static final Properties properties = new Properties();
    private static final String TOPICS = "topics";
    private static final String APP_NAME = "app.name";
    private static final String MASTER = "app.master";
    private static final String DURATION = "stream.duration";
    private static final String CHECKPOINT_DIR = "stream.checkpoint.dir";
    private static final String HDFS_DIR = "app.hdfs.dir";
    public static final Integer POSITIVE = 1;
    public static final Integer NEGATIVE = -1;
    public static final Integer NEUTRAL = 0;

    static {
        try {
            InputStream inputStream = new FileInputStream(CONFIG_PATH);
            properties.load(inputStream);
        }catch (Exception e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static String getHDFSDir(){
        return properties.getProperty(HDFS_DIR);
    }

    public static String getCheckpointDir(){
        return properties.getProperty(CHECKPOINT_DIR);
    }

    public static long getStreamDuration(){
        return Long.parseLong(properties.getProperty(DURATION));
    }

    public static SparkConf getSparkConfig(){
        return new SparkConf()
                .setAppName(properties.getProperty(APP_NAME))
                .setMaster(properties.getProperty(MASTER));
    }

    /**
     * Get topics kafka
     * @return
     */
    public static Collection<String> getTopics(){
        String topicsStr = properties.getProperty(TOPICS);
        String[] topics = topicsStr.split(",");
        return Arrays.asList(topics);
    }

    /**
     * Get kafka configuration
     * @return
     * @throws ClassNotFoundException
     */
    public static Map<String, Object> getKafkaConfig() throws ClassNotFoundException {
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Class.forName(properties.getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG)));
        kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Class.forName(properties.getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG)));
        kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getProperty(ConsumerConfig.GROUP_ID_CONFIG));
        kafkaParams.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
        kafkaParams.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return kafkaParams;
    }
    public static Properties config() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        return properties;
    }

}
