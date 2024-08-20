package org.albert.design_patterns.command.invoker;

public class FilePathHolder
{
    private String currentFilePath;

    public String getCurrentFilePath()
    {
        return currentFilePath;
    }

    public void setCurrentFilePath(String currentFilePath)
    {
        this.currentFilePath = currentFilePath;
    }
}
