/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   DistFile.java is part of OntoSim.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DistFile {

    /**
     * WARNING: Tentative description (may not be accurate)
     * Reads an index file made of double floats separated by ";"
     * and prints out the index?
     *
     * @param args: the name of the file to 
     * @throws IOException when the file cannot be read
     */
    public static void main(String[] args) throws IOException {
	File f1 = new File(args[0]);
	
	int[] dist = new int[200];
	
	BufferedReader br = new BufferedReader(new FileReader(f1));
	
	String line=null;
	while ((line=br.readLine())!=null) {
	    String nb = line.substring(line.lastIndexOf(';')+1);
	    double val = Double.parseDouble(nb);
	    int idx = (int) (val/0.005);
	    //System.out.println(idx);
	    if (idx==dist.length) idx--;
	    dist[idx]++;
	 }
	
	for (int i=0 ; i< dist.length ; i++) {
	    System.out.println(i+"\t"+dist[i]);
	}

    }

}
