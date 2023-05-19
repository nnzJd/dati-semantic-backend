package it.gov.innovazione.ndc.validator;

import it.gov.innovazione.ndc.validator.model.ErrorValidatorMessage;
import it.gov.innovazione.ndc.validator.model.WarningValidatorMessage;
import lombok.Data;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ValidationResultDto {

    private List<ErrorValidatorMessage> errors;

    private List<WarningValidatorMessage> warnings;

    public ValidationResultDto(List<ErrorValidatorMessage> errors, List<WarningValidatorMessage> warnings) {
        this.errors = errors.stream()
                .sorted(Comparator.comparing(ErrorValidatorMessage::getFieldName))
                .collect(Collectors.toList());
        this.warnings = warnings.stream()
                .sorted(Comparator.comparing(WarningValidatorMessage::getFieldName))
                .collect(Collectors.toList());;
    }
}
