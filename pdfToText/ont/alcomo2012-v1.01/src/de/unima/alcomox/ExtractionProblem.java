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

package de.unima.alcomox;

import java.util.*;

// TODO (ugly code) check why this import statement is needed, looks tsrange here, should be available only at deeper level
// import org.semanticweb.owl.model.OWLClass;


import org.semanticweb.owlapi.model.OWLClass;
import de.unima.alcomox.algorithms.*;
import de.unima.alcomox.mapping.*;
import de.unima.alcomox.ontology.*;
import de.unima.alcomox.util.*;
import de.unima.alcomox.exceptions.*;

public class ExtractionProblem extends AlcomoProblem {
	
	/**
	* The method parameter determines whether greedy or optimal strategy 
	* (with respect to confidences) is chosen. Chose between values
	* {@link #METHOD_GREEDY} and {@link #METHOD_OPTIMAL}.
	*/
	public static final String METHOD = "METHOD";
	
	/**
	* The entities parameter determines whether only concepts correspondences or
	* correspondences between concepts and relations are accepted.  Chose between values
	* {@link #ENTITIES_ONLYCONCEPTS} and {@link #ENTITIES_CONCEPTSPROPERTIES}.
	*/
	public static final String ENTITIES = "ENTITIES";
	
	/**
	* The reasoning parameter determines whether efficient (incomplete) pairwise
	* reasoning or complete reasoning is used to detect conflicts. Chose between values
	* {@link #REASONING_EFFICIENT}, {@link #REASONING_COMPLETE} and {@link #REASONING_BRUTEFORCE}.
	*/
	public static final String REASONING = "REASONING";
	
	
	/**
	* The conflict sensitivity exactness determines the exactness of the
	* conflict sensitivity estimation. In particular, it determines the number
	* of randomly chosen correspondences that are checked when estimating
	* the conflict sensitivity.
	*/
	public static final String CSEXACTNESS = "CSEXACTNESS";
	/**
	* Greedy method, formerly refered to as naive descending, now referred to as local optimal diagnosis.
	*/
	public static final int METHOD_GREEDY = 1;
	
	/**
	* Method to find an optimal solution (a minimum weighted hitting set). Unweighted (counting
	* correspondences) can be achieved by setting all confidences to the same value in input mapping.
	* Now referred to as global optimal diagnosis.
	*/
	public static final int METHOD_OPTIMAL = 2;
	
	/**
	* Method to find an optimal 1:1 solution (a minimum weighted hitting set). Unweighted (counting
	* correspondences) can be achieved by setting all confidences to the same value in input mapping.
	* Performs a search where each serch node is 1:1 alignment. Setings related to one-to-one properties
	* do not at all affect this method. 
	*/
	public static final int METHOD_OPTIMAL_HUN = 3;	
	
	
	/**
	* Greedy method that generates an minimal hitting set over all pattern-based conflicts by
	* iteratively deleting the correspondence that is involved in the highest number of conflicts. If
	* there are several those correspondences, it removes the correspondence with lowest confidence.
	* Can only be used together with efficient reasoning. It does not compute a local nor a global diagnosis,
	* however, the results is a diagnosis. 
	*/
	public static final int METHOD_GREEDY_MINIMIZE = 4;	
	
	/**
	* Only concepts correspondences.
	*/
	public static final int ENTITIES_ONLYCONCEPTS = 10;
	
	/**
	* Concept and property correspondences. 
	*/
	public static final int ENTITIES_CONCEPTSPROPERTIES = 20;
	
	/**
	* Pairwise incomplete conflict reasoning.
	*/
	public static final int REASONING_EFFICIENT = 100;
	
	/**
	* Complete reasoning. Results is complete even though incomplete reasoning is used to a large degree
	* to increase efficiency in a tricky way. In some extremly rare theoretical cases (non has been observed yet!)
	* it might happen that brute force approach is more efficient.
	*/
	public static final int REASONING_COMPLETE = 200;

	/**
	* Brute force complete reasoning. Only uses reasoning in the merged ontology, not taking
	* advantage of efficient reasoning at all.  
	*/
	public static final int REASONING_BRUTEFORCE = 300;

	/**
	* Brute force complete reasoning. Only uses reasoning in the merged ontology, not taking
	* advantage of efficient reasoning at all.  
	*/
	public static final int REASONING_D = 400;		
	
	/**
	* The namespace for additionally created entities (domain and range concepts). 
	*/
	public static final String ALCOMO_URL = "http://ki.informatik.uni-mannheim.de/alcomo";
	
	// private variables
	
	private HashMap<String, Integer> params;
	
	private Mapping nonReferingInputMapping;
	private Mapping extractedMapping;
	private Mapping discardedMapping;
	
	private AlcomoExtraction algorithm;

	private boolean partOfAsDisjointness = false;
	
	/**
	* Constructs an extraction problem and specifies the way it will be solved.
	* 
	* @param valueX Value of the parameter {@link #METHOD}, {@link #ENTITIES}, or {@link #REASONING}.
	* @param valueY Value of the parameter {@link #METHOD}, {@link #ENTITIES}, or {@link #REASONING}.
	* @param valueZ Value of the parameter {@link #METHOD}, {@link #ENTITIES}, or {@link #REASONING}.
	* @throws PCFException Thrown if invalid values for the parameters are chosen or one required parameter has not been set.
	*/
	public ExtractionProblem(int valueX, int valueY, int valueZ) throws PCFException {
		this.inputMapping = null;
		this.algorithm = null;
		this.sourceOntology = null;
		this.targetOntology = null;
		this.params = new HashMap<String, Integer>();
		int valueMethod = 0;
		int valueEntities = 0;
		int valueReasoning = 0;
		// reassign values to params
		if (valueX >= 1 && valueX < 10) valueMethod = valueX;
		if (valueY >= 1 && valueY < 10) valueMethod = valueY;
		if (valueZ >= 1 && valueZ < 10) valueMethod = valueZ;
		if (valueX >= 10 && valueX < 100) valueEntities = valueX;
		if (valueY >= 10 && valueY < 100) valueEntities = valueY;
		if (valueZ >= 10 && valueZ < 100) valueEntities = valueZ;
		if (valueX >= 100 && valueX < 1000) valueReasoning = valueX;
		if (valueY >= 100 && valueY < 1000) valueReasoning = valueY;
		if (valueZ >= 100 && valueZ < 1000) valueReasoning = valueZ;
		
		this.params.put(METHOD, valueMethod);
		this.params.put(ENTITIES, valueEntities);
		this.params.put(REASONING, valueReasoning);	
		this.solved = false;
		this.initialized = false;
		this.checkParams();	
		// used to get rid of logging messages sent by some external libraries
		Tools.deactivateLogger();		
	}
	
	/**
	* Constructs an extraction problem with standard settings (concepts and props, optimal, complete).
	* 
	*/
	public ExtractionProblem() throws PCFException {
		this(METHOD_OPTIMAL, ENTITIES_CONCEPTSPROPERTIES, REASONING_COMPLETE);	
	}

	/**
	* Returns the value of a parameter.
	* 
	* @param key The parameter name.
	* @return The value of the parameter.
	*/
	public int getParam(String key) {
		return this.params.get(key);
	}
	
	public boolean interpretPartOfAsDisjointness() {
		return this.partOfAsDisjointness;
	}
	

	/**
	* Calling this method adds disjointness axioms for each part-of axiom. More precisely:
	* for each A < Ex.part-of.B the method adds the axiom A < non B to the ontology.
	* This method is especially designed for medical contexts, but should also work
	* for other domains. Each relation that contains a *part+of* pattern 
	* will be accounted as a part of relation. 
	*
	*/
	public void partOfAsDisjointness() {
		this.partOfAsDisjointness  = true;		
	}
	
	/**
	* Solves the extraction problem.
	*
	* @throws AlcomoException Thrown if some parameters are not set correctly or
	* some problems occur during reasoning.
	* @return True, if the chosen algorithm terminated correctly (without interruption);
	* false otherwise.
	*/
	public boolean solve() throws AlcomoException {
		super.solve();
		if (this.params.containsKey(CSEXACTNESS)) { adjustProbabilities(); }
		if (params.get(METHOD) == METHOD_GREEDY) {
			algorithm = new Greedy(this);
		}
		else if (params.get(METHOD) == METHOD_OPTIMAL) {
			algorithm = new AStarSearch(this);
		}
		else if (params.get(METHOD) == METHOD_OPTIMAL_HUN) {
			algorithm = new HungarianSearch(this);
		}
		else if (params.get(METHOD) == METHOD_GREEDY_MINIMIZE) {
			algorithm = new GreedyMinimize(this);
		}	
		this.algorithm.setSourceOntology(this.sourceOntology);
		this.algorithm.setTargetOntology(this.targetOntology);
		this.algorithm.setMapping(this.inputMapping);
		
		// DataStorage.mirror.startClock();
		AlcomoLogger.takeTime("running the algorithm");
		this.algorithm.run();
		AlcomoLogger.takeTime("algorithm finished");
		// DataStorage.mirror.takeTime();
		
		this.extractedMapping = this.algorithm.getActiveMapping();
		this.discardedMapping = this.algorithm.getInactiveMapping();
		return this.algorithm.terminatedCorrectly();
	}
	
	/**
	* Checks with complete reasoning wether the extracted mapping is coherent. 
	* Requires to solve the extraction problem first.
	* 
	* @return True if extracted mapping is coherent, false otherwise.
	 * @throws AlcomoException 
	*/
	public boolean isCoherentExtraction() throws AlcomoException {
		if (!this.solved) {
			throw new PCFException(
				PCFException.INVALID_OPERATION,
				"Coherence of extraction can only be checked after solving the problem"
			);
		}
		CompleteReasoner reasoner = new CompleteReasoner(this.sourceOntology, this.targetOntology, this);
		return (!(reasoner.isConflictSet(this.extractedMapping)));
	}	
	
	/**
	* @deprecated Will be removed in following AlcomoVersions  (was used in javasript based manual evaluation).
	* 
	* Computes the set of all conflict sets in the efficient mode. This method will find most
	* pairs of conflicting correspondences, conflict sets of higher cardinality are not yet
	* supported.
	* 
	* @return A set of conflict sets, where each conflict set is a set of correspondences.
	*/
	public Set<ConflictPair> getConflicSets() {
		ConflictSearch conflictSearch = new ConflictSearch(this);
		conflictSearch.setSourceOntology(this.sourceOntology);
		conflictSearch.setTargetOntology(this.targetOntology);
		conflictSearch.setMapping(this.inputMapping);		
		conflictSearch.run();
		return conflictSearch.getConflictSets();
	}
	
	
	/**
	* Returns the mapping extracted as results of solving this extraction problem.
	* 
	* @return The extracted mapping.
	* @throws PCFException Thrown if the problem has not been solved yet.
	*/
	public Mapping getExtractedMapping() throws PCFException {
		if (this.solved) { return this.extractedMapping; }
		else {
			throw new PCFException(
					PCFException.INVALID_OPERATION,
					"problem not yet solved, there is no extracted mapping"
			);
			
		}
	}
	

	public Mapping getNonReferingInputMapping() throws PCFException {
		if (this.initialized) { return this.nonReferingInputMapping; }
		else {
			throw new PCFException(
					PCFException.INVALID_OPERATION,
					"problem not yet initialized, there is no extracted mapping"
			);
		}
	}
	
	public Mapping getReferingInputMapping() throws PCFException {
		if (this.initialized) { return this.inputMapping; }
		else {
			throw new PCFException(
					PCFException.INVALID_OPERATION,
					"problem not yet initialized, there is no extracted mapping"
			);
		}
	}	

	/**
	* Returns the mapping discarded as results of solving this extraction problem.
	* 
	* @return The discarded mapping.
	* @throws PCFException Thrown if the problem has not been solved yet.
	*/	
	public Mapping getDiscardedMapping() throws PCFException {
		if (this.solved) { return this.discardedMapping; }
		else {
			throw new PCFException(
					PCFException.INVALID_OPERATION,
					"problem not yet solved, there is no discarded mapping"
			);	
		}
	}	
	
	public void init() throws AlcomoException {
		super.init();
		// load both ontologies for connecting with the mapping and reasoning
		// DataStorage.mirror.startClock();
		AlcomoLogger.takeTime("load source ontology");
		this.sourceOntology.init(this);
		// DataStorage.mirror.takeTime();
		AlcomoLogger.takeTime("load target ontology");
		this.targetOntology.init(this);
		// DataStorage.mirror.takeTime();
		this.nonReferingInputMapping = this.inputMapping.bind(this.sourceOntology, this.targetOntology);

	}
	
	// ***************************************
	//        private playground
	// ***************************************

	private void adjustProbabilities() throws CorrespondenceException {
		EfficientReasoner conflictReasoner = new EfficientReasoner(this.sourceOntology, this.targetOntology, this);
		for (Correspondence c : this.inputMapping.getCorrespondences()) {
			double sensitivity = conflictReasoner.estimateConflictSensitivity(c, this.params.get(CSEXACTNESS));
			c.setConfidence(c.getConfidence() * sensitivity);
		}
	}
	
	private void checkParams() throws PCFException {
		// values specified at all?
		if ((this.params.get(METHOD) == null) || (this.params.get(ENTITIES) == null) || (this.params.get(REASONING) == null)) {
			throw new PCFException(
					PCFException.MISSING_PARAM,
					"at least on of " + METHOD + ", " + ENTITIES + ", " + REASONING + " has not been specified"
			);
		}
		// valid values chosen ?
		if ((this.params.get(METHOD) != METHOD_GREEDY) && (this.params.get(METHOD) != METHOD_OPTIMAL) && (this.params.get(METHOD) != METHOD_OPTIMAL_HUN)  && (this.params.get(METHOD) != METHOD_GREEDY_MINIMIZE)) {
			throw new PCFException(
					PCFException.INVALID_PARAM,
					"value of " + METHOD + " is invalid"
			);			
		}
		if ((this.params.get(ENTITIES) != ENTITIES_ONLYCONCEPTS) && (this.params.get(ENTITIES) != ENTITIES_CONCEPTSPROPERTIES)) {
			throw new PCFException(
					PCFException.INVALID_PARAM,
					"value of " + ENTITIES + " is invalid"
			);			
		}
		if ((this.params.get(REASONING) != REASONING_COMPLETE) && (this.params.get(REASONING) != REASONING_EFFICIENT) && (this.params.get(REASONING) != REASONING_BRUTEFORCE) && (this.params.get(REASONING) != REASONING_D)) {
			throw new PCFException(
					PCFException.INVALID_PARAM,
					"value of " + REASONING + "invalid"
			);			
		}
		// checking combinations .... will be completed soon!
		int paramCode = this.params.get(METHOD) + this.params.get(ENTITIES) + this.params.get(REASONING);
		switch (paramCode) {
		case 111:
			break;
		case 112:
			break;
		case 113:
			break;	
		case 121:
			break;
		case 122:
			break;
		case 123:
			break;
		case 211:
			break;
		case 212:
			break;
		case 213:
			break;
		case 221:
			break;
		case 222:
			break;
		case 223:
			break;
		case 311:
			break;
		case 312:
			break;
		case 321:
			break;
		case 322:
			break;
		case 411:
			break;
		case 114:
			break;
		case 124:
			break;
		default:
			throw new PCFException(
					PCFException.INVALID_PARAM_COMBINATION,
					"actually only a few combinations of are allowed"
			);
		}
	}

	public void equalizeBoundedMappingTrust() {
		try {
			this.inputMapping.normalize(1.0);
		}
		catch (AlcomoException e) {
			// can never happen, we have to catch the Exception because of bad programming style 
		}
	}
	
	public HashSet<String> getEntities() throws PCFException {
		HashSet<String> entityURIs = new HashSet<String>();
		if (this.initialized) {
			for (OWLClass c : this.sourceOntology.getClasses()) {
				entityURIs.add(c.getIRI().toURI().toString());
			}
			for (OWLClass c : this.targetOntology.getClasses()) {
				entityURIs.add(c.getIRI().toURI().toString());
			}
		}
		else {
			throw new PCFException(
					PCFException.INVALID_OPERATION,
					"first init the problem before retrieving classes"
			);			
		}
		return entityURIs;
	}

	/**
	* Returns the uris (as strings) of all unsatisfiable concepts of both local ontologies. This comprises
	* also the additionally introduced concepts used for reasoning over properties.
	* 
	*  @return The set of the URIs of all locally unsatisfiable concepts.
	 * @throws AlcomoException 
	*/
	public HashSet<String> getLocalUnsatisfiableEntities() throws AlcomoException {
		if (this.initialized) {
			HashSet<String> localUnsatisfiableEntityURIs = new HashSet<String>();
			for (OWLClass c : this.sourceOntology.getUnsatisfiableClasses()) {
				localUnsatisfiableEntityURIs.add(c.getIRI().toURI().toString());
			}
			for (OWLClass c : this.targetOntology.getUnsatisfiableClasses()) {
				localUnsatisfiableEntityURIs.add(c.getIRI().toURI().toString());
			}
			return localUnsatisfiableEntityURIs;
		}
		else {
			throw new PCFException(
				PCFException.INVALID_OPERATION,
				"first init the problem before computing local unsatisfiable classes"
			);
		}
	}
	
	/**
	* Returns the uris (as strings) of all unsatisfiable concepts of ther merged ontology based on the currecnt input mapping.
	* This comprises also the additionally introduced concepts used for reasoning over properties.
	* 
	*  @return The set of the URIs of all merged unsatisfiable concepts.
	*/
	public HashSet<String> getMergedUnsatisfiableEntities() throws AlcomoException {
		if (this.initialized) {
			HashSet<String> mergedUnsatisfiableEntityURIs = new HashSet<String>();
			CompleteReasoner cr = new CompleteReasoner(this.sourceOntology, this.targetOntology, this);
			for (OWLClass c : cr.getUnsatisfiableClasses(this.inputMapping)) {
				mergedUnsatisfiableEntityURIs.add(c.getIRI().toURI().toString());
			}
			return mergedUnsatisfiableEntityURIs;	
		}
		else {
			throw new PCFException(
					PCFException.INVALID_OPERATION,
					"first init the problem before computing merged unsatisfiable classes"
			);
		}
	}

	public MergedOntology getMergedOntology() throws AlcomoException {
		if (this.initialized) {
			CompleteReasoner cr = new CompleteReasoner(this.sourceOntology, this.targetOntology, this);
			MergedOntology mo = cr.getMergedOntology();
			return mo;
		}
		else {
			throw new PCFException(
					PCFException.INVALID_OPERATION,
					"first init the problem before computing merged ontology"
			);
		}
		
	}

}




