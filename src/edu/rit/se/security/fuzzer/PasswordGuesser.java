package edu.rit.se.security.fuzzer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class PasswordGuesser {
	public static void guessPassword(String loginPage) {
		WebClient wc = new WebClient();
		wc.getOptions().setTimeout(0);
		try {
			HtmlPage p = wc.getPage(loginPage);
			List<String> myListUsers = null, myListPasswords = null;
			File user_names = new File("resources/user_guesses.txt");
			File passwords = new File("resources/password_guesses.txt");
			myListUsers = PageEnumerator.getLines(user_names);
			myListPasswords = PageEnumerator.getLines(passwords);
			//for every user name
			//System.out.println(String.format("Guessing Usernames and Passwords on page %s", p.getUrl()));
			for(HtmlForm form : p.getForms()) {	
				HtmlSubmitInput submit = null; 
				HtmlSubmitInput logout = null;
				try {
					submit = form.getInputByName("Login");
				} catch(ElementNotFoundException e) {
					try {
						submit = form.getInputByValue("Login");
					} catch (ElementNotFoundException e1) { break; }
				}
				for(String user : myListUsers) {
					for(String pwd : myListPasswords) {

						form.getInputByName("username").setValueAttribute(user); 
						form.getInputByName("password").setValueAttribute(pwd); 					

						HtmlPage newPage = submit.click(); 
						if (newPage.asText().contains("Logout")){
							System.out.println("Login on page " + loginPage + ":\n\t with username: " + user +", and password: " + pwd + " was successful.");
							wc.getCookieManager().clearCookies();
						}
					}
				}

			}

		} catch (FailingHttpStatusCodeException | IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

	}
}