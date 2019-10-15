/**
 * $Id: ASLargestCoverageMeasure.java 40 2009-06-09 11:44:41Z euzenat $
 *
 *   Copyright 2009-2010, 2017 INRIA, Université Pierre Mendès France
 *   
 *   $filename$ is part of OntoSim.
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

package fr.inrialpes.exmo.ontosim.align;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Hashtable;
import java.net.URI;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.OntologyNetwork;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.ontosim.OntoSimException;

/**
 * This class implements measures based on the coverage of the initial ontology by the composition of alignments
 */
public class ASCoverageMeasure extends ASAbstractCoverageTraversal {

    int globaliterations = 3;

    public ASCoverageMeasure( OntologyNetwork noo, int it ){
	super( noo );
	globaliterations = it;
    }

    public ASCoverageMeasure( OntologyNetwork noo ){
	super( noo );
    }

    public ASCoverageMeasure(){
	super();
    }

    // Here this should roughly be a copy of ASShortestPathMeasure
    // We can do the matrix, but it will contain set of entities...
    // Yes we can...

    // This one is a bit more tricky because NO ONE can tell what is to be preserved at the end of the path... hence the best is:
    // Having in each cell the result of all of the paths so far...

    // It is also possible to compute it by traversal...
    // This is what is implemented here.

    // In the end, we compare the size or do the union...
    Hashtable<URI,Set<Hashtable<URI,URI>>> prevtable2;

    // JE: for the moment, this is ASLC --- without iteration: just once
    public double getValue( Ontology<?> o1, Ontology<?> o2 ) throws OntoSimException {
	Hashtable<URI,URI> topreserve = new Hashtable<URI,URI>();
	int size = 0;
	for ( URI u : objectsToPreserve( o1 ) ) {
	    topreserve.put( u, u );
	    size++;
	}
	//System.err.println("GETVALUE("+size+") "+o1.getURI()+" -- "+o2.getURI() );
	if ( size == 0 ) return 0.;
	else {
	    // We no that no more iterations than size are necessary
	    int iterations = Math.min( globaliterations, size );
	    Hashtable<URI,URI> fullresult = new Hashtable<URI,URI>();
	    while ( iterations > 0 ){
		prevtable2 = new Hashtable<URI,Set<Hashtable<URI,URI>>>();//?
		//System.err.print("\nIteration "+iterations);
		Hashtable<URI,URI> result = traverse( new Hashtable<URI,URI>(), size, topreserve, o1.getURI(), o2.getURI() );
		fullresult.putAll( result );
		//System.err.println( " // "+result.size()+" - "+fullresult.size()+" - "+topreserve.size() );
		if ( result.size() == 0 ) iterations = 0;
		else if ( topreserve.size() == result.size() ) iterations = 0; //return 1.
		else {
		    differs( topreserve, result );
		    iterations--;
		}
	    }
	    return ((double)fullresult.size()) / (double)size;
	}
    }

    public void differs( Hashtable<URI,URI> tab1, Hashtable<URI,URI> tab2 ) {
	for ( Object k : tab2.keySet() ) {
	    tab1.remove( k );
	}
    }

    public Hashtable<URI,URI> traverse( Hashtable<URI,URI> max, int size, Hashtable<URI,URI> topreserve, URI current, URI target ) {
	if ( current.equals( target ) ) { // Stop if finished
	    return topreserve; // return the preserved ones
	} else if ( !alreadyVisited( current, topreserve ) ) {
	    if ( prevtable2.get( current ) == null ) prevtable2.put( current, new HashSet<Hashtable<URI,URI>>() );
	    prevtable2.get( current ).add( topreserve );
	    Hashtable<URI,URI> result = max;
	    for ( Alignment al : network.getSourceAlignments( current ) ) {
		Hashtable<URI,URI> preserved = applyAlignment( al, topreserve );
		try {
		    if ( preserved.size() > result.size() ) { // we can try to do better
			// is it really preserved.size() ??
			Hashtable<URI,URI> obtained = traverse( result, preserved.size(), preserved, al.getOntology2URI(), target );
			 if ( obtained.size() == size ) return obtained; // we reached the end with optimal preservation
			 else if ( obtained.size() > result.size() ) result = obtained;
		    }
		    // Could be used for debugging, but so far, just ignore
		} catch ( AlignmentException aex ) {}
	    }
	    return result;
	} else {
	    return max; //??
	}
    }

    // did I already visited the current node with the same set of objects to preserve
    // Is it a good measure?
    public boolean alreadyVisited( URI current, Hashtable<URI,URI> topreserve ) {
	Set<Hashtable<URI,URI>> previous = prevtable2.get( current );
	if ( previous == null ) return false;
	for ( Hashtable<URI,URI> s : previous ) {
	    if ( includedIn( topreserve, s ) ) {
		return true;
	    }
	}
	return false;
    }

    // For this one, it is good to preserve the IMAGE
    public boolean includedIn( Hashtable<URI,URI> sub, Hashtable<URI,URI> sup ) {
	for ( Object ob : sub.values() ) {
	    if ( !sup.containsValue( ob ) ) return false;
	}
	return true;
    }

    /**
     * returns an hashtable containing pairs: target URI x source URI
     * has modified entities so as to suppress those source URI which have been transformed.
     */
    public Hashtable<URI,URI> applyAlignment( Alignment al, Hashtable<URI,URI> entities ) {
	Hashtable<URI,URI> results = new Hashtable<URI,URI>();
	URIAlignment align = null;
	if ( al instanceof URIAlignment ) {
	    align = ((URIAlignment)al);
	} else {
	    if ( al instanceof BasicAlignment ) {
		try { align = ((BasicAlignment)al).toURIAlignment(); }
		catch (AlignmentException aex) {} // we have tried
	    }
	}
	if ( align == null ) return results;
	for ( URI u : entities.keySet() ) {
	    URI intermediate = entities.get( u );
	    try {
		Set<Cell> cells = align.getAlignCells1( intermediate );
		if ( cells != null ) {
		    for ( Cell cell : cells ) {
			if ( cell.getRelation().equals( EquivRel ) ) {
			    results.put( u, cell.getObject2AsURI() );
			}
		    }
		}
	    } catch ( AlignmentException aex ) { aex.printStackTrace(); }
	}
	return results;
    }

}
