package edu.rit.se.security.fuzzer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttackSurfaceAnalyzer{
	
	public static void analyze(Map<String, Set<PageInfo>> pageSet) throws MalformedURLException{
		int getInputs = 0, postInputs = 0, putInputs = 0, deleteInputs = 0;
		List<PageInfo> pages = new LinkedList<PageInfo>();
		for(String rootPage : pageSet.keySet()){
			PageInfo p = new PageInfo(new URL(rootPage));	
			for(PageInfo page : pageSet.get(rootPage) ){
				p.supportedActions.get(HTTPMethod.GET).addAll(page.supportedActions.get(HTTPMethod.GET));
				p.supportedActions.get(HTTPMethod.PUT).addAll(page.supportedActions.get(HTTPMethod.PUT));
				p.supportedActions.get(HTTPMethod.POST).addAll(page.supportedActions.get(HTTPMethod.POST));
				p.supportedActions.get(HTTPMethod.DELETE).addAll(page.supportedActions.get(HTTPMethod.DELETE));
			}
			pages.add(p);

			getInputs += p.supportedActions.get(HTTPMethod.GET).size();
			postInputs += p.supportedActions.get(HTTPMethod.POST).size();
			putInputs += p.supportedActions.get(HTTPMethod.PUT).size();
			deleteInputs += p.supportedActions.get(HTTPMethod.DELETE).size();
		}
		
		Collections.sort(pages, new Comparator<PageInfo>(){
			public int compare(PageInfo o1, PageInfo o2) {
				return Integer.compare(o2.inputCount(), o1.inputCount());
		}});
		
		int pageCount = pages.size();
		int totalInput = getInputs + postInputs + putInputs + deleteInputs;
		
		System.out.println("**** Site Statistics ****");
		System.out.format("Total Number of Pages Found: %d\n", pages.size());
		System.out.format("Total Inputs: %d \tAverage Inputs Per Page: %f%n", totalInput, ((double)totalInput)/pageCount);
		System.out.format("Total Get Inputs: %d \tAverage Get Inputs Per Page: %f%n", getInputs, ((double)getInputs)/pageCount);
		System.out.format("Total Post Inputs: %d \tAverage Post Inputs Per Page: %f%n", postInputs, ((double)postInputs)/pageCount);
		System.out.format("Total Put Inputs: %d \tAverage Put Inputs Per Page: %f%n", putInputs, ((double)putInputs)/pageCount);
		System.out.format("Total Delete Inputs: %d \tAverage Delete Inputs Per Page: %f%n%n%n", deleteInputs, ((double)deleteInputs)/pageCount);
		
		System.out.println("**** Page Statistics ****");
		for(PageInfo page : pages){
			System.out.println("Page: " + page.rootURL.toString());
			System.out.format("\tTotal Inputs: %d \tPercent Of Site: %f%n", page.inputCount(), (((double)page.inputCount())/totalInput)*100);
			System.out.format("\tTotal Get Inputs: %d \tPercent Of Site: %f%n", page.inputCount(HTTPMethod.GET), (((double)page.inputCount(HTTPMethod.GET))/totalInput)*100);
			if(page.inputCount(HTTPMethod.GET)>0)
				System.out.format("\t\tInput Names: %s\n", listInput(page.supportedActions.get(HTTPMethod.GET)));
			System.out.format("\tTotal Post Inputs: %d \tPercent Of Site: %f%n", page.inputCount(HTTPMethod.POST), (((double)page.inputCount(HTTPMethod.POST))/totalInput)*100);
			if(page.inputCount(HTTPMethod.POST)>0)
				System.out.format("\t\tInput Names: %s\n", listInput(page.supportedActions.get(HTTPMethod.POST)));
			System.out.format("\tTotal Put Inputs: %d \tPercent Of Site: %f%n", page.inputCount(HTTPMethod.PUT), (((double)page.inputCount(HTTPMethod.PUT))/totalInput)*100);
			if(page.inputCount(HTTPMethod.PUT)>0)
				System.out.format("\t\tInput Names: %s\n", listInput(page.supportedActions.get(HTTPMethod.PUT)));
			System.out.format("\tTotal Delete Inputs: %d \tPercent Of Site: %f%n%n", page.inputCount(HTTPMethod.DELETE), (((double)page.inputCount(HTTPMethod.DELETE))/totalInput)*100);
			if(page.inputCount(HTTPMethod.DELETE)>0)
				System.out.format("\t\tInput Names: %s\n", listInput(page.supportedActions.get(HTTPMethod.GET)));
		}
	}
	
	public static String listInput(Set<String> set){
		String output = "";
		for(String input : set)
			output += input + "   ";
		
		return output;
	}

}
