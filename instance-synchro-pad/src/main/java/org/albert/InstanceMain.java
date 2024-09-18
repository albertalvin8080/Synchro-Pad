package org.albert;

import org.albert.component.SynchroPad;

import javax.swing.*;

public class InstanceMain
{
    public static void main( String[] args )
    {
        SwingUtilities.invokeLater(SynchroPad::new);
    }
}

