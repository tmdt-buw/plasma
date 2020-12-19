package de.buw.tmdt.plasma.utilities.misc.fuse;

public abstract class AbstractFuse<E extends Exception> implements Fuse<E> {
	private static final long serialVersionUID = 3885484957456921809L;
	private boolean sane = true;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("RedundantThrows")
	@Override
	public void destroy() throws E {
		sane = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSane() {
		return sane;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDestroyed() {
		return !sane;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean destroyIfSane() throws E {
		if (sane) {
			destroy();
			return true;
		}
		return false;
	}
}
