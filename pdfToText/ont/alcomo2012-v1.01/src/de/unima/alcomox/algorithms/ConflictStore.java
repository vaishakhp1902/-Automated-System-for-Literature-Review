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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.unima.alcomox.mapping.Correspondence;
import de.unima.alcomox.mapping.Mapping;
import de.unima.alcomox.ontology.EfficientReasoner;
import de.unima.alcomox.util.AlcomoLogger;

public class ConflictStore {
	
	// for conflicts of size 1 (very rare)
	private boolean[] singularities;
	// for conflicts of size 2
	private boolean[][] conflicts;
	// for conflicts of with size > 2
	private  HashMap<Integer, List<Integer[]>> conflictSets;
	
	// from correspondence to index
	private HashMap<Correspondence, Integer> hashedIndices;
	// from index to correspondence
	private HashMap<Integer, Correspondence> hashedCorrespondences;
	
	// random is needed for equal distribution of conflict keys
	private Random rand;
	// just the seed, no need to worry ;-)
	private final int NUMBER_OF_THE_BEAST = 666;
	
	protected AlcomoLogger log;

	
	// the correspondences that are involved in some conflict
	private boolean[] blamed; 
	
	/**
	* Construct a conflict store where some conflicts are
	* precomputed due to efficient reasoning.
	* 
	* @param reasoner The efficient reasoning component.
	* @param mapping The mapping to be analyzed.
	*/
	public ConflictStore(EfficientReasoner reasoner, Mapping mapping) {
		
		AlcomoLogger.takeTime("starting pattern precomputation");
		
		this.log = new AlcomoLogger(this.getClass());
		this.rand = new Random(NUMBER_OF_THE_BEAST);
		this.singularities = new boolean[mapping.size()];
		this.blamed = new boolean[mapping.size()];
		for (int i = 0; i < this.blamed.length; i++) {
			this.blamed[i] = false;
		}
		this.conflicts = new boolean[mapping.size()][mapping.size()];
		this.conflictSets = new HashMap<Integer, List<Integer[]>>();
		this.hashedIndices = new HashMap<Correspondence, Integer>();
		this.hashedCorrespondences = new HashMap<Integer, Correspondence>();
		
		this.log.infoS("precomputing pattern conflicts");
		this.log.infoPStart();
		
		// precomputing pattern conflicts
		
		
		
		int counter = 0;
		double progress = 0.0;
		
		for (int x = 0; x < mapping.size(); x++) {
			counter++;
			// used for INFO
			if (((double)counter / (double)mapping.size()) >= progress) {
				double realProgress = (double)counter / (double)mapping.size();
				while (realProgress > progress) { progress += 0.05; }
				this.log.infoP(progress);
			}
			this.singularities[x] = false;
			this.hashedIndices.put(mapping.get(x), x);
			this.hashedCorrespondences.put(x, mapping.get(x));
			for (int y = x; y < mapping.size(); y++) {
				if (x == y) {
					this.conflicts[x][y] = false; 
					continue;
				}
				if (reasoner.isConflictPair(mapping.get(x), mapping.get(y))) {
					this.conflicts[x][y] = true;
					this.conflicts[y][x] = true;
					this.blamed[x] = true;
					this.blamed[y] = true;
				}
				else {
					this.conflicts[x][y] = false;
					this.conflicts[y][x] = false;
				}
			}
		}
		/*
		int numOfUnblamed = 0;
		int numOfBlamed = 0;
		for (int i = 0; i < this.blamed.length; i++) {
			if (this.blamed[i]) {
				numOfBlamed++;	
			}
			else {
				numOfUnblamed++;
			}
		}
		*/
		this.log.infoPEnd();
		
		AlcomoLogger.takeTime("finished pattern precomputation");
		
		// System.out.println("\nUnblamed = " + numOfUnblamed +  "   Blamed = " +  numOfBlamed);
		
	}
	
	/**
	* Constructs an empty conflict store where no efficient component is used
	* for pre processing.
	* 
	* @param mapping The mapping to be anaylzed.
	*/
	public ConflictStore(Mapping mapping) {
		this.rand = new Random(NUMBER_OF_THE_BEAST);
		this.singularities = new boolean[mapping.size()];
		this.blamed = new boolean[mapping.size()];
		for (int i = 0; i < this.blamed.length; i++) this.blamed[i] = false;
		this.conflicts = new boolean[mapping.size()][mapping.size()];
		this.conflictSets = new HashMap<Integer, List<Integer[]>>();
		this.hashedIndices = new HashMap<Correspondence, Integer>();
		this.hashedCorrespondences = new HashMap<Integer, Correspondence>();
		for (int x = 0; x < mapping.size(); x++) {
			this.singularities[x] = false;
			this.hashedIndices.put(mapping.get(x), x);
			this.hashedCorrespondences.put(x, mapping.get(x));
			for (int y = x; y < mapping.size(); y++) {
				this.conflicts[x][y] = false;
				this.conflicts[y][x] = false;
			}
		}
	}
		
	
	public String toString() {
		String rep = "";
		for (int x = 0; x < this.conflicts.length; x++) {
			for (int y = 0; y < this.conflicts.length; y++) { rep += (conflicts[x][y] ? 1 : 0) + " "; }
			rep += "\n";
		}
		return rep;
	}
	
	/*
	// no one seems to use this method anymore
	private boolean isIndependentConflict(IndexMarker marker, int x, int y) {
		if (!marker.isActive(x) || !marker.isActive(y)) { return false; }
		if (!this.conflicts(x,y)) { return false; }
		for (int i = 0; i < this.conflicts.length; i++) {
			if (i == y) { continue; }
			if (this.conflicts[x][i] && marker.isActive(i)) { return false; }
		}
		for (int i = 0; i < this.conflicts.length; i++) {
			if (i == x) { continue; }
			if (this.conflicts[i][y] && marker.isActive(i)) { return false; }
		}		
		return true;
	}
	*/

	/**
	* Checks whether there are still some conflicts unresolved with respect to the index marker.
	* 
	* @param m An index marker which marks those correspondences active resp. passive in an mapping.
	* @return A set of indices which refer to conflicting correspondences (a MUPS).
	*/
	
	public Set<Integer> getConflictingIndices(IndexMarker m) {
		Set<Integer> conflictSet = new HashSet<Integer>();		
		for (int x = 0; x < this.conflicts.length; x++) {
			if (!m.isActive(x)) continue;
			if (this.blamed[x] == false) continue;
			// check single conflicts
			if (this.singularities[x]) {
				conflictSet.add(x);
				return  conflictSet;
			}
			// check pairs of conflicts
			for (int y = x+1; y < this.conflicts.length; y++) {
				if (!m.isActive(y)) { continue; }
				if (this.conflicts(x,y)) {
					conflictSet.add(x);
					conflictSet.add(y);
					return  conflictSet;
				}
			}			
		}
		// go for the more complex conflicts
		List<Integer[]> l;
		boolean conflicts = false;
		for (int index = 0; index < this.conflicts.length; index++) {
			if (m.isActive(index)) {
				if (this.conflictSets.containsKey(index)) {
					l= this.conflictSets.get(index);
					for (Integer[] badGuys : l) {
						conflicts = true;
						for (int b : badGuys) {
							if (!(m.isActive(b))) { conflicts = false; }
						}
						if (conflicts) {
							for (int b : badGuys) { conflictSet.add(b); }
							return conflictSet;
						}						
					}
				}
			}
		}
		return null;
	}
	
	/**
	* Computes the next index that has to be removed following a greedy strategy that first chooses  according to conflicts
	* in which a correspondence is involved, and next takes uses the confidence value.
	* 
	* @param m An index marker that defines the active correspondences
	* @return The index of the correspondence that is involved in most conflicts and has the lowest confidence.
	*/
	public int getTopWeightedConflictingIndex(IndexMarker m) {
		int topIndex = -1;
		int numOfConflictsOfTop = 0;
		double confidenceOfTop = Double.MAX_VALUE;
		for (int index = 0; index < m.size(); index++) {
			if (!(m.isActive(index))) continue;
			if (this.singularities[index]) {
				return index;			
			}
			int conflictCounter = this.getNumOfInvolvedConflicts(index,  m);
			// System.out.println("index " + index + " involved in " + conflictCounter + " conflicts");
			// update top index
			if (numOfConflictsOfTop > 0 && numOfConflictsOfTop == conflictCounter) {
				if (confidenceOfTop > this.hashedCorrespondences.get(index).getConfidence()) {
					confidenceOfTop = this.hashedCorrespondences.get(index).getConfidence();
					topIndex = index;
				}
			}
			else if (numOfConflictsOfTop < conflictCounter) {
				numOfConflictsOfTop = conflictCounter;
				confidenceOfTop = this.hashedCorrespondences.get(index).getConfidence();
				topIndex = index;
			}
			
		}
		/*
		if (topIndex != -1) {
			System.out.println();
			System.out.println(">>> topIndex: " + topIndex);
			System.out.println(">>> numof conflicts: " +  this.getNumOfInvolvedConflicts(topIndex,  m));
			System.out.println(">>> confidence: " + this.hashedCorrespondences.get(topIndex).getConfidence());
		}
		*/
		m.initOrResetIndex();
		return topIndex;
	}
	

	public int getNumOfInvolvedConflicts(int index, IndexMarker m) {
		int conflictCounter = 0;
		for (int i = 0; i < this.conflicts[index].length; i++) {
			if (this.conflicts[index][i] && m.isActive(i)) {
				conflictCounter++;
			}
		}
		return conflictCounter;
	}
	
	/**
	* Checks whether there are still some conflicts unresolved with respect to a list of active correspondence indices.
	* 
	* @param indices An arraylist of indices that contains all active correspondence indices.
	* @return A set of indices which refer to conflicting correspondences (a MUPS).
	*/	
	
	public Set<Integer> getConflictingIndices(ArrayList<Integer> indices) {
	
		HashSet<Integer> hashedIndices = new HashSet<Integer>();
		hashedIndices.addAll(indices);
		Set<Integer> conflictSet = new HashSet<Integer>();
		for (int i = 0; i < indices.size(); i++) {
			int x = indices.get(i);
			
			// the singularity conflicts
			if (this.singularities[x]) {
				conflictSet.add(x);
				return  conflictSet;
			}
			if (this.blamed[x] == false) continue;
			
			// the pairwise conflicts
			for (int j = i + 1; j < indices.size(); j++)  {
				int y = indices.get(j);
				if (this.conflicts(x,y)) {
					conflictSet.add(x);
					conflictSet.add(y);
					return  conflictSet;
				}
			}	
		}
		// now go for the non trivial stuff
		List<Integer[]> l;
		boolean conflicts = false;
		for (int i = 0; i < indices.size(); i++) {
			int index = indices.get(i);
			if (this.conflictSets.containsKey(index)) {
				l = this.conflictSets.get(index);
				for (Integer[] badGuys : l) {
					conflicts = true;
					for (int b : badGuys) {
						if (!(hashedIndices.contains(b))) { conflicts = false; }
					}
					if (conflicts) {
						for (int b : badGuys) { conflictSet.add(b); }
						return conflictSet;
					}						
				}
			}
		}
		return null;
	}


	/**
	* Returns the indices that refer to the correspondences of a given mapping and sets
	* these indices as additional conflicts.0000000000000
	* 
	* @param mapping The mapping that is a minimal conflict set.
	* @return The corresponding indices.
	*/
	public Set<Integer> getIndicesAndSetConflicts(Mapping mapping) {
		Set<Integer> indices = new HashSet<Integer>();
		for (Correspondence c : mapping) {
			indices.add(this.hashedIndices.get(c));
		}
		
		for (int index : indices) {
			this.blamed[index] = true;
		}
		if (indices.size() == 1) {
			Integer[] singleConflict = indices.toArray(new Integer[1]);
			this.singularities[singleConflict[0]] = true; 
		}
		// conflicts of size 2
		else if (indices.size() == 2) {
			Integer[] conflictPair = indices.toArray(new Integer[2]);
			this.conflicts[conflictPair[0]][conflictPair[1]] = true;
			this.conflicts[conflictPair[1]][conflictPair[0]] = true;
		}
		// conflicts of size > 2
		else {
			Integer[] conflictSet = indices.toArray(new Integer[indices.size()]);
			int keyIndex = this.rand.nextInt(conflictSet.length);
			List<Integer[]> l;
			if (this.conflictSets.containsKey(conflictSet[keyIndex])) {
				l = this.conflictSets.get(conflictSet[keyIndex]);
				l.add(this.getDiminishedArray(conflictSet, keyIndex));
			}
			else {
				l = new LinkedList<Integer[]>();
				l.add(this.getDiminishedArray(conflictSet, keyIndex));
				this.conflictSets.put(conflictSet[keyIndex], l);
			}
		}
		return indices;
	}
	
	
	// ******** PRIVATE PLAYGROUND *****
	
	private Integer[] getDiminishedArray(Integer[] field, int rindex) {
		Integer[] dimField = new Integer[field.length -1];
		int up = 0;
		for (int i = 0; i <  field.length; i++) {
			if (i == rindex) {
				up = -1;
				continue;
			}
			dimField[i + up] = field[i];
		}
		return dimField;		
	}
	
	
	private boolean conflicts(int x, int y) {
		return this.conflicts[x][y];
	}		

}
