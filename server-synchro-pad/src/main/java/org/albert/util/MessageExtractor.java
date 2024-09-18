package org.albert.util;

public class MessageExtractor
{
    private final String uuid;
    private final short operationType;
    private final int offset;
    private final int length;
    private final String text;

    /*
     * [0] -> UUID (string)
     * [1] -> Operation Type
     * [2] -> offset
     * [3] -> length
     * [4] -> text from JTextArea
     * */
    public MessageExtractor(String msg)
    {
        String[] parts = msg.split(":", 5);
        uuid = parts[0];
        operationType = Short.parseShort(parts[1]);
        offset = Integer.parseInt(parts[2]);
        length = Integer.parseInt(parts[3]);
        text = parts[4];
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
}
