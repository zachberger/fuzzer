package edu.rit.se.security.fuzzer;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class AttackSurfaceAnalyzer{
	
	public static void Analyze(LinkedList<PageInfo> pages){
		int getInputs = 0, postInputs = 0, putInputs = 0, deleteInputs = 0;
		for(PageInfo page : pages){
			getInputs += page.supportedActions.get(HTTPMethod.GET).size();
			postInputs += page.supportedActions.get(HTTPMethod.POST).size();
			putInputs += page.supportedActions.get(HTTPMethod.PUT).size();
			deleteInputs += page.supportedActions.get(HTTPMethod.DELETE).size();
		}
		
		Collections.sort(pages, new Comparator<PageInfo>(){
			public int compare(PageInfo o1, PageInfo o2) {
				return Integer.compare(o2.inputCount(), o1.inputCount());
		}});
		
		int pageCount = pages.size();
		int totalInput = getInputs + postInputs + putInputs + deleteInputs;
		
		System.out.println("**** Site Statistics ****");
		System.out.format("Total Inputs: %d    Average Inputs Per Page: %f%n", totalInput, ((double)totalInput)/pageCount);
		System.out.format("Total Get Inputs: %d    Average Get Inputs Per Page: %f%n", getInputs, ((double)getInputs)/pageCount);
		System.out.format("Total Post Inputs: %d    Average Post Inputs Per Page: %f%n", postInputs, ((double)postInputs)/pageCount);
		System.out.format("Total Put Inputs: %d    Average Put Inputs Per Page: %f%n", putInputs, ((double)putInputs)/pageCount);
		System.out.format("Total Delete Inputs: %d    Average Delete Inputs Per Page: %f%n%n%n", deleteInputs, ((double)deleteInputs)/pageCount);
		
		System.out.println("**** Page Statistics ****");
		for(PageInfo page : pages){
			System.out.println("Page: " + page.rootURL.toString());
			System.out.format("Total Inputs: %d    Percent Of Site: %f%n", page.inputCount(), (((double)page.inputCount())/totalInput)*100);
			System.out.format("Total Get Inputs: %d    Percent Of Site: %f%n", page.inputCount(HTTPMethod.GET), (((double)page.inputCount(HTTPMethod.GET))/totalInput)*100);
			System.out.format("Total Post Inputs: %d    Percent Of Site: %f%n", page.inputCount(HTTPMethod.POST), (((double)page.inputCount(HTTPMethod.POST))/totalInput)*100);
			System.out.format("Total Put Inputs: %d    Percent Of Site: %f%n", page.inputCount(HTTPMethod.PUT), (((double)page.inputCount(HTTPMethod.PUT))/totalInput)*100);
			System.out.format("Total Delete Inputs: %d    Percent Of Site: %f%n%n", page.inputCount(HTTPMethod.DELETE), (((double)page.inputCount(HTTPMethod.DELETE))/totalInput)*100);
		}
	}

}
