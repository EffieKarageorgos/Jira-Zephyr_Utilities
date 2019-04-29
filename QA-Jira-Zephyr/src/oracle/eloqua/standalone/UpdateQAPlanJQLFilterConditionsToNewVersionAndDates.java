package oracle.eloqua.standalone;

import java.util.Map;

public class UpdateQAPlanJQLFilterConditionsToNewVersionAndDates {

	public static void main(String[] args) {
		//System.out.println(args.length);
		String FROMSTRING = "";
		String TOSTRING = "";
		if (args.length == 2) {
			FROMSTRING = args[0].toString();
			TOSTRING = args[1].toString();
			System.out.println("FROMSTRING: " + FROMSTRING + " TOSTRING " + TOSTRING);
		} else {
			FROMSTRING = "474";
			TOSTRING = "475";
		}
		
		JiraTestCycleOutcomeAnalyzer jtcoa = new JiraTestCycleOutcomeAnalyzer();
		
		// The 108882722 page contains the list of QA Plan related JQL filters
		String content=jtcoa.getStorageValueOfConfluencePage ("108882722");
		
		Map<String, String> firstTwoColumns=jtcoa.parseHTMLTableString(content);
		for(String id: firstTwoColumns.keySet()){
            //System.out.println(id  +" Filter Name: "+ firstTwoColumns.get(id));
            jtcoa.updateConditionForJQLFilter (id, FROMSTRING, TOSTRING);
        }
	}
}