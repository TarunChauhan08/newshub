package com.mycompany.myproject.core.schedulers;

import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.event.jobs.JobManager;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Activate;

import java.util.HashMap;
import java.util.Map;

@Component(immediate = true)
public class AutomaticArticleArchiverScheduler {

    private static final String JOB_TOPIC = "myproject/archive/job";
    private static final String SCHEDULER_NAME = "MyProjectArticleArchiver";

    @Reference
    private Scheduler scheduler;

    @Reference
    private JobManager jobManager;

    @Activate
    protected void activate() {

        ScheduleOptions options = scheduler.EXPR("0 0 3 * * ?");
        options.name(SCHEDULER_NAME);
        options.canRunConcurrently(false);
        options.onLeaderOnly(true);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                triggerJob();
            }
        }, options);
    }

    private void triggerJob() {

        Map<String,Object> props = new HashMap<>();

        props.put("newsRoot","/content/myproject/us/en/news");
        props.put("archiveRoot","/content/myproject/us/en/archive");
        props.put("daysLimit",365);
        props.put("batchSize",20);

        jobManager.addJob(JOB_TOPIC, props);
    }
}