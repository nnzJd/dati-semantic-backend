package it.gov.innovazione.ndc.harvester.model;

import static it.gov.innovazione.ndc.harvester.SemanticAssetType.CONTROLLED_VOCABULARY;
import static it.gov.innovazione.ndc.model.profiles.EuropePublicationVocabulary.FILE_TYPE_RDF_TURTLE;
import static java.lang.String.format;
import static org.apache.jena.vocabulary.DCAT.distribution;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import it.gov.innovazione.ndc.harvester.model.exception.InvalidModelException;
import it.gov.innovazione.ndc.harvester.model.index.Distribution;
import it.gov.innovazione.ndc.harvester.model.index.SemanticAssetMetadata;
import it.gov.innovazione.ndc.model.profiles.NDC;
import it.gov.innovazione.ndc.validator.model.ErrorValidatorMessage;
import it.gov.innovazione.ndc.validator.model.WarningValidatorMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ControlledVocabularyModel extends BaseSemanticAssetModel {
    public static final String NDC_ENDPOINT_URL_TEMPLATE = "%s/vocabularies/%s/%s";
    public static final String KEY_CONCEPT_VALIDATION_PATTERN = "^\\w(:?[\\w-]+\\w)*$";

    private String endpointUrl = "";

    public ControlledVocabularyModel(Model coreModel, String source, String repoUrl) {
        super(coreModel, source, repoUrl);
    }

    public String getKeyConcept() {
        Resource mainResource = getMainResource();
        StmtIterator stmtIterator = mainResource.listProperties(NDC.keyConcept);
        String keyConcept;
        try {
            if (!stmtIterator.hasNext()) {
                log.warn("No key concept ({}) statement for controlled vocabulary '{}'",
                        NDC.keyConcept, mainResource);
                throw new InvalidModelException(
                        "No key concept property for controlled vocabulary " + mainResource);
            }

            Statement statement = stmtIterator.nextStatement();
            if (stmtIterator.hasNext()) {
                log.warn("Multiple key concept ({}) statements for controlled vocabulary '{}'",
                        NDC.keyConcept, mainResource);
                throw new InvalidModelException(
                        "Multiple key concept properties for controlled vocabulary " + mainResource);
            }

            keyConcept = statement.getObject().toString();
        } finally {
            stmtIterator.close();
        }

        validateKeyConcept(keyConcept, mainResource);
        return keyConcept;
    }

    private void validateKeyConcept(String keyConcept, Resource mainResource) {
        if (!keyConcept.matches(KEY_CONCEPT_VALIDATION_PATTERN)) {
            log.warn("Key concept string ({}) invalid for controlled vocabulary '{}'",
                    keyConcept, mainResource);
            throw new InvalidModelException(format("Key concept '%s' value does not meet expected pattern", keyConcept));
        }
    }

    public String getAgencyId() {
        Statement rightsHolder;
        try {
            rightsHolder = getMainResource().getRequiredProperty(DCTerms.rightsHolder);
        } catch (Exception e) {
            throw new InvalidModelException(format("Cannot find required rightsHolder property (%s)", DCTerms.rightsHolder));
        }
        Statement idProperty;
        try {
            idProperty = rightsHolder.getProperty(DCTerms.identifier);
        } catch (Exception e) {
            String rightsHolderIri = rightsHolder.getObject().toString();
            throw new InvalidModelException(format("Cannot find required id (%s) for rightsHolder '%s'", DCTerms.identifier, rightsHolderIri));
        }
        return idProperty.getString();
    }

    public void addNdcDataServiceProperties(String baseUrl) {
        endpointUrl = buildEndpointUrl(baseUrl);
        Resource dataServiceNode = rdfModel.createResource(buildDataServiceIndividualUri());
        rdfModel.add(dataServiceNode, RDF.type, NDC.DataService);
        rdfModel.add(dataServiceNode, NDC.servesDataset, getMainResource());
        rdfModel.add(getMainResource(), NDC.hasDataService, dataServiceNode);
        rdfModel.add(dataServiceNode, NDC.endpointURL, endpointUrl);
    }

    private String buildDataServiceIndividualUri() {
        return format("https://w3id.org/italia/data/data-service/%s-%s", getAgencyId(), getKeyConcept());
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    private String buildEndpointUrl(String baseUrl) {
        return format(NDC_ENDPOINT_URL_TEMPLATE, baseUrl, getAgencyId(), getKeyConcept());
    }

    @Override
    protected String getMainResourceTypeIri() {
        return CONTROLLED_VOCABULARY.getTypeIri();
    }

    @Override
    public SemanticAssetMetadata extractMetadata() {
        return super.extractMetadata().toBuilder()
                .type(CONTROLLED_VOCABULARY)
                .distributions(getDistributions())
                .keyConcept(getKeyConcept())
                .agencyId(getAgencyId())
                .endpointUrl(getEndpointUrl())
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
        getKeyConcept(errors, SemanticAssetMetadata.Fields.keyConcept);
        getAgencyId(errors, SemanticAssetMetadata.Fields.agencyId);
    }

	protected List<Distribution> getDistributions() {
        return extractDistributionsFilteredByFormat(distribution, FILE_TYPE_RDF_TURTLE);
    }
	
	private List<Distribution> getDistributions(List<ErrorValidatorMessage> errors, List<WarningValidatorMessage> warnings, String fieldName) {
        return extractDistributionsFilteredByFormat(distribution, FILE_TYPE_RDF_TURTLE, errors, warnings, fieldName);
    }
	
	private String getKeyConcept(List<ErrorValidatorMessage> errors, String fieldName) {
		try {
			return getKeyConcept();
		} catch (InvalidModelException e) {
			errors.add(new ErrorValidatorMessage(fieldName, e.getMessage()));
			return null;
		}
	}
	
	private String getAgencyId(List<ErrorValidatorMessage> errors, String fieldName) {
		try {
			return getAgencyId();
		} catch (InvalidModelException e) {
			errors.add(new ErrorValidatorMessage(fieldName, e.getMessage()));
			return null;
		}
	}
}
