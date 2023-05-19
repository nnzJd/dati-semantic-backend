package it.gov.innovazione.ndc.validator;

import it.gov.innovazione.ndc.harvester.SemanticAssetType;
import it.gov.innovazione.ndc.harvester.model.BaseSemanticAssetModel;
import it.gov.innovazione.ndc.validator.model.ErrorValidatorMessage;
import it.gov.innovazione.ndc.validator.model.WarningValidatorMessage;
import lombok.RequiredArgsConstructor;
import org.apache.jena.rdf.model.Model;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public abstract class BasicSemanticAssetValidator<M extends BaseSemanticAssetModel> implements SemanticAssetValidator {

	private final SemanticAssetType type;

	@Override
	public SemanticAssetType getType() {
		return type;
	}

	@Override
	public ValidationResultDto validate(Model resource) {
		M model = getValidatorModel(resource);

		List<ErrorValidatorMessage> errors = new ArrayList<>();
		List<WarningValidatorMessage> warnings = new ArrayList<>();

		model.validateMetadata(errors, warnings);

		return new ValidationResultDto(errors,warnings);
	}

	protected abstract M getValidatorModel(Model rdfModel);

}
