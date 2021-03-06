package com.example.webfluxdemo.controller;

import com.example.webfluxdemo.model.Tweet;
import com.example.webfluxdemo.repository.TweetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
public class TweetController {

    // inject service
    @Resource
    private TweetRepository tweetRepository;

    @GetMapping("/tweets")
    public Flux<Tweet> getAllTweets(){
        return tweetRepository.findAll();
    }

    @PostMapping("/tweets")
    public Mono<Tweet> createTweets(@Valid @RequestBody Tweet tweet){
        return tweetRepository.save(tweet);
    }

    @GetMapping("tweets/{id}")
    public Mono<ResponseEntity<Tweet>> getTweetById(@PathVariable(value = "id") String tweetId){
        return tweetRepository
                .findById(tweetId)
                .map(savedTweet -> ResponseEntity.ok(savedTweet))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("tweets/{id}")
    public Mono<ResponseEntity<Void>> deleteTweetById(@PathVariable(value = "id") String tweetId){
        return tweetRepository
                .findById(tweetId)
                .flatMap(existingTweet ->
                        tweetRepository.delete(existingTweet)
                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
                ).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("tweets/{id}")
    public Mono<ResponseEntity<Tweet>> updateTweet(@PathVariable(value = "id") String tweetId,
                                                   @Valid @RequestBody Tweet tweet){

        return tweetRepository
                .findById(tweetId)
                .flatMap(existingTweet -> {
                    existingTweet.setText(tweet.getText());
                    return tweetRepository.save(existingTweet);
                })
                .map(updatedTweet -> new ResponseEntity<>(updatedTweet, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

    // Tweets are Sent to the client as Server Sent Events
    @GetMapping(value = "/stream/tweets", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> streamAllTweets() {
        return tweetRepository.findAll();
    }
}


