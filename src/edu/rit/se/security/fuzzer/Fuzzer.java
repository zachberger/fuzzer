package edu.rit.se.security.fuzzer;

public interface Fuzzer {

	/**
	 * @param page - The page to fuzz
	 */
	public void fuzz( PageInfo page );
	
}
