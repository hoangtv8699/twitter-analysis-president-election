package com.hust.lazyyy.service;

import com.hust.lazyyy.config.GlobalConfig;
import org.apache.kafka.clients.producer.*;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Row;

import java.net.ServerSocket;

public class KafkaService {

    private static final Logger LOGGER = org.apache.log4j.Logger.getLogger(KafkaService.class);

    private static KafkaService _instance;
    private ServerSocket serverSocket;

    private KafkaService(){

    }

    public static KafkaService getInstance(){
        if(_instance == null){
            _instance = new KafkaService();
        }
        return _instance;
    }

    public void process(Row data){
        Producer producer = new KafkaProducer(GlobalConfig.config());
        ProducerRecord<String, String> record = new ProducerRecord<String, String>("RESULTS", System.currentTimeMillis()+"", data.toString());
        try {
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    LOGGER.info("Message sent to topic -> " + metadata.topic()
                            + " stored at offset -> " + metadata.offset()
                            + " stored at partition -> " + metadata.partition());
                }
            });
        }catch (Exception e){
            LOGGER.error(e.getMessage(), e);
        }finally {
            producer.flush();
            producer.close();
        }
    }
}
