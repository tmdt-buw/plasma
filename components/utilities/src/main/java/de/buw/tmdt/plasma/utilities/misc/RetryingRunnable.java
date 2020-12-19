package de.buw.tmdt.plasma.utilities.misc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class RetryingRunnable implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(RetryingRunnable.class);

	private final Callable<Boolean> condition;
	private final long delayInMS;
	private final int maxTries;
	private final Runnable successCallback;
	private final Consumer<Exception> failureCallback;
	private int tries;

	/**
	 * Creates a runnable which will evaluates it's condition on start and then every {@code delayInMS} milliseconds for a maximum of {@code maxTries} times.
	 * The first time the condition evaluates to true the successCallback is invoked and the runnable terminates. If an exception occures or the maximum of
	 * retries is reached the {@code failureCallback} is invoked.
	 *
	 * @param condition       the condition which is checked multiple times
	 * @param delayInMS       retry delay between evaluations of the condition
	 * @param maxTries        maximum amount of condition evaluations
	 * @param successCallback invoked when condition evaluates to true
	 * @param failureCallback invoked when exception occurres or condition was never evaluated to true for all tries
	 */
	public RetryingRunnable(
			@NotNull Callable<Boolean> condition,
			long delayInMS,
			int maxTries,
			@Nullable Runnable successCallback,
			@Nullable Consumer<Exception> failureCallback
	) {
		this.condition = condition;
		this.delayInMS = delayInMS;
		this.maxTries = maxTries;
		this.successCallback = successCallback;
		this.failureCallback = failureCallback;
		this.tries = 0;
	}

	@SuppressWarnings("BusyWait - that's exactly what we want here.")
	@Override
	public void run() {
		long lastRun = System.currentTimeMillis();

		while (tries++ <= maxTries) {
			try {
				if (condition.call()) {
					success();
					return;
				}
			} catch (Exception e) {
				fail(e);
			}
			long now = System.currentTimeMillis();
			while (lastRun + delayInMS > now) {
				try {
					Thread.sleep(lastRun + delayInMS - now);
					break;
				} catch (InterruptedException e) {
					logger.warn("Retrying thread was interrupted unexpectedly while waiting.", e);
				}
				now = System.currentTimeMillis();
			}
			lastRun += delayInMS;
		}
		fail(new RuntimeException("Operation was not successful after " + maxTries + " tries with a delay of " + delayInMS + "ms."));
	}

	private void success() {
		logger.trace("Successful after {} tries.", tries);
		if (this.successCallback != null) {
			this.successCallback.run();
		}
	}

	private void fail(Exception e) {
		logger.trace("Failed after {} tries", maxTries, e);
		if (this.failureCallback != null) {
			this.failureCallback.accept(e);
		}
	}
}
