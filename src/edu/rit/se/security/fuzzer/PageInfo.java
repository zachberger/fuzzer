package edu.rit.se.security.fuzzer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class PageInfo {

	URL rootURL;
	Map<HTTPMethod,Set<String>> supportedActions;
	HtmlPage page;
		
	public PageInfo( URL newURL ) throws MalformedURLException{
		if(newURL.getQuery() == null){
			rootURL = newURL;
		} else {
			rootURL = new URL(newURL.toString().replace("?" + newURL.getQuery(), ""));
		}
		supportedActions = new HashMap<HTTPMethod,Set<String>>();
		supportedActions.put(HTTPMethod.GET, new HashSet<String>());	
		supportedActions.put(HTTPMethod.PUT, new HashSet<String>());	
		supportedActions.put(HTTPMethod.POST, new HashSet<String>());	
		supportedActions.put(HTTPMethod.DELETE, new HashSet<String>());		
	}
	
	@Override
	public boolean equals( Object o ){
		if( o instanceof PageInfo ){
			PageInfo castedObject = (PageInfo) o;
			if( castedObject.rootURL != null ){
				String origQuery = rootURL.getQuery() == null ? "" : rootURL.getQuery();
				String origNoQuery = rootURL.toString().replace("?"+origQuery, "");
				String castedQuery = castedObject.rootURL.getQuery() == null ? "" : castedObject.rootURL.getQuery();
				String castedNoQuery = castedObject.rootURL.toString().replace("?"+castedQuery,"");
				//System.out.println(origNoQuery + ":" +castedNoQuery);
				return origNoQuery.equals(castedNoQuery);
			}
		}
		return false;
	}
	
	public int hashCode(){
		String origQuery = rootURL.getQuery() == null ? "" : rootURL.getQuery();
		String origNoQuery = rootURL.toString().replace("?"+origQuery, "");
		return origNoQuery.hashCode();
	}
	
	public int inputCount(){
		return inputCount(HTTPMethod.GET) + inputCount(HTTPMethod.POST) + 
				inputCount(HTTPMethod.PUT) + inputCount(HTTPMethod.DELETE);
	}
	
	public int inputCount(HTTPMethod method){
		return supportedActions.get(method).size();
	}
	
	public String toString(){
		return rootURL.toString();
	}
	
}
