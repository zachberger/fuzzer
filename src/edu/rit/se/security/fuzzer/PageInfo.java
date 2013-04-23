package edu.rit.se.security.fuzzer;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class PageInfo {

	URL rootURL;
	Map<HTTPMethod,Set<String>> supportedActions;
	
	public PageInfo(){
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
	
	public String toString(){
		return rootURL.toString();
	}
	
}
