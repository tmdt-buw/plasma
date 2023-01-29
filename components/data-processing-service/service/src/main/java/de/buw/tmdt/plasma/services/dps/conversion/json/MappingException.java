package de.buw.tmdt.plasma.services.dps.conversion.json;

public class MappingException extends Exception {
    public MappingException(String s) {
        super(s);
    }

    public MappingException(String s, Exception e) {
        super(s, e);
    }
}
