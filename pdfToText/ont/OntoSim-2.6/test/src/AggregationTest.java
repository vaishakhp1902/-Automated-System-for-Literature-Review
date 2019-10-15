import org.testng.annotations.Test;

import fr.inrialpes.exmo.ontosim.aggregation.GenericMean;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;;

public class AggregationTest {
   
    double[] vals={1,2,3,4,5,6,7,8,9,10};
    
    @Test(groups = { "full", "aggregation", "noling" })
    public void genericMeanArithmetic() {
	GenericMean m = GenericMean.getInstance(GenericMean.ARITHMETIC);
	assertEquals(m.getValue(vals),5.5);
    }
    
    @Test(groups = { "full", "aggregation", "noling" })
    public void genericMeanGeometric() {
	GenericMean m = GenericMean.getInstance(GenericMean.GEOMETRIC);
	double v = m.getValue(vals);
	assertTrue(v<4.529 && v>4.528);
    }
    
    @Test(groups = { "full", "aggregation", "noling" })
    public void genericMeanHarmonic() {
	GenericMean m = GenericMean.getInstance(GenericMean.HARMONIC);
	double v = m.getValue(vals);
	assertTrue(v<3.415 && v>3.414);
    }
    
}
