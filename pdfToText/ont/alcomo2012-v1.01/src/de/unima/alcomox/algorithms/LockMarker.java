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

package de.unima.alcomox.algorithms;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
* A lock marker is an array of flags that inform wether or not a correspondence
* of a mapping is active or not. It is used for fast performance of hungarian algorithms.
*
*/
public class LockMarker implements Comparable<LockMarker>, Iterable<Coord> {
	
	
	private List<Coord> locks;
	private double score;
	private int hashcode; 

	
	/**
	* Constructs an empty list of locks as lock-marker. Normally the root in hungarian search.
	* 
	*/
	public LockMarker() {
		this.hashcode = 0;
		this.locks = new LinkedList<Coord>();
	}
	
	/**
	* Constructs a copy of that lock marker with an additional lock being added.
	* 
	* @param that Ths lock marker to be copied.
	*/
	private LockMarker(LockMarker that, Coord thatLock) {
		this.locks = new LinkedList<Coord>();
		this.locks.addAll(that.getLocks());
		this.hashcode = that.hashCode() + thatLock.hashCode();
		this.locks.add(thatLock);
	}
	
	/**
	* Constructs a child of this lock marker, where an additional lock has been added.
	* 
	* @param lock The additional lock.
	*/	
	public LockMarker getChild(Coord lock) {
		LockMarker child = new LockMarker(this, lock);
		return child;
	}
	
	public List<Coord> getLocks() {
		return this.locks;
	}
	


	/**
	* 
	* @return The size (number of locks) of this lock marker.
	*/
	public int size() {
		return this.locks.size();
	}

	public boolean equals(Object object) {
		LockMarker that;
		try {
			that = (LockMarker)object;
		}
		catch(ClassCastException e) {
			return false;
		}
		int n1 = this.size();
		int n2 = that.size();
		if (n1 == n2) {
			for (Coord l : this) {
				if (!(that.containsLock(l)))  { return false; }
			}
			return true;
		}
		else { return false; }
	}	
	
	public boolean containsLock(Coord lock) {
		for (Coord l : this) {
			if (lock.equals(l)) { return true; }
		}	
		return false;
	}
	
	public int hashCode() {
		return hashcode;
	}

	public int compareTo(LockMarker that) {
		if (this.score < that.score) return -1;
		else if (that.score < this.score) return 1;
		else return 0;
	}

	public Iterator<Coord> iterator() {
		return this.locks.iterator();
	}

	public void setScore(double score) {
		this.score = score;
		
	}	
	public double getScore() {
		return this.score;
	}	
	
}
