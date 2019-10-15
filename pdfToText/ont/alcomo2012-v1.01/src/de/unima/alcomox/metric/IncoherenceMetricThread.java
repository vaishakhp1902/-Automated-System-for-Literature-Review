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

import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.Settings;
import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.mapping.Mapping;
import de.unima.alcomox.ontology.LocalOntology;
import de.unima.alcomox.util.ThreadTimer;

/**
* Replaces the old IMerasureProblem class and is under development
* at the moment to be used inside SEALS maybe via the Alignment API.
* 
* Only computes the max-card measure, can be used with a timeout, and returns
* the score so far computed (= a lower bound for the correct value). It will be
* applied only to a subset of the alignment which is the set of those
* correspondences that have a confidence > 0 and are  equivalence correspondences.
*/
class IncoherenceMetricThread extends Thread {
	
	private long timeout;

	private LocalOntology sourceOnt = null;
	private LocalOntology targetOnt = null;
	
	private String errorMessage = null;
	
	private Mapping nonReferingInputMapping;
	private Mapping extracted;
	private Mapping mapping;
	
	private boolean terminated = false;
	private boolean successful = false;
	

	IncoherenceMetricThread(long timeout) {
		this.timeout = timeout;	
	}
	

	void eval(LocalOntology sourceOnt, LocalOntology targetOnt, Mapping mapping) {
		this.sourceOnt = sourceOnt;
		this.targetOnt = targetOnt;
		this.mapping = mapping;
		
		ThreadTimer evaluationThread = new ThreadTimer(this, this.timeout);
		evaluationThread.setConcurrentExecution(false);
		evaluationThread.start();
		// wait until the thread has finished or is interrupted
		if (this.successful && this.nonReferingInputMapping.size() > 0) {
			this.errorMessage = "mapping contains non referring uris";
			this.errorMessage += "e.g. " + nonReferingInputMapping.get(0);
			this.successful = false;
		}
	}
	
	public void run() {
		if (this.sourceOnt == null || this.targetOnt == null || this.mapping == null) {
			this.errorMessage = "call method eval before running an evaluation thread";
			return;
		}
		boolean one_to_one = Settings.ONE_TO_ONE;
		try {
			// this.mapping.applyThreshhold(0.00001);
			this.mapping.reduceToEquivalenceCorrespondences();
			this.mapping.normalize(1.0);
			
			Settings.ONE_TO_ONE = false;
			
			ExtractionProblem ep = new ExtractionProblem(
					ExtractionProblem.METHOD_OPTIMAL,
					ExtractionProblem.ENTITIES_CONCEPTSPROPERTIES,
					ExtractionProblem.REASONING_COMPLETE
					// TODO rechecnge to complete!
			);
			ep.bindSourceOntology(sourceOnt);
			ep.bindTargetOntology(targetOnt);
			ep.bindMapping(this.mapping);
			
			ep.init();
			this.nonReferingInputMapping = ep.getNonReferingInputMapping();
			this.terminated = ep.solve();
			this.extracted = ep.getExtractedMapping();
			this.successful = true;
			Settings.ONE_TO_ONE = one_to_one;
		}
		catch (AlcomoException e) {
			this.errorMessage = e.getMessage();
			this.successful = false;
			Settings.ONE_TO_ONE = one_to_one;
		}
	}

	boolean evaluationTerminated() {
		return this.terminated ;
	}


	boolean evaluationSuccessful() {
		return this.successful;
	}
	
	
	String getErrorMessage() {
		if (this.successful) { return ""; }
		return errorMessage;
	}
	
	public double getDegreeOfIncoherence() {
		if (!this.successful) { return -1.0; }
		double numOfEx = (double)this.extracted.size();
		double numOfIn = (double)this.mapping.size();
		return ((numOfIn - numOfEx) / numOfIn);
	}
	
}
