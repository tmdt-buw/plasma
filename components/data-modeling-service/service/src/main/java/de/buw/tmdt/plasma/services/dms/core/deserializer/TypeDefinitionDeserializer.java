package de.buw.tmdt.plasma.services.dms.core.deserializer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.Type;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.TypeDefinitionDTO;
import de.buw.tmdt.plasma.utilities.misc.ReflectionUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

import static de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.TypeDefinitionDTO.*;

@Service
public class TypeDefinitionDeserializer extends JsonDeserializer<TypeDefinitionDTO<?>> {

    private static final List<String> TYPE_DEFINITION_PROPERTIES = Arrays.asList(
            TYPE_PROPERTY,
            NAME_PROPERTY,
            MINIMUM_CARDINALITY_PROPERTY,
            MAXIMUM_CARDINALITY_PROPERTY,
            HIDDEN_PROPERTY,
            VALUE_PROPERTY
    );

    @Override
    @NotNull
    public TypeDefinitionDTO<?> deserialize(
            @NotNull JsonParser jsonParser,
            @NotNull DeserializationContext deserializationContext
    ) throws IOException, JsonProcessingException, JsonParseException, JsonMappingException {
        JsonNode rootNode = jsonParser.readValueAsTree();

        for (String key : TYPE_DEFINITION_PROPERTIES) {
            if (rootNode.get(key) == null) {
                throw new JsonParseException(jsonParser, "Missing property in " + TypeDefinitionDTO.class.getSimpleName() + ": `" + key + "`.");
            }
        }

        String typeProperty = rootNode.get(TYPE_PROPERTY).textValue();
        Type<?, ?> type = Type.valueOf(typeProperty);
        if (type == null) {
            throw new JsonParseException(jsonParser, "Invalid value for key `" + TYPE_PROPERTY + "`: " + typeProperty);
        }

        String name = rootNode.get(NAME_PROPERTY).textValue();
        String label = rootNode.get(LABEL_PROPERTY).textValue();
        String description = rootNode.get(DESCRIPTION_PROPERTY).textValue();
        int minCardinality = rootNode.get(MINIMUM_CARDINALITY_PROPERTY).asInt();
        int maxCardinality = rootNode.get(MAXIMUM_CARDINALITY_PROPERTY).asInt();
        boolean hidden = rootNode.get(HIDDEN_PROPERTY).asBoolean();

        JsonNode valueNode = rootNode.get(VALUE_PROPERTY);
        if (!valueNode.isArray()) {
            throw new JsonParseException(jsonParser, "Invalid value for key `" + VALUE_PROPERTY + "`: " + typeProperty);
        }

        final Serializable[] values;
        if (valueNode.size() == 0) {
            values = new Serializable[0];
        } else {
            TypeAggregator typeAggregator = new TypeAggregator();
            values = StreamSupport.stream(valueNode.spliterator(), false)
                    .map(node -> {
                        try {
                            return jsonParser.getCodec().treeToValue(node, type.getRawClass());
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }).peek(serializable -> typeAggregator.mostSpecializedCommonParentTypeFor(serializable.getClass()))
                    .toArray(size -> (Serializable[]) Array.newInstance(typeAggregator.getCurrentCommonType(), size));
        }

        return new TypeDefinitionDTO<>(typeProperty, name, label, description, minCardinality, maxCardinality, values, hidden);
    }

    @NotNull
    private static class TypeAggregator {
        private Class<? extends Serializable> currentClass = null;

        synchronized void mostSpecializedCommonParentTypeFor(@NotNull Class<? extends Serializable> otherClass) {
            if (this.currentClass == null) {
                this.currentClass = otherClass;
            }

            Deque<@NotNull Class<?>> currentClassHierarchy = ReflectionUtilities.getClassHierarchy(this.currentClass);
            Deque<@NotNull Class<?>> otherClassHierarchy = ReflectionUtilities.getClassHierarchy(otherClass);

            Class<?> commonAncestor = null;
            //Objects.equals instead of e1.equals(e2) since the compiler warns about a possible null value for e1
            while (!currentClassHierarchy.isEmpty() && Objects.equals(currentClassHierarchy.peek(), otherClassHierarchy.pop())) {
                commonAncestor = currentClassHierarchy.pop();
            }

            assert commonAncestor != null;

            this.currentClass = commonAncestor.asSubclass(Serializable.class);
        }

        @Nullable
        synchronized Class<? extends Serializable> getCurrentCommonType() {
            return currentClass;
        }
    }
}
