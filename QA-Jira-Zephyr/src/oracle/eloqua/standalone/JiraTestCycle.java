package oracle.eloqua.standalone;

/**
 * @author jdogra
 * Purpose: Custom Class used to define a list that contains a test execution cycle name,
 * the Id of the Jira filter used to populate the cycle with test cases and the cycle id that uniquely identifies the the cycle
 */
public class JiraTestCycle {
	private String cycleName;
	private String filterId;
	private String cycleId;
	
	public JiraTestCycle(){}
	
	public JiraTestCycle(String cycleName, String filterId, String cycleId) {
		this.cycleName = cycleName;
	    this.filterId = filterId;
	    this.cycleId = cycleId;
	}
	
	public String getCycleName(){
		return this.cycleName;
	}
	
	public String getCycleId(){
		return this.cycleId;
	}

	public String getFilterId(){
		return this.filterId;
	}

	public void setCycleName(String cycleName){
		this.cycleName = cycleName;
	}
	
	public void setCycleId(String cycleId){
		this.cycleId = cycleId;
	}

	public void setFilterId(String filterId){
		this.filterId = filterId;
	}
}
