package com.tuhalang.fake_stream.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuhalang.fake_stream.model.Tweet;
import com.tuhalang.fake_stream.repository.TweetRepository;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;


@Service
public class PublishMessage {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishMessage.class);

    @Autowired
    Producer producer;

    @Autowired
    TweetRepository tweetRepository;

    public void sendMessage(Tweet tweet) throws JsonProcessingException {

        String json = new ObjectMapper().writeValueAsString(tweet);
        System.out.println(json);
        ProducerRecord<String, String> record = new ProducerRecord<>("TWEETS", tweet.getId(), json);
        try {
            producer.send(record, (metadata, e) -> LOGGER.info("Message sent to topic -> " + metadata.topic()
                    + " stored at offset -> " + metadata.offset()
                    + " stored at partition -> " + metadata.partition()));
        }catch (Exception e){
            LOGGER.error(e.getMessage(), e);
        }finally {
            producer.flush();
        }
    }

    @PostConstruct
    public void run(){
        Pageable pageable = PageRequest.of(0, 100);
        Page<Tweet> page = tweetRepository.findAll(pageable);
        int totalPage = page.getTotalPages();
        System.out.println(totalPage);
        for(int i=0; i<totalPage; i++){
            Page<Tweet> tweetPage = tweetRepository.findAll(pageable);
            tweetPage.stream().forEach(tweet -> {
                try {
                    sendMessage(tweet);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
        }
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
