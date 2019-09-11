//$Id$
package com.yesiamvj.desk;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class Homepage {

    @RequestMapping(value = "/", method= RequestMethod.GET)
    public String  process(){
    	return "Voila! App is running";
    }

}