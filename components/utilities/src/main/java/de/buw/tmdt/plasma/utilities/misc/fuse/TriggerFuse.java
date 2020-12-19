package de.buw.tmdt.plasma.utilities.misc.fuse;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * A bi-state object which can switch only from sane to non-sane but not vice versa.
 * When switching from sane to destroyed it executes the given {@link Runnable}. This will be done only once.
 */
public final class TriggerFuse<E extends Exception, A extends Runnable & Serializable> extends AbstractFuse<E> {
	private static final long serialVersionUID = 1885379641588750963L;
	private final A runnable;

	public TriggerFuse(@NotNull A runnable) {
		this.runnable = runnable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void destroy() throws E {
		if (isSane()) {
			runnable.run();
			super.destroy();
		}
	}
}
