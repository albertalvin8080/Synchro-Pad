package org.albert;

import org.albert.component.SynchroPad;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class InstanceMain
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(SynchroPad::new);
    }
}

