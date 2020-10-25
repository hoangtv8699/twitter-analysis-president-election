package com.hust.lazyyy.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hust.lazyyy.model.Tweet;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

public class TweetDeserializer implements Deserializer<Tweet> {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map map, boolean b) {

    }

    @Override
    public Tweet deserialize(String s, byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, Tweet.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {

    }
}
