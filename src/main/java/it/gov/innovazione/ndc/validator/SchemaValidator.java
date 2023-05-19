package it.gov.innovazione.ndc.validator;

import it.gov.innovazione.ndc.harvester.SemanticAssetType;
import it.gov.innovazione.ndc.harvester.model.SchemaModel;
import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Component;

@Component
public class SchemaValidator extends BasicSemanticAssetValidator<SchemaModel> {

    public SchemaValidator() {
        super(SemanticAssetType.SCHEMA);
    }

    @Override
    protected SchemaModel getValidatorModel(Model rdfModel) {
        return new SchemaModel(rdfModel, null, null);
    }
}
