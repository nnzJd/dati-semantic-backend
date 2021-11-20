package it.teamdigitale.ndc.harvester;

import static java.lang.String.format;
import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

import java.util.List;

import it.teamdigitale.ndc.harvester.exception.InvalidAssetException;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Component;

@Component
public class SemanticAssetsParser {

    public static final String DATASET_IRI = "http://dati.gov.it/onto/dcatapit#Dataset";

    public Resource getControlledVocabulary(String ttlFile) {
        List<Resource> resources =
                RDFDataMgr.loadModel(ttlFile, Lang.TURTLE)
                        .listResourcesWithProperty(RDF.type, createResource(DATASET_IRI))
                        .toList();

        checkFileDeclaresSingleControlledVocabulary(ttlFile, resources);
        return resources.get(0);
    }

    private void checkFileDeclaresSingleControlledVocabulary(
            String ttlFile, List<Resource> resources) {
        if (resources.size() == 1) {
            return;
        }

        if (resources.isEmpty()) {
            throw new InvalidAssetException(
                    format("No statement for a node whose type is '%s' in '%s'", DATASET_IRI, ttlFile));
        }
        throw new InvalidAssetException(
                format(
                        "Found %d statements for nodes whose type is '%s' in '%s', expecting only 1",
                        resources.size(), DATASET_IRI, ttlFile));
    }

    public String getKeyConcept(Resource controlledVocabulary) {
        return controlledVocabulary
                .getRequiredProperty(createProperty("https://w3id.org/italia/onto/NDC/keyConcept"))
                .getString();
    }

    public String getRightsHolderId(Resource controlledVocabulary) {
        return controlledVocabulary
                .getRequiredProperty(DCTerms.rightsHolder)
                .getProperty(DCTerms.identifier)
                .getString();
    }
}
