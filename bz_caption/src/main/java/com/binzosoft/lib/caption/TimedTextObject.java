package com.binzosoft.lib.caption;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

public class TimedTextObject {
	
	/*
	 * Attributes
	 * 
	 */
	//meta info
	public String title = "";
	public String description = "";
	public String copyrigth = "";
	public String author = "";
	public String fileName = "";
	public String language = "";
	
	//list of captions (begin time, reference)
	//represented by a tree map to maintain order
	public TreeMap<Integer, Caption> captions;
	
	//to store non fatal errors produced during parsing
	public String warnings;
	
	//**** OPTIONS *****
	//to know whether file should be saved as .ASS or .SSA
	public boolean useASSInsteadOfSSA = true;
	//to delay or advance the subtitles, parsed into +/- milliseconds
	public int offset = 0;
	
	//to know if a parsing method has been applied
	public boolean built = false;
	
	
	/**
	 * Protected constructor so it can't be created from outside
	 */
	protected TimedTextObject(){
		captions = new TreeMap<>();
		
		warnings = "List of non fatal errors produced during parsing:\n\n";
	}
	
	
	/*
	 * Writing Methods
	 * 
	 */
	/**
	 * Method to generate the .SRT file
	 * 
	 * @return an array of strings where each String represents a line
	 */
	public String[] toSRT(){
		return new FormatSRT().toFile(this);
	}
}
