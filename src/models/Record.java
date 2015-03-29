package models;

import java.util.ArrayList;

public class Record {
	private String mName;
	private String mType;
	private ArrayList<String> mCreations;
	
	public Record(String name, String type) {
		mName = name;
		mType = type;
		mCreations = new ArrayList<String>();
	}
	
	public String getName() {
		return mName;
	}
	
	public String getType() {
		return mType;
	}
	
	public ArrayList<String> getCreations() {
		return mCreations;
	}
	
	public void addCreation(String creation) {
		mCreations.add(creation);
	}
	
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append(mName + " (as " + mType + ") created ");
		
		for (int i = 0; i < mCreations.size(); i++) {
			if (i == mCreations.size()-1) str.append("and <" + mCreations.get(i) + ">");
			else if (mCreations.size() == 2) str.append("<" + mCreations.get(i) + "> ");
			else str.append("<" + mCreations.get(i) + ">, ");
		}
		
		return str.toString();
	}
}
