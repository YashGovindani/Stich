package com.stitch.pojo;

import javax.servlet.*;
import javax.servlet.http.*;

public class RequestScope
{
    private HttpServletRequest request;
    public RequestScope(HttpServletRequest request)
    {
        this.request = request;
    }
    public void setAttribute(String key, Object value)
    {
        this.request.setAttribute(key,value);
    }
    public Object getAttribute(String key)
    {
        return this.request.getAttribute(key);
    }
}