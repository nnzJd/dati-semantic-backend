package it.teamdigitale.ndc.harvester;

import java.nio.file.Path;

public interface SemanticAssetHarvester {
    SemanticAssetType getType();

    void cleanUpBeforeHarvesting(String repoUrl);

    void harvest(String repoUrl, Path rootPath);
}