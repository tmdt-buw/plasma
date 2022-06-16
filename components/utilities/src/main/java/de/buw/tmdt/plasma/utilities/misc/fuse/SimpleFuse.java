package de.buw.tmdt.plasma.utilities.misc.fuse;

/**
 * A bi-state object which can switch only from sane to non-sane but not vice versa.
 * This can be used as a guard for code which should be executed only once.
 */
public final class SimpleFuse extends AbstractFuse<RuntimeException> {

    private static final long serialVersionUID = -4944414086744562217L;

    /**
     * Creates a new sane Fuse.
     */
    public SimpleFuse() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void destroy() {
        super.destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isSane() {
        return super.isSane();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isDestroyed() {
        return super.isDestroyed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean destroyIfSane() {
        return super.destroyIfSane();
    }
}
