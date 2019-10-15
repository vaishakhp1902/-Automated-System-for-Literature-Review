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
import de.unima.alcomox.mapping.Correspondence;
import de.unima.alcomox.mapping.Mapping;
import de.unima.alcomox.ontology.CompleteReasoner;
import de.unima.alcomox.ontology.EfficientReasoner;
//import de.unima.alcomox.util.DataStorage;

/**
* A greedy algoritm to solve an extraction problem. In some publication referred to as naive descending. 
*/
public class Greedy extends AlcomoExtraction {
	
	
	private EfficientReasoner efficientReasoner;
	private CompleteReasoner completeReasoner;
	
	/**
	* Constructs a greedy algorithm for solving the extraction problem.
	*  
	* @param extractionProblem The problem to be solved.
	*/
	public Greedy(ExtractionProblem extractionProblem) {
		super(extractionProblem);
	}

	/**
	* @see de.unima.alcomox.algorithms.AlcomoExtraction#run()
	*/
	public void run() throws AlcomoException {
		this.mapping.sortDescending();
		
		// efficient reasoning
		if (this.typeOfReasoning == ExtractionProblem.REASONING_EFFICIENT) {
			this.log.infoS("greedy algorithm with efficient (incomplete) reasoning ...");
			this.efficientReasoner = new EfficientReasoner(this.sourceOntology, this.targetOntology, this.extractionProblem);
			int counter = 0;
			double progress = 0.0;
			this.log.infoPStart();
			for (int candidateIndex = 0; candidateIndex < this.mapping.size(); candidateIndex++) {
				Correspondence candidateNexus = this.mapping.get(candidateIndex);				
				counter++;
				if (this.efficientReasoner.conflictsWithMapping(candidateNexus, this.activeMapping)) {
					this.inactiveMapping.push(candidateNexus);
				}
				else { 
					this.activeMapping.push(candidateNexus);	
				}
				// used for INFO
				if (((double)counter / (double)this.mapping.size()) >= progress) {
					double realProgress = (double)counter / (double)this.mapping.size();
					while (realProgress > progress) { progress += 0.05; }
					this.log.infoP(progress);
				}
				// in case of interrupt the remaining part of the alignment is activated
				if (this.hasBeenInterrupted()) {
					candidateIndex++;
					while (candidateIndex < this.mapping.size()) {
						this.activeMapping.push(this.mapping.get(candidateIndex));	
						candidateIndex++;
					}
					break;
				}
			}
			this.log.infoPEnd();
			this.log.infoS("... done, active=" + this.activeMapping.size() + " inactive=" + this.inactiveMapping.size() + " (greedy)");
		}
		
		
		// complete reasoning not using the efficient component at all (only for experimental study)
		else if (this.typeOfReasoning == ExtractionProblem.REASONING_BRUTEFORCE) {
			this.log.infoS("greedy algorithm with brute force complete reasoning ...");
			this.completeReasoner = new CompleteReasoner(this.sourceOntology, this.targetOntology, this.extractionProblem);	
			this.log.infoS("pre coherency check ...");
			if (!(completeReasoner.isConflictSet(this.mapping))) {
				this.activeMapping = this.mapping.getCopy();
				this.log.infoS(" ... upps, no coherency detected");
				this.log.infoS("... done, active=" + this.activeMapping.size() + " inactive=" + this.inactiveMapping.size() + " (greedy)");
				return;
			}
			
			int counter = 0;
			double progress = 0.0;
			this.log.infoPStart();
			for (int candidateIndex = 0; candidateIndex < this.mapping.size(); candidateIndex++) {
				Correspondence candidateNexus = this.mapping.get(candidateIndex);				
				counter++;
				this.activeMapping.push(candidateNexus);
				

				
				if (completeReasoner.isConflictSet(this.activeMapping)) {
					this.activeMapping.pop();
					this.inactiveMapping.push(candidateNexus);
				}

		
				// used for INFO
				if (((double)counter / (double)this.mapping.size()) >= progress) {
					double realProgress = (double)counter / (double)this.mapping.size();
					while (realProgress > progress) { progress += 0.05; }
					this.log.infoP(progress);
				}
				if (this.hasBeenInterrupted()) {
					candidateIndex++;
					while (candidateIndex < this.mapping.size()) {
						this.activeMapping.push(this.mapping.get(candidateIndex));	
						candidateIndex++;
					}
					break;
				}
			}
			this.log.infoPEnd();
			// used for INFO
			this.log.infoS("... done, active=" + this.activeMapping.size() + " inactive=" + this.inactiveMapping.size() + " (greedy)");
		}
		// complete reasoning using the efficient component
		else if (this.typeOfReasoning == ExtractionProblem.REASONING_COMPLETE) {
			this.log.infoS("greedy algorithm with complete and efficient reasoning ...");
			// construct reasoner
			this.efficientReasoner = new EfficientReasoner(this.sourceOntology, this.targetOntology, this.extractionProblem);
			this.completeReasoner = new CompleteReasoner(this.sourceOntology, this.targetOntology, this.extractionProblem);
			// some helper mappings	
			Mapping unvalidated = this.mapping.getCopy();
			Mapping chosenUnvalidated;
			Correspondence conflictingNexus = null;
			int counter = 0;
			do {
				this.log.infoS("start a turn with efficient reasoning ... ");
				counter++;
				chosenUnvalidated  = this.getChosenUnvalidated(this.activeMapping, unvalidated);
				this.log.infoS("complete reasoning (validation and search)");
				conflictingNexus = this.getConflictingCorrespondences(this.activeMapping, chosenUnvalidated);
				
				if (conflictingNexus == null) {
					this.log.infoS("... done, valid (a turn)");
				}
				else {
					// DataStorage.mirror.count();
					this.log.infoS("... done, found conflict, eleminate '" + conflictingNexus.toShortString() + " (a round)");
				}
				this.updateMappings(conflictingNexus, this.activeMapping, unvalidated, chosenUnvalidated);
				if (this.hasBeenInterrupted()) { 
					this.activeMapping.push(unvalidated);
					break;
				}
			} while (conflictingNexus != null);
			this.completeReasoner.resetValidatedMapping();
			this.inactiveMapping = this.mapping.getDifference(this.activeMapping);
			this.log.infoS("... done, active=" + this.activeMapping.size() + " inactive=" + this.inactiveMapping.size() + " (greedy)");
			
		}
	}	


	private Correspondence getConflictingCorrespondences(Mapping validated, Mapping chosenUnvalidated) throws AlcomoException {
		// TODO nex line has been added newly
		this.completeReasoner = new CompleteReasoner(this.sourceOntology, this.targetOntology, this.extractionProblem);	
		this.completeReasoner.attachValidatedMapping(validated);
		Correspondence invalidNexus = this.completeReasoner.searchInvalidCorrespondence(chosenUnvalidated);
		return invalidNexus;
	}
	
	private Mapping getChosenUnvalidated(Mapping validated, Mapping unvalidated) {
		Mapping chosenUnvalidated = new Mapping();
		for (Correspondence candidateNexus : unvalidated) {
			if (this.efficientReasoner.conflictsWithMapping(candidateNexus, validated)) { continue; }
			if (this.efficientReasoner.conflictsWithMapping(candidateNexus, chosenUnvalidated)) { continue; }
			chosenUnvalidated.push(candidateNexus);	
		}
		return chosenUnvalidated;
	}
	
	private void updateMappings(Correspondence conflictingNexus, Mapping validated, Mapping unvalidated, Mapping chosenUnvalidated) {
		for (Correspondence nexus : chosenUnvalidated) {
			if ((conflictingNexus == null) || !(nexus.equals(conflictingNexus))) { validated.push(nexus); }
			else { break; }
		}
		unvalidated.shrinkTill(conflictingNexus);
	}
	
}
