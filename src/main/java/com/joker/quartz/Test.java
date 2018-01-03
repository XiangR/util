package com.joker.quartz;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

public class Test {

	public static void main(String[] args) throws Exception {
		Test.go();
	}

	public static void go() {
		try {

			// 首先，必需要取得一个 Scheduler 的引用
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler sched = sf.getScheduler();
			// jobs 可以在 scheduled 的 sched.start() 方法前被调用

			// job 1 将每隔 20 秒执行一次
			JobDetail job = newJob(myJob.class).withIdentity("job1", "group1").build();
			CronTrigger trigger = newTrigger().withIdentity("trigger1", "group1").withSchedule(cronSchedule("0/10 * * * * ?")).build();
			Date ft = sched.scheduleJob(job, trigger);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
			System.out.println(job.getKey() + "已被安排执行于:" + sdf.format(ft) + "，并且以如下重复规则重复执行:" + trigger.getCronExpression());

			// job 2 将每 2 分钟执行一次（在该分钟的第 15 秒)
			job = newJob(myJob.class).withIdentity("job2", "group1").build();
			trigger = newTrigger().withIdentity("trigger2", "group1").withSchedule(cronSchedule("15 0/2 * * * ?")).build();
			ft = sched.scheduleJob(job, trigger);
			System.out.println(job.getKey() + "已被安排执行于:" + sdf.format(ft) + "，并且以如下重复规则重复执行:" + trigger.getCronExpression());

			// 开始执行，start() 方法被调用后，计时器就开始工作，计时调度中允许放入 N 个 Job
			sched.start();
			// 主线程等待一分钟
			// Thread.sleep(60L * 1000L);
			// 关闭定时调度，定时器不再工作
			// sched.shutdown(true);
		} catch (SchedulerException e) {

		}

	}
}