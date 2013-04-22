package edu.rit.se.security.fuzzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class PageEnumerator {

	protected static Map<String, HtmlPage> foundPages;
	protected WebClient webClient;
	protected String rootURL;
	
	/**
	 * 
	 * @param webClient
	 * @param rootURL
	 */
	public PageEnumerator(WebClient webClient, String rootURL ){
		//BasicConfigurator.configure();
		
		this.webClient = webClient;
		this.rootURL = rootURL;
		foundPages = new HashMap<>();
	}
	
	/**
	 * 
	 * @param webClient
	 * @param rootURL
	 */
	private void discoverLinks() {
		HtmlPage page;
		try {
			page = webClient.getPage( rootURL );
			List<HtmlAnchor> links = page.getAnchors();
			for (HtmlAnchor link : links) {
				System.out.println("Link discovered: " + link.asText() + " @URL=" + link.getHrefAttribute());
			}
		} catch (FailingHttpStatusCodeException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void discoverUnlinkedPages(List<String> pageNames, List<String> extensions){
		for(String page : pageNames){
			for(String ext : extensions){
				String pageURL = "/" + page +  "." + ext;
				try {
					HtmlPage success = webClient.getPage( rootURL + pageURL );
					foundPages.put(pageURL, success);
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
	private List<String> getPageNames(File file) throws FileNotFoundException, IOException{
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


		public static void main(String[] args) {
			WebClient webClient = new WebClient();
			String rootURL = "http://localhost:8080/bodgeit";
			//List<String> myListNames = Arrays.asList(new String [] {"admin", "default","store"}); 
			//List<String> myListExtensions = Arrays.asList(new String [] {"htm", "jsp"}); 

			File page_names = new File("resources/page_names.txt");
			File extensions = new File("resources/extensions.txt");
			
			PageEnumerator pageEnumerator = new PageEnumerator(webClient, rootURL);
			List<String> myListNames = null, myListExtensions = null;
			
			try {
				myListNames =  pageEnumerator.getPageNames(page_names);
				for (String name : myListNames) System.out.println(name);
				myListExtensions =  pageEnumerator.getPageNames(extensions);
				for (String ext : myListExtensions) System.out.println(ext);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			pageEnumerator.discoverUnlinkedPages(myListNames, myListExtensions);
			
			for (Map.Entry<String, HtmlPage> entry : foundPages.entrySet()) {
			    String label = entry.getKey();
			    	System.out.println("Page: " + label);
			        System.out.println("Value: " + entry.getValue());
			}
		}
}