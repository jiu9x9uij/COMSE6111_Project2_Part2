package models;

import java.util.Comparator;

public class NameComparator implements Comparator<Record> {
	@Override
	public int compare(Record r1, Record r2) {
		String name1 = r1.getName();
		String name2 = r2.getName();

		return name1.compareTo(name2);
	}
	
}