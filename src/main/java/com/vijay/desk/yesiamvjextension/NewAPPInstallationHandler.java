//$Id$
package com.vijay.desk.yesiamvjextension;

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

@RestController
@EnableAutoConfiguration
public class NewAPPInstallationHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NewAPPInstallationHandler.class);

    @RequestMapping(value = "/github/initializeIntegration", method= RequestMethod.POST)
    public ResponseEntity<String>  process(@RequestBody String payload) {
    	try {
    		
			JSONObject payloadJSON = new JSONObject(payload);
			String orgId = payloadJSON.getString("orgId");
			String securityContext = payloadJSON.getString("securityContext");
			
			LOGGER.info(" initializeIntegration user info callback ... "+orgId);
			
			String gitHubUserAPIURL = "https://api.github.com/user";
			JSONObject userAPIresponse = DeskUtility.callDeskInvokeAPI(orgId, securityContext, gitHubUserAPIURL, RequestMethod.GET, null);
			String githubUserName = userAPIresponse.getJSONObject("statusMessage").getString("login");
			
			JSONObject integCredentialsValueJSON = new JSONObject();
				integCredentialsValueJSON.put("deskOrgId", orgId);
				integCredentialsValueJSON.put("deskSecurityContext", securityContext);
				integCredentialsValueJSON.put("githubUsername", githubUserName);
				
			JSONObject extnStorageObject = new JSONObject();
				extnStorageObject.put("key", "credentials");
				extnStorageObject.put("queriableValue", "appInitConfig");
				extnStorageObject.put("value", integCredentialsValueJSON);
				
			String extensionStorageAPIURL = DeskUtility.DESK_API_ROOT+"/api/v1/installedExtensions/{{installationId}}/storage";
			
			DeskUtility.callDeskInvokeAPI(orgId, securityContext, extensionStorageAPIURL, RequestMethod.POST, extnStorageObject);
			
	    	return ResponseUtility.sendSuccessResult(null);
    	
    	} catch (JSONException e) {
    		LOGGER.error("error on process", e);
			return ResponseUtility.sendErrorResult("Problem with the server");
		}
    }
}
