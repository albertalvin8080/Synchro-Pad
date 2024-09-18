package org.albert;

import org.albert.server.tcp.SynchroPadServerTcp;

public class ServerMain
{
    public static void main( String[] args )
    {
//        new TextEditorServerMulticast().init();
        new SynchroPadServerTcp().init();
    }
}
