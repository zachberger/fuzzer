package edu.rit.se.security.fuzzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.UrlUtils;

public class PageEnumerator {

	protected URL rootURL;
	protected static String loginPath = null;
	protected static WebClient wc = new WebClient();
	protected final static List<Fuzzer> fuzzersToRun;
	private Random r = new Random( System.currentTimeMillis() );
	static{
		List<Fuzzer> tmp = new ArrayList<Fuzzer>();
		tmp.add( new SensitiveData() );
		tmp.add( new SanatizationFuzzer(wc));
		fuzzersToRun = Collections.unmodifiableList( tmp );
	}
	
	private Map<String, Set<PageInfo>> foundPages;
		
	public PageEnumerator(URL rootURL){
		this.rootURL = rootURL;
		foundPages = new HashMap<String, Set<PageInfo>>();
		wc.getOptions().setTimeout(0);
	}

	public void beforeStart(){}
	
	public boolean start(){
		beforeStart();
		try{
			System.out.println("******** Crawling For Pages ********");
			HtmlPage p = wc.getPage( rootURL );
			discoverLinks( p );
			List<String> myListNames = null, myListExtensions = null;
			File page_names = new File("resources/page_names.txt");
			File extensions = new File("resources/extensions.txt");
			try {
				myListNames =  getLines(page_names);
				myListExtensions =  getLines(extensions);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("******** Discovering Un-Linked Pages ********");
			discoverUnlinkedPages(myListNames, myListExtensions, wc);
			AttackSurfaceAnalyzer.analyze(foundPages);
			
			System.out.println("******** Fuzzing Pages ********");
			List<Set<PageInfo>> pagesToFuzz = new ArrayList<Set<PageInfo>>();
			for( Set<PageInfo> toFuzz : foundPages.values() ){
				if( r.nextInt( 100 ) < Settings.RANDOM_FACTOR ){
					pagesToFuzz.add(toFuzz);
				}
			}
			for( Fuzzer f : fuzzersToRun ){
				for( Set<PageInfo> toFuzz : pagesToFuzz ){
					f.fuzz(toFuzz);
				}
			}			
			
			if(loginPath != null){
				System.out.println("******** Guessing Passwords ********");
				PasswordGuesser.guessPassword(loginPath);
			}
			
			return true;
		}catch(IOException e) {
			System.err.println("Exception in PageEnumerator: " + e.getMessage());
			return false;
		}

	}
	
	public Map<String, Set<PageInfo>> getResults(){
		return foundPages;
	}
	
	protected void discoverLinks( HtmlPage page ) throws IOException {
		if( isPageUnique( page.getUrl() ) ){
			addPage(page);
			for( HtmlAnchor link : page.getAnchors() ) {
				try{			
					URL newURL = new URL( UrlUtils.resolveUrl(page.getUrl(), link.getHrefAttribute() ) );
					if( !newURL.getHost().equals(rootURL.getHost())){
						//System.err.println( "Ignoring off domain url: " + newURL );
						continue;
					}
					if(!exemptPage(newURL))
						discoverLinks( (HtmlPage) link.click() );
				}catch(Exception e ){
					System.err.println(e.getMessage());
					continue;
				}
			}
		} else {
			return;
		}
	}
	
	protected boolean exemptPage( URL url ){
		return url.getFile().equals("/bodgeit/logout.jsp");
	}
	
	private boolean isPageUnique( URL newURL ){
		try {
			PageInfo pageInfo = new PageInfo( newURL );
			if(foundPages.containsKey(pageInfo.rootURL.toString()))
				return !foundPages.get(pageInfo.rootURL.toString()).contains(pageInfo);
			else
				return true;
			
		} catch (MalformedURLException e) {
			return false;
		}
	}
	
	private void discoverUnlinkedPages(List<String> pageNames, List<String> extensions, WebClient webClient){
		for(String page : pageNames){
			for(String ext : extensions){
				String pageURL = page +  "." + ext;
				try {
					HtmlPage success = webClient.getPage( new URL(rootURL,pageURL) );
					PageInfo p = new PageInfo( success.getUrl(), success,wc.getCookieManager().getCookies() );
					if(foundPages.get(p.rootURL.toString()) == null)
						foundPages.put(p.rootURL.toString(), new HashSet<PageInfo>());
					foundPages.get(p.rootURL.toString()).add(p);
				} catch (Exception e) {
					//System.err.println(e.getMessage());
				}
			}
		}
	}
	
	private void addPage(HtmlPage page ) throws FailingHttpStatusCodeException, IOException{
		System.out.println("Found new page: " + page.getUrl() );
		PageInfo pageInfo = new PageInfo( page.getUrl(), page, wc.getCookieManager().getCookies()  );
		if(foundPages.get(pageInfo.rootURL.toString()) == null)
			foundPages.put(pageInfo.rootURL.toString(), new HashSet<PageInfo>());
		foundPages.get(pageInfo.rootURL.toString()).add(pageInfo);
		discoverFormInputs(pageInfo, page );
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	protected static List<String> getLines( File file ) throws FileNotFoundException, IOException{	
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
	
	/**
	 * Discovers all inputs of the page and stores them in supportedActions
	 * 
	 * @throws FailingHttpStatusCodeException
	 * @throws IOException
	 */
	public void discoverFormInputs(PageInfo pageInfo, HtmlPage page) throws FailingHttpStatusCodeException, IOException{
		for(HtmlForm form : page.getForms()){
			Set<String> methodInputs = null;
			switch(form.getMethodAttribute().toLowerCase()){
				case "post": methodInputs = pageInfo.supportedActions.get(HttpMethod.POST); break;
				case "get": methodInputs = pageInfo.supportedActions.get(HttpMethod.GET); break;
				case "put": methodInputs = pageInfo.supportedActions.get(HttpMethod.PUT); break;
				case "delete": methodInputs = pageInfo.supportedActions.get(HttpMethod.DELETE); break;
				default: methodInputs = pageInfo.supportedActions.get(HttpMethod.GET); // No form method was specified, defaults to GET
			}
			if(methodInputs != null){ // Ensure that the form is set
				for(DomAttr input : (List<DomAttr>)form.getByXPath("//input/@name")){
					methodInputs.add(input.getValue());
				}
			}
		}
	}
	
	public void discoverInputs(PageInfo pageInfo, WebClient webClient) throws FailingHttpStatusCodeException, IOException{
		try{
			HtmlPage page = webClient.getPage(pageInfo.rootURL);
			discoverFormInputs(pageInfo, page);
		} catch(ClassCastException e){
		
		}
	}
	
		public static void main(String[] args) throws MalformedURLException {
			// DVWA
			//String rootURL = "http://127.0.0.1/dvwa/login.php";
			//BodgetIt
			//String rootURL = "http://localhost:8080/bodgeit/";
			//loginPath = "http://localhost:8080/bodgeit/login.jsp";
			//JPetStore
			String rootURL = "http://127.0.0.1:8080/jpetstore/";
			BasicConfigurator.configure( new NullAppender() );
			PageEnumerator pageEnumerator = new PageEnumerator(new URL(rootURL));
			pageEnumerator.start();
		}
}
