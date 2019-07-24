//$Id$
package com.yesiamvj.desk.youtube;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;

public class DeskUtility {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DeskUtility.class);
	
	public static final String DESK_API_ROOT = "https://desk.zoho.com";
	
	public static JSONObject callDeskInvokeAPI(String orgId, String securityContext, String requestURL, RequestMethod requestMethod, JSONObject payload) {
		
		LOGGER.info("calling desk invoke api for... " + requestURL);
		
		try {
			String deskInvokeAPIURL = DESK_API_ROOT+ "/api/v1/invoke?orgId="+orgId;
			URL url = new URL(deskInvokeAPIURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			
			JSONObject targetHeaders = new JSONObject();
			targetHeaders.put("orgId", orgId);
			targetHeaders.put("Content-Type", "application/json");
			
			String connectionLinkName = requestURL.indexOf("www.googleapis.com")>0 ? "youtube_connection" : "for_github_zoho";
			
			Map<String, String> invokeAPIPostParams = new HashMap<String, String>();
			invokeAPIPostParams.put("requestURL", requestURL);
			invokeAPIPostParams.put("requestType", requestMethod.name());
			invokeAPIPostParams.put("securityContext", securityContext);
			invokeAPIPostParams.put("headers", targetHeaders.toString());
			invokeAPIPostParams.put("connectionLinkName", connectionLinkName);
			
			if(requestMethod == RequestMethod.POST) {
				invokeAPIPostParams.put("postBody", payload.toString());
			}
			
			String invokeAPI_HASH = createHashForDeskInvokeAPI(invokeAPIPostParams);
			
			conn.setRequestProperty("HASH", invokeAPI_HASH);
			
			LOGGER.info("calling desk invoke api... " + deskInvokeAPIURL);
			
			String urlParameters = getPostFieldsAsParameter(invokeAPIPostParams);
			
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		    wr.writeBytes(urlParameters); wr.flush (); wr.close ();

		    InputStream is = conn.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    String line;
		    StringBuffer response = new StringBuffer(); 
		    while((line = rd.readLine()) != null) {
		    	response.append(line);
		    	response.append('\r');
		    }
		    rd.close();
		    
		    String responseStr = response.toString();
			
		    LOGGER.info(" response received ... " + responseStr);
		    
		    return new JSONObject(new JSONObject(responseStr).getString("response"));
			
		}
		catch(Exception e) {
			LOGGER.error("error on desk invoke api ", e);
		}
		return null;
	}
	
	private static String createHashForDeskInvokeAPI(Map<String, String> invokeAPIPostParams) throws NoSuchAlgorithmException, InvalidKeyException {
		String[] hashParamOrder = {"requestURL", "requestType", "postBody", "headers", "connectionLinkName"};
		String hash = "";
		String desk_secret = "vijayakumar.mk+20180801@secretkey.zohocorp.com";
        String to_be_hashed = "";
        for(String param : hashParamOrder) {
        	String value = invokeAPIPostParams.get(param);
        	if(value==null) {
        		 continue;
        	}
            if(!to_be_hashed.isEmpty()){
                to_be_hashed += "&";
            }
            to_be_hashed += param+"="+value;
        }
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        sha256_HMAC.init(new SecretKeySpec(desk_secret.getBytes(), "HmacSHA256"));
        byte[] result = sha256_HMAC.doFinal(to_be_hashed.getBytes());
        hash = DatatypeConverter.printHexBinary(result).toLowerCase();
        LOGGER.info(" hash calculated for  " + to_be_hashed+ " is == "+hash);
        return hash;
	}
	
	private static String getPostFieldsAsParameter(Map<String, String> invokeAPIPostParams) throws UnsupportedEncodingException {
		String urlParameters = "";
		for(String param : invokeAPIPostParams.keySet()) {
			if(!urlParameters.isEmpty()){
				urlParameters += "&";
            }
			urlParameters += param + "=" + URLEncoder.encode(invokeAPIPostParams.get(param), "UTF-8");
		}
		return urlParameters;
	}
	
	protected static Map<String, String> getConfigParamMap(JSONArray configParams) throws JSONException {
    	Map<String, String> configParamsMap = new HashMap<String, String>();
    	for(int i=0;i<configParams.length();i++) {
    		JSONObject configParam = configParams.getJSONObject(i);
    		configParamsMap.put(configParam.getString("name"), configParam.getString("value"));
    	}
    	return configParamsMap;
    }


}
