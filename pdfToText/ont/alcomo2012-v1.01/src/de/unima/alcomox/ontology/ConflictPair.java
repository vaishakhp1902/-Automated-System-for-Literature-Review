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

package de.unima.alcomox.ontology;

import java.util.HashSet;
import java.util.Set;

import de.unima.alcomox.mapping.Correspondence;


/**
* A conflict pair is a conflict set of size 2 that can can be detected by incomplete
* and effient reasoning. It might also contain some context description of the entities
* matched by the correspondences of the pair. 
*/
public class ConflictPair  {
	
	private Correspondence correspondence1;
	private Correspondence correspondence2;

	
	public ConflictPair(Correspondence c1, Correspondence c2) {
		this.correspondence1 = c1;
		this.correspondence2 = c2;

		
		
	}
	
	public String toString() {
		StringBuffer rep = new StringBuffer();
		rep.append("(1) " + this.correspondence1 + "\n");
		rep.append("(2) " + this.correspondence2 + "\n");
		return rep.toString();
	}
	
	public boolean equals(Object thatObject) {
		ConflictPair that = (ConflictPair)thatObject;
		if (this.correspondence1.equals(that.correspondence1) && this.correspondence2.equals(that.correspondence2)) {
			return true;
		}
		else if (this.correspondence1.equals(that.correspondence2) && this.correspondence2.equals(that.correspondence1)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	// to complicated to makes this a fast computation that has necesarry properties
	public int hashCode() {	return 1; }

	public Set<Correspondence> getCorrespondences() {
		Set<Correspondence> cAsSet = new HashSet<Correspondence>();
		cAsSet.add(this.correspondence1);
		cAsSet.add(this.correspondence2);
		return cAsSet;
	}

	public Correspondence getCorrespondence1() {
		return this.correspondence1;
	}

	public Correspondence getCorrespondence2() {
		return this.correspondence2;
	}

	
}
