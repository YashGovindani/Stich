package com.stitch;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import com.stitch.exception.*;
import com.stitch.annotation.*;
import com.stitch.model.*;
import com.stitch.pojo.*;
import java.lang.reflect.*;
import java.util.*;
import com.google.gson.*;

public class StitchSecurity extends HttpServlet
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
            ServletContext servletContext = getServletContext();
            HttpSession session = request.getSession();
            ServletConfig servletConfig = getServletConfig();
            String requestParameterName = servletConfig.getInitParameter("REQUEST_PARAMETER");
            if(requestParameterName.length() == 0) throw new ServiceException("Length of REQUEST_PARAMETER cannot be zero");
            String path = request.getParameter(requestParameterName);
            if(path == null || path.length() == 0) throw new ServiceException("Request parameter \"" + requestParameterName + "\" is required");
            if(!path.startsWith("/")) 
            {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                throw new ServiceException("Path should start with \"/\"");
            }
            if(path.contains("/../") || path.endsWith("/.."))
            {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                throw new ServiceException("Request Parameter \"" + requestParameterName + "\" should not contain \"/../\" or end with \"/..\"");
            }
            File file = new File(servletContext.getRealPath("") + File.separator + "WEB-INF" + File.separator + "secured" + File.separator + path.substring(1).replaceAll("/",File.separator));
            if(!file.exists())
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            StitchModel model = (StitchModel)servletContext.getAttribute("model");
            if(model.hasSecurity(path))
            {
                List<Service> services = model.getSecurityList(path);
                Class serviceClass;
                Object object;
                for(Service service:services)
                {
                    serviceClass = service.getServiceClass();
                    object = serviceClass.getConstructor().newInstance();
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
                    AutoWired autoWiredAnnotation;
                    InjectRequestParameter injectRequestParameterAnnotation;
                    String name;
                    Object value;
                    String fieldName;
                    Class requestFieldClass;
                    String valueString;
                    for(Field field:serviceClass.getDeclaredFields())
                    {
                        injectRequestParameterAnnotation = (InjectRequestParameter)field.getAnnotation(InjectRequestParameter.class);
                        if(injectRequestParameterAnnotation != null)
                        {
                            name = injectRequestParameterAnnotation.value();
                            valueString = request.getParameter(name);
                            requestFieldClass = field.getType();
                            if(valueString == null) value = valueString;
                            else if(requestFieldClass.equals(Character.class) || requestFieldClass.equals(char.class)) value = valueString.charAt(0);
                            else if(requestFieldClass.equals(byte.class) || requestFieldClass.equals(Byte.class)) value = Byte.valueOf(valueString);
                            else if(requestFieldClass.equals(short.class) || requestFieldClass.equals(Short.class)) value = Short.valueOf(valueString);
                            else if(requestFieldClass.equals(int.class) || requestFieldClass.equals(Integer.class)) value = Integer.parseInt(valueString);
                            else if(requestFieldClass.equals(long.class) || requestFieldClass.equals(Long.class)) value = Long.valueOf(valueString);
                            else if(requestFieldClass.equals(float.class) || requestFieldClass.equals(Float.class)) value = Float.valueOf(valueString);
                            else if(requestFieldClass.equals(double.class) || requestFieldClass.equals(Double.class)) value = Double.valueOf(valueString);
                            else if(requestFieldClass.equals(boolean.class) || requestFieldClass.equals(Boolean.class)) value = Boolean.valueOf(valueString);
                            else if(requestFieldClass.equals(String.class)) value = valueString;
                            else value = new Gson().fromJson(valueString, requestFieldClass);
                            fieldName = field.getName();
                            fieldName = fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
                            Method method = serviceClass.getMethod("set" + fieldName, new Class[]{field.getType()});
                            if(method == null) throw new ServiceException("For service : " + path + ", setter not found to inject request parameter field : " + field.getName());
                            method.invoke(object, new Object[]{value});
                            continue;
                        }
                        autoWiredAnnotation = (AutoWired)field.getAnnotation(AutoWired.class);
                        if(autoWiredAnnotation == null) continue;
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
                    String requestParameterValue;
                    Class requestParameterClass;
                    for(Parameter parameter:service.getService().getParameters())
                    {
                        requestParameterAnnotation = (RequestParameter)parameter.getAnnotation(RequestParameter.class);
                        requestParameterClass = parameter.getType();
                        if(requestParameterAnnotation == null)
                        {
                            if(requestParameterClass.equals(ApplicationDirectory.class)) argumentList.add(new ApplicationDirectory(new File(servletContext.getRealPath(""))));
                            else if(requestParameterClass.equals(ApplicationScope.class)) argumentList.add(new ApplicationScope(servletContext));
                            else if(requestParameterClass.equals(RequestScope.class)) argumentList.add(new RequestScope(request));
                            else if(requestParameterClass.equals(SessionScope.class)) argumentList.add(new SessionScope(session));
                            else throw new ServiceException("For service : " + path +", either parameter should be annoted with " + RequestParameter.class + ", or should should have type either \n\t" + ApplicationDirectory.class + "\nor\t" + ApplicationScope.class + "\nor\t" + RequestScope.class + "\nor\t" + SessionScope.class);
                            continue;
                        }
                        requestParameterName = requestParameterAnnotation.value();
                        requestParameterValue = request.getParameter(requestParameterName);
                        if(requestParameterValue == null) argumentList.add(requestParameterValue);
                        else if(requestParameterClass.equals(Character.class) || requestParameterClass.equals(char.class)) argumentList.add(requestParameterValue.charAt(0));
                        else if(requestParameterClass.equals(byte.class) || requestParameterClass.equals(Byte.class)) argumentList.add(Byte.valueOf(requestParameterValue));
                        else if(requestParameterClass.equals(short.class) || requestParameterClass.equals(Short.class)) argumentList.add(Short.valueOf(requestParameterValue));
                        else if(requestParameterClass.equals(int.class) || requestParameterClass.equals(Integer.class)) argumentList.add(Integer.parseInt(requestParameterValue));
                        else if(requestParameterClass.equals(long.class) || requestParameterClass.equals(Long.class)) argumentList.add(Long.valueOf(requestParameterValue));
                        else if(requestParameterClass.equals(float.class) || requestParameterClass.equals(Float.class)) argumentList.add(Float.valueOf(requestParameterValue));
                        else if(requestParameterClass.equals(double.class) || requestParameterClass.equals(Double.class)) argumentList.add(Double.valueOf(requestParameterValue));
                        else if(requestParameterClass.equals(boolean.class) || requestParameterClass.equals(Boolean.class)) argumentList.add(Boolean.valueOf(requestParameterValue));
                        else if(requestParameterClass.equals(String.class)) argumentList.add(requestParameterValue);
                        else argumentList.add(new Gson().fromJson(requestParameterValue, requestParameterClass));
                    }
                    Object serviceResponse = service.getService().invoke(object, argumentList.toArray());
                    Boolean responseValue = false;
                    if(serviceResponse instanceof Boolean) responseValue = (Boolean)serviceResponse;
                    else responseValue = (boolean)serviceResponse;
                    if(!responseValue)
                    {
                        response.sendError(response.SC_FORBIDDEN);
                        return;
                    }
                }
            }
            PrintWriter printWriter = response.getWriter();
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            while(randomAccessFile.getFilePointer() < randomAccessFile.length()) printWriter.println(randomAccessFile.readLine());
            randomAccessFile.close(); 
        }catch(Exception exception)
        {
            exception.printStackTrace();
            try {
                response.sendError(response.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception e) {}
        }
    }
}