package it.gov.innovazione.ndc.config;

import it.gov.innovazione.ndc.harvester.HarvesterService;
import it.gov.innovazione.ndc.repository.HarvestJobException;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;

@Slf4j
public class HarvestRepositoryProcessor implements Tasklet, StepExecutionListener {
    private final HarvesterService harvesterService;
    private List<String> repos;
    private List<String> failedRepos;
    private Boolean envRepositories;

    public HarvestRepositoryProcessor(HarvesterService harvesterService) {
        this.harvesterService = harvesterService;
    }

    // Used for Testing
    public HarvestRepositoryProcessor(HarvesterService harvesterService, List<String> repos) {
        this.harvesterService = harvesterService;
        this.repos = repos;
        this.failedRepos = new ArrayList<>();
        this.envRepositories = false;
    }
    
    // Used for Testing
    public HarvestRepositoryProcessor(HarvesterService harvesterService, List<String> repos, Boolean envRepository) {
        this.harvesterService = harvesterService;
        this.repos = repos;
        this.failedRepos = new ArrayList<>();
        this.envRepositories = envRepository;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        JobParameters parameters = stepExecution.getJobExecution().getJobParameters();
        String repositories = parameters.getString("repositories");
        this.repos = isNull(repositories) ? new ArrayList<>() : Arrays.asList(repositories.split(","));
        this.failedRepos = new ArrayList<>();
        String envParam = parameters.getString("envRepositories");
        this.envRepositories = Objects.nonNull(envParam) && StringUtils.hasText(envParam) ? Boolean.valueOf(envParam) : Boolean.FALSE;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // if the harvester is launched for processing declared repos, delete obsolete graphs from virtuoso
        if (this.envRepositories) {
            try {
                harvesterService.deleteUnprocessingRepos(repos);
            } catch (Exception e) {
                log.error("Unable to verify if there are unprocessed repository stored on triple store. Skip this step. ", e);
            }
        }
        
        for (String repo : repos) {
            try {
                harvesterService.harvest(repo);
            } catch (Exception e) {
                log.error("Unable to process {}", repo, e);
                failedRepos.add(repo);
            }
        }
        if (!failedRepos.isEmpty()) {
            throw new HarvestJobException(String.format("Harvesting failed for repos '%s'", failedRepos));
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return failedRepos.isEmpty() ? ExitStatus.COMPLETED : ExitStatus.FAILED;
    }
}
