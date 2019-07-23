//String IdString 
package com.yesiamvj.desk.github;

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

@RestController
@EnableAutoConfiguration
public class GithubCommentListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GithubCommentListener.class);

    @RequestMapping(value = "/github/githubCommentListener", method= RequestMethod.POST)
    public ResponseEntity<String>  process(
    		@RequestBody String payload,
    		@RequestParam("appOrgId") String orgId,
    		@RequestParam("appSecurityContext") String securityContext) {
    	try {
    		
    		JSONObject payloadJSON = new JSONObject(payload);
    		
			JSONObject deskReplyObject = convertGitHubDataForDesk(payloadJSON);
	    	
			String deskImportAPIURL = DeskUtility.DESK_API_ROOT + "/api/v1/channels/{{installationId}}/import"; 
			
			LOGGER.info(" data sent to desk "+ deskReplyObject.toString());
			
			DeskUtility.callDeskInvokeAPI(orgId, securityContext, deskImportAPIURL, RequestMethod.POST, deskReplyObject);
			
	    	return ResponseUtility.sendSuccessResult(null);
    	
    	} catch (JSONException e) {
    		LOGGER.error("error on process", e);
			return ResponseUtility.sendErrorResult("Problem with the server");
		}
    }
    
    private JSONObject convertGitHubDataForDesk(JSONObject githubPayload) throws JSONException {
    	JSONArray threads = new JSONArray();
    	JSONArray tickets = new JSONArray();
        
        String repo_name = githubPayload.getJSONObject("repository").getString("name");
        String repo_owner = githubPayload.getJSONObject("repository").getJSONObject("owner").getString("login");
        
        if(githubPayload.has("issue")){
            JSONObject ticket = convertGitHubIssueAsDeskTicket(githubPayload);
            tickets.put(ticket);
        }
        
        if(githubPayload.has("comment")){
        	
            JSONObject issue = githubPayload.getJSONObject("issue");
            JSONObject issue_comment = githubPayload.getJSONObject("comment");
            JSONObject issue_comment_author = issue_comment.getJSONObject("user");
            String authorName = issue_comment_author.getString("login"); 
            String photoURL = issue_comment_author.getString("avatar_url");
            String issueNumber = issue.getString("number");
            
            JSONObject deskThreadJSON = new JSONObject();
            deskThreadJSON.put("extId", issue_comment.getString("id"));
            deskThreadJSON.put("extParentId", repo_name+":-:"+issueNumber+":-:"+repo_owner);
            deskThreadJSON.put("content", issue_comment.getString("body"));
            deskThreadJSON.put("createdTime", issue_comment.getString("created_at"));
            deskThreadJSON.put("contentType", "text/html");
            deskThreadJSON.put("direction", issue_comment.getString("author_association").equals("NONE") ? "in" : "out");
            deskThreadJSON.put("from", issue_comment_author.getString("login"));
            deskThreadJSON.put("canReply", true);
            deskThreadJSON.put("extra", getCommentExtraMetaJSON(issue_comment));
            
            JSONObject actorJSON = new JSONObject();
            actorJSON.put("name", authorName);
            actorJSON.put("displayName", authorName+" (Github)");
            actorJSON.put("extId", authorName);
            actorJSON.put("photoURL", photoURL.split("\\?")[0]);
            
            deskThreadJSON.put("actor", actorJSON);
            
            threads.put(deskThreadJSON);
        }
       
        JSONObject deskSyncDataObject = new JSONObject();
        deskSyncDataObject.put("tickets", tickets);
        deskSyncDataObject.put("threads", threads);
        
        JSONObject deskSyncObject = new JSONObject();
        deskSyncObject.put("data", deskSyncDataObject);
        
        return deskSyncObject;
    }
    
    private JSONObject convertGitHubIssueAsDeskTicket(JSONObject githubPaylodData) throws JSONException {

    	String repo_name = githubPaylodData.getJSONObject("repository").getString("name");
    	String repo_owner = githubPaylodData.getJSONObject("repository").getJSONObject("owner").getString("login");

    	JSONObject issue = githubPaylodData.getJSONObject("issue");
    	JSONObject issue_author = issue.getJSONObject("user");
    	String authorName = issue_author.getString("login"); 
    	String photoURL = issue_author.getString("avatar_url");
    	String issueNumber = issue.getString("number");

    	JSONObject deskTicketJSON = new JSONObject();
    	deskTicketJSON.put("extId", repo_name+":-:"+issueNumber+":-:"+repo_owner);
    	deskTicketJSON.put("subject", repo_name+" - "+"#"+issueNumber+" "+issue.getString("title"));
    	deskTicketJSON.put("description", issue.getString("body"));
    	deskTicketJSON.put("createdTime", issue.getString("created_at"));
    	deskTicketJSON.put("extra", getIssueExtraMetaJSON(issue));

    	JSONObject actorJSON = new JSONObject();
    	actorJSON.put("name", authorName);
    	actorJSON.put("displayName", authorName+" (Github)");
    	actorJSON.put("extId", authorName);
    	actorJSON.put("photoURL", photoURL.split("\\?")[0]);

    	deskTicketJSON.put("actor", actorJSON);

    	return deskTicketJSON;
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
    
    private JSONObject getIssueExtraMetaJSON(JSONObject githubIssue) {
    	try {
    		
			JSONObject extraValueJSON = new JSONObject();
				extraValueJSON.put("node_id", githubIssue.getString("node_id"));
				extraValueJSON.put("number", githubIssue.getString("number"));
				extraValueJSON.put("labels", githubIssue.getString("labels"));
				extraValueJSON.put("state", githubIssue.getString("state"));
				extraValueJSON.put("locked", githubIssue.getString("locked"));
				extraValueJSON.put("milestone", githubIssue.getString("milestone"));
				extraValueJSON.put("assignee", githubIssue.getString("assignee"));
				extraValueJSON.put("assignees", githubIssue.getString("assignees"));
				extraValueJSON.put("closed_at", githubIssue.getString("closed_at"));
				extraValueJSON.put("author_association", githubIssue.getString("author_association"));
				
			JSONObject extraJSON = new JSONObject();
				extraJSON.put("key", "{{ticket.id}}_channel_details");
				extraJSON.put("queriableValue", "github_ticket_extras");
				extraJSON.put("value", extraValueJSON);
				
			return extraJSON;
    	}
		catch (JSONException e) {
			LOGGER.error("error on getCommentExtraMetaJSON", e);
			return null;
		}
    }
}