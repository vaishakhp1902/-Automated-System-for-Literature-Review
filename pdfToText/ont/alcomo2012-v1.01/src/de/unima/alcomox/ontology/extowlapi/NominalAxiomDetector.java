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

import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.util.OWLEntityCollector;

public class NominalAxiomDetector extends OWLEntityCollector {
	
	

	
	public NominalAxiomDetector(Set<OWLEntity> toReturn) {
		super(toReturn);
		// TODO Auto-generated constructor stub
	}

	// private HashSet<OWLIndividual> nominalIndividuals = new HashSet<OWLIndividual>();
	private boolean containsNominal = false;

    public void visit(OWLObjectOneOf desc) {
		// System.out.println("ObjectOneOf: " +  desc + " with values " + desc.getIndividuals());
		// this.nominalIndividuals.addAll(desc.getIndividuals());
		containsNominal = true;
	}

    public void visit(OWLObjectHasValue desc) {
		// System.out.println("ObjectValueRestriction: " + desc + " with value " + desc.getValue());
		// this.nominalIndividuals.add(desc.getValue());
		containsNominal = true;		
	}
	
	/**
	* Informs about the existence of a nominal in an axiom.
	* 
	* @return True if in previous check contains a definition of a nominal.
	*/
	public boolean detectedNominal() {
		if (this.containsNominal) {
			this.containsNominal = false;
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	* Resets the flag that indicates wether or not a nominal has been found.
	*/
	public void reset() {
		this.containsNominal = false;
	}



	
	
	
	

}
