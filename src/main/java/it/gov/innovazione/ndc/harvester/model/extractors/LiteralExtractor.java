package it.gov.innovazione.ndc.harvester.model.extractors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.Predicate;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import it.gov.innovazione.ndc.harvester.model.exception.InvalidModelException;
import it.gov.innovazione.ndc.validator.model.ErrorValidatorMessage;
import it.gov.innovazione.ndc.validator.model.WarningValidatorMessage;

public class LiteralExtractor {

	private static final String ERROR_MESSAGE="Cannot find property '%s' for resource '%s'";
	
    public static String extractOptional(Resource resource, Property property) {
        try {
            return extract(resource, property);
        } catch (InvalidModelException e) {
            return null;
        }
    }

    public static String extract(Resource resource, Property property) {
        List<Statement> properties = resource.listProperties(property).toList();
        return properties.stream()
            .filter(s -> s.getObject().isLiteral())
            .filter(filterItalianOrEnglishOrDefault())
            .max((o1, o2) -> o1.getLanguage().compareToIgnoreCase(o2.getLanguage()))
            .map(Statement::getString)
            .orElseThrow(() -> new InvalidModelException(
                format(ERROR_MESSAGE, property, resource)));
    }

    public static List<String> extractAll(Resource resource, Property property) {
        return resource.listProperties(property).toList().stream()
            .map(Statement::getString)
            .collect(toList());
    }

    private static Predicate<Statement> filterItalianOrEnglishOrDefault() {
        return s -> s.getLanguage().equalsIgnoreCase("it")
            || s.getLanguage().equalsIgnoreCase("en")
            || s.getLanguage().isEmpty();
    }
    
    /*
     * validation methods
     */
   
    public static String extractOptional(Resource resource, Property property, List<WarningValidatorMessage> warnings, String fieldName) {
        try {
            return extract(resource, property);
        } catch (InvalidModelException e) {
        	warnings.add(new WarningValidatorMessage(fieldName, e.getMessage()));
            return null;
        }
    }
    
    public static String extract(Resource resource, Property property, List<ErrorValidatorMessage> errors, String fieldName) {
        List<Statement> properties = resource.listProperties(property).toList();
        String retVal = null;
        
        var check = properties.stream()
            .filter(s -> s.getObject().isLiteral())
            .filter(filterItalianOrEnglishOrDefault())
            .max((o1, o2) -> o1.getLanguage().compareToIgnoreCase(o2.getLanguage()))
            .map(Statement::getString);
        
        if(check.isPresent()) {
        	retVal=check.get();
        } else {
        	errors.add(new ErrorValidatorMessage(fieldName, format(ERROR_MESSAGE, property, resource)));
        }
        
        return retVal;
    }
    
    public static List<String> extractAll(Resource resource, Property property, List<WarningValidatorMessage> warnings, String fieldName) {
        var retVal = extractAll(resource, property);
        if(retVal.isEmpty()) {
        	warnings.add(new WarningValidatorMessage(fieldName, format(ERROR_MESSAGE, property, resource)));
        }
        return retVal;
    }

}
