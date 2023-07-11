package it.gov.innovazione.ndc.harvester;

import it.gov.innovazione.ndc.repository.HarvestJobException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HarvesterJob {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Job harvestSemanticAssetsJob;
    private final Clock clock;
    @Value("#{'${harvester.repositories}'}")
    private final String repositories;

    public void harvest() {
        harvest(repositories);
    }

    public void harvest(String repositories) {
        try {
            LocalDateTime now = LocalDateTime.now(clock);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
            String currentDateTime = now.format(formatter);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("harvestTime", currentDateTime)
                    .addString("repositories", repositories)
                    .toJobParameters();
            JobExecution run = jobLauncher.run(harvestSemanticAssetsJob, jobParameters);
            log.info("Harvest job started at " + currentDateTime);
            log.info("Harvester job instance:" + run.getJobInstance());
        } catch (Exception e) {
            log.error("Error in harvest job ", e);
            throw new HarvestJobException(e);
        }
    }

    public List<JobExecutionStatusDto> getStatusOfLatestHarvestingJob() {
        JobInstance latestJobInstance = jobExplorer.getLastJobInstance("harvestSemanticAssetsJob");
        if (isNull(latestJobInstance)) {
            return emptyList();
        }

        return jobExplorer.getJobExecutions(latestJobInstance).stream().map(JobExecutionStatusDto::new).collect(toList());
    }

    public List<JobExecutionStatusDto> getStatusOfHarvestingJobs() {
        List<JobInstance> jobInstances = jobExplorer.getJobInstances("harvestSemanticAssetsJob", 0, 30);
        if (jobInstances.isEmpty()) {
            return emptyList();
        }

        List<JobExecutionStatusDto> statuses = new ArrayList<>();
        for (JobInstance jobInstance : jobInstances) {
            statuses.addAll(jobExplorer.getJobExecutions(jobInstance).stream().map(JobExecutionStatusDto::new).collect(toList()));
        }
        return statuses;
    }
}
