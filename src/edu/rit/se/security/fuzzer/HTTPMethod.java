package edu.rit.se.security.fuzzer;

public enum HTTPMethod {
	GET("Get"),
	POST("Post"),
	PUT("Put"),
	DELETE("Delete");
	
	private String fmt;
	
	HTTPMethod( String s ){
		fmt=s;
	}
	
	public String toString(){
		return fmt;
	}
}
