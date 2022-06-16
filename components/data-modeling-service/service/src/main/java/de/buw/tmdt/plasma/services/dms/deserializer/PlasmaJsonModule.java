package de.buw.tmdt.plasma.services.dms.deserializer;

import com.fasterxml.jackson.databind.module.SimpleModule;
import de.buw.tmdt.plasma.datamodel.modification.operation.TypeDefinitionDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class PlasmaJsonModule extends SimpleModule {
	private static final long serialVersionUID = -6187683743188640007L;

	@SuppressWarnings("HardcodedFileSeparator")
	@Autowired
	public PlasmaJsonModule(@NotNull TypeDefinitionDeserializer typeDefinitionDeserializer) {
		super("PLASMA DTO De-/Serialization Module");
		this.addDeserializer(TypeDefinitionDTO.class, typeDefinitionDeserializer);
	}
}
