package edu.rit.se.security.fuzzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class SanatizationFuzzer extends Object implements Fuzzer {

	public static List<String> xss_vectors;
	public static List<String> sql_vectors;

	@Override
	public void fuzz(PageInfo page) {
		try {
			if (xss_vectors == null || sql_vectors == null) {
				File xss_file = new File("resources/fuzz_vectors_xss.txt");
				File sql_file = new File("resources/fuzz_vectors_sql.txt");

				xss_vectors = PageEnumerator.getLines(xss_file);
				sql_vectors = PageEnumerator.getLines(sql_file);
			}
			
			for(String xss_vec : xss_vectors){
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
