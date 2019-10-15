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

import org.apache.log4j.Logger;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.exceptions.CorrespondenceException;
import de.unima.alcomox.exceptions.MappingException;
import de.unima.alcomox.exceptions.OntologyException;
import de.unima.alcomox.exceptions.PCFException;
import de.unima.alcomox.ontology.ConflictPair;
import de.unima.alcomox.ontology.Entity;
import de.unima.alcomox.ontology.LocalOntology;


/**
* A mapping is a ordered list of correspondences. It provided several set operations on the correspondences.
* As well as ordering and several operations like normalization and thresholding.
* 
*/
public class Mapping implements Iterable<Correspondence>, Comparable<Mapping>   {
	
	/**
	* Simple text-based proprietary format. One line per correspondence. Easy to read by a human. 
	*/
	public static final int FORMAT_TXT = 0;
	
	/**
	* Standard format of the OAEI. The part in the header where metainformation (e.g. 1:1 mapping)
	* is described is not supported.
	*/
	public static final int FORMAT_RDF = 1;
	


	private ArrayList<Correspondence> correspondences = null;
	private Set<ConflictPair> conflictPairs = null;
	
	protected Logger log;
	
	protected boolean[] idPattern = null;

	
	
	
	private double propertyAdjustmentFactor = 0.0;
	private double conceptAdjustmentFactor = 0.0;

	/**
	* Constructs a mapping with an empty list of correspondences.
	*/
	public Mapping() {
		this(new ArrayList<Correspondence>());
		this.log = Logger.getLogger("de.unima.alcomox.mapping.Mapping");
	}
	
	/**
	* Constructs a mapping from a mapping given in a file.
	* 
	* @param filepath The path to the file.
	* @param format The format of the mapping file.
	* @throws MappingException Thrown if the file is not available or caontains invalid format.
	*/
	public Mapping(String filepath, int format) throws MappingException {
		MappingReader mr;
		if (format == Mapping.FORMAT_TXT) {
			mr = new MappingReaderTxt();
		}
		else if (format == Mapping.FORMAT_RDF) {
			mr = new MappingReaderXml();
		}		
		else {
			throw new MappingException(MappingException.IO_ERROR, "chosen a not supported mapping format");
		}
		this.setCorrespondences(mr.getMapping(filepath).getCorrespondences());
	}
	
	/**
	* Constructs a mapping from a mapping given in a file, that has to be formated in 
	* the alignment API format.
	* 
	* @param filepath The path to the file.
	* @throws MappingException Thrown if the file is not available or caontains invalid format.
	*/
	public Mapping(String filepath) throws MappingException {
		this(filepath, FORMAT_RDF);
	}
	
	
	public void write(String filepath, int format) throws MappingException {
		MappingWriter mw;
		if (format == Mapping.FORMAT_TXT) {
			mw = new MappingWriterTxt();
		}
		else if (format == Mapping.FORMAT_RDF) {
			mw = new MappingWriterXml();
		}
		else {
			throw new MappingException(MappingException.IO_ERROR, "chosen a not supported mapping format");
		}
		mw.writeMapping(filepath, this);
	}
	
	/**
	* Writes a mapping in RDF format to a specified path.
	* 
	* @param filepath
	* @throws MappingException
	*/
	public void write(String filepath) throws MappingException {
		this.write(filepath, FORMAT_RDF);
	}

	
	
	/**
	* Constructs a mapping with the given list of correspondences.
	* 
	* @param correspondences The correspondences of the mapping as list.
	*/
	public Mapping(ArrayList<Correspondence> correspondences) {
		this.setCorrespondences(correspondences);
	}
	
	/**
	* Constructs a mapping with the given set of correspondences.
	* 
	* @param correspondences The correspondences of the mapping as set.
	*/
	public Mapping(Set<Correspondence> correspondencesAsSet) {
		this.setCorrespondences(correspondencesAsSet);
	}
	
	/**
	* Activates fast idpattern used for hast identification is e.g. hashsets.
	* It is used for recognizing the same descendants of a parent mapping in a search tree
	* in different branches, for duplicate elemination.
	* 
	* 
	*/
	public void activateFastIdentification() {
		this.idPattern = new boolean[this.size()];
		for (int i = 0; i < this.size(); i++) { this.idPattern[i] = true; }
	}
	


	/**
	* Returns a string representation of this mapping. 
	*/
	private String toSomeString(boolean verbose) {
		if (this.size() == 0) { return "[empty mapping]\n"; }
		StringBuffer sb = new StringBuffer();
		ArrayList<Correspondence> sortedCorrespondences = this.getCorrespondences();
		Collections.sort(sortedCorrespondences);
		Collections.reverse(sortedCorrespondences);	
		// sb.append("Mapping of size " + this.correspondences.size() + "\n");
		
		for (Correspondence c : sortedCorrespondences) {
			if (verbose) { sb.append(c.toString() + "\n"); }
			else { sb.append(c.toShortString() + "\n"); }
		}
		return sb.toString();
	}
	
	public String toString() {
		return this.toSomeString(true);
	}	
	
	public String toShortString() {
		return this.toSomeString(false);
	}
	
	public String toVeryShortString() {
		String rep = this.toShortString();
		rep = rep.replace('\n', ' ');
		return rep;
	}
	
	/**
	* Returns different kinds of meta information about this mapping.
	* 
	* @return Information about this mapping.
	*/
	public String getMetaDescription() {
		StringBuffer sb = new StringBuffer();

		HashMap<SemanticRelation, Integer> relations = new HashMap<SemanticRelation, Integer>();
		HashSet<String> sourceNamespaces = new HashSet<String>();
		HashSet<String> targetNamespaces = new HashSet<String>();
		
		double lowerBound = this.getConfidenceLowerBound();
		double upperBound = this.getConfidenceUpperBound();
		
		for (Correspondence c : this.getCorrespondences()) {
			SemanticRelation rel;
			rel = c.getRelation();
			if (relations.containsKey(rel)) relations.put(rel, relations.get(rel) + 1);
			else relations.put(rel, 1);
			sourceNamespaces.add(c.getSourceNamespace());
			targetNamespaces.add(c.getTargetNamespace());
		}		
		sb.append("NUMBER OF CORRESPONDENCES: " +  this.size() + "\n");
		sb.append("SEMANTIC RELATIONS:");
		for (SemanticRelation rel : relations.keySet()) {
			sb.append(" " + rel + "(" + relations.get(rel) + ")");
		}
		sb.append("\n");
		sb.append("NAMESPACES FOR SOURCE ENTITIES: " + sourceNamespaces + "\n");
		sb.append("NAMESPACES FOR TARGET ENTITIES: " + targetNamespaces + "\n");
		sb.append("CONFIDENCE RANGE: [" + lowerBound + ", " + upperBound + "]\n");
		sb.append("IS UNIQUE: " + this.isUnique() + "\n");
		return sb.toString();
	}
	
	public double getConfidenceLowerBound() {
		double lowerBound = Double.MAX_VALUE;
		for (Correspondence c : this.getCorrespondences()) {
			lowerBound = c.getConfidence() < lowerBound ? c.getConfidence() : lowerBound;
		}
		return lowerBound;
	}
	
	public double getConfidenceUpperBound() {
		double upperBound = Double.MIN_VALUE;
		for (Correspondence c : this.getCorrespondences()) {
			upperBound = c.getConfidence() > upperBound ? c.getConfidence() : upperBound;
		}
		return upperBound;
	}	
	
	public boolean isUnique() {
		for (int i = 0; i < this.size(); i++) {
			for (int j = i + 1; j < this.size(); j++) {
				if (this.getCorrespondences().get(i).equals(this.getCorrespondences().get(j))) {
					return false;
				}
			}
		}
		return true;
	}	
	
	// --- SET OPERATIONS ---
	
	
	/**
	* Returns the set difference between this mapping and that mapping.
	* 
	* @param that The mapping to be compared to this mapping.
	* @return The set difference this without that.
	*/	
	public Mapping getDifference(Mapping that) {
		HashSet<Correspondence> thisCSet = this.getCorrespondencesAsSet();
		HashSet<Correspondence> thatCSet = that.getCorrespondencesAsSet();
		thisCSet.removeAll(thatCSet);
		return (new Mapping(thisCSet));
	}

	/**
	* Returns the intersection between this mapping and that.
	* 
	* @param that The mapping to be intersected to this mapping.
	* @return The intersection bewteen this and that.
	*/	
	public Mapping getIntersection(Mapping that) {
		HashSet<Correspondence> thisCSet = this.getCorrespondencesAsSet();
		HashSet<Correspondence> thatCSet = that.getCorrespondencesAsSet();
		thisCSet.retainAll(thatCSet);
		return (new Mapping(thisCSet));
	}
	
	/**
	* Returns the union of this mapping and that. The confidence values of correspondences
	* contained in borth this and that are summed up.
	* 
	* @param that The mapping to be unioned with this mapping.
	* @return The union of this and that.
	*/	
	public Mapping getUnion(Mapping that) {
		// this is very bad code, but i totally are confused with identity and references in java
		HashMap<Correspondence, Double> hashedCorrespondences = new HashMap<Correspondence, Double>();
		Mapping mapping = new Mapping();
		for (Correspondence c : this) {
			hashedCorrespondences.put(c, c.getConfidence());
		}
		for (Correspondence c : that) {
			if (hashedCorrespondences.containsKey(c)) {
				double c1 = c.getConfidence();				
				double c2 = hashedCorrespondences.get(c);
				// System.out.println("--- c1:" + c1 + " c2:" + c2);
				hashedCorrespondences.put(c, c1 + c2);
			}
			else {
				hashedCorrespondences.put(c, c.getConfidence());
			}
		}
		for (Correspondence c : hashedCorrespondences.keySet()) {
			double newConfidence = hashedCorrespondences.get(c);
			
			c.setConfidence(newConfidence);


			
			
		}
		
		mapping.setCorrespondences(hashedCorrespondences.keySet());
		return mapping;
		// old stuff
		// HashSet<Correspondence> thisCSet = this.getCorrespondencesAsSet();
		// HashSet<Correspondence> thatCSet = that.getCorrespondencesAsSet();
		// thisCSet.addAll(thatCSet);
	}
	
	/**
	* Creates and returns a copy of this mapping. Te copy has ist own list of correspondences,
	* but contains references to the same correspondences as are referred to be this mapping. 
	* 
	* @return A copy of this mapping.
	*/
	public Mapping getCopy() {
		Mapping copy = new Mapping();
		for (Correspondence c : this) {
			Correspondence cloned = c.getDeepCopy();
			copy.push(cloned);
			// copy.push(c); that code results in some bugs related to the joining operation
			
		}
		copy.adjustConceptConfidence(this.conceptAdjustmentFactor);
		copy.adjustPropertyConfidence(this.propertyAdjustmentFactor);
		if (this.idPattern != null) {
			copy.idPattern = new boolean[this.idPattern.length];
			for (int i = 0; i < this.idPattern.length; i++) {
				copy.idPattern[i] = this.idPattern[i];
			}
		}
		return copy;
	}
	
	// --- CHANGING THE MAPPING --- 

	/**
	* Applies a threshold on the mapping by removing every
	* correspondence with a confidence below the threhold.
	* 
	* @param threshhold The threshold.
	* @return The number of correspondences that have been removed.
	*/
	public int applyThreshhold(double threshhold) {
		ArrayList<Correspondence> thresholdedCorrespondences = new ArrayList<Correspondence>();
		int numOfRemovedCorrespondences = 0;
		for (Correspondence c : this.correspondences) {
			if (c.getConfidence() > threshhold) { thresholdedCorrespondences.add(c); }
			else { numOfRemovedCorrespondences++; }
		}
		this.correspondences = thresholdedCorrespondences;
		return numOfRemovedCorrespondences;
	}
	
	/**
	* Sets all correspondences in this mapping to the same value.
	* 
	* @param normConfidence The new confidence value for all correspondences in this mapping.
	* @throws AlcomoException Thrown if the chosen confidence value is not in the range [0,1].
	*/
	public void normalize(double normConfidence) throws AlcomoException {
		for (Correspondence c : this.correspondences) {
			c.setConfidence(normConfidence);
		}
		
	}
	
	/**
	* Normalizes the confidences of the correspondences to the range [0.0, 1.0].
	* 
	* @param minBound The lower bound (inclusive) of the range.
	* @param maxBound The upper bound (inclusive) of the range.
	*/
	public void normalize() {
		this.normalize(0.0, 1.0);
	}
	
	/**
	* Normalizes the confidences of the correspondences to a given range.
	* If all correspondences have the same confidence, all of them
	* are set to the upper bound of the range.
	* 
	* @param minBound The lower bound (inclusive) of the range.
	* @param maxBound The upper bound (inclusive) of the range.
	*/
	public void normalize(double minBound, double maxBound) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		// store the lowest and greates value of the correspondences.
		for (Correspondence c : this.correspondences) {
			min = (c.getConfidence() < min) ? c.getConfidence() : min;
			max = (c.getConfidence() > max) ? c.getConfidence() : max;
		}
		if (this.correspondences.size() > 0) {
			// if all correspondences had the same confidence, set them to the upper bound
			if (min == max) {
				for (int i = 0; i < this.correspondences.size(); i++) {
					this.correspondences.get(i).setConfidence(maxBound);
				
				}			
			}
			// the normal case
			else {
				double sim;
				for (int i = 0; i < this.correspondences.size(); i++) {
					sim = this.correspondences.get(i).getConfidence();
					sim = ((sim - min) * ((maxBound - minBound)  / (max - min))) + minBound;
					sim = (sim > maxBound) ? maxBound : sim;
					sim = (sim < minBound) ? minBound : sim;
					// System.out.println(sim);
					this.correspondences.get(i).setConfidence(sim);
			
				}
			}
		}
	}	
	
	/**
	* Inverts this mapping. This operation changes source and target entities of the
	* correspondences as well as replaces the semantic relations with inverse relations.
	*
	*/
	public void invert() {	
		for (Correspondence c : this.correspondences) {
			c.invert();		
		}
	}
	
	// --- GETTER AND SETTER ---
	

	
	/**
	* Sets a list of correspondences as correspondences of the mapping. 
	*/
	public void setCorrespondences(AbstractList<Correspondence> correspondences) {
		this.correspondences = new ArrayList<Correspondence>();
		HashSet<Correspondence> correspondencesAsSet = new HashSet<Correspondence>();
		correspondencesAsSet.addAll(correspondences);
		this.correspondences.addAll(correspondencesAsSet);
	}
	
	/**
	* Sets a set of correspondences as correspondences of the mapping. 
	*/
	public void setCorrespondences(Set<Correspondence> correspondencesAsSet) {
		this.correspondences = new ArrayList<Correspondence>();
		this.correspondences.addAll(correspondencesAsSet);
	}	

	/**
	* Returns the correspondences of this mapping as list.
	* 
	* @return The internally stored list of correspondences.
	*/
	public ArrayList<Correspondence> getCorrespondences() {
		return correspondences;
	}

	/**
	* Returns the correspondences of this mapping as set.
	* 
	* @return The set of correspondences.
	*/
	public HashSet<Correspondence> getCorrespondencesAsSet() {
		HashSet<Correspondence> correspondencesAsSet = new HashSet<Correspondence>();
		correspondencesAsSet.addAll(this.getCorrespondences());
		return correspondencesAsSet;
	}	
	
	/**
	* Returns the size of the mapping.
	* 
	* @return The size of the mapping in number of correspondences.
	*/
	public int size() {
		return this.correspondences.size();
	}
	
	/**
	* Binds the mapping (more precise: the uris of the correspondences) to
	* the internal representation of the entities.
	* 
	* @param sourceOntology Source ontology.
	* @param targetOntology Target ontology.
	* @return The part of the mapping that contained referenced that could not be resolved.
	* @throws MappingException Thrown if a correspondences relates entities of different types that
	* cannot be matched for principle reasons.
	* @throws CorrespondenceException Thrown in case of an adjustment of confidences that is not possible.
	*/
	public Mapping bind(LocalOntology sourceOntology, LocalOntology targetOntology) throws MappingException, CorrespondenceException {
		Mapping nonreferingMapping = new Mapping();
		Entity sourceEntity = null;
		Entity targetEntity = null;
		int i = 0; 
		while (i < this.size()) {
			Correspondence c = this.get(i);

			try { sourceEntity = sourceOntology.getEntityByUri(c.getSourceEntityUri()); }
			catch(OntologyException e) {
				this.log.warn("<correspondence " + c + " refers not existing entity " + c.getSourceEntityUri()+ ">");
				nonreferingMapping.push(c);
				this.remove(i);
				continue;
			}
			try { targetEntity = targetOntology.getEntityByUri(c.getTargetEntityUri()); }
			catch(OntologyException e) {
				this.log.warn("<correspondence " + c + " refers not existing entity " + c.getTargetEntityUri() + ">");
				nonreferingMapping.push(c);
				this.remove(i);
				continue;
			}			
			if (!(sourceEntity.isConcept() == targetEntity.isConcept())) {
				this.log.warn("<correspondence " + c + " combines different types of entities (ignored) >");
				nonreferingMapping.push(c);
				this.remove(i);
				continue;
				/*
				throw new MappingException(
					MappingException.CORRESPONDENCE_PROBLEM,
					"entity " + c.getSourceEntityUri() + " and entity " +  c.getTargetEntityUri() + " are incompatible"
				);
				*/
			}
			
			c.setSourceEntity(sourceEntity);
			c.setTargetEntity(targetEntity);
			
			
			
			if ((sourceEntity.isConcept()) && (this.conceptAdjustmentFactor > 0.0)){
				c.setConfidence(c.getConfidence() * this.conceptAdjustmentFactor);
			}
			if ((!(sourceEntity.isConcept())) && (this.propertyAdjustmentFactor > 0.0)){
				c.setConfidence(c.getConfidence() * this.propertyAdjustmentFactor);
			}
			i++;
		}
		return nonreferingMapping;
	}

	public Correspondence get(int index) {
		if (index < this.size()) { return this.correspondences.get(index); }
		else { return null; }
	}

	/**
	* Adds a correspondence at the end of a mapping.
	* 
	* @param correspondence The correspondence to be added.
	*/
	public void push(Correspondence correspondence) {
		this.correspondences.add(correspondence);
	}
	
	
	/**
	* Adds each correspondence of another mapping at the end of this mapping.
	* 
	* @param mapping The mapping to be added.
	*/
	public void push(Mapping mapping) {
		for (Correspondence c : mapping) {
			this.correspondences.add(c);
		}
	}	
	
	
	
	/**
	* Removes the last correspondence from this mapping.
	*  
	* @return The correspondence that has been removed.
	*/
	public Correspondence pop() {
		return this.correspondences.remove(this.correspondences.size() - 1);
	}
	
	/**
	* Removes a correspondence at a specified index.
	* 
	* @param index The index of the correspondence.
	* @return The correspondence that will be removed.
	*/
	public Correspondence remove(int index) {
		if (this.idPattern != null) { this.adjustIdentification(index); }
		return this.correspondences.remove(index);
	}
	

	
	private void adjustIdentification(int index) {
		int thrown = 0;
		int counter = 0;
		while ((!(counter - thrown == index )) || (idPattern[counter] == false)) {
			if (this.idPattern[counter] == false) {
				thrown++;
			}
			counter++;
		}
		idPattern[counter] = false;
	}
	
	public final boolean[] getIdPattern() {
		return this.idPattern;
	}
	
	public boolean equals(Object object) {
		// System.out.println("This: " +  this.size() + "\nThat: ?");
		try {
			Mapping that = (Mapping)object;
			if (this.getIdPattern() == null || that.getIdPattern() == null) {
				return false;
			}
			boolean[] thisPattern = this.getIdPattern();
			boolean[] thatPattern = that.getIdPattern();
			if (thisPattern.length != thatPattern.length) { return false; }
			else {
				for (int i = 0; i < thisPattern.length; i++) {
					if (thisPattern[i] != thatPattern[i]) { return false; }
				}
				return true;
			}
		}
		catch(ClassCastException e) { return false; }
	}
	
	
	public int hashCode() {
		if (this.idPattern == null) {
			return 1;
		}		
		int hashcode = 0;
		int size = (this.idPattern.length < 32) ? this.idPattern.length : 32;
		for (int i = 0 ; i < size; i++) {
			if (this.idPattern[i]) { hashcode += 1; }
			hashcode = hashcode << 1;
		}
		return hashcode;
	}
	
	public Iterator<Correspondence> iterator() {
		return this.correspondences.iterator();
	}

	public void sortDescending() {
		Collections.sort(this.correspondences);
		Collections.reverse(this.correspondences);		
	}

	/**
	* Removes every correspondence from the first one till the specified correspondence inclusive.
	*/
	public void shrinkTill(Correspondence c) {
		// remove all correspondences if c is null
		if (c == null) {
			this.correspondences.clear();
		}
		// otherwise remove step by step until c reached
		else {
			Correspondence temp;
			do { temp = this.correspondences.remove(0); }
			while (!(temp.equals(c)));
		}
	}

	/**
	* Returns a submapping of this mapping from index 0 to indexInclusive.
	* 
	* @param indexInclusive The index of the last correspondence to be part of the sub mapping
	* @return The submapping.
	*/
	public Mapping subMapping(int indexInclusive) {
		int indexExclusive = (indexInclusive >= this.size()) ? this.size() : indexInclusive + 1;
		Mapping subMapping = new Mapping();
		ArrayList<Correspondence> subCorrespondences = new ArrayList<Correspondence>();
		subCorrespondences.addAll(this.correspondences.subList(0, indexExclusive));
		subMapping.setCorrespondences(subCorrespondences);
		return subMapping;
	}

	/**
	* Remove all correspondences from the mapping that express a different semantic relation than
	* equivalence.
	* 
	* @return The number of correspondences removed.
	*
	*/
	public int reduceToEquivalenceCorrespondences() {
		int i = 0;
		int removed = 0;
		while (i < this.size()) {
			if (this.get(i).getRelation().getType() == SemanticRelation.EQUIV) {
				i++;
			}
			else {
				removed++;
				this.correspondences.remove(i);
			}
		}
		return removed;
	}

	public void modifyToEquivalenceCorrespondences() {
		int i = 0;
		while (i < this.size()) {
			if (this.get(i).getRelation().getType() != SemanticRelation.EQUIV) {
				try {
					this.get(i).setRelation(new SemanticRelation(SemanticRelation.EQUIV));
				}
				catch (CorrespondenceException e) {
					// will never happen in this situation
				}
			}
			i++;
		}
		
	}
	

	/**
	* Joins the specified mappings into a single mapping that contains all correspondences
	* of the specified mappings. The new confidence value is computed as the normalized
	* sum of all confidences. If for example we join two mappings and there is a correspondence
	* c1 in both we compute the new confidence as the sum of both confidences each divided by two.
	* If its only ibn one of the them, the new confidence will be between 0 and 0.5. It is recommended
	* to normalize the mappings before joining them.
	* 
	* @param mappings The mappings to be joined.
	* @return The joined mapping.
	* @throws PCFException
	* @throws CorrespondenceException
	*/
	public static Mapping getJoinedMapping(Mapping[] mappings) throws PCFException, CorrespondenceException {
		double[] weights = new double[mappings.length];
		double weight = 1.0 / (double)weights.length;
		for (int i = 0; i < weights.length; i++) {
			weights[i] = weight;
		}
		return getJoinedMapping(mappings, weights);
	}
	
	public static Mapping getJoinedMapping(Mapping[] mappings, double[] weights) throws PCFException, CorrespondenceException {
		if (mappings.length != weights.length) {
			throw new PCFException(
					PCFException.INVALID_PARAM_COMBINATION,
					"in a mapping join operation you need the same number of mappings and weights"
			);
		}
		HashMap <Correspondence, Double> hashedConfidences = new HashMap<Correspondence, Double>();
		for (int m = 0; m < mappings.length; m++) {
			double weight = weights[m];
			for (Correspondence c : mappings[m]) {
				if (hashedConfidences.containsKey(c)) {
					hashedConfidences.put(c, c.getConfidence() * weight + hashedConfidences.get(c));
				}
				else {
					hashedConfidences.put(c, c.getConfidence() * weight);
				}
			}
		}
		Mapping joinedMapping = new Mapping();
		for (Correspondence c : hashedConfidences.keySet()) {
			double conf = hashedConfidences.get(c);
			// double conf = hashedConfidences.get(c) > 1 ? 1 :  hashedConfidences.get(c);
			c.setConfidence(conf);
			joinedMapping.push(c);
		}
		return joinedMapping;
	}
	
	public void adjustPropertyConfidence(double factor) {
		this.propertyAdjustmentFactor = factor;
	}
	
	public void adjustConceptConfidence(double factor) {
		this.conceptAdjustmentFactor = factor;
	}

	public double getConfidenceTotal() {
		double total = 0.0d;
		for (Correspondence c : this) {
			total += c.getConfidence();
		}
		return total;
	}

	/**
	* @return -1 if this is less than that which is the case if trust of this
	* is greater than trust of that
	*/
	public int compareTo(Mapping that) {
		double thisTrust = this.getConfidenceTotal();
		double thatTrust = that.getConfidenceTotal();
		if (thisTrust < thatTrust) { return 1; }
		else if (thisTrust == thatTrust) { return 0; }
		else { return -1; }
	}
	
	/**
	* Filters out every correspondence in this mapping that has not the specified source namespace.
	* 
	* @param sourceNamespace The namespace to be used as filter.
	*/
	public void filterBySourceNamespace(String sourceNamespace) {
		this.filterNamespace(sourceNamespace, true);
	}

	/**
	* Filters out every correspondence in this mapping that has not the specified target namespace.
	* 
	* @param targetNamespace The namespace to be used as filter.
	*/
	public void filterByTargetNamespace(String targetNamespace) {
		this.filterNamespace(targetNamespace, false);
	}

	private void filterNamespace(String namespace, boolean sourceNotTarget) {
		ArrayList<Correspondence> filteredCorrespondences = new ArrayList<Correspondence>();
		for (Correspondence c : this.correspondences) {
			if (sourceNotTarget) {
				if (c.getSourceNamespace().equals(namespace)) {
					filteredCorrespondences.add(c);
				}
			}
			else {
				if (c.getTargetNamespace().equals(namespace)) {
					filteredCorrespondences.add(c);
				}
			}
		}
		this.correspondences = filteredCorrespondences;
	}

	public void setConflictPairs(Set<ConflictPair> cps) {
		this.conflictPairs = cps;
	}
	
	public Set<ConflictPair> getConflictPairs() {
		return this.conflictPairs;
	}

	/**
	* Helper method for easy working with different version of alignments that encoded version numbers in 
	* uris (like for each year of the benchmark track).
	* 
	* @param regExp
	* @param newString
	* @throws CorrespondenceException
	*/
	public void replaceSubstring(String regExp, String newString) throws CorrespondenceException {
		String e1, e2;
		for (Correspondence c : this) {
			e1 = c.getSourceEntityUri();
			e2 = c.getTargetEntityUri();
			e1 = e1.replaceAll(regExp, newString);
			e2= e2.replaceAll(regExp, newString);
			// System.out.println(e1);
			c.setSourceEntityUri(e1);
			c.setTargetEntityUri(e2);
			
		}
		
	}

	public void replaceSourceNamespace(String namespace) {
		for (Correspondence c : this) {
			c.setSourceNamespace(namespace);
		}
	}
	
	public void replaceTargetNamespace(String namespace) {
		for (Correspondence c : this) {
			c.setTargetNamespace(namespace);
		}
	}

	/**
	* Applies a threshold such that the fraction of the rt worst correspondences are removed.
	*  
	* @param rt The relative threshold (between 0 and 1.0).
	*/
	public void applyThresholdRelative(double rt) {
		int numOfCorrs = this.size();
		int maxNumOfCorrs = (int)Math.round(numOfCorrs * (1.0 - rt));
		this.sortDescending();
		while (this.size() > maxNumOfCorrs) {
			this.correspondences.remove(this.size() - 1);
			// System.out.println(c.getConfidence());
		}
	}
	
	/**
	* Converts  all correspondences from the mapping that express a different semantic relation than
	* equivalence to equivalence.
	* 
	* @return The number of correspondences removed.
	*
	*/
	public int convertToEquivalenceCorrespondences() {
		int i = 0;
		int removed = 0;
		while (i < this.size()) {
			if (this.get(i).getRelation().getType() != SemanticRelation.EQUIV) {
				try {
					this.get(i).setRelation(new SemanticRelation(SemanticRelation.EQUIV));
				} catch (CorrespondenceException e) { }
			}
			i++;
		}
		return removed;
	}
	/**
	 * Splits all equivalence correspondences in this mapping into subsumption correspondences in both directions. If
	 * all correspondences in the mapping are equivalence correspondences, a call to this method duplicates the
	 * number of correspondences. All non-equivalenve correspondences are not touched at all.
	 * 
	 * @throws CorrespondenceException Cannot occur within this method.
	 */
	public void splitToSubsumptionCorrespondences() throws CorrespondenceException {
		ArrayList<Correspondence> correspondences = new ArrayList<Correspondence>();
		SemanticRelation sub = new SemanticRelation(SemanticRelation.SUB);
		SemanticRelation sup = new SemanticRelation(SemanticRelation.SUPER);
		for (Correspondence c : this.correspondences) {
			double conf = c.getConfidence();
			String sourceUri = c.getSourceEntityUri();
			String targetUri = c.getTargetEntityUri();
			SemanticRelation sr = c.getRelation();
			if (SemanticRelation.EQUIV == sr.getType()) {
				Correspondence c1 = new Correspondence(sourceUri, targetUri, sub, conf);
				Correspondence c2 = new Correspondence(sourceUri, targetUri, sup, conf);
				c1.setSourceEntity(c.getSourceEntity());
				c1.setTargetEntity(c.getTargetEntity());
				c2.setSourceEntity(c.getSourceEntity());
				c2.setTargetEntity(c.getTargetEntity());
				correspondences.add(c1);
				correspondences.add(c2);
			}
			else {
				correspondences.add(c);
			}
		}
		this.correspondences  = correspondences;
		
	}
	
	/**
	* Checks if a given correspondence is contained in this mapping.
	* Requires at the moemnt to iterate over all correpondences, baldy implemented.
	* Can be done by constant time if hashmaps are used.
	* 
	* @param c1 The correspondence for which it is checked containment
	* @return True, if the correspondence is contained, false otherwise.
	*/	
	public boolean contains(Correspondence c1) {
		for (Correspondence c2 : this) {
			if (c1.equals(c2)) return true;
		}
		return false;
	}

	/**
	 * Joins all pairs like a < b and a > b into equivalence correspondences a = b in place.
	 */
	public void joinToEquivalence() {
		ArrayList<Correspondence> joinedCorrespondences = new ArrayList<Correspondence>();
		HashSet<String> subCorKeys = new HashSet<String>();
		HashSet<String> superCorKeys = new HashSet<String>();
		HashSet<Correspondence> subCorrs = new HashSet<Correspondence>();
		HashSet<Correspondence> superCorrs = new HashSet<Correspondence>();
		for (Correspondence c : this) {
			String key = c.getSourceEntityUri() + c.getTargetEntityUri();
			if (c.getRelation().getType() == SemanticRelation.SUB) {
				subCorKeys.add(key);
				subCorrs.add(c);
				if (superCorKeys.contains(key)) {
					subCorKeys.remove(key);
					subCorrs.remove(c);
					c.invertRelation();
					superCorrs.remove(c);
					Correspondence cEquiv = createAsEquivalenceCorrespondence(c);
					joinedCorrespondences.add(cEquiv);
				}
			}
			else if (c.getRelation().getType() == SemanticRelation.SUPER) {
				superCorKeys.add(key);
				superCorrs.add(c);
				if (subCorKeys.contains(key)) {
					superCorKeys.remove(key);
					superCorrs.remove(c);
					c.invertRelation();
					subCorKeys.remove(key);
					subCorrs.remove(c);
					Correspondence cEquiv = createAsEquivalenceCorrespondence(c);
					joinedCorrespondences.add(cEquiv);
				}
			}
			else {
				joinedCorrespondences.add(c);
			}
		}
		joinedCorrespondences.addAll(subCorrs);
		joinedCorrespondences.addAll(superCorrs);
		this.correspondences = joinedCorrespondences;
	}
	
	private Correspondence createAsEquivalenceCorrespondence(Correspondence c) {
		String sourceEntityUri = c.getSourceEntityUri();
		String targetEntityUri = c.getTargetEntityUri();
		double conf = c.getConfidence();
		
		Correspondence cEquiv = null;
		try {
			cEquiv = new Correspondence(sourceEntityUri, targetEntityUri, new SemanticRelation(SemanticRelation.EQUIV), conf);
			cEquiv.setSourceEntity(c.getSourceEntity());
			cEquiv.setTargetEntity(c.getTargetEntity());	
		}
		catch (CorrespondenceException e) {
			
		}
		return cEquiv;
	}
	



}
