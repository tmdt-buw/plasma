package de.buw.tmdt.plasma.services.dms.shared.api;

import java.time.ZonedDateTime;

public class ModelingInfo {

    private final String id;

    private final String name;

    private final String description;

    private final ZonedDateTime created;

    private final String dataId;

    public ModelingInfo(String id, String name, String description, ZonedDateTime created, String dataId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.created = created;
        this.dataId = dataId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDataId() {
        return dataId;
    }

    public ZonedDateTime getCreated() {
        return created;
    }
}
