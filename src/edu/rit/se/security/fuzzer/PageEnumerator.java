package edu.rit.se.security.fuzzer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class PageEnumerator {

	private final URL rootURL;
	private Set<PageInfo> foundPages;
	private Logger logger = Logger.getLogger( PageEnumerator.class );
		
	public PageEnumerator(URL rootURL){
		this.rootURL = rootURL;
		foundPages = new HashSet<PageInfo>();
	}
	
	public boolean start(){
		WebClient wc = new WebClient();
		try{
			discoverLinks( wc, rootURL );
			tryCommonURLS( wc );
			return true;
		}catch (FailingHttpStatusCodeException | IOException e) {
			System.err.println("Exception in PageEnumerator: " + e.getMessage());
			return false;
		}
	}
	
	public Set<PageInfo> getResults(){
		return foundPages;
	}
	
	private void tryCommonURLS(WebClient wc) {
		// TODO Auto-generated method stub
		
	}
	
	private void discoverLinks( WebClient webClient, URL rootURL ) 
			throws FailingHttpStatusCodeException, IOException {
		HtmlPage page = webClient.getPage( rootURL );
		URL newURL;
		String contentType;
		for( HtmlAnchor link : page.getAnchors() ) {
			try{			
				Page newPage = link.openLinkInNewWindow();
				newURL = newPage.getUrl();
				contentType = page.getWebResponse().getContentType();
				System.err.println( contentType );
				if( !contentType.equals("text/html") ){
					System.err.println("Ignorning " + contentType + ": " +  newURL );
					continue;
				}
				if( !newURL.getHost().equals(rootURL.getHost())){
					logger.warn( "Ignoring off domain url: " + newURL );
					continue;
				}
			}catch( MalformedURLException | ClassCastException e ){
				logger.error( e.getMessage() );
				logger.warn("Skipping malformed url/response: " + link.getHrefAttribute() );
				continue;
			}

			
			PageInfo i = new PageInfo();
			i.rootURL = newURL;
			
			if( !foundPages.contains(i) ){
				System.out.println("Found new "+contentType+": " + newURL );
				i.supportedActions.get(HTTPMethod.GET).add(newURL.getQuery());
				foundPages.add( i );
				discoverLinks( webClient, newURL );
			}
		}
	}

}
