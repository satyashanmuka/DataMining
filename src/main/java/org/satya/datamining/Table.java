package org.satya.datamining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class Table

{

	private ArrayList<Row> rows;
	private TreeMap<String, ArrayList<Double>> cutpointsMap;
	public ArrayList<String> attributeNames;
	private ArrayList<HashSet<Integer>> decisionStar = new ArrayList<HashSet<Integer>>();
	private HashMap<String, MinMaxHolder> attrMinMaxMap = new HashMap<String, MinMaxHolder>();
	private String tableName;
	private String decisionName;
	
	public Table() {
		rows = null;
		attributeNames = null;
	}

	public Table(Table t) {
		this.rows = new ArrayList<Row>();
		for (Row r : t.rows) {
			this.rows.add(new Row(r));
		}
		this.cutpointsMap = t.cutpointsMap;
		this.attributeNames = t.attributeNames;
		this.decisionStar = t.decisionStar;
		this.attrMinMaxMap = t.attrMinMaxMap;
	}

	public Table(ArrayList<Row> rows, ArrayList<String> attributeNames,String tableName,String decsionName) {
		this.rows = rows;
		this.attributeNames = attributeNames;
		this.tableName=tableName;
		this.cutpointsMap = new TreeMap<String, ArrayList<Double>>();
		this.decisionName=decsionName;
		for (String attributes : attributeNames) {
			cutpointsMap.put(attributes, new ArrayList<Double>());
		}

		
		HashSet<String> decisions = new HashSet<String>();
		for (Row r : rows) {
			if (!decisions.contains(r.getDecision())) {
				String decision = r.getDecision();
				decisions.add(decision);
				HashSet<Integer> concept = new HashSet<Integer>();
				for (Row innerRow : rows) {
					if (decision.equals(innerRow.getDecision()))
						concept.add(innerRow.getRowIndex());
				}
				decisionStar.add(concept);
			}

		}

		
		for (int i = 0; i < attributeNames.size(); i++) {
			double max = 1000000000;
			double min = -1000000000;
			for (Row r : rows) {
				double temp = r.getAttributeValues().get(i).getValue();
				if (max > temp)
					max = temp;
				else if (min < temp) {
					min = temp;
				}

			}
			attrMinMaxMap
					.put(attributeNames.get(i), new MinMaxHolder(min, max));
		}

	}

	public void addCutPoint(String attribute, double value) {
		ArrayList<Double> listOfCutPoints = cutpointsMap.get(attribute);
		listOfCutPoints.add(value);
		addRanges(this);

	}

	public static void addRanges(Table table) {

		for (int index = 0; index < table.attributeNames.size(); index++) {
			String attributeName = table.attributeNames.get(index);
			ArrayList<Double> cutpoints = table.getCutpointsMap().get(
																	attributeName);
			Collections.sort(cutpoints);
			double min = table.attrMinMaxMap.get(attributeName).getMin();
			double max = table.attrMinMaxMap.get(attributeName).getMax();

			for (Row row : table.rows) {
				AttributeData attrb = row.getAttributeValues().get(index);
				if (cutpoints.isEmpty()) {
					attrb.setInterval(min + ".." + max);
				} else {
					for (int i = 0; i < cutpoints.size(); i++) {
						double temp = attrb.getValue();
						double cut = cutpoints.get(i);
						if (i == 0) {
							if (min <= temp && temp < cut) {
								attrb.setInterval(min + ".." + cut);
							}
							if(i != cutpoints.size() - 1) {
								if (temp >= cut && temp < cutpoints.get(i + 1)) {
									attrb.setInterval(cut + ".."
											+ cutpoints.get(i + 1));
								}
							}
							
						}
						if (i == cutpoints.size() - 1) {
							if (temp >= cut && temp <= max) {
								attrb.setInterval(cut + ".." + max);
							}
						}
						if (i != 0 && i != cutpoints.size() - 1) {
							if (temp >= cut && temp < cutpoints.get(i + 1)) {
								attrb.setInterval(cut + ".."
										+ cutpoints.get(i + 1));
							}
						}
					}
				}
			}
		}
	}

	public ArrayList<Row> getRows() {
		return rows;
	}

	public void setRows(ArrayList<Row> rows) {
		this.rows = rows;
	}

	public ArrayList<String> getAttributeNames() {
		return attributeNames;
	}

	public void setAttributeNames(ArrayList<String> attributeNames) {
		this.attributeNames = attributeNames;
	}

	public TreeMap<String, ArrayList<Double>> getCutpointsMap() {
		return cutpointsMap;
	}

	public void setCutpointsMap(TreeMap<String, ArrayList<Double>> cutpointsMap) {
		this.cutpointsMap = cutpointsMap;
	}

	public static Table getInconsistentSubTable(Table passedTable) {
		
		if (passedTable.cutpointsMap.isEmpty()) {

			return passedTable;
		} else {
			Table temp;

			temp = new Table(passedTable);

			ArrayList<HashSet<Integer>> attributeStar = new ArrayList<HashSet<Integer>>();
			Set<Row> calculatedSet = new HashSet<Row>();
			for (Row row : temp.rows) {
				if (!calculatedSet.contains(row)) {
					calculatedSet.add(row);
					HashSet<Integer> subSet = new HashSet<Integer>();
					for (Row innerRow : temp.rows) {
						if (row.equals(innerRow)) {
							subSet.add(innerRow.getRowIndex());

						}
					}
					attributeStar.add(subSet);
				}
			}

			ArrayList<HashSet<Integer>> inconsistentSet = getInconsistentSets(
					attributeStar, temp.decisionStar);
			
			HashSet<Integer> returnSet = inconsistentSet.get(0);
			
			ArrayList<Integer> indexesToRemove = new ArrayList<Integer>();
			for (int i = 0; i < temp.rows.size(); i++) {
				Row row = temp.rows.get(i);
				boolean flagDeletion = true;
				
					if (returnSet.contains(row.getRowIndex())) {
						flagDeletion = false;
					}
				
				if (flagDeletion) {
					indexesToRemove.add(i);
				}

			}
			int indexToBeSubtracted=0;
			for (Integer j : indexesToRemove) {

				temp.rows.remove(j.intValue()-indexToBeSubtracted++);
			}
			
			return temp;

		}

	}

	public boolean isConsistent() {
		ArrayList<HashSet<Integer>> attributeStar = new ArrayList<HashSet<Integer>>();
		Set<Row> calculatedSet = new HashSet<Row>();
		for (Row row : this.rows) {
			if (!calculatedSet.contains(row)) {
				calculatedSet.add(row);
				HashSet<Integer> subSet = new HashSet<Integer>();
				for (Row innerRow : this.rows) {
					if (row.equals(innerRow)) {
						subSet.add(innerRow.getRowIndex());

					}
				}
				attributeStar.add(subSet);
			}
		}

		return getInconsistentSets(attributeStar, decisionStar).isEmpty();

	}

	public static ArrayList<HashSet<Integer>> getInconsistentSets(
			ArrayList<HashSet<Integer>> attributeStar,
			ArrayList<HashSet<Integer>> decisionStr) {

		ArrayList<HashSet<Integer>> inConsistentSet = new ArrayList<HashSet<Integer>>();

		for (HashSet<Integer> subAttr : attributeStar) {
			boolean subSetFlag = false;
			for (HashSet<Integer> concept : decisionStr) {
				if (isSubset(subAttr, concept)) {
					subSetFlag = true;
				}
			}
			if (!subSetFlag) {
				inConsistentSet.add(subAttr);
			}

		}
		return inConsistentSet;
	}

	static boolean isSubset(HashSet<Integer> one, HashSet<Integer> two) {

		boolean flag = true;
		for (Integer temp : one) {
			if (!two.contains(temp))
				flag = false;
		}
		return flag;
	}

	public void dropCutpoints() 
	{
		for(String attribute : this.attributeNames) {
		ArrayList<Double> listOfCutPoints = cutpointsMap.get(attribute);
		ArrayList<Double> tempCutPoints = (ArrayList<Double>) listOfCutPoints.clone();
		for(Double value:tempCutPoints) {
		listOfCutPoints.remove(value);
		addRanges(this);
		if(!this.isConsistent()) {
			listOfCutPoints.add(value);
			addRanges(this);
		}
		else {
			
		}
		
		}
		
		}
	}

	public ArrayList<HashSet<Integer>> getDecisionStar() {
		return decisionStar;
	}

	public void setDecisionStar(ArrayList<HashSet<Integer>> decisionStar) {
		this.decisionStar = decisionStar;
	}

	public HashMap<String, MinMaxHolder> getAttrMinMaxMap() {
		return attrMinMaxMap;
	}

	public void setAttrMinMaxMap(HashMap<String, MinMaxHolder> attrMinMaxMap) {
		this.attrMinMaxMap = attrMinMaxMap;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getDecisionName() {
		return decisionName;
	}

	public void setDecisionName(String decisionName) {
		this.decisionName = decisionName;
	}

}

class MinMaxHolder {
	private double min;
	private double max;

	public MinMaxHolder(double max, double min) {
		super();
		this.min = min;
		this.max = max;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}
	

}


class Row 

{
	private String decision;
	private int rowIndex;
	private ArrayList<AttributeData> attributeValues;	

	Row(ArrayList<Double> values,String decision,ArrayList<String> attributeNames,int rowIndex)
	{
		attributeValues=new ArrayList<AttributeData>();
		int counter=0;
		for(String attributeName:attributeNames)
		{
			attributeValues.add(new AttributeData(attributeName, values.get(counter++), decision));
		}
		
		this.decision=decision;
		this.rowIndex=rowIndex;
	}

	public Row(Row r) 
	{
		this.decision=new String(r.decision);
		this.rowIndex=r.rowIndex;
		this.attributeValues=new ArrayList<AttributeData>();
		for(AttributeData  attr: r.getAttributeValues()) {
			this.attributeValues.add(new AttributeData(attr));
		}
		
	}
	
	public String getDecision() 
	{
		return decision;
	}

	public void setDecision(String decision) 
	{
		this.decision = decision;
	}


	public ArrayList<AttributeData> getAttributeValues() {
		return attributeValues;
	}

	public void setAttributeValues(ArrayList<AttributeData> attributeValues) {
		this.attributeValues = attributeValues;
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}
	
	@Override
	public boolean equals(Object row) {		
		ArrayList<AttributeData> crntRowAtrbList = this.attributeValues;
		ArrayList<AttributeData> argRowAtrvList = ((Row)row).getAttributeValues();
		for(int index=0;index<crntRowAtrbList.size();index++) {
			if(!(crntRowAtrbList.get(index)).getInterval().equals(argRowAtrvList.get(index).getInterval())){
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		ArrayList<AttributeData> crntRowAtrbList = this.attributeValues;
		String allInterval="";
		for(int index=0;index<crntRowAtrbList.size();index++) {
			allInterval=allInterval+crntRowAtrbList.get(index).getInterval();
		}
        return allInterval.hashCode(); 
    }
		
}


class AttributeData 

{
	
	private String attributName;
	private double value;
	private String interval;
	private String decision;
	
	public AttributeData(String attributName, double value,String decision) {
		super();
		this.attributName = attributName;
		this.value = value;
		this.interval = "";
		this.decision = decision;
	}
	
	public AttributeData(AttributeData data) {
		this.attributName=data.attributName;
		this.value=data.value;
		this.interval=data.interval;
		this.decision=data.decision;
	}
	
	public String getAttributName() {
		return attributName;
	}
	public void setAttributName(String attributName) {
		this.attributName = attributName;
	}
	public double getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public String getInterval() {
		return interval;
	}
	public void setInterval(String interval) {
		this.interval = interval;
	}
	public String getDecision() {
		return decision;
	}
	public void setDecision(String decision) {
		this.decision = decision;
	}
	
}

class AttributeValueRetrieval 

{
	
	public static Double getValueForAtrributeInRow(Row row,String attrName) {
		
		for(AttributeData attr:row.getAttributeValues()) {
			if(attr.getAttributName().equals(attrName)) {
				return  attr.getValue();
			}
		}
		return null;
	}
}
