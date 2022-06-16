package de.buw.tmdt.plasma.utilities.sequence;

public class NaturalNumbers extends Sequence<Integer> {

    public NaturalNumbers() {
        super(0, i -> Math.addExact(i, 1));
    }
}
