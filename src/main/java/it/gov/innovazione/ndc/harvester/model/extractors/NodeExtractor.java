package it.gov.innovazione.ndc.harvester.model.extractors;

import static java.lang.String.format;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceRequiredException;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.PropertyNotFoundException;

import it.gov.innovazione.ndc.harvester.model.exception.InvalidModelException;
import it.gov.innovazione.ndc.validator.model.ErrorValidatorMessage;
import it.gov.innovazione.ndc.validator.model.WarningValidatorMessage;

public class NodeExtractor {

    public static Resource requireNode(Resource resource, Property property) {
        return extractMaybeNode(resource, property)
            .orElseThrow(() -> invalidModelException(resource, property));
    }

    public static Resource extractNode(Resource resource, Property property) {
        return extractMaybeNode(resource, property).orElse(null);
    }

    public static List<Resource> requireNodes(Resource resource, Property property) {
        try {
            List<Resource> resources = resource.listProperties(property).toList().stream()
                .map(Statement::getResource)
                .collect(Collectors.toList());
            if (resources.isEmpty()) {
                throw invalidModelException(resource, property);
            }
            return resources;
        } catch (PropertyNotFoundException | ResourceRequiredException e) {
            throw invalidModelException(resource, property);
        }
    }

    public static List<Resource> extractMaybeNodes(Resource resource, Property property) {
        try {
            return requireNodes(resource, property);
        } catch (InvalidModelException e) {
            return List.of();
        }
    }

    private static Optional<Resource> extractMaybeNode(Resource resource, Property property) {
        return extractMaybeNodes(resource, property)
                .stream().findFirst();
    }

    public static InvalidModelException invalidModelException(Resource resource,
                                                               Property property) {
        return new InvalidModelException(
            format("Cannot find node '%s' for resource '%s'", property, resource));
    }
    
    /*
     * validation methods
     */
    public static List<Resource> requireNodes(Resource resource, Property property,List<ErrorValidatorMessage> errors, String fieldName) {
    	 try {
             return requireNodes(resource, property);
         } catch (InvalidModelException e) {
             errors.add(new ErrorValidatorMessage(fieldName, e.getMessage()));
             return List.of();
         }
    }
    
    public static Resource requireNode(Resource resource, Property property, List<ErrorValidatorMessage> errors, String fieldName) {
    	 try {
             return requireNode(resource, property);
         } catch (InvalidModelException e) {
             errors.add(new ErrorValidatorMessage(fieldName, e.getMessage()));
             return null;
         }
    }
    
    public static List<Resource> extractMaybeNodes(Resource resource, Property property, List<WarningValidatorMessage> warnings, String fieldName) {
        try {
            return requireNodes(resource, property);
        } catch (InvalidModelException e) {
        	warnings.add(new WarningValidatorMessage(fieldName, e.getMessage()));
            return List.of();
        }
    }
    
    public static Resource extractNode(Resource resource, Property property, List<WarningValidatorMessage> warnings, String fieldName) {
    	return extractMaybeNodes(resource, property,warnings, fieldName).stream().findFirst().orElse(null);
    }
    
}
