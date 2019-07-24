//$Id$
package com.yesiamvj.desk;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class ResponseUtility {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseUtility.class);

    public static ResponseEntity<String> sendSuccessResult(String json){
    	HttpHeaders responseHeaders = new HttpHeaders();
    	responseHeaders.setContentType(MediaType.APPLICATION_JSON);
    	return new ResponseEntity<String>(json, responseHeaders, HttpStatus.OK);
    }
    
    public static ResponseEntity<String> sendErrorResult(String message){
    	JSONObject json = new JSONObject();
    	try {
			json.put("message", message);
		}
    	catch (JSONException e) {
			LOGGER.error("error on sendErrorResult", e);
		}
    	HttpHeaders responseHeaders = new HttpHeaders();
    	responseHeaders.setContentType(MediaType.APPLICATION_JSON);
    	return new ResponseEntity<String>(json.toString(), responseHeaders, HttpStatus.BAD_REQUEST);
    }
}
