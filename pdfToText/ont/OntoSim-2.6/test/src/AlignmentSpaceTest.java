/*
 * $Id$
 *
 * Copyright (C) INRIA, 2009, 2015
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

//package test.com.acme.dona.dep;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.align.ASAlignmentPathMeasure;
import fr.inrialpes.exmo.ontosim.align.ASShortestPathMeasure;
import fr.inrialpes.exmo.ontosim.align.ASShortestPathMeasure.NORM;
import fr.inrialpes.exmo.ontosim.align.ASLargestCoverageMeasure;
import fr.inrialpes.exmo.ontosim.align.ASUnionPathCoverageMeasure;

import fr.inrialpes.exmo.align.impl.BasicOntologyNetwork;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.jena25.JENAOntology;


import java.net.URI;
/**
 * These tests corresponds to the README file in the main directory
 */

public class AlignmentSpaceTest {

    BasicOntologyNetwork noo;
    Ontology o1, o2, o3, o4, o5;

    @Test(groups = { "full", "noling" })
    public void createNetwork() throws Exception {

	noo = new BasicOntologyNetwork();
// Make a new model to act as an OWL ontology for WordNet
//

// Use OntModel's convenience method to describe 
// WordNet's hyponymOf property as transitive
//wnOntology.createTransitiveProperty(WordnetVocab.hyponymOf.getURI());

// Alternatively, just add a statement to the underlying model to express that
// hyponymOf is of type TransitiveProperty
//wnOntology.add(WordnetVocab.hyponymOf, RDF.type, OWL.TransitiveProperty);	

	//o1: a1, b1, c1
	o1 = new JENAOntology();
	o1.setURI( new URI( "http://www.examples.org/o1" ) );
	// Not really this but not far
	o1.setOntology( ModelFactory.createOntologyModel() ); // should be OntModel
	//o1.getURI() 
	noo.addOntology( o1.getURI() );
	URI a1 = new URI( "http://www.examples.org/o1/a" );
	URI b1 = new URI( "http://www.examples.org/o1/b" );
	URI c1 = new URI( "http://www.examples.org/o1/c" );
	((OntModel)o1.getOntology()).createClass( "http://www.examples.org/o1/a" );
	((OntModel)o1.getOntology()).createClass( "http://www.examples.org/o1/b" );
	((OntModel)o1.getOntology()).createClass( "http://www.examples.org/o1/c" );
	//o2: a2, b2, c2
	o2 = new JENAOntology();
	o2.setURI( new URI( "http://www.examples.org/o2" ) );
	o2.setOntology( ModelFactory.createOntologyModel() ); // should be OntModel
	noo.addOntology( o2.getURI() );
	URI a2 = new URI( "http://www.examples.org/o2/a" );
	URI b2 = new URI( "http://www.examples.org/o2/b" );
	URI c2 = new URI( "http://www.examples.org/o2/c" );
	((OntModel)o2.getOntology()).createClass( "http://www.examples.org/o2/a" );
	((OntModel)o2.getOntology()).createClass( "http://www.examples.org/o2/c" );
	((OntModel)o2.getOntology()).createClass( "http://www.examples.org/o2/d" );
	//o3: a3, b3, c3
	o3 = new JENAOntology();
	o3.setURI( new URI( "http://www.examples.org/o3" ) );
	o3.setOntology( ModelFactory.createOntologyModel() ); // should be OntModel
	noo.addOntology( o3.getURI() );
	URI a3 = new URI( "http://www.examples.org/o3/a" );
	URI b3 = new URI( "http://www.examples.org/o3/b" );
	URI c3 = new URI( "http://www.examples.org/o3/c" );
	((OntModel)o3.getOntology()).createClass( "http://www.examples.org/o3/a" );
	((OntModel)o3.getOntology()).createClass( "http://www.examples.org/o3/b" );
	((OntModel)o3.getOntology()).createClass( "http://www.examples.org/o3/c" );
	//o4: a4, b4, c4
	o4 = new JENAOntology();
	o4.setURI( new URI( "http://www.examples.org/o4" ) );
	o4.setOntology( ModelFactory.createOntologyModel() ); // should be OntModel
	noo.addOntology( o4.getURI() );
	URI a4 = new URI( "http://www.examples.org/o4/a" );
	URI b4 = new URI( "http://www.examples.org/o4/b" );
	URI c4 = new URI( "http://www.examples.org/o4/c" );
	((OntModel)o4.getOntology()).createClass( "http://www.examples.org/o4/a" );
	((OntModel)o4.getOntology()).createClass( "http://www.examples.org/o4/b" );
	((OntModel)o4.getOntology()).createClass( "http://www.examples.org/o4/c" );
	// o5: disconnected
	o5 = new JENAOntology();
	o5.setURI( new URI( "http://www.examples.org/o5" ) );
	o5.setOntology( ModelFactory.createOntologyModel() ); // should be OntModel
	noo.addOntology( o5.getURI() );

	//A12: a, b
	URIAlignment al12 = new URIAlignment();
	al12.init( o1.getURI(), o2.getURI() );
	al12.addAlignCell( a1, a2 );
	al12.addAlignCell( b1, b2 );
	noo.addAlignment( al12 );
	//A13: b, c
	URIAlignment al13 = new URIAlignment();
	al13.init( o1.getURI(), o3.getURI() );
	al13.addAlignCell( b1, b3 );
	al13.addAlignCell( c1, c3 );
	noo.addAlignment( al13 );
	//A24: a
	URIAlignment al24 = new URIAlignment();
	al24.init( o2.getURI(), o4.getURI() );
	al24.addAlignCell( a2, a4 );
	noo.addAlignment( al24 );
	//A34: b, c
	URIAlignment al34 = new URIAlignment();
	al34.init( o3.getURI(), o4.getURI() );
	al34.addAlignCell( b3, b4 );
	al34.addAlignCell( c3, c4 );
	noo.addAlignment( al34 );
	//A31: c
	URIAlignment al31 = new URIAlignment();
	al31.init( o3.getURI(), o1.getURI() );
	al31.addAlignCell( b3, b1 );
	noo.addAlignment( al31 );
    }

    @Test(groups = { "full", "noling" }, dependsOnMethods = {"createNetwork"})
    public void ASAlignmentPathMeasureTest() throws Exception {
	Measure m = new ASAlignmentPathMeasure( noo );
	assertEquals( m.getSim( o1, o1 ), 1. );
	assertEquals( m.getSim( o1, o5 ), 0. );
	assertEquals( m.getSim( o5, o1 ), 0. );
	assertEquals( m.getSim( o1, o2 ), .67, .01 );
	assertEquals( m.getSim( o2, o1 ), 0. );
	assertEquals( m.getSim( o1, o4 ), 0.33, .01 );
	assertEquals( m.getSim( o4, o1 ), 0. );
	assertEquals( m.getSim( o2, o3 ), 0. );
	assertEquals( m.getSim( o3, o2 ), 0.33, .01 );
    }
    @Test(groups = { "full", "noling" }, dependsOnMethods = {"createNetwork"})
    public void ASShortestPathMeasureTest() throws Exception {
	Measure m = new ASShortestPathMeasure( noo );
	assertEquals( m.getSim( o1, o1 ), 1. );
	assertEquals( m.getSim( o1, o5 ), 0. );
	assertEquals( m.getSim( o5, o1 ), 0. );
	assertEquals( m.getSim( o1, o2 ), .8 );
	assertEquals( m.getSim( o2, o1 ), 0. );
	assertEquals( m.getSim( o1, o4 ), .6 );
	assertEquals( m.getSim( o4, o1 ), 0. );
	assertEquals( m.getSim( o2, o3 ), 0. );
	assertEquals( m.getSim( o3, o2 ), .6 );
	((ASShortestPathMeasure)m).setNormModality( NORM.diameter );
	// Note: this is an accident that it has the same values as ASAlignmentPathMeasure
	assertEquals( m.getSim( o1, o1 ), 1. );
	assertEquals( m.getSim( o1, o5 ), 0. );
	assertEquals( m.getSim( o5, o1 ), 0. );
	assertEquals( m.getSim( o1, o2 ), .67, .01 );
	assertEquals( m.getSim( o2, o1 ), 0. );
	assertEquals( m.getSim( o1, o4 ), .33, .01 );
	assertEquals( m.getSim( o4, o1 ), 0. );
	assertEquals( m.getSim( o2, o3 ), 0. );
	assertEquals( m.getSim( o3, o2 ), .33, .01 );
    }
    @Test(groups = { "full", "noling" }, dependsOnMethods = {"createNetwork"})
    public void ASLargestCoverageMeasureTest() throws Exception {
	Measure m = new ASLargestCoverageMeasure( noo );
	assertEquals( m.getSim( o1, o1 ), 1. );
	assertEquals( m.getSim( o1, o5 ), 0. );
	assertEquals( m.getSim( o5, o1 ), 0. );
	assertEquals( m.getSim( o1, o2 ), .67, .01 );
	assertEquals( m.getSim( o2, o1 ), 0. );
	assertEquals( m.getSim( o1, o2 ), .67, .01 );
	assertEquals( m.getSim( o1, o4 ), .67, .01 );
	assertEquals( m.getSim( o4, o1 ), 0. );
	assertEquals( m.getSim( o2, o3 ), 0. );
	assertEquals( m.getSim( o3, o2 ), .33, .01 );
    }
    @Test(groups = { "full", "noling" }, dependsOnMethods = {"createNetwork"})
    public void ASUnionPathCoverageMeasureTest() throws Exception {
	Measure m = new ASUnionPathCoverageMeasure( noo );
	assertEquals( m.getSim( o1, o1 ), 1. );
	assertEquals( m.getSim( o1, o5 ), 0. );
	assertEquals( m.getSim( o5, o1 ), 0. );
	assertEquals( m.getSim( o1, o2 ), .67, .01 );
	assertEquals( m.getSim( o2, o1 ), 0. );
	assertEquals( m.getSim( o1, o4 ), 1. );
	assertEquals( m.getSim( o4, o1 ), 0. );
	assertEquals( m.getSim( o2, o3 ), 0. );
	assertEquals( m.getSim( o3, o2 ), .33, .01 );
    }

}
