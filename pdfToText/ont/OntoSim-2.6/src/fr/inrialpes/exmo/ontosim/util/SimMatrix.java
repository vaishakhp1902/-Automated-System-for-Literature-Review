/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   SimMatrix.java is part of OntoSim.
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

package fr.inrialpes.exmo.ontosim.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Set;

import fr.inrialpes.exmo.ontosim.entity.EntityLexicalMeasure;
import fr.inrialpes.exmo.ontosim.entity.model.Entity;
import fr.inrialpes.exmo.ontosim.entity.model.EntityImpl;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class SimMatrix {

    /**
     * Printout the similarity matrix between two ontologies
     *
     * @param args: the two ontology filename to compare
     * @throws OntowrapException when the ontologies cannot be loaded
     * @throws FileNotFoundException when the files are not reachable
     */
    @SuppressWarnings("unchecked")
    public static void main( String[] args ) throws OntowrapException, FileNotFoundException {
	
	File f1 = new File(args[0]);
	File f2 = new File(args[1]);
	
	PrintStream out = System.out;// new PrintStream(new File("ev43-eurlex.txt"));
	OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.skoslite.SKOSLiteOntologyFactory");
	
	OntologyFactory ontoFactory = OntologyFactory.getFactory();
	
	LoadedOntology<?> o1 = ontoFactory.loadOntology(f1.toURI());
	LoadedOntology<?> o2 = ontoFactory.loadOntology(f2.toURI());
	
	EntityLexicalMeasure<?> sim = new EntityLexicalMeasure("en");
	
	Set<?> entites2Temp = o2.getClasses();
	ArrayList<Entity<?>> entities2 = new ArrayList<Entity<?>>(entites2Temp.size());
	
	for (Object o : entites2Temp) {
	    entities2.add(new EntityImpl(o2,o));
	}

	for (Object o : o1.getClasses()) {
	    Entity e1 = new EntityImpl(o1,o);
	    for (Entity e2 : entities2) {
		out.println(e1.getURI()+";"+e2.getURI()+";"+sim.getSim(e1, e2));
	    }
	}
	
	out.close();
	
    }

}
