package de.buw.tmdt.plasma.services.sas.core.converter;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.modification.operation.DataType;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.Edge;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.SchemaNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.SyntaxModel;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.Node;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.ObjectNode;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.PrimitiveNode;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.SetNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static de.buw.tmdt.plasma.datamodel.syntaxmodel.SchemaNode.*;

/**
 * Converts the local {@link de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.Node} tree to
 * the shared {@link CombinedModel}, or {@link SyntaxModel} respectively.
 */
@Service
public class CombinedModelConverter {

	private static final Logger logger = LoggerFactory.getLogger(CombinedModelConverter.class);

	@Autowired
	public CombinedModelConverter() {

	}

	/**
	 * Conversion methods internal model to {@link CombinedModel}.
	 *
	 * @param root The root element of the internal tree
	 * @return The resulting {@link SyntaxModel}
	 */
	@NotNull
	public SyntaxModel toCombinedModel(@NotNull Node root) {
		@NotNull SerializationContext serializationContext = new SerializationContext();
		SchemaNode syntaxRoot;
		List<String> path = new ArrayList<>();
		if (root instanceof SetNode) {
			syntaxRoot = toCombinedModel((SetNode) root, "ROOT", ROOT_PATH_TOKEN, path, serializationContext);
		} else if (root instanceof ObjectNode) {
			syntaxRoot = toCombinedModel((ObjectNode) root, "ROOT", ROOT_PATH_TOKEN, path, serializationContext);
		} else {
			throw new IllegalArgumentException(root.toString());
		}
		syntaxRoot.setLabel("ROOT");
		SyntaxModel syntaxModel = new SyntaxModel(
				syntaxRoot.getUuid(),
				new ArrayList<>(serializationContext.nodeLookUp.values()),
				serializationContext.edges
		);
		syntaxModel.validate();
		return syntaxModel;
	}

	@NotNull
	@SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Explicit check that value is not null")
	private de.buw.tmdt.plasma.datamodel.syntaxmodel.SetNode toCombinedModel(@NotNull SetNode setNode, @NotNull String label, String pathToken, List<String> parentPath, @NotNull SerializationContext serializationContext) {

		Double xCoordinate = null;
		Double yCoordinate = null;
		if (setNode.getPosition() != null) {
			xCoordinate = setNode.getPosition().getXCoordinate();
			yCoordinate = setNode.getPosition().getYCoordinate();
		}
		List<String> nodePath = new ArrayList<>(parentPath);
		nodePath.add(pathToken);

		de.buw.tmdt.plasma.datamodel.syntaxmodel.SetNode root =
				new de.buw.tmdt.plasma.datamodel.syntaxmodel.SetNode(
						UUID.randomUUID().toString(),
						label,
						nodePath,
						xCoordinate,
						yCoordinate,
						setNode.isValid(), true, false);

		nodePath = new ArrayList<>(nodePath); // copy list again to ensure child does get a new instance

		for (SetNode.Child child : setNode.getChildren()) {
			createArrayNodeChildren(root, child, nodePath, serializationContext);
		}
		return root;
	}

	private void createArrayNodeChildren(@NotNull de.buw.tmdt.plasma.datamodel.syntaxmodel.SetNode parentSetNode, @NotNull SetNode.Child child, List<String> nodePath, @NotNull SerializationContext serializationContext) {
		SchemaNode schemaNode;
		Node rawChildNode = child.getNode();
		if (rawChildNode instanceof SetNode) {
			SetNode childNode = (SetNode) rawChildNode;
			nodePath = new ArrayList<>(nodePath); // copy list again to ensure child does get a new instance
			de.buw.tmdt.plasma.datamodel.syntaxmodel.SetNode childSetNode = toCombinedModel(childNode,ARRAY_LABEL, ARRAY_PATH_TOKEN,nodePath,serializationContext );
			schemaNode = childSetNode;
		} else if (rawChildNode instanceof ObjectNode) {
			ObjectNode childNode = (ObjectNode) rawChildNode;
			schemaNode = toCombinedModel(childNode, OBJECT_LABEL,ARRAY_PATH_TOKEN , nodePath, serializationContext);
		}   else if (rawChildNode instanceof PrimitiveNode) {
			PrimitiveNode childNode = (PrimitiveNode) rawChildNode;
			schemaNode = toCombinedModel(childNode, VALUE_LABEL, ARRAY_PATH_TOKEN, nodePath);
		} else {
			throw new IllegalArgumentException("Unsupported sub-type of " + Node.class + " found: " + child.getClass());
		}
		serializationContext.nodeLookUp.put(schemaNode.getUuid(), schemaNode);
		serializationContext.edges.add(new Edge(parentSetNode.getUuid(), schemaNode.getUuid()));
	}

	@NotNull
	@SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Explicit check that value is not null")
	private de.buw.tmdt.plasma.datamodel.syntaxmodel.ObjectNode toCombinedModel(@NotNull ObjectNode objectNode,  @NotNull String label, @NotNull String pathToken,List<String> parentPath, @NotNull SerializationContext serializationContext) {
		Double xCoordinate = null;
		Double yCoordinate = null;
		if (objectNode.getPosition() != null) {
			xCoordinate = objectNode.getPosition().getXCoordinate();
			yCoordinate = objectNode.getPosition().getYCoordinate();
		}

		List<String> nodePath = new ArrayList<>(parentPath);
		nodePath.add(pathToken);

		de.buw.tmdt.plasma.datamodel.syntaxmodel.ObjectNode object =
				new de.buw.tmdt.plasma.datamodel.syntaxmodel.ObjectNode(
						label, nodePath, xCoordinate, yCoordinate, objectNode.isValid());

		serializationContext.nodeLookUp.put(object.getUuid(), object);
		nodePath = new ArrayList<>(nodePath); // copy list again to ensure child does gut a new instance

		for (Map.Entry<String, Node> entry : objectNode.getChildren().entrySet()) {
			createObjectChild(object, entry.getValue(), entry.getKey(), entry.getKey(), nodePath, serializationContext);
		}

		return object;
	}

	private void createObjectChild(@NotNull de.buw.tmdt.plasma.datamodel.syntaxmodel.ObjectNode parentObjectNode,
								   @NotNull Node child,
								   String label,
								   String pathToken,
								   List<String> nodePath,
								   @NotNull SerializationContext serializationContext) {
		SchemaNode schemaNode;
		if (child instanceof SetNode) {
			SetNode childNode = (SetNode) child;
			nodePath = new ArrayList<>(nodePath); // copy list again to ensure child does get a new instance
			de.buw.tmdt.plasma.datamodel.syntaxmodel.SetNode childSetNode = toCombinedModel(childNode, label,pathToken,nodePath,serializationContext );
			schemaNode = childSetNode;
		} else if (child instanceof ObjectNode) {
			ObjectNode childNode = (ObjectNode) child;
			schemaNode = toCombinedModel(childNode,label ,pathToken, nodePath, serializationContext);
		}   else if (child instanceof PrimitiveNode) {
			PrimitiveNode childNode = (PrimitiveNode) child;
			schemaNode = toCombinedModel(childNode, label, pathToken, nodePath);
		} else {
			throw new IllegalArgumentException("Unsupported sub-type of " + Node.class + " found: " + child.getClass());
		}
		serializationContext.nodeLookUp.put(schemaNode.getUuid(), schemaNode);
		serializationContext.edges.add(new Edge(parentObjectNode.getUuid(), schemaNode.getUuid()));
	}

	@NotNull
	@SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Explicit check that value is not null")
	private de.buw.tmdt.plasma.datamodel.syntaxmodel.PrimitiveNode toCombinedModel(
			@NotNull PrimitiveNode primitiveNode,
			@NotNull String label,
			String pathToken,
			List<String> parentPath
	) {

		//transform position to coordinates
		Double xCoordinate = null;
		Double yCoordinate = null;
		if (primitiveNode.getPosition() != null) {
			xCoordinate = primitiveNode.getPosition().getXCoordinate();
			yCoordinate = primitiveNode.getPosition().getYCoordinate();
		}

		List<String> nodePath = new ArrayList<>(parentPath);
		nodePath.add(pathToken);

		return new de.buw.tmdt.plasma.datamodel.syntaxmodel.PrimitiveNode(
			   UUID.randomUUID().toString(),
			   label,
			   nodePath,
			   xCoordinate,
			   yCoordinate,
			   primitiveNode.isValid(),
			   DataType.valueOf(primitiveNode.getDataType().identifier),
			   primitiveNode.getExamples(),
			   primitiveNode.getCleansingPattern(),true, false
	   );
	}

	/**
	 * Contains the current state of converted elements.
	 */
	private static class SerializationContext {
		private final HashMap<@NotNull String, @NotNull SchemaNode> nodeLookUp;
		private final ArrayList<@NotNull Edge> edges;

		public SerializationContext(
		) {
			this.nodeLookUp = new HashMap<>();
			this.edges = new ArrayList<>();
		}
	}
}
