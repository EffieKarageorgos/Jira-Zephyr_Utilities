package oracle.eloqua.standalone;

import java.util.Iterator;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import HTTPClient.HTTPResponse;
import HTTPClient.NVPair;
import oracle.jira.JIRAHttpConnection;


public class JiraTestCycleManager {
	private static JIRAHttpConnection jiraClient = null;
	private HTTPResponse response = null;
	private JSONObject allJiraTestCycles = null;
	private String PROJECTID = null;
	private String PROJECTVERSIONID = null;
	private String JIRAQUERYMAXRESULT = null;

	
	public JiraTestCycleManager(String projectId, String projectVersionId, String jiraQueryMaxResult){
		this.PROJECTID = projectId;
		this.PROJECTVERSIONID = projectVersionId;
		this.JIRAQUERYMAXRESULT = jiraQueryMaxResult;
		jiraClient = JiraConnectionManager.getJiraClient();
		retrieveJiraTestCycles();
	}
	
	public JSONObject getAllJiraTestCycles() {
		return allJiraTestCycles;
	}

	public void setAllJiraTestCycles(JSONObject allJiraTestCycles) {
		this.allJiraTestCycles = allJiraTestCycles;
	}
	
	/**
	 * @param projectId
	 * @param projectVersionId
	 * @return = returns a json object containing all the cycles for a particular project id and version.
	 */
	public void retrieveJiraTestCycles(){
		try {
			String request = "jira/rest/zapi/latest/cycle?projectId=" + PROJECTID + "&versionId=" + PROJECTVERSIONID;
			response = jiraClient.Get(request);
			int statusCode = response.getStatusCode();
			
			if(statusCode == 200)
				setAllJiraTestCycles(new JSONObject(new String(response.getData())));
			else
				System.out.println("The GET request to JIRA failed with status code: " + statusCode
						+ "\n and response as: " + new String(response.getData()));
		} catch (Exception e) {
			System.out.println("Failure occured on JIRA GET request/converting response into JSON object: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * @param allJiraTestCycles = all existing test cycles based on project id and version.
	 * @param jiraTestCycleNameToLookup = the name of particular test that we are looking up if exists or not
	 * @return = true - if cycle already exists else returns false.
	 */
	private Boolean doesJiraCycleAlreadyExists(JSONObject allJiraTestCycles, String jiraTestCycleNameToLookup){
		Boolean doesJiraCycleExists = false;
		if(allJiraTestCycles == null || allJiraTestCycles.toString().isEmpty())
			return doesJiraCycleExists;
		else{
			try {
				allJiraTestCycles.remove("recordsCount");
				Iterator allCycleIds = allJiraTestCycles.keys();
				while(allCycleIds.hasNext()){
					String key = (String) allCycleIds.next();
					JSONObject cycleInfo = allJiraTestCycles.getJSONObject(key);
					System.out.println(cycleInfo.get("name"));
					if(jiraTestCycleNameToLookup.equals(cycleInfo.get("name"))){
						doesJiraCycleExists = true;
						break;
					}
				}
			} catch (Exception e) {
				System.out.println("A JSON exception occured while accessing the JSONObject. Please refer the stack:");
				e.printStackTrace();
			}
		}
		return doesJiraCycleExists;
	}
	
	/**
	 * @param cycleName
	 * @param filterId
	 * @param jiraTestCycles == can be set to null, if there is no need to check if a cycle already exists before creating a new one with the same name.
	 * @return == newly created Jira cycle.
	 */
	public JiraTestCycle createJiraTestCycle(String cycleName, String filterId){
		String cycleId = null;
		JiraTestCycle jiraTestCycle = null;
		JSONObject allJiraTestCycles = getAllJiraTestCycles();
		try{
			NVPair[] headers = {new NVPair("Content-Type", "application/json")};
			JSONObject jo = new JSONObject();
			jo.accumulate("name", cycleName);
			jo.accumulate("description", cycleName);
			jo.accumulate("projectId", PROJECTID);
			jo.accumulate("versionId", PROJECTVERSIONID);
	
			System.out.println(jo.toString());
			String postData = jo.toString();

			if((allJiraTestCycles != null && !doesJiraCycleAlreadyExists(allJiraTestCycles, cycleName)) ||
					(allJiraTestCycles == null)){
				response = jiraClient.Post("/jira/rest/zapi/latest/cycle", postData.getBytes(), headers);
				int statusCode = response.getStatusCode();
				if(statusCode == 200){
					JSONObject respObject = new JSONObject(new String(response.getData()));
					cycleId = respObject.getString("id");
					System.out.println(respObject);
					jiraTestCycle = new JiraTestCycle(cycleName, filterId, cycleId);
				}
				else{
					System.out.println("The POST request to JIRA failed with status code: " + statusCode
							+ "\n and response as: " + new String(response.getData()));
				}
			}
			else{
				System.out.println("Test execution cycle " + cycleName + " already exists, therefore new one will not be created with the same name");
			}
		}
		catch(Exception e){
			System.out.println("Please refer the stack:");
			e.printStackTrace();
		}
		return jiraTestCycle;
	}
	
	/**
	 * @param filterId
	 * @return
	 * Purpose: Given a Jira filter Id, retrieve the filter condition for the filter
	 * If the Jira filter not shared with everyone, this method will fail in execution.
	 */
	public String retrieveFilterQuery(String filterId){
		String filterQuery = null;
		try {
			String request = "/jira/rest/api/latest/filter/" + filterId +"?expand=jql";
			response = jiraClient.Get(request);
			int statusCode = response.getStatusCode();
			if(statusCode == 200){
				JSONObject obj = new JSONObject(new String(response.getData()));
				filterQuery = obj.getString("jql");
			}
			else{
				System.out.println("The GET request to JIRA failed with status code: " + statusCode
						+ "\n and response as: " + new String(response.getData()));
			}
		} catch (Exception e) {
			System.out.println("Please refer the stack:");
			e.printStackTrace();
		}
		return filterQuery;
	}
	
	/**
	 * @param filterJQL
	 * @return
	 * Purpose: Post a search issue request to Jira using a jql filter condition
	 * If the Jira filter is not shared with everyone, this method will fail in execution.
	 */
	public String retrieveFilterQueryOutcome (String filterJQL) {
		String filterQueryOutcome = null;
		try {
			if(filterJQL != null){
				NVPair[] headers = {new NVPair("Content-Type", "application/json")};
	
				JSONArray fields = new JSONArray();
				fields.put("key");
		
				JSONObject jo = new JSONObject();
				jo.accumulate("jql", filterJQL);
				jo.accumulate("maxResults", JIRAQUERYMAXRESULT);
				jo.accumulate("fields", fields);
						
				String postData = jo.toString();
				response = jiraClient.Post("/jira/rest/api/latest/search", postData.getBytes(), headers);
				int statusCode = response.getStatusCode();
				if(statusCode == 200){
					filterQueryOutcome = new String(response.getData());
				}
				else{
					System.out.println("The POST request to JIRA failed with status code: " + statusCode
							+ "\n and response as: " + new String(response.getData()));
				}
			}
			else{
				System.out.println("The input JQL for the filter was null and hence no test cases were added to the cycle for this particular filter id");
			}
		} catch (Exception e) {
			System.out.println("Please refer the stack:");
			e.printStackTrace();
		}
		return filterQueryOutcome;
	}
	
	/**
	 * @param jiraTestCycle
	 * 	Purpose: Populate the test execution cycles for a specific Jira project - Jira project version combination using Jira filters
	 * 	The matching cycleName, filterId and Cycle id is defined in a CycleAndFilterId list
	 *	This method can be called many times without overriding any existing test executions in the cycle
	 *	If the cycle id does not exist - aka the cycle is not created yet, the cycle will be skipped and a warning will be printed
	 *
	 *	For each listed test execution cycle, the filter condition of its mapped filter id is extracted using Jira REST API
	 *	The retrieved JQL condition is used to post a search request (using Jira REST API) to get a list of test cases that 
	 *	get added to the test execution cycle.
	 */
	public void populateTestCycleWithTestCases(JiraTestCycle jiraTestCycle){
		try {
			if(jiraTestCycle != null){
				NVPair[] headers = {new NVPair("Content-Type", "application/json")};
				
				String filterJQL = retrieveFilterQuery(jiraTestCycle.getFilterId());
				String filterJQLResult = retrieveFilterQueryOutcome(filterJQL);
				JSONObject filterJQLResultObj = new JSONObject(filterJQLResult);
				JSONArray issues = filterJQLResultObj.getJSONArray("issues");
				JSONArray testCaseIds = new JSONArray();
				
				for(int i = 0; i < issues.length(); i++){
					String issueKey = issues.getJSONObject(i).getString("key");
					testCaseIds.put(issueKey);
				}
				
				JSONObject addTtoCyclePost = new JSONObject();
				addTtoCyclePost.accumulate("cycleId", jiraTestCycle.getCycleId());
				addTtoCyclePost.accumulate("projectId", PROJECTID);
				addTtoCyclePost.accumulate("versionId", PROJECTVERSIONID);
				addTtoCyclePost.accumulate("issues", testCaseIds);
						
				String addTtoCyclePostData = addTtoCyclePost.toString();
					
				response = jiraClient.Post("/jira/rest/zapi/latest/execution/addTestsToCycle", addTtoCyclePostData.getBytes(), headers);
				int statusCode = response.getStatusCode();
				if(statusCode == 200){
					System.out.println("Succefully added tests to cycle " + jiraTestCycle.getCycleId() + "\n");
				}
				else{
					System.out.println("The POST request to JIRA failed with status code: " + statusCode
							+ "\n and response as: " + new String(response.getData()));
					System.out.println("Failed to add tests to cycle " + jiraTestCycle.getCycleId() + "\n");
				}
			}
			else{
				System.out.println("Since the input jiraTestCycle was null (as cycle already existed), therefore no test cases were added to it!!!");
			}
		} catch (Exception e) {
			System.out.println("Please refer the stack:");
			e.printStackTrace();
		}
	}
}