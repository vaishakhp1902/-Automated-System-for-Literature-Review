import org.testng.annotations.Test;

import fr.inrialpes.exmo.ontosim.vector.KendallTau;


public class VectorMeasureTest {

    @Test(groups = { "full", "noling" })
    public void kendall() {
	KendallTau m = new KendallTau(); 
	double v1[] = {1,2,3,4,5,6,7,8,9,10};
	double v2[] = {3,1,4,2,6,5,9,8,10,7};
	
	
	
	//System.out.println(m.getMeasureValue(v1,v2));
	
	double[] v3 = {6.5,9,9,12,12,12,13,14};
	double[] v4 = {8.5,8.5,6.5,11,12,11,15,13};
	
	//System.out.println(m.getMeasureValue(v3,v4));
	
	
	double[] v5 = {1,2,3,4,5,6,7,8,9,10};
	double[] v6 = {1,1,1,1,1,1,1,1,1,1};
	double[] v7= {10,9,8,7,6,5,4,3,2,1};
	double[] v8= {2,2,2,2,2,2,2,2,2,2,2};
	
	/*System.out.println(m.getMeasureValue(v6,v7));
	System.out.println(m.getMeasureValue(v6,v6));
	System.out.println(m.getMeasureValue(v6,v8));*/
    }
}
