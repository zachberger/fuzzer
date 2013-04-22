package edu.rit.se.security.fuzzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
			List<String> myListNames = null, myListExtensions = null;
			File page_names = new File("resources/page_names.txt");
			File extensions = new File("resources/extensions.txt");
			try {
				myListNames =  getPageNames(page_names);
				for (String name : myListNames) System.out.println(name);
				myListExtensions =  getPageNames(extensions);
				for (String ext : myListExtensions) System.out.println(ext);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			discoverUnlinkedPages(myListNames, myListExtensions, wc);
			return true;
		}catch (FailingHttpStatusCodeException | IOException e) {
			System.err.println("Exception in PageEnumerator: " + e.getMessage());
			return false;
		}

	}
	
	public Set<PageInfo> getResults(){
		return foundPages;
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
	
	private void discoverUnlinkedPages(List<String> pageNames, List<String> extensions, WebClient webClient){
		for(String page : pageNames){
			for(String ext : extensions){
				String pageURL = "/" + page +  "." + ext;
				try {
					HtmlPage success = webClient.getPage( rootURL + pageURL );
					PageInfo p = new PageInfo();
					p.rootURL = success.getUrl();
					foundPages.add(p);
				} catch (FailingHttpStatusCodeException | IOException e) {
					// TODO Auto-generated catch block
				}
			}
		}
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private List<String> getPageNames( File file ) throws FileNotFoundException, IOException{
		
		List<String> myList = new LinkedList<>();
		try (FileReader fileReader = new FileReader(file)) {
			try (BufferedReader br = new BufferedReader(fileReader)){ 
				String sCurrentLine;
				while ((sCurrentLine = br.readLine()) != null) {
					myList.add(sCurrentLine);
				}
			}
		} 
		return myList;
	}


		public static void main(String[] args) throws MalformedURLException {
			String rootURL = "http://localhost:8080/bodgeit";

			PageEnumerator pageEnumerator = new PageEnumerator(new URL(rootURL));
			pageEnumerator.start();
			for( PageInfo i : pageEnumerator.getResults() ){
				System.out.println(i.rootURL);
			}

		}
}
