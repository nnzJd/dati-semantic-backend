package it.gov.innovazione.ndc.validator.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorValidatorMessage {

	private String fieldName;

	private String message;

}
