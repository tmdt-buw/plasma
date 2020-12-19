package de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel;

import de.buw.tmdt.plasma.services.dms.core.model.Position;
import de.buw.tmdt.plasma.services.dms.core.model.Traversable;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SemanticModelTest {
	private SemanticModel testee;

	private Position position = new Position(0, 0);
	private List<EntityType> setUpETList;
	private List<Relation> setUpRList;

	private EntityConcept entityConcept;
	private RelationConcept relationConcept;

	private EntityType getEntityType() {
		return new EntityType(null, "", "", "", entityConcept, position);
	}

	private Relation getRelation(@NotNull EntityType from, @NotNull EntityType to) {
		return new Relation(null, from, to, relationConcept, "", position);
	}

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		entityConcept = new EntityConcept(null, "ec", "", "", position);
		relationConcept = new RelationConcept(null, "rc", "", "", Collections.emptySet(), position);
	}

	private void createTestee(
			List<EntityType> entityTypes,
			List<Relation> relations
	) {
		setUpETList = new ArrayList<>(entityTypes);
		setUpRList = new ArrayList<>(relations);
		testee = new SemanticModel(
				UUID.randomUUID().toString(),
				"",
				"",
				entityTypes,
				relations,
				position,
				1L
		);
	}

	/**
	 * Creates a pair of two mocks. The left element will return the right one on {@code copy()} and both elements will return the same randomly generated
	 * Identity on {@code getIdentity()}.
	 *
	 * @param type Class of which Mocks are wished to be created.
	 */
	private <T extends Traversable> Pair<T, T> createMockPair(Class<T> type) {
		T t1 = mock(type);
		T t2 = mock(type);
		Traversable.Identity random = Traversable.Identity.random();
		when(t1.copy(any())).thenReturn(t2);
		Mockito.<Traversable.Identity<?>>when(t1.getIdentity()).thenReturn(random);
		Mockito.<Traversable.Identity<?>>when(t2.getIdentity()).thenReturn(random);
		return new Pair<>(t1, t2);
	}

	@Test
	void copyEmptyIM() {
		//prep
		createTestee(Collections.emptyList(), Collections.emptyList());

		//preCondition

		//exec
		SemanticModel copy = testee.copy();

		//postCondition
		assertNotEquals(testee.getId(), copy.getId());
		assertEquals(testee.getIdentity(), copy.getIdentity());
		assertEquals(testee.getEntityTypes(), copy.getEntityTypes());
		assertEquals(testee.getRelations(), copy.getRelations());
	}

	@Test
	void copyIMWithECandR() {
		//prep
		Pair<EntityType, EntityType> etPair1 = this.createMockPair(EntityType.class);
		Pair<EntityType, EntityType> etPair2 = this.createMockPair(EntityType.class);
		Pair<Relation, Relation> rPair = this.createMockPair(Relation.class);

		createTestee(Arrays.asList(etPair1.getLeft(), etPair2.getLeft()), Collections.singletonList(rPair.getLeft()));

		//preCondition
		assertEquals(setUpRList.size(), testee.getRelations().size());
		assertEquals(setUpETList.size(), testee.getEntityTypes().size());

		//exec
		SemanticModel copy = testee.copy();
		verify(etPair1.getLeft(), times(1)).copy(any());
		verify(etPair2.getLeft(), times(1)).copy(any());
		verify(rPair.getLeft(), times(1)).copy(any());

		//postCondition
		assertEquals(setUpRList.size(), copy.getRelations().size());
		assertEquals(setUpETList.size(), copy.getEntityTypes().size());
		assertTrue(containsIdentity(copy.getEntityTypes(), etPair1.getRight().getIdentity()));
		assertTrue(containsIdentity(copy.getEntityTypes(), etPair2.getRight().getIdentity()));
		assertTrue(containsIdentity(copy.getRelations(), rPair.getRight().getIdentity()));
		assertEquals(testee.getIdentity(), copy.getIdentity());
		assertNull(copy.getId());
	}

	@Test
	void replaceET() {
		//prep
		EntityType entityType1 = getEntityType();
		EntityType entityType2 = getEntityType();
		createTestee(Collections.singletonList(entityType1), Collections.emptyList());

		//preCondition
		assertTrue(containsIdentity(testee.getEntityTypes(), entityType1.getIdentity()));

		//exec
		testee.replace(entityType1.getIdentity(), entityType2);

		//postCondition
		assertTrue(containsIdentity(testee.getEntityTypes(), entityType2.getIdentity()));
	}

	@Test
	void replaceETwithR() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			//prep
			EntityType entityType = getEntityType();
			Relation relation = getRelation(entityType, entityType);
			createTestee(Collections.singletonList(entityType), Collections.emptyList());

			//preCondition
			assertTrue(containsIdentity(testee.getEntityTypes(), entityType.getIdentity()));

			//exec
			testee.replace(entityType.getIdentity(), relation);
		});
	}

	@Test
	void replaceR() {
		//prep
		Relation relation1 = getRelation(getEntityType(), getEntityType());
		Relation relation2 = getRelation(getEntityType(), getEntityType());
		createTestee(Collections.emptyList(), Collections.singletonList(relation1));

		//preCondition
		assertEquals(setUpRList.size(), testee.getRelations().size());
		assertTrue(containsIdentity(testee.getRelations(), relation1.getIdentity()));

		//exec
		testee.replace(relation1.getIdentity(), relation2);

		//postCondition
		assertEquals(setUpRList.size(), testee.getRelations().size());
		assertTrue(containsIdentity(testee.getRelations(), relation2.getIdentity()));
	}

	@Test
	void replaceRWithECNotInIM() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			//prep
			EntityType entityType1 = getEntityType();
			EntityType entityType2 = getEntityType();
			EntityType entityType3 = getEntityType();
			Relation relation1 = getRelation(entityType1, entityType2);
			Relation relation2 = getRelation(entityType1, entityType3);
			createTestee(Arrays.asList(entityType1, entityType2), Collections.singletonList(relation1));

			//preCondition
			assertEquals(setUpRList.size(), testee.getRelations().size());
			assertEquals(setUpETList.size(), testee.getEntityTypes().size());

			//exec
			testee.replace(relation1.getIdentity(), relation2);
			testee.prePersist();
		});
	}

	@Test
	void removeUnboundET() {
		//prep
		EntityType entityType = getEntityType();
		createTestee(Collections.singletonList(entityType), Collections.emptyList());

		//preCondition
		assertEquals(setUpETList.size(), testee.getEntityTypes().size());
		assertTrue(containsIdentity(testee.getEntityTypes(), entityType.getIdentity()));

		//exec
		testee.remove(entityType.getIdentity(), new HashSet<>(), new ArrayDeque<>());

		//postCondition
		assertEquals(setUpETList.size() - 1, testee.getEntityTypes().size());
		assertFalse(containsIdentity(testee.getEntityTypes(), entityType.getIdentity()));
	}

	@Test
	void removeBoundET() {
		//prep
		EntityType entityType1 = getEntityType();
		EntityType entityType2 = getEntityType();
		Relation relation1 = getRelation(entityType1, entityType2);
		createTestee(Arrays.asList(entityType1, entityType2), Collections.singletonList(relation1));

		//preCondition
		assertEquals(setUpRList.size(), testee.getRelations().size());
		assertEquals(setUpETList.size(), testee.getEntityTypes().size());
		assertTrue(containsIdentity(testee.getEntityTypes(), entityType1.getIdentity()));

		//exec
		testee.remove(entityType1.getIdentity(), new HashSet<>(), new ArrayDeque<>());

		//postCondition
		assertEquals(setUpRList.size() - 1, testee.getRelations().size());
		assertEquals(setUpETList.size() - 1, testee.getEntityTypes().size());
		assertFalse(containsIdentity(testee.getEntityTypes(), entityType1.getIdentity()));
		assertFalse(containsIdentity(testee.getEntityTypes(), relation1.getIdentity()));
	}

	@Test
	void removeRelation() {
		//prep
		EntityType entityType1 = getEntityType();
		EntityType entityType2 = getEntityType();
		Relation relation1 = getRelation(entityType1, entityType2);
		createTestee(Arrays.asList(entityType1, entityType2), Collections.singletonList(relation1));

		//preCondition
		assertEquals(setUpRList.size(), testee.getRelations().size());
		assertTrue(containsIdentity(testee.getRelations(), relation1.getIdentity()));

		//exec
		testee.remove(relation1.getIdentity(), new HashSet<>(), new ArrayDeque<>());

		//postCondition
		assertEquals(setUpRList.size() - 1, testee.getRelations().size());
	}

	@Test
	void removeMultiStepRelation() {
		//prep
		EntityType entityType1 = getEntityType();
		EntityType entityType2 = getEntityType();
		EntityType entityType3 = getEntityType();
		Relation relation1 = getRelation(entityType1, entityType2);
		Relation relation2 = getRelation(entityType2, entityType3);
		createTestee(
				Arrays.asList(entityType1, entityType2, entityType3),
				Arrays.asList(relation1, relation2)
		);

		//preCondition
		assertEquals(setUpRList.size(), testee.getRelations().size());
		assertEquals(setUpETList.size(), testee.getEntityTypes().size());
		assertTrue(containsIdentity(testee.getRelations(), relation1.getIdentity()));

		//exec
		testee.remove(relation1.getIdentity(), new HashSet<>(), new ArrayDeque<>());

		//postCondition
		assertEquals(setUpRList.size() - 1, testee.getRelations().size());
		assertEquals(setUpETList.size(), testee.getEntityTypes().size());
	}

	@Test
	void removeEC() {
		//prep
		EntityType entityType1 = getEntityType();
		EntityType entityType2 = getEntityType();
		Relation relation = getRelation(entityType1, entityType2);
		createTestee(Arrays.asList(entityType1, entityType2), Collections.singletonList(relation));

		//preCondition
		assertEquals(setUpRList.size(), testee.getRelations().size());
		assertEquals(setUpETList.size(), testee.getEntityTypes().size());
		assertEquals(entityConcept, entityType1.getEntityConcept());

		//exec
		testee.remove(entityConcept.getIdentity(), new HashSet<>(), new ArrayDeque<>());

		//postCondition
		assertEquals(setUpRList.size() - 1, testee.getRelations().size());
		assertEquals(setUpETList.size() - 2, testee.getEntityTypes().size());
	}

	@Test
	void removeRC() {
		//prep
		Relation relation1 = getRelation(getEntityType(), getEntityType());
		Relation relation2 = getRelation(getEntityType(), getEntityType());
		createTestee(Collections.emptyList(), Arrays.asList(relation1, relation2));

		//preCondition
		assertEquals(setUpRList.size(), testee.getRelations().size());
		assertEquals(setUpETList.size(), testee.getEntityTypes().size());

		//exec
		testee.remove(relationConcept.getIdentity(), new HashSet<>(), new ArrayDeque<>());

		//postCondition
		assertEquals(setUpRList.size() - 2, testee.getRelations().size());
		assertEquals(setUpETList.size(), testee.getEntityTypes().size());
	}

	private boolean containsIdentity(List<? extends Traversable> list, Traversable.Identity identity) {
		return list.stream()
				.anyMatch(tmb -> identity.equals(tmb.getIdentity()));
	}
}