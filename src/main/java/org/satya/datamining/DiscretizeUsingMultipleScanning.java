
package org.satya.datamining;

import java.io.*;
import java.util.Scanner;

public class DiscretizeUsingMultipleScanning 
	{

		public static void main(String[] args) 
			{
				try 
				{
		
					System.out.println("Please enter the input file name:");
					Scanner in=new Scanner(System.in);
					String filePath=in.next();
					boolean validatePath=validateFilePath(filePath);
		
					while(validatePath==false)
					{
			
						System.out.println("unable to read the input file\n please enter  your choice from below options:");
						System.out.println("1.Enter the input file name again \n2.Exit");
			
						int choice=in.nextInt();
			
						if(choice==1)
							{
								System.out.println("Please enter the input file name:");
								filePath=in.next();
						
								validatePath=validateFilePath(filePath);
				
							}
						else
							{
								System.out.println("\nexiting");
								System.exit(0);
							}
					}//End of While
		
					System.out.println("Please enter the number of scans:");
					int numberOfScans = in.nextInt();
					in.close();
					
					CreatingTableFromInputFile TabCreator =new CreatingTableFromInputFile(); // if possible try to change the class name here as well ;)
		
					Table table=TabCreator.createTable(filePath);	
		
					boolean isTableConsistent = false ;
		
					for(int i=1; i<=numberOfScans; i++)
						{
							Table subTable=Table.getInconsistentSubTable(table);
							for (String attr:table.attributeNames)
				
							{
					
								table.addCutPoint(attr, DiscretizeUsingDominantAttribute.getBestCutPointForBestAttribute(subTable, attr));
					
							}
							
							if(table.isConsistent()) 
							{
								isTableConsistent = true ;
								break;
							}
						}
	
		
					if (isTableConsistent )
						{
							System.out.println("merging is being done by dropping cutpoints");
							table.dropCutpoints();
							WriteDiscretizedTableToOutput.writeOutputToFile(table);
							System.out.println("Finished creating the output files test.int and test.data");
						}
		
					else 
						{
							DiscretizeUsingDominantAttribute.discretize(table);
						}
			
				
		}
				
		catch (Exception e) {
			System.out.println("Please check the input file to check whether it is in proper LERS format or not");
		}
				
	}//End of Main method
	
		
		public static boolean validateFilePath(String filePath)
		{
			
			boolean flag=true;
			
			try
			{
				FileReader fstream = new FileReader(filePath);
				BufferedReader in = new BufferedReader(fstream);
				in.close();
			
			}
			catch (IOException e)
			{
				flag=false;
			} 
			
			return flag;
			
		}
	
}