package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import models.NameComparator;
import models.Record;

import org.apache.commons.lang.StringUtils;
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
	private final boolean isInShellMode;
	private final String API_KEY;
	private final String queryTerm;
	private ArrayList<Record> records;
	private int maxPersonNameLength = -1;
	
	public QuestionProcessor(String[] args) {
		records = new ArrayList<Record>();
		
		// Set print mode
		isInShellMode = args[0].matches("true");
		
		// Store API_KEY
		API_KEY = args[1];
		
		// Extract query terms
		StringBuffer queryTermBuffer = new StringBuffer();
		for (int i = 4; i < args.length; i++) {
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
				String personName = ((JSONObject)rawRecord).get("name").toString();
				Record record = new Record(personName, "Author");
				if (personName.length() > maxPersonNameLength) maxPersonNameLength = personName.length();
				
				JSONArray works = (JSONArray) ((JSONObject) rawRecord).get("/book/author/works_written");
				for (Object work: works) record.addCreation(((JSONObject) work).get("a:name").toString());
				
				records.add(record);
			}
		}
		
		result = queryAsOrganization();
		if (result != null) {
//			System.out.println(result);///
			for (Object rawRecord: result) {
				String personName = ((JSONObject)rawRecord).get("name").toString();
				Record record = new Record(personName, "Businessperson");
				if (personName.length() > maxPersonNameLength) maxPersonNameLength = personName.length();
				
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
		
		// Shell mode is on
		if (isInShellMode) {
			final int BOX_WIDTH = 100;
			
			// Print title
			System.out.println(" " + StringUtils.center("", BOX_WIDTH-2, "-") + " ");
			String title = "Who created " + queryTerm + "?";
			System.out.println("| " + StringUtils.center(title, BOX_WIDTH-4) + " |");
			System.out.println(" " + StringUtils.center("", BOX_WIDTH-2, "-") + " ");
			
			// Print records
			for (Record r: records) {
				int firstCellLength = (int) ((BOX_WIDTH - (3+maxPersonNameLength) - 7)/2.0);
				int secondCellLength = BOX_WIDTH - (3+maxPersonNameLength) - 7 - firstCellLength;
				
				// Print person name and titles
				System.out.print("| " + StringUtils.rightPad(r.getName(), maxPersonNameLength) + " ");
				System.out.print("| " + StringUtils.rightPad("AS", firstCellLength) + " | ");
				System.out.println(StringUtils.rightPad("CREATION", secondCellLength) + " |");
				// Print separator
				System.out.print("| " + StringUtils.rightPad("", maxPersonNameLength) + " ");
				System.out.print("--" + StringUtils.rightPad("", firstCellLength, "-") + "---");
				System.out.println(StringUtils.rightPad("", secondCellLength, "-") + "- ");
				// Print content
				System.out.print("| " + StringUtils.rightPad("", maxPersonNameLength) + " ");
				System.out.print("| " + StringUtils.rightPad(r.getType(), firstCellLength) + " | ");
				String creations = r.getCreationsString();
				if (creations.length() > secondCellLength) creations = creations.substring(0, secondCellLength-3) + "...";
				System.out.println(StringUtils.rightPad(creations, secondCellLength) + " |");
				// Print separator
				System.out.println(" " + StringUtils.center("", BOX_WIDTH-2, "-") + " ");
			}
		}
		// Shell mode is not on
		else {
			int index = 1;
			for (Record r: records) {
				System.out.println(index + ". " + r);
				index++;
			}
			
			if (records.size() == 0) System.out.println("It seems no one created [" + queryTerm + "].");
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
		if (args.length < 5 || !args[2].toLowerCase().equals("who") || !args[3].toLowerCase().equals("created")) {
			printCorrectQuestionFormat();
			System.exit(0);
		}
		
		// Begin processing
		QuestionProcessor questionProcessor = new QuestionProcessor(args);
		questionProcessor.execute();
	}
}
