//$Id$
package com.yesiamvj.desk.github;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yesiamvj.desk.ResponseUtility;

@RestController
@EnableAutoConfiguration
public class RedirectHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RedirectHandler.class);

    @RequestMapping(value = "/github/redirect", method= RequestMethod.GET)
    public ResponseEntity<String>  process(
    		@RequestParam("id") String id,
    		@RequestParam("entity") String entity,
    		@RequestParam(value = "parentId", required = false) String parentId) {
    	try {
    		String link = "https://github.com";
    	    switch (entity) {
    	        case "user_profile":
    	        	link += "/"+ id;
    	            break;
    	        case "ticket":
    	            link = getIssueURL(id);
    	        break;
    	        case "thread":
    	            link = getIssueCommentURL(parentId, id);
    	        break;
    	        default:
    	            break;
    	    }
    	    HttpHeaders responseHeaders = new HttpHeaders();
        	responseHeaders.setLocation(new URI(link));
        	return new ResponseEntity<String>(null, responseHeaders, HttpStatus.TEMPORARY_REDIRECT);
    	
    	} catch (URISyntaxException e) {
    		LOGGER.error("error on process", e);
			return ResponseUtility.sendErrorResult("Problem with the server");
		}
    }
    
    private static String getIssueURL(String externalId){
        return _getIssueURL(getExternalIdMap(externalId));
    }
    
    private static String getIssueCommentURL(String issueId, String commentId){
        return _getIssueCommentURL(getExternalIdMap(issueId), commentId);
    }
    
    private static String[] getExternalIdMap(String externalId){
        return externalId.split(":-:");
    }
    
    private static String _getUserProfileURL(String[] external_id_map){
        String author_name = external_id_map[2];
        return "https://github.com/"+author_name;
    }
    
    private static String _getRepositoryURL(String[] external_id_map){
    	String repo_name = external_id_map[0];
        return _getUserProfileURL(external_id_map)+"/"+repo_name;
    }
    
    private static String _getIssueURL(String[] external_id_map){
    	String issue_number = external_id_map[1];
        return _getRepositoryURL(external_id_map)+"/issues/"+issue_number;
    }
    
    private static String _getIssueCommentURL(String[] external_id_map, String comment_id){
        return _getIssueURL(external_id_map)+"#issuecomment-"+comment_id;
    }
}
