/**
 *   Copyright 2008-2011 INRIA, Université Pierre Mendès France
 *   
 *   VectorSpaceMeasure.java is part of OntoSim.
 *
 *   OntoSim is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   OntoSim is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with OntoSim; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package fr.inrialpes.exmo.ontosim;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.TokenStream;
//import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import fr.inrialpes.exmo.ontosim.util.LuceneAnalyser;
import fr.inrialpes.exmo.ontosim.vector.CosineVM;
import fr.inrialpes.exmo.ontosim.vector.VectorMeasure;
import fr.inrialpes.exmo.ontosim.vector.model.Document;
import fr.inrialpes.exmo.ontosim.vector.model.DocumentCollection;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;


public  class VectorSpaceMeasure implements Measure<LoadedOntology<?>> {

    	

	protected Map<LoadedOntology<?>, Document> ontologies= new HashMap<LoadedOntology<?>, Document>();
	private DocumentCollection ontIndex= new DocumentCollection();

	protected DocumentCollection.WEIGHT vectorType=DocumentCollection.WEIGHT.TF;

	protected VectorMeasure measure=new CosineVM();

	public VectorSpaceMeasure(){
		super();
	}

	public VectorSpaceMeasure(Collection<LoadedOntology<?>> ontologies) {
		super();
		for (LoadedOntology<?> ont : ontologies)
			addOntology(ont);
	}



	public VectorSpaceMeasure(VectorMeasure m, DocumentCollection.WEIGHT vectorType) {
		super();
		this.measure=m;
		this.vectorType=vectorType;
	}

	public VectorSpaceMeasure(Collection<LoadedOntology<?>> ontologies,VectorMeasure m, DocumentCollection.WEIGHT vectorType) {
		super();
		this.measure=m;
		this.vectorType=vectorType;
		for (LoadedOntology<?> ont : ontologies)
			addOntology(ont);
	}


	public final boolean addOntology(LoadedOntology<?> ontology) {
	    if (ontologies.containsKey(ontology))
		return false;
	    Document ontDoc = new Document(ontology.getURI().toString());
	    try {
		for ( Object e : ontology.getEntities() ) {
		    try {
			for (String annot : ontology.getEntityAnnotations(e)) {
			    Collection<String> terms = new Vector<String>();
			    LuceneAnalyser.getTerms(annot, terms);
			    ontDoc.addOccTerms(terms);
			}
			URI entUri = ontology.getEntityURI(e);
			if (entUri != null && entUri.getFragment() != null) {
			    ontDoc.addOccTerm(entUri.getFragment());
			}
		    } catch (Exception ex) { ex.printStackTrace(); }
		}
	    } catch ( OntowrapException owex ) { owex.printStackTrace(); }

	    ontIndex.add(ontDoc);
	    ontologies.put(ontology, ontDoc);
	    return true;
	}

	public final void addOntologies(Collection<LoadedOntology<?>> ontologies) {
	    for (LoadedOntology<?> o : ontologies) {
		addOntology(o);
	    }
	}


	public double getSim(LoadedOntology<?> o1, LoadedOntology<?> o2, VectorMeasure m, DocumentCollection.WEIGHT vectorType) {
		addOntology(o1);
		addOntology(o2);
		return m.getMeasureValue(ontIndex.getDocVector(ontologies.get(o1),vectorType),
				ontIndex.getDocVector(ontologies.get(o2),vectorType));
	}

	public double getDissim(LoadedOntology<?> o1, LoadedOntology<?> o2, VectorMeasure m, DocumentCollection.WEIGHT vectorType) {
		return 1.0 - getSim(o1,o2,m,vectorType);
	}

	public double getSim(LoadedOntology<?> o1, LoadedOntology<?> o2) {
		return getSim(o1,o2,measure,vectorType);
	}

	public double getDissim(LoadedOntology<?> o1, LoadedOntology<?> o2) {
		return getDissim(o1,o2,measure,vectorType);
	}

	public double getMeasureValue(LoadedOntology<?> o1, LoadedOntology<?> o2) {
		return getSim(o1,o2,measure,vectorType);
	}

	public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
		return measure.getMType();
	}
	
	public DocumentCollection.WEIGHT getVectorType() {
	    return vectorType;
	}


}
