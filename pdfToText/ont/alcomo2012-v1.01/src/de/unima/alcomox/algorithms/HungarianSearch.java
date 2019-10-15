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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.mapping.Correspondence;
import de.unima.alcomox.mapping.MappingMatrix;
import de.unima.alcomox.ontology.EfficientReasoner;

/**
* A uniform cost search using the hungarian method
*/
public class HungarianSearch extends AlcomoExtraction {
	
	private EfficientReasoner efficientReasoner;
	// private CompleteReasoner completeReasoner = null;
	

	
	/**
	* Constructs a algorithm implementing a uniform cost search by spanning a tree where every node
	* in the tree is a (sub) mapping.
	*  
	* @param extractionProblem The problem to be solved.
	*/
	public HungarianSearch(ExtractionProblem extractionProblem) {
		super(extractionProblem);
	}

	/**
	* @see de.unima.alcomox.algorithms.AlcomoExtraction#run()
	*/
	public void run() throws AlcomoException {		

		// efficient reasoning optimized version
		if (this.typeOfReasoning == ExtractionProblem.REASONING_EFFICIENT || this.typeOfReasoning == ExtractionProblem.REASONING_COMPLETE) {
			// prepare a complete reasoner if necesarry
			if (this.typeOfReasoning == ExtractionProblem.REASONING_COMPLETE) { 
				// this.completeReasoner = new CompleteReasoner(this.sourceOntology, this.targetOntology, this.extractionProblem);
				this.log.infoS("uniform cost search in hungarian nodes with complete reasoning finding an optimal solution ...");
			}
			else {
				this.log.infoS("uniform cost search in hungarian nodes with efficient (incomplete) reasoning finding an optimal solution ...");
			}
			
			this.efficientReasoner = new EfficientReasoner(this.sourceOntology, this.targetOntology, this.extractionProblem);
			PriorityQueue<LockMarker> queue = new PriorityQueue<LockMarker>();
			// prepare hashset for duplicate detection
			HashSet<LockMarker> nodesInQueue = new HashSet<LockMarker>();
			// convert mapping into matrix
			MappingMatrix mappingMatrix = new MappingMatrix(mapping);
			// prepare method for performing munkre algorithm
			// HungarianMethod hungarianMethod = new HungarianMethod();
			// now precompute and store the conflicts
			ConflictStore conflictStore = new ConflictStore(this.efficientReasoner, this.mapping); 
			// prepare and  push the 'root' in the queue
			
			LockMarker rootMarker = new LockMarker();
			rootMarker.setScore(0.0); // value does not matter here
			queue.add(rootMarker);
			nodesInQueue.add(rootMarker);
			this.log.infoS("starting with all cells unlocked (" + this.mapping.size() + ")");

			
			LockMarker marker;
			int minActiveSizeInTree = 0;
			// double min;
			StateAnalyzer analyzer = new StateAnalyzer(this.mapping);
			while (true) {
				marker = queue.poll();
				nodesInQueue.remove(marker);
				if (marker.size() > minActiveSizeInTree) {					
					minActiveSizeInTree = marker.size();
					this.log.infoS("set locks=" + minActiveSizeInTree + " for best solution, score=" + marker.getScore() + ", queuesize=" + queue.size() + "");					
				}
				HungarianMethod hungarianMethod = new HungarianMethod();
				hungarianMethod.setInputMatrix(mappingMatrix.getDistances());
				hungarianMethod.setLocks(marker);
				// min = hungarianMethod.getMinimum();
				ArrayList<Integer> indices = hungarianMethod.getSolution(mappingMatrix);
				
				
			
				Correspondence[] chosenCorrespondences = hungarianMethod.getChosenCorrespondences(mappingMatrix);
				analyzer.store(chosenCorrespondences);
				
				//System.out.println(hungarianMethod.toOString());
				//System.out.println("Min: " + hungarianMethod.getMinimum());

				
				Set<Integer> conflictIndices = conflictStore.getConflictingIndices(indices); 
				/*
				System.out.println("Conflict indices set = " + conflictIndices.size() + ": ");
				for (int x : conflictIndices) {
					System.out.println(x + " => " + this.mapping.get(x).toShortString());
				}
				System.out.println("---------------------------");
				*/
				
				if (conflictIndices == null) {
					this.activeMapping = hungarianMethod.getMapping(mappingMatrix);
					this.inactiveMapping = this.mapping.getDifference(this.activeMapping);
				}
				// now check if really everything is fine
				if (this.typeOfReasoning == ExtractionProblem.REASONING_EFFICIENT) {
					// everthings fine, no need to worry anymore
				}
				else if (this.typeOfReasoning == ExtractionProblem.REASONING_COMPLETE) {
					// now do wome additional stuff
					// ...
					// ...
					
				}
				// looks strange, but in former if-branch conflict indices
				// might habe been reinitialized due to complete reasoning
				if (conflictIndices != null) {
					for (Integer index : conflictIndices) {
						Coord lockedCoord = mappingMatrix.getCoord(index);
						// System.out.println("LOCK COORD: " +  lockedCoord);
						LockMarker childLock = marker.getChild(lockedCoord);
						
						if (!(nodesInQueue.contains(childLock))) {
							hungarianMethod = new HungarianMethod();
							hungarianMethod.setInputMatrix(mappingMatrix.getDistances());
							hungarianMethod.setLocks(childLock);
							
							childLock.setScore(hungarianMethod.getMinimum());
							queue.add(childLock);
							nodesInQueue.add(childLock);
						}	
					}
				}
				else {
					break;
				}
				
				
				// solution found for efficient reasoning (complete reasoning might still detect conflicts)
				/*
				if (conflictIndices == null) {
					tempMapping = m.getActiveMapping(this.mapping);
					
					if (this.typeOfReasoning == ExtractionProblem.REASONING_EFFICIENT) {
						this.activeMapping = tempMapping;
						this.inactiveMapping = m.getInactiveMapping(this.mapping);
						break;
					}
					else if (this.typeOfReasoning == ExtractionProblem.REASONING_COMPLETE) {
						
						this.log.infoW("[complete reasoning ... ");
						if (!(completeReasoner.isConflictSet(tempMapping))) {
							this.activeMapping = tempMapping;
							this.inactiveMapping = m.getInactiveMapping(this.mapping);
							this.log.infoW("no additional conflicts detected]\n");
							break;
						}
						else {							
							Mapping conflictMapping = completeReasoner.getConflictSet(tempMapping);
							conflictIndices = conflictStore.getIndicesAndSetConflicts(conflictMapping);
							this.log.infoW("detected conflict set " + conflictMapping.toVeryShortString() + " of size " + conflictIndices.size() + "]\n");
						}						
					}
				}
				*/

 	
			} 
			// System.out.println(analyzer);
			
			this.log.infoS("... done, active=" + this.activeMapping.size() + " inactive=" + this.inactiveMapping.size() + " (optimal)");
		}		
	}

	
}
