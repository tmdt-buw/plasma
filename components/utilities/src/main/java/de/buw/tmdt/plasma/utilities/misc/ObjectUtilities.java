package de.buw.tmdt.plasma.utilities.misc;

import org.jetbrains.annotations.Contract;

import java.io.*;

public final class ObjectUtilities {
    private ObjectUtilities() {
    }

    /**
     * Creates a deep copy of an object by serializing and deserializing it.
     *
     * @param object object to copy
     * @param <T>    type of object to copy
     *
     * @return a copy of the passed object
     *
     * @throws IOException            if the serialization or deserialization fails due to it's underlying stream
     * @throws ClassNotFoundException if the class is not present for deserialization
     */
    @SuppressWarnings("unchecked") //getClass on an object of Type T is ensured to return a class object of Type Class<T>
    public static <T> T deepCopy(T object) throws IOException, ClassNotFoundException {
        return ObjectUtilities.fromBytes(ObjectUtilities.toBytes(object), (Class<T>) object.getClass());
    }

    /**
     * Convenience method to use the jre default serialization.
     *
     * @param object object to serialize
     *
     * @return the byte[] representation of the passed object
     *
     * @throws IOException if the serialization fails due to it's underlying stream
     */
    public static byte[] toBytes(Object object) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(buffer);
        oos.writeObject(object);
        oos.close();

        return buffer.toByteArray();
    }

    /**
     * Convenience mehtod to use the jre default deserialization.
     *
     * @param data  serialized object
     * @param clazz expected type of serialized object
     * @param <T>   type of serialized object
     *
     * @return deserialized object
     *
     * @throws IOException            if the deserialization fails due to it's underlying stream
     * @throws ClassNotFoundException if the class is not present for deserialization
     * @throws ClassCastException     if the deserialized object can't be cast to the expected type {@code clazz}
     */
    public static <T> T fromBytes(byte[] data, Class<T> clazz) throws IOException, ClassNotFoundException, ClassCastException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInput in = new ObjectInputStream(bis);
        Object o = in.readObject();
        if (clazz.isInstance(o)) {
            return clazz.cast(o);
        }
        throw new ClassCastException(
                "Class of deserialized byte[] was not of the expected type." +
                "\nExpected: " + clazz.getCanonicalName() +
                "\nFound: " + o.getClass().getCanonicalName()
        );
    }

    /**
     * Tries to cast the object to type T and throws exceptions if either {@code type} is null or {@code object} does not inherit from {@code T}.
     *
     * @param object the object which is cast
     * @param type   the class reference of the type to cast to
     * @param <T>    the type to cast to
     *
     * @return the {@code object} cast to type {@code T}
     */
    @Contract(pure = true, value = "null,_ -> null ; !null,null -> fail ; !null,!null -> !null")
    public static <T> T checkedReturn(Object object, Class<T> type) {
        if (object == null) {
            return null;
        }
        if (type == null) {
            throw new IllegalArgumentException("Type must no be null.");
        }
        if (type.isInstance(object)) {
            return type.cast(object);
        }
        throw new IllegalArgumentException(String.format(
                "Can not replace %s by an object of type %s",
                type.getSimpleName(),
                object.getClass().getSimpleName()
        ));
    }
}