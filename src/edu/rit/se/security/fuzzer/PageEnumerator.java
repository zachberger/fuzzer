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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.UrlUtils;

public class PageEnumerator {

	protected URL rootURL;
	protected final static List<Fuzzer> fuzzersToRun;
	private Random r = new Random( System.currentTimeMillis() );
	static{
		List<Fuzzer> tmp = new ArrayList<Fuzzer>();
		tmp.add( new PasswordGuesser() );
		tmp.add( new SensitiveData() );
		fuzzersToRun = Collections.unmodifiableList( tmp );
	}
	
	private Set<PageInfo> foundPages;
	protected WebClient wc;
		
	public PageEnumerator(URL rootURL){
		this.rootURL = rootURL;
		foundPages = new HashSet<PageInfo>();
		wc = new WebClient();
		wc.getOptions().setTimeout(0);
	}

	public void beforeStart(){}
	
	public boolean start(){
		//beforeStart();
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
			AttackSurfaceAnalyzer.analyze(new LinkedList<PageInfo>(this.getResults()));
			
			System.out.println("******** Fuzzing Pages ********");
			List<PageInfo> pagesToFuzz = new ArrayList<PageInfo>();
			for( PageInfo toFuzz : foundPages ){
				if( r.nextInt( 100 ) < Settings.RANDOM_FACTOR ){
					pagesToFuzz.add(toFuzz);
				}
			}
			for( Fuzzer f : fuzzersToRun ){
				for( PageInfo toFuzz : pagesToFuzz ){
					f.fuzz(toFuzz);
				}
			}			
			return true;
		}catch(IOException e) {
			System.err.println("Exception in PageEnumerator: " + e.getMessage());
			return false;
		}

	}
	
	public Set<PageInfo> getResults(){
		return foundPages;
	}
	
	private void discoverLinks( HtmlPage page ) throws IOException {
		if( isPageUnique( page.getUrl() ) ){
			addPage(page);
			for( HtmlAnchor link : page.getAnchors() ) {
				try{			
					URL newURL = new URL( UrlUtils.resolveUrl(page.getUrl(), link.getHrefAttribute() ) );
					if( !newURL.getHost().equals(rootURL.getHost())){
						//System.err.println( "Ignoring off domain url: " + newURL );
						continue;
					}
					discoverLinks( (HtmlPage) link.click() );
				}catch(Exception e ){
					System.err.println(e.getMessage());
					continue;
				}
			}
			
			for( HtmlElement e : page.getHtmlElementDescendants() ){
				if( e.hasAttribute("onclick") ){
					HtmlPage p = e.click();
					URL newURL = new URL( UrlUtils.resolveUrl(page.getUrl(), p.getUrl().getPath() ) );
					//If we're on a new page
					if( !newURL.equals(page.getUrl()) ){
						discoverLinks( p );
					}
				}
			}
		}else{
			PageInfo np = new PageInfo( page.getUrl() );
			for( PageInfo p : foundPages ){
				if( p.equals(np) ){
					addActions(page.getUrl().getQuery(), p);
					break;
				}
			}	
		}
	}
	
	private boolean isPageUnique( URL newURL ){
		try {
			PageInfo pageInfo = new PageInfo( newURL );
			return !foundPages.contains(pageInfo);
		} catch (MalformedURLException e) {
			return false;
		}
	}
	
	private void addActions(String query, PageInfo pageInfo){
		if( query != null ){
			String[] params = query.split("&|;");
			Set<String> actions = pageInfo.supportedActions.get(HTTPMethod.GET);
			for(String param : params){
				actions.add(param.split("=",2)[0].trim());
			}
		}
	}
	
	private void discoverUnlinkedPages(List<String> pageNames, List<String> extensions, WebClient webClient){
		for(String page : pageNames){
			for(String ext : extensions){
				String pageURL = page +  "." + ext;
				try {
					HtmlPage success = webClient.getPage( new URL(rootURL,pageURL) );
					PageInfo p = new PageInfo( success.getUrl(), success,wc.getCookieManager().getCookies() );
					foundPages.add(p);
				} catch (Exception e) {
					//System.err.println(e.getMessage());
				}
			}
		}
	}
	
	private void addPage(HtmlPage page ) throws FailingHttpStatusCodeException, IOException{
		//System.out.println("Found new page: " + oldPage.getUrl() );
		PageInfo pageInfo = new PageInfo( page.getUrl(), page, wc.getCookieManager().getCookies()  );
		foundPages.add(pageInfo);
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
				case "post": methodInputs = pageInfo.supportedActions.get(HTTPMethod.POST); break;
				case "get": methodInputs = pageInfo.supportedActions.get(HTTPMethod.GET); break;
				case "put": methodInputs = pageInfo.supportedActions.get(HTTPMethod.PUT); break;
				case "delete": methodInputs = pageInfo.supportedActions.get(HTTPMethod.DELETE); break;
				default: methodInputs = pageInfo.supportedActions.get(HTTPMethod.GET); // No form method was specified, defaults to GET
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
			//Bodget
			String rootURL = "http://localhost:8080/bodgeit/";
			BasicConfigurator.configure( new NullAppender() );
			PageEnumerator pageEnumerator = new PageEnumerator(new URL(rootURL));
			pageEnumerator.start();
		}
}
