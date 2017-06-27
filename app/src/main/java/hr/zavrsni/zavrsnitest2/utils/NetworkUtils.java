package hr.zavrsni.zavrsnitest2.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class NetworkUtils {

    public static byte[] serialize(Object object) {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
             ObjectOutputStream output = new ObjectOutputStream(byteStream)) {

            output.writeObject(object);
            byte[] buffer = byteStream.toByteArray();

            return buffer;

        } catch (EOFException e) {
            // skip if thrown
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] serializeMultiple(List<Object> objects) {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
             ObjectOutputStream output = new ObjectOutputStream(byteStream)) {

            for (Object object : objects)
                output.writeObject(object);

            byte[] buffer = byteStream.toByteArray();

            return buffer;

        } catch (EOFException e) {
            // skip if thrown
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<Object> deserializeMultiple(byte[] buffer) {
        List<Object> lo = new ArrayList<>();

        try (ObjectInputStream input = new ObjectInputStream(
                new ByteArrayInputStream(buffer))) {

            while (true) {
                lo.add(input.readObject());
            }

        } catch (EOFException e) {
            // skip if thrown
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lo;
    }

    public static Object deserialize(byte[] buffer) {
        try (ObjectInputStream input = new ObjectInputStream(
                new ByteArrayInputStream(buffer))) {

            return input.readObject();

        } catch (EOFException e) {
            // skip if thrown
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
