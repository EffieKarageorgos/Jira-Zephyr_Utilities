package oracle.eloqua.standalone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import HTTPClient.HTTPResponse;
import HTTPClient.NVPair;
import HTTPClient.URI;
import oracle.jira.JIRAHttpConnection;


public class CreateAndPopulateTestExecCycle {
	private final String ORACLEJIRAURL = "https://jira.oraclecorp.com";
//	private final String ORACLEJIRAURL = "https://jira-uat.us.oracle.com";
	
	private final String ORACLEGENERICUSERNAME = "eloquaunassigned_ar@oracle.com";
	private final String ORACLEGENERICUSERPASSWORD = "Cdv631311";
	
	private final String PROJECTID = "12237";
	private final String JIRAQUERYMAXRESULT = "300";
	
	private final int JIRATIMEOUT = 30000;
	
	
	private static JIRAHttpConnection jiraClient = null;
	private HTTPResponse response = null;
	
	public CreateAndPopulateTestExecCycle(){
		try {
			jiraClient = new JIRAHttpConnection(new URI(ORACLEJIRAURL), ORACLEGENERICUSERNAME, ORACLEGENERICUSERPASSWORD.toCharArray(), "JIRA");
			jiraClient.setTimeout(JIRATIMEOUT);
		} catch (Exception e) {
			System.out.println("Failed to create Jira client in CreateAndPopulateTestExecCycleOLD with error message: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	//Purpose: Get the list of all the test cycles defined for a specific Jira project - Jira project version combination
	//If needed in future, it could have an argument to specify, if the execution summaries should be included in the response (add + "&expand=executionSummaries" in request) or not
	public JSONObject getTestCyclesFromZephyrJiraForProjectAndVersion(String projectVersionId) {
		String responseData = "";
		
		try {
			String request = "jira/rest/zapi/latest/cycle?projectId=" + PROJECTID + "&versionId=" + projectVersionId;
			HTTPResponse response = jiraClient.Get(request);
			int statusCode = response.getStatusCode();
			
			responseData = new String(response.getData());
			JSONObject responseDataJO = new JSONObject(responseData);
			
			if(statusCode != 200 ){
				System.out.println("Failed get call to JIRA in getTestCyclesFromZephyrJiraForProjectAndVersion with status code: " + statusCode);;
				System.out.println("Failed get call to JIRA in getTestCyclesFromZephyrJiraForProjectAndVersion with response: " + responseData);
				return responseDataJO;
			}
			return responseDataJO;
		} catch (Exception e) {
			System.out.println("Failed to create Jira client with error message: " + e.getMessage());
			e.printStackTrace();
			JSONObject responseDataJO = new JSONObject();
			return responseDataJO;
		}
		finally {
		}		
//		return responseDataJO;
	}
	
	
	//Purpose: Find the cycle id that matches a cycle name provided a list of cycles for a specific project & version combination
	public String findTestCycleIdByName(JSONObject testCycles, String cycleName) {
		//System.out.println("In findTestCycleIdByName ");
		
		String cycleId = "";
		if(testCycles==null || testCycles.toString().isEmpty()) {
			return cycleId;
		}
		
		try {
			JSONArray testCyclesArray = testCycles.names();
			//System.out.println("In findTestCycleIdByName " + testCyclesArray.toString());
			
			if(testCyclesArray!=null && testCyclesArray.length() > 0) {
				Boolean keeplooking = true;
				int x = 0;
				while (keeplooking & x < testCyclesArray.length()) {
					String arrayElement = testCyclesArray.get(x).toString();
					x = x+1;
					if (!arrayElement.matches("-1") & !arrayElement.matches("recordsCount")) {
						JSONObject cycle = testCycles.getJSONObject(arrayElement);
						String cycleNameInArray = cycle.getString("name");
						//System.out.println("cycle: " + cycle.toString());
						if (cycleNameInArray.compareTo(cycleName)==0) {
							//System.out.println("FOUND: " + cycleNameInArray);
							cycleId = arrayElement;
							keeplooking = false;
						}
					}
				}
				if (x == testCyclesArray.length()) {
					System.out.println("Test Cycle NOT FOUND: " + cycleName);
				}
			}
		} catch (Exception e) {
			System.out.println("Failed to create Jira client with error message: " + e.getMessage());
			e.printStackTrace();
		}
		return cycleId;
	}
		
	//Purpose: Create an execution cycles for a specific Jira project - Jira project version combination
	public String createTestCycle(String projectVersionId, String cycleName) {
		try {
			System.out.println("In createTestCycle");
			JSONObject jo = new JSONObject();
			jo.accumulate("name", cycleName);
			jo.accumulate("description", cycleName);
			jo.accumulate("projectId", PROJECTID);
			jo.accumulate("versionId", projectVersionId);
	
			System.out.println(jo.toString());
			String postData = jo.toString();
			
			NVPair[] headers = {new NVPair("Content-Type", "application/json")};
			HTTPResponse response = jiraClient.Post("/jira/rest/zapi/latest/cycle", postData.getBytes(), headers);
			int statusCode = response.getStatusCode();
	
			String responseData = new String(response.getData());
			//System.out.println(responseData);
		
			if(statusCode != 200 ){
				System.out.println("Failed get call to JIRA in createTestCycle with status code: " + statusCode);
				System.out.println("Failed get call to JIRA in createTestCycle with response: " + responseData);
				return "";
			}
		} catch (Exception e) {
			System.out.println("Failed to create Jira client with error message: " + e.getMessage());
			e.printStackTrace();
			return "";
		}
		finally {
		}
		return "";
	}
	
	//Purpose: Given a Jira filter Id, retrieve the filter condition for the filter
	//If the Jira filter not shared with everyone, this method will fail in execution.
	public String retrieveFilterConditionForFilter(String filterId) {
		System.out.println("In retrieveFilterConditionForFilter");
		String responseData ="";
		try {
			String request = "/jira/rest/api/latest/filter/" + filterId +"?expand=jql";
			HTTPResponse response = jiraClient.Get(request);
			int statusCode = response.getStatusCode();
			
			responseData = new String(response.getData());
			
			if(statusCode != 200 ){
				System.out.println("Failed get call to JIRA in retrieveFilterConditionForFilter with status code: " + statusCode);;
				System.out.println("Failed get call to JIRA in retrieveFilterConditionForFilter with response: " + responseData);
				return "";
			}
			if(statusCode == 200 ) {
				JSONObject obj = new JSONObject(responseData);
		
				if(responseData!=null && !responseData.isEmpty()) {
					//System.out.println(responseData);
					String jql = obj.getString("jql");
					//System.out.println(jql);
					return (jql);
				}
			}
		} catch (Exception e) {
			System.out.println("Failed to create Jira client with error message: " + e.getMessage());
			e.printStackTrace();
			return "";
		}
		finally {
		}
		return "";
	}

	
	//Purpose: Post a search issue request to Jira using a jql filter condition
	//If the Jira filter is not shared with everyone, this method will fail in execution.
	public String applyJQLFilter (String jql) {
		System.out.println("In applyJQLFilter");
		try {
			NVPair[] headers = {new NVPair("Content-Type", "application/json")};
				
			JSONArray fields = new JSONArray();
			fields.put("key");
	
			JSONObject jo = new JSONObject();
			jo.accumulate("jql", jql);
			jo.accumulate("maxResults", JIRAQUERYMAXRESULT);
			jo.accumulate("fields", fields);
					
			String postData = jo.toString();
//			System.out.println("postData " + postData);
			HTTPResponse query = jiraClient.Post("/jira/rest/api/latest/search", postData.getBytes(), headers);
			int statusCode = query.getStatusCode();
			
			String queryResults = new String(query.getData());
			
			if(statusCode != 200 ){
				System.out.println("Failed get call to JIRA with status code: " + statusCode);;
				System.out.println("Failed get call to JIRA with response: " + queryResults);
				return "";
			}
			if(statusCode == 200 ) {
//				System.out.println("Query results " + queryResults);
				return (queryResults);
			
			}
		} catch (Exception e) {
			System.out.println("Failed to create Jira client with error message: " + e.getMessage());
			e.printStackTrace();
			return "";
		}
		finally {
		}
		return "";
	}
	
	//Purpose: Add a list of test cases to a specified test execution cycle using ZAPI REST API POST call
	public String addTestsToCycle (String cycleId, JSONArray testcases, String projectVersionId) {
		System.out.println("In add Tests to Cycle");
		try {
			NVPair[] headers = {new NVPair("Content-Type", "application/json")};
				
			JSONObject addTtoCyclePost = new JSONObject();
			addTtoCyclePost.accumulate("cycleId", cycleId);
			addTtoCyclePost.accumulate("projectId", PROJECTID);
			addTtoCyclePost.accumulate("versionId", projectVersionId);
			addTtoCyclePost.accumulate("issues", testcases);
					
			String addTtoCyclePostData = addTtoCyclePost.toString();
			System.out.println("addTtoCyclePostData " + addTtoCyclePostData);
				
			HTTPResponse responseAddToCycle = jiraClient.Post("/jira/rest/zapi/latest/execution/addTestsToCycle", addTtoCyclePostData.getBytes(), headers);
			int statusCode = responseAddToCycle.getStatusCode();
			
			if(statusCode != 200 ){
				System.out.println("In add Tests to Cycle: Failed post call to JIRA with status code: " + statusCode);;
				System.out.println("In add Tests to Cycle: Failed post call to JIRA with response: " + responseAddToCycle.getData());
				System.out.println("In add Tests to Cycle: Failed to add tests to cycle " + cycleId + "\n");
				return "";
			}
			if(statusCode == 200 ) {
				System.out.println("Succefully added tests to cycle " + cycleId + "\n");
				return "";
			}
		} catch (Exception e) {
			System.out.println("Failed to create Jira client with error message: " + e.getMessage());
			e.printStackTrace();
			return "";
		}
		finally {
		}
		return "";
	}
	
	//Purpose: Populate a test execution cycles for a specific Jira project - Jira project version combination using a Jira filter
	//This method can be called many times without overriding any existing test executions in the cycle
	//
	//For the listed test execution cycle, the filter condition of its mapped filter id is extracted using Jira REST API
	//The retrieved JQL condition is used to post a search request (using Jira REST API) to get a list of test cases that 
	// get added to the test execution cycle.
	public String populateTestCycle(String projectVersionId, String cycleName, String cycleId, String filterId) {
		try {
			System.out.println("In populateTestCycle before getting the list of cycles");
			
			//Retrieve the filter info for the specified filter Id
			String jql = retrieveFilterConditionForFilter(filterId);
			
			if (cycleId.isEmpty() || jql.isEmpty()) {
				if (cycleId.isEmpty()) {
					System.out.println("The cycle " + cycleName + " does not exist - no cycle id is returned. Please create it first.");
				}
				if (jql.isEmpty()) {
					System.out.println("No jql condition was returned for filter with filter id  " + filterId + " while trying to populate cycle " + cycleName + ".");
				}
			} else {
				System.out.println("Populating " + cycleName + " - cycle Id: " + cycleId + " cycle name with test cases returned by filter with id " + filterId);
				//System.out.println(jql + "\n");
				
				//Get test case issues returned by filter condition
				String queryResults = applyJQLFilter(jql);
				
				//Extract test case issue keys formatted in a string list, e.g. ["key1", "key2", ...]
				JSONObject obj1 = new JSONObject(queryResults);
				JSONArray issues = obj1.getJSONArray("issues");
				//System.out.println("Issues " + issues.toString());
				
				JSONArray issuekeys = new JSONArray();
				
				for(int i = 0; i < issues.length(); i++){
					String issuekey = issues.getJSONObject(i).getString("key");
					issuekeys.put(issuekey);
				}
				
				addTestsToCycle (cycleId, issuekeys, projectVersionId);
			}	
	} catch (Exception e) {
			System.out.println("Failed to create Jira client with error message: " + e.getMessage());
			e.printStackTrace();
			return "";
	}
	finally {
	}
	return "";
	}


	//Purpose: Returns the count of the issues returned by a Jira filter
	//If the Jira filter is not shared with everyone, this method will fail in execution.
	public String getIssueCountForJQLFilter (String filterId) {
		System.out.println("In getIssueCountForJQLFilter");
		try {
			String request = "/jira/rest/zapi/latest/test/mySearches/" + filterId +"/";
			System.out.println(request);
			HTTPResponse queryresponse = jiraClient.Get(request);
			int statusCode = queryresponse.getStatusCode();
			String queryResults = new String(queryresponse.getData());
			
			if(statusCode != 200 ){
				System.out.println("Failed get call to JIRA with status code: " + statusCode);;
				System.out.println("Failed get call to JIRA with response: " + queryResults);
				return "";
			}
			if(statusCode == 200 ) {
				System.out.println("Query results " + queryResults);
				return (queryResults);
			}
			
		} catch (Exception e) {
			System.out.println("Failed to create Jira client with error message: " + e.getMessage());
			e.printStackTrace();
			return "";
		}
		finally {
		}
		return "";
	}
	
	
}