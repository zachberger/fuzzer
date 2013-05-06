package edu.rit.se.security.fuzzer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

public class SensitiveData implements Fuzzer{

	//not working correctly...
	@Override
	public void fuzz(PageInfo page) {
		List<String> myListRegex = null;
		File sensitive_data = new File("resources/sensitive_data.txt");
		Pattern pattern = null;
		try {
			myListRegex = PageEnumerator.getLines(sensitive_data);
		} catch (FailingHttpStatusCodeException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Searching for sensitive data on page " + page.page.getUrl());
//		System.out.println("   hmmm" + page.page.asText());
		for (String data : myListRegex) {
			pattern.compile(data);
			if (data.matches(page.page.asText())) {
				System.out.println("    " + page.page.asText() + " found.");
			}
		}
	}
}