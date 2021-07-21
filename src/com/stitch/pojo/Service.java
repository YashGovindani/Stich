package com.stitch.pojo;

import java.lang.reflect.*;

public class Service
{
    private Class serviceClass;
    private String path;
    private Method service;
    private Boolean isGetAllowed;
    private Boolean isPostAllowed;
    private String forwardTo;
    public Service()
    {
        this.serviceClass = null;
        this.path = null;
        this.service = null;
        this.isGetAllowed = false;
        this.isPostAllowed = false;
        this.forwardTo = "";
    }
    public void setServiceClass(Class serviceClass)
    {
        this.serviceClass = serviceClass;
    }
    public Class getServiceClass()
    {
        return this.serviceClass;
    }
    public void setPath(String path)
    {
        this.path = path;
    }
    public String getPath()
    {
        return this.path;
    }
    public void setService(Method service)
    {
        this.service = service;
    }
    public Method getService()
    {
        return this.service;
    }
    public void isGetAllowed(Boolean isGetAllowed)
    {
        this.isGetAllowed = isGetAllowed;
    }
    public Boolean isGetAllowed()
    {
        return this.isGetAllowed;
    }
    public void isPostAllowed(Boolean isPostAllowed)
    {
        this.isPostAllowed = isPostAllowed;
    }
    public Boolean isPostAllowed()
    {
        return this.isPostAllowed;
    }
    public void setForwardTo(String forwardTo)
    {
        this.forwardTo = forwardTo;
    }
    public String getForwardTo()
    {
        return this.forwardTo;
    }
}