package com.stitch;

import com.stitch.pojo.*;
import com.stitch.model.*;
import com.stitch.exception.*;
import com.stitch.annotation.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class StartStitching extends HttpServlet
{
    private static void traversePackage(StitchModel model, File packageFolder,String currentPackage) throws ServiceException
    {
        try
        {
            System.out.println("Checking in package : " + currentPackage);
            for(File file:packageFolder.listFiles())
            {
                if(file.isDirectory())
                {
                    traversePackage(model, file, currentPackage + "." + file.getName());
                    continue;
                }
                if(!file.getName().endsWith(".class")) continue;
                String className = file.getName().substring(0,file.getName().length()-6);
                System.out.println("Checking for class : " + currentPackage + "." + className);
                Class classReference = Class.forName(currentPackage + "." + className);
                Class path = Class.forName("com.stitch.annotation.Path");
                Path pathObject = (Path)classReference.getAnnotation(Path.class);
                POST postObject = (POST)classReference.getDeclaredAnnotation(POST.class);
                Service.RequestMethod classRequestType = Service.GET;
                if(postObject != null) classRequestType =  Service.POST;
                if(pathObject == null) continue;
                String pathString = pathObject.value();
                for(Method method:classReference.getMethods())
                {
                    pathObject = (Path)method.getAnnotation(Path.class);
                    GET getObject = (GET)method.getAnnotation(GET.class);
                    postObject = (POST)method.getAnnotation(POST.class);
                    if(pathObject == null) continue;
                    pathString += pathObject.value();
                    Service.RequestMethod methodRequestType = classRequestType;
                    if(getObject != null) methodRequestType = Service.GET;
                    else if(postObject != null) methodRequestType = Service.POST;
                    Service service = new Service();
                    service.setServiceClass(classReference);
                    service.setPath(pathString);
                    service.setService(method);
                    service.setRequestMethod(methodRequestType);
                    Forward forwardAnnotation = method.getAnnotation(Forward.class);
                    if(forwardAnnotation != null) service.setForwardTo(forwardAnnotation.value());
                    model.put(pathString, service);
                    System.out.println("Added service path : " + pathString);
                }
            }
        }catch(Exception exception)
        {
            throw new ServiceException(exception.getMessage());
        }
    }
    public void init()
    {
        try
        {
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("/*** Initiating Stitch ***/");
            System.out.println();
            ServletConfig servletConfig = getServletConfig();
            String packageHead = servletConfig.getInitParameter("SERVICE_PACKAGE_PREFIX");
            StitchModel model = new StitchModel();
            ServletContext servletContext = getServletContext();
            String pathToClasses = servletContext.getRealPath("") + File.separator + "WEB-INF" + File.separator + "classes";
            if(!(new File(pathToClasses + File.separator + packageHead).exists())) throw new ServiceException("Invalid SERVICE_PACKAGE_PREFIX");
            traversePackage(model, new File(pathToClasses + File.separator + packageHead), packageHead);
            servletContext.setAttribute("model", model);
            System.out.println();
            System.out.println("/*** Stitch initiated ***/");
            System.out.println();
            System.out.println();
            System.out.println();
        }catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }
}