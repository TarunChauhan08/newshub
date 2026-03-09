package com.mycompany.myproject.core.servlets;

import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.day.cq.search.result.Hit;
import com.day.cq.search.PredicateGroup;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.jcr.Session;
import javax.jcr.RepositoryException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component(service = Servlet.class)
@SlingServletPaths("/bin/newshub/search")
public class ArticleSearchServlet extends SlingSafeMethodsServlet {

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws IOException {

        String keyword = request.getParameter("q");

        ResourceResolver resolver = request.getResourceResolver();
        Session session = resolver.adaptTo(Session.class);

        Map<String, String> map = new HashMap<>();

        map.put("path", "/content/myproject/us/en/news");
        map.put("type", "cq:Page");

        if (keyword != null && !keyword.isEmpty()) {
            map.put("fulltext", keyword);
        }

        map.put("p.limit", "10");
        map.put("orderby", "@jcr:content/publishDate");
        map.put("orderby.sort", "desc");

        Query query = queryBuilder.createQuery(PredicateGroup.create(map), session);
        SearchResult result = query.getResult();

        response.setContentType("application/json");

        response.getWriter().write("[");

        boolean first = true;

        for (Hit hit : result.getHits()) {

            try {
                Resource page = hit.getResource();
                Resource content = page.getChild("jcr:content");

                String title = content.getValueMap().get("jcr:title", "");
                String subtitle = content.getValueMap().get("subtitle", "");

                if (!first) {
                    response.getWriter().write(",");
                }

                response.getWriter().write("{");
                response.getWriter().write("\"title\":\"" + title + "\",");
                response.getWriter().write("\"subtitle\":\"" + subtitle + "\",");
                response.getWriter().write("\"path\":\"" + page.getPath() + "\"");
                response.getWriter().write("}");

                first = false;

            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }

        response.getWriter().write("]");
    }
}