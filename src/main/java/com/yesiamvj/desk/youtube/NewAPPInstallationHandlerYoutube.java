//$Id$
package com.yesiamvj.desk.youtube;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.yesiamvj.desk.ResponseUtility;

@RestController
@EnableAutoConfiguration
public class NewAPPInstallationHandlerYoutube {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NewAPPInstallationHandlerYoutube.class);

    @RequestMapping(value = "/youtube/initializeIntegration", method= RequestMethod.POST)
    public ResponseEntity<String>  process(@RequestBody String payload) {
    	try {
    		
			JSONObject payloadJSON = new JSONObject(payload);
			String orgId = payloadJSON.getString("orgId");
			String securityContext = payloadJSON.getString("securityContext");
			
			LOGGER.info(" initializeIntegration user info callback ... "+orgId);
			
			String youtubeChannelAPIURL = "https://www.googleapis.com/youtube/v3/channels?part=contentDetails&mine=true";
			JSONObject youtubeChannelInvokeAPIResult = DeskUtility.callDeskInvokeAPI(orgId, securityContext, youtubeChannelAPIURL, RequestMethod.GET, null);
			JSONObject youtubeChannelResponse = youtubeChannelInvokeAPIResult.getJSONObject("statusMessage");
			String channelId = getFirstYoutubeChannelId(youtubeChannelResponse);
			
			JSONArray configParams = new JSONArray().put(new JSONObject().put("name", "channel_id").put("value", channelId));
			JSONObject configParamPayload = new JSONObject().put("variables", configParams);
			
			String extensionConfigParamAPIURL = DeskUtility.DESK_API_ROOT+"/api/v1/installedExtensions/{{installationId}}/configParams";
			
			DeskUtility.callDeskInvokeAPI(orgId, securityContext, extensionConfigParamAPIURL, RequestMethod.POST, configParamPayload);
			
	    	return ResponseUtility.sendSuccessResult(null);
    	
    	} catch (JSONException e) {
    		LOGGER.error("error on process", e);
			return ResponseUtility.sendErrorResult("Problem with the server");
		}
    }
    
    private static String getFirstYoutubeChannelId(JSONObject youtubeChannelResponse) throws JSONException {
    	JSONArray channels = youtubeChannelResponse.getJSONArray("items");
    	return channels.getJSONObject(0).getString("id");
    }
}
