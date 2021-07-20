package com.stitch.pojo;

import java.lang.reflect.*;

public class Service
{
    public enum RequestMethod{ GET, POST};
    public static final RequestMethod GET = RequestMethod.GET;
    public static final RequestMethod POST = RequestMethod.POST;
    private Class serviceClass;
    private String path;
    private Method service;
    private RequestMethod requestMethod;
    private String forwardTo;
    public Service()
    {
        this.serviceClass = null;
        this.path = null;
        this.service = null;
        this.requestMethod = GET;
        this.forwardTo = "";
    }
    public Service(Class serviceClass, String path, Method service, RequestMethod requestMethod, String forwardTo)
    {
        this.serviceClass = serviceClass;
        this.path = path;
        this.service = service;
        this.requestMethod = requestMethod;
        this.forwardTo = forwardTo;
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
    public void setRequestMethod(RequestMethod requestMethod)
    {
        this.requestMethod = requestMethod;
    }
    public RequestMethod getRequestMethod()
    {
        return this.requestMethod;
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