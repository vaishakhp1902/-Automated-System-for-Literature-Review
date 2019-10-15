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

import org.mindswap.pellet.exceptions.InconsistentOntologyException;

import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.model.*;

import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.exceptions.OntologyException;
import de.unima.alcomox.mapping.Correspondence;
import de.unima.alcomox.mapping.Mapping;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
* This class represents a complete reasoner for conflict detection in mappings.
* It is complete for  correspondences connecting classes with classes and object properties
* with object properties. For data properties what is meant by completeness is explained in some paper.
*/
public class CompleteReasoner extends AlcomoReasoner {

	private MergedOntology mergedOntology;
	private Set<OWLAxiom> attachedValidatedAxioms;
	
	private OWLClass lastUnsatisfiableClass = null;

	/**
	* Constructs an reasoner for complete conflict detection. 
	*  
	* @param sourceOntology Source ontology.
	* @param targetOntology Target ontology.
	* @param extractionProblem The extraction problem that this reasoner is used for.
	* @throws AlcomoException 
	*/	
	public CompleteReasoner(LocalOntology sourceOntology, LocalOntology targetOntology, ExtractionProblem extractionProblem) throws AlcomoException {
		super(sourceOntology, targetOntology, extractionProblem);
		this.attachedValidatedAxioms = new HashSet<OWLAxiom>();
		this.mergedOntology = this.getMergedOntology();
	}
	
	/**
	* A complete method for checking if a set of correspondences induces a conflict.
	* 
	* @param mapping The mapping that is checked for conflicts
	* @return True if the mapping is induces a conflict.
	* @throws AlcomoException Thrown if the merging if the ontologies fails.
	*/
	public boolean isConflictSet(Mapping mapping) throws AlcomoException {
		boolean isConflictSet = false;
		Set<OWLAxiom> mappingAxioms = this.mergedOntology.toAxioms(mapping);
		this.mergedOntology.addAxioms(mappingAxioms);
		try {
			this.mergedOntology.initReasoner();
			Set<OWLClass> mergedUnsatisfiableClasses = this.mergedOntology.getUnsatisfiableClasses();
			Set<OWLClass> sourceUnsatisfiableClasses = this.sourceOntology.getUnsatisfiableClasses();
			Set<OWLClass> targetUnsatisfiableClasses = this.targetOntology.getUnsatisfiableClasses();
			mergedUnsatisfiableClasses.removeAll(sourceUnsatisfiableClasses);
			mergedUnsatisfiableClasses.removeAll(targetUnsatisfiableClasses);
			isConflictSet = mergedUnsatisfiableClasses.size() > 0;
			for (OWLClass c : mergedUnsatisfiableClasses) {
				this.setLastUnsatisfiableClass(c);
				
				break;
			}
		}
		catch(InconsistentOntologyException e) {
			System.out.println("Inconsistent thing " + e);
			isConflictSet = true;
		}
		finally { this.mergedOntology.freeReasoner(); }
		this.mergedOntology.removeAxioms(mappingAxioms);
		return isConflictSet;
	}
	
	public Mapping getConflictSet(Mapping mapping) throws OntologyException {
		Mapping tempMapping = mapping.getCopy();
		if (this.getLastUnsatisfiableClass() == null) {
			throw new OntologyException(
				OntologyException.REASONING_ERROR,
				"can only search for conflict set if previously an unsatisfiable class has been detected"
			);
		}
		for (int i = mapping.size() - 1; i >= 0; i--)  {
			Correspondence c = tempMapping.remove(i);
			// System.out.println(tempMapping);
			if (!(this.isConflict(tempMapping, this.getLastUnsatisfiableClass()))) {
				tempMapping.push(c);
			}
		}
		// System.out.println();
		this.setLastUnsatisfiableClass(null);
		//System.out.println("CONFLICT: " + tempMapping);
		return tempMapping;	
	}
	



	/**
	* A complete method for computing the set of all unsatisfiable classes in the merged ontology..
	*
	* @return The set of all unsatisfiable concepts including those additionally introduced for reasoning about properties.
	*/
	public Set<OWLClass> getUnsatisfiableClasses(Mapping mapping) throws AlcomoException {
		Set<OWLClass> unsatisfiableClasses = null;
		Set<OWLAxiom> mappingAxioms = this.mergedOntology.toAxioms(mapping);
		this.mergedOntology.addAxioms(mappingAxioms);
		try {
			this.mergedOntology.initReasoner();
			unsatisfiableClasses = this.mergedOntology.getUnsatisfiableClasses();
		}
		finally {
			this.mergedOntology.freeReasoner();
		}
		this.mergedOntology.removeAxioms(mappingAxioms);
		return unsatisfiableClasses;
	}
	

	
	
	/**
	* Searches binary and returns a correspondence being part of a conflict. The following applies for
	* this method (the order of the mapping is referred to as >). If c is removed from the mapping,
	* the sub set {c' in m | c' < c} of the mapping contains no conflict.
	* 
	* @param mapping The mapping where the correspondence is searched for.
	* @return The correspondence cusing the conflict or null, if there exists no conflict in the mapping. 
	* @throws AlcomoException   
	*/
	public Correspondence searchInvalidCorrespondence(Mapping mapping) throws AlcomoException {
		// an empty mapping cannot contain a conflict
		
		if (mapping.size() == 0) { return null; }
		// first check one time if there is a conflict
		// DataStorage.mirror.count();
		if (!(this.isConflictSet(mapping))) {
			
			return null;
		}
		// mapping size > 0 and there is at least one conflict, go get it!
		else {
			int previous;
			for (previous = 1; previous < mapping.size(); previous *= 2) { }
			int current = previous / 2;
			return this.searchBinaryInvalidCorrespondence(mapping, previous, current);
		}
	}

	/**
	* Adds a validated mapping that will be used in reasoning in a fixed way. That means that it has an
	* influence on the results, but is not considered as something that might contain defective correspondences.
	* This method works incremental, which means that multiple calls will be incremental with
	* respect to previous calls of this method.
	* 
	* @param mapping The mapping that will be attached.
	* @throws OntologyException 
	*/
	public void attachValidatedMapping(Mapping mapping) throws OntologyException {
		Set<OWLAxiom> validatedAxioms = this.mergedOntology.toAxioms(mapping);
		this.mergedOntology.addAxioms(validatedAxioms);
		this.attachedValidatedAxioms.addAll(validatedAxioms);
	}
	
	/**
	* Removes an (incrementally) attached validated mapping.
	* 
	* @throws OntologyException 
	*/
	public void resetValidatedMapping() throws OntologyException {
		this.mergedOntology.removeAxioms(this.attachedValidatedAxioms);
	}	
	
	
	// **********************************
	//     private playground
	// **********************************
	
	private Correspondence searchBinaryInvalidCorrespondence(Mapping mapping, int previous, int current) throws AlcomoException {
		int distance;
		while (true) {
			distance = Math.abs(previous - current);
			if (distance > 1) {
				// DataStorage.mirror.count();
				if (this.isConflictSet(mapping.subMapping(current))) {
					previous = current;
					current -= (distance / 2);
				}
				else {
					previous = current;
					current += (distance / 2);
				}
			}
			else { break; }
		}
		if (this.isConflictSet(mapping.subMapping(current))) {
			if (current == 1) {
				// DataStorage.mirror.count();
				if (this.isConflictSet(mapping.subMapping(0))) { return mapping.get(0); }
				else { return mapping.get(1); }
			}
			else { return mapping.get(current); }
		}
		else { return mapping.get(current + 1); }
	}	
	
	
	public MergedOntology getMergedOntology() throws AlcomoException {
		OWLOntologyManager mergedManager = OWLManager.createOWLOntologyManager();
		URI ontologyURI = URI.create(ExtractionProblem.ALCOMO_URL + "-merged.owl");
		try {
	    	IRI ontologyIRI = IRI.create(ontologyURI);
			OWLOntology ontology = mergedManager.createOntology(ontologyIRI);
			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
			axioms.addAll(sourceOntology.getAxioms());
			axioms.addAll(targetOntology.getAxioms());
			for (OWLAxiom axiom : axioms) {
				AddAxiom addAxiom = new AddAxiom(ontology, axiom);
				mergedManager.applyChange(addAxiom);
			}
			MergedOntology mergedOntology = new MergedOntology(ontology, mergedManager);
			// ??? what was this about ....
			// mergedOntology.setClassUris(this.sourceOntology.getReferencedClassUris(), this.targetOntology.getReferencedClassUris());
			mergedOntology.setExtractionProblem(this.extractionProblem);
			return mergedOntology;

		}
		catch (OWLOntologyCreationException e) {
			throw new OntologyException(
				OntologyException.MODIFICATION_FAILED,
				"could not created merged ontology with uri " + ontologyURI
			);
		}
		catch (OWLOntologyChangeException e) {
			throw new OntologyException(
				OntologyException.MODIFICATION_FAILED,
				"could not merge axioms of merged ontology with uri " + ontologyURI
			);
		}	
	}
	
	private boolean isConflict(Mapping mapping, OWLClass lastUnsatisfiableClass) throws OntologyException {
		boolean isConflictSet = false;
		Set<OWLAxiom> mappingAxioms = this.mergedOntology.toAxioms(mapping);
		this.mergedOntology.addAxioms(mappingAxioms);
		try {
			this.mergedOntology.initReasonerWithoutClassification();
			if (this.mergedOntology.isUnsatisfiable(lastUnsatisfiableClass)) {
				isConflictSet = true;
			}
		}
		catch(InconsistentOntologyException e) {
			isConflictSet = true;
		}
		finally { this.mergedOntology.freeReasoner(); }
		this.mergedOntology.removeAxioms(mappingAxioms);
		return isConflictSet;
	}

	private void setLastUnsatisfiableClass(OWLClass c) {
		this.lastUnsatisfiableClass = c;
	}

	private OWLClass getLastUnsatisfiableClass() {
		return this.lastUnsatisfiableClass;
	}	

	

}
