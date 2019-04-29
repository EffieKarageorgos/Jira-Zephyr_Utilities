package oracle.eloqua.standalone;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import org.json.JSONObject;

public class TestTheVariousMethods {
	
	//473 - id: 26367
	//474 - id: 30756
	//475 - version id: 30757
	private final static String ELQARELVERSION = "475";
	private final static String ELQARELVERSIONID = "30757";
	

	public static void main(String[] args) {
		//CreateAndPopulateTestExecCycleOLD testObj = new CreateAndPopulateTestExecCycleOLD();
		
		//ARCHIVE executions for 470
		//String cnt = testObj.getIssueCountForJQLFilter("38279");
		
		JiraTestCycleOutcomeAnalyzer jtcoa = new JiraTestCycleOutcomeAnalyzer();
		//int xx = jtcoa.updateConditionForJQLFilter("40651", "473", "474");
	}
}