package com.brashear.readlater.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class ReadlaterController {

    @GetMapping
    public String getHealth() {
        return "OK";
    }
}
