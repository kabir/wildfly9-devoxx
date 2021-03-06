package org.kabir.example.modcluster.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Stuart Douglas
 */
@WebServlet(urlPatterns = "")
public class SimpleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //-Djboss.node.name is set by the bootstrap
        resp.getWriter().write("Server " + System.getProperty("jboss.node.name") + " ");
        if(req.getParameter("stateful") != null) {
            HttpSession session = req.getSession(true);
            Integer count = (Integer) session.getAttribute("count");
            if(count == null) {
                count = 1;
            }
            resp.getWriter().write("Request Count " + count);
            session.setAttribute("count", ++count);
        }
    }
}