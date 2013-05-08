package edu.rit.se.security.fuzzer;

import java.util.Set;

public interface Fuzzer {

	/**
	 * @param page - The page to fuzz
	 */
	public void fuzz( Set<PageInfo> page );
	
}
