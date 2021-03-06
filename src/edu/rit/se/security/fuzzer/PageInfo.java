package edu.rit.se.security.fuzzer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;

public class PageInfo {

	URL rootURL;
	Map<HttpMethod,Set<String>> supportedActions;
	HtmlPage page;
	HashSet<Cookie> cookies;
	String query;
		
	public PageInfo( URL newURL ) throws MalformedURLException{
		if(newURL.getQuery() == null){
			rootURL = newURL;
			query = "";
		} else {
			rootURL = new URL(newURL.toString().replace("?" + newURL.getQuery(), ""));
			rootURL = new URL(rootURL.toString().replaceFirst("#.*", ""));
			query = "?" + newURL.getQuery();
			query = query.replaceFirst("#.*", "");
		}
		supportedActions = new HashMap<HttpMethod,Set<String>>();
		supportedActions.put(HttpMethod.GET, new HashSet<String>());	
		supportedActions.put(HttpMethod.PUT, new HashSet<String>());	
		supportedActions.put(HttpMethod.POST, new HashSet<String>());	
		supportedActions.put(HttpMethod.DELETE, new HashSet<String>());		
	}
	
	public PageInfo( URL newURL, HtmlPage page, Set<Cookie> set ) throws MalformedURLException{
		this( newURL );
		this.page = page;
		this.cookies = new HashSet<Cookie>( set );
	}
	
	@Override
	public boolean equals( Object o ){
		if( o instanceof PageInfo ){
			PageInfo castedObject = (PageInfo) o;
			if( castedObject.rootURL != null ){
				return rootURL.toString().equals(castedObject.rootURL.toString()) && query.equals(castedObject.query);
			}
		}
		return false;
	}
	
	public int hashCode(){
		String origQuery = rootURL.getQuery() == null ? "" : rootURL.getQuery();
		String origNoQuery = rootURL.toString().replace("?"+origQuery, "");
		//return origNoQuery.hashCode();
		return (rootURL.toString()+"?"+query).hashCode();

	}
	
	public int inputCount(){
		return inputCount(HttpMethod.GET) + inputCount(HttpMethod.POST) + 
				inputCount(HttpMethod.PUT) + inputCount(HttpMethod.DELETE);
	}
	
	public int inputCount(HttpMethod method){
		return supportedActions.get(method).size();
	}
	
	public String toString(){
		return rootURL.toString();
	}
	
}
