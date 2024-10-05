package org.albert.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CompilerProperties
{
    private static final Logger logger = LoggerFactory.getLogger(CompilerProperties.class);

    public static final boolean DEBUG;

    static
    {
        Properties properties = new Properties();
        try (InputStream input = CompilerProperties.class.getClassLoader().getResourceAsStream("config.properties"))
        {
            if (input == null)
            {
                logger.info("Unable to find config.properties");
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
