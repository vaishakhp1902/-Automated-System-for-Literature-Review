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
import java.util.PriorityQueue;
import java.util.Set;

import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.mapping.Mapping;
import de.unima.alcomox.ontology.CompleteReasoner;
import de.unima.alcomox.ontology.EfficientReasoner;
// import de.unima.alcomox.util.DataStorage;

/**
* A uniform cost search to solve an extraction problem. . 
*/
public class AStarSearch extends AlcomoExtraction {
	
	private EfficientReasoner efficientReasoner;
	private CompleteReasoner completeReasoner = null;
	

	
	/**
	* Constructs a algorithm implementing a uniform cost search by spanning a tree where every node
	* in the tree is a (sub) mapping.
	*  
	* @param extractionProblem The problem to be solved.
	*/
	public AStarSearch(ExtractionProblem extractionProblem) {
		super(extractionProblem);
	}

	/**
	* @see de.unima.alcomox.algorithms.AlcomoExtraction#run()
	*/
	public void run() throws AlcomoException {		

		// efficient reasoning optimized version
		if (this.typeOfReasoning == ExtractionProblem.REASONING_EFFICIENT || this.typeOfReasoning == ExtractionProblem.REASONING_COMPLETE || this.typeOfReasoning == ExtractionProblem.REASONING_BRUTEFORCE) {
			// prepare a complete reasoner if necesarry
			Mapping tempMapping = null;
			if (this.typeOfReasoning == ExtractionProblem.REASONING_COMPLETE || this.typeOfReasoning == ExtractionProblem.REASONING_BRUTEFORCE) { 
				this.completeReasoner = new CompleteReasoner(this.sourceOntology, this.targetOntology, this.extractionProblem);
				this.log.infoS("a*search with complete reasoning finding an optimal solution ...");
			}
			else {
				this.log.infoS("a*search with efficient (incomplete) reasoning finding an optimal solution ...");
			}
			
			this.efficientReasoner = new EfficientReasoner(this.sourceOntology, this.targetOntology, this.extractionProblem);
			PriorityQueue<IndexMarker> queue = new PriorityQueue<IndexMarker>();
			// prepare hashset for duplicate detection
			HashSet<IndexMarker> nodesInQueue = new HashSet<IndexMarker>();
			// prepare and  push the 'root' in the queue
			IndexMarker m;
			ConflictStore conflictStore;
			if (this.typeOfReasoning == ExtractionProblem.REASONING_BRUTEFORCE) {
				// empty conflict store for brute force
				
				conflictStore = new ConflictStore(this.mapping); 
				
			}
			else {
				// pre computed conflict store
				conflictStore = new ConflictStore(this.efficientReasoner, this.mapping); 
			}
			IndexMarker marker = new IndexMarker(mapping, conflictStore);
			queue.add(marker);
			nodesInQueue.add(marker);
			int minActiveSizeInTree = marker.activeSize();
			this.log.infoS("starting with " + minActiveSizeInTree + " correspondences");
			this.activeMapping = marker.getActiveMapping(this.mapping);
			this.inactiveMapping = marker.getInactiveMapping(this.mapping);
			
			while (true) {
				m = queue.poll();
				if (this.hasBeenInterrupted()) { 
					break;
				}
				nodesInQueue.remove(m);
				if (m.activeSize() < minActiveSizeInTree) {					
					minActiveSizeInTree = m.activeSize();
					this.activeMapping = m.getActiveMapping(this.mapping);
					this.inactiveMapping = m.getInactiveMapping(this.mapping);
					this.log.infoS("size of smallest mapping reduced, size=" + minActiveSizeInTree + ", trust=" + m.getTrust() + ", queuesize=" + queue.size() + "");					
				}
				Set<Integer> conflictIndices = conflictStore.getConflictingIndices(m);
				// solution found for efficient reasoning (complete reasoning might still detect conflicts)
				if (conflictIndices == null) {
					tempMapping = m.getActiveMapping(this.mapping);
					
					if (this.typeOfReasoning == ExtractionProblem.REASONING_EFFICIENT) {
						this.activeMapping = tempMapping;
						this.inactiveMapping = m.getInactiveMapping(this.mapping);
						break;
					}
					else if (this.typeOfReasoning == ExtractionProblem.REASONING_COMPLETE || this.typeOfReasoning == ExtractionProblem.REASONING_BRUTEFORCE) {
						// TODO here is where the rubber hits the road
						// System.out.println("*** create new complete reasoner  ***");
						this.completeReasoner = new CompleteReasoner(this.sourceOntology, this.targetOntology, this.extractionProblem);
						
						this.log.infoW("[complete reasoning ... ");
						if (!(completeReasoner.isConflictSet(tempMapping))) {
							
							this.activeMapping = tempMapping;
							this.inactiveMapping = m.getInactiveMapping(this.mapping);
							this.log.infoW("no additional conflicts detected]\n");
							break;
						}
						else {
							// DataStorage.mirror.count();
							Mapping conflictMapping = completeReasoner.getConflictSet(tempMapping);
							// System.out.println("*** finished computation of conflict set ***");
							conflictIndices = conflictStore.getIndicesAndSetConflicts(conflictMapping);
							this.log.infoW("detected conflict set " + conflictMapping.toVeryShortString() + " of size " + conflictIndices.size() + "]\n");
						}						
					}
				}
				// looks strange, but in former if-branch conflict indices
				// might have been reinitialized due to complete reasoning
				if (conflictIndices != null) {
					for (Integer index : conflictIndices) {
						IndexMarker child = m.getChild(index, conflictStore);			
						if (!(nodesInQueue.contains(child))) {
							queue.add(child);
							nodesInQueue.add(child);
						}	
					}
				} 	
			} 
			this.log.infoS("... done, active=" + this.activeMapping.size() + " inactive=" + this.inactiveMapping.size() + " (optimal)");
		}		
	}

	
}
