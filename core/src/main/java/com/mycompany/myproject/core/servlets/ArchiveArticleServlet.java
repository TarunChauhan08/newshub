package com.mycompany.myproject.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.resource.ResourceResolver;

import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import javax.servlet.Servlet;
import javax.jcr.Session;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.io.IOException;

@Component(service = Servlet.class)
@SlingServletPaths("/bin/newshub/archive")
public class ArchiveArticleServlet extends SlingAllMethodsServlet {

    @Override
    protected void doPost(SlingHttpServletRequest request,
                          SlingHttpServletResponse response) throws IOException {

        String articlePath = request.getParameter("articlePath");

        ResourceResolver resolver = request.getResourceResolver();
        Session session = resolver.adaptTo(Session.class);

        String archivePath = "/content/myproject/us/en/archive";

        try {

            Node articleNode = session.getNode(articlePath);

            String articleName = articleNode.getName();

            String newPath = archivePath + "/" + articleName;

            session.move(articlePath, newPath);

            session.save();

            response.getWriter().write("Article archived successfully");

        } catch (RepositoryException e) {
            response.getWriter().write("Error archiving article");
        }
    }
}