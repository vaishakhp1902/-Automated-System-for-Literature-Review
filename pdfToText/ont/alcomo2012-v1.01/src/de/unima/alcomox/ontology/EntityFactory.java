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

public class EntityFactory {
	
	public static Entity createConceptEntity(OWLClass concept) {
		Entity entity = new Entity();
		entity.setConcept(concept);
		return entity;
	}
	
	/**
	* Constructs an entity corresponding to an object property with both domain and range class
	* extension.
	* 
	* @param property The corresponding property.
	* @param domain Domain class extension of the property.
	* @param range Range class extension of the property
	* 
	* @return The constructed entity.
	*/
	public static Entity createObjectPropertyEntity(OWLObjectProperty property, OWLClass domain, OWLClass range) {
		Entity entity = new Entity();
		entity.setObjectProperty(property);
		entity.setDomain(domain);
		entity.setRange(range);
		return entity;
	}

	/**
	* Constructs an entity corresponding to an object property with only a domain extension.
	* This is used when range extension is turned off.
	* 
	* @param property The corresponding property.
	* @param domain Domain class extension of the property.
	* @param range Range class extension of the property
	* 
	* @return The constructed entity.
	*/
	public static Entity createObjectPropertyEntity(OWLObjectProperty property, OWLClass domain) {
		Entity entity = new Entity();
		entity.setObjectProperty(property);
		entity.setDomain(domain);
		return entity;
	}	
	
	public static Entity createDataPropertyEntity(OWLDataProperty property, OWLClass domain) {
		Entity entity = new Entity();
		entity.setDataProperty(property);
		entity.setDomain(domain);
		return entity;
	}	

}
