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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.*;


import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.exceptions.OntologyException;
import de.unima.alcomox.mapping.Correspondence;
import de.unima.alcomox.mapping.Mapping;
import de.unima.alcomox.mapping.SemanticRelation;


/**
* This class represents an e(X)tendable ontology.
*
*/
public class MergedOntology extends AlcomoOntology {
	
	private HashMap<String, OWLClass> uriToClass;	
	

	
	public MergedOntology(OWLOntology owlOntology, OWLOntologyManager owlManager) throws AlcomoException {
		super(owlOntology, owlManager);

		
	}
	
	// only used for disjointness stuff
	public void createUriClassReferences() {
		Set<OWLClass> referencedClasses =  this.ontology.getClassesInSignature();
		this.uriToClass = new HashMap<String, OWLClass>();		
		for (OWLClass someClass : referencedClasses) {
			if ((!(someClass.getIRI().toURI().equals(THING.getIRI().toURI()))) && (!(someClass.getIRI().toURI().equals(NOTHING.getIRI().toURI())))) {
				this.uriToClass.put(someClass.getIRI().toURI().toString(), someClass);
			}
		}
	}

	public void addAxioms(Set<OWLAxiom> mappingAxioms) throws OntologyException {
		AddAxiom addAxiom;
		for (OWLAxiom axiom : mappingAxioms) {
			try {
				addAxiom = new AddAxiom(this.ontology, axiom);
				this.manager.applyChange(addAxiom);
			}
			catch (OWLOntologyChangeException e) {
				throw new OntologyException(
						OntologyException.MODIFICATION_FAILED,
						"could not add axiom " + axiom
				);
			}
		}
	}
	
	public void removeAxioms(Set<OWLAxiom> mappingAxioms) throws OntologyException {
		// System.out.println("*** No Of Axioms before remove: " + this.getAxioms().size());
		RemoveAxiom removeAxiom;
		for (OWLAxiom axiom : mappingAxioms) {
			try {
				removeAxiom = new RemoveAxiom(this.ontology, axiom);
				this.manager.applyChange(removeAxiom);	
			}
			catch (OWLOntologyChangeException e) {
				throw new OntologyException(
						OntologyException.MODIFICATION_FAILED,
						"could not remove axiom " + axiom
				);
			}
		}
	}	
	
	public Set<OWLAxiom> toAxioms(Mapping mapping) throws OntologyException {
		HashSet<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for (Correspondence c : mapping) {
			// System.out.println(c);
			// concept to concept
			if (c.getSourceEntity().isConcept() && c.getTargetEntity().isConcept()) {
				if (c.getRelation().getType() == SemanticRelation.SUB || c.getRelation().getType() == SemanticRelation.EQUIV) {
			
					axioms.add(this.factory.getOWLSubClassOfAxiom(c.getSourceEntity().getConcept(), c.getTargetEntity().getConcept()));
				}
				if (c.getRelation().getType() == SemanticRelation.SUPER || c.getRelation().getType() == SemanticRelation.EQUIV) {
					axioms.add(this.factory.getOWLSubClassOfAxiom(c.getTargetEntity().getConcept(), c.getSourceEntity().getConcept()));
				}	
			}

			
			// object-property to object-property
			else if (c.getSourceEntity().isObjectProperty() && c.getTargetEntity().isObjectProperty()) {
				if (c.getRelation().getType() == SemanticRelation.SUB || c.getRelation().getType() == SemanticRelation.EQUIV) {
					axioms.add(this.factory.getOWLSubObjectPropertyOfAxiom(c.getSourceEntity().getObjectProperty(), c.getTargetEntity().getObjectProperty()));
					// System.out.println("yyy " + this.factory.getOWLSubObjectPropertyAxiom(c.getSourceEntity().getObjectProperty(), c.getTargetEntity().getObjectProperty()));
				}
				if (c.getRelation().getType() == SemanticRelation.SUPER || c.getRelation().getType() == SemanticRelation.EQUIV) {
					axioms.add(this.factory.getOWLSubObjectPropertyOfAxiom(c.getTargetEntity().getObjectProperty(), c.getSourceEntity().getObjectProperty()));
				}		
			}
			// only for test purpose
			/*
			else if (c.getSourceEntity().isDataProperty() && c.getTargetEntity().isDataProperty()) {
				if (c.getRelation().getType() == SemanticRelation.SUB || c.getRelation().getType() == SemanticRelation.EQUIV) {
					axioms.add(this.factory.getOWLSubDataPropertyAxiom(c.getSourceEntity().getDataProperty(), c.getTargetEntity().getDataProperty()));
				}
				if (c.getRelation().getType() == SemanticRelation.SUPER || c.getRelation().getType() == SemanticRelation.EQUIV) {
					axioms.add(this.factory.getOWLSubDataPropertyAxiom(c.getTargetEntity().getDataProperty(), c.getSourceEntity().getDataProperty()));
				}		
			}
			*/
			// whenever a datatypeproperty is used in a correspondence, do only apply the relation to the domain
			// this approach enables to deal with disjoint datatypes (even though this kind of disjointness might not be supported
			// by a reasoner
			else if (c.getSourceEntity().isDataProperty() || c.getTargetEntity().isDataProperty()) {
				if (c.getRelation().getType() == SemanticRelation.SUB || c.getRelation().getType() == SemanticRelation.EQUIV) {
					axioms.add(this.factory.getOWLSubClassOfAxiom(c.getSourceEntity().getDomain(), c.getTargetEntity().getDomain()));
				}				
				if (c.getRelation().getType() == SemanticRelation.SUPER || c.getRelation().getType() == SemanticRelation.EQUIV) {
					axioms.add(this.factory.getOWLSubClassOfAxiom(c.getTargetEntity().getDomain(), c.getSourceEntity().getDomain()));
				}		
			}
			
			
		}
		return axioms;	
	}
	
	public void write(String filepath) throws AlcomoException {
		this.write(filepath, this.extractionProblem.getReferingInputMapping());
		
	}
	
	private void write(String filepath, Mapping m) throws AlcomoException {
		Set<OWLAxiom> mAxioms = this.toAxioms(m);
		this.addAxioms(mAxioms);
		super.write(filepath);
		this.removeAxioms(mAxioms);
	}
	
	/**
	* Dont you ever use this, very bad hack for generating merged ontology without helper classes.
	*  
	* @param mo
	* @param filepath
	* @param mapping
	 * @throws URISyntaxException 
	*/
	public void writeMergedOntology(String filepath, Mapping mapping) throws URISyntaxException {
		
		HashSet<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for (Correspondence c : mapping) {
			System.out.println(c);
			// concept to concept
			if (this.ontology.containsClassInSignature(IRI.create(c.getSourceEntityUri()))) {
				OWLClass c1 = this.factory.getOWLClass(IRI.create(c.getSourceEntityUri()));
				OWLClass c2 = this.factory.getOWLClass(IRI.create(c.getTargetEntityUri()));
				if (c.getRelation().getType() == SemanticRelation.SUB || c.getRelation().getType() == SemanticRelation.EQUIV) {

					axioms.add(this.factory.getOWLSubClassOfAxiom(c1, c2));
				}
				if (c.getRelation().getType() == SemanticRelation.SUPER || c.getRelation().getType() == SemanticRelation.EQUIV) {
					axioms.add(this.factory.getOWLSubClassOfAxiom(c2, c1));
				}	
			}
			// object-property to object-property
			else if (this.ontology.containsObjectPropertyInSignature(IRI.create(c.getSourceEntityUri()))) {
				OWLObjectProperty p1 = this.factory.getOWLObjectProperty(IRI.create(c.getSourceEntityUri()));
				OWLObjectProperty p2 = this.factory.getOWLObjectProperty(IRI.create(c.getTargetEntityUri()));
				if (c.getRelation().getType() == SemanticRelation.SUB || c.getRelation().getType() == SemanticRelation.EQUIV) {
					axioms.add(this.factory.getOWLSubObjectPropertyOfAxiom(p1, p2));
				}
				if (c.getRelation().getType() == SemanticRelation.SUPER || c.getRelation().getType() == SemanticRelation.EQUIV) {
					axioms.add(this.factory.getOWLSubObjectPropertyOfAxiom(p2, p1));
				}		
			}

			else if (this.ontology.containsDataPropertyInSignature(IRI.create(c.getSourceEntityUri()))) {
				OWLDataProperty p1 = this.factory.getOWLDataProperty(IRI.create(c.getSourceEntityUri()));
				OWLDataProperty p2 = this.factory.getOWLDataProperty(IRI.create(c.getTargetEntityUri()));
				if (c.getRelation().getType() == SemanticRelation.SUB || c.getRelation().getType() == SemanticRelation.EQUIV) {
					axioms.add(this.factory.getOWLSubDataPropertyOfAxiom(p1, p2));
				}
				if (c.getRelation().getType() == SemanticRelation.SUPER || c.getRelation().getType() == SemanticRelation.EQUIV) {
					axioms.add(this.factory.getOWLSubDataPropertyOfAxiom(p2, p1));
				}		
			}	
		}
			//return axioms;	
		for (OWLAxiom axiom : axioms) {
			AddAxiom addAxiom;
			try {
				addAxiom = new AddAxiom(this.ontology, axiom);
				this.manager.applyChange(addAxiom);	
			}
			catch (OWLOntologyChangeException e) {
				System.out.println("die young");
				System.exit(1);
			}
		}

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
			System.out.println("die young");
			System.exit(1);
		}

		
	}

}
