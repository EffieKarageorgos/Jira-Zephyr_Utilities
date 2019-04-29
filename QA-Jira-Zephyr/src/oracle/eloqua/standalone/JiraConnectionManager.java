package oracle.eloqua.standalone;

import oracle.jira.JIRAHttpConnection;
import HTTPClient.URI;

public class JiraConnectionManager {
	
	private final static String ORACLEJIRAURL = "https://jira.oraclecorp.com";
//	private final String ORACLEJIRAURL = "https://jira-uat.us.oracle.com";
	private final static String ORACLEGENERICUSERNAME = "eloquaunassigned_ar@oracle.com";
	private final static String ORACLEGENERICUSERPASSWORD = "Cdv631311";
	
	private final static String ORACLEGENERICUSERNAME2 = "effie.karageorgos@oracle.com";
	private final static String ORACLEGENERICUSERPASSWORD2 = "XXX";

	private static JIRAHttpConnection jiraClient = null;
	private static JIRAHttpConnection jiraClient2 = null;
	private final static int JIRATIMEOUT = 30000;

	//TODO: Might have to check how long we can continue using the same session, i.e. there
	// might be a point where we might have to re-connect to jira using a new connection.
	public static JIRAHttpConnection getJiraClient(){
		if(jiraClient != null)
			return jiraClient;
		else{
			try {
				jiraClient = new JIRAHttpConnection(new URI(ORACLEJIRAURL), ORACLEGENERICUSERNAME, ORACLEGENERICUSERPASSWORD.toCharArray(), "JIRA");
				jiraClient.setTimeout(JIRATIMEOUT);
			} catch (Exception e) {
				System.out.println("Failed to create Jira client with error message: " + e.getMessage());
				e.printStackTrace();
			}
		}
		return jiraClient;
	}
	
	//REMOVE THIS WHEN YOU FIGURE OUT A WAY TO CHANGE THE OWNERSHIP of the ZQL filters that you manipulate
	public static JIRAHttpConnection getJiraClient2(){
		if(jiraClient2 != null)
			return jiraClient2;
		else{
			try {
				jiraClient2 = new JIRAHttpConnection(new URI(ORACLEJIRAURL), ORACLEGENERICUSERNAME2, ORACLEGENERICUSERPASSWORD2.toCharArray(), "JIRA");
				jiraClient2.setTimeout(JIRATIMEOUT);
			} catch (Exception e) {
				System.out.println("Failed to create Jira client with error message: " + e.getMessage());
				e.printStackTrace();
			}
		}
		return jiraClient2;
	}
	//TODO: Method to close or dump above established jira connection safely.
}
