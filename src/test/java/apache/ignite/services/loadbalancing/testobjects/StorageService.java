package apache.ignite.services.loadbalancing.testobjects;

import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceContext;

import java.util.ArrayList;
import java.util.Collection;

/** Ignite service implementing {@link Storage}. */
public class StorageService implements Service, Storage {
    /** Storage. */
    private final Collection<Integer> storage = new ArrayList<>();

    /** {@inheritDoc} */
    @Override public void cancel(ServiceContext ctx) {
    }

    /** {@inheritDoc} */
    @Override public void init(ServiceContext ctx) {
    }

    /** {@inheritDoc} */
    @Override public void execute(ServiceContext ctx) {
    }

    /** {@inheritDoc} */
    @Override public void store(Integer val) {
        storage.add(val);
    }

    /** {@inheritDoc} */
    @Override public Integer[] contents() {
        return storage.toArray(new Integer[0]);
    }
}
