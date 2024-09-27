package org.albert.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CompilerProperties
{
    public static final boolean DEBUG;

    static
    {
        Properties properties = new Properties();
        try (InputStream input = CompilerProperties.class.getClassLoader().getResourceAsStream("config.properties"))
        {
            if (input == null)
            {
                System.out.println("Unable to find config.properties");
                DEBUG = false; // default to false
            }
            else
            {
                properties.load(input);
                DEBUG = Boolean.parseBoolean(properties.getProperty("debug"));
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            throw new ExceptionInInitializerError("Failed to load properties");
        }
    }
}
