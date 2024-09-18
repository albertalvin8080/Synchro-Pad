package org.albert.design_patterns.observer;

public interface DataSharer extends StateChangeObserver
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

    void destroy();
}
