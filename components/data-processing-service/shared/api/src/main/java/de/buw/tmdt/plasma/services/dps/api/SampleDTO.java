package de.buw.tmdt.plasma.services.dps.api;

import java.util.List;

public class SampleDTO {

    private final String dataId;

    private final List<String> samples;

    private final String filename;

    public SampleDTO(String dataId, String filename, List<String> samples) {
        this.dataId = dataId;
        this.filename = filename;
        this.samples = samples;
    }

    public String getDataId() {
        return dataId;
    }

    public List<String> getSamples() {
        return samples;
    }

    public String getFilename() {
        return filename;
    }
}
