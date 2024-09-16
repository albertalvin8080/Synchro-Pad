package org.albert.util;

import java.io.*;
import java.nio.file.Files;

public class SharedFileUtils
{
    public static final String SHARED_FILE = "shared_file.txt";

    public static StringBuilder readFromSharedFile()
    {
        File file = new File(SHARED_FILE);
        if (!file.exists())
        {
            try
            {
                Files.createFile(file.toPath());
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr))
        {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
            {
                content.append(line).append("\n"); // Avoiding Windows's "\r\n"
            }
            return content;
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public static synchronized void writeToSharedFile(StringBuilder localText)
    {
        File file = new File(SHARED_FILE);
        try (FileWriter fr = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fr))
        {
            String content = localText.toString();
            // This converts '\n' to the platform's line separator
            content = content.replaceAll("\n", System.lineSeparator());
            bw.write(content);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
