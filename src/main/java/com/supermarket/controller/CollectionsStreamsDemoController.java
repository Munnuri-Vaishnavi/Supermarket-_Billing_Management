package com.supermarket.controller;

import com.supermarket.service.CollectionsDemoService;
import com.supermarket.service.StreamAnalyticsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@CrossOrigin(origins = "*")
public class CollectionsStreamsDemoController {

    private final CollectionsDemoService collectionsDemoService;
    private final StreamAnalyticsService streamAnalyticsService;

    public CollectionsStreamsDemoController(CollectionsDemoService collectionsDemoService, StreamAnalyticsService streamAnalyticsService) {
        this.collectionsDemoService = collectionsDemoService;
        this.streamAnalyticsService = streamAnalyticsService;
    }

    @GetMapping("/collections/list")
    public Map<String, Object> demoList() {
        return collectionsDemoService.demonstrateListOperations();
    }

    @GetMapping("/collections/set")
    public Map<String, Object> demoSet() {
        return collectionsDemoService.demonstrateSetOperations();
    }

    @GetMapping("/collections/map")
    public Map<String, Object> demoMap() {
        return collectionsDemoService.demonstrateMapOperations();
    }

    @GetMapping("/collections/queue")
    public Map<String, Object> demoQueue() {
        return collectionsDemoService.demonstrateQueueOperations();
    }

    @GetMapping("/streams/full")
    public Map<String, Object> demoStreams() {
        return streamAnalyticsService.runFullStreamDemo();
    }
}

