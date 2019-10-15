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

public class DataStorage {
	
	public static DataStorage mirror = null;
	
	private ArrayList<ArrayList<String>> data;
	private ArrayList<String> row;
	private String pattern;
	private long clock = 0;
	private int counter = 0;
	
	private boolean stopWatchActive = true;
	private boolean counterActive = true;
	
	
	/**
	* Constructs a data storage. Used for collecting results and runtimes of experiments. 
	* Uses 0.000 to display double / float values.
	* 
	*/
	public DataStorage() {
		this("0.000");
	}
	
	
	/**
	* Constructs a data storage. Used for collecting results and runtimes of experiments. 
	* 
	* @param pattern The pattern for double / float values to be stored as strings (in case rounding is required).
	*/
	public DataStorage(String pattern) {
		this.data = new ArrayList<ArrayList<String>>();	
		this.row = null;
		this.pattern = pattern;
		// mirror = this;
	}
	
	public void makeVisible() {
		mirror = this;
	}
	
	public void deactivateStopWatch() {
		this.stopWatchActive = false;
	}
	
	public void deactivateCounter() {
		this.counterActive = false;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (ArrayList<String> row : this.data) {
			for (String value : row) {
				sb.append(value + "\t");
			}
			
			sb.deleteCharAt(sb.length()-1);
			sb.append("\n");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString(); 
	}
	
	public void createDataRow() {
		this.row = new ArrayList<String>();
		this.data.add(row);
	}
	
	public void addValue(String value) {
		if (row == null) { this.createDataRow(); }
		row.add(value);
	}
	
	public void addValue(int value) {
		this.addValue((new Integer(value)).toString());
	}
	

	
	public void addValue(double value) {
		this.addValue(Tools.toDecimalFormat(value, this.pattern));
	}
	
	private void addDuration(long value) {
		this.addValue((new Long(value)).toString());
	}
	
	public void startClock() {
		this.clock = System.currentTimeMillis();
		
	}
	
	public void takeTime() {
		if (!(this.stopWatchActive)) { return; }
		long startTime = this.clock;
		long endTime = System.currentTimeMillis();
		this.clock = endTime;
		this.addDuration(endTime - startTime);
	}

	/**
	* Adds the value of the counter as data value and restets the counter to 0.
	*  
	*/
	public void addCounter() {
		if (!(this.counterActive)) { return; }
		this.addValue(this.counter);
		this.counter = 0;
	}
	
	public void count() {
		this.counter++;
	}
	
	/**
	* Returns the value of the counter and resets it to 0.
	* 
	* @return Old value of the counter.
	*/
	public int resetCounter() {
		int temp = this.counter;
		this.counter = 0;
		return temp;
	}
	

}
