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
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;



public class Entity {

	private OWLClass concept;
	private OWLDataProperty dataProperty;
	private OWLObjectProperty objectProperty;
	private OWLClass domain;
	private OWLClass range;

	
	public Entity() {
		this.concept = null;
		this.objectProperty = null;
		this.dataProperty = null;
		this.domain = null;
		this.range = null;	
	}
	
	public String toString() {
		String rep = "Entity: ";
		rep += (this.concept != null) ? this.concept : ""; 
		rep += (this.objectProperty != null) ? this.objectProperty : ""; 
		rep += (this.dataProperty != null) ? this.dataProperty : ""; 
		rep += " (C=" + this.isConcept() + " OP=" + this.isObjectProperty() + " DP=" + this.isDataProperty() + ")";
		// rep += "\n[C:" + this.concept + " OP:" + this.objectProperty  + " DP:" + this.dataProperty + " DOM:" + this.domain + " RAN:" + this.range;	
		return rep;
		
	}
	
	
	public OWLClass getConcept() {
		return this.concept;
	}
	
	public void setConcept(OWLClass concept) {
		this.concept = concept;
	}
	
	public OWLObjectProperty getObjectProperty() {
		return objectProperty;
	}
	
	public void setObjectProperty(OWLObjectProperty property) {
		this.objectProperty = property;
	}	
	
	public OWLDataProperty getDataProperty() {
		return dataProperty;
	}
	
	public void setDataProperty(OWLDataProperty property) {
		this.dataProperty = property;
	}
	
	public OWLClass getRange() {
		return range;
	}
	
	public void setRange(OWLClass range) {
		this.range = range;
	}
	
	public OWLClass getDomain() {
		return domain;
	}
	
	public void setDomain(OWLClass domain) {
		this.domain = domain;
	}
	
	public boolean isConcept() {
		if (this.concept != null) { return true; }
		else { return false; }
	}
	
	public boolean isObjectProperty() {
		if (this.objectProperty != null) { return true; }
		else { return false; }
	}
	
	public boolean isDataProperty() {
		if (this.dataProperty != null) { return true; }
		else { return false; }
	}

}
