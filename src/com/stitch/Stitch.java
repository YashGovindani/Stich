package com.stitch;

import com.stitch.exception.*;
import com.stitch.annotation.*;
import com.stitch.pojo.*;
import com.stitch.model.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.lang.reflect.*;

public class Stitch extends HttpServlet
{
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
                service.getService().invoke(object, new Object[0]);
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