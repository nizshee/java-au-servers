package com.github.nizshee.shared;


import com.github.nizshee.Message;

import java.io.*;
import java.util.Arrays;

public class Util {

    public static byte[] read(InputStream input) throws IOException {
        int size = new DataInputStream(input).readInt();
        byte[] buffer = new byte[size];
        int read = 0;
        while (read < size) {
            read += input.read(buffer, read, size - read);
        }
        return buffer;
    }

    public static void write(OutputStream output, byte[] bytes) throws IOException {
        DataOutputStream dos = new DataOutputStream(output);
        dos.writeInt(bytes.length);
        dos.write(bytes);
    }

    public static void readOk(InputStream input) throws IOException {
        new DataInputStream(input).readByte();
    }

    public static void writeOk(OutputStream output) throws IOException {
        new DataOutputStream(output).writeByte(1);
    }

    public static Integer[] readArray(InputStream input) throws IOException {
        byte[] buffer = Util.read(input);
        Message.Arr arr = Message.Arr.parseFrom(buffer);
        return arr.getValuesList().toArray(new Integer[arr.getValuesCount()]);
    }

    public static void writeArray(OutputStream output, Integer[] array) throws IOException {
        Message.Arr arr = Message.Arr.newBuilder()
                .addAllValues(Arrays.asList(array))
                .build();
        Util.write(output, arr.toByteArray());
    }

    public static Integer[] readArray(byte[] bytes) throws IOException { // TODO bis
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        int size = new DataInputStream(b).readInt();
        byte[] data = new byte[size];
        System.arraycopy(bytes, 4, data, 0, size);
        Message.Arr arr = Message.Arr.parseFrom(data);
        return arr.getValuesList().toArray(new Integer[arr.getValuesCount()]);
    }

    public static byte[] writeArray(Integer[] array) throws IOException {
        Message.Arr arr = Message.Arr.newBuilder().addAllValues(Arrays.asList(array)).build();
        int size = arr.getSerializedSize();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(4 + size);
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(size);
        dos.write(arr.toByteArray());
        return bos.toByteArray();
    }
}
