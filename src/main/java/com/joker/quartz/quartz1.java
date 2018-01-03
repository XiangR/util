package com.joker.quartz;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import com.joker.fileupload.ConfigParser;
import com.joker.staticcommon.TimeUtility;

public class quartz1 {

    static String quartzJob4 = ConfigParser.getCommonProperty("quartzJob4");

    public static void main(String[] args) {
        // @SuppressWarnings("unused")
        // quartzJob1 quartzJob1 = new quartzJob1();
        // @SuppressWarnings("unused")
        // quartzJob2 quartzJob2 = new quartzJob2();
        // @SuppressWarnings("unused")
        // quartzJob3 quartzJob3 = new quartz1().new quartzJob3();

        new quartz1().run();
    }

    private void run() {

        try {
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();

            // job 1 将每隔 10 秒执行一次
            JobDetail job = newJob(quartzJob1.class).withIdentity("job1", "group1").build();
            // CronTrigger trigger = newTrigger().withIdentity("trigger1",
            // "group1").withSchedule(cronSchedule("0 0 0 1 1 ?")).build();
            CronTrigger trigger = newTrigger().withIdentity("trigger1", "group1").withSchedule(cronSchedule("0/5 * * * * ?")).build();
            Date ft = scheduler.scheduleJob(job, trigger);
            System.out.print(job.getKey() + "已被安排执行于:" + TimeUtility.toStringMore(ft));
            System.out.println("，并且以如下重复规则重复执行:" + trigger.getCronExpression());

            job = newJob(quartzJob4.class).withIdentity("job2", "group1").build();
            trigger = newTrigger().withIdentity("trigger2", "group1").withSchedule(cronSchedule("0 0 0 * * ?")).build();
            // trigger = newTrigger().withIdentity("trigger2",
            // "group1").withSchedule(cronSchedule(quartzJob4)).build();
            ft = scheduler.scheduleJob(job, trigger);
            System.out.print(job.getKey() + "已被安排执行于:" + TimeUtility.toStringMore(ft));
            System.out.println("，并且以如下重复规则重复执行:" + trigger.getCronExpression());
            // 开始执行，start() 方法被调用后，计时器就开始工作，计时调度中允许放入 N 个 Job
            scheduler.start();

            Thread.sleep(100000);

            scheduler.shutdown(true);
        } catch (SchedulerException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class quartzJob1 implements Job {
        @Override
        public void execute(JobExecutionContext arg0) throws JobExecutionException {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
            System.out.println("hello now is: " + sdf.format(new Date()));
        }
    }

    public static class quartzJob4 implements Job {
        @Override
        public void execute(JobExecutionContext arg0) throws JobExecutionException {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
            System.out.println("hello I am quartzJob4 now is: " + sdf.format(new Date()));
        }
    }

    static class quartzJob2 implements Job {
        @Override
        public void execute(JobExecutionContext arg0) throws JobExecutionException {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
            System.out.println("hello now is: " + sdf.format(new Date()));
        }
    }

    class quartzJob3 implements Job {
        @Override
        public void execute(JobExecutionContext arg0) throws JobExecutionException {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
            System.out.println("hello now is: " + sdf.format(new Date()));
        }
    }
}
