package com.mycompany.myproject.core.schedulers;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.result.SearchResult;
import com.day.cq.search.result.Hit;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Session;
import javax.jcr.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

@Component(service = Runnable.class,
        property = {
                "scheduler.expression=0 0 3 * * * ?",
                "scheduler.concurrent=false"
        })
public class AutomaticArticleArchiver implements Runnable {

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    public void run() {

        try {

            Map<String,Object> params = new HashMap<>();
            params.put(ResourceResolverFactory.SUBSERVICE, "content-writer");

            ResourceResolver resolver = resolverFactory.getServiceResourceResolver(params);

            Session session = resolver.adaptTo(Session.class);

            Calendar dateLimit = Calendar.getInstance();
            dateLimit.add(Calendar.DAY_OF_YEAR, -365);

            Map<String,String> map = new HashMap<>();

            map.put("path","/content/myproject/us/en/news");
            map.put("type","cq:Page");
            map.put("1_property","jcr:content/publishDate");
            map.put("1_property.operation","less");
            map.put("1_property.value", String.valueOf(dateLimit.getTimeInMillis()));

            Query query = queryBuilder.createQuery(PredicateGroup.create(map), session);
            SearchResult result = query.getResult();

            List<Hit> hits = result.getHits();

            int success = 0;
            int failed = 0;
            int batchCount = 0;

            for(Hit hit : hits){

                try{

                    Node node = session.getNode(hit.getPath());

                    String newPath = "/content/myproject/us/en/archive/" + node.getName();
                    session.move(hit.getPath(), newPath);
                    success++;
                    batchCount++;
                    if(batchCount == 20){
                        session.save();
                        batchCount = 0;
                    }
                } catch(Exception e){
                    failed++;
                    session.refresh(false);
                    System.out.println("Failed to archive: " + hit.getPath());
                }
            }
            session.save();
            System.out.println("Archive Summary: success=" + success + " failed=" + failed);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}