package de.buw.tmdt.plasma.utilities.statemachine;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings("AccessToStaticFieldLockedOnInstance")
public class StateMachine<I extends Serializable & Comparable<I>> implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(StateMachine.class);

	private static final long serialVersionUID = -254971899608633890L;
	private final Map<I, Set<I>> transitionsLookUp;
	private final Set<Transition<I>> transitions;
	private final Set<I> states;
	@SuppressWarnings("FieldCanBeLocal - allows for easier debugging")
	private final String name;
	private final String prefix;
	private I currentState;

	/**
	 * Creates a state machine with the given name on the given states which allows only the given transition and is initialized on the give start state.
	 *
	 * @param name        name of the state machine
	 * @param states      the complete set of states
	 * @param startState  the start state
	 * @param transitions the legal transitions
	 */
	public StateMachine(@NotNull String name, @NotNull Set<I> states, @NotNull I startState, @NotNull Set<Transition<I>> transitions) {
		this.name = name;
		this.prefix = this.name + ": ";
		this.states = new TreeSet<>(states);
		this.transitionsLookUp = new TreeMap<>();
		for (Transition<I> transition : transitions) {
			if (transition == null) {
				throw new IllegalArgumentException(prefixMessage("Transitions must not contain null."));
			}
			//redundant checks for more efficient branch prediction
			if (this.states.contains(transition.getFrom()) && this.states.contains(transition.getTo())) {
				this.transitionsLookUp.computeIfAbsent(
						transition.getFrom(), ignored -> new TreeSet<>()
				).add(transition.getTo());
			} else {
				I illegalState;
				if (!this.states.contains(transition.getFrom())) {
					illegalState = transition.getFrom();
				} else {
					illegalState = transition.getTo();
				}
				throw new IllegalArgumentException(prefixMessage(
						"Transition to State " + illegalState + " illegal since it is not in the State-Set."));
			}
		}
		this.transitions = new TreeSet<>(transitions);
		if (this.states.contains(startState)) {
			currentState = startState;
		} else {
			throw new IllegalArgumentException(prefixMessage("Start State " + startState + " is illegal since it is not in the State Set."));
		}
	}

	private String prefixMessage(String message) {
		return prefix + message;
	}

	/**
	 * Returns the state in which the state machine is currently.
	 *
	 * @return the current state
	 */
	public synchronized @NotNull I getCurrentState() {
		return currentState;
	}

	/**
	 * Evaluates if the state machine is in the given state.
	 *
	 * @param state the state where the current state is compared to
	 * @return true iff {@code state.compareTo(this.state) == 0}
	 */
	public synchronized boolean isInState(@NotNull I state) {
		logger.trace(prefixMessage("Returning state: {}"), currentState);
		return this.currentState.compareTo(state) == 0;
	}

	/**
	 * Checks if the state machine has the passed state in it's state set.
	 *
	 * @param state the state for which the lookup is done
	 * @return true iff the state machine contains the given state
	 */
	public boolean containsState(I state) {
		return states.contains(state);
	}

	/**
	 * Returns an unmodifiable set of all states of the state machine.
	 *
	 * @return an unmodifiable set of all states
	 */
	public Set<I> getStates() {
		return Collections.unmodifiableSet(states);
	}

	/**
	 * Returns an unmodifiable set of all legal transitions of the state machine.
	 *
	 * @return an unmodifiable set of all legal transitions
	 */
	public Set<Transition<I>> getTransitions() {
		return Collections.unmodifiableSet(transitions);
	}

	/**
	 * Conditional state change. Transition is only executed when the state machine is in {@code expectedCurrentState}. If the state machine is not in state
	 * {@code expectedCurrentState} false is returned. If the transitions is successful true is returned. If the state machine is in state {@code
	 * expectedCurrentState} but the transition to {@code futureState} is not legal an {@link UnsupportedOperationException} is thrown.
	 *
	 * @param futureState          the state which should be entered conditionally
	 * @param expectedCurrentState the state from which the transition should be done
	 * @return true iff the transition was successfully executed
	 * @throws UnsupportedOperationException if the state machine is in state {@code expectedCurrentState} but the transition to {@code futureState} is not
	 *                                       legal
	 */
	public synchronized boolean changeStateIfCurrentlyIn(
			@NotNull I futureState,
			@NotNull I expectedCurrentState
	) throws UnsupportedOperationException {
		logger.trace(prefixMessage("Conditional state change from {} to {}"), expectedCurrentState, futureState);
		if (this.currentState.compareTo(expectedCurrentState) == 0) {
			changeState(futureState);
			return true;
		}
		return false;
	}

	//define more precise exception type
	public synchronized void changeState(@NotNull I futureState) throws UnsupportedOperationException {
		logger.trace(prefixMessage("Changing state to: {}"), futureState);
		if (!states.contains(futureState)) {
			throw new IllegalArgumentException(prefixMessage("Future State " + futureState + " is illegal since it is not in the State Set."));
		}
		if (!transitionsLookUp.getOrDefault(currentState, Collections.emptySet()).contains(futureState)) {
			throw new UnsupportedOperationException(prefixMessage("Transition from " + currentState + " to " + futureState + " is illegal."));
		}
		currentState = futureState;
		logger.trace(prefixMessage("Changed state successfully to: {}"), currentState);
	}

	@SuppressWarnings("ReturnOfThis")
	public static class Builder<I extends Serializable & Comparable<I>> {
		private final Set<I> states;
		private final Set<Transition<I>> transitions;
		private I startState;
		private String name;

		public Builder() {
			this(null);
		}

		public Builder(String name) {
			this.name = name;
			this.states = new TreeSet<>();
			this.transitions = new TreeSet<>();
		}

		@SafeVarargs
		@NotNull
		public final Builder<I> withStates(@NotNull I... stateIDs) {
			this.states.addAll(Arrays.asList(stateIDs));
			return this;
		}

		@NotNull
		public Builder<I> withTransition(@NotNull I fromID, @NotNull I toID) {
			this.transitions.add(new Transition<>(fromID, toID));
			return this;
		}

		@NotNull
		public Builder<I> withStartState(@NotNull I startStateID) {
			this.startState = startStateID;
			return this;
		}

		@NotNull
		public Builder<I> withName(@NotNull String name) {
			this.name = name;
			return this;
		}

		/**
		 * Registers all transitions according to the natural order of the array.
		 * This means each transition from element {@code stateIDs[i]} to it's successor {@code stateIDs[i+1]}.
		 *
		 * @param stateIDs the sequence of state ids
		 * @return the {@code Builder} on which the method was invoked
		 */
		@SafeVarargs
		public final @NotNull Builder<I> withTransitionSequence(@NotNull I... stateIDs) {
			for (int i = 0; i < stateIDs.length - 1; i++) {
				withTransition(stateIDs[i], stateIDs[i + 1]);
			}
			return this;
		}

		/**
		 * Registers all transitions according to the hierarchy of the array.
		 * This means each transition from elements {@code stateIDs[i]} to elements {@code stateIDs[k]} iff {@code i<k}
		 *
		 * @param stateIDs the hierarchy of state ids
		 * @return the {@code Builder} on which the method was invoked
		 */
		@SafeVarargs
		public final @NotNull Builder<I> withTransitionSequenceAndShortcuts(@NotNull I... stateIDs) {
			if (stateIDs.length != 0) {
				Set<I> previousStates = new TreeSet<>();
				previousStates.add(stateIDs[0]);
				for (int i = 1; i < stateIDs.length; i++) {
					I currentState = stateIDs[i];
					for (I state : previousStates) {
						withTransition(state, currentState);
					}
					previousStates.add(currentState);
				}
			}
			return this;
		}

		public StateMachine<I> build() {
			return new StateMachine<>(name, states, startState, transitions);
		}
	}
}
