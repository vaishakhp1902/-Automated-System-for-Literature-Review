/*
 * $Id: LuceneAnalyser.java 156 2015-11-21 08:15:36Z euzenat $
 *
 * Copyright (C) INRIA, 2004-2005, 2007-2011, 2013, 2015
 * This program was originaly part of the Alignment API implementation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.ontosim.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class LuceneAnalyser {

    private static Analyzer analyzer = new EnglishAnalyzer();//new SnowballAnalyzer(Version.LUCENE_30, "English",StopAnalyzer.ENGLISH_STOP_WORDS_SET);
    
    /**
     * Tokenizes and extracts terms from a string to add them into words collection. Words are stemmed.
     *
     * @param toAnalyse : the string to be analysed
     * @param elements : the collection to which extracted words is added
     * @return the modified collection
     */
    public static Collection<String> getTerms(String toAnalyse, Collection<String> elements) {
	
	if (elements==null) elements = new ArrayList<String>();
	
	try {
	    TokenStream tokenS = analyzer.tokenStream("", new StringReader( toAnalyse ));
	    CharTermAttribute termAtt = tokenS.addAttribute(CharTermAttribute.class);
	    tokenS.reset();
	    while ( tokenS.incrementToken() ) {
		elements.add( termAtt.toString() );
	    }
	    tokenS.end();
	    tokenS.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return elements;
	
    }
}
