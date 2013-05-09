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

import com.gargoylesoftware.htmlunit.HttpMethod;

public class AttackSurfaceAnalyzer{

	public static void analyze(Map<String, Set<PageInfo>> pageSet) throws MalformedURLException{
		int getInputs = 0, postInputs = 0, putInputs = 0, deleteInputs = 0;
		List<PageInfo> pages = new LinkedList<PageInfo>();
		for(String rootPage : pageSet.keySet()){
			PageInfo p = new PageInfo(new URL(rootPage));	
			for(PageInfo page : pageSet.get(rootPage) ){
				p.supportedActions.get(HttpMethod.GET).addAll(page.supportedActions.get(HttpMethod.GET));
				p.supportedActions.get(HttpMethod.PUT).addAll(page.supportedActions.get(HttpMethod.PUT));
				p.supportedActions.get(HttpMethod.POST).addAll(page.supportedActions.get(HttpMethod.POST));
				p.supportedActions.get(HttpMethod.DELETE).addAll(page.supportedActions.get(HttpMethod.DELETE));
			}
			pages.add(p);

			getInputs += p.supportedActions.get(HttpMethod.GET).size();
			postInputs += p.supportedActions.get(HttpMethod.POST).size();
			putInputs += p.supportedActions.get(HttpMethod.PUT).size();
			deleteInputs += p.supportedActions.get(HttpMethod.DELETE).size();
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
			if(page.inputCount()>0){
				System.out.println("Page: " + page.rootURL.toString());
				System.out.format("\tTotal Inputs: %d \tPercent Of Site: %f%n", page.inputCount(), (((double)page.inputCount())/totalInput)*100);
				System.out.format("\tTotal Get Inputs: %d \tPercent Of Site: %f%n", page.inputCount(HttpMethod.GET), (((double)page.inputCount(HttpMethod.GET))/totalInput)*100);
				if(page.inputCount(HttpMethod.GET)>0)
					System.out.format("\t\tInput Names: %s\n", listInput(page.supportedActions.get(HttpMethod.GET)));
				System.out.format("\tTotal Post Inputs: %d \tPercent Of Site: %f%n", page.inputCount(HttpMethod.POST), (((double)page.inputCount(HttpMethod.POST))/totalInput)*100);
				if(page.inputCount(HttpMethod.POST)>0)
					System.out.format("\t\tInput Names: %s\n", listInput(page.supportedActions.get(HttpMethod.POST)));
				System.out.format("\tTotal Put Inputs: %d \tPercent Of Site: %f%n", page.inputCount(HttpMethod.PUT), (((double)page.inputCount(HttpMethod.PUT))/totalInput)*100);
				if(page.inputCount(HttpMethod.PUT)>0)
					System.out.format("\t\tInput Names: %s\n", listInput(page.supportedActions.get(HttpMethod.PUT)));
				System.out.format("\tTotal Delete Inputs: %d \tPercent Of Site: %f%n%n", page.inputCount(HttpMethod.DELETE), (((double)page.inputCount(HttpMethod.DELETE))/totalInput)*100);
				if(page.inputCount(HttpMethod.DELETE)>0)
					System.out.format("\t\tInput Names: %s\n", listInput(page.supportedActions.get(HttpMethod.GET)));
			}
		}
	}

	public static String listInput(Set<String> set){
		String output = "";
		for(String input : set)
			output += input + "   ";

		return output;
	}

}
