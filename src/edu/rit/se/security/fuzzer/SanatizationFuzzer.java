package edu.rit.se.security.fuzzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

public class SanatizationFuzzer extends Object implements Fuzzer {

	public static List<String> xss_vectors;
	public static List<String> sql_vectors;

	private WebClient wc;
	
	public SanatizationFuzzer(WebClient wc){
		this.wc = wc;
		
	}

	@Override
	public void fuzz(Set<PageInfo> pages) {
		try {
			if (xss_vectors == null || sql_vectors == null) {
				File xss_file = new File("resources/fuzz_vectors_xss.txt");
				File sql_file = new File("resources/fuzz_vectors_sql.txt");

				xss_vectors = PageEnumerator.getLines(xss_file);
				sql_vectors = PageEnumerator.getLines(sql_file);
			}

			for(String xss_vec : xss_vectors){
				for(PageInfo page : pages){
					for(HttpMethod action : page.supportedActions.keySet()){
						if(page.supportedActions.get(action).size() > 0){
							executeVector(xss_vec, action, page);
							if(wc.getCookieManager().getCookie("xss") != null){
								// The XSS was executed
								Cookie cookie = wc.getCookieManager().getCookie("xss");
								wc.getCookieManager().removeCookie(cookie);
								System.out.println("Found XSS vulnerability on page: "+page.rootURL.toString());
								if(!"".equals(page.query))
									System.out.println("\twith query: "+page.query);
								System.out.println("\tUsing vector: " + xss_vec);
							}
						}
					}
				}
			}
			
			for(String sql_vec : sql_vectors){
				for(PageInfo page : pages){
					for(HttpMethod action : page.supportedActions.keySet()){
						if(page.supportedActions.get(action).size() > 0){
							HtmlPage result = executeVector(sql_vec, action, page);
							if(result != null && result.asText().contains("MySQL ")){
								System.out.println("Potential SQL Injection vulnerability on page: "+page.rootURL.toString());
								if(!"".equals(page.query))
									System.out.println("\twith query: "+page.query);
								System.out.println("\tUsing vector: " + sql_vec);
							}
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private HtmlPage executeVector(String vector, HttpMethod action, PageInfo pageInfo){
		// Instead of requesting the page directly we create a WebRequestSettings object
		WebRequest request = new WebRequest(
				pageInfo.rootURL, action);

		List<NameValuePair> inputs = new LinkedList<NameValuePair>();
		for(String input : pageInfo.supportedActions.get(action)){
			inputs.add(new NameValuePair(input, vector));
		}
		// Then we set the request parameters
		request.setRequestParameters(inputs);

		// Finally, we can get the page
		try {
			return wc.getPage(request);
		} catch (FailingHttpStatusCodeException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

	}

}
