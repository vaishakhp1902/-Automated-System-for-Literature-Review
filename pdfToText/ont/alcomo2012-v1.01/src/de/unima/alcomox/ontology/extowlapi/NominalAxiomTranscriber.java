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

package de.unima.alcomox.ontology.extowlapi;


import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;


import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.exceptions.OntologyException;
import de.unima.alcomox.ontology.LocalOntology;

public class NominalAxiomTranscriber extends OWLEntityCollector {

	
	public NominalAxiomTranscriber(Set<OWLEntity> toReturn) {
		super(toReturn);
		// TODO Auto-generated constructor stub
	}

	private OWLDataFactory factory;
	private LocalOntology ontology;
	private OWLOntology ontologyCore;
	
	
	
	private ArrayList<OWLAxiom> rebuiltAxioms;
	private ArrayList<OWLObject> rebuiltObjects;
	

	
	public void setLocalOntology(LocalOntology ontology) {
		this.ontology = ontology;	
	}
	
	public void setOntologyCore(OWLOntology ontology) {
		this.ontologyCore = ontology;	
	}
	
	public void setFactory(OWLDataFactory factory) {
		this.factory = factory;
	}
	

	

	// *************************************************
	// *** visiting axioms potentially with nominals ***
	// *************************************************

	public ArrayList<OWLAxiom> rebuild(OWLAxiom axiom) throws OntologyException {
		this.rebuiltAxioms = new ArrayList<OWLAxiom>();
		this.rebuiltObjects = new ArrayList<OWLObject>();
		if (axiom instanceof OWLSubClassOfAxiom) {
			this.rebuild((OWLSubClassOfAxiom)axiom);
		}
		else if (axiom instanceof OWLEquivalentClassesAxiom) {
			this.rebuild((OWLEquivalentClassesAxiom)axiom);
		}
		else if (axiom instanceof OWLDisjointUnionAxiom) {
			this.rebuild((OWLDisjointUnionAxiom)axiom);
		}
		else if (axiom instanceof OWLDisjointClassesAxiom) {
			this.rebuild((OWLDisjointClassesAxiom)axiom);
		}
		else if (axiom instanceof OWLDataPropertyDomainAxiom) {
			this.rebuild((OWLDataPropertyDomainAxiom)axiom);
		}
		else if (axiom instanceof OWLDataPropertyRangeAxiom) {
			this.rebuild((OWLDataPropertyRangeAxiom)axiom);
		}
		else if (axiom instanceof OWLObjectPropertyDomainAxiom) {
			this.rebuild((OWLObjectPropertyDomainAxiom)axiom);
		}
		else if (axiom instanceof OWLObjectPropertyRangeAxiom) {
			this.rebuild((OWLObjectPropertyRangeAxiom)axiom);
		}	
		else {
			throw new OntologyException(
				OntologyException.MODIFICATION_FAILED,
				"could not rewrite ontology to get rid of nominals (" + axiom + " is " + axiom.getClass() +")"
			);
		}
		return this.rebuiltAxioms;
	}
	

    public void rebuild(OWLSubClassOfAxiom axiom) {   	
    	axiom.getSubClass().accept(this);
    	OWLClassExpression subClass = this.getRelevantDescription();
        axiom.getSuperClass().accept(this);
        OWLClassExpression superClass = this.getRelevantDescription();
        this.rebuiltAxioms.add(this.factory.getOWLSubClassOfAxiom(subClass, superClass));
    }

    public void rebuild(OWLEquivalentClassesAxiom axiom) {
        for (OWLClassExpression desc : axiom.getClassExpressions()) {
            desc.accept(this);
        }
        Set<OWLClassExpression> equivClasses = this.getRelevantDescriptionsAsSet(0);
        this.rebuiltAxioms.add(this.factory.getOWLEquivalentClassesAxiom(equivClasses));
    }
    

    public void rebuild(OWLDisjointUnionAxiom axiom) {
        axiom.getOWLClass().accept(this);
        OWLClass equivClass = (OWLClass)this.getRelevantDescription();
        for (OWLClassExpression desc : axiom.getClassExpressions()) {
            desc.accept(this);
        }
        Set<OWLClassExpression> disjointUnionClasses = this.getRelevantDescriptionsAsSet(0);
        this.rebuiltAxioms.add(this.factory.getOWLDisjointUnionAxiom(equivClass, disjointUnionClasses));
    }
	 
    public void rebuild(OWLDisjointClassesAxiom axiom) {
        for (OWLClassExpression desc : axiom.getClassExpressions()) {
            desc.accept(this);
        }
        Set<OWLClassExpression> disjointClasses = this.getRelevantDescriptionsAsSet(0);
        this.rebuiltAxioms.add(this.factory.getOWLDisjointClassesAxiom(disjointClasses));     
    }

    public void rebuild(OWLDataPropertyDomainAxiom axiom) {
        axiom.getDomain().accept(this);
        OWLClassExpression domainClass = this.getRelevantDescription();
        this.rebuiltAxioms.add(this.factory.getOWLDataPropertyDomainAxiom(axiom.getProperty(), domainClass));
    }

    public void rebuild(OWLObjectPropertyDomainAxiom axiom) {
        axiom.getDomain().accept(this);
        OWLClassExpression domainClass = this.getRelevantDescription();
        this.rebuiltAxioms.add(this.factory.getOWLObjectPropertyDomainAxiom(axiom.getProperty(), domainClass));
    }

    public void rebuild(OWLObjectPropertyRangeAxiom axiom) {
        axiom.getRange().accept(this);
        OWLClassExpression rangeClass = this.getRelevantDescription();
        this.rebuiltAxioms.add(this.factory.getOWLObjectPropertyRangeAxiom(axiom.getProperty(), rangeClass));
        
    }
    
	// *************************************
	// *** visiting complex descriptions ***
	// *************************************
    
	// DONE
    public void visit(OWLObjectIntersectionOf desc) {
    	int reduceTo = this.rebuiltObjects.size();
    	for (OWLClassExpression operand : desc.getOperands()) {
            operand.accept(this);
        }
    	HashSet<OWLClassExpression> ds = this.getRelevantDescriptionsAsSet(reduceTo);
    	this.rebuiltObjects.add(this.factory.getOWLObjectIntersectionOf(ds));
    }

    // DONE
    public void visit(OWLObjectUnionOf desc) {
    	int reduceTo = this.rebuiltObjects.size();
    	for (OWLClassExpression operand : desc.getOperands()) {
            operand.accept(this);
        }
    	HashSet<OWLClassExpression> ds = this.getRelevantDescriptionsAsSet(reduceTo);
    	this.rebuiltObjects.add(this.factory.getOWLObjectUnionOf(ds));
    }

    // DONE
    public void visit(OWLObjectComplementOf desc) {
        desc.getOperand().accept(this);
        OWLClassExpression d = this.getRelevantDescription();
        this.rebuiltObjects.add(this.factory.getOWLObjectComplementOf(d));
    }

    // DONE
    public void visit(OWLObjectSomeValuesFrom desc) {
        desc.getProperty().accept(this);
        desc.getFiller().accept(this);
        OWLClassExpression d = this.getRelevantDescription();
        this.rebuiltObjects.add(this.factory.getOWLObjectSomeValuesFrom(desc.getProperty(), d));
    }

    // DONE
    public void visit(OWLObjectAllValuesFrom desc) {
        // desc.getProperty().accept(this);
        desc.getFiller().accept(this);
        OWLClassExpression d = this.getRelevantDescription();
        this.rebuiltObjects.add(this.factory.getOWLObjectAllValuesFrom(desc.getProperty(), d));
    }

    // DONE
    public void visit(OWLObjectMinCardinality desc) {
        // desc.getProperty().accept(this);
        desc.getFiller().accept(this);
        OWLClassExpression d = this.getRelevantDescription();
        this.rebuiltObjects.add(this.factory.getOWLObjectMinCardinality(desc.getCardinality(), desc.getProperty(), d));
    }

    // DONE
    public void visit(OWLObjectExactCardinality desc) {
	   // desc.getProperty().accept(this);
	   desc.getFiller().accept(this);
	   OWLClassExpression d = this.getRelevantDescription();
	   this.rebuiltObjects.add(this.factory.getOWLObjectExactCardinality(desc.getCardinality(), desc.getProperty(), d));
    }

    // DONE
    public void visit(OWLObjectMaxCardinality desc) {
        // desc.getProperty().accept(this);
        desc.getFiller().accept(this);
        OWLClassExpression d = this.getRelevantDescription();
        this.rebuiltObjects.add(this.factory.getOWLObjectMaxCardinality(desc.getCardinality(), desc.getProperty(), d));
    }
    

    
    // continu here, rething the outcommented stuff below
    // surround with if or better make two extension classes (to avoid to much if switches
    

    /* --- all this seems not to be relevant for the nominal case ---

    public void visit(OWLDataSomeRestriction desc) {
        desc.getProperty().accept(this);
        desc.getFiller().accept(this);
    }

    public void visit(OWLDataAllRestriction desc) {
        desc.getProperty().accept(this);
        desc.getFiller().accept(this);
    }

    public void visit(OWLDataValueRestriction desc) {
        desc.getProperty().accept(this);
        desc.getValue().accept(this);
    }


    public void visit(OWLDataMinCardinalityRestriction desc) {
        desc.getProperty().accept(this);
        desc.getFiller().accept(this);
    }

    public void visit(OWLDataExactCardinalityRestriction desc) {
        desc.getProperty().accept(this);
        desc.getFiller().accept(this);
    }

    public void visit(OWLDataMaxCardinalityRestriction desc) {
        desc.getProperty().accept(this);
        desc.getFiller().accept(this);
    }
    */
    
	// ***********************************
	// *** visiting named descriptions ***
	// ***********************************   

    // DONE - very important one, forgot this in first version
    public void visit(OWLClass c) {
    	this.rebuiltObjects.add(c);
    	
    }
    
	// ******************************************
	// *** visiting and transcribing nominals ***
	// ******************************************   
	
    public void visit(OWLObjectOneOf desc) {
		URI nominalClassURI = URI.create(ExtractionProblem.ALCOMO_URL + "#OneOfSubstitute_" + this.ontology.getNextExtId());
		OWLClass nominalClass = this.factory.getOWLClass(IRI.create(nominalClassURI));
		this.addSubClassAxiomForNominalSubstitute(desc.getIndividuals(), nominalClass);
		this.rebuiltObjects.add(nominalClass);
	}

    public void visit(OWLObjectHasValue desc) {
		URI nominalClassURI = URI.create(ExtractionProblem.ALCOMO_URL + "#ValueRestrictionSubstitute_" + this.ontology.getNextExtId());
		OWLClass nominalClass = this.factory.getOWLClass(IRI.create(nominalClassURI));
		OWLObjectSomeValuesFrom someRestriction = this.factory.getOWLObjectSomeValuesFrom(desc.getProperty(), nominalClass);
		HashSet<OWLIndividual> individuals = new HashSet<OWLIndividual>();
		individuals.add(desc.getValue());
		this.addSubClassAxiomForNominalSubstitute(individuals, nominalClass);
		this.rebuiltObjects.add(someRestriction);
	}
    
    // kind of nominal: EX loves.Self (self restriction, defines the class of those people loving theirselves)
    // seems to be a kind of special case of a ValueRestriction
    // TODO (???) add subclass statements (just add the domain ... maybe, seems to be not part of OWL 1.0)
    public void visit(OWLObjectHasSelf desc) {
    	URI nominalClassURI = URI.create(ExtractionProblem.ALCOMO_URL + "#ValueRestrictionSubstitute_" + this.ontology.getNextExtId());
    	OWLClass nominalClass = this.factory.getOWLClass(IRI.create(nominalClassURI));
    	OWLObjectSomeValuesFrom someRestriction = this.factory.getOWLObjectSomeValuesFrom(desc.getProperty(), nominalClass);
    	this.rebuiltObjects.add(someRestriction);
    }
	
	// **********************
	// *** hepler methods ***
	// ********************** 

	private void addSubClassAxiomForNominalSubstitute(Set<OWLIndividual> individuals, OWLClass nominalClass) {
		Set<OWLClassExpression> typesOfIndividuals = new HashSet<OWLClassExpression>();
    	for (OWLIndividual ind : individuals) {
    		typesOfIndividuals.addAll(ind.getTypes(this.ontologyCore));
    	}
    	if (typesOfIndividuals.size() == 1) {
    		OWLClassExpression typeOfIndividual = null;
    		for (OWLClassExpression typeOfInd : typesOfIndividuals) { typeOfIndividual = typeOfInd; }
			OWLSubClassOfAxiom subClassAxiom = this.factory.getOWLSubClassOfAxiom(nominalClass, typeOfIndividual);
			this.rebuiltAxioms.add(subClassAxiom);
    	}
    	if (typesOfIndividuals.size() > 1) {
			OWLObjectUnionOf unionClass = this.factory.getOWLObjectUnionOf(typesOfIndividuals);
			OWLSubClassOfAxiom subClassAxiom = this.factory.getOWLSubClassOfAxiom(nominalClass, unionClass);
			this.rebuiltAxioms.add(subClassAxiom);
    	}
   
	}
	
	private HashSet<OWLClassExpression>  getRelevantDescriptionsAsSet(int reduceTo) {
		HashSet<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
		while (this.rebuiltObjects.size() > reduceTo) {
			descs.add((OWLClassExpression)this.rebuiltObjects.remove(this.rebuiltObjects.size() - 1));
		}
		return descs;
	}
	
	private OWLClassExpression getRelevantDescription() {
		return (OWLClassExpression)this.rebuiltObjects.remove(this.rebuiltObjects.size() - 1);
	}	
	

}
