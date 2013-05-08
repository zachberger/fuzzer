package edu.rit.se.security.fuzzer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

public class SensitiveData implements Fuzzer{

	@Override
	public void fuzz(Set<PageInfo> pageSet) {
		List<String> myListRegex = null;
		File sensitive_data = new File("resources/sensitive_data.txt");
		Pattern pattern = null;
		for(PageInfo page : pageSet){
			try {
				myListRegex = PageEnumerator.getLines(sensitive_data);
			} catch (FailingHttpStatusCodeException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("Searching for sensitive data on page " + page.page.getUrl());
			for (String data : myListRegex) {
				pattern = Pattern.compile(data, Pattern.CASE_INSENSITIVE);
				Matcher m = pattern.matcher(page.page.asText());
				if(m.find()){
					System.out.println("Potentially sentisitive data: " + m.group(0) + ", found on: " + page.page.getUrl());
				}
			}
		}
	}
}