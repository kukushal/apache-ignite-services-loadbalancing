package apache.ignite.services.loadbalancing.testobjects;

/** A storage interface. */
public interface Storage {
    /** Store a value. */
    void store(Integer val);

    /** @return All stored values. */
    Integer[] contents();
}
