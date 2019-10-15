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

package de.unima.alcomox.metric;

import de.unima.alcomox.mapping.Mapping;
import de.unima.alcomox.ontology.LocalOntology;


/**
* Replaces the old IMerasureProblem class and is under development
* at the moment to be used inside SEALS maybe via the Alignment API.
* 
* Only computes the max-card measure, can be used with a timeout, and returns
* the score so far computed (= a lower bound for the correct value). It will be
* applied only to a subset of the alignment which is the set of those
* correspondences that have a confidence > 0 and are  equivalence correspondences.
*/
public class IncoherenceMetric {
	
	// default is a 5 minute timeout
	private final long DEFAULT_TIMEOUT = 300000l; 
	
	private IncoherenceMetricThread imt;

	
	/**
	* Constructs an incoherence metric with default timeout. 
	*/
	public IncoherenceMetric() {
		this.imt = new IncoherenceMetricThread(DEFAULT_TIMEOUT);
	}
	
	/**
	* Constructs an incoherence metric with timeout specified in millis. 
	*/
	public IncoherenceMetric(long timeout) {
		this.imt = new IncoherenceMetricThread(timeout);
	}
	
	/**
	* Runs an evaluation by applying the incoherence metric a.k.a maximum cardinality
	* incoherence measure (as defined in Meilicke,Stuckenschmidt OM 2008).
	* 
	* @param sourceOntPathOrUri Filepath or URL of the source ontology.
	* @param targetOntPathOrUri Filepath or URL of the target ontology.
	* @param mappingPathOrUri Filepath or URL of the target ontology.
	*/
	public void eval(LocalOntology sourceOnt, LocalOntology targetOnt, Mapping mapping) {
		this.imt.eval(sourceOnt, targetOnt, mapping);
	}
	

	/**
	* 
	* @return True if the evaluation terminated successful before the timeout 
	* expired; false otherwise (due to timeout or some interal problems).
	*/
	public boolean evaluationTerminated() {
		return this.imt.evaluationTerminated();
	}

	/**
	* Checks whether the evaluation was successful and generated
	* a meaningful value.
	* 
	* @return True if the evaluation was successful, i.e. generated a
	* meaningful value. Such a value (due to the kind of anytime behaviour)
	* will also be generated of the timout expired, as long as the algorithm
	* could successfully work with the input data. 
	*/
	public boolean evaluationSuccessful() {
		return this.imt.evaluationSuccessful();
	}
	
	/**
	* If the evaluation has not been successful, a string representations of
	* caught exceptions can be retrieved.
	* 
	* @return An error message if an error has occured.
	*/
	public String getErrorMessage() {
		return this.imt.getErrorMessage();
	}
	
	/**
	* Returns the degree of incoherence.
	* 
	* @return The degree of incoherence or an lower bound (if interupted prior to
	* finishing) or -1 (in case of an internal error).
	*/
	public double getDegreeOfIncoherence() {
		return this.imt.getDegreeOfIncoherence();
	}
	
}
