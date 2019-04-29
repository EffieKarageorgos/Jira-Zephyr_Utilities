package oracle.eloqua.standalone;

public class CalculateAndPostEloquaCoreRegressionStats {
	private final static String ELQACORESPACE = "ELQAQA";
	private final static String ELQAEXECSTATSCONFLUENCEPAGEID="71572222";
	
	public static void main(String[] args) {
		//System.out.println(args.length);
		String ELQARELVERSION = "";
		if (args.length != 0) {
			//System.out.println("ELQARELVERSION: " + args[0].toString());
			ELQARELVERSION = args[0].toString();
		} else {
			ELQARELVERSION = "475";
		}
		
		String ELQAEXECSTATSCONFLUENCEPAGETITLE=ELQARELVERSION + " Eloqua SCRUM + Release Teams Manual Regression Stats";
		
		JiraTestCycleOutcomeAnalyzer jtcoa = new JiraTestCycleOutcomeAnalyzer();
		jtcoa.manualTestExecutionStats(ELQARELVERSION, ELQACORESPACE, ELQAEXECSTATSCONFLUENCEPAGEID, ELQAEXECSTATSCONFLUENCEPAGETITLE);
		
	}
}