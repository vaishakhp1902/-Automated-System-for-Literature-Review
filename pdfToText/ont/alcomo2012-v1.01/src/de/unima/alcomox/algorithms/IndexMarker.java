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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import de.unima.alcomox.exceptions.PCFException;
import de.unima.alcomox.mapping.Correspondence;
import de.unima.alcomox.mapping.Mapping;

/**
 * An index marker is an array of flags that inform wether or not a
 * correspondence of a mapping is active or not. It is used for fast performance
 * of algorithms compared to directly working on mappings and correspondences.
 * 
 */
public class IndexMarker implements Comparable<IndexMarker>, Iterator<Integer> {

	private static double[] confidences;
	private int index = 0;
	private boolean[] flag;
	private double trust;
	private double estimatedTrustLoss;
	private int hashcode;

	/**
	 * Constructs an index-marker of size specified by a mapping. The flags are
	 * set such that each correspondence is accepted. Normally the root in
	 * search algorithms.
	 * 
	 * @param mapping
	 *            The mapping that determines size and confidence values of this
	 *            index-marker.
	 */
	public IndexMarker(Mapping mapping, ConflictStore cm) {
		this.flag = new boolean[mapping.size()];
		IndexMarker.confidences = new double[mapping.size()];
		for (int i = 0; i < mapping.size(); i++) {
			IndexMarker.confidences[i] = mapping.get(i).getConfidence();
			this.flag[i] = true;
		}
		this.computeTrust();
		this.estimatedTrustLoss = 0.0;
		this.computeHeuristic(cm);
		this.prepareHashcode();
		this.initOrResetIndex();
	}

	public void initOrResetIndex() {
		for (int i = 0; i < this.flag.length; i++) {
			if (flag[i]) {
				this. index = i;
				return;
			}
		}
		this.index = -1;
	}

	/**
	 * Constructs a child of an index-marker which ic the parent index-maker
	 * where on of the flags is set from true to false (one correspondence
	 * dismissed).
	 * 
	 * @param index
	 *            The index of the correspondence to be dismissed.
	 */
	public IndexMarker getChild(int index, ConflictStore cm) {
		IndexMarker child = new IndexMarker(this);
		child.deactivate(index);
		child.trust = this.getTrust() - IndexMarker.confidences[index];
		this.estimatedTrustLoss = 0.0;
		child.computeHeuristic(cm);
		child.prepareHashcode();
		return child;
	}

	public boolean isActive(int index) {
		return this.flag[index];
	}

	public Mapping getActiveMapping(Mapping mapping) {
		return this.getMapping(mapping, true);
	}

	public Mapping getInactiveMapping(Mapping mapping) {
		return this.getMapping(mapping, false);
	}

	public double getEstimatedTrustLoss() {
		return this.estimatedTrustLoss;
	}

	public double getTrust() {
		return this.trust;
	}

	public double getFinalTrust() {
		// return this.trust;
		return this.trust - this.estimatedTrustLoss;
	}

	public int activeSize() {
		int counter = 0;
		for (int i = 0; i < this.size(); i++) {
			if (this.isActive(i)) {
				counter++;
			}
		}
		return counter;
	}

	public int size() {
		return flag.length;
	}

	public int compareTo(IndexMarker that) {
		if (this.getFinalTrust() < that.getFinalTrust()) {
			return 1;
		} else if (this.getFinalTrust() == that.getFinalTrust()) {
			return 0;
		} else {
			return -1;
		}
	}

	public boolean equals(Object object) {
		try {
			IndexMarker that = (IndexMarker) object;
			if (this.size() != that.size()) {
				return false;
			}
			for (int i = 0; i < this.size(); i++) {
				if (this.isActive(i) != that.isActive(i)) {
					return false;
				}
			}
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}

	public int hashCode() {
		return this.hashcode;
	}

	// *** PRIVATE PLAYGROUND ***

	private void computeHeuristic(ConflictStore cm) {
		Set<Integer> conflict = cm.getConflictingIndices(this);
		ArrayList<Integer> tempRemovedNodes = new ArrayList<Integer>();
		double estimatedTrustLoss = 0.0;
		while (conflict != null) {
			double minConfidence = 1.0;
			for (int index : conflict) {
				tempRemovedNodes.add(index);
				this.deactivate(index);
				minConfidence = (this.getConfidence(index) < minConfidence) ? this
						.getConfidence(index) : minConfidence;
			}
			estimatedTrustLoss += minConfidence;
			conflict = cm.getConflictingIndices(this);
		}
		for (Integer index : tempRemovedNodes) {
			this.activate(index);
		}
		// this.estimatedTrustLoss = 0;
		this.estimatedTrustLoss = estimatedTrustLoss;
	}

	private void activate(int index) {
		this.flag[index] = true;
	}

	public void deactivate(int index) {
		this.flag[index] = false;
	}

	private IndexMarker(IndexMarker that) {
		this.flag = new boolean[that.size()];
		for (int i = 0; i < that.size(); i++) {
			this.flag[i] = that.isActive(i);
		}
	}

	private Mapping getMapping(Mapping mapping, boolean active) {
		ArrayList<Correspondence> correspondences = new ArrayList<Correspondence>();
		for (int i = 0; i < this.size(); i++) {
			if (this.isActive(i) == active) {
				correspondences.add(mapping.get(i));
			}
		}
		return new Mapping(correspondences);
	}

	private void computeTrust() {
		double t = 0.0;
		for (int i = 0; i < this.size(); i++) {
			if (this.isActive(i)) {
				t += IndexMarker.confidences[i];
			}
		}
		this.trust = t;
	}

	private void prepareHashcode() {
		Double t = this.getTrust() * 10000;
		int hc = t.intValue();
		int hcAdjust = 0;
		int power = 1;
		for (int i = 0; i < ((this.size() < 32) ? this.size() : 31); i++) {
			power *= 2;
			if (this.isActive(i)) {
				hcAdjust += power;
			}
		}
		hc += hcAdjust;
		this.hashcode = hc;
	}

	public double getConfidence(int index) {
		return IndexMarker.confidences[index];
	}

	@Override
	public boolean hasNext() {
		if (index >= 0) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public Integer next() {
		if (this.index == -1) {
			throw new NoSuchElementException();
		}
		int previousIndex = this.index;
		this.index++;
		while (this.index < this.flag.length && this.flag[this.index] == false) {
			this.index++;
		}
		if (this.index >= this.flag.length) {
			this.index = -1;
		}
		return previousIndex;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

}
