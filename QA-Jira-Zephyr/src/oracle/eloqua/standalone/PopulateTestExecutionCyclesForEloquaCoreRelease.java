package oracle.eloqua.standalone;

import java.util.Map;

import org.codehaus.jettison.json.JSONObject;

public class PopulateTestExecutionCyclesForEloquaCoreRelease {
	
	//473 - id: 26367
	//474 - id: 30756
	//475 - version id: 30757
	private final static String ELQARELVERSION = "475";
	private final static String ELQARELVERSIONID = "30757";
	

	//Is there a way to get version id from version name? If yes, we should make the version an argument and then find the id
	
	public static void main(String[] args) {
		CreateAndPopulateTestExecCycle cycleObj = new CreateAndPopulateTestExecCycle();
		JiraTestCycleOutcomeAnalyzer jtcoa = new JiraTestCycleOutcomeAnalyzer();
		
		JSONObject testCycles = cycleObj.getTestCyclesFromZephyrJiraForProjectAndVersion(ELQARELVERSIONID);
		
		// The 52399516 page contains the list of Cycles and corresponding filters
		String content=jtcoa.getStorageValueOfConfluencePage ("52399516");			
		Map<String, String> firstTwoColumns=jtcoa.parseHTMLTableString(content);
		
		for(String cycleNameGen: firstTwoColumns.keySet()){
//			System.out.println("");
//          System.out.println(cycleNameGen  +" Filter Id: "+ firstTwoColumns.get(cycleNameGen));
            
            if (!cycleNameGen.isEmpty()) {
            	String cycleName = "R(" + ELQARELVERSION + ")_" + cycleNameGen;
            	String cycleId=cycleObj.findTestCycleIdByName(testCycles, cycleName);
            	String filterId = firstTwoColumns.get(cycleNameGen);
            
            	if (cycleId.isEmpty() || filterId.isEmpty()) {
            		System.out.println("Cycle with name " + cycleName  +" does not exist OR filter id is not specified");
            	} else {
            		cycleObj.populateTestCycle(ELQARELVERSIONID, cycleName, cycleId, filterId);
            	}
            } else {
            	System.out.println("Empty cycle name with filter id " + firstTwoColumns.get(cycleNameGen));
            }
        }
	}
}