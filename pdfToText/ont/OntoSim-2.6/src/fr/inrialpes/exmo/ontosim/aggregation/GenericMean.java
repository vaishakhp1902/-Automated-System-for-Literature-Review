/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendes France
 *   
 *   GenericMean.java is part of OntoSim.
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

package fr.inrialpes.exmo.ontosim.aggregation;

import java.util.HashMap;
import java.util.Map;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.extractor.matching.Matching;


/**
 * Generic mean implementation
 * It follows the formula m=inversePhi(sum(phi(x)/n)
 * phi(x)=x (arithmetic mean): use new GenericMean(GenericMean.ARITHMETIC)
 * phi(x)=x^ (quadratic mean): use new GenericMean(GenericMean.QUADRATIC)
 * phi(x) = ln(x) (geometric mean): use new GenericMean(GenericMean.GEOMETRIC)
 * phi(x) = 1/x (harmonic mean): use new GenericMean(GenericMean.HARMONIC)
 * ...
 *
 * To implement a specific weighted average, it is enough to
 * override method protected int weight(double x) and add the specific 
 * weighting formula
 * 
 * To implement another mean, implement a new Type
 * 
 * @author Jérôme David
 *
 */
public class GenericMean extends AggregationScheme {
    
    private static final Map<Type,GenericMean> INSTANCES=new HashMap<Type,GenericMean>();
    
    
    public synchronized static GenericMean getInstance(Type t) {
	/*Map<Type,GenericMean> mapType = INSTANCES.get(c);
	if (mapType==null) {
	    mapType=new HashMap<Type,GenericMean>();
	    INSTANCES.put(c, mapType);
	}*/
	GenericMean m = INSTANCES.get(t);//mapType.get(t);
	if (m==null) {
	    m=new GenericMean(t);
	    INSTANCES.put(t,m);
	}
	return m;
    }
    
    public interface Type {
	public double phi(double x);
	public double inversePhi(double x);
    }
    
    /*
     * phi functions implementations
     */
    public static final Type ARITHMETIC = new  Type() {
	@Override
	public final double inversePhi(double x) {
	    return x;
	}

	@Override
	public final double phi(double x) {
	    return x;
	}
    };

    public static final Type QUADRATIC = new  Type() {
	@Override
	public final double inversePhi(double x) {
	    return Math.sqrt(x);
	}

	@Override
	public double phi(double x) {
	    return x*x;
	}
    };
    
    
    public static final Type GEOMETRIC = new  Type() {
	@Override
	public final double inversePhi(double x) {
	    return Math.exp(x);
	}

	@Override
	public final double phi(double x) {
	    return Math.log(x);
	}
    };
    
    public static final Type HARMONIC = new  Type() {
	@Override
	public final double inversePhi(double x) {
	    return 1/x;
	}

	@Override
	public final double phi(double x) {
	    return 1/x;
	}
    };
    

    protected Type p;
    
    protected GenericMean(Type t) {
	this.p=t;
    }
    
    protected GenericMean() {
	this(GenericMean.ARITHMETIC);
    }
    
    
    @Override
    public final double getValue(double[] vals) {
	double sum=0;
	double sumW=0;
	for (double v : vals) {
	    int w = weight(v);
	    sum+=w*p.phi(v);
	    sumW+=w;
	}
	return p.inversePhi(sum/sumW);
    }
    
    
    protected int weight(double x) {return 1;}


    public final <O> double getValue(Measure<O> measure, Matching<O> matching) {
	double sum=0;
	double sumW=0;
	for (Matching.Entry<O> entry : matching) {
	    double v = measure.getMeasureValue(entry.getSource(), entry.getTarget());
	    int w = weight(v);
	    sum+=w*p.phi(v);
	    sumW+=w;
	}
	return p.inversePhi(sum/sumW);
    }        
}
