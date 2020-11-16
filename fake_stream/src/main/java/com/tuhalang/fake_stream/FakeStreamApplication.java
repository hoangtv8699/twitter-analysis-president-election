package com.tuhalang.fake_stream;

import com.tuhalang.fake_stream.model.Tweet;
import com.tuhalang.fake_stream.repository.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@SpringBootApplication
public class FakeStreamApplication {



    public static void main(String[] args) {
        SpringApplication.run(FakeStreamApplication.class, args);



    }

}
