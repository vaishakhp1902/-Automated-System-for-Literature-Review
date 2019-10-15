// *****************************************************************************
//
// Copyright (c) 2011 Christian Meilicke (University of Mannheim)
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge,
// publish, distribute, sublicense, and/or sell copies of the Software,
// and to permit persons to whom the Software is furnished to do so,
// subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
// IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
// *********************************************************************************

package de.unima.alcomox.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
* A wrapper for the log4j logging mechanism.
*/
public class AlcomoLogger {
	
	private Logger log;
	
	private static ArrayList<String> breakpoints = new ArrayList<String>();
	private static HashMap<String, Long> timestamps = new HashMap<String, Long>();
	
	/**
	* Constructs a wrapper logger for the log4j logger for a certain class.
	* 
	* @param c The class which activities will be logged.
	*/
	@SuppressWarnings("unchecked")
	public AlcomoLogger(Class c) {
		this.log = Logger.getLogger(c.getName());
	}

	/**
	* Logs a start sign for a series of progress infos as info message.
	* 
	* @param word
	*/
	public void infoPStart() {
		this.log.info("[ ");
	}
	
	/**
	* Logs a start sign for a series of progress infos as info message.
	* 
	* @param prefixExpl An explanation that prefixes the progress information.
	*/
	public void infoPStart(String prefixExpl) {
		this.log.info("[ " + prefixExpl + " ");
	}
	
	/**
	* Logs a end sign for a series of progress infos as info message followed by a newline.
	* 
	* @param word
	*/
	public void infoPEnd() {
		this.log.info("]\n");
	}		
	
	/**
	* Logs a word as info message.
	* 
	* @param word
	*/
	public void infoW(String word) {
		this.log.info(word);
	}
	
	/**
	* Logs a double interpreted as progress in percentage as info message.
	* 
	* @param word
	*/
	public void infoP(double progress) {
		// System.out.println("xx: " +  progress);
		this.log.info((int)(progress * 100) + "% ");
	}	
	
	/**
	* Logs a statement as info message. Message will be surrounded by [ ] followed by a newline.
	*  
	* @param statement
	*/
	public void infoS(String statement) {
		this.log.info("[" + statement + "]\n");
	}
	
	/**
	* Logs a statement as warning. Message will be surrounded by [WARNING: ] followed by a newline.
	*  
	* @param statement
	*/
	public void warnS(String statement) {
		this.log.warn("[!WARN! " + statement + " !WARN!]\n");
	}
	
	public static void takeTime(String breakpoint) {
		if (breakpoints.contains(breakpoint)) {
			if (breakpoint.endsWith(" I")) {
				breakpoint = breakpoint + "I";
			}
		}
		breakpoints.add(breakpoint);
		timestamps.put(breakpoint, System.currentTimeMillis());
	}
	
	
	public static void printTimestamps() {
		String first = breakpoints.get(0);
		String last = breakpoints.get(breakpoints.size() - 1);
		long firstTimer = timestamps.get(first);
		long lastTimer = timestamps.get(last);
		long whole = lastTimer - firstTimer;
		
		
		for (int i = 0; i < breakpoints.size() - 1; i++) {
			String current = breakpoints.get(i);
			String next =  breakpoints.get(i+1);
			long l1 = timestamps.get(current);
			long l2 = timestamps.get(next);
			long l = l2 - l1;
			long secs = l / 1000;
			long millis = l % 1000;
			System.out.print("(" + i + ") " + current + " -> " + next + " \t" + secs + "." + millis);
			
			System.out.println("\t" + Math.round(((double)l / (double)whole) * 100.0) + "%");
			
		}
	}

	public static void resetTimer() {
		breakpoints.clear();
		timestamps.clear();	
	}
}
