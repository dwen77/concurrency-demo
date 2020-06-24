package com.example.concurrent.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("demo")
@Slf4j
public class DemoController {

    @GetMapping("ping/{taskId}")
    public ResponseEntity<String> ping(@PathVariable("taskId") Integer taskId) {
        log.info("received ping request {} ", taskId);
        try {
            Thread.sleep(1000* 5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("send pong response {} ", taskId);
        return ResponseEntity.ok("pong");
    }
}
