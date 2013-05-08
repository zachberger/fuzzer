package edu.rit.se.security.fuzzer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.util.Cookie;

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
			
			// Set the security to low
			Cookie security = wc.getCookieManager().getCookie("security");
			if(security != null){
				wc.getCookieManager().removeCookie(security);
			}
			wc.getCookieManager().addCookie(new Cookie(rootURL.getHost(), "security", "low"));
			
			HtmlPage page = wc.getPage("http://127.0.0.1/dvwa/security");
			
		} catch (FailingHttpStatusCodeException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws MalformedURLException{
		BasicConfigurator.configure( new NullAppender() );
		PageEnumerator p = new DVWAPageEnumerator(new URL("http://127.0.0.1/dvwa/login.php"));
		p.start();
	}
}
