package fr.inrialpes.exmo.ontosim.extractor;

import java.util.Collection;
import java.util.Iterator;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.extractor.matching.Matching;

public class DummyExtractor extends AbstractExtractor {

    private final class DummyMatching<O> implements Matching<O> {
	Collection<? extends O> src;
	Collection<? extends O> trg;
	
	public DummyMatching(Collection<? extends O> src, Collection<? extends O> trg) {
	    this.src=src;
	    this.trg=trg;
	}
	
	public boolean add(O s, O t) {
	    throw new UnsupportedOperationException();
	}

	public void clear() {
	    throw new UnsupportedOperationException();
	}

	public boolean contains(O s, O t) {
	   return src.contains(s) && trg.contains(t);
	}

	public int size() {
	   return src.size()*trg.size();
	}

	@Override
	public Matching<O> transposeView() {
	    return new DummyMatching<O>(trg,src);
	}

	@Override
	public Iterator<Entry<O>> iterator() {
	    return new Iterator<Entry<O>>() {
		private Iterator<? extends O> srcIt=src.iterator();
		private Iterator<? extends O> trgIt=trg.iterator();
		
		private O currentSrc=srcIt.next();
		
		@Override
		public boolean hasNext() {
		    return srcIt.hasNext()||trgIt.hasNext();
		}

		@Override
		public Entry<O> next() {
		    if (!trgIt.hasNext()) {
			currentSrc=srcIt.next();
			trgIt=trg.iterator();
		    }
		    return new Matching.Entry<O>(currentSrc,trgIt.next());
		}

		@Override
		public void remove() {
		    throw new UnsupportedOperationException();
		    
		}
		
	    };
	}
	
    }
    
    
    public final <O> Matching<O> extract(Measure<O> m, Collection<? extends O> s, Collection<? extends O> t) {
	return new DummyMatching<O>(s,t);
    }

}
