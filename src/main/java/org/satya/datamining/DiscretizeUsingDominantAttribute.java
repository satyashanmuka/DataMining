package org.satya.datamining;

import java.io.*;  //its a good practice to write specific class you are using
import java.util.*;

public class DiscretizeUsingDominantAttribute {
	
	public static void discretize(Table table)
	{
				
		do {
			
			Table subTable=Table.getInconsistentSubTable(table);
			String bestAttribute=getBestAttribute(subTable);
			table.addCutPoint(bestAttribute, getBestCutPointForBestAttribute(subTable, bestAttribute));
			if(table.isConsistent()) {
				break;
			}
			
		}while(true);
		
		table.dropCutpoints();
		WriteDiscretizedTableToOutput.writeOutputToFile(table);
		System.out.println("Discretization is completed and corresponding output files have been generated");
	
	}
	
	public static String getBestAttribute(Table table) 
	{
		
		String minimumAtrribute="";
		Double minimumEntropyValue=null;
		
		for(String attributeName:table.getAttributeNames()){
			
			ArrayList<String[]> listOfvalues=new ArrayList<String[]>();
			HashSet<Double> calculatedValues=new HashSet<Double>();
			
			for(Row row:table.getRows()) {
				Double value= AttributeValueRetrieval.getValueForAtrributeInRow(row, attributeName);
				if(!calculatedValues.contains(value)) {
					calculatedValues.add(value);
					ArrayList<String> decisionHolder=new ArrayList<String>();
					
					for(Row innerRow:table.getRows()) {
						Double innerValue=AttributeValueRetrieval.getValueForAtrributeInRow(innerRow,attributeName);
						if(value.equals(innerValue)) decisionHolder.add(innerRow.getDecision());					
					}
					listOfvalues.add((String[]) decisionHolder.toArray(new String[0]));
				}				
				
			}
			double calculatedEnrtopy=EntropyCalculator.entropyCalculator(listOfvalues);
			
			if(minimumEntropyValue==null) {
				minimumEntropyValue=calculatedEnrtopy;
				minimumAtrribute=attributeName;
			}
			else {
				if(calculatedEnrtopy<minimumEntropyValue) {
					minimumEntropyValue=calculatedEnrtopy;
					minimumAtrribute=attributeName;
				}
			}
			
		}
		
		return minimumAtrribute;
	}
	
	public static double getBestCutPointForBestAttribute(Table table, String bestAttribute) {
		
		double bestCutpoint=-1000000000;
		double minEntropyValue=1000000000;
		ArrayList<AttributeData> bstAttr=new ArrayList<AttributeData>();
		
		for(Row row:table.getRows()) {
			for(AttributeData attr: row.getAttributeValues()) {
				if(attr.getAttributName().equals(bestAttribute)) {
					bstAttr.add(attr);
				}
			}
		}
		
		Collections.sort(bstAttr, new AttributeComparator());
				
		for(int i=0;i<bstAttr.size();i++) {
			double curntValue=bstAttr.get(i).getValue();
			if(i+1==bstAttr.size()) break;
			double nxtValue=bstAttr.get(i+1).getValue();
			
			ArrayList<String[]> listOfvalues=new ArrayList<String[]>();
			double cutpoint;
			if(curntValue<nxtValue) {
				String firstHalf[]=new String[i+1];
				String otherHalf[]=new String[bstAttr.size()-(i+1)];
				double two=2;
				cutpoint=(curntValue+nxtValue)/two;
				int first=0;
				int second=0;
				for(int j=0;j<bstAttr.size();j++) {
					
					if(j<=i)
						firstHalf[first++]=bstAttr.get(j).getDecision();
					else
						otherHalf[second++]=bstAttr.get(j).getDecision();
						
				}
				
				listOfvalues.add(firstHalf);
				listOfvalues.add(otherHalf);
				double entropy=EntropyCalculator.entropyCalculator(listOfvalues);
				if(i==0) {
					minEntropyValue=entropy;
					bestCutpoint=cutpoint;
				}			
				else {
					if(entropy<minEntropyValue && !table.getCutpointsMap().get(bestAttribute).contains(cutpoint)) {
						minEntropyValue=entropy;
						bestCutpoint=cutpoint;
					}
				}
			}
		}
	
		return bestCutpoint;
	
	}
	
	
	

}

class AttributeComparator implements Comparator<AttributeData> {

	
	public int compare(AttributeData d1, AttributeData d2) {

		
		return ((Double)d1.getValue()).compareTo(d2.getValue());
		
	}
	
}

class EntropyCalculator {
	
	public static double entropyCalculator(ArrayList<String[]> coloumn) {
		
		double totalLength=0;
		double entropy=0;
		
		for(String[] a :coloumn) {
			totalLength+=a.length;
		}
		
		for(String[] bin :coloumn) {
			entropy+=(bin.length/totalLength)*(entropyCalculatorForBin(bin));
		}		
		
		return entropy; 
	}
	
	public static double entropyCalculatorForBin(String[] decisions) {
		
		double subTotal=0;
		double total=decisions.length;
		Set<String> descionSet=new HashSet<String>();
		
		for(String decision:decisions) {
			if(!descionSet.contains(decision)) {
				descionSet.add(decision);				
				double count=0;
				for(String decisionInner:decisions) 
					if(decisionInner.equals(decision)) count++;
				double temp=count/total;
				subTotal+=-(temp)*(Math.log(temp)/Math.log(2));
			}
		}
				
		return subTotal;
	}

}

	
class WriteDiscretizedTableToOutput 

{
	
public static void writeOutputToFile(Table table) {

	try {
		
		File file = new File(table.getTableName() + ".int");
		FileWriter fstreamWrite = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstreamWrite);
		for (String attribute : table.getAttributeNames()) {
			out.write(attribute);
			out.newLine();
			ArrayList<Double> listOfCutPoints = table.getCutpointsMap()
					.get(attribute);
			if (listOfCutPoints.isEmpty()) {
				out.write(table.getAttrMinMaxMap().get(attribute).getMin()
						+ ".."
						+ table.getAttrMinMaxMap().get(attribute).getMax());
				out.newLine();
			} else {
				for (int index = 0; index < listOfCutPoints.size(); index++) {
					if (index == 0) {
						out.write(table.getAttrMinMaxMap().get(attribute)
								.getMin()
								+ ".." + listOfCutPoints.get(index));
						out.newLine();
					}
					if (index == listOfCutPoints.size() - 1) {
						out.write(listOfCutPoints.get(index)
								+ ".."
								+ table.getAttrMinMaxMap().get(attribute)
										.getMax());
						out.newLine();
					} else {
						out.write(listOfCutPoints.get(index) + ".."
								+ listOfCutPoints.get(index + 1));
						out.newLine();
					}
				}
			}

		}
		out.close();

		
		File discFile = new File(table.getTableName() + ".data");
		FileWriter discFstreamWrite = new FileWriter(discFile);
		BufferedWriter discOut = new BufferedWriter(discFstreamWrite);

		for (String attributeName : table.getAttributeNames()) {
			discOut.write(attributeName + " ");
		}
		discOut.write(table.getDecisionName());
		discOut.newLine();
		for (Row row : table.getRows()) {

			for (AttributeData data : row.getAttributeValues()) {
				discOut.write(data.getInterval() + " ");
			}
			discOut.write(row.getDecision());
			
			discOut.newLine();
		}
		discOut.close();

	}  catch (Exception e) {
					e.printStackTrace();
	}
}

}