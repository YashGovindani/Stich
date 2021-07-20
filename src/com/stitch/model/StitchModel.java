package com.stitch.model;

import com.stitch.pojo.*;
import com.stitch.exception.*;
import java.util.*;

public class StitchModel
{
    private Map<String, Service> pathMap;
    public StitchModel()
    {
        this.pathMap = new HashMap<>();
    }
    public void put(String key, Service value) throws ServiceException
    {

        if(key == null) throw new ServiceException("Path cannot be null");
        if(key.length()==0) throw new ServiceException("Path length cannot be zero");
        if(!key.startsWith("/")) throw new ServiceException("Path should start with \"/\"");
        int i,j,count = 0;
        for(i = key.indexOf('/', 1), j = 1; i!=-1 && j!=i; j = i + 1, i = key.indexOf('/',j), ++count);
        if(j==i || count==0) throw new ServiceException("invalid path : "+key); 
        if(this.pathMap.containsKey(key)) throw new ServiceException("Servlet exists with path : " + key);
        if(value == null) throw new ServiceException("Service object can not be null for path : " + key);
        try
        {
            this.pathMap.put(key, value);
        }catch(Exception exception){
            throw new ServiceException(exception.getMessage());
        }
    }
    public Boolean has(String key) throws ServiceException
    {
        try
        {
            return this.pathMap.containsKey(key);
        }catch(Exception exception){
            throw new ServiceException(exception.getMessage());
        }
    }
    public Service get(String key) throws ServiceException
    {
        try
        {
            return pathMap.get(key);
        }catch(Exception exception){
            throw new ServiceException(exception.getMessage());
        }
    }
}