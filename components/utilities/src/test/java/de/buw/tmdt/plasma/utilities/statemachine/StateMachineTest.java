package de.buw.tmdt.plasma.utilities.statemachine;


import de.buw.tmdt.plasma.utilities.collections.ArrayUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static de.buw.tmdt.plasma.utilities.statemachine.StateMachineTest.TestState.*;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StateMachineTest {
	private static final Logger logger = LoggerFactory.getLogger(StateMachineTest.class);
	private static final String TEST_NAME = "testStateMachineName";

	@Test
	public void creationSuccessful() {
		Set<TestState> states = ArrayUtilities.toCollection(HashSet::new, TestState.values());

		Set<Transition<TestState>> transitions = new HashSet<>();
		transitions.add(new Transition<>(STATE1, STATE2));
		transitions.add(new Transition<>(STATE1, STATE3));
		transitions.add(new Transition<>(STATE2, STATE3));
		transitions.add(new Transition<>(STATE3, STATE4));
		StateMachine<TestState> stateMachine = new StateMachine<>("testName", states, STATE1, transitions);

		logger.trace("Verify that StateMachine has created right states");
		assertEquals(states, stateMachine.getStates());

		logger.trace("Verify that StateMachine has created right transitions");
		assertEquals(transitions, stateMachine.getTransitions());

		logger.trace("Verify that initial state is current state");
		Assert.assertEquals(STATE1, stateMachine.getCurrentState());
	}

	@Test(expected = IllegalArgumentException.class)
	public void creationTransitionToUndefined() {
		logger.trace("Add only one state to StateMachine");
		Set<TestState> states = Collections.singleton(STATE1);

		Set<Transition<TestState>> transitions = new HashSet<>();
		transitions.add(new Transition<>(STATE1, STATE2));
		new StateMachine<>(TEST_NAME, states, STATE1, transitions);
	}

	@Test
	public void testStatesWithSameIDs() {
		Set<TestState> states = ArrayUtilities.toCollection(HashSet::new, STATE1, STATE1, STATE2);

		Set<Transition<TestState>> transitions = new HashSet<>();
		transitions.add(new Transition<>(STATE1, STATE2));
		transitions.add(new Transition<>(STATE1, STATE1));

		StateMachine<TestState> stateMachine = new StateMachine<>(TEST_NAME, states, STATE1, transitions);
		assertTrue(stateMachine.isInState(STATE1));
		assertTrue(stateMachine.isInState(STATE1));
		stateMachine.changeStateIfCurrentlyIn(STATE2, STATE1);
		assertTrue(stateMachine.isInState(STATE2));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testTakeUndefinedTransition() {
		Set<TestState> states = ArrayUtilities.toCollection(HashSet::new, STATE1, STATE2);
		StateMachine<TestState> stateMachine = new StateMachine<>(TEST_NAME, states, STATE1, new HashSet<>());

		assertTrue(stateMachine.isInState(STATE1));
		stateMachine.changeState(STATE2);
	}

	@Test
	public void createOneStateSelfTransitions() {
		Set<TestState> states = Collections.singleton(STATE1);

		Set<Transition<TestState>> transitions = new HashSet<>();
		transitions.add(new Transition<>(STATE1, STATE1));
		StateMachine<TestState> stateMachine = new StateMachine<>(TEST_NAME, states, STATE1, transitions);

		logger.trace("Verify that StateMachine has created right states");
		assertEquals(states, stateMachine.getStates());

		logger.trace("Verify that StateMachine has created right transitions");
		assertEquals(transitions, stateMachine.getTransitions());

		logger.trace("Verify that initial state is current state");
		Assert.assertEquals(STATE1, stateMachine.getCurrentState());
	}

	@Test
	public void testMultipleSameTransition() {
		logger.trace("Create States");
		Set<TestState> states = ArrayUtilities.toCollection(HashSet::new, STATE1, STATE2);

		logger.trace("Create Transitions");
		Set<Transition<TestState>> transitions = new HashSet<>();
		transitions.add(new Transition<>(STATE1, STATE2));
		transitions.add(new Transition<>(STATE1, STATE2));

		StateMachine<TestState> stateMachine = new StateMachine<>(TEST_NAME, states, STATE1, transitions);

		stateMachine.changeState(STATE2);
		assertTrue(stateMachine.isInState(STATE2));
	}

	@Test
	public void testTraverseStates() {
		logger.trace("Create States");
		Set<TestState> states = ArrayUtilities.toCollection(HashSet::new, STATE1, STATE2, STATE3);

		Set<Transition<TestState>> transitions = new HashSet<>();
		transitions.add(new Transition<>(STATE1, STATE2));
		transitions.add(new Transition<>(STATE2, STATE3));
		transitions.add(new Transition<>(STATE3, STATE1));

		StateMachine<TestState> stateMachine = new StateMachine<>(TEST_NAME, states, STATE1, transitions);

		logger.trace("Traverse all defined transitions(one cycle) - with changeState()");
		assertTrue(stateMachine.isInState(STATE1));
		stateMachine.changeState(STATE2);
		assertTrue(stateMachine.isInState(STATE2));
		stateMachine.changeState(STATE3);
		assertTrue(stateMachine.isInState(STATE3));
		stateMachine.changeState(STATE1);
		assertTrue(stateMachine.isInState(STATE1));

		logger.trace("Traverse all defined transitions(one cycle) - with changeState()");
		stateMachine.changeStateIfCurrentlyIn(STATE2, STATE1);
		assertTrue(stateMachine.isInState(STATE2));
		stateMachine.changeStateIfCurrentlyIn(STATE3, STATE2);
		assertTrue(stateMachine.isInState(STATE3));
		stateMachine.changeStateIfCurrentlyIn(STATE1, STATE3);
		assertTrue(stateMachine.isInState(STATE1));
	}

	@Test
	public void testContainsUndefinedSameState() {
		Set<TestState> states = ArrayUtilities.toCollection(HashSet::new, STATE1, STATE2);

		StateMachine<TestState> stateMachine = new StateMachine<>(TEST_NAME, states, STATE1, new HashSet<>());
		assertTrue(stateMachine.containsState(STATE2));
	}

	@Test
	public void traverseConditionallyLegalTransition() {
		StateMachine<TestState> stateMachine = new StateMachine.Builder<TestState>(TEST_NAME)
				.withStates(STATE1, STATE2)
				.withTransition(STATE1, STATE2)
				.withStartState(STATE1)
				.build();

		assertTrue(stateMachine.changeStateIfCurrentlyIn(STATE2, STATE1));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void traverseConditionallyIllegalTransition() {
		Set<TestState> states = ArrayUtilities.toCollection(HashSet::new, STATE1, STATE2);

		StateMachine<TestState> stateMachine = new StateMachine<>(TEST_NAME, states, STATE1, new HashSet<>());
		stateMachine.changeStateIfCurrentlyIn(STATE2, STATE1);
	}

	@Test
	public void traverseNotConditionallyLegalTransition() {
		StateMachine<TestState> stateMachine = new StateMachine.Builder<TestState>(TEST_NAME)
				.withStates(STATE1, STATE2)
				.withTransition(STATE2, STATE2)
				.withStartState(STATE1)
				.build();

		assertFalse(stateMachine.changeStateIfCurrentlyIn(STATE2, STATE2));
	}

	@Test
	public void traverseNotConditionallyIllegalTransition() {
		StateMachine<TestState> stateMachine = new StateMachine.Builder<TestState>(TEST_NAME)
				.withStates(STATE1, STATE2)
				.withTransition(STATE1, STATE2)
				.withStartState(STATE1)
				.build();

		assertFalse(stateMachine.changeStateIfCurrentlyIn(STATE2, STATE2));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void traverseIllegalStateExceptionResult() {
		Set<TestState> states = ArrayUtilities.toCollection(HashSet::new, STATE1, STATE2);

		StateMachine<TestState> stateMachine = new StateMachine<>(TEST_NAME, states, STATE1, new HashSet<>());
		stateMachine.changeState(STATE2);
	}

	@Test(expected = NullPointerException.class)
	public void createWithNullArgs() {
		//noinspection ConstantConditions
		new StateMachine<>(TEST_NAME, null, null, null);
	}

	@Test(expected = NullPointerException.class)
	public void buildStateMachineWithNull() {
		StateMachine.Builder<TestState> builder = new StateMachine.Builder<>(TEST_NAME);
		builder.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void buildStateMachineUndefinedStartState() {
		StateMachine.Builder<TestState> builder = new StateMachine.Builder<>(TEST_NAME);
		builder.withStartState(STATE1);
		builder.build();
	}

	@Test
	public void buildStateMachine() {
		StateMachine.Builder<TestState> builder = new StateMachine.Builder<>(TEST_NAME);
		Transition<TestState> transition1 = new Transition<>(STATE1, STATE2);
		Transition<TestState> transition2 = new Transition<>(STATE2, STATE3);
		Transition<TestState> transition3 = new Transition<>(STATE3, STATE4);

		builder.withStates(TestState.values())
				.withStartState(STATE1)
				.withTransition(transition1.getFrom(), transition1.getTo())
				.withTransitionSequence(STATE2, STATE3, STATE4);

		logger.trace("Build StateMachine");
		StateMachine<TestState> stateMachine = builder.build();

		logger.trace("Verify content");
		assertEquals(TestState.values().length, stateMachine.getStates().size());
		assertTrue(stateMachine.containsState(STATE1));
		assertTrue(stateMachine.containsState(STATE2));
		assertTrue(stateMachine.containsState(STATE3));
		assertTrue(stateMachine.containsState(STATE4));

		assertEquals(3, stateMachine.getTransitions().size());
		assertTrue(stateMachine.getTransitions().contains(transition1));
		assertTrue(stateMachine.getTransitions().contains(transition2));
		assertTrue(stateMachine.getTransitions().contains(transition3));
	}

	@Test
	public void buildStateMachineWithTransitionSequenceOfOneState() {
		StateMachine.Builder<TestState> builder = new StateMachine.Builder<>(TEST_NAME);
		Transition<TestState> loop = new Transition<>(STATE1, STATE1);

		builder.withStates(STATE1, STATE2)
				.withStartState(STATE1)
				.withTransitionSequence(STATE1, STATE1, STATE1);

		logger.trace("Build StateMachine");
		StateMachine<TestState> stateMachine = builder.build();

		logger.trace("Verify self transition");
		assertEquals(1, stateMachine.getTransitions().size());
		assertTrue(stateMachine.getTransitions().contains(loop));
	}

	@Test
	public void buildStateMachineWithTransitionShortcuts() {
		StateMachine.Builder<TestState> builder = new StateMachine.Builder<>(TEST_NAME);

		logger.trace("Build expected transitions");
		Set<Transition<TestState>> expectedTransitions = ArrayUtilities.toCollection(
				HashSet::new,
				//from STATE1
				new Transition<>(STATE1, STATE2),
				new Transition<>(STATE1, STATE3),
				new Transition<>(STATE1, STATE4),
				//from STATE2
				new Transition<>(STATE2, STATE3),
				new Transition<>(STATE2, STATE4),
				//from STATE3
				new Transition<>(STATE3, STATE4)
		);

		logger.trace("Build StateMachine");
		builder.withStates(STATE1, STATE2, STATE3, STATE4)
				.withStartState(STATE1)
				.withTransitionSequenceAndShortcuts(STATE1, STATE2, STATE3, STATE4);

		logger.trace("Build StateMachine");
		StateMachine<TestState> stateMachine = builder.build();

		logger.trace("Verify transitions");
		assertEquals(expectedTransitions, stateMachine.getTransitions());
	}

	protected enum TestState {
		STATE1,
		STATE2,
		STATE3,
		STATE4
	}
}
