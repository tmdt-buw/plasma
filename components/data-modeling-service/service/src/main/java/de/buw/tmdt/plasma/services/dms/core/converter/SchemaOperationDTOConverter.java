package de.buw.tmdt.plasma.services.dms.core.converter;

import de.buw.tmdt.plasma.services.dms.core.operations.Operation;
import de.buw.tmdt.plasma.services.dms.core.operations.OperationLookUp;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.ParameterDefinition;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.SyntacticOperationDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.Type;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.TypeDefinitionDTO;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serializable;

@Component
public class SchemaOperationDTOConverter {

	private static final Logger logger = LoggerFactory.getLogger(SchemaOperationDTOConverter.class);
	private final OperationLookUp operationLookUp;

	@Autowired
	public SchemaOperationDTOConverter(@NotNull OperationLookUp operationLookUp) {
		this.operationLookUp = operationLookUp;
	}

	@NotNull
	public Operation.Handle fromDTO(@NotNull SyntacticOperationDTO syntacticOperationDTO) {
		Operation<?> operation;
		try {
			operation = operationLookUp.getOperationForName(syntacticOperationDTO.getName());
		} catch (IllegalArgumentException e) {
			logger.warn("Failed to find syntactic operation.", e);
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Failed to find syntactic operation."
			);
		}
		try {
			ParameterDefinition<?, ?> parameterDefinition = fromDTO(syntacticOperationDTO.getParameter());
			return new Operation.Handle(operation, parameterDefinition);
		} catch (IllegalArgumentException e) {
			logger.warn("Failed to parse parameters.", e);
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Failed to parse parameters."
			);
		}
	}

	@NotNull
	private <R extends Serializable, P extends Serializable> ParameterDefinition<R, P> fromDTO(@NotNull TypeDefinitionDTO<R> typeDefinitionDTO) {
		@SuppressWarnings("unchecked")
		ParameterDefinition<R, P> parameterDefinition = Type.COMPLEX.parse(typeDefinitionDTO);
		return parameterDefinition;
	}

	@NotNull
	public <R extends Serializable> TypeDefinitionDTO<R> toDTO(@NotNull ParameterDefinition<R, ?> parameterDefinition) {
		@SuppressWarnings("unchecked")
		TypeDefinitionDTO<R> typeDefinitionDTO = Type.COMPLEX.serialize(parameterDefinition);
		return typeDefinitionDTO;
	}
}
