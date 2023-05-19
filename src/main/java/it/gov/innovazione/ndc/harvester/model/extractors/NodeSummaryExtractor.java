package it.gov.innovazione.ndc.harvester.model.extractors;

import static it.gov.innovazione.ndc.harvester.model.extractors.LiteralExtractor.extractOptional;
import static it.gov.innovazione.ndc.harvester.model.extractors.NodeExtractor.extractMaybeNodes;
import static java.lang.String.format;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import it.gov.innovazione.ndc.harvester.model.exception.InvalidModelException;
import it.gov.innovazione.ndc.harvester.model.index.NodeSummary;
import it.gov.innovazione.ndc.validator.model.ErrorValidatorMessage;
import it.gov.innovazione.ndc.validator.model.WarningValidatorMessage;

public class NodeSummaryExtractor {

    private static final String ERROR_MESSAGE = "Unable to extract node summary from resource '%s' using '%s'";

    public static NodeSummary extractRequiredNodeSummary(Resource resource, Property nodeProperty,
                                                         Property summaryProperty) {
        return maybeNodeSummaries(resource, nodeProperty, summaryProperty)
            .stream()
            .findFirst()
            .orElseThrow(() -> invalidModelException(resource, nodeProperty));
    }

    /*
     *  validation methods
     */
    public static NodeSummary extractRequiredNodeSummary(Resource resource, Property nodeProperty, Property summaryProperty,
                                                         List<ErrorValidatorMessage> errors, List<WarningValidatorMessage> warnings, String fieldName) {
        NodeSummary retVal = null;
        var optional = maybeNodeSummariesOnlySummaryMessage(resource, nodeProperty, summaryProperty, warnings, fieldName)
                .stream()
                .findFirst();

        if (optional.isPresent()) {
            retVal = optional.get();
        } else {
            errors.add(new ErrorValidatorMessage(fieldName, format(ERROR_MESSAGE, resource, nodeProperty)));
        }

        return retVal;
    }


    public static List<NodeSummary> maybeNodeSummaries(Resource resource, Property nodeProperty,
                                                       Property summaryProperty) {
        return extractMaybeNodes(resource, nodeProperty)
            .stream()
            .map(node -> NodeSummary.builder()
                .iri(node.getURI())
                .summary(extractOptional(node, summaryProperty))
                .build())
            .collect(Collectors.toList());
    }

    /*
     *  validation methods
     */
    public static List<NodeSummary> maybeNodeSummaries(Resource resource, Property nodeProperty, Property summaryProperty, List<WarningValidatorMessage> warnings, String fieldName) {
        return extractMaybeNodes(resource, nodeProperty, warnings, fieldName)
                .stream()
                .map(node -> NodeSummary.builder()
                    .iri(node.getURI())
                    .summary(extractOptional(node, summaryProperty, warnings, fieldName))
                    .build())
                .collect(Collectors.toList());
    }

    private static List<NodeSummary> maybeNodeSummariesOnlySummaryMessage(Resource resource, Property nodeProperty, Property summaryProperty, List<WarningValidatorMessage> warnings, String fieldName) {
        return extractMaybeNodes(resource, nodeProperty)
                .stream()
                .map(node -> NodeSummary.builder()
                    .iri(node.getURI())
                    .summary(extractOptional(node, summaryProperty, warnings, fieldName))
                    .build())
                .collect(Collectors.toList());
    }

    public static InvalidModelException invalidModelException(Resource resource,
                                                              Property property) {
        return new InvalidModelException(
            format(ERROR_MESSAGE, resource, property));
    }
}
