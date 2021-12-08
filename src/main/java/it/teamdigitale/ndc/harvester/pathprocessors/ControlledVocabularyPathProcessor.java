package it.teamdigitale.ndc.harvester.pathprocessors;

import it.teamdigitale.ndc.harvester.CsvParser;
import it.teamdigitale.ndc.harvester.model.ControlledVocabularyModel;
import it.teamdigitale.ndc.harvester.model.CvPath;
import it.teamdigitale.ndc.harvester.model.SemanticAssetModelFactory;
import it.teamdigitale.ndc.repository.SemanticAssetMetadataRepository;
import it.teamdigitale.ndc.repository.TripleStoreRepository;
import it.teamdigitale.ndc.service.VocabularyDataService;
import it.teamdigitale.ndc.service.VocabularyIdentifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ControlledVocabularyPathProcessor extends BaseSemanticAssetPathProcessor<CvPath, ControlledVocabularyModel> {
    private final SemanticAssetModelFactory modelFactory;
    private final CsvParser csvParser;
    private final VocabularyDataService vocabularyDataService;
    private final String baseUrl;

    public ControlledVocabularyPathProcessor(TripleStoreRepository tripleStoreRepository, SemanticAssetModelFactory modelFactory,
                                             CsvParser csvParser, VocabularyDataService vocabularyDataService,
                                             SemanticAssetMetadataRepository metadataRepository,
                                             @Value("${ndc.baseUrl}") String baseUrl) {
        super(tripleStoreRepository, metadataRepository);
        this.modelFactory = modelFactory;
        this.csvParser = csvParser;
        this.vocabularyDataService = vocabularyDataService;
        this.baseUrl = baseUrl;
    }

    @Override
    protected void processWithModel(String repoUrl, CvPath path, ControlledVocabularyModel model) {
        super.processWithModel(repoUrl, path, model);

        path.getCsvPath().ifPresent(p -> {
            String keyConcept = model.getKeyConcept();
            String rightsHolder = model.getRightsHolderId();
            VocabularyIdentifier vocabularyIdentifier = new VocabularyIdentifier(rightsHolder, keyConcept);

            parseAndIndexCsv(vocabularyIdentifier, p);
        });
    }

    @Override
    protected ControlledVocabularyModel loadModel(String ttlFile, String repoUrl) {
        return modelFactory.createControlledVocabulary(ttlFile, repoUrl);
    }

    @Override
    protected void enrichModelBeforePersisting(ControlledVocabularyModel model, CvPath path) {
        path.getCsvPath().ifPresent(p -> model.addNdcUrlProperty(baseUrl));
    }

    private void parseAndIndexCsv(VocabularyIdentifier vocabularyIdentifier, String csvPath) {
        List<Map<String, String>> flatData = csvParser.convertCsvToMapList(csvPath);
        vocabularyDataService.indexData(vocabularyIdentifier, flatData);
    }
}
