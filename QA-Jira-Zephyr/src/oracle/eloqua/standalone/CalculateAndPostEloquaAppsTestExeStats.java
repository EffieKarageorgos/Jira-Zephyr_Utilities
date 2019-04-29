package oracle.eloqua.standalone;

import java.util.Map;
import java.util.LinkedHashMap;
// TEST COMMIT

public class CalculateAndPostEloquaAppsTestExeStats {
	
	private final static String APPSSPACE = "EG";
	private final static String APPSPROJECTID = "12238";
	private final static String ELQAAPPSEXECSTATSCONFLUENCEPAGEID="81850103";
	private final static String ELQAAPPSEXECSTATSCONFLUENCEPAGETITLE="Automation Execution Status";

	public static void main(String[] args) {
		JiraTestCycleOutcomeAnalyzer jtcoa = new JiraTestCycleOutcomeAnalyzer();
		
		//Need to use LinkedHashMap in order to get the results posted in the order specified
		Map<String, String> appsTestVersions = new LinkedHashMap<>();
		appsTestVersions.put("36517", "GAL_QA01");
		appsTestVersions.put("36518", "GAL_QA02");
		appsTestVersions.put("36519", "GAL_QA03");
		appsTestVersions.put("36520", "GAL_STG");
		
		jtcoa.getTestExectionResultsForAppsTeams(APPSPROJECTID, APPSSPACE, ELQAAPPSEXECSTATSCONFLUENCEPAGEID, ELQAAPPSEXECSTATSCONFLUENCEPAGETITLE, appsTestVersions);
	}
}