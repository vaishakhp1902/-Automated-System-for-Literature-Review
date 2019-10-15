import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.set.AverageLinkage;
import fr.inrialpes.exmo.ontosim.set.FullLinkage;
import fr.inrialpes.exmo.ontosim.set.Hausdorff;
import fr.inrialpes.exmo.ontosim.set.MaxCoupling;
import fr.inrialpes.exmo.ontosim.set.SetMeasure;
import fr.inrialpes.exmo.ontosim.set.SingleLinkage;


public class SetMeasureTest {

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
    public void initSetMeasureTest() {
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
   
    @Test(groups = { "full", "noling" }, dependsOnMethods = {"dummyMeasure","initSetMeasureTest"})
    public void haussdorffSM() {
	SetMeasure<Integer> sm = new Hausdorff<Integer>(m);
	assertEquals(sm.getMeasureValue(s1, s2),3.);
	assertEquals(sm.getMeasureValue(s3, s4),3.);
	
	assertEquals(sm.getDissim(s1, s2),0.3);
	assertEquals(sm.getDissim(s3, s4),0.3);
	
	assertEquals(sm.getSim(s1, s2),0.7);
	assertEquals(sm.getSim(s3, s4),0.7);
	
    }
    
    @Test(groups = { "full", "noling" }, dependsOnMethods = {"dummyMeasure","initSetMeasureTest"})
    public void averageLinkageSM() {
	SetMeasure<Integer> sm = new AverageLinkage<Integer>(m);
	assertEquals(sm.getMeasureValue(s1, s2),3.);
	assertEquals(sm.getMeasureValue(s3, s4),25./9);
	
	assertEquals(sm.getDissim(s1, s2),0.30000000000000004);//3./10);//
	assertEquals(sm.getDissim(s3, s4),25./90);
	
	assertEquals(sm.getSim(s1, s2),0.7);
	assertEquals(sm.getSim(s3, s4),65./90);
    }
    
    @Test(groups = { "full", "noling" }, dependsOnMethods = {"dummyMeasure","initSetMeasureTest"})
    public void fullLinkageSM() {
	SetMeasure<Integer> sm = new FullLinkage<Integer>(m);
	assertEquals(sm.getMeasureValue(s1, s2),5.);
	assertEquals(sm.getMeasureValue(s3, s4),7.);
	
	assertEquals(sm.getDissim(s1, s2),0.5);
	assertEquals(sm.getDissim(s3, s4),0.7);
	
	assertEquals(sm.getSim(s1, s2),0.5);
	assertEquals(sm.getSim(s3, s4),0.30000000000000004);//3./10);//
    }
    
    @Test(groups = { "full", "noling" }, dependsOnMethods = {"dummyMeasure","initSetMeasureTest"})
    public void singleLinkageSM() {
	SetMeasure<Integer> sm = new SingleLinkage<Integer>(m);
	assertEquals(sm.getMeasureValue(s1, s2),1.);
	assertEquals(sm.getMeasureValue(s3, s4),1.);
	
	assertEquals(sm.getDissim(s1, s2),0.1);
	assertEquals(sm.getDissim(s3, s4),0.1);
	
	assertEquals(sm.getSim(s1, s2),0.9);
	assertEquals(sm.getSim(s3, s4),0.9);
    }
    
    @Test(groups = { "full", "noling" }, dependsOnMethods = {"dummyMeasure","initSetMeasureTest"})
    public void maxCouplingSM() {
	SetMeasure<Integer> sm = new MaxCoupling<Integer>(m);
	assertEquals(sm.getMeasureValue(s1, s2),3.);
	assertEquals(sm.getMeasureValue(s3, s4),5./3);
	
	assertEquals(sm.getDissim(s1, s2),0.3);
	assertEquals(sm.getDissim(s3, s4),1./6);
	
	assertEquals(sm.getSim(s1, s2),0.7000000000000001);//7./10);//
	assertEquals(sm.getSim(s3, s4),5./6);
    }
    
}
