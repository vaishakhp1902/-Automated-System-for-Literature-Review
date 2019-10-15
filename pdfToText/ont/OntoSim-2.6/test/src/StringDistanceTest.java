/*
 * $Id$
 *
 * Copyright (C) INRIA, 2008-2009, 2015, 2017
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

import fr.inrialpes.exmo.ontosim.string.StringDistances;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

/**
 * These tests corresponds to the README file in the main directory
 */

public class StringDistanceTest {

    @Test(groups = { "full", "noling" })
    public void tokenizeTest() throws Exception {
	Vector<String> res = StringDistances.tokenize( "ComputerScience" );
	// [Computer, Science]
	assertEquals( res.size(), 2 );
	assertEquals( res.get(1), "Science" );
	res = StringDistances.tokenize( "FullProfessor in ComputerScience" );
	// [FullProfessor, in, ComputerScience]
	assertEquals( res.size(), 3 );
	assertEquals( res.get(1), "in" );
	res = StringDistances.tokenize( "USofAmerica" );
	// [USof, America]
	assertEquals( res.size(), 2 );
	assertEquals( res.get(1), "America" );
	res = StringDistances.tokenize( "SpiritofAmerica" );
	// [Spiritof, America]
	assertEquals( res.size(), 2 );
	assertEquals( res.get(1), "America" );
	res = StringDistances.tokenize( "SpiritOfAmerica" );
	// [Spirit, Of, America]
	assertEquals( res.size(), 3 );
	assertEquals( res.get(1), "Of" );
	res = StringDistances.tokenize( "ProtoDL" );
	// [Proto, DL]
	assertEquals( res.size(), 2 );
	assertEquals( res.get(1), "DL" );
	res = StringDistances.tokenize( "And/or" );
	// [And, or]
	assertEquals( res.size(), 2 );
	assertEquals( res.get(1), "or" );
	res = StringDistances.tokenize( "title: FullProfessor" );
	// [title, FullProfessor]
	assertEquals( res.size(), 2 );
	assertEquals( res.get(1), "FullProfessor" );
    }

    // nGramm tests
    @Test(groups = { "full", "noling" })
    public void ngrammTest() throws Exception {
	assertEquals( StringDistances.ngramDistance( "ana", "ana" ), 0. );
	assertEquals( StringDistances.ngramDistance( "anan", "anan" ), 0. );
	assertEquals( StringDistances.ngramDistance( "anan", "anab" ), .6666666666666667 );
	// Sensible tests
	// This is not adapted because here the distances are all the same
	assertEquals( StringDistances.ngramDistance( "anana", "ananana" ), 0. ); 
	assertEquals( StringDistances.ngramDistance( "anana", "ananana" ), 0. ); 
	assertEquals( StringDistances.ngramDistance( "anana", "ananana" ), StringDistances.ngramDistance( "ananana", "anana" ) );
	//assertTrue( ( StringDistances.ngramDistance( "anana", "ananana" ) < StringDistances.ngramDistance( "anana", "anananana" ) ) );
	// Some tricky ones... should be between 0 and 1.
	assertEquals( StringDistances.ngramDistance( "anal canal", "anal_canal" ), .6666666666666667 ); // 1-3/7
	assertEquals( StringDistances.ngramDistance( "coracobrachial", "coracobrachialis" ), .15384615384615385 ); // 1-11/13
	assertEquals( StringDistances.ngramDistance( "articular cartilage", "articular_cartilage"), 0.33333333333333337 ); // 1-(12/18) ?!
	assertEquals( StringDistances.ngramDistance( "tibialis caudalis", "tibialis_caudalis" ), 0.375 ); // 1-(10/16)
	assertEquals( StringDistances.ngramDistance( "mmmm", "mmmm" ), 0. );	
    }
    // nGramm occurence test
    @Test(groups = { "full", "noling" })
    public void ngrammOccurTest() throws Exception {
	assertEquals( StringDistances.ngramOccurDistance( "ana", "ana" ), 0. );
	assertEquals( StringDistances.ngramOccurDistance( "anan", "anan" ), 0. );
	assertEquals( StringDistances.ngramOccurDistance( "anan", "anab" ), .5 );
	// Sensible tests
	// This is not adapted because here the distances are all the same
	assertEquals( StringDistances.ngramOccurDistance( "anana", "ananana" ), 0. ); 
	assertEquals( StringDistances.ngramOccurDistance( "anana", "ananana" ), 0. ); 
	assertEquals( StringDistances.ngramOccurDistance( "anana", "ananana" ), StringDistances.ngramOccurDistance( "ananana", "anana" ) );
	//assertTrue( ( StringDistances.ngramOccurDistance( "anana", "ananana" ) < StringDistances.ngramOccurDistance( "anana", "anananana" ) ) );
	// Some tricky ones... should be between 0 and 1.
	assertEquals( StringDistances.ngramOccurDistance( "anal canal", "anal_canal" ), 0.375 );
	assertEquals( StringDistances.ngramOccurDistance( "coracobrachial", "coracobrachialis" ), 0.07692307692307687 );
	assertEquals( StringDistances.ngramOccurDistance( "articular cartilage", "articular_cartilage"), 0.17647058823529416 );
	assertEquals( StringDistances.ngramOccurDistance( "tibialis caudalis", "tibialis_caudalis" ), 0.19999999999999996 );
	assertEquals( StringDistances.ngramOccurDistance( "mmmm", "mmmm" ), 0. );	
    }
}
