package de.buw.tmdt.plasma.services.srs.model.datasource;

import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("DefaultAnnotationParam - explicit > implicit")
public class DataSourceModel {

	private UUID uuid;

	//The value is only set after the schema analysis
	private List<DataSourceSchema> dataSourceSchemaStack = new ArrayList<>();

	private int stackIndexPointer;

	private DataSourceSchema currentSchema;

	public DataSourceModel() {
		this.stackIndexPointer = -1;
	}

	public DataSourceModel(UUID uuid) {
		this();
		this.uuid = uuid;
	}

	@NotNull
	public List<DataSourceSchema> getDataSourceSchemaStack() {
		return new ArrayList<>(this.dataSourceSchemaStack.subList(0, stackIndexPointer + 1));
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - hibernate uses proxies which must not be rewrapped")
	public void setDataSourceSchemaStack(@NotNull List<DataSourceSchema> schemaDefinitionStack) {
		this.dataSourceSchemaStack.clear();
		this.dataSourceSchemaStack.addAll(schemaDefinitionStack);
		final int lastStackIndex = this.dataSourceSchemaStack.size() - 1;
		this.currentSchema = lastStackIndex >= 0 ? this.dataSourceSchemaStack.get(lastStackIndex) : null;
		this.stackIndexPointer = lastStackIndex;
	}

	public void pushDataSourceSchema(@NotNull DataSourceSchema schema) {
		if (stackIndexPointer >= 0 && stackIndexPointer != dataSourceSchemaStack.size() - 1) {
			if (dataSourceSchemaStack.size() > stackIndexPointer + 1) {
				dataSourceSchemaStack.subList(stackIndexPointer + 1, dataSourceSchemaStack.size()).clear();
			}
		}
		dataSourceSchemaStack.add(schema);
		stackIndexPointer = dataSourceSchemaStack.size() - 1;
		this.currentSchema = schema;
	}

	@NotNull
	public UUID getUuid() {
		return uuid;
	}

	@NotNull
	public DataSourceSchema peekDataSourceSchema() {
		return this.currentSchema;
	}

	public void popDataSourceSchema() {
		if (stackIndexPointer == -1 || stackIndexPointer == 0) {
			return;
		}
		this.currentSchema = this.dataSourceSchemaStack.get(--this.stackIndexPointer);
	}

	@NotNull
	public DataSourceSchema restoreDataSourceSchema() {
		if (stackIndexPointer >= this.dataSourceSchemaStack.size() - 1) {
			throw new RuntimeException("Schema cannot be null");
		}
		this.currentSchema = this.dataSourceSchemaStack.get(++this.stackIndexPointer);
		return this.currentSchema;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"DataSourceModel\""
		       + ", \"@super\":" + super.toString()
		       + ", \"dataSourceSchemaStack\":" + StringUtilities.listToJson(dataSourceSchemaStack)
		       + '}';
	}
}