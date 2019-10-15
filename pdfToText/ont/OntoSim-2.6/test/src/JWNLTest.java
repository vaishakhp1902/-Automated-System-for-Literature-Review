/*
 * $Id$
 *
 * Copyright (C) INRIA, 2008-2009, 2015
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeSuite;

import fr.inrialpes.exmo.ontosim.string.JWNLDistances;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

/**
 * These tests corresponds to the JWNL test of the README file in the main directory
 */

public class JWNLTest {

    private JWNLDistances jDist = null;

    @BeforeSuite @Test(groups = { "full", "ling" })
    public void routineInitializeWordNet() throws Exception {
        jDist = new JWNLDistances();
	jDist.Initialize( "../WordNet-3.0/dict", "3.0" );
    }

    // This was in the class "main"
    @Test(groups = { "full", "ling" }, dependsOnMethods = {"routineInitializeWordNet"})
    public void routineJWNLDistanceTest() throws Exception {

	assertEquals( jDist.compareComponentNames( "French997Guy", "Dutch_Goa77ly" ), 0.23684210526315788);
	assertEquals( jDist.compareComponentNames( "FREnch997guy21GUIe", "Dutch_GOa77ly." ), 0.09795918367346938);
	assertEquals( jDist.compareComponentNames( "a997c", "77ly."), .0);
	assertEquals( jDist.compareComponentNames( "MSc", "PhD"), 0.2777777777777778);
    }

    @Test(groups = { "full", "ling" })
    public void routineJWNLWuPalmerTest() throws Exception {

	// In comment the values in our book (1st ed. p102; computed with WordNet 2.0)
	assertEquals( jDist.wuPalmerSimilarity( "author", "author" ), 1.);//1.
	assertEquals( jDist.wuPalmerSimilarity( "author", "writer" ), 1.); //1.
	assertEquals( jDist.wuPalmerSimilarity( "author", "illustrator" ), .6);//.5
	assertEquals( jDist.wuPalmerSimilarity( "author", "creator" ), .75);//.67
	assertEquals( jDist.wuPalmerSimilarity( "author", "Person" ), .8);//.4
    }

    @Test(groups = { "full", "ling" })
    public void basicSynonymDistanceTest() throws Exception {

	assertEquals( jDist.basicSynonymDistance( "author", "author" ), 0.);//
	assertEquals( jDist.basicSynonymDistance( "author", "writer" ), 0.); //
	assertEquals( jDist.basicSynonymDistance( "author", "illustrator" ), .5);//
	assertEquals( jDist.basicSynonymDistance( "author", "creator" ), .5);//
	assertEquals( jDist.basicSynonymDistance( "author", "Person" ), 0.6666666666666667);//
    }

    @Test(groups = { "full", "ling" })
    public void cosynonymySimilarityTest() throws Exception {

	// In comment the values in our book (1st ed. p89)
	assertEquals( jDist.cosynonymySimilarity( "author", "author" ), 1.);//1.
	assertEquals( jDist.cosynonymySimilarity( "author", "writer" ), .25); //.25
	assertEquals( jDist.cosynonymySimilarity( "author", "illustrator" ), 0.);//0.
	assertEquals( jDist.cosynonymySimilarity( "author", "creator" ), 0.);//0.
	assertEquals( jDist.cosynonymySimilarity( "author", "Person" ), 0.);//0.
    }

    @Test(groups = { "full", "ling" })
    public void basicSynonymySimilarityTest() throws Exception {

	// In comment the values in our book (1st ed. p89)
	assertEquals( jDist.basicSynonymySimilarity( "author", "author" ), 1.);//1.
	assertEquals( jDist.basicSynonymySimilarity( "author", "writer" ), 1.); //1.
	assertEquals( jDist.basicSynonymySimilarity( "author", "illustrator" ), 0.);//0.
	assertEquals( jDist.basicSynonymySimilarity( "author", "creator" ), 0.);//0.
	assertEquals( jDist.basicSynonymySimilarity( "author", "Person" ), 0.);//0.
    }

    @Test(groups = { "full", "ling" })
    public void computeSimilarityTest() throws Exception {

	// In comment the values in our book
	assertEquals( jDist.computeSimilarity( "author", "author" ), 1.);//
	assertEquals( jDist.computeSimilarity( "author", "writer" ), 1.); //
	assertEquals( jDist.computeSimilarity( "author", "illustrator" ), .8);//
	assertEquals( jDist.computeSimilarity( "author", "creator" ), 0.8888888888888888);//
	assertEquals( jDist.computeSimilarity( "author", "Person" ), 0.875);//
    }

}
