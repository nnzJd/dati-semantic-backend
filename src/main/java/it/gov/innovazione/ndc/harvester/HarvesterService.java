package it.gov.innovazione.ndc.harvester;

import it.gov.innovazione.ndc.config.HarvestExecutionContext;
import it.gov.innovazione.ndc.config.HarvestExecutionContextUtils;
import it.gov.innovazione.ndc.model.harvester.Repository;
import it.gov.innovazione.ndc.repository.SemanticAssetMetadataRepository;
import it.gov.innovazione.ndc.repository.TripleStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class HarvesterService {
    private final AgencyRepositoryService agencyRepositoryService;
    private final List<SemanticAssetHarvester> semanticAssetHarvesters;
    private final TripleStoreRepository tripleStoreRepository;
    private final SemanticAssetMetadataRepository semanticAssetMetadataRepository;

    public void harvest(Repository repository) throws IOException {
        harvest(repository, null);
    }

    public void harvest(Repository repository, String revision) throws IOException {
        log.info("Processing repo {}", repository.getUrl());
        Repository normalisedRepo = repository.withUrl(normaliseRepoUrl(repository.getUrl()));
        String repoUrl = normalisedRepo.getUrl();
        log.debug("Normalised repo url {}", repoUrl);
        try {
            Path path = cloneRepoToTempPath(repoUrl, revision);

            try {
                updateContextWithRootPath(path);
                harvestClonedRepo(normalisedRepo, path);
            } finally {
                agencyRepositoryService.removeClonedRepo(path);
            }
            log.info("Repo {} processed correctly", repoUrl);
        } catch (IOException e) {
            log.error("Exception while processing {}", repoUrl, e);
            throw e;
        }
    }

    private static void updateContextWithRootPath(Path path) {
        HarvestExecutionContext context = HarvestExecutionContextUtils.getContext();
        if (Objects.nonNull(context)) {
            HarvestExecutionContextUtils.setContext(context.withRootPath(path.toString()));
        }
    }

    public void clear(String repoUrl) {
        log.info("Clearing repo {}", repoUrl);
        repoUrl = normaliseRepoUrl(repoUrl);
        log.debug("Normalised repo url {}", repoUrl);
        try {
            clearRepo(repoUrl);
            log.info("Repo {} cleared", repoUrl);
        } catch (Exception e) {
            log.error("Error while clearing {}", repoUrl, e);
            throw e;
        }
    }

    private String normaliseRepoUrl(String repoUrl) {
        return repoUrl.replace(".git", "");
    }

    private void harvestClonedRepo(Repository repository, Path path) {
        clearRepo(repository.getUrl());

        harvestSemanticAssets(repository, path);

        log.info("Repo {} processed", repository);
    }

    private void clearRepo(String repoUrl) {
        cleanUpWithHarvesters(repoUrl);
        cleanUpTripleStore(repoUrl);
        cleanUpIndexedMetadata(repoUrl);
    }

    private void cleanUpWithHarvesters(String repoUrl) {
        semanticAssetHarvesters.forEach(h -> {
            log.debug("Cleaning for {} before harvesting {}", h.getType(), repoUrl);
            h.cleanUpBeforeHarvesting(repoUrl);

            log.debug("Cleaned for {}", h.getType());
        });
    }

    private void harvestSemanticAssets(Repository repository, Path path) {
        semanticAssetHarvesters.forEach(h -> {
            log.debug("Harvesting {} for {} assets", path, h.getType());
            h.harvest(repository, path);

            log.debug("Harvested {} for {} assets", path, h.getType());
        });
    }

    private Path cloneRepoToTempPath(String repoUrl, String revision) throws IOException {
        Path path = agencyRepositoryService.cloneRepo(repoUrl, revision);
        log.debug("Repo {} cloned to temp folder {}", repoUrl, path);
        return path;
    }

    private void cleanUpTripleStore(String repoUrl) {
        log.debug("Cleaning up triple store for {}", repoUrl);
        tripleStoreRepository.clearExistingNamedGraph(repoUrl);
    }

    private void cleanUpIndexedMetadata(String repoUrl) {
        log.debug("Cleaning up indexed metadata for {}", repoUrl);
        long deletedCount = semanticAssetMetadataRepository.deleteByRepoUrl(repoUrl);
        log.debug("Deleted {} indexed metadata for {}", deletedCount, repoUrl);
    }
}
