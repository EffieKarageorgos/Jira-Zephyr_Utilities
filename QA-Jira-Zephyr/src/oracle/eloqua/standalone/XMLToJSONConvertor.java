package oracle.eloqua.standalone;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.XML;

import java.io.*;

public class XMLToJSONConvertor {
	
	public JSONObject getJSONFromXML (String table) {
		try {

			JSONObject jsonObj = XML.toJSONObject(table);
			System.out.println(jsonObj);
			return (jsonObj);
		} catch (Exception e) {
			System.out.println("Failed to create Jira client with error message: " + e.getMessage());
			e.printStackTrace();
			return new JSONObject();
		}
		finally {
		}
	}
}