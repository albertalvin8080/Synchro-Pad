package org.albert;

import org.albert.server.tcp.TextEditorServerTcp;

public class App
{
    public static void main( String[] args )
    {
//        new TextEditorServerMulticast().init();
        new TextEditorServerTcp().init();
    }
}
