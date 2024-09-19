package org.albert.util;

import java.io.*;

public class MessageHolder implements Serializable
{
    private final String uuid;
    private final short operationType;
    private final int offset;
    private final int length;
    private final String text;

    public MessageHolder(String uuid, short operationType, int offset, int length, String text)
    {
        this.uuid = uuid;
        this.operationType = operationType;
        this.offset = offset;
        this.length = length;
        this.text = text;
    }

    public String getUuid()
    {
        return uuid;
    }

    public short getOperationType()
    {
        return operationType;
    }

    public int getOffset()
    {
        return offset;
    }

    public int getLength()
    {
        return length;
    }

    public String getText()
    {
        return text;
    }

    public byte[] serialize() throws IOException
    {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream))
        {
            objectOutputStream.writeObject(this);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        }
    }

    public static MessageHolder deserialize(byte[] data) throws IOException, ClassNotFoundException
    {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data); ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream))
        {
            return (MessageHolder) objectInputStream.readObject();
        }
    }
}
