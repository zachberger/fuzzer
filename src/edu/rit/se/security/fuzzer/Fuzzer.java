package edu.rit.se.security.fuzzer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

public class Fuzzer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PageInfo pageInfo = new PageInfo();
		try {
			pageInfo.rootURL = new URL("http://www.w3schools.com/html/html_forms.asp");
			pageInfo.discoverInputs();
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			
		}
	}

}
