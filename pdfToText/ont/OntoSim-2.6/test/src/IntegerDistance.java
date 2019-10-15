

import fr.inrialpes.exmo.ontosim.Measure;

public class IntegerDistance implements Measure<Integer> {

    public int normalize;
    
    public IntegerDistance(int normalize) {
	this.normalize=normalize;
    }
    @Override
    public double getDissim(Integer o1, Integer o2) {
	return getMeasureValue(o1,o2)/normalize;
    }

    @Override
    public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
	return TYPES.distance;
    }

    @Override
    public double getMeasureValue(Integer o1, Integer o2) {
	return Math.abs(o1-o2);
    }

    @Override
    public double getSim(Integer o1, Integer o2) {
	return 1-getDissim(o1,o2);
    }

}
