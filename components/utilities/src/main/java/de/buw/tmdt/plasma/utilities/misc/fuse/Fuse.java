package de.buw.tmdt.plasma.utilities.misc.fuse;

import java.io.Serializable;

/**
 * A bi-state object which can switch only from sane to non-sane but not vice versa.
 */
public interface Fuse<E extends Exception> extends Serializable {
	/**
	 * Switches the state of the fuse to destroyed. Can not be reversed. Multiple invocations have no effect after the first.
	 * @throws E thrown if the implementation can't destroy the fuse
	 */
	void destroy() throws E;

	/**
	 * Indicates if the fuse is sane or destroyed.
	 *
	 * @return true iff the fuse is sane
	 */
	boolean isSane();

	/**
	 * Indicates if the fuse is destroyed or sane.
	 *
	 * @return true iff the fuse is destroyed
	 */
	boolean isDestroyed();

	/**
	 * Switches the state of the fuse to destroyed like {@link SimpleFuse#destroy()} but returns true iff the fuse was sane before the operation.
	 *
	 * @return true iff the fuse was sane before the operation
	 * @throws E thrown if the implementation can't destroy the fuse
	 */
	boolean destroyIfSane() throws E;
}
