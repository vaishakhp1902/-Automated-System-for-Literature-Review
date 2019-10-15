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

package de.unima.alcomox.algorithms;

import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.mapping.Mapping;
import de.unima.alcomox.ontology.LocalOntology;
import de.unima.alcomox.util.AlcomoLogger;

public abstract class AlcomoExtraction {
	
	protected ExtractionProblem extractionProblem;
	
	protected LocalOntology sourceOntology = null;
	protected LocalOntology targetOntology = null;
	
	protected Mapping mapping = null;
	protected Mapping activeMapping = null;
	protected Mapping inactiveMapping = null;
	
	protected int typeOfReasoning;
	
	private boolean terminated  = true;
	
	protected AlcomoLogger log;
	
	
	/**
	* Solves the eaxtraction problem by selecting a part of the mapping as activated and the
	* remainder as inactive. 
	*/
	public abstract void run() throws AlcomoException;
	
	/**
	* Constructs an algorithm for solving an extraction problem.
	* 
	* @param extractionProblem The problem to be solved.
	*/
	public AlcomoExtraction(ExtractionProblem extractionProblem) {
		this.log = new AlcomoLogger(this.getClass());
		this.extractionProblem = extractionProblem;
		this.typeOfReasoning = this.extractionProblem.getParam(ExtractionProblem.REASONING);
		this.activeMapping = new Mapping();
		this.inactiveMapping = new Mapping();
	}
	
	/**
	* Returns the activated mapping (also referred to as the extracted mapping).
	* 
	* @return The activated mapping.
	*/
	public Mapping getActiveMapping() {
		return this.activeMapping;
	}	

	/**
	* Returns the deactivated mapping (also referred to as the thrown, removed or rejected mapping).
	* 
	* @return The inactive mapping.
	*/
	public Mapping getInactiveMapping() {
		return this.inactiveMapping;
	}
	
	/**
	* @param sourceOntology The source ontology.
	*/
	public void setSourceOntology(LocalOntology sourceOntology) {
		this.sourceOntology = sourceOntology;
	}

	/**
	* @param targetOntology The target ontology.
	*/
	public void setTargetOntology(LocalOntology targetOntology) {
		this.targetOntology = targetOntology;
	}	

	/**
	* @param mapping The mapping that the algorithm extracts from.
	*/
	public void setMapping(Mapping mapping) {
		this.mapping = mapping.getCopy();
	}
	
	/**
	* Checks for an interrupt, stores current results and stops computation.
	* 
	* @return True, if the thread has been interrupted; false otherwise.
	*/
	protected boolean hasBeenInterrupted() {
		if (Thread.currentThread().isInterrupted()) {
			this.terminated = false;
			this.log.infoS("... interrupted, thread has been terminated due to a timeout!");
			// this.log.infoS("... result at time of interrupt: active=" + this.activeMapping.size() + " inactive=" + this.inactiveMapping.size());
			return true;
		}
		return false;
	}
	
	/**
	*@return True if the algorithm has not yet been run or if it has been run
	* and correctly terminated without an interrupt.
	*/
	public boolean terminatedCorrectly() {
		return this.terminated;
	}
	
}
