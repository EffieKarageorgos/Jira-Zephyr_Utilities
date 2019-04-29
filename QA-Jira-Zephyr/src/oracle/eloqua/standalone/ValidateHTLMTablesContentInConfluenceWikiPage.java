package oracle.eloqua.standalone;

import java.util.Map;

public class ValidateHTLMTablesContentInConfluenceWikiPage {
		
	public static void main(String[] args) {
		JiraTestCycleOutcomeAnalyzer jtcoa = new JiraTestCycleOutcomeAnalyzer();
		String CONFLUENCEPAGEID = "";
		if (args.length != 0) {
			//System.out.println("CONFLUENCEPAGEID: " + args[0].toString());
			CONFLUENCEPAGEID = args[0].toString();
		} else {
			//Page id for Execution cycles and filter ids: 52399516
			//Page id for Regression ZQL filters for TCM dash boards: 87522119
			//Page id for QA Plan JQL filters: 108882722
			CONFLUENCEPAGEID = "108882722";
		}
		
		String content=jtcoa.getStorageValueOfConfluencePage (CONFLUENCEPAGEID);
		Map<String, String> firstTwoColumns=jtcoa.parseHTMLTableString(content);
			
		for(String col1: firstTwoColumns.keySet()){
			System.out.println("First column: " + col1  +"	Second column: "+ firstTwoColumns.get(col1));
			System.out.println("\n");
	    }
	}
}
