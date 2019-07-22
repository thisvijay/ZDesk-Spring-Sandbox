//$Id$
package com.vijay.desk.yesiamvjextension;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class Helloworld {

    @RequestMapping("/")
    String hello() {
        return "Welcome to extension server";
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Helloworld.class, args);
    }
}