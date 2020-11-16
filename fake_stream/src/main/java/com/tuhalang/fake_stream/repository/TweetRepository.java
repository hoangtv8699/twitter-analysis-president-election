package com.tuhalang.fake_stream.repository;

import com.tuhalang.fake_stream.model.Tweet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, String> {

    Page<Tweet> findAll(Pageable pageable);
}
