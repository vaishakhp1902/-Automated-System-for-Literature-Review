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


public class Settings {
	
	/**
	* It is possible tochoose between different reasoners that act internally 
	* as black box oracle.
	*/
	public enum BlackBoxReasoner {
		PELLET,
		HERMIT
	}
	
	/**
	* The reasoner used internally in a black-box approach.
	*/
	public static BlackBoxReasoner BLACKBOX_REASONER = BlackBoxReasoner.HERMIT;
	
	
	/**
	* Probably no longer used internally. Was once a parameter of ALCOMOs manual
	* mapping debugging component. 
	*/
	public static boolean HTML_DEBUG_DISPLAY_LABELS = false;
	

	
	/**
	* If true, instances are removed before processing to avoid incoherencies. Some information might get
	* lost for nominals. In the standard setting this should be activated.
	*/
	public static boolean REMOVE_INDIVIDUALS = true;
	
	
	/**
	* Defines the path to the log4j property file.
	* 
	* TODO (ugly code) This has to be done in a slightly different way, when public available. 
	*/
	public static String LOG4J_PROPERTY_FILE = "log4j.properties";
	
	/**
	* 
	* Very important property, if false only domain restrictions are checked by efficient reasoning methods.
	* Experiments indicate that it might be better to deactivate this option, nevertheless, in the ALCOMO
	* standard setting this option is activated.
	* 
	*/
	public static boolean PROPERTY_RANGE_EXTENSION = false;


	/**
	* If activated the ontologies are always classifed before e.g. subsumption queries are asked.
	* Normal statues should be true, use false for fast reference checks of correspondences uris.
	*/
	public static boolean PRE_CLASSIFY = true;
	
	
	/**
	* If activated only strict one to one alignments are extracted. In standard settings this should not be
	* actived. Works only when using efficient reasoning strategies. resp. does not work in brute force
	* approaches. Behaviour depends on  setting of ONE_TO_ONE_ONLYEQUIV.
	*/
	public static boolean ONE_TO_ONE = false;
	
	/**
	* If activated only one to many (from source to target) alignments are extracted.
	* In standard settings this should not be actived. Works only when using efficient reasoning strategies. resp.
	* does not work in brute force approaches.  
	*/
	public static boolean ONE_TO_MANY = false;
	
	/**
	* If activated only many to one (from source to target) are extracted.
	* In standard settings this should not be actived. Works only when using efficient reasoning strategies. resp.
	* does not work in brute force approaches.  
	*/
	public static boolean MANY_TO_ONE = false;
	
	/**
	* If activated, the chosen one to one setting, holds only for equivalenve relations.
	*/
	public static boolean ONE_TO_ONE_ONLYEQUIV = true;

	/**
	 * If activated no conflicts are computed except one-to-one conflicts.
	 */
	public static boolean DISABLE_REASONING = false;
	
}
