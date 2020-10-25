package com.hust.lazyyy.processor;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Map;

final public class TweetOffSetCommitCallBack implements OffsetCommitCallback, Serializable {
    private static final Logger log = Logger.getLogger(TweetOffSetCommitCallBack.class);

    @Override
    public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
        log.info("---------------------------------------------------");
        log.info(String.format("{0} | {1}", new Object[]{offsets, exception}));
        log.info("---------------------------------------------------");
    }
}
