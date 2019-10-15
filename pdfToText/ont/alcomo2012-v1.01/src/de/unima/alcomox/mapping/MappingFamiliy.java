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

import java.util.HashMap;
import java.util.LinkedList;


/**
* A mapping familiy is a set of mapping togeterh with the information which matcher
* generated which mapping. It can be used to generate a merged mapping, with several
* different as adjustments/aggregations of confidence values. 
*
*/
public class MappingFamiliy {
	
	
	HashMap<String, Mapping> hashedMappings;
	LinkedList<String> matcherIds;
	
	/**
	* Constructs an empty mapping family. 
	*
	*/
	public MappingFamiliy() {
		this.hashedMappings = new HashMap<String, Mapping>();
		this.matcherIds = new LinkedList<String>();
	}
	
	public String toString() {
		StringBuffer rep = new StringBuffer("Ontology Family:\n");
		for (String matcherId : this.matcherIds) {
			rep.append("\t" + matcherId + ": " + this.hashedMappings.get(matcherId).size() + "\n");
		}		
		return rep.toString();
	}
	
	/**
	* Adds a mapping to this family of mappings.
	* 
	* @param matcherId Id of the matcher which generated the mapping.
	* @param mapping Mapping to be added.
	*/
	public void addMapping(String matcherId, Mapping mapping) {
		this.matcherIds.add(matcherId);
		this.hashedMappings.put(matcherId, mapping);
	}
	

	/**
	* Returns the merged mapping where confindences have been computed in a way
	* such that the following holds for two correspondences c1 and c2:
	* 
	* (1) conf(c1) &lth; conf(c2) iff less systems found c1 (Vote).
	* (2) conf(c1) &lth; conf(c2) iff the same number of systems found c1 and c2
	* but c1 has a lower total of confidence values (Confidence).
	* 
	*  All confidences are spanned to [0.0, 1.0] before computing this.
	* 
	* @return The merged mapping.
	*/
	public Mapping getMergedMappingVC() {
		this.normalize();
		HashMap<Correspondence, Integer> occurences = new HashMap<Correspondence, Integer>();
		HashMap<Correspondence, Double> confidences = new HashMap<Correspondence, Double>();
		LinkedList<Correspondence> correspondences = new LinkedList<Correspondence>();
		int score;
		double conf;
		for (String matcherId : this.matcherIds) {
			for (Correspondence c : this.hashedMappings.get(matcherId)) {
				if (occurences.containsKey(c)) {
					score = occurences.get(c);
					conf = confidences.get(c);
					occurences.put(c, score+1);
					confidences.put(c, conf + c.getConfidence());
					
				}
				else {
					correspondences.add(c);
					occurences.put(c, 1);
					confidences.put(c, c.getConfidence());
				}
			}
		}
		double fractionOccurrences = 1.0 / (double)matcherIds.size();
		double fractionConfidences = fractionOccurrences / ((double)matcherIds.size() + 1.0);
		double confidence;
		for (Correspondence c : correspondences) {
			confidence = ((double)occurences.get(c)-1.0) * fractionOccurrences;
			confidence += (double)confidences.get(c) * fractionConfidences;
			c.setConfidence(confidence);
		
		}
		Mapping mergedMapping = new Mapping();
		mergedMapping.setCorrespondences(correspondences);
		mergedMapping.normalize();
		mergedMapping.sortDescending();
		return mergedMapping;
	}
	
	private void normalize() {
		for (String matcherId : this.matcherIds) {
			this.hashedMappings.get(matcherId).normalize();
		}
		
	}
	
	

}
