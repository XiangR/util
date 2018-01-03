package com.joker.quartz;

import java.text.SimpleDateFormat;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class myJob implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
		System.out.println("hello now is: " + sdf.format(new Date()));
	}

}