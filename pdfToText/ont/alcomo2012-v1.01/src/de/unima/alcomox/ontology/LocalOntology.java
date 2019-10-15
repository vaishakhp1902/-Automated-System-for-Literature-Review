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

package de.unima.alcomox.ontology;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


import org.semanticweb.owlapi.apibinding.OWLManager;

import org.semanticweb.owlapi.model.*;


import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.Settings;
import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.exceptions.OntologyException;
import de.unima.alcomox.ontology.extowlapi.NominalAxiomDetector;
import de.unima.alcomox.ontology.extowlapi.NominalAxiomTranscriber;
import de.unima.alcomox.util.AlcomoLogger;
import de.unima.alcomox.util.DataStorage;
import de.unima.alcomox.util.Tools;


/**
* This class represents a local possibly extended ontology.
* Two of these extandable ontologies are used for the pairwise efficient but incomplete reasoning.
*/
public class LocalOntology extends AlcomoOntology {
	
	
	private boolean initialized = false; 

	public static int xxx = 1;
	
	public static HashSet<String> rangeIds = new HashSet<String>();
	public static long loadedOntCounter = 0;
	
	
	// used for getting a random entity, fixed seed to make things reproducible
	private final long SEED = 1000l;
	private Random rand;
	
	// all concept uris and property uris are stored in an array
	// for getting them via an index, this index is only used internally
	private ArrayList<String> indexedConceptEntityUris;
	private ArrayList<String> indexedPropertyEntityUris;
	
	// used for easy access from uri to entity
	private HashMap<String, Entity> entities;
	
	// used for increasing enumeration of domain /range extensions
	private int extCounter = 0;
	
	protected int myOntId;
	
	private Set<OWLClass> unsatisfiableClasses;

	
	/**
	* Constructs an ontology, thats a kind of wrapper for an owl ontology, by reading a 
	* file specified by a filepath or url.
	* 
	* @param filepathOrUrl The string representation of the filepath or URL.
	* @throws AlcomoException Thrown if filepath or url do not refer or the file
	* is somehow corrupted / not valid.
	*/
	public LocalOntology(String filepathOrUrl)  throws AlcomoException  {
		super();
		loadedOntCounter++;
		this.myOntId = (int) loadedOntCounter % 1000;
		Tools.deactivateLogger();	

		URI physicalURI = null;
    	File ontologyFile = new File(filepathOrUrl);

		if (!(ontologyFile.exists())) {
			try { physicalURI = new URI(filepathOrUrl); }
			catch (URISyntaxException e) {
				throw new OntologyException(OntologyException.IO_ERROR, "ontology at filepath or URL " + filepathOrUrl + " does not exist");
			}

		}
		else {
			physicalURI = ontologyFile.toURI();
		}
		
		try {
			this.manager = OWLManager.createOWLOntologyManager();
			IRI ontologyIRI = IRI.create(physicalURI);
			this.ontology = manager.loadOntology(ontologyIRI);
			this.setFactory();
			this.unsatisfiableClasses = new HashSet<OWLClass>();
			this.log = new AlcomoLogger(this.getClass());
			this.rand = new Random(SEED);
			this.extCounter = 0;
		}
		catch (OWLOntologyCreationException e) {
			throw new OntologyException(
					OntologyException.INVALID_FORMAT, 
					"ontology file at " + filepathOrUrl + " could not sucessfully be parsed", 
					e
			);
		}
		catch (IllegalArgumentException e) {
			throw new OntologyException(
					OntologyException.INVALID_FORMAT, 
					"ontology file at " + filepathOrUrl + " could not sucessfully be parsed", 
					e
			);
		}

		
	}
	
	/**
	* Resolves the uri reference and returns the entity that the uri refers to.
	* 
	* @param entityUri The uri of the entity as string.
	* @return Entity refered to by entityUri
	* @throws OntologyException Thrown if entityUri does not refer to some
	* named entity in the ontology.
	*/
	public Entity getEntityByUri(String entityUri) throws OntologyException {
		if (this.entities.containsKey(entityUri)) {
			return this.entities.get(entityUri);
		}
		else {
			throw new OntologyException(
				OntologyException.REFERENCE_MISSING,
				"entity with URI " + entityUri + " not available"
			);			
		}
	}
	
	/**
	* Inits this extandable ontology and binds it to the extraction problem.
	* Initalising an extandable ontology results in extending the ontology and
	* building up some indices. Finally the extended ontology is classified.
	*  
	* @param extractionProblem 
	* @throws OntologyException 
	* @throws AlcomoException
	*/
	public void init(ExtractionProblem extractionProblem) throws OntologyException {
		if (this.initialized) { return; }
		DataStorage ds = new DataStorage();

		
		this.log.infoS("init " + this.toShortString().toLowerCase() + " ...");
		
		this.extractionProblem = extractionProblem;
		
		this.entities = new HashMap<String, Entity>();
		this.indexedConceptEntityUris = new ArrayList<String>();
		this.indexedPropertyEntityUris = new ArrayList<String>();
		
		
		
		// remove individuals
		if (Settings.REMOVE_INDIVIDUALS) {
			try {
				this.transcribeNominalAxioms();
			}
			catch (Exception e) {
				this.log.warnS("could not transcribe axioms containing nominals");
				this.log.warnS("ontology looses some terminological axioms without substitution");
				this.log.warnS("caught exception: " +  e);
			}
			this.deleteABox();
		}
		
		
		Set<OWLClass> referencedClasses =  this.ontology.getClassesInSignature();
		
		
		
		/*
		// create complement of each class in case its an IOntology
		if (this instanceof IOntology) {
			System.out.println("before adding complements ... ");
			for (OWLClass concept : referencedClasses) {
				if (!(concept.equals(this.NOTHING))) {
					((IOntology)this).createComplement(concept);
				}
			}	
		}
		*/


		
		for (OWLClass someClass : referencedClasses) {
			ds.createDataRow();
			ds.addValue(someClass.getIRI().toURI().toString());
			if ((!(someClass.getIRI().toURI().equals(THING.getIRI().toURI()))) && (!(someClass.getIRI().toURI().equals(NOTHING.getIRI().toURI())))) {
				this.indexedConceptEntityUris.add(someClass.getIRI().toURI().toString());
				this.entities.put(someClass.getIRI().toURI().toString(), EntityFactory.createConceptEntity(someClass));
				
			}
		}
		
		

		
		// go for the role extension
		
		int id;
		if (this.extractionProblem.getParam(ExtractionProblem.ENTITIES) == ExtractionProblem.ENTITIES_CONCEPTSPROPERTIES) {
			// create entities for object properties
			Set<OWLObjectProperty> referencedObjectProperties = new HashSet<OWLObjectProperty>();
			referencedObjectProperties.addAll(this.ontology.getObjectPropertiesInSignature());
			for (OWLObjectProperty someProperty : referencedObjectProperties) {
				id = this.getNextExtId();
				OWLClass domain;
				OWLClass range = null;
				domain = this.createObjectPropertyDomain(someProperty, id);
				if (Settings.PROPERTY_RANGE_EXTENSION) {
					range = this.createObjectPropertyRange(someProperty, id);		
				}
				this.indexedPropertyEntityUris.add(someProperty.getIRI().toURI().toString());
				this.entities.put(someProperty.getIRI().toURI().toString(), EntityFactory.createObjectPropertyEntity(someProperty, domain, range));
			}
			// create entities for data properties
			Set<OWLDataProperty> referencedDataProperties =  new HashSet<OWLDataProperty>();
			referencedDataProperties.addAll(this.ontology.getDataPropertiesInSignature());
			for (OWLDataProperty someProperty : referencedDataProperties) {
				id = this.getNextExtId();
				OWLClass domain = this.createDataPropertyDomain(someProperty, id);
				this.indexedPropertyEntityUris.add(someProperty.getIRI().toURI().toString());
				this.entities.put(someProperty.getIRI().toURI().toString(), EntityFactory.createDataPropertyEntity(someProperty, domain));
			}		
		}
		


		// Tools.writeToFile("E://test" + yyy + ".txt", ds.toString());
		xxx++;
		// do the classification
		String reasonerDesc = null;
		if (Settings.BLACKBOX_REASONER == Settings.BlackBoxReasoner.PELLET) {
			reasonerDesc = "Pellet";
		}
		else if (Settings.BLACKBOX_REASONER == Settings.BlackBoxReasoner.HERMIT){
			reasonerDesc = "Hermit";
		}
		this.log.infoS("classify (with " + reasonerDesc + ")");
		this.initReasoner();	
		this.computeUnsatisfiableClasses();	
		this.log.infoS("... done (init)");
		this.initialized = true;
	
	}
	
	private void transcribeNominalAxioms() throws Exception {
		// init the detector
		NominalAxiomDetector nominalDetector = new NominalAxiomDetector(new HashSet<OWLEntity>());
		// init the transcriber
		NominalAxiomTranscriber nominalTranscriber = new NominalAxiomTranscriber(new HashSet<OWLEntity>());
		nominalTranscriber.setLocalOntology(this);
		nominalTranscriber.setOntologyCore(this.ontology);
		nominalTranscriber.setFactory(this.factory);
		// ... keep track of the things to be changed
		Set<OWLAxiom> axiomsToBeRemoved = new HashSet<OWLAxiom>();
		Set<OWLAxiom> axiomsToBeAdded = new HashSet<OWLAxiom>();
		Set<OWLAxiom> axioms = this.ontology.getAxioms();
		for (OWLAxiom axiom : axioms) {
			//System.out.println("AXIOM:" + axiom);
			axiom.accept(nominalDetector);
			if (nominalDetector.detectedNominal()) {
				axiomsToBeRemoved.add(axiom);
				nominalDetector.reset();
				log.infoS("detected axiom using nominal: " + axiom);
				ArrayList<OWLAxiom> rebuiltAxioms = nominalTranscriber.rebuild(axiom);
				log.infoW("[transcribed as: ");
				for (OWLAxiom rebuiltAxiom : rebuiltAxioms) {
					axiomsToBeAdded.add(rebuiltAxiom);
					log.infoW(rebuiltAxiom.toString() + " ");
				}
				log.infoW("]\n");
			}
		}
		// first add all axioms replacing nominal-axioms
		for (OWLAxiom axiomToBeAdded : axiomsToBeAdded) {
			 this.manager.applyChange(new AddAxiom(this.ontology, axiomToBeAdded));
		}
		// then remove the nominal-axioms 
		for (OWLAxiom axiomToBeRemoved : axiomsToBeRemoved) {
			 this.manager.applyChange(new RemoveAxiom(this.ontology, axiomToBeRemoved));
		}
	}
	
	
	private void deleteABox() throws OntologyException {	
		this.log.infoW("[try to remove individuals to avoid inconsistencies ... ");
		OWLEntityRemover remover = new OWLEntityRemover(this.manager, Collections.singleton(this.ontology));
		int indCounter = 0;
		for(OWLNamedIndividual individual : this.ontology.getIndividualsInSignature()) {
			indCounter++;
			try {
				individual.accept(remover);
			}
			catch(Exception e) {
				// TODO remoce try-catch after some successful runs
				System.out.println("Damned, it failed ...");
			}
		}
		if (this.ontology.getIndividualsInSignature().size() > 0) {
			try {
				this.manager.applyChanges(remover.getChanges());
			}
			catch (OWLOntologyChangeException e) {
				throw new OntologyException(
						OntologyException.MODIFICATION_FAILED,
						"could not remove individuals from ontology " + this.ontology.toString()
				);
			}
			this.log.infoW("removed " +  indCounter + " individuals]\n");
		}
		else {
			this.log.infoW("ontology contained no individuals]\n");
		}
	}	
	

	
	// *******************************************
	//     random entities
	// *******************************************
	
	public Entity getRandomEntity() {
		int index = this.rand.nextInt(this.indexedConceptEntityUris.size() + this.indexedPropertyEntityUris.size());
		if (index < this.indexedConceptEntityUris.size()) {
			return this.getRandomEntity(true);
		}
		else {
			return this.getRandomEntity(false);
		}
	}
	
	public Entity getRandomEntity(boolean isConcept) {
		if (isConcept) {
			int index = this.rand.nextInt(this.indexedConceptEntityUris.size());
			return this.entities.get(this.indexedConceptEntityUris.get(index));	
		}
		else {
			int index = this.rand.nextInt(this.indexedPropertyEntityUris.size());
			return this.entities.get(this.indexedPropertyEntityUris.get(index));	
		}
	}	
	
	// disjointness
	public Set<String> getReferencedClassUris() {
		HashSet<String> referencedClassUris = new HashSet<String>();
		referencedClassUris.addAll(this.indexedConceptEntityUris);
		return referencedClassUris;
	}
	
	
	
	

	
	public boolean isUnsatisfiable(OWLClass c) {
		return this.unsatisfiableClasses.contains(c);
	}
	
	// **************************
	// *** PRIVATE PLAYGROUND ***
	// **************************	
	
	private OWLClass createObjectPropertyDomain(OWLObjectProperty property, int id) throws OntologyException {
		String fragment = property.getIRI().toURI().getFragment();
		OWLClass domain = this.factory.getOWLClass(IRI.create(ExtractionProblem.ALCOMO_URL + "#" + fragment + "_" + id + "_" + this.myOntId + "_DOMAIN"));
		OWLClassExpression domainDescription= factory.getOWLObjectSomeValuesFrom(property, THING);
		OWLSubClassOfAxiom subclassAxiom = this.factory.getOWLSubClassOfAxiom(domain, domainDescription);
		AddAxiom addAx = new AddAxiom(this.ontology, subclassAxiom);
		try { this.manager.applyChange(addAx); } 
		catch (OWLOntologyChangeException e) {
			throw new OntologyException(
				OntologyException.MODIFICATION_FAILED,
				"could not add domain class for object property " + property 
			); 
		}
		return domain;
	}

	private OWLClass createObjectPropertyRange(OWLObjectProperty property, int id) throws OntologyException {
		
		String fragment = property.getIRI().toURI().getFragment();
		OWLClass range = this.factory.getOWLClass(IRI.create(ExtractionProblem.ALCOMO_URL + "#" + fragment + "_" + id + "_" + this.myOntId + "_RANGE"));
		OWLObjectProperty invProperty = this.factory.getOWLObjectProperty(IRI.create(ExtractionProblem.ALCOMO_URL + "#" + fragment + "_" + id + "_" + this.myOntId + "_INV"));
		OWLInverseObjectPropertiesAxiom inverseAxiom = this.factory.getOWLInverseObjectPropertiesAxiom(property, invProperty);
		AddAxiom addAx1 = new AddAxiom(this.ontology, inverseAxiom);
		OWLClassExpression rangeDescription= factory.getOWLObjectSomeValuesFrom(invProperty, THING);
		OWLSubClassOfAxiom subclassAxiom = this.factory.getOWLSubClassOfAxiom(range, rangeDescription);
		AddAxiom addAx2 = new AddAxiom(this.ontology, subclassAxiom);
		try {
			this.manager.applyChange(addAx1);
			this.manager.applyChange(addAx2);
		} 
		catch (OWLOntologyChangeException e) {
			throw new OntologyException(
				OntologyException.MODIFICATION_FAILED,
				"could not add domain class for object property " + property 
			); 
		}
		return range;
	}	

	private OWLClass createDataPropertyDomain(OWLDataProperty property, int id) throws OntologyException {
		// property.getSuperProperties(arg0)
		String fragment = property.getIRI().toURI().getFragment();
		OWLClass domain = this.factory.getOWLClass(IRI.create(ExtractionProblem.ALCOMO_URL + "#" + fragment + "_" + id + "_DOMAIN"));
		OWLDatatype dataType =  determineDatatypeOfProperty(property);
		// the following might happen if something extension (e.g. foaf) is not supported by OWL API
		// or if the ontology is in some way buggy
		if (dataType == null) {

			throw new OntologyException(
				OntologyException.MODIFICATION_FAILED,
				"could not obtain datatype for datatype property " + property + " (something not yet implemented, probably)"
			); 
		}
		OWLClassExpression domainDescription= factory.getOWLDataSomeValuesFrom(property, dataType);		
		OWLSubClassOfAxiom subclassAxiom = this.factory.getOWLSubClassOfAxiom(domain, domainDescription);
		AddAxiom addAx = new AddAxiom(this.ontology, subclassAxiom);
		try { this.manager.applyChange(addAx); } 
		catch (OWLOntologyChangeException e) {
			throw new OntologyException(
				OntologyException.MODIFICATION_FAILED,
				"could not add domain class for data property " + property 
			); 
		}
		return domain;
	}

	private OWLDatatype determineDatatypeOfProperty(OWLDataPropertyExpression property ) {
		OWLDatatype dataType = null;
		Set<OWLDataRange> ranges = property.getRanges(this.ontology);
		for (OWLDataRange dr : ranges) {
			if (dr instanceof OWLDatatype) { dataType = (OWLDatatype)dr; }
			else if (dr instanceof OWLDataOneOf) {
				OWLDataOneOf dataOneOf = (OWLDataOneOf)dr;
				Set<OWLLiteral> constants = dataOneOf.getValues();
				for (OWLLiteral constant : constants) {
					dataType = constant.getDatatype();
					/*
					if (constant.isTyped()) {
						dataType = constant.asOWLTypedConstant().getDataType();
					}
					*/
				}
			}				
			if (dataType != null) { break; }
		}
		if (ranges.isEmpty()) {
			Set<OWLDataPropertyExpression> parentProps = property.getSuperProperties(this.ontology);
			if (parentProps.size() > 0) {
				for (OWLDataPropertyExpression parentProp : parentProps) {
					return determineDatatypeOfProperty(parentProp);
				}
			}
			this.log.infoS("type of dataproperty '" + property + "' in '" + this.toShortString() + "' not specified");
			dataType = factory.getOWLDatatype(XSDVocabulary.ANY_TYPE.getIRI());
		}
		return dataType;
	}	
	
	private void computeUnsatisfiableClasses() throws OntologyException {
		Set<OWLClass> unsatisfiableClasses = this.getUnsatisfiableClasses();
		if (unsatisfiableClasses.size() > 0) {
			this.log.warnS("detected " + unsatisfiableClasses.size() + " unsatisfiable class(es) in input ontology " + this.toShortString());
			for (OWLClass c : unsatisfiableClasses) {
				this.log.warnS("unsatisfiable classs:  " + c);
				this.unsatisfiableClasses.add(c);
			}
			this.log.warnS("reasoning might by negatively affected in this system version");		
		}
	}
	
	public int getNextExtId() {
		this.extCounter++;
		return this.extCounter;
	}
	
	
}
