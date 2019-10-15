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

package de.unima.alcomox.mapping;

import de.unima.alcomox.exceptions.CorrespondenceException;

/**
* A semantic relation can be used to express equivalence, subsumption or disjointness 
* between entities of different ontologies.   
*/
public class SemanticRelation {
	
	/**
	* Relation thats not available.
	*/	
	public static final int NA = 0;	
	
	/**
	* Relation of equivalence, e.g. A = B.
	*/
	public static final int EQUIV = 1;
	
	/**
	* Relation of subsumption, e.g. A < B.
	*/
	public static final int SUB = 2;
	
	/**
	* Relation of supsumption, e.g. A > B.
	*/
	public static final int SUPER = 3;	
	
	/**
	* Relation of disjointness, e.g. A != B.
	*/	
	public static final int DIS = 4;
	

	
	private int type;
	
	/**
	* Constructs a semantic relation of a certain type.
	* 
	* @param type The type of the semantic relation.
	* @throws CorrespondenceException Thrown, if a not existing type of relation is chosen.
	*/
	public SemanticRelation(int type) throws CorrespondenceException {
		if ((type != EQUIV) && (type != SUB) && (type != SUPER) && (type != DIS)) {
			throw new CorrespondenceException(CorrespondenceException.INVALID_SEMANTIC_RELATION);
		}
		else {
			this.type = type;
		}
	}
	
	/**
	* 
	* @return The type of this semantic relation.
	*/
	public int getType() {
		return this.type;
	}
	
	/**
	* Returns the string representation of this semantic relation.
	* 
	* @return Representation as string.
	*/
	public String toString() {
		switch (this.type) {
		case SemanticRelation.EQUIV:
			return "=";
		case SemanticRelation.SUB:
			return "<";
		case SemanticRelation.SUPER:
			return ">";
		case SemanticRelation.DIS:
			return "!=";
		default:
			// will never occur, hopefully
			return "?";
		}
	}
	
	/**
	* Checks equality of this and that semantic relation.
	* 
	* @param that That semantic relation.
	* @return True, if this and that are of the same type.
	*/
	public boolean equals(Object that) {
		return this.type == ((SemanticRelation)that).type;	
	}
	
	public int hashCode() {
		return this.type;
	}

	/**
	* Returns the inverse semantic relation of this semantic relation.
	* 
	* @return The inverted semantic relation
	*/
	public SemanticRelation getInverse() {
		switch (this.type) {
		case SemanticRelation.SUB:
			try { return new SemanticRelation(SemanticRelation.SUPER); }
			catch (CorrespondenceException e) { }
		case SemanticRelation.SUPER:
			try { return new SemanticRelation(SemanticRelation.SUB); }
			catch (CorrespondenceException e) { }
		default:
			return this;
		}
	}
	
	
}
