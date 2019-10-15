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


import java.text.DecimalFormat;

import de.unima.alcomox.exceptions.CorrespondenceException;
import de.unima.alcomox.ontology.Entity;


/**
* A correspondence represents a (simple) semantic relation between two
* entities belonging to different ontologies. The enties are first only
* referred to by their uris (unbound correspondence), when the container
* mapping is bound to the ontologies the entities attributed are set to
* the internal entity representation.
*/
public class Correspondence implements Comparable<Correspondence>  {
	
	private static int idCounter = 0;
	
	private int id = 0;
	private String sourceEntityUri;
	private String targetEntityUri;
	private SemanticRelation relation;
	private double confidence;	
	private Entity sourceEntity;
	private Entity targetEntity;

	/**
	* Constructs an unbound correspondence with confidence value set to 1.0.
	* 
	* @param sourceEntityUri Uri of the source entity.
	* @param targetEntityUri Uri of the target entity.
	* @param relation Semantic relation between two entities.
	* @throws CorrespondenceException Thrown if the uris are not wellformed (in a weak sense).
	* Checking wether the uri reference can be resolved does not occur in this context.
	*/
	public Correspondence(String sourceEntityUri, String targetEntityUri, SemanticRelation relation) throws CorrespondenceException {
		this.setSourceEntityUri(sourceEntityUri);
		this.setTargetEntityUri(targetEntityUri);
		this.setRelation(relation);
		this.setConfidence(1.0);
		this.sourceEntity = null;
		this.targetEntity = null;
	}
	
	/**
	* Constructs an unbound correspondence with confidence value set to 1.0.
	* 
	* @param sourceEntityUri Uri of the source entity.
	* @param targetEntityUri Uri of the target entity.
	* @param relation Semantic relation between two entities.
	* @param confidence Confidence value of this correspondence.
	* @throws CorrespondenceException Thrown if the uris are not wellformed (in a weak sense) and
	* if the confidence value is not in the range from 0.0 to 1.0.
	* Checking wether the uri reference can be resolved does not occur in this context.
	*/
	public Correspondence(String sourceConcept, String targetConcept, SemanticRelation relation, double confidence) throws CorrespondenceException {
		this(sourceConcept, targetConcept, relation);
		this.setConfidence(confidence);
	}
	
	/**
	* @return String representation of this correspondence. 
	*/
	public String toString() {	
		// return  sourceEntityUri + " " + relation + " " + targetEntityUri + " | " + confidence ;
		return  sourceEntityUri + " " + relation + " " + targetEntityUri + " | " + confidence  + "(" + this.getSourceEntity() + " - " +  this.getTargetEntity() +  ")";
	}
	
	/**
	* @return Short string representation of this correspondence. 
	*/	
	public String toShortString() {
		return  this.getFragement(sourceEntityUri) + " " + relation + " " + this.getFragement(targetEntityUri) + " | " + formatConfidence(confidence);
	}
	
	public int getId() {
		if (this.id == 0) { this.id = idCounter++; }
		return this.id;
	}

	// equality and hashcode
	
	/**
	*  @return True if uri of source entity, uri of target entity and semantic relation are the same,
	*  false otherwise. Equality is not affected by the confidence value. This means that two
	*  correspondences can be equal if they make thze same statement with different trust in the statement.
	*  Input mappings that contain such correspondences are regarded as incorrect.
	*/
	public boolean equals(Object thatObject) {
		Correspondence that = (Correspondence)thatObject;
		String c1 = this.sourceEntityUri + this.relation + this.targetEntityUri;
		String c2 = that.sourceEntityUri + that.relation + that.targetEntityUri;
		if (c1.equals(c2)) { return true; }
		else { return false; }
	}
	


	public int hashCode() {
		return (this.sourceEntityUri + this.relation + this.targetEntityUri).hashCode();
	}
	
	// related to ordering correspondences
	
	public int compareTo(Correspondence correspondence) {
		Correspondence that = (Correspondence)correspondence;
		if (this.getConfidence() > that.getConfidence()) { return 1; }	
		else if (this.getConfidence() < that.getConfidence()) { return -1; }
		else {
			if (this.getSourceEntityUri().compareTo(that.getSourceEntityUri()) > 0) { return 1; }	
			else if	(this.getSourceEntityUri().compareTo(that.getSourceEntityUri()) < 0) { return -1; }	
			else {
				if (this.getTargetEntityUri().compareTo(that.getTargetEntityUri()) > 0) { return 1; }	
				else if	(this.getTargetEntityUri().compareTo(that.getTargetEntityUri()) < 0) { return -1; }	
				else { return 0; }
			}
		}
	}	
	
	// getter 
	
	public String getSourceEntityUri() {
		return this.sourceEntityUri;
	}

	public String getTargetEntityUri() {
		return this.targetEntityUri;
	}

	public SemanticRelation getRelation() {
		return this.relation;
	}
	
	public double getConfidence() {
		return this.confidence;
	}	
	
	public Entity getSourceEntity() {
		return sourceEntity;
	}
	
	public Entity getTargetEntity() {
		return targetEntity;
	}
	
	public String getSourceNamespace() {
		return this.getNamespace(this.getSourceEntityUri());
	}
	
	public String getTargetNamespace() {
		return this.getNamespace(this.getTargetEntityUri());
	}
	
	public boolean isEquivOrSuper() {
		return (this.relation.getType() == SemanticRelation.EQUIV || this.relation.getType() == SemanticRelation.SUPER);
	}
	
	public boolean isEquivOrSub() {
		return (this.relation.getType() == SemanticRelation.EQUIV || this.relation.getType() == SemanticRelation.SUB);
	}
	
	
	// setter
	
	public void setSourceEntityUri(String sourceEntity) throws CorrespondenceException {
		this.sourceEntityUri = sourceEntity;
		/*
		if (!(isFullname(this.sourceEntityUri))) {
			throw new CorrespondenceException(
					CorrespondenceException.MISSING_NAMESPACE,
					"occured with respect to '" + this.toString() + "'"
			);				
		}
		*/
	}

	public void setTargetEntityUri(String targetEntity) throws CorrespondenceException {
		this.targetEntityUri = targetEntity;
		/*
		if (!(isFullname(this.targetEntityUri))) {
			throw new CorrespondenceException(
					CorrespondenceException.MISSING_NAMESPACE,
					"occured with respect to '" + this.toString() + "'"
			);				
		}
		*/
	}
	
	public void setConfidence(double confidence) {
		this.confidence = confidence;
		/*
		// it was for some application to strict to have the following rangen restriction
		if (this.confidence < 0.0 || this.confidence > 1.0) {
			throw new CorrespondenceException(
				CorrespondenceException.INVALID_CONFIDENCE_VALUE,
				"occured with respect to '" + this.toString() + "'"
			);
		}
		*/
	}
	
	/**
	* Sets the confidence value to 1.0 without need to catch a Exception. 
	*
	*/
	public void setTopConfidence() {
		this.confidence = 1.0;
	}
	
	public void setRelation(SemanticRelation relation) {
		this.relation = relation;
	}
	
	public void setSourceEntity(Entity entity) {
		this.sourceEntity = entity;	
	}
	
	public void setTargetEntity(Entity entity) {
		this.targetEntity = entity;
	}
	

	// private
	
	private boolean hasFragment(String fullname) {
		if (fullname.contains("#")) { return true; }
		else { return false; }	
	}
	
	private String getNamespace(String uri) {
		if (this.hasFragment(uri)) {
			String[] parts = uri.split("#");
			return parts[0];
		}
		else {
			return uri;
		}
	}
	
	private String getFragement(String uri) {
		if (this.hasFragment(uri)) {
			String[] parts = uri.split("#");
			return parts[1];
		}
		else {
			return uri;
		}
	}


	public String getSourceEntityId() {
		return this.getFragement(this.getSourceEntityUri());
	}
	
	public String getTargetEntityId() {
		return this.getFragement(this.getTargetEntityUri());
	}

	public void setSourceNamespace(String namespace) {
		String fragment = this.getFragement(this.getSourceEntityUri());
		try {
			this.setSourceEntityUri(namespace + "#" + fragment);
		}
		catch (CorrespondenceException e) { }	
	}
	
	public void setTargetNamespace(String namespace) {
		String fragment = this.getFragement(this.getTargetEntityUri());
		try {
			this.setTargetEntityUri(namespace + "#" + fragment);
		}
		catch (CorrespondenceException e) { }	
	}

	public void invert() {
		String sourceEntity;
		String targetEntity;
		SemanticRelation semanticRelation;		
		sourceEntity = this.getSourceEntityUri();
		targetEntity = this.getTargetEntityUri();
		semanticRelation = this.getRelation();
		try {
			this.setSourceEntityUri(targetEntity);
			this.setTargetEntityUri(sourceEntity);
		} catch (CorrespondenceException e) {
			// cannot occur in this context
		}
		
		this.setRelation(semanticRelation.getInverse());
	}	
	
	private String formatConfidence(double c) {
		DecimalFormat nf = new DecimalFormat("0.000");
		return nf.format(c);
	}
	
	protected void invertRelation() {
		try {
			if (this.getRelation().getType() == SemanticRelation.SUB) {
				this.setRelation(new SemanticRelation(SemanticRelation.SUPER));
			}
			else if (this.getRelation().getType() == SemanticRelation.SUPER) {
				this.setRelation(new SemanticRelation(SemanticRelation.SUB));
			}
		} catch (CorrespondenceException e) {
			// nop
		}

	}

	/**
	* Creates and returns a new instance that is a deep clone of this correspondences. used to (deep) copy
	* mappings without keeping the same references to correspondences or their attributes.
	* @return
	*/
	public Correspondence getDeepCopy() {
		Correspondence cloned = null;
		try {
			SemanticRelation clonedRelation = new SemanticRelation(relation.getType());
			cloned = new Correspondence(this.sourceEntityUri, this.targetEntityUri, clonedRelation, this.confidence);
			cloned.setSourceEntity(this.sourceEntity);
			cloned.setTargetEntity(this.targetEntity);
			cloned.id = 0;
			
		}
		catch (CorrespondenceException e) {
			e.printStackTrace();
		}
		return cloned;
	}
	
	



}
