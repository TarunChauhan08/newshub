package com.mycompany.myproject.core.jobs;

import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.result.SearchResult;
import com.day.cq.search.result.Hit;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceResolver;

import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.apache.sling.event.jobs.Job;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;
import javax.jcr.Node;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        service = JobConsumer.class,
        property = {
                JobConsumer.PROPERTY_TOPICS + "=myproject/archive/job"
        }
)
public class AutomaticArticleArchiverJobConsumer implements JobConsumer {

    private static final Logger LOG =
            LoggerFactory.getLogger(AutomaticArticleArchiverJobConsumer.class);

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    public JobResult process(Job job) {

        String newsRoot = (String) job.getProperty("newsRoot");
        String archiveRoot = (String) job.getProperty("archiveRoot");
        int daysLimit = (Integer) job.getProperty("daysLimit");
        int batchSize = (Integer) job.getProperty("batchSize");

        int success = 0;
        int failed = 0;
        int batchCount = 0;

        try {

            Map<String,Object> params = new HashMap<>();
            params.put(ResourceResolverFactory.SUBSERVICE,"content-writer");

            ResourceResolver resolver =
                    resolverFactory.getServiceResourceResolver(params);

            Session session = resolver.adaptTo(Session.class);

            Calendar dateLimit = Calendar.getInstance();
            dateLimit.add(Calendar.DAY_OF_YEAR, -daysLimit);

            Map<String,String> map = new HashMap<>();

            map.put("path",newsRoot);
            map.put("type","cq:Page");
            map.put("1_property","jcr:content/publishDate");
            map.put("1_property.operation","less");
            map.put("1_property.value",
                    String.valueOf(dateLimit.getTimeInMillis()));

            Query query =
                    queryBuilder.createQuery(PredicateGroup.create(map), session);

            SearchResult result = query.getResult();

            List<Hit> hits = result.getHits();

            for(Hit hit : hits){

                try{

                    Node contentNode = session.getNode(hit.getPath());
                    Node pageNode = contentNode.getParent();

                    String sourcePath = pageNode.getPath();
                    String destinationPath =
                            archiveRoot + "/" + pageNode.getName();

                    session.move(sourcePath, destinationPath);

                    success++;
                    batchCount++;

                    if(batchCount == batchSize){

                        session.save();
                        batchCount = 0;

                    }

                } catch(Exception e){

                    failed++;

                    session.refresh(false);

                    LOG.error("Failed to archive article {}", hit.getPath(), e);
                }
            }

            if(batchCount > 0){
                session.save();
            }

            LOG.info("Archive Summary → Success: {} Failed: {}", success, failed);

        } catch(Exception e){

            LOG.error("Archive job failed", e);
            return JobResult.FAILED;
        }

        return JobResult.OK;
    }
}