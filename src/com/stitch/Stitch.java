package com.stitch;

import com.stitch.exception.*;
import com.stitch.annotation.*;
import com.stitch.pojo.*;
import com.stitch.model.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

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
                Object object = service.getServiceClass().getConstructor().newInstance();
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