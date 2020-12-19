package de.buw.tmdt.plasma.services.dms.core.operations;

import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.Node;
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

	public HashMap<Node, Set<Operation.Handle>> getOperationHandles(@NotNull Node root) {
		HashMap<Node, Set<Operation.Handle>> result = new HashMap<>();

		for (Operation<?> operation : operationByNameLookup.values()) {
			for (Map.Entry<Node, Set<Operation.Handle>> nodeSetEntry : operation.generateParameterDefinitionsOnGraph(root).entrySet()) {
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
