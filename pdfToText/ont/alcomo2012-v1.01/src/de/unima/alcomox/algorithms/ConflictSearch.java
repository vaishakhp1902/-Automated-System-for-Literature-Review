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

import java.util.HashSet;
import java.util.Set;

import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.mapping.Correspondence;
import de.unima.alcomox.ontology.EfficientReasoner;
import de.unima.alcomox.ontology.ConflictPair;

public class ConflictSearch extends AlcomoExtraction {

	public ConflictSearch(ExtractionProblem extractionProblem) {
		super(extractionProblem);
	}

	private Set<ConflictPair> conflictSets;
	

	
	public void run() {
		EfficientReasoner alcomoReasoner = new EfficientReasoner(this.sourceOntology, this.targetOntology, this.extractionProblem);
		this.conflictSets = new HashSet<ConflictPair>();
		Correspondence c1 = null;
		Correspondence c2 = null;
		int counter = 0;
		double progress = 0.0;
		this.log.infoS("computing all conflicts pair detectable by pattern base reasoning ...");
		this.log.infoPStart();
		// int conflictCounter = 0;
		for (int i = 0; i < this.mapping.size(); i++) {	
			counter++;
			if (((double)counter / (double)this.mapping.size()) >= progress) {
				double realProgress = (double)counter / (double)this.mapping.size();
				while (realProgress > progress) { progress += 0.001; }
				this.log.infoP(progress);
			}
			for (int j = i + 1; j < this.mapping.size(); j++) {
				c1 = this.mapping.get(i);
				c2 = this.mapping.get(j);
				if (alcomoReasoner.isConflictPair(c1, c2)) {
					ConflictPair cs = new ConflictPair(c1, c2);
	
					this.conflictSets.add(cs); 
				}
			}	
		}
		this.log.infoPEnd();
		this.log.infoS("... done, detected " + this.conflictSets.size() + " conflict pairs (incomplete)");
		
	}
	
	
	public Set<ConflictPair> getConflictSets() {
		return this.conflictSets;
	}

	/*
	public String[] getUnsatisfiableClasses() throws AlcomoException {
		CompleteReasoner completeReasoner = new CompleteReasoner(this.sourceOntology, this.targetOntology, this.extractionProblem);		
		return completeReasoner.getAllUnsatisfiableClasses(this.mapping);
		
		
	}
	*/

}
