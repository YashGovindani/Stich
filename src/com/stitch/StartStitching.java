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
    private static List<Service> startupList;
    static{
        startupList = new ArrayList<Service>();
    }
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
                Boolean injectApplicationDirectory = (classReference.getDeclaredAnnotation(InjectApplicationDirectory.class)!=null);
                Boolean injectSessionScope = (classReference.getDeclaredAnnotation(InjectSessionScope.class)!=null);
                Boolean injectApplicationScope = (classReference.getDeclaredAnnotation(InjectApplicationScope.class)!=null);
                Boolean injectRequestScope = (classReference.getDeclaredAnnotation(InjectRequestScope.class)!=null);
                for(Method method:classReference.getMethods())
                {
                    Boolean isGetAllowedOnMethod = (isGetAllowedOnClass || (method.getAnnotation(GET.class) != null));
                    Boolean isPostAllowedOnMethod = (isPostAllowedOnClass || (method.getAnnotation(POST.class) != null));
                    pathObject = (Path)method.getAnnotation(Path.class);
                    if(pathObject == null) continue;
                    String servicePath = pathString + pathObject.value();
                    //pathString += pathObject.value();
                    Service service = new Service();
                    service.setServiceClass(classReference);
                    service.setPath(servicePath);
                    service.setService(method);
                    service.isGetAllowed(isGetAllowedOnMethod);
                    service.isPostAllowed(isPostAllowedOnMethod);
                    service.injectApplicationDirectory(injectApplicationDirectory);
                    service.injectApplicationScope(injectApplicationScope);
                    service.injectRequestScope(injectRequestScope);
                    service.injectSessionScope(injectSessionScope);
                    Forward forwardAnnotation = (Forward)method.getAnnotation(Forward.class);
                    if(forwardAnnotation != null) service.setForwardTo(forwardAnnotation.value());
                    OnStartup onStartup = (OnStartup)method.getAnnotation(OnStartup.class);
                    if(onStartup != null)
                    {
                        int priority = onStartup.priority();
                        if(priority <= 0) throw new ServiceException("For startup service : " + method.getName() + ", Priority should be >=1");
                        if(!method.getReturnType().equals(void.class)) throw new ServiceException("For startup service : " + method.getName() + ", return type should " + void.class);
                        if(method.getParameterTypes().length > 0) throw new ServiceException("Startup service : " + method.getName() + " should be non-parameterized");
                        service.setRunOnStartup(true);
                        service.setPriority(priority);
                        System.out.println("Startup service : " + service.getPath() + ", added with priority : " + priority);
                        startupList.add(service);
                    }
                    model.put(servicePath, service);
                    System.out.println("Added service path : " + servicePath);
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
            Collections.sort(startupList, (Service s1, Service s2) -> s1.getPriority() - s2.getPriority());
            for(Service service:startupList)
            {
                System.out.println("Executing service : " + service.getPath());
                Class serviceClass = service.getServiceClass();
                Object object = serviceClass.getConstructor().newInstance();
                if(service.injectApplicationScope())
                {
                    Field field = serviceClass.getDeclaredField("applicationScope");
                    if(field == null) throw new ServiceException("For service : " + service.getPath() + ", Service class : " + serviceClass + " should have property : applicationScope of type : " + ApplicationScope.class + " to inject application scope");
                    if(!field.getType().equals(ApplicationScope.class)) throw new ServiceException("For service : " + service.getPath() + ", Service class : " + serviceClass + " should have property : applicationScope of type : " + ApplicationScope.class + " to inject application scope");
                    Method method = serviceClass.getMethod("setApplicationScope",new Class[]{ApplicationScope.class});
                    method.invoke(object, new Object[]{new ApplicationScope(servletContext)});
                }
                service.getService().invoke(object, new Object[0]);
            }
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