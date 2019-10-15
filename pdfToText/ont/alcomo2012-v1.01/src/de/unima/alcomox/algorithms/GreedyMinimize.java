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
import de.unima.alcomox.ontology.EfficientReasoner;
// import de.unima.alcomox.util.DataStorage;

/**
* A uniform cost search to solve an extraction problem. . 
*/
public class GreedyMinimize extends AlcomoExtraction {
	
	private EfficientReasoner efficientReasoner;
	

	
	/**
	* Constructs a algorithm implementing a greedy search that removes first those correspondences
	* that are involved in most conflicts. In cases these are several, the correspondence with 
	* lowest confidence is first removed.
	*  
	* @param extractionProblem The problem to be solved.
	*/
	public GreedyMinimize(ExtractionProblem extractionProblem) {
		super(extractionProblem);
	}

	/**
	* @see de.unima.alcomox.algorithms.AlcomoExtraction#run()
	*/
	public void run() throws AlcomoException {		

		// efficient reasoning optimized version
		if (this.typeOfReasoning == ExtractionProblem.REASONING_EFFICIENT) {
			// prepare a complete reasoner if necessary
			this.log.infoS("greedy minimize with efficient (incomplete) reasoning approximating an optimal solution ...");
			this.efficientReasoner = new EfficientReasoner(this.sourceOntology, this.targetOntology, this.extractionProblem);
			// pre-compute and store pattern-based conflicts
			ConflictStore conflictStore = new ConflictStore(this.efficientReasoner, this.mapping); 
			// prepare the index marker that is modified in place during the greedy search
			IndexMarker marker = new IndexMarker(mapping, conflictStore);
			int minActiveSizeInTree = marker.activeSize();
			this.log.infoS("starting with " + minActiveSizeInTree + " correspondences");
			
			int index = conflictStore.getTopWeightedConflictingIndex(marker);
			int removalCounter = 0;
			double progress = 0;
			this.log.infoPStart("reductions rate at");
			while (index >= 0) {
				
				// used for INFO
				if (((double)removalCounter / (double)this.mapping.size()) >= progress) {
					double realProgress = (double)removalCounter / (double)this.mapping.size();
					while (realProgress > progress) { progress += 0.01; }
					this.log.infoP(progress);
				}
				marker.deactivate(index);
				index = conflictStore.getTopWeightedConflictingIndex(marker);
				removalCounter++;
			}
			
			
			
			this.activeMapping = marker.getActiveMapping(this.mapping);
			this.inactiveMapping = marker.getInactiveMapping(this.mapping);
			this.log.infoPEnd();
			this.log.infoS("... done, active=" + this.activeMapping.size() + " inactive=" + this.inactiveMapping.size() + " (greedy minmized)");
		}		
	}

	
}
