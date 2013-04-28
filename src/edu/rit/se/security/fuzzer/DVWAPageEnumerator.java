package edu.rit.se.security.fuzzer;

import java.net.URL;

public class DVWAPageEnumerator extends PageEnumerator {

	public DVWAPageEnumerator(URL rootURL) {
		super(rootURL);
	}

	@Override
	public void beforeStart(){
		//login
	}

}
