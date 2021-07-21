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
                Path pathObject = (Path)classReference.getAnnotation(Path.class);
                Boolean isGetAllowedOnClass = (classReference.getDeclaredAnnotation(GET.class) != null);
                Boolean isPostAllowedOnClass = (classReference.getDeclaredAnnotation(POST.class) != null);
                if(pathObject == null) continue;
                String pathString = pathObject.value();
                for(Method method:classReference.getMethods())
                {
                    Boolean isGetAllowedOnMethod = (isGetAllowedOnClass || (method.getAnnotation(GET.class) != null));
                    Boolean isPostAllowedOnMethod = (isPostAllowedOnClass || (method.getAnnotation(POST.class) != null));
                    pathObject = (Path)method.getAnnotation(Path.class);
                    if(pathObject == null || (!isGetAllowedOnMethod && !isPostAllowedOnMethod)) continue;
                    pathString += pathObject.value();
                    Service service = new Service();
                    service.setServiceClass(classReference);
                    service.setPath(pathString);
                    service.setService(method);
                    service.isGetAllowed(isGetAllowedOnMethod);
                    service.isPostAllowed(isPostAllowedOnMethod);
                    Forward forwardAnnotation = (Forward)method.getAnnotation(Forward.class);
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