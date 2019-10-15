import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.extractor.Extractor;
import fr.inrialpes.exmo.ontosim.extractor.Hausdorff;
import fr.inrialpes.exmo.ontosim.extractor.Max;
import fr.inrialpes.exmo.ontosim.extractor.MaxCoupling;
import fr.inrialpes.exmo.ontosim.extractor.Min;
import fr.inrialpes.exmo.ontosim.extractor.matching.Matching;

import static org.testng.Assert.assertEquals;

public class ExtractorTest {
    
    public class AllEqualsMeasure implements Measure<Integer> {

	private final double v = Math.random();

	public double getDissim(Integer o1, Integer o2) {
	    return 1.-v;
	}

	public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
	    return TYPES.similarity;
	}

	public double getMeasureValue(Integer o1, Integer o2) {
	   return v;
	}

	public double getSim(Integer o1, Integer o2) {
	   return v;
	}
	
    }

    private Set<Integer> s1;
    private Set<Integer> s2;
    
    private Set<Integer> s3;
    private Set<Integer> s4;
    
    private Measure<Integer> m;
    
    @Test(groups = { "full", "noling" })
    public void dummyMeasure() {
	m = new IntegerDistance(10);
	assertEquals(m.getMeasureValue(3, 4),1.);
	assertEquals(m.getDissim(3, 4),1.0/10);
	assertEquals(m.getSim(3, 4),1.0-(1.0/10));
    }
    
    @Test(groups = { "full", "noling" })
    public void initData() {
	s1 = new HashSet<Integer>();
	s2 = new HashSet<Integer>();
	s1.add(1);s1.add(2);s1.add(3);
	s2.add(4);s2.add(5);s2.add(6);
	
	s3  = new HashSet<Integer>();
	s4 = new HashSet<Integer>();
	s3.add(1);s3.add(4);s3.add(5);
	s4.add(2);s4.add(3);s4.add(8);
	
    }
    
    /**
     * The distance matrix :
     *    4 5 6
     *    | | |	 
     * 1-|3|4|5|
     * 2-|2|3|4|
     * 3-|1|2|3|
     */
    // All matchings are minimal weight distance...
    /**
     * Another distance matrix :
     *    2 3 8
     *    | | |	 
     * 1-|1|2|7|
     * 4-|2|1|4|
     * 5-|3|2|3|
     */
   
    @Test(groups = { "full", "noling" }, dependsOnMethods = {"dummyMeasure","initData"})
    public void max() {
	Extractor e = new Max();
	Matching<Integer> match = e.extract(m, s1, s2);
	assertEquals(match.size(),1);
	assertEquals(match.contains(1, 4),false);
	assertEquals(match.contains(1, 5),false);
	assertEquals(match.contains(1, 6),true);
	
	assertEquals(match.contains(2, 4),false);
	assertEquals(match.contains(2, 5),false);
	assertEquals(match.contains(2, 6),false);
	
	assertEquals(match.contains(3, 4),false);
	assertEquals(match.contains(3, 5),false);
	assertEquals(match.contains(3, 6),false);
    }
    
    @Test(groups = { "full", "noling" }, dependsOnMethods = {"dummyMeasure","initData"})
    public void min() {
	Extractor e = new Min();
	Matching<Integer> match = e.extract(m, s1, s2);
	assertEquals(match.size(),1);
	assertEquals(match.contains(1, 4),false);
	assertEquals(match.contains(1, 5),false);
	assertEquals(match.contains(1, 6),false);
	
	assertEquals(match.contains(2, 4),false);
	assertEquals(match.contains(2, 5),false);
	assertEquals(match.contains(2, 6),false);
	
	assertEquals(match.contains(3, 4),true);
	assertEquals(match.contains(3, 5),false);
	assertEquals(match.contains(3, 6),false);
    }
    
    @Test(groups = { "full", "noling" }, dependsOnMethods = {"dummyMeasure","initData"})
    public void hausdorff() {
	Extractor e = new Hausdorff();
	Matching<Integer> match = e.extract(m, s1, s2);
	assertEquals(match.size(),2);
	
	assertEquals(match.contains(1, 4),true);
	assertEquals(match.contains(1, 5),false);
	assertEquals(match.contains(1, 6),false);
	
	assertEquals(match.contains(2, 4),false);
	assertEquals(match.contains(2, 5),false);
	assertEquals(match.contains(2, 6),false);
	
	assertEquals(match.contains(3, 4),false);
	assertEquals(match.contains(3, 5),false);
	assertEquals(match.contains(3, 6),true);
    }
    
    @Test(groups = { "full", "noling" }, dependsOnMethods = {"dummyMeasure","initData"})
    public void maxcoupling() {
	Extractor e = new MaxCoupling();
	Matching<Integer> match = e.extract(m, s3, s4);
	assertEquals(match.size(),Math.min(s3.size(), s4.size()));
	assertEquals(match.contains(1, 2),true);
	assertEquals(match.contains(1, 3),false);
	assertEquals(match.contains(1, 8),false);
	
	assertEquals(match.contains(4, 2),false);
	assertEquals(match.contains(4, 3),true);
	assertEquals(match.contains(4, 8),false);
	
	assertEquals(match.contains(5, 2),false);
	assertEquals(match.contains(5, 3),false);
	assertEquals(match.contains(5, 8),true);
	
	// try if Hungarian work with a matrix which contains all the same values
	match = e.extract(new AllEqualsMeasure(),s3,s4);
	assertEquals(match.size(),Math.min(s3.size(), s4.size()));
	
    }
    
}
