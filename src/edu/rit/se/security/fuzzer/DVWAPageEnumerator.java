package edu.rit.se.security.fuzzer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class DVWAPageEnumerator extends PageEnumerator {

	public DVWAPageEnumerator(URL rootURL) {
		super(rootURL);
	}

	@Override
	public void beforeStart(){
		try {
			HtmlPage loginPage = wc.getPage(rootURL);
			HtmlForm loginForm = loginPage.getForms().get(0);
			loginForm.getInputByName("username").setValueAttribute("admin");
			loginForm.getInputByName("password").setValueAttribute("password");
			HtmlSubmitInput submit = loginForm.getInputByName("Login");
			HtmlPage dvwaPage = submit.click();
			rootURL = dvwaPage.getUrl();
		} catch (FailingHttpStatusCodeException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws MalformedURLException{
		PageEnumerator p = new DVWAPageEnumerator(new URL("http://127.0.0.1/dvwa/login.php"));
		p.start();
	}
}
