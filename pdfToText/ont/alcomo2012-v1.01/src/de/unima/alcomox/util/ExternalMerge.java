package de.unima.alcomox.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.Settings;
import de.unima.alcomox.Settings.BlackBoxReasoner;
import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.exceptions.MappingException;
import de.unima.alcomox.exceptions.OntologyException;
import de.unima.alcomox.exceptions.PCFException;
import de.unima.alcomox.mapping.Correspondence;
import de.unima.alcomox.mapping.Mapping;
import de.unima.alcomox.mapping.SemanticRelation;
import de.unima.alcomox.ontology.Entity;
import de.unima.alcomox.ontology.EntityFactory;
import de.unima.alcomox.ontology.IOntology;

public class ExternalMerge {
	
	
	protected OWLOntology sourceOntology;
	protected OWLOntology targetOntology;
	
	protected OWLOntology mergedOntology;
	
	protected OWLOntologyManager manager;
	protected OWLDataFactory factory;
	
	protected OWLReasoner reasoner;
	
	
	
	public static void main(String[] args) throws OWLOntologyCreationException, PCFException, AlcomoException {
		
		String sourcePath  = "testdata/biomed/oaei2012_FMA_small_overlapping_nci.owl";
		String targetPath  = "testdata/biomed/oaei2012_NCI_small_overlapping_fma.owl";
		String mappingPath = "testdata/temp/hertuda_small_fma2nci.rdf";
		// String myMappingPath = "testdata/temp/hertuda-selfmade-SplitandJoin.rdf";
		
		// String ernestoMappingPath = "testdata/temp/hertuda_small_fma2nci_repaired_with_Alcomo_Hermit_SplitandJoin.rdf";
		
		
		// String ernestoMappingPath = "testdata/temp/hertuda_small_fma2nci_repaired_with_Alcomo_Hermit_onlySplit.rdf";
		
		//ExternalMerge em = new ExternalMerge(sourcePath, targetPath, mappingPath);
		
		
		

		
		run(sourcePath, targetPath, mappingPath);
		
	}
	
	
	private static void run(String ont1Path, String ont2Path, String alignPath) throws AlcomoException, MappingException, PCFException {
		AlcomoLogger.resetTimer();
		IOntology sourceOnt = new IOntology(ont1Path);
		IOntology targetOnt = new IOntology(ont2Path);
		

		Mapping mapping = new Mapping(alignPath);
		System.out.println("Mapping size = " + mapping.size());
		
		mapping.splitToSubsumptionCorrespondences();
		
		ExtractionProblem ep = new ExtractionProblem(
				ExtractionProblem.ENTITIES_CONCEPTSPROPERTIES,
				ExtractionProblem.METHOD_GREEDY,
				ExtractionProblem.REASONING_EFFICIENT
		);
		

		ep.bindSourceOntology(sourceOnt);
		ep.bindTargetOntology(targetOnt);
		
		ep.bindMapping(mapping);
		
		// solve the problem
		ep.solve();
	

		
		
		Mapping extracted = ep.getExtractedMapping();
        
	       

		extracted.joinToEquivalence();           
		mapping.joinToEquivalence();
		ep.getDiscardedMapping().joinToEquivalence();
		
			
		
		
		System.out.println("META of extracted:\n" + extracted.getMetaDescription());
		
		
		// extracted.joinToEquivalence(); 
		
		// System.out.println("META2 of extracted:\n" + extracted.getMetaDescription());
		
		AlcomoLogger.printTimestamps();
	}	
	
	
	public ExternalMerge(String sourcePath, String targetPath, String mappingPath) throws OntologyException, OWLOntologyCreationException, MappingException {
		
		this.manager = OWLManager.createOWLOntologyManager();
		this.factory = this.manager.getOWLDataFactory();
		
		this.sourceOntology = this.loadOntology(sourcePath);
		this.targetOntology = this.loadOntology(targetPath);
		
		Set<OWLAxiom> sourceAxioms = sourceOntology.getAxioms();
		System.out.println("source axiom count = " + sourceAxioms.size());
		Set<OWLAxiom> targetAxioms = targetOntology.getAxioms();
		System.out.println("target axiom count = " + targetAxioms.size());
		Set<OWLAxiom> mergedAxioms = new HashSet<OWLAxiom>();
		mergedAxioms.addAll(sourceAxioms);
		mergedAxioms.addAll(targetAxioms);
		
		
		Mapping mapping = new Mapping(mappingPath);
		
		Set<OWLAxiom> mappingAxioms = this.convertToAxioms(mapping);
		System.out.println("mapping axiom count = " + mappingAxioms.size());
		mergedAxioms.addAll(mappingAxioms);
		
		this.mergedOntology = this.manager.createOntology(mergedAxioms);
		
		this.initReasoner(mergedOntology);
		
		for (OWLClass c : this.reasoner.getUnsatisfiableClasses()) {
			System.out.println(">>> " + c);
		}
		
		
		
	}
	
	
	private void initReasoner(OWLOntology ontology) throws OntologyException {
		this.initReasonerWithoutClassification(ontology);
		if (Settings.PRE_CLASSIFY) {
			try {
				reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY); 
			}
			catch(InconsistentOntologyException e) {
				// TODO (HIGH priority) its pretty unclear why this exception is thrown this sometimes happens
				// this bug has high priority
				throw new OntologyException(
					OntologyException.REASONING_ERROR,
					"Ontology is inconsistent, cannot do any reasoning\n" +
					"Check in settings wether individuals will be removed prior reasoning!",
					e
				);
				
				
			}
		}
	}
	
	private void initReasonerWithoutClassification(OWLOntology ontology) throws OntologyException {
		if (Settings.BLACKBOX_REASONER == BlackBoxReasoner.PELLET) {
			PelletReasonerFactory reasonerFactory =  new PelletReasonerFactory();
			this.reasoner = reasonerFactory.createReasoner(ontology);
		}
		else if (Settings.BLACKBOX_REASONER == BlackBoxReasoner.HERMIT){
			// this.reasoner = new Reasoner(ontology);
			// ReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
			Configuration config = new Configuration();
			config.ignoreUnsupportedDatatypes = true;
			this.reasoner = new Reasoner(config, ontology);
			// this.reasoner = reasonerFactory.createReasoner(ontology, config);
			
		}
		else {
			throw new OntologyException(
					OntologyException.REASONING_ERROR,
					"could not init reasoner, reasoner not specified / not available"
			);
		}
	}	
	
	
	
	private Set<OWLAxiom> convertToAxioms(Mapping mapping) {
		HashSet<OWLAxiom> mappingAxioms = new HashSet<OWLAxiom>();
		Entity entity;
		// go for the classes
		for (OWLClass concept : this.sourceOntology.getClassesInSignature()) {
			for (Correspondence c : mapping) {
				// System.out.println("|| " + c.getSourceEntityUri().toString() + " XXX " + concept.getIRI().toString() + "||");
				if (c.getSourceEntityUri().toString().equals(concept.getIRI().toString())) {
					// System.out.println(c.getSourceEntityUri().toString() + " === " + concept.getIRI().toString());
					entity = EntityFactory.createConceptEntity(concept);
					c.setSourceEntity(entity);
				}				
			}
		}
		for (OWLClass concept : this.targetOntology.getClassesInSignature()) {
			for (Correspondence c : mapping) {
				if (c.getTargetEntityUri().toString().equals(concept.getIRI().toString())) {
					entity = EntityFactory.createConceptEntity(concept);
					c.setTargetEntity(entity);
				}				
			}
		}
		// go for object properties
		for (OWLObjectProperty prop : this.sourceOntology.getObjectPropertiesInSignature()) {
			for (Correspondence c : mapping) {
				if (c.getSourceEntityUri().toString().equals(prop.getIRI().toString())) {
					entity = EntityFactory.createObjectPropertyEntity(prop, null);
					c.setSourceEntity(entity);
				}				
			}
		}
		for (OWLObjectProperty prop : this.targetOntology.getObjectPropertiesInSignature()) {
			for (Correspondence c : mapping) {
				if (c.getTargetEntityUri().toString().equals(prop.getIRI().toString())) {
					entity = EntityFactory.createObjectPropertyEntity(prop, null);
					c.setTargetEntity(entity);
				}				
			}
		}
		// go for data properties
		for (OWLDataProperty prop : this.sourceOntology.getDataPropertiesInSignature()) {
			for (Correspondence c : mapping) {
				if (c.getSourceEntityUri().toString().equals(prop.getIRI().toString())) {
					entity = EntityFactory.createDataPropertyEntity(prop, null);
					c.setSourceEntity(entity);
				}				
			}
		}
		for (OWLDataProperty prop : this.targetOntology.getDataPropertiesInSignature()) {
			for (Correspondence c : mapping) {
				if (c.getTargetEntityUri().toString().equals(prop.getIRI().toString())) {
					entity = EntityFactory.createDataPropertyEntity(prop, null);
					c.setTargetEntity(entity);
				}				
			}
		}
		
		// itertae over alignment
		for (Correspondence c : mapping) {
			if (c.getSourceEntity().getConcept() == null) continue;
			OWLAxiom axiom = null;
			if (c.getRelation().getType() == SemanticRelation.EQUIV) {
				// System.out.println(c);
				axiom = this.factory.getOWLEquivalentClassesAxiom(c.getSourceEntity().getConcept(), c.getTargetEntity().getConcept());
			}
			else if (c.getRelation().getType() == SemanticRelation.SUB) {
				axiom = this.factory.getOWLSubClassOfAxiom(c.getSourceEntity().getConcept(), c.getTargetEntity().getConcept());
			}
			else if (c.getRelation().getType() == SemanticRelation.SUPER) {
				axiom = this.factory.getOWLSubClassOfAxiom(c.getTargetEntity().getConcept(), c.getSourceEntity().getConcept());
			}
			mappingAxioms.add(axiom);
		}
		return mappingAxioms;
	}


	private OWLOntology loadOntology(String ontPath) throws OntologyException {
		Tools.deactivateLogger();	
		OWLOntology ontology;
		URI physicalURI = null;
    	File ontologyFile = new File(ontPath);

		if (!(ontologyFile.exists())) {
			try { physicalURI = new URI(ontPath); }
			catch (URISyntaxException e) {
				throw new OntologyException(OntologyException.IO_ERROR, "ontology at filepath or URL " + ontPath + " does not exist");
			}

		}
		else {
			physicalURI = ontologyFile.toURI();
		}
		
		try {
			IRI ontologyIRI = IRI.create(physicalURI);
			
			ontology = manager.loadOntology(ontologyIRI);
			System.out.println("load ontology " + ontology);
		}
		catch (OWLOntologyCreationException e) {
			throw new OntologyException(
					OntologyException.INVALID_FORMAT, 
					"ontology file at " + ontPath + " could not sucessfully be parsed", 
					e
			);
		}
		catch (IllegalArgumentException e) {
			throw new OntologyException(
					OntologyException.INVALID_FORMAT, 
					"ontology file at " + ontPath + " could not sucessfully be parsed", 
					e
			);
		}
		return ontology;
	}

}
