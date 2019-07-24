//$Id$
package com.yesiamvj.desk.youtube;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YoutbeDeskConverstionUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(YoutbeDeskConverstionUtil.class);
	
	public static JSONObject convertYoutubeCommentResponseForDesk(String channelId, JSONObject youtubeResponse) throws JSONException {
		if(youtubeResponse.getString("type").equals("commentReply")) {
			return convertCommentReplyForDesk(channelId, youtubeResponse);
		}
		else {
			return convertParentCommentForDesk(channelId, youtubeResponse);
		}
	}
	
	public static JSONObject convertCommentAsTicketForDesk(JSONObject youtubeResponse){
		try {
	        JSONObject topSnippet = youtubeResponse.getJSONObject("snippet");
	        JSONObject topLevelComment=  topSnippet.getJSONObject("topLevelComment");
	        JSONObject topLevelCommentSnippet = topLevelComment.getJSONObject("snippet");
	        String videoId = topSnippet.getString("videoId");
	        String commentId = youtubeResponse.getString("id");
	        String externalId = videoId+":-:"+commentId;
	        
	        JSONObject ticketJSON = new JSONObject()
		        						.put("subject", topLevelCommentSnippet.getString("textOriginal"))
								        .put("createdTime", topLevelCommentSnippet.getString("updatedAt"))
								        .put("extId", externalId)
								        .put("actor", 
								        		new JSONObject()
			        							.put("extId", topLevelCommentSnippet.getJSONObject("authorChannelId").getString("value"))
			        							.put("name", topLevelCommentSnippet.getString("authorDisplayName"))
			        							.put("photoURL", topLevelCommentSnippet.getString("authorProfileImageUrl")));
	        return ticketJSON;
		}
		catch(Exception e) {
			LOGGER.error("error on process", e);
		}
		return null;
    }
	
	public static JSONObject convertCommentReplyForDesk(String channelId, JSONObject youtubeResponse){
		try {
	        String ticket_convert_type = "video_ticket";
	        if(youtubeResponse==null){
	            return null;
	        }
	        JSONObject topLevelCommentSnippet = youtubeResponse.getJSONObject("snippet");
	        String contentType = "text/plain";
	        if(!topLevelCommentSnippet.getString("textOriginal").equals(topLevelCommentSnippet.getString("textDisplay"))){
	            contentType = "text/html";
	        }
	        String videoId = topLevelCommentSnippet.getString("videoId");
	        String commentId = youtubeResponse.getString("id");
	        String parentCommentId = topLevelCommentSnippet.getString("parentId");
	        String externalParentId = ticket_convert_type.equals("comment_ticket") ? videoId+":-:"+parentCommentId : parentCommentId; 
	        String direction = channelId.equals(topLevelCommentSnippet.getJSONObject("authorChannelId").getString("value")) ? "out" : "in";
	        
	        JSONObject threadJSON = new JSONObject()
	        						.put("extId", commentId)
							        .put("extParentId", externalParentId)
							        .put("createdTime", topLevelCommentSnippet.getString("updatedAt"))
							        .put("direction",direction)
							        .put("content", topLevelCommentSnippet.getString("textOriginal"))
							        .put("contentType", contentType)
							        .put("canReply", false)
	        						.put("actor",
	        								new JSONObject()
	        								.put("extId", topLevelCommentSnippet.getJSONObject("authorChannelId").getString("value"))
	        								.put("name", topLevelCommentSnippet.getString("authorDisplayName"))
	        								.put("photoURL", topLevelCommentSnippet.getString("authorProfileImageUrl")))
	        						.put("extra",
	        								new JSONObject()
	    					        		.put("key", "{{thread.id}}_channel_details")
	    							        .put("queriableValue", "youtube_thread_extras")
	    							        .put("value", 
	    							        		new JSONObject()
	    							        		.put("likeCount", topLevelCommentSnippet.getString("likeCount"))
	    											.put("totalReplyCount", 0)
	    											.put("canReply", false)
	    							        		.put("isPublic", true)
	    							        		.put("moderationStatus", topLevelCommentSnippet.getString("moderationStatus"))));
	        return threadJSON;
		}
		catch(Exception e) {
			LOGGER.error("error on process", e);
		}
		return null;
    }
	
	public static JSONObject convertParentCommentForDesk(String channelId, JSONObject youtubeResponse){
		try {
	        String ticket_convert_type = "video_ticket";
	        if(youtubeResponse==null){
	            return null;
	        }
	        JSONObject topSnippet = youtubeResponse.getJSONObject("snippet");
            JSONObject topLevelComment= topSnippet.getJSONObject("topLevelComment");
            JSONObject topLevelCommentSnippet = topLevelComment.getJSONObject("snippet");
            
	        String contentType = "text/plain";
	        if(!topLevelCommentSnippet.getString("textOriginal").equals(topLevelCommentSnippet.getString("textDisplay"))){
	            contentType = "text/html";
	        }
	        String videoId = topSnippet.getString("videoId");
	        String commentId = youtubeResponse.getString("id");
	        String externalParentId = ticket_convert_type.equals("comment_ticket") ? videoId+":-:"+commentId : videoId; 
	        String direction = channelId.equals(topLevelCommentSnippet.getJSONObject("authorChannelId").getString("value")) ? "out" : "in";
	        
	        JSONObject threadJSON = new JSONObject()
	        						.put("extId", commentId)
							        .put("extParentId", externalParentId)
							        .put("createdTime", topLevelCommentSnippet.getString("updatedAt"))
							        .put("direction",direction)
							        .put("content", topLevelCommentSnippet.getString("textOriginal"))
							        .put("contentType", contentType)
							        .put("canReply", topSnippet.getBoolean("canReply"))
	        						.put("actor",
	        								new JSONObject()
	        								.put("extId", topLevelCommentSnippet.getJSONObject("authorChannelId").getString("value"))
	        								.put("name", topLevelCommentSnippet.getString("authorDisplayName"))
	        								.put("photoURL", topLevelCommentSnippet.getString("authorProfileImageUrl")))
	        						.put("extra",
	        								new JSONObject()
	    					        		.put("key", "{{thread.id}}_channel_details")
	    							        .put("queriableValue", "youtube_thread_extras")
	    							        .put("value", 
	    							        		new JSONObject()
	    							        		.put("likeCount", topLevelCommentSnippet.getString("likeCount"))
	    											.put("totalReplyCount", topSnippet.getString("likeCount"))
	    											.put("canReply", topSnippet.getBoolean("canReply"))
	    							        		.put("isPublic", topSnippet.getBoolean("isPublic"))
	    							        		.put("moderationStatus", topLevelCommentSnippet.getString("moderationStatus"))));
	        return threadJSON;
		}
		catch(Exception e) {
			LOGGER.error("error on process", e);
		}
		return null;
    }
}
