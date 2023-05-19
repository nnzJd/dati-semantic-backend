package it.gov.innovazione.ndc.service;

import it.gov.innovazione.ndc.controller.exception.InvalidFileException;
import it.gov.innovazione.ndc.controller.exception.InvalidSemanticAssetException;
import it.gov.innovazione.ndc.controller.exception.SemanticAssetGenericErrorException;
import it.gov.innovazione.ndc.validator.ValidationResultDto;
import it.gov.innovazione.ndc.validator.SemanticAssetValidator;
import it.gov.innovazione.ndc.validator.model.ErrorValidatorMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ValidationService {

    @Autowired
    private List<SemanticAssetValidator> semanticAssetValidators;

    private static final String FILE_CONTENT_TYPE = "text/turtle";

    public ValidationResultDto validate(MultipartFile file, String assetType) {
        String fileContentType = file.getContentType();
        if (!StringUtils.hasText(fileContentType) || !fileContentType.equalsIgnoreCase(FILE_CONTENT_TYPE)) {
            throw new InvalidFileException("Only Turtle file are supported");
        }
        SemanticAssetValidator semanticAssetValidator = semanticAssetValidators.stream()
                .filter(s -> s.getType().getDescription().equalsIgnoreCase(assetType))
                .findFirst()
                .orElseThrow(() -> new InvalidSemanticAssetException(String.format("Invalid semantic asset type: %s", assetType)));

        try (InputStream i = file.getInputStream()) {
            Model model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, i, Lang.TURTLE);

            return semanticAssetValidator.validate(model);

        } catch (IOException e) {
            log.error("Error during validation on file", e);
            throw new SemanticAssetGenericErrorException();
        } catch (RiotException e) {
            return new ValidationResultDto(Arrays.asList(new ErrorValidatorMessage(null, e.getMessage())), Collections.emptyList());
        }

    }
}
