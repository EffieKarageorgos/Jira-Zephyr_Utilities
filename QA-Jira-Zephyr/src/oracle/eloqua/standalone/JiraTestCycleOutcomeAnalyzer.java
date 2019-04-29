package oracle.eloqua.standalone;

import java.net.URLDecoder;
import java.net.URLEncoder;

import oracle.jira.JIRAHttpConnection;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import HTTPClient.HTTPResponse;
import HTTPClient.NVPair;
import HTTPClient.URI;
import HTTPClient.CookieModule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.*;




// TODO: This class still needs to be refactored
public class JiraTestCycleOutcomeAnalyzer {
	private static JIRAHttpConnection jiraClient = null;
	
	// REMOVE the line below after ALM-3156 Jira ticket is completed
	private static JIRAHttpConnection jiraClient2 = null;
	
	private final String ORACLECONFLUENCEURL = "https://confluence.oraclecorp.com";
	
	private final String ConfluenceHTLMacroHeader = "<ac:structured-macro ac:name=\"html\"><ac:plain-text-body><![CDATA[";
	private final String ConfluenceHTLMacroFooter = "]]></ac:plain-text-body></ac:structured-macro>";

	public JiraTestCycleOutcomeAnalyzer(){
		jiraClient = JiraConnectionManager.getJiraClient();
		// REMOVE the line below after ALM-3156 Jira ticket is completed
		jiraClient2 = JiraConnectionManager.getJiraClient2();
	}
	
	public List<String> getFirstColumnOfHTMLRow (String row) {
		List<String> colAndNewRow = new ArrayList<String>();
		
		int colStart=row.indexOf("<td");
		int colEnd=row.indexOf("</td>");
		String col = row.substring(colStart, colEnd+5);
		//System.out.println("Col: " + col);
		
		int colContentEnd=col.indexOf("</");
		String colContentWithStart = col.substring(1, colContentEnd);
		
		int colContentStart=colContentWithStart.lastIndexOf(">");
		String colContent = colContentWithStart.substring(colContentStart+1, colContentWithStart.length());
		//System.out.println("ColCont: " + colContent);
		
		String newRow = row.replace(col, "");
		
		colAndNewRow.add(colContent);
		colAndNewRow.add(newRow);
		return colAndNewRow;
	}
	
	public Map<String, String> parseHTMLTableString (String HTMLTable) {
		System.out.println("In parseHTMLTableString");
		//System.out.println("HTMLTable: " + HTMLTable);
		
		Map<String, String> FirstTwoTableColumns = new LinkedHashMap<>();
		
		try {
			if(HTMLTable !=null ) {
				int start=HTMLTable.indexOf("<table>");
				int end=HTMLTable.indexOf("</table>");
				
				String table = HTMLTable.substring(start, end+8);
				table = table.replaceAll("&nbsp;*", "");
				table = table.replaceAll("&amp;*", "&");
				
//				table = table.replaceAll("colspan=\"1\"", "");
				
				Boolean therearerows = true;
				while (therearerows) {
					int rowStart=table.indexOf("<tr>");
					int rowEnd=table.indexOf("</tr>");
					if (rowStart == - 1) {
						therearerows = false;
					} else {
						String row = table.substring(rowStart, rowEnd+5);
						table = table.replace(row, "");
						int headerRow=row.indexOf("</th>");
						if (headerRow == - 1) {
							//System.out.println("Row: " + row);
							
							List<String> colAndNewRow1 = getFirstColumnOfHTMLRow (row);
							String col1 = colAndNewRow1.get(0).trim();
							List<String> colAndNewRow2 = getFirstColumnOfHTMLRow (colAndNewRow1.get(1));
							String col2 = colAndNewRow2.get(0).trim();
							
							//System.out.println("Col1: " + col1 + " Col2: " + col2);
							
							FirstTwoTableColumns.put(col1, col2);
						} else {
							//System.out.println("Header Row: " + row);
						}
					}
				}
				return FirstTwoTableColumns;
			}
		} catch (Exception e) {
			System.out.println("Failed to HTML Table string: " + e.getMessage());
			e.printStackTrace();
			return FirstTwoTableColumns;
		}
		finally {
		}
		return FirstTwoTableColumns;
	}
	
	// Confluence API related methods
	//
	public String getStorageValueOfConfluencePage (String pageId) {
		String confluencePageStorageValue = "";
		System.out.println("In getStorageValueOfConfluencePage");
		try {
			CookieModule.setCookiePolicyHandler(null);
			JIRAHttpConnection confluenceClient = new JIRAHttpConnection(new URI(ORACLECONFLUENCEURL), "eloquaunassigned_ar@oracle.com", "Cdv631311".toCharArray(), "CONFLUENCE");
			
			String request = "/confluence/rest/api/content/" + pageId + "?expand=body.storage";
			HTTPResponse queryresponse = confluenceClient.Get(request);
		    
			int statusCode = queryresponse.getStatusCode();
			String queryResults = new String(queryresponse.getData());
				
			if(statusCode != 200 ){
					System.out.println("Failed get call to Confluence with status code: " + statusCode);;
					System.out.println("Failed get call to Confluence with response: " + queryResults);
					return confluencePageStorageValue;
			}
			if(statusCode == 200 ) {
				JSONObject bodyStorage = new JSONObject(queryResults).getJSONObject("body");
				JSONObject confluencePageStorageObj = bodyStorage.getJSONObject("storage");
				confluencePageStorageValue = confluencePageStorageObj.getString("value");
				
				return confluencePageStorageValue;
			}
		} catch (Exception e) {
			System.out.println("Failed to create Confluence client with error message: " + e.getMessage());
			e.printStackTrace();
			return confluencePageStorageValue;
		}
		finally {
		}
		return confluencePageStorageValue;
	}
	
	
	public int getVersionOfConfluencePage (String pageId) {
		try {
			System.out.println("In getVersionOfConfluencePage");
			
			CookieModule.setCookiePolicyHandler(null);
			JIRAHttpConnection confluenceClient = new JIRAHttpConnection(new URI(ORACLECONFLUENCEURL), "eloquaunassigned_ar@oracle.com", "Cdv631311".toCharArray(), "CONFLUENCE");
			
			String request = "/confluence/rest/api/content/" + pageId;
			HTTPResponse queryresponse = confluenceClient.Get(request);
		
			int statusCode = queryresponse.getStatusCode();
			String queryResults = new String(queryresponse.getData());
			
			if(statusCode != 200 ){
					System.out.println("Failed get call to confluence with status code: " + statusCode);;
					System.out.println("Failed get call to confluence with response: " + queryResults);
					return -1;
			}
			if(statusCode == 200 ) {
				JSONObject obj1 = new JSONObject(queryResults);
				JSONObject confluencePageVersionObj = obj1.getJSONObject("version");
				int confluencePageVersion = confluencePageVersionObj.getInt("number");
				return (confluencePageVersion);
			}
		} catch (Exception e) {
			System.out.println("Failed to create Confluence client in getVersionOfConfluencePage with error message: " + e.getMessage());
			e.printStackTrace();
			return -1;
		}
		finally {
		}
		return -1;
	}
	
	public String postStorageToConfluence (String storageValue, String confluenceSpace, String confluencePageId, String title) {
		try {
		if (storageValue.isEmpty()) {
			System.out.println("storageValue is empty: " + storageValue);
		} else {
			int confluencePageVersion = getVersionOfConfluencePage(confluencePageId);
			System.out.println("confluencePageVersion " + confluencePageVersion);
			
			if (confluencePageVersion != -1) {
				//Increase version
				int confluencePageVersionNext = confluencePageVersion +  1;
				System.out.println("confluencePageVersionNext " + confluencePageVersionNext);
	
				Map<String, String> spaceKey = new HashMap<>();
				spaceKey.put("key", confluenceSpace);
			
			
				JSONObject joStorage = new JSONObject();
				joStorage.accumulate("value", storageValue);
				joStorage.accumulate("representation", "storage");
			
				JSONObject joVersion = new JSONObject();
				joVersion.accumulate("number", confluencePageVersionNext);
				joVersion.accumulate("minorEdit", true);
			
				JSONObject joBody = new JSONObject();
				joBody.put("storage", joStorage);
			
				JSONObject jo = new JSONObject();
				jo.accumulate("id", confluencePageId);
				jo.accumulate("title", title);
				jo.accumulate("type", "page");
			
				jo.put("space", spaceKey);
				jo.put("version", joVersion);
				jo.put("body", joBody);
		
			
				//System.out.println(jo.toString());
				String joString = jo.toString().replace("\\/", "/");
				//System.out.println(joString);
			
				CookieModule.setCookiePolicyHandler(null);
				JIRAHttpConnection confluenceClient = new JIRAHttpConnection(new URI(ORACLECONFLUENCEURL), "eloquaunassigned_ar@oracle.com", "Cdv631311".toCharArray(), "CONFLUENCE");
			
				NVPair[] headers = {new NVPair("Content-Type", "application/json")};
		
				HTTPResponse response = confluenceClient.Put("/confluence/rest/api/content/" + confluencePageId, joString.getBytes(), headers);
				int statusCode = response.getStatusCode();
				String putResults = new String(response.getData());
				
				if(statusCode != 200 ){
					System.out.println("Failed get call to Confluence in postStorageToConfluence with status code: " + statusCode);;
					System.out.println("Failed get call to Confluence in postStorageToConfluence with response: " + putResults);
					return "";
				}
				if(statusCode == 200 ) {
					//System.out.println("Put results " + putResults);
					System.out.println("Posted results succesfully to Confluence");
					return "";
				}
			} else {
				System.out.println("Confluence version could not be retrieved properly; Results did not get posted");
				return "";
			}
		}		
		} catch (Exception e) {
			System.out.println("Failed to create Confluence client in postStorageToConfluence with error message: " + e.getMessage());
			e.printStackTrace();
			return "";
		}
		finally {
		}
		return "";
	}
	
	
	// Jira API related methods
	//
	public int updateConditionForJQLFilter (String filterId, String fromS, String toS) {
		String getResults ="";
		try {
			System.out.println("In updateConditionForJQLFilter: " + filterId);
			
			String request = "/jira/rest/api/latest/filter/" + filterId +"?expand=jql";
			HTTPResponse filter = jiraClient.Get(request);
			int statusCode = filter.getStatusCode();
	
			if(statusCode != 200 ){
				System.out.println("Failed get call to JIRA with status code: " + statusCode);;
				System.out.println("Failed get call to JIRA with response: " + filter.getData());
				return 0;
			}
			if(statusCode == 200 ) {
				getResults = new String(filter.getData());	
				JSONObject joFilter = new JSONObject(getResults);
		
				if(getResults!=null && !getResults.isEmpty()) {
					//System.out.println(responseData);
					String jql = joFilter.getString("jql");
					String filterName = joFilter.getString("name");
					System.out.println("jql: " + jql);
					String newJql = jql.replaceAll(fromS, toS);
					
					JSONObject joNewFilter = new JSONObject();
					joNewFilter.put("jql", newJql);
					joNewFilter.put("name", filterName);
					joNewFilter.put("id", filterId);
					
					String joNewFilterString = joNewFilter.toString();
					System.out.println("New JSon Filter " + joNewFilterString);
					
					NVPair[] headers = {new NVPair("Content-Type", "application/json")};
					
					String request2 = "/jira/rest/api/latest/filter/" + filterId;
					HTTPResponse response = jiraClient.Put(request2, joNewFilterString.getBytes(), headers);
					
					int putStatusCode = response.getStatusCode();
					String putResults =new String(response.getData());
							
					if(putStatusCode != 200 ){
						System.out.println("Failed put call to Jira in updateConditionForJQLFilter with status code: " + putStatusCode);;
						System.out.println("Failed put call to Jira in updateConditionForJQLFilter with response: " + putResults);
						return 0;
					}
					if(putStatusCode == 200 ) {
						//System.out.println("Put results " + putResults);
						System.out.println("Updated JQL filter succesfully");
						return 1;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Failed to create Jira client with error message: " + e.getMessage());
			e.printStackTrace();
			return 0;
		}
		finally {
		}
		return 0;
	}
	
	// ZAPI - Zephyr API related methods
	//
	// /jira_server/rest/zapi/latest/zql/executionFilter/{id}
	public int updateConditionForZQLFilter (String filterId, String fromS, String toS) {
		try {
			System.out.println("In updateConditionForZQLFilter: " + filterId);
			
			// REPLACE the two jiraClient2 instances with jiraClient  after ALM-3156 Jira ticket is completed
				
			String request = "/jira/rest/zapi/latest/zql/executionFilter/" + filterId;
			HTTPResponse filter = jiraClient2.Get(request);

				
			int statusCode = filter.getStatusCode();
			String getResults = new String(filter.getData());
							
			if(statusCode != 200 ){
					System.out.println("Failed get call to Zephyr with status code: " + statusCode);;
					System.out.println("Failed get call to Zephyr with response: " + getResults);
					return 0;
			}
			if(statusCode == 200 ) {
				//System.out.println("Filter results " + getResults);
				JSONObject joFilter = new JSONObject(getResults);
				String query = joFilter.getString("query");
				String filterName = joFilter.getString("filterName");
				//System.out.println("Filter name " + filterName + "  Filter query " + query);
				
				String newQuery = query.replaceAll(fromS, toS);
				
				JSONObject joNewFilter = new JSONObject();
				joNewFilter.put("query", newQuery);
				joNewFilter.put("filterName", filterName);
				joNewFilter.put("id", filterId);
				
				String joNewFilterString = joNewFilter.toString();
				//System.out.println("New JSon Filter " + joNewFilterString);
				
				NVPair[] headers = {new NVPair("Content-Type", "application/json")};//, new NVPair("Accept", "application/json")};
				
				HTTPResponse response = jiraClient2.Put("/jira/rest/zapi/latest/zql/executionFilter/update", joNewFilterString.getBytes(), headers);
				
				int putStatusCode = response.getStatusCode();
				String putResults =new String(response.getData());
						
				if(putStatusCode != 200 ){
					System.out.println("Failed put call to Jira/Zephyr in updateConditionForZQLFilter with status code: " + putStatusCode);;
					System.out.println("Failed put call to Jira/Zephyr in updateConditionForZQLFilter with response: " + putResults);
					return 0;
				}
				if(putStatusCode == 200 ) {
					//System.out.println("Put results " + putResults);
					System.out.println("Updated Zephyr filter succesfully");
					return 1;
				}
			}
		} catch (Exception e) {
			System.out.println("Failed to create Jira client with error message: " + e.getMessage());
			e.printStackTrace();
			return 0;
		}
		finally {
		}
		return 0;
	}
		
	
	//Purpose: Get the count of test cases that are assigned to execution cycles that meet a ZQL filter condition
	//
	//
	//Use maxRecords=1 intentional not to get back a long response, since we are only interested in the totalCount
	public int getIssueExecutionCountForZQLFilter (String zQLQuery) {
		try {
			//System.out.println("In getIssueExecutionCountForZQLFilter");
				
			String request = "/jira/rest/zapi/latest/zql/executeSearch/?maxRecords=1&zqlQuery=" + zQLQuery;
			//System.out.println(request);
			HTTPResponse queryresponse = jiraClient.Get(request);

				
			int statusCode = queryresponse.getStatusCode();
							
			if(statusCode != 200 ){
					System.out.println("Failed get call to Zephyr with status code: " + statusCode);;
					System.out.println("Failed get call to Zephyr with response: " + new String(queryresponse.getData()));
					return 0;
			}
			if(statusCode == 200 ) {
				String queryResults = new String(queryresponse.getData());
				//System.out.println("Execute search results " + queryResults);
				JSONObject obj1 = new JSONObject(queryResults);
				String executions = obj1.getString("executions");
				if (executions.compareTo("[]")!=0) {
					int count = Integer.parseInt(obj1.getString("totalCount"));
					return (count);
				}
			}
		} catch (Exception e) {
			System.out.println("Failed to create Jira client with error message: " + e.getMessage());
			e.printStackTrace();
			return 0;
		}
		finally {
		}
		return 0;
	}
		
	
	//Purpose: Get the execution stats for a specific project team for a specific version
	//Use ZQL filter conditions to get:
	//	Total PASS
	//	Total FAIL
	//  Total KNOWN FAIL
	//	Total BLOCKED
	//	Total UNEXECUTED (UNEXECUTED, BLOCKED, STALE, WIP)
	//
	//Calculate:
	//	Total = Total PASS + Total FAIL + Total UNEXECUTED
	//  Pass Rate %
	//  
	//Currently the ZQL is hard coded here. Instead, we could define and save ZQL filters in jira with unique names and
	//then use "jira_server/rest/zapi/latest/zql/executionFilter/search" to search for each one required filters and grab the ZQL from there
	//It will create more maintenance though as the queries below are parameterized
	//
	//We can also make this function return the numbers calculated for each team as we need to calculate the overall total - Now I am retrieving via a query as well
	public String manualTestExecutionStatsPerTeam (String version, String projectTeam) {
		int TeamTotal = 0;
		int TeamTotalPass = 0;
		int TeamTotalFailed = 0;
		int TeamTotalBlocked = 0;
		int TeamTotalUnexecuted = 0;
		int TeamTotalExecuted = 0;
		int TeamTotalKnownFailed = 0;
		double TeamPassRate = 0.00;
		double TeamCompletionRate = 0.00;
			
		String html = "";
		String htmlColumnPlain = "<td align='center'>";
		String htmlColumnPink = "<td align='center' bgcolor='pink'>";
		String htmlColumnLimegreen = "<td align='center' bgcolor='limegreen'>";
		String htmlColumnOrange = "<td align='center' bgcolor='orange'>";
		String htmlColumnTomato = "<td align='center' bgcolor='tomato'>";
		String htmlColumnBlocked = htmlColumnPlain;
		String htmlColumnCompletion = htmlColumnPlain;
		String htmlColumnFailed = htmlColumnPlain;

		String cycleNamePrefix = "cycleName~\'R(";
		String projectTeamSuffix = "";
		String additionalCondition = "";
			
		if (projectTeam.compareTo("All")==0) {
			projectTeamSuffix ="";		
		} else {
			projectTeamSuffix = "_" + projectTeam;
			//This does not matter unless they start calling it "R" instead if "Rel"
			additionalCondition = " AND cycleName!~\'Rel(" + version + ")_Release\'";
		}
		
		if (projectTeam.compareTo("Release")==0) {
			additionalCondition ="";
			cycleNamePrefix = "cycleName~\'Rel(";
		} 
		String zQLQueryTotalPass = cycleNamePrefix + version + ")" + projectTeamSuffix + "\' AND fixVersion=\'" + version +"\' AND project=EMCS AND executionStatus in (PASS)" + additionalCondition;
		String zQLQueryTotalFail = cycleNamePrefix + version + ")" + projectTeamSuffix + "\' AND fixVersion=\'" + version +"\' AND project=EMCS AND executionStatus in (FAIL)" + additionalCondition;
		String zQLQueryTotalKnownFail = cycleNamePrefix + version + ")" + projectTeamSuffix + "\' AND fixVersion=\'" + version +"\' AND project=EMCS AND executionStatus in ('KNOWN FAIL')" + additionalCondition;
		
		String zQLQueryTotalBlocked = cycleNamePrefix + version + ")" + projectTeamSuffix + "\' AND fixVersion=\'" + version +"\' AND project=EMCS AND executionStatus in (BLOCKED)" + additionalCondition;
		String zQLQueryTotalUnexecuted = cycleNamePrefix + version + ")" + projectTeamSuffix + "\' AND fixVersion=\'" + version +"\' AND project=EMCS AND executionStatus in (UNEXECUTED, STALE, WIP, BLOCKED) AND priority not in (\'2 - By next distributed version\')" + additionalCondition;
			
		// I cannot not make it work if I call with out the encoding
//		int EmailsTotalPassX = getIssueExecutionCountForZQLFilter (zQLQueryTotalPass);
//		int EmailsTotalFailedX = getIssueExecutionCountForZQLFilter (zQLQueryTotalFail);
//		int EmailsTotalBlockedX = getIssueExecutionCountForZQLFilter (zQLQueryTotalBlocked);
//		int EmailsTotalUnexecutedX = getIssueExecutionCountForZQLFilter (zQLQueryTotalUnexecuted);
			
		try {
			String encodedZQLQueryTotalPass = URLEncoder.encode(zQLQueryTotalPass, "UTF-8");
			TeamTotalPass = TeamTotalPass + getIssueExecutionCountForZQLFilter (encodedZQLQueryTotalPass);
		} catch (Exception e) {
			System.out.println("Issue");
		}
			finally {
		}
			
		try {
			String encodedZQLQueryTotalFail = URLEncoder.encode(zQLQueryTotalFail, "UTF-8");
			TeamTotalFailed = TeamTotalFailed + getIssueExecutionCountForZQLFilter (encodedZQLQueryTotalFail);
		} catch (Exception e) {
			System.out.println("Issue");
		}
		finally {
		}
		
		try {
			String encodedZQLQueryTotalKnownFail = URLEncoder.encode(zQLQueryTotalKnownFail, "UTF-8");
			TeamTotalKnownFailed = TeamTotalKnownFailed + getIssueExecutionCountForZQLFilter (encodedZQLQueryTotalKnownFail);
		} catch (Exception e) {
			System.out.println("Issue");
		}
		finally {
		}
			
		try {
			String encodedZQLQueryTotalBlocked = URLEncoder.encode(zQLQueryTotalBlocked, "UTF-8");
			TeamTotalBlocked = TeamTotalBlocked + getIssueExecutionCountForZQLFilter (encodedZQLQueryTotalBlocked);
			if (TeamTotalBlocked >= 5) {
				htmlColumnBlocked = htmlColumnPink;
			}
		} catch (Exception e) {
			System.out.println("Issue");
		}
			finally {
		}
			
		try {
			String encodedZQLQueryTotalUnexecuted = URLEncoder.encode(zQLQueryTotalUnexecuted, "UTF-8");
			TeamTotalUnexecuted = TeamTotalUnexecuted + getIssueExecutionCountForZQLFilter (encodedZQLQueryTotalUnexecuted);
		} catch (Exception e) {
			System.out.println("Issue");
		}
		finally {
		}

		TeamTotalExecuted = TeamTotalExecuted + TeamTotalFailed + TeamTotalKnownFailed + TeamTotalPass;
		TeamPassRate = TeamPassRate + (double)TeamTotalPass/(double)TeamTotalExecuted*100;
		TeamTotal = TeamTotal + TeamTotalUnexecuted + TeamTotalExecuted; 
		TeamCompletionRate = TeamCompletionRate + (double)TeamTotalExecuted/(double)TeamTotal*100;
		double RoundCompletionRate = (double) Math.round(TeamCompletionRate * 100) / 100;
		double RoundPassRate = (double) Math.round(TeamPassRate * 100) / 100;
			
		if (TeamCompletionRate == 100) {
			htmlColumnCompletion = htmlColumnLimegreen;
		}
		if (TeamPassRate < 97) {
			htmlColumnFailed = htmlColumnTomato;
		} else {
			if (TeamPassRate < 98.5) {
				htmlColumnFailed = htmlColumnOrange;
			}
		}
			
		//Pad the project name to print the values aligned
		String paddedProjectTeam = padRight(projectTeam, 18);
			
		//Want to round but keep two decimals - HELP NEEDED
		System.out.println(paddedProjectTeam + TeamTotal + "		" + TeamTotalExecuted + "		" + TeamTotalFailed + "		" + TeamTotalKnownFailed + "		" + TeamTotalBlocked + "		" + TeamTotalPass + "		" + RoundPassRate + "			" + RoundCompletionRate);
		html = html + "<tr><td><b>" + projectTeam + "</b></td>" + htmlColumnPlain + TeamTotal + "</td>" + htmlColumnPlain + TeamTotalExecuted + "</td>" + htmlColumnFailed + TeamTotalFailed + "</td>" + htmlColumnPlain + TeamTotalKnownFailed + "</td>" + htmlColumnBlocked + TeamTotalBlocked + "</td>" + htmlColumnPlain + TeamTotalPass + "</td>" + "<td align='center'>" + RoundPassRate + "</td>" + htmlColumnCompletion + RoundCompletionRate + "</td>";
		
		return html;
	}
		
	public String manualTestExecutionStats (String version, String confluenceSpace, String confluencePageId, String confluencePageTitle) {
		String paddedProjectTeam = padRight("", 18);
		System.out.println(paddedProjectTeam + "Total" +  "		Executed" + " 	Failed" + "		Known Failed"  + "		Blocked"  + "		Passed" + "		% Pass Rate" + "		% Completion Rate");
		String htmlTableHeader = "<table border='1' cellpadding='3' cellspacing='0'><thead><tr><th align='center' bgcolor='#FF0000'><b>Team</b></th><th align='center' bgcolor='yellow'><b>Total  TCs </b></th><th align='center' bgcolor='yellow'><b>Executed TCs </b></th><th align='center' bgcolor='yellow'><b>Failed</b></th><th align='center' bgcolor='yellow'><b>Known Failed</b></th><th align='center' bgcolor='yellow'><b>Blocked</b></th><th align='center' bgcolor='yellow'><b>Passed</b></th><th align='center' bgcolor='yellow'><b>Pass Rate %</b><th align='center' bgcolor='yellow'><b>Completion  Rate %</b></th></tr></thead><tbody>";
		String html = htmlTableHeader;
		
		html=html+manualTestExecutionStatsPerTeam(version, "Automation");
		if (version.compareTo("471") == 0 || version.compareTo("470") == 0 || version.compareTo("469") == 0 || version.compareTo("468") == 0 || version.compareTo("467") == 0) {
			html=html+manualTestExecutionStatsPerTeam(version, "Verticals");
			html=html+manualTestExecutionStatsPerTeam(version, "Infrastructure");
		} else html=html+manualTestExecutionStatsPerTeam(version, "Enterprise");
		
		html=html+manualTestExecutionStatsPerTeam(version, "SalesTools");
		html=html+manualTestExecutionStatsPerTeam(version, "Forms");
		html=html+manualTestExecutionStatsPerTeam(version, "Insight");
		html=html+manualTestExecutionStatsPerTeam(version, "Emails");
		html=html+manualTestExecutionStatsPerTeam(version, "Targeting");
		html=html+manualTestExecutionStatsPerTeam(version, "Tracking");
		html=html+manualTestExecutionStatsPerTeam(version, "UAC");
		html=html+manualTestExecutionStatsPerTeam(version, "Integration");
		
		html=html+manualTestExecutionStatsPerTeam(version, "All");
		
			
		html = html + "</tbody></table>";	
		//System.out.println(html);
		
		String htmlRelease = htmlTableHeader;
		
		htmlRelease = htmlRelease + manualTestExecutionStatsPerTeam(version, "Release");
		htmlRelease = htmlRelease + "</tbody></table>";	
		//System.out.println(htmlRelease);
		
		String storageValue = "<h2><strong>" + "SCRUM Teams" + "</strong></h2>" + ConfluenceHTLMacroHeader + html + ConfluenceHTLMacroFooter;	
		storageValue = storageValue + "<h2><strong>" + "Release Team" + "</strong></h2>" + ConfluenceHTLMacroHeader + htmlRelease + ConfluenceHTLMacroFooter;
		
		postStorageToConfluence (storageValue, confluenceSpace, confluencePageId, confluencePageTitle);
		
		return "";
	}
	
	public static String padRight(String s, int n) {
		return String.format("%1$-" + n + "s", s);
	}
		

		
	public JSONObject getTestExecutionCountForVersionAndProject (String projectId, String versionId) {
		try {
			//System.out.println("In getTestExecutionCountForVersionAndProject");
				
			String request = "/jira/rest/zapi/latest/execution/count/?groupFld=cycle&versionId=" + versionId + "&projectId=" + projectId;
			//System.out.println(request);
			HTTPResponse queryresponse = jiraClient.Get(request);
			
			int statusCode = queryresponse.getStatusCode();
			String queryResults = new String(queryresponse.getData());
			JSONObject execCounts = new JSONObject(queryResults);
							
			if(statusCode != 200 ){
				System.out.println("Failed get call to JIRA with status code: " + statusCode);;
				System.out.println("Failed get call to JIRA with response: " + queryResults);
			}
			if(statusCode == 200 ) {
				//System.out.println("Execute search results " + queryResults);
				return execCounts;
			}
		} catch (Exception e) {
			System.out.println("Failed to create Jira client with error message: " + e.getMessage());
			e.printStackTrace();
			return new JSONObject();
		}
		finally {
		}
		return new JSONObject();
	}
		
	public String parseExecCycleCountsForVerion (JSONObject execCounts, String versionName) {
			try {
				//System.out.println("In parseExecCycleCountsForVerion");
				if (execCounts.toString().isEmpty()){
					System.out.println("Failed to get execution results for " + versionName);
				} else {
					JSONArray cycleArray = execCounts.getJSONArray("data");
					int cycleCount = cycleArray.length();
					
					String html = "<table border = '1' cellpadding='3' cellspacing='0'><thead><tr><th align='center' bgcolor='lightgrey'><b></b></th><th align='center' bgcolor='lightgrey' width='10'><b>Fail	</b></th><th align='center' bgcolor='lightgrey'><b>Pass	</b></th><th align='center' bgcolor='lightgrey'><b>WIP	</b></th><th align='center' bgcolor='lightgrey'><b>Blocked	</b></th><th align='center' bgcolor='lightgrey'><b>Stale	</b></th><th align='center' bgcolor='lightgrey'><b>Unexecuted	</b><th align='center' bgcolor='lightgrey'><b>Total	</b></th></tr></thead><tbody>";
					String htmlFail = "";
					String htmlPass = "";
					
					//Uncomment if you want to see the results in output window
//					System.out.println(versionName);
//					System.out.println(padRight("", 25) + "FAIL		" + "PASS		" +  "WIP		" +  "BLOCKED		" + "STALE		" + "UNEXECUTED		" + "Total");
					String cycleColumncolour = " bgcolor='lightgrey'";
					String countColumncolour = "";
					
					for(int i = 0; i < cycleCount; i++){
						String cycleName = cycleArray.getJSONObject(i).get("name").toString();
						JSONObject counts = cycleArray.getJSONObject(i).getJSONObject("cnt");
						//System.out.println(versionName + " " + counts.toString());
						String passCount = counts.getString("1");
						String failCount = counts.getString("2");
						String wipCount = counts.getString("3");
						String blockedCount = counts.getString("4");
						String staleCount = counts.getString("5");
						String unExecCount = counts.getString("-1");
						String totalCount = counts.getString("total");
						
						if (failCount.compareTo("0") != 0) {
							countColumncolour = " bgcolor='tomato'";
							cycleColumncolour = " bgcolor='tomato'";
							htmlFail = htmlFail + "<tr><td" + cycleColumncolour + "><b>" + cycleName + "</b></td><td align='center'" + countColumncolour + ">" + failCount + "</td><td align='center'"+ countColumncolour + ">" + passCount + "</td><td align='center'" + countColumncolour + ">" + wipCount + "</td><td align='center'" + countColumncolour + ">" + blockedCount + "</td><td align='center'"+ countColumncolour + ">" + staleCount + "</td><td align='center'" + countColumncolour + ">" + unExecCount + "</td><td align='center'" + countColumncolour + ">" + totalCount + "</td>";
						} else {
							countColumncolour = " bgcolor='white'";
							cycleColumncolour = " bgcolor='lightgrey'";
							htmlPass = htmlPass + "<tr><td" + cycleColumncolour + "><b>" + cycleName + "</b></td><td align='center'" + countColumncolour + ">" + failCount + "</td><td align='center'"+ countColumncolour + ">" + passCount + "</td><td align='center'" + countColumncolour + ">" + wipCount + "</td><td align='center'" + countColumncolour + ">" + blockedCount + "</td><td align='center'"+ countColumncolour + ">" + staleCount + "</td><td align='center'" + countColumncolour + ">" + unExecCount + "</td><td align='center'" + countColumncolour + ">" + totalCount + "</td>";
						}
						
						//Uncomment if you want to see the results in output window
//						System.out.print(padRight(cycleName, 25));
//						System.out.println(failCount + "		" + passCount + "		" + wipCount + "		" + blockedCount + "		" + staleCount + "		" + unExecCount + "			"  + totalCount);
					}
					
//					System.out.println("");
//					System.out.println("");
					
					html = html + htmlFail;
					html = html + htmlPass;
					html = html + "</tbody></table>";
					//System.out.println(html);
					return html;
				}
			} catch (Exception e) {
				System.out.println("Failed in parseExecCycleCountsForVerion with error message: " + e.getMessage());
				e.printStackTrace();
				return "";
			}
			finally {
			}
			return "";
		}
		
	public String getTestExectionResultsForAppsTeams (String projectId, String confluenceSpace, String confluencePageId, String confluencePageTitle, Map <String, String>appsTestVersions) {
			try {
				//System.out.println("In getTestExectionResultsForAppsTeams");
				String storageValue = "";
				
				for(String key: appsTestVersions.keySet()){
					String versionName = appsTestVersions.get(key);
		            JSONObject testObj=getTestExecutionCountForVersionAndProject(projectId, key);
		            System.out.println(testObj.toString());
					String html=parseExecCycleCountsForVerion(testObj, versionName);
					storageValue = storageValue + "<h2><strong>" + versionName + "</strong></h2>" + ConfluenceHTLMacroHeader + html + ConfluenceHTLMacroFooter;
					//<p>&nbsp;</p>  --> for new line
				}
				
				//Release SCRUM Testers confluence wiki page id = 45978608
				postStorageToConfluence (storageValue, confluenceSpace, confluencePageId, confluencePageTitle);
				
			} catch (Exception e) {
				System.out.println("Failed to getTestExectionResultsForAppsTeams: " + e.getMessage());
				e.printStackTrace();
				return "";
			}
			finally {
			}
			return "";
		}
}
