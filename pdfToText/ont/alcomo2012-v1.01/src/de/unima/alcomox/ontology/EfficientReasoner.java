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


import org.semanticweb.owlapi.model.OWLClass;

import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.Settings;
import de.unima.alcomox.exceptions.CorrespondenceException;
import de.unima.alcomox.mapping.Correspondence;
import de.unima.alcomox.mapping.Mapping;
import de.unima.alcomox.mapping.SemanticRelation;

/**
* This class represents a reasoner for efficient pairwise but incomplete conflict detection. 
* It is the main type of structural reasoning used within the alcomo systems to avoid
* the complexity of full reasoning.
*/
public class EfficientReasoner extends AlcomoReasoner {
	
	/**
	* If set to true, both interval based and reasoner based methods are executed. Should only be
	* used for testing correctness of the interval based approach.
	*/
	public static boolean TEST_INTERVALTREE_MODE = false;
	
	/**
	* Will be set by the internal comparison to true, if there is a difference in the behavior of
	* interval based method and reasoner based method.
	*/
	public static boolean TEST_INTERVALTREE_MODE_DEVIATION = false;
	

	// public static boolean display = false;
	private static final int PATTERN_SUB = 1;
	private static final int PATTERN_DIS = 2;	
	
	/**
	* Constructs an reasoner for efficient pairwise but incomplete conflict detection.
	*  
	* @param sourceOntology Source ontology.
	* @param targetOntology Target ontology.
	* @param extractionProblem The extraction problem that this reasoner is used for.
	*/
	public EfficientReasoner(LocalOntology sourceOntology, LocalOntology targetOntology, ExtractionProblem extractionProblem) {
		super(sourceOntology, targetOntology, extractionProblem);
	}
	

	public boolean conflictsWithMapping(Correspondence candidate, Mapping mapping) {
		for (Correspondence c : mapping) {		
			if (this.isConflictPair(c, candidate)) { return true; }
		}
		return false;
	}
	
	public boolean isConflictSet(Mapping mapping) {
		for (int i = 0; i < mapping.size(); i++) {
			for (int j = i + 1; j < mapping.size(); j++) {
				if (this.isConflictPair(mapping.get(i), mapping.get(j))) {
					return true;
				}
			}			
		}
		return false;
	}
	
	/**
	* Checks wether two correspondences are in conflict. These check is incomplete. There might be pairs of
	* correspondences that induce inconsistencies that is not detected by this method.
	* 
	* @param Some correspondence.
	* @param Another correspondence.
	* @return True if both correspondences are in conflict, false if they are not n conflict or if the
	* conflict cannot be detected.
	*/
	public boolean isConflictPair(Correspondence c1, Correspondence c2) {
		Entity s1 = c1.getSourceEntity();
		Entity s2 = c2.getSourceEntity();
		Entity t1 = c1.getTargetEntity();
		Entity t2 = c2.getTargetEntity();
		
		// its about one-to-one alignments (for equivalence and eventually for subsumption) 
		
		if (!(Settings.ONE_TO_ONE_ONLYEQUIV)) {
			if (Settings.ONE_TO_MANY || Settings.ONE_TO_ONE) {
				if (c1.getTargetEntityUri().equals(c2.getTargetEntityUri())) {
					return true;
				}
			}
			if (Settings.MANY_TO_ONE || Settings.ONE_TO_ONE) {
				if (c1.getSourceEntityUri().equals(c2.getSourceEntityUri())) {
					return true;
				}
			}
		}
		else {
			if ((c1.getRelation().getType() == SemanticRelation.EQUIV) && (c2.getRelation().getType() == SemanticRelation.EQUIV)) {
				if (Settings.ONE_TO_MANY || Settings.ONE_TO_ONE) {
					if (c1.getTargetEntityUri().equals(c2.getTargetEntityUri())) {
						return true;
					}
				}
				if (Settings.MANY_TO_ONE || Settings.ONE_TO_ONE) {
					if (c1.getSourceEntityUri().equals(c2.getSourceEntityUri())) {
						return true;
					}
				}
			}
		}
		if (Settings.DISABLE_REASONING) { return false; }
		
		if (c1.isEquivOrSub()) {
			if (c2.isEquivOrSub()) {
				// System.out.println("SUB-SUB");
				if (this.checkPropagationPattern(t1, t2, s1, s2, this.targetOntology, this.sourceOntology, PATTERN_DIS)) {
					return true;
				}	
			}
			if (c2.isEquivOrSuper()) {
				// System.out.println("SUB-SUPER");
				if (this.checkPropagationPattern(s2, s1, t2, t1, this.sourceOntology, this.targetOntology, PATTERN_SUB))  {
					return true;
				}
				if (this.checkPropagationPattern(t1, t2, s1, s2, this.targetOntology, this.sourceOntology, PATTERN_SUB)) {
					return true;
				}
			}
		}
	
		
		if (c1.isEquivOrSuper()) {
			if (c2.isEquivOrSub()) {
				// System.out.println("SUPER-SUB");
				if (this.checkPropagationPattern(s1, s2, t1, t2, this.sourceOntology, this.targetOntology, PATTERN_SUB)) {
					return true;
				}
				if (this.checkPropagationPattern(t2, t1, s2, s1, this.targetOntology, this.sourceOntology, PATTERN_SUB)) {
					return true;
				}
			}
			if (c2.isEquivOrSuper()) {
				// System.out.println("SUPER-SUPER");
				if (this.checkPropagationPattern(s1, s2, t1, t2, this.sourceOntology, this.targetOntology, PATTERN_DIS)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	* Estimates the conflict sensitivity by drawing a sample of randomly generated correspondences.
	* 
	* @param c A correspondence for which conflict sensitivity has to be computed.
	* @param exactness The size of the sample in number of randomly generated correspondences.
	* @return A probabaility for c to in in a conflict with a randomly generated correspondence.
	* @throws CorrespondenceException
	*/
	public double estimateConflictSensitivity(Correspondence c, int exactness) throws CorrespondenceException {
		Correspondence cr;
		int conflictCounter = 0;
		for (int i = 0; i < exactness; i++) {
			cr = this.createRandomEquivCorrespondence();
			if (this.isConflictPair(c, cr)) { conflictCounter++; }
		}
		return ((double)conflictCounter / (double)exactness);
	}
	
	// **********************************
	//     private playground
	// **********************************
	
	private Correspondence createRandomEquivCorrespondence() throws CorrespondenceException {
		Entity sourceEntity = this.sourceOntology.getRandomEntity();
		Entity targetEntity = this.targetOntology.getRandomEntity(sourceEntity.isConcept());
		Correspondence c = new Correspondence(ExtractionProblem.ALCOMO_URL + "#sourceEntity", ExtractionProblem.ALCOMO_URL + "#targetEntity", new SemanticRelation(SemanticRelation.EQUIV));
		c.setSourceEntity(sourceEntity);
		c.setTargetEntity(targetEntity);
		return c;
	}
	
	// full version, 
	private boolean checkPropagationPattern(Entity s1, Entity s2, Entity  t1, Entity t2, LocalOntology sourceOnt, LocalOntology targetOnt, int p) {
		if (Settings.PROPERTY_RANGE_EXTENSION == false) {
			return this.checkPropagationPatternWithoutRange(s1, s2, t1, t2, sourceOnt, targetOnt, p);
		}
		if (s1.isConcept() && s2.isConcept()) {
			return checkAtomicPattern(s1.getConcept(), s2.getConcept(), t1.getConcept(), t2.getConcept(), sourceOnt, targetOnt, p);
		}
		else if (s1.isConcept() && (s2.isDataProperty() || t2.isDataProperty())) {
			return checkAtomicPattern(s1.getConcept(), s2.getDomain(), t1.getConcept(), t2.getDomain(), sourceOnt, targetOnt, p);
		}
		else if ((s1.isDataProperty() || t1.isDataProperty()) && s2.isConcept()) {
			return checkAtomicPattern(s1.getDomain(), s2.getConcept(), t1.getDomain(), t2.getConcept(), sourceOnt, targetOnt, p);
		}
		else if ((s1.isObjectProperty() && t1.isObjectProperty()) && s2.isConcept()) {
			return (
				checkAtomicPattern(s1.getDomain(), s2.getConcept(), t1.getDomain(), t2.getConcept(), sourceOnt, targetOnt, p) ||
				checkAtomicPattern(s1.getRange(), s2.getConcept(), t1.getRange(), t2.getConcept(), sourceOnt, targetOnt, p)
			);
		}
		else if (s1.isConcept() && (s2.isObjectProperty() && t2.isObjectProperty())) {
			return (
				checkAtomicPattern(s1.getConcept(), s2.getDomain(), t1.getConcept(), t2.getDomain(), sourceOnt, targetOnt, p) ||
				checkAtomicPattern(s1.getConcept(), s2.getRange(), t1.getConcept(), t2.getRange(), sourceOnt, targetOnt, p)
			);
		}
		else if ((s1.isDataProperty() || t1.isDataProperty()) && (s2.isDataProperty() || t2.isDataProperty())) {
			return checkAtomicPattern(s1.getDomain(), s2.getDomain(), t1.getDomain(), t2.getDomain(), sourceOnt, targetOnt, p);
		}		
		else if ((s1.isDataProperty() || t1.isDataProperty()) && (s2.isObjectProperty() && t2.isObjectProperty())) {
			return(
				checkAtomicPattern(s1.getDomain(), s2.getDomain(), t1.getDomain(), t2.getDomain(), sourceOnt, targetOnt, p) ||
				checkAtomicPattern(s1.getDomain(), s2.getRange(), t1.getDomain(), t2.getRange(), sourceOnt, targetOnt, p)
			);
		}		
		else if ((s1.isObjectProperty() && t1.isObjectProperty()) && (s2.isDataProperty() || t2.isDataProperty())) {
			return(
				checkAtomicPattern(s1.getDomain(), s2.getDomain(), t1.getDomain(), t2.getDomain(), sourceOnt, targetOnt, p) ||
				checkAtomicPattern(s1.getRange(), s2.getDomain(), t1.getRange(), t2.getDomain(), sourceOnt, targetOnt, p)
			);
		}
		else if ((s1.isObjectProperty() && t1.isObjectProperty()) && (s2.isObjectProperty() && t2.isObjectProperty())) {
			return (
				checkAtomicPattern(s1.getDomain(), s2.getDomain(), t1.getDomain(), t2.getDomain(), sourceOnt, targetOnt, p) ||
				checkAtomicPattern(s1.getRange(), s2.getDomain(), t1.getRange(), t2.getDomain(), sourceOnt, targetOnt, p) ||
				checkAtomicPattern(s1.getDomain(), s2.getRange(), t1.getDomain(), t2.getRange(), sourceOnt, targetOnt, p) ||
				checkAtomicPattern(s1.getRange(), s2.getRange(), t1.getRange(), t2.getRange(), sourceOnt, targetOnt, p)	
			);
		}		
		return false;
	}

	// version where all range checks are removed 
	private boolean checkPropagationPatternWithoutRange(Entity s1, Entity s2, Entity  t1, Entity t2, LocalOntology sourceOnt, LocalOntology targetOnt, int p) {

		if (s1.isConcept() && s2.isConcept()) {
			return checkAtomicPattern(s1.getConcept(), s2.getConcept(), t1.getConcept(), t2.getConcept(), sourceOnt, targetOnt, p);
		}
		else if (s1.isConcept() && (s2.isDataProperty() || t2.isDataProperty())) {
			return checkAtomicPattern(s1.getConcept(), s2.getDomain(), t1.getConcept(), t2.getDomain(), sourceOnt, targetOnt, p);
		}
		else if ((s1.isDataProperty() || t1.isDataProperty()) && s2.isConcept()) {
			return checkAtomicPattern(s1.getDomain(), s2.getConcept(), t1.getDomain(), t2.getConcept(), sourceOnt, targetOnt, p);
		}
		else if ((s1.isObjectProperty() && t1.isObjectProperty()) && s2.isConcept()) {
			return checkAtomicPattern(s1.getDomain(), s2.getConcept(), t1.getDomain(), t2.getConcept(), sourceOnt, targetOnt, p);
		}
		else if (s1.isConcept() && (s2.isObjectProperty() && t2.isObjectProperty())) {
			return checkAtomicPattern(s1.getConcept(), s2.getDomain(), t1.getConcept(), t2.getDomain(), sourceOnt, targetOnt, p);
		}
		else if ((s1.isDataProperty() || t1.isDataProperty()) && (s2.isDataProperty() || t2.isDataProperty())) {
			return checkAtomicPattern(s1.getDomain(), s2.getDomain(), t1.getDomain(), t2.getDomain(), sourceOnt, targetOnt, p);
		}		
		else if ((s1.isDataProperty() || t1.isDataProperty()) && (s2.isObjectProperty() && t2.isObjectProperty())) {
			return checkAtomicPattern(s1.getDomain(), s2.getDomain(), t1.getDomain(), t2.getDomain(), sourceOnt, targetOnt, p);
		}		
		else if ((s1.isObjectProperty() && t1.isObjectProperty()) && (s2.isDataProperty() || t2.isDataProperty())) {
			return checkAtomicPattern(s1.getDomain(), s2.getDomain(), t1.getDomain(), t2.getDomain(), sourceOnt, targetOnt, p);
		}
		else if ((s1.isObjectProperty() && t1.isObjectProperty()) && (s2.isObjectProperty() && t2.isObjectProperty())) {
			return checkAtomicPattern(s1.getDomain(), s2.getDomain(), t1.getDomain(), t2.getDomain(), sourceOnt, targetOnt, p);
		}		
		return false;
	}

	
	private boolean checkAtomicPattern(OWLClass sc1, OWLClass sc2, OWLClass tc1, OWLClass tc2, LocalOntology sourceOnt, LocalOntology targetOnt, int pattern) {
		if (targetOnt.isUnsatisfiable(tc1)) { return false; }
		if (targetOnt.isUnsatisfiable(tc2)) { return false; }
		if (sourceOnt.isUnsatisfiable(sc1)) { return false; }
		if (sourceOnt.isUnsatisfiable(sc2)) { return false; }
		
		if (pattern == PATTERN_SUB) {
			return this.checkAtomicSubsumptionPattern(sc1, sc2, tc1, tc2, sourceOnt, targetOnt);
		}
		else if (pattern == PATTERN_DIS) {
			return this.checkAtomicDisjointnessPattern(sc1, sc2, tc1, tc2, sourceOnt, targetOnt);
		}
		return false;
	}
	
	
	private boolean checkAtomicSubsumptionPattern(OWLClass sc1, OWLClass sc2, OWLClass tc1, OWLClass tc2, LocalOntology sourceOnt, LocalOntology targetOnt) {
		if (TEST_INTERVALTREE_MODE) return  checkAtomicSubsumptionPatternXXX(sc1, sc2, tc1, tc2, sourceOnt, targetOnt);
		// uses interval based methods
		if (sourceOnt instanceof IOntology && targetOnt instanceof IOntology) {
			IOntology sourceIOnt = (IOntology)sourceOnt;
			IOntology targetIOnt = (IOntology)targetOnt;
			if (sourceIOnt.isISubClassOfClass(sc1, sc2)) {
				if (targetIOnt.hasICommonSubDisjointClass(tc1, tc2)) {
					return true;
				}	
			}
			return false;
		}
		// uses standard reasoner methods
		else {
			if (sourceOnt.isSubClassOfClass(sc1, sc2)) {
				for (OWLClass subClass : targetOnt.getSubClassesOfClass(tc1)) {
					if (targetOnt.isUnsatisfiable(subClass)) { continue; }
					if (targetOnt.isDisjointClassWithClass(subClass, tc2)) { 
						return true;
					}
				}
			}
			return false;
		}
	}
	
	private boolean checkAtomicDisjointnessPattern(OWLClass sc1, OWLClass sc2, OWLClass tc1, OWLClass tc2, LocalOntology sourceOnt, LocalOntology targetOnt) {
		if (TEST_INTERVALTREE_MODE) return  checkAtomicDisjointnessPatternXXX(sc1, sc2, tc1, tc2, sourceOnt, targetOnt);
		// uses interval based representation for reasoning requests
		if (sourceOnt instanceof IOntology && targetOnt instanceof IOntology) {
			IOntology sourceIOnt = (IOntology)sourceOnt;
			IOntology targetIOnt = (IOntology)targetOnt;
			if (sourceIOnt.isIDisjointClassWithClass(sc1, sc2)) {
				if (targetIOnt.hasICommonSubClass(tc1, tc2)) {
					return true;
				}
			}
			return false;
		}
		// uses standard reasoner methods
		else {
			if (sourceOnt.isDisjointClassWithClass(sc1, sc2)) {
				for (OWLClass subClass : targetOnt.getSubClassesOfClass(tc1)) {
					if (targetOnt.isUnsatisfiable(subClass)) { continue; }
					if (targetOnt.isSubClassOfClass(subClass, tc2)) {
						return true;
					}
				}
				//this has been added, not specified in previous version (what has been a fault, i guess ...)
				for (OWLClass subClass : targetOnt.getSubClassesOfClass(tc2)) {
					if (targetOnt.isUnsatisfiable(subClass)) { continue; }
					if (targetOnt.isSubClassOfClass(subClass, tc1)) {
						return true;
					}
				}	
			}
			return false;
		}
	}
	
	private boolean checkAtomicSubsumptionPatternXXX(OWLClass sc1, OWLClass sc2, OWLClass tc1, OWLClass tc2, LocalOntology sourceOnt, LocalOntology targetOnt) {
		boolean intervalResult = false;
		boolean standardResult = false;
		String differentConcept = null;
		IOntology sourceIOnt = (IOntology)sourceOnt;
		IOntology targetIOnt = (IOntology)targetOnt;
		if (sourceIOnt.isISubClassOfClass(sc1, sc2)) {
			if (targetIOnt.hasICommonSubDisjointClass(tc1, tc2)) {
				intervalResult = true;
			}
			
		}
		if (sourceOnt.isSubClassOfClass(sc1, sc2)) {
			for (OWLClass subClass : targetOnt.getSubClassesOfClass(tc1)) {
				if (targetOnt.isUnsatisfiable(subClass)) { continue; }
				if (targetOnt.isDisjointClassWithClass(subClass, tc2)) { 
					differentConcept = subClass.toString();
					standardResult = true;
				}
			}
		}	
		if (standardResult != intervalResult) {
			System.out.println("checkAtomicSubsumptionPattern ::: STANDARD=" + standardResult + "  INTERVAL=" + intervalResult);
			printDebugInfo(sc1, sc2, tc1, tc2, differentConcept);
			TEST_INTERVALTREE_MODE_DEVIATION = true;
		}
		return intervalResult;
	}
	
	private boolean checkAtomicDisjointnessPatternXXX(OWLClass sc1, OWLClass sc2, OWLClass tc1, OWLClass tc2, LocalOntology sourceOnt, LocalOntology targetOnt) {

		boolean intervalResult = false;
		boolean standardResult = false;
		String differentConcept = null;
		IOntology sourceIOnt = (IOntology)sourceOnt;
		IOntology targetIOnt = (IOntology)targetOnt;
		if (sourceIOnt.isIDisjointClassWithClass(sc1, sc2)) {
			if (targetIOnt.hasICommonSubClass(tc1, tc2)) {
				intervalResult = true;
			}
		}
		if (sourceOnt.isDisjointClassWithClass(sc1, sc2)) {
			for (OWLClass subClass : targetOnt.getSubClassesOfClass(tc1)) {
				if (targetOnt.isUnsatisfiable(subClass)) { continue; }
				if (targetOnt.isSubClassOfClass(subClass, tc2)) {
					standardResult = true;
					differentConcept = subClass.toString();
				}
			}
			//this has been added, not specified in previous version (what has been a fault, i guess ...)
			for (OWLClass subClass : targetOnt.getSubClassesOfClass(tc2)) {
				if (targetOnt.isUnsatisfiable(subClass)) { continue; }
				if (targetOnt.isSubClassOfClass(subClass, tc1)) {
					standardResult = true;
					differentConcept = subClass.toString();
				}
			}	
		}
		if (standardResult != intervalResult) {	
			System.out.println("checkAtomicDisjointnessPattern ::: STANDARD=" + standardResult + "  INTERVAL=" + intervalResult);
			printDebugInfo(sc1, sc2, tc1, tc2, differentConcept);
			TEST_INTERVALTREE_MODE_DEVIATION = true;
			
			
		}
		return intervalResult;
	}	
	
	private void printDebugInfo(OWLClass sc1, OWLClass sc2, OWLClass tc1, OWLClass tc2, String differentConcept) {
		System.out.println("sc1: + " + sc1);
		System.out.println("sc2: + " + sc2);
		System.out.println("tc1: + " + tc1);
		System.out.println("tc2: + " + tc2);
		System.out.println("different: + " + differentConcept);
		// System.exit(1);
	}
	

}
