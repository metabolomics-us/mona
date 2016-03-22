package edu.ucdavis.fiehnlab.mona.backend.curation.writer;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

/**
 * Created by wohlgemuth on 3/22/16.
 */
public abstract class WriterAdapter implements ItemWriter<Spectrum> {
    @Override
    public void write(List<? extends Spectrum> list) throws Exception {
        for(int i = 0; i < list.size(); i++){
            write(list.get(i));
        }
    }

    protected abstract void write(Spectrum spectrum);
}
