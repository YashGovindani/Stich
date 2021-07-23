package com.stitch;

import com.stitch.exception.*;
import com.stitch.annotation.*;
import com.stitch.pojo.*;
import com.stitch.model.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class Stitch extends HttpServlet
{
    private boolean compareFieldType(Field field, Object obj)
    {
        boolean result = field.getType().isInstance(obj);
        if(result) return result;
        if(obj instanceof Character && field.getType().equals(char.class)) return true;
        if(obj instanceof Byte && field.getType().equals(byte.class)) return true;
        if(obj instanceof Short && field.getType().equals(short.class)) return true;
        if(obj instanceof Integer && field.getType().equals(int.class)) return true;
        if(obj instanceof Long && field.getType().equals(long.class)) return true;
        if(obj instanceof Float && field.getType().equals(float.class)) return true;
        if(obj instanceof Double && field.getType().equals(double.class)) return true;
        if(obj instanceof Boolean && field.getType().equals(boolean.class)) return true;
        return result;
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        doGet(request, response);
    }
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            String requestURI = request.getRequestURI();
            String servletPath = request.getServletPath();
            String path = requestURI.substring(requestURI.indexOf(servletPath) + servletPath.length());
            ServletContext servletContext = getServletContext();
            HttpSession session = request.getSession();
            StitchModel model = (StitchModel)servletContext.getAttribute("model");
            if(model.has(path))
            {
                Service service = model.get(path);
                String requestMethod = request.getMethod();
                if(requestMethod.equals("GET") && !service.isGetAllowed())
                {
                    response.sendError(response.SC_METHOD_NOT_ALLOWED);
                    return;
                }
                if(requestMethod.equals("POST") && service.isPostAllowed())
                {
                    response.sendError(response.SC_METHOD_NOT_ALLOWED);
                    return;
                }
                Class serviceClass = service.getServiceClass();
                Object object = serviceClass.getConstructor().newInstance();
                if(service.injectApplicationDirectory())
                {
                    Field field = serviceClass.getDeclaredField("applicationDirectory");
                    if(field == null) throw new ServiceException("For service : " + path + ", Service class : " + serviceClass + " should have property : applicationDirectory of type : " + ApplicationDirectory.class + " to inject application directory");
                    if(!field.getType().equals(ApplicationDirectory.class)) throw new ServiceException("For service : " + path + ", Service class : " + serviceClass + " should have property : applicationDirectory of type : " + ApplicationDirectory.class + " to inject application directory");
                    Method method = serviceClass.getMethod("setApplicationDirectory",new Class[]{ApplicationDirectory.class});
                    method.invoke(object, new Object[]{new ApplicationDirectory(new File(servletContext.getRealPath("")))});
                }
                if(service.injectApplicationScope())
                {
                    Field field = serviceClass.getDeclaredField("applicationScope");
                    if(field == null) throw new ServiceException("For service : " + path + ", Service class : " + serviceClass + " should have property : applicationScope of type : " + ApplicationScope.class + " to inject application scope");
                    if(!field.getType().equals(ApplicationScope.class)) throw new ServiceException("For service : " + path + ", Service class : " + serviceClass + " should have property : applicationScope of type : " + ApplicationScope.class + " to inject application scope");
                    Method method = serviceClass.getMethod("setApplicationScope",new Class[]{ApplicationScope.class});
                    method.invoke(object, new Object[]{new ApplicationScope(servletContext)});
                }
                if(service.injectRequestScope())
                {
                    Field field = serviceClass.getDeclaredField("requestScope");
                    if(field == null) throw new ServiceException("For service : " + path + ", Service class : " + serviceClass + " should have property : requestScope of type : " + RequestScope.class + " to inject request scope");
                    if(!field.getType().equals(RequestScope.class)) throw new ServiceException("For service : " + path + ", Service class : " + serviceClass + " should have property : requestScope of type : " + RequestScope.class + " to inject request scope");
                    Method method = serviceClass.getMethod("setRequestScope",new Class[]{RequestScope.class});
                    method.invoke(object, new Object[]{new RequestScope(request)});
                }
                if(service.injectSessionScope())
                {
                    Field field = serviceClass.getDeclaredField("sessionScope");
                    if(field == null) throw new ServiceException("For service : " + path + ", Service class : " + serviceClass + " should have property : sessionScope of type : " + SessionScope.class + " to inject session scope");
                    if(!field.getType().equals(SessionScope.class)) throw new ServiceException("For service : " + path + ", Service class : " + serviceClass + " should have property : sessionScope of type : " + SessionScope.class + " to inject session scope");
                    Method method = serviceClass.getMethod("setSessionScope",new Class[]{SessionScope.class});
                    method.invoke(object, new Object[]{new SessionScope(session)});
                }
                List<Field> autoWiredList = new ArrayList<>(); // though not required but said by sir
                AutoWired autoWiredAnnotation;
                String name;
                Object value;
                String fieldName;
                for(Field field:serviceClass.getDeclaredFields())
                {
                    autoWiredAnnotation = (AutoWired)field.getAnnotation(AutoWired.class);
                    if(autoWiredAnnotation == null) continue;
                    autoWiredList.add(field);
                    name = autoWiredAnnotation.name();
                    value = request.getAttribute(name);
                    if(value == null) value = session.getAttribute(name);
                    if(value == null) value = servletContext.getAttribute(name);
                    if(value != null && !compareFieldType(field, value)) throw new ServiceException("For service : " + path + ", to auto wire field : " + field.getName() + ", expected : " + field.getType() + ", got : " + value.getClass());
                    fieldName = field.getName();
                    fieldName = fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
                    Method method = serviceClass.getMethod("set" + fieldName, new Class[]{field.getType()});
                    if(method == null) throw new ServiceException("For service : " + path + ", setter not found to auto wire field : " + field.getName());
                    method.invoke(object, new Object[]{value});
                } 
                RequestParameter requestParameterAnnotation;
                List<Object> argumentList = new ArrayList<>();
                String requestParameterName;
                String requestParameterValue;
                Class requestParameterClass;
                for(Parameter parameter:service.getService().getParameters())
                {
                    requestParameterAnnotation = (RequestParameter)parameter.getAnnotation(RequestParameter.class);
                    if(requestParameterAnnotation == null) throw new ServiceException("For service : " + path +", parameter should be annoted with " + RequestParameter.class);
                    requestParameterName = requestParameterAnnotation.value();
                    requestParameterValue = request.getParameter(requestParameterName);
                    requestParameterClass = parameter.getType();
                    if(requestParameterValue == null) argumentList.add(requestParameterValue);
                    else if(requestParameterClass.equals(Character.class) || requestParameterClass.equals(char.class)) argumentList.add(requestParameterValue.charAt(0));
                    else if(requestParameterClass.equals(byte.class) || requestParameterClass.equals(Byte.class)) argumentList.add(Byte.valueOf(requestParameterValue));
                    else if(requestParameterClass.equals(short.class) || requestParameterClass.equals(Short.class)) argumentList.add(Short.valueOf(requestParameterValue));
                    else if(requestParameterClass.equals(int.class) || requestParameterClass.equals(Integer.class)) argumentList.add(Integer.parseInt(requestParameterValue));
                    else if(requestParameterClass.equals(long.class) || requestParameterClass.equals(Long.class)) argumentList.add(Long.valueOf(requestParameterValue));
                    else if(requestParameterClass.equals(float.class) || requestParameterClass.equals(Float.class)) argumentList.add(Float.valueOf(requestParameterValue));
                    else if(requestParameterClass.equals(double.class) || requestParameterClass.equals(Double.class)) argumentList.add(Double.valueOf(requestParameterValue));
                    else if(requestParameterClass.equals(boolean.class) || requestParameterClass.equals(Boolean.class)) argumentList.add(Boolean.valueOf(requestParameterValue));
                    else argumentList.add(requestParameterValue);
                }
                service.getService().invoke(object, argumentList.toArray());
                if(service.getForwardTo().length() == 0) return;
                RequestDispatcher requestDispatcher = request.getRequestDispatcher(service.getForwardTo());
                requestDispatcher.forward(request, response);
                return;
            }
            RequestDispatcher requestDispatcher = request.getRequestDispatcher(path);
            requestDispatcher.forward(request, response);
        }catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }
}