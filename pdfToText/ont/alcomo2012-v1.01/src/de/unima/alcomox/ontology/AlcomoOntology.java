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


import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.Configuration;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.mindswap.pellet.exceptions.InternalReasonerException;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;





import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.Settings;
import de.unima.alcomox.Settings.BlackBoxReasoner;
import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.exceptions.OntologyException;
import de.unima.alcomox.util.AlcomoLogger;

/**
* This abstract class represents an ontology used in the alcomo system.
* It can be seen
*
*/
public abstract class AlcomoOntology {
	
	public OWLClass THING = null;
	public OWLClass NOTHING = null;
	
	// random world

	
	protected ExtractionProblem extractionProblem;
	
	protected OWLOntology ontology;
	protected OWLOntologyManager manager;
	protected OWLDataFactory factory;
	
	protected HashMap<String, Entity> entities;
	
	protected ArrayList<String> indexedConceptEntityUris;
	protected ArrayList<String> indexedPropertyEntityUris;
	
	protected AlcomoLogger log;
	
	protected OWLReasoner reasoner;
	
	public AlcomoOntology(OWLOntology owlOntology, OWLOntologyManager owlManager) throws AlcomoException {
		this.extractionProblem = null;
		this.ontology = owlOntology;
		this.manager = owlManager;
		this.setFactory();
		this.log = new AlcomoLogger(this.getClass());
	}
	


	public AlcomoOntology() {
		this.extractionProblem = null;
		this.log = new AlcomoLogger(this.getClass());
	}
	
	protected void setFactory() {
		this.factory = this.manager.getOWLDataFactory();
		this.NOTHING = factory.getOWLNothing();
		this.THING = factory.getOWLThing();	
	}



	/**
	* Returns a string representation of this ontology. Contains
	* URI of ontology and some information e.g. number of classes and so on.
	* 
	* @return String representation of this ontology.
	*/
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ID ONTOLOGY: " + this.ontology.toString() + "\n");
		sb.append("NUMBER OF CLASSES: " + this.ontology.getClassesInSignature().size() + "\n");
		sb.append("NUMBER OF DATA PROPERTIES: " + this.ontology.getDataPropertiesInSignature().size() + "\n");
		sb.append("NUMBER OF OBJECT PROPERTIES: " + this.ontology.getObjectPropertiesInSignature().size() + "\n");
		return sb.toString();
	}
	
	/**
	* Returns a short string representation of this ontology.
	* 
	* @return String representation of this ontology.
	*/	
	public String toShortString() {
		return this.ontology.toString();
	}	
	
	public Set<OWLAxiom> getAxioms() {
		return this.ontology.getAxioms();
	}	
	


	public void initReasoner() throws OntologyException {
		this.initReasonerWithoutClassification();
		if (Settings.PRE_CLASSIFY) {
			try {
				reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY); 
			}
			catch(InconsistentOntologyException e) {
				// TODO (HIGH priority) its pretty unclear why this exception is thrown this sometimes happens
				// this bug has high priority
				throw new OntologyException(
					OntologyException.REASONING_ERROR,
					"Ontology " + this.toShortString() + " is inconsistent, cannot do any reasoning\n" +
					"Check in settings wether individuals will be removed prior reasoning!",
					e
				);
				
				
			}
		}
	}
	
	
	public void initReasonerWithoutClassification() throws OntologyException {
		if (Settings.BLACKBOX_REASONER == BlackBoxReasoner.PELLET) {
			PelletReasonerFactory reasonerFactory =  new PelletReasonerFactory();
			this.reasoner = reasonerFactory.createReasoner(ontology);
		}
		else if (Settings.BLACKBOX_REASONER == BlackBoxReasoner.HERMIT){
			// this.reasoner = new Reasoner(ontology);
			// ReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
			Configuration config = new Configuration();
			config.ignoreUnsupportedDatatypes = true;
			this.reasoner = new Reasoner(config, ontology);
			// this.reasoner = reasonerFactory.createReasoner(ontology, config);
			
		}
		else {
			throw new OntologyException(
					OntologyException.REASONING_ERROR,
					"could not init reasoner, reasoner not specified / not available"
			);
		}
	}	
	
	public void freeReasoner() {
		this.reasoner = null;
	}
	
	public ExtractionProblem getExtractionProblem() {
		return this.extractionProblem;
	}

	public void setExtractionProblem(ExtractionProblem extractionProblem) {
		this.extractionProblem = extractionProblem;
	}
	
	

	/*
	public String[] getUnsatisfiableClasses() {
		Set<OWLClass> inconsistentClasses = this.reasoner.getInconsistentClasses();
		String[] unsatisfiableClasses = null;
		if (inconsistentClasses.size() > 0) {
			unsatisfiableClasses = new String[inconsistentClasses.size()];
			int index = 0;
			for (OWLClass inconsistentClass : inconsistentClasses) {
				unsatisfiableClasses[index] =  inconsistentClass.toString();
				index++;
			}			
		}
		else {
			unsatisfiableClasses = new String[]{};
		}
		return unsatisfiableClasses;
	}
	*/	
	
	// basic Reasoning methods, HAVE BEEN MOEVED FROM THE XONTOLOGY
	
	// ********************************
	// *** REASONER WRAPPER METHODS ***
	// ********************************
	
	/**
	* Checks wether first class is subclass of second class. Equivalent classes arse also in subclass relation.
	* 
	* @param c1 First class.
	* @param c2 Second class.
	* @return 
	*/
	public boolean isSubClassOfClass(OWLClass c1, OWLClass c2) {
		OWLAxiom subClassOfAxiom  = factory.getOWLSubClassOfAxiom(c1, c2);
		OWLAxiom equivClassOfAxiom  = factory.getOWLEquivalentClassesAxiom(c1, c2);
		if (this.reasoner.isEntailed(equivClassOfAxiom)) {
			return true;
		}
		return this.reasoner.isEntailed(subClassOfAxiom);
	}
	
	/**
	* Returns the set of all classes that are subclasses of the specified class as well as the class itself.
	* The class nothing is not included in the result.
	* 
	* @param c A class.
	* @return The set of subclasses.
	*/
	public Set<OWLClass> getSubClassesOfClass(OWLClass c) {
		NodeSet<OWLClass> subClsSets = this.reasoner.getSubClasses(c, false);
		Set<OWLClass> subCls = subClsSets.getFlattened();
		subCls.add(c);
		subCls.remove(this.NOTHING);
		return subCls;
	}
	
	/**
	* Returns the set of all classes that direct are superclasses of the specified class
	* The class itself is excluded. The class top is not included in the result.
	* 
	* @param c A class.
	* @return The set of real superclasses.
	*/
	public Set<OWLClass> getRealDirectSuperClassesOfClass(OWLClass c) {
		NodeSet<OWLClass> superClsSets = this.reasoner.getSuperClasses(c, false);
		Set<OWLClass> superCls = superClsSets.getFlattened();
		superCls.remove(this.THING);
		return superCls;
	}	

	/**
	* Checks wether two classes are disjoint.
	* 
	* @param c1 A class.
	* @param c2 Another class.
	* @return True if both classes are disjoint, false otherwise.
	*/
	public boolean isDisjointClassWithClass(OWLClass c1, OWLClass c2) {	
		// it seems to be a bug in Pellet that the same class is disjoint with itself
		// for that reason we check it manually
		if (c1.equals(c2)) return false;
		OWLAxiom disAxiom  = factory.getOWLDisjointClassesAxiom(c1, c2);

		return this.reasoner.isEntailed(disAxiom);
	}
	
	/**
	* Returns all unsatisfiable classes in this ontology.
	* 
	* @return The set of all unsatisfiable classes (except NOTHING).
	*/
	public Set<OWLClass> getUnsatisfiableClasses() {
		// TODO maby the bug is here ... (maybe not)
		Set<OWLClass> usatClasses = new HashSet<OWLClass>();
		try {
			Node<OWLClass> usatClassesSet = this.reasoner.getBottomClassNode();
			usatClasses = usatClassesSet.getEntities();
		}
		catch(InternalReasonerException e) {
			System.err.println("WARNING: Pellet throws some strange exception !!!!");
			System.err.println("Algorithm continues as if all classes are satisfiable");
		}
		
		usatClasses.remove(NOTHING);
		return usatClasses;
	}

	/**
	* Returns alls classes in this ontology.
	* 
	* @return The set of all classes (except THING and NOTHING).
	*/
	public Set<OWLClass> getClasses() {
		// this.reasoner.get
		Set<OWLClass> classes = this.ontology.getClassesInSignature();
		classes.remove(NOTHING);
		classes.remove(THING);
		return classes;
	}
	
	
	/**
	* Checks the unsatisfiability of a class.
	* 
	* @param c The class that has to be checked for unsatisfiability.
	* @return True if c is unsatisfiable, false otherwise.
	*/
	public boolean isUnsatisfiable(OWLClass c) {
		return (!(this.reasoner.isSatisfiable(c)));
	}	
	
	/**
	* Writes the ontology into a file.
	* 
	* @param filepath Destination path
	* @throws OntologyException Thrwon if file could not be written.
	* @throws AlcomoException 
	*/
	public void write(String filepath) throws AlcomoException {
		try {
	    	File ontologyFile = new File(filepath);
			if (!(ontologyFile.exists())) {
				ontologyFile.createNewFile();
			}
	    	URI physicalURI = URI.create(ontologyFile.toURI().toString());
	    	IRI physicalIRI = IRI.create(physicalURI);
	    	this.manager.saveOntology(this.ontology, physicalIRI);
		}
		catch (Exception e) {
			throw new OntologyException(
					OntologyException.IO_ERROR, 
					"could not write ontology to file at " + filepath + "", 
					e
			);
		}
	}
	
	

	
	


}
