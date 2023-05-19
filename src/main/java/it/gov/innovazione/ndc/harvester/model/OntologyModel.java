package it.gov.innovazione.ndc.harvester.model;

import static it.gov.innovazione.ndc.model.profiles.Admsapit.hasSemanticAssetDistribution;
import static it.gov.innovazione.ndc.model.profiles.EuropePublicationVocabulary.FILE_TYPE_RDF_TURTLE;
import static org.apache.jena.rdf.model.ResourceFactory.createProperty;

import java.util.List;

import it.gov.innovazione.ndc.harvester.model.exception.InvalidModelException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDFS;

import it.gov.innovazione.ndc.harvester.SemanticAssetType;
import it.gov.innovazione.ndc.harvester.model.extractors.LiteralExtractor;
import it.gov.innovazione.ndc.harvester.model.extractors.NodeSummaryExtractor;
import it.gov.innovazione.ndc.harvester.model.index.Distribution;
import it.gov.innovazione.ndc.harvester.model.index.NodeSummary;
import it.gov.innovazione.ndc.harvester.model.index.SemanticAssetMetadata;
import it.gov.innovazione.ndc.model.profiles.Admsapit;
import it.gov.innovazione.ndc.validator.model.ErrorValidatorMessage;
import it.gov.innovazione.ndc.validator.model.WarningValidatorMessage;

public class OntologyModel extends BaseSemanticAssetModel {

    public OntologyModel(Model coreModel, String source, String repoUrl) {
        super(coreModel, source, repoUrl);
    }

    @Override
    protected String getMainResourceTypeIri() {
        return SemanticAssetType.ONTOLOGY.getTypeIri();
    }

    @Override
    public SemanticAssetMetadata extractMetadata() {
        return super.extractMetadata().toBuilder()
            .type(SemanticAssetType.ONTOLOGY)
            .distributions(getDistributions())
            .keyClasses(getKeyClasses())
            .prefix(LiteralExtractor.extractOptional(getMainResource(), Admsapit.prefix))
            .projects(NodeSummaryExtractor.maybeNodeSummaries(getMainResource(), Admsapit.semanticAssetInUse,
                createProperty("https://w3id.org/italia/onto/l0/name")))
            .build();
    }


    /*
     * Use the same methods of extract Metadata. Instead to throw exceptions, it add the error inside the collection.
     */
    @Override
    public void validateMetadata(List<ErrorValidatorMessage> errors,
                                 List<WarningValidatorMessage> warnings) {
        try {
            super.validateMetadata(errors, warnings);
        } catch (InvalidModelException ex) {
            errors.add(new ErrorValidatorMessage(null, ex.getMessage()));
            return;
        }
        getDistributions(errors, warnings, SemanticAssetMetadata.Fields.distributions);
        getKeyClasses(warnings, SemanticAssetMetadata.Fields.keyClasses);
        LiteralExtractor.extractOptional(getMainResource(), Admsapit.prefix, warnings, SemanticAssetMetadata.Fields.prefix);
        NodeSummaryExtractor.maybeNodeSummaries(getMainResource(), Admsapit.semanticAssetInUse,
                createProperty("https://w3id.org/italia/onto/l0/name"), warnings, SemanticAssetMetadata.Fields.projects);
    }

    private List<NodeSummary> getKeyClasses() {
        return NodeSummaryExtractor.maybeNodeSummaries(getMainResource(), Admsapit.hasKeyClass, RDFS.label);
    }

    private List<NodeSummary> getKeyClasses(List<WarningValidatorMessage> warnings, String fieldName) {
        return NodeSummaryExtractor.maybeNodeSummaries(getMainResource(), Admsapit.hasKeyClass, RDFS.label, warnings, fieldName);
    }

    protected List<Distribution> getDistributions() {
        return extractDistributionsFilteredByFormat(hasSemanticAssetDistribution, FILE_TYPE_RDF_TURTLE);
    }

    private List<Distribution> getDistributions(List<ErrorValidatorMessage> errors, List<WarningValidatorMessage> warnings, String fieldName) {
        return extractDistributionsFilteredByFormat(hasSemanticAssetDistribution, FILE_TYPE_RDF_TURTLE, errors, warnings, fieldName);
    }
}
