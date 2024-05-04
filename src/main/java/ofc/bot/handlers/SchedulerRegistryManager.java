package ofc.bot.handlers;

import ofc.bot.content.annotations.jobs.CronJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Set;

public class SchedulerRegistryManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerRegistryManager.class);
    private static final Reflections reflections = new Reflections(new ConfigurationBuilder().forPackage("ofc.bot"));

    public static void initializeSchedulers() throws SchedulerException {

        Set<Class<?>> jobs = reflections.getTypesAnnotatedWith(CronJob.class);
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        for (Class<?> jobClass : jobs) {

            if (!Job.class.isAssignableFrom(jobClass))
                throw new IllegalStateException("Class '" + jobClass.getName() + "' annotated with CronJob does not implement the " + Job.class.getName() + " interface");

            try {
                Constructor<?> constructor = jobClass.getConstructor();
                constructor.setAccessible(true);
                Job job = (Job) constructor.newInstance();

                JobDetail detail = getDetail(job);
                Trigger trigger = getTrigger(job);

                scheduler.scheduleJob(detail, trigger);

            } catch (ReflectiveOperationException e) {
                LOGGER.error("Could not instantiate job at " + jobClass.getName(), e);
            }
        }

        scheduler.start();
    }

    private static JobDetail getDetail(Job job) {

        Class<? extends Job> jobClass = job.getClass();
        CronJob jobAnnotation = jobClass.getDeclaredAnnotation(CronJob.class);

        if (jobAnnotation == null)
            throw new IllegalArgumentException("Job class " + jobClass.getName() + " must be annotated with CronJob");

        String identity = jobAnnotation.identity();
        String name = identity.isBlank()
                ? jobClass.getSimpleName()
                : identity;

        return JobBuilder.newJob(jobClass)
                .withIdentity(name)
                .build();
    }

    private static Trigger getTrigger(Job job) {

        Class<? extends Job> jobClass = job.getClass();
        CronJob jobAnnotation = jobClass.getDeclaredAnnotation(CronJob.class);

        if (jobAnnotation == null)
            throw new IllegalArgumentException("Job class " + jobClass.getName() + " must be annotated with CronJob");

        String identity = jobAnnotation.identity();
        String name = identity.isBlank()
                ? jobClass.getSimpleName()
                : identity;

        return TriggerBuilder.newTrigger()
                .withIdentity(name)
                .withSchedule(CronScheduleBuilder.cronSchedule(jobAnnotation.expression()))
                .build();
    }
}