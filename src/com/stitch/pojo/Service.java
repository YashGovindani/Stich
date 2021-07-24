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
    private Boolean runOnStartup;
    private int priority;
    private Boolean injectApplicationDirectory;
    private Boolean injectSessionScope;
    private Boolean injectApplicationScope;
    private Boolean injectRequestScope;
    private Boolean isSecure;
    private String checkPost;
    private String guard;
    public Service()
    {
        this.serviceClass = null;
        this.path = null;
        this.service = null;
        this.isGetAllowed = false;
        this.isPostAllowed = false;
        this.forwardTo = "";
        this.runOnStartup = false;
        this.priority = 0;
        this.injectApplicationDirectory = false;
        this.injectApplicationScope = false;
        this.injectRequestScope = false;
        this.injectSessionScope = false;
        this.isSecure = false;
        this.checkPost = "";
        this.guard = "";
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
    public void injectApplicationDirectory(Boolean injectApplicationDirectory)
    {
        this.injectApplicationDirectory = injectApplicationDirectory;
    }
    public Boolean injectApplicationDirectory()
    {
        return this.injectApplicationDirectory;
    }
    public void injectSessionScope(Boolean injectSessionScope)
    {
        this.injectSessionScope = injectSessionScope;
    }
    public Boolean injectSessionScope()
    {
        return this.injectSessionScope;
    }
    public void injectApplicationScope(Boolean injectApplicationScope)
    {
        this.injectApplicationScope = injectApplicationScope;
    }
    public Boolean injectApplicationScope()
    {
        return this.injectApplicationScope;
    }
    public void injectRequestScope(Boolean injectRequestScope)
    {
        this.injectRequestScope = injectRequestScope;
    }
    public Boolean injectRequestScope()
    {
        return this.injectRequestScope;
    }
    public void setRunOnStartup(Boolean runOnStartup)
    {
        this.runOnStartup = runOnStartup;
    }
    public Boolean getRunOnStartup()
    {
        return this.runOnStartup;
    }
    public void setPriority(int priority)
    {
        this.priority = priority;
    }
    public int getPriority()
    {
        return this.priority;
    }
    public void isSecure(Boolean isSecure)
    {
        this.isSecure = isSecure;
    }
    public Boolean isSecure()
    {
        return this.isSecure;
    }
    public void setCheckPost(String checkPost)
    {
        this.checkPost = checkPost;
    }
    public String getCheckPost()
    {
        return this.checkPost;
    }
    public void setGuard(String guard)
    {
        this.guard = guard;
    }
    public String getGuard()
    {
        return this.guard;
    }
}