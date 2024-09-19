package org.albert.server;

public interface DataSharer
{
    public static final short OP_BREAK = -1;
    public static final short OP_INSERT = 1;
    public static final short OP_DELETE = 2;

    // Multicast only
    public static final short OP_NEW_REQUEST = 10;
    public static final short OP_NEW_RESPONSE = 11;
    public static final short OP_NEW_RESPONSE_END = 12;
    public static final short OP_NEW_REQUEST_INIT = 13;
    public static final short OP_NEW_REQUEST_RECEIVED = 14;

    // TCP only
    public static final short OP_INIT_GLOBAL = 15;
    public static final short OP_REQUEST_GLOBAL_WRITE = 16;
    public static final short OP_DENIED_GLOBAL_WRITE = 17;
    public static final short OP_ACCEPTED_GLOBAL_WRITE = 18;
    public static final short OP_CLIENT_CONFIRMATION_GLOBAL_WRITE = 19;
    public static final short OP_DISCONNECT_GLOBAL = 20;

    void destroy();
}
