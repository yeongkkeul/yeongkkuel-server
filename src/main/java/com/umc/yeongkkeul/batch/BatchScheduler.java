package com.umc.yeongkkeul.batch;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job updateUserScoreJob;
    private final Job updateChatRoomScoreJob;

    @Scheduled(cron = "0 0 0 * * ?")
    public void runBatchJobs() throws Exception{
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp",System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(updateUserScoreJob,jobParameters);
        jobLauncher.run(updateChatRoomScoreJob,jobParameters);
    }
}
