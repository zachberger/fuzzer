package edu.rit.se.security.fuzzer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class PageEnumerator {

	Map<URL, Page> foundPages;
	
	
	private void discoverLinks( WebClient webClient, URL rootURL ) {
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

}
