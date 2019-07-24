//$Id$
package com.yesiamvj.desk.youtube;

import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yesiamvj.desk.ResponseUtility;

@RestController
@EnableAutoConfiguration
public class DeskReplyListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DeskReplyListener.class);

    @RequestMapping(value = "/youtube/deskReplyListener", method= RequestMethod.POST)
    public ResponseEntity<String>  process(
    		@RequestBody String payload,
    		@RequestParam("orgId") String orgId,
    		@RequestParam("securityContext") String securityContext) {
    	try {
    		JSONObject deskPayload = new JSONObject(payload);
			JSONObject deskReplyObject = deskPayload.getJSONObject("resource");
			JSONArray configParams = deskPayload.getJSONArray("configParams");
			Map<String, String> configParamsMap = DeskUtility.getConfigParamMap(configParams);
			String youtubeChannelId = configParamsMap.get("channel_id");
	    	JSONObject youtubeCommentResponse = convertAndPushToYoutube(youtubeChannelId, orgId, securityContext, deskReplyObject);
	    	JSONObject responseObjectForDesk = YoutbeDeskConverstionUtil.convertYoutubeCommentResponseForDesk(youtubeChannelId, youtubeCommentResponse);
	    	return ResponseUtility.sendSuccessResult(responseObjectForDesk.toString());
    	
    	} catch (JSONException e) {
    		LOGGER.error("error on process", e);
			return ResponseUtility.sendErrorResult("Problem with the server");
		}
    }
    
    public JSONObject convertAndPushToYoutube(String youtubeChannelId, String orgId, String securityContext, JSONObject deskReplyObject) throws JSONException {
    	
    	String agentReplyContent = deskReplyObject.getString("content");
    	
    	String externalParentId = deskReplyObject.getString("extParentId");
    	String replyToThreadId = deskReplyObject.getString("replyToExtId");
    	
    	if(replyToThreadId!=null && !replyToThreadId.isEmpty()) {
    		String parentId = replyToThreadId;
    		if(replyToThreadId.indexOf('.')>1) {
    			parentId = replyToThreadId.split(".")[0];
    		}
    		
    		JSONObject youtubeCommentSnippet = new JSONObject();
    		youtubeCommentSnippet.put("parentId", parentId);
    		youtubeCommentSnippet.put("textOriginal", agentReplyContent);
    		
    		JSONObject youtubePayload = new JSONObject().put("snippet", youtubeCommentSnippet);
    		
    		String githubCommentAPIURL = "https://www.googleapis.com/youtube/v3/comments?part=snippet";
        	
        	JSONObject invokeAPIResult = DeskUtility.callDeskInvokeAPI(orgId, securityContext, githubCommentAPIURL, RequestMethod.POST, youtubePayload);
        	JSONObject invokeAPIResponse = invokeAPIResult.getJSONObject("statusMessage");
        	invokeAPIResponse.put("type", "commentReply");
        	return invokeAPIResponse;
    	}
    	else {
    		JSONObject youtubeCommentSnippet = new JSONObject();
    		youtubeCommentSnippet.put("channelId", youtubeChannelId);
    		youtubeCommentSnippet.put("videoId", externalParentId);
    		
    		JSONObject youtubeTopLevelCommentSnippet = new JSONObject().put("snippet", new JSONObject().put("textOriginal", agentReplyContent));
    		
    		youtubeCommentSnippet.put("topLevelComment", youtubeTopLevelCommentSnippet);
    		
    		JSONObject youtubePayload = new JSONObject().put("snippet", youtubeCommentSnippet);
    		
    		String githubCommentAPIURL = "https://www.googleapis.com/youtube/v3/comments?part=snippet";
        	
        	JSONObject invokeAPIResult = DeskUtility.callDeskInvokeAPI(orgId, securityContext, githubCommentAPIURL, RequestMethod.POST, youtubePayload);
        	JSONObject invokeAPIResponse = invokeAPIResult.getJSONObject("statusMessage");
        	invokeAPIResponse.put("type", "topComment");
        	return invokeAPIResponse;
    	}
    }
  
}