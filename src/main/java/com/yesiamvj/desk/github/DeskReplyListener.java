//$Id$
package com.yesiamvj.desk.github;

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

@RestController
@EnableAutoConfiguration
public class DeskReplyListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DeskReplyListener.class);

    @RequestMapping(value = "/github/deskReplyListener", method= RequestMethod.POST)
    public ResponseEntity<String>  process(
    		@RequestBody String payload,
    		@RequestParam("orgId") String orgId,
    		@RequestParam("securityContext") String securityContext) {
    	try {
			JSONObject deskReplyObject = new JSONObject(payload).getJSONObject("resource");
	    	JSONObject gitHubCommentResponse = convertAndPushToGitHub(orgId, securityContext, deskReplyObject);
	    	JSONObject responseObjectForDesk = githubCommentResponseToDeskReply(gitHubCommentResponse);
	    	return ResponseUtility.sendSuccessResult(responseObjectForDesk.toString());
    	
    	} catch (JSONException e) {
    		LOGGER.error("error on process", e);
			return ResponseUtility.sendErrorResult("Problem with the server");
		}
    }
    
    public JSONObject convertAndPushToGitHub(String orgId, String securityContext, JSONObject deskReplyObject) throws JSONException {
    	
    	String agentReplyContent = deskReplyObject.getString("content");
    	JSONObject githubCommentPayload = new JSONObject().put("body", agentReplyContent);
    	
    	String externalParentId = deskReplyObject.getString("extParentId");
    	String replyToThreadId = deskReplyObject.getString("replyToExtId");
    	String externalID = externalParentId!=null && !externalParentId.isEmpty() ? externalParentId : replyToThreadId;
    	
    	String[] githubCombinedID = externalID.split(":-:");
    	
    	String repoName = githubCombinedID[0];
    	String issueNumber = githubCombinedID[1];
    	String authorUserName = githubCombinedID[2];
    	
    	String githubCommentAPIURL = "https://api.github.com/repos/"+authorUserName+"/"+repoName+"/issues/"+issueNumber+"/comments";
    	
    	JSONObject invokeAPIResult = DeskUtility.callDeskInvokeAPI(orgId, securityContext, githubCommentAPIURL, RequestMethod.POST, githubCommentPayload);
    	JSONObject invokeAPIResponse = invokeAPIResult.getJSONObject("statusMessage");
    	return invokeAPIResponse;
    }
    
    private JSONObject githubCommentResponseToDeskReply(JSONObject githubComment) throws JSONException {
    	JSONObject deskReplyObject = new JSONObject();
	    	deskReplyObject.put("extId", githubComment.getString("id"));
	    	deskReplyObject.put("canReply", true);
	    	deskReplyObject.put("extra", getCommentExtraMetaJSON(githubComment));
    	return deskReplyObject;
    }
    
    private JSONObject getCommentExtraMetaJSON(JSONObject githubComment) {
    	try {
    		
			JSONObject extraValueJSON = new JSONObject();
				extraValueJSON.put("node_id", githubComment.getString("node_id"));
				extraValueJSON.put("author_association", githubComment.getString("author_association"));
				
			JSONObject extraJSON = new JSONObject();
				extraJSON.put("key", "{{thread.id}}_channel_details");
				extraJSON.put("queriableValue", "github_thread_extras");
				extraJSON.put("value", extraValueJSON);
				
			return extraJSON;
    	}
		catch (JSONException e) {
			LOGGER.error("error on getCommentExtraMetaJSON", e);
			return null;
		}
    }
  
}