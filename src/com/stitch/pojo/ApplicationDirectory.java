package com.stitch.pojo;

import java.io.*;

public class ApplicationDirectory
{
    private File directory;
    public ApplicationDirectory(File directory)
    {
        this.directory = directory;
    }
    public File getDirectory()
    {
        return this.directory;
    }
}