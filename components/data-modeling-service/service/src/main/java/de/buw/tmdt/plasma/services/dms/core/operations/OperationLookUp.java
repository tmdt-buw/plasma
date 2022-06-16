package de.buw.tmdt.plasma.services.dms.core.operations;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.SchemaNode;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class OperationLookUp {

	private final Map<String, Operation<?>> operationByNameLookup;

	public OperationLookUp() {
		this.operationByNameLookup = new HashMap<>();
	}

	@NotNull
	public Operation<?> getOperationForName(@NotNull String name) {
		Operation<?> result = operationByNameLookup.get(name);
		if (result == null) {
			throw new IllegalArgumentException("Unknown operation of name: " + name);
		}
		return result;
	}

	public HashMap<SchemaNode, Set<Operation.Handle>> getOperationHandles(@NotNull CombinedModel model) {
		HashMap<SchemaNode, Set<Operation.Handle>> result = new HashMap<>();

		for (Operation<?> operation : operationByNameLookup.values()) {
			for (Map.Entry<SchemaNode, Set<Operation.Handle>> nodeSetEntry : operation.generateHandlesForApplicableNodes(model).entrySet()) {
				Set<Operation.Handle> resultSet = result.computeIfAbsent(nodeSetEntry.getKey(), ignored -> new HashSet<>());
				resultSet.addAll(nodeSetEntry.getValue());
			}
		}
		return result;
	}

	@SuppressWarnings("ObjectEquality - equality is defined by name and thus insufficient")
	public void registerOperation(@NotNull Operation<?> operation) {
		String operationName = operation.getName();
		if (this.operationByNameLookup.containsKey(operationName) && this.operationByNameLookup.get(operationName) != operation) {
			throw new IllegalArgumentException("Operation of name " + operation.getName() + " was already registered.");
		}
		this.operationByNameLookup.put(operationName, operation);
	}
}
