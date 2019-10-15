package com.test.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/hello"},
    produces = MediaType.APPLICATION_JSON_VALUE)
public class TestController {


    public TestController() {
    }


    @GetMapping(value = "/hello")
    public ResponseEntity<String> getMarker() {
        return new ResponseEntity<String>("hello", HttpStatus.OK);
    }
}
