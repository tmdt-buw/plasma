package de.buw.tmdt.plasma.utilities.buffer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;

@Deprecated
@SuppressFBWarnings(value = "SE_NO_SUITABLE_CONSTRUCTOR", justification = "not intended to be used, thus Deprecated")
public class SerializableListSlidingWindowBuffer<T extends Serializable> extends ListSlidingWindowBuffer<T> implements Serializable {

    private static final long serialVersionUID = 408767204625114841L;

    public SerializableListSlidingWindowBuffer(int capacity) {
        super(capacity);
    }
}
