package project;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.jayway.jsonpath.JsonPath;


public class Launcher {
	public static Properties properties = new Properties();
	
	public static ArrayList<String> step1(String query) {
		try {
			properties.load(new FileInputStream("freebase.properties"));
		    HttpTransport httpTransport = new NetHttpTransport();
		    HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
		    JSONParser parser = new JSONParser();
		    GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/search");
		    url.put("query", query);
//		    url.put("filter", "(all type:/music/artist created:\"The Lady Killer\")");
//		    url.put("limit", "10");
//		    url.put("indent", "true");
		    url.put("key", properties.get("API_KEY"));
		    HttpRequest request = requestFactory.buildGetRequest(url);
		    HttpResponse httpResponse = request.execute();
		    JSONObject response = (JSONObject)parser.parse(httpResponse.parseAsString());
		    JSONArray results = (JSONArray)response.get("result");
		    for (Object result : results) {
		      System.out.println(JsonPath.read(result,"$.name").toString());
		    }
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
		return null;
	}
	
	public static JSONObject step2(ArrayList<String> midArray) {
	    try {
	    	properties.load(new FileInputStream("freebase.properties"));
	        HttpTransport httpTransport = new NetHttpTransport();
	        HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
	        JSONParser parser = new JSONParser();
	        String topicId = "/m/0d6lp";
	        GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/topic" + topicId);
	        url.put("key", properties.get("API_KEY"));
	        HttpRequest request = requestFactory.buildGetRequest(url);
	        HttpResponse httpResponse = request.execute();
	        JSONObject topic = (JSONObject)parser.parse(httpResponse.parseAsString());
//	        System.out.println(JsonPath.read(topic,"$.property['/type/object/name'].values[0].value").toString());
//	        System.out.println(JsonPath.read(topic,"$.property['/type/object/type'].values[0].text").toString());
	        
	        return topic;
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    
	    return null;
	}

	
	public static void main(String[] args) {
		ArrayList<String> midArray = step1("bill gates");
		JSONObject topic = step2(midArray);
	}

}
