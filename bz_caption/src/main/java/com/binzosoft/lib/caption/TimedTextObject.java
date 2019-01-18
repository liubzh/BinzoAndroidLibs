package com.binzosoft.lib.caption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
	
	//list of captions
	public ArrayList<Caption> captions;

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
		captions = new ArrayList<>();

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
	public void toSRT(String filePath) throws IOException {
		new FormatSRT().toFile(this, filePath);
	}

	public void addCaption(Caption caption) {
		if (caption == null) {
			warnings += "null caption is found.\n\n";
		} else if (caption.start >= caption.end) {
			warnings += String.format("end-time is lesser than or equal to start-time:\n%s\n\n",
                    caption.toString());
		}
		captions.add(caption);
	}

    /**
     * 解析完成之后对列表进行排序(升序)
     */
	public void sortCaptions() {
        Collections.sort(captions, new Comparator<Caption>() {
            @Override
            public int compare(Caption caption1, Caption caption2) {
                if (caption1.start > caption2.start) {
                    return 1;
                } else if (caption1.start < caption2.start) {
                    return -1;
                }
                return 0;
            }
        });
	}
}
