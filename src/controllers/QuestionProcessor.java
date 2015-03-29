package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import models.NameComparator;
import models.Record;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

public class QuestionProcessor {
	private final String API_KEY;
	private final String queryTerm;
	private ArrayList<Record> records; 
	
	public QuestionProcessor(String[] args) {
		records = new ArrayList<Record>();
		
		// Store API_KEY
		API_KEY = args[0];
		
		// Extract query terms
		StringBuffer queryTermBuffer = new StringBuffer();
		for (int i = 3; i < args.length; i++) {
			if ((i == args.length - 1)) {
				if (args[i].charAt(args[i].length()-1) == '?') queryTermBuffer.append(args[i].substring(0, args[i].length()-1));
				else queryTermBuffer.append(args[i]);
			}
			else {
				queryTermBuffer.append(args[i]);
				queryTermBuffer.append(' ');
			}
		}
		queryTerm = queryTermBuffer.toString();
		
		/* TEST print 
		System.out.println(API_KEY);
		System.out.println(queryTerm);
		*/
	}
	
	public void execute() {		
		JSONArray result = queryAsBook();
		if (result != null) {
//			System.out.println(result);///
			for (Object rawRecord: result) {
				Record record = new Record(((JSONObject)rawRecord).get("name").toString(), "Author");
				
				JSONArray works = (JSONArray) ((JSONObject) rawRecord).get("/book/author/works_written");
				for (Object work: works) record.addCreation(((JSONObject) work).get("a:name").toString());
				
				records.add(record);
			}
		}
		
		result = queryAsOrganization();
		if (result != null) {
//			System.out.println(result);///
			for (Object rawRecord: result) {
				Record record = new Record(((JSONObject)rawRecord).get("name").toString(), "Businessperson");
				
				JSONArray orgs = (JSONArray) ((JSONObject) rawRecord).get("/organization/organization_founder/organizations_founded");
				for (Object org: orgs) record.addCreation(((JSONObject) org).get("a:name").toString());
				
				records.add(record);
			}
		}
		
		/* TEST print 
		for (Record r: records) {
			System.out.print(r.getName() + " (as " + r.getType() + ") created ");
			ArrayList<String> creations = r.getCreations();
			for (int i = 0; i < creations.size(); i++) {
				if (i == creations.size()-1) System.out.print("and <" + creations.get(i) + ">");
				else if (creations.size() == 2) System.out.print("<" + creations.get(i) + "> ");
				else System.out.print("<" + creations.get(i) + ">, ");
			}
			System.out.println();
		}
		*/
		
		printSortedRecords();
	}
	
	private JSONArray queryAsBook() {
		JSONArray results = null;
		
		// Build query string
		String query = "[{\"/book/author/works_written\":[{\"a:name\":null,\"name~=\":\"" + queryTerm + "\"}],\"id\":null,\"name\":null,\"type\":\"/book/author\"}]";
		
		GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread");
		url.put("key", API_KEY);
		url.put("query", query);
		
		// Query Freebase server
		try {
			HttpTransport httpTransport = new NetHttpTransport();
			HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
			
			HttpRequest request = requestFactory.buildGetRequest(url);
			HttpResponse httpResponse = request.execute();
			JSONParser parser = new JSONParser();
			JSONObject response = (JSONObject)parser.parse(httpResponse.parseAsString());
			results = (JSONArray)response.get("result");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return results;
	}
	
	private JSONArray queryAsOrganization() {
		JSONArray results = null;
		
		// Build query string
		String query = "[{\"/organization/organization_founder/organizations_founded\":[{\"a:name\":null,\"name~=\":\"" + queryTerm + "\"}],\"id\":null,\"name\":null,\"type\":\"/organization/organization_founder\"}]";
		
		GenericUrl url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread");
		url.put("key", API_KEY);
		url.put("query", query);
		
		// Query Freebase server
		try {
			HttpTransport httpTransport = new NetHttpTransport();
			HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
			
			HttpRequest request = requestFactory.buildGetRequest(url);
			HttpResponse httpResponse = request.execute();
			JSONParser parser = new JSONParser();
			JSONObject response = (JSONObject)parser.parse(httpResponse.parseAsString());
			results = (JSONArray)response.get("result");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return results;
	}
	
	private void printSortedRecords() {
		Collections.sort(records, new NameComparator());
		
		int index = 1;
		for (Record r: records) {
			System.out.println(index + ". " + r);
			index++;
		}
	}
	
	private static void printCorrectQuestionFormat() {
		System.out.println("Question format is not correct.");
		System.out.println("Correct format:");
		System.out.println("	Who created [X]?");
	}

	public static void main(String[] args) {
		/* TEST print args 
		System.out.println("========== args =========");
		for (int i = 0; i < args.length; i++) {
			System.out.println("'" + args[i] + "'");
		}
		System.out.println("========================");
		*/
		
		// Check arguments
		if (args.length < 4 || !args[1].toLowerCase().equals("who") || !args[2].toLowerCase().equals("created")) {
			printCorrectQuestionFormat();
			System.exit(0);
		}
		
		// Begin processing
		QuestionProcessor questionProcessor = new QuestionProcessor(args);
		questionProcessor.execute();
	}
}
