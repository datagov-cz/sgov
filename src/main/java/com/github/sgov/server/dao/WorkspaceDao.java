package com.github.sgov.server.dao;

import com.github.sgov.server.ValidationResultSeverityComparator;
import com.github.sgov.server.Validator;
import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.exception.PersistenceException;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.model.util.DescriptorFactory;
import com.github.sgov.server.util.Vocabulary;
import com.google.gson.JsonObject;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.ontodriver.Connection;
import cz.cvut.kbss.ontodriver.exception.OntoDriverException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.topbraid.shacl.validation.ValidationReport;
import org.topbraid.shacl.validation.ValidationResult;

/**
 * DAO for accessing workspace.
 */
@Slf4j
@Repository
public class WorkspaceDao extends BaseDao<Workspace> {

    private final RepositoryConf properties;

    private final DescriptorFactory descriptorFactory;

    /**
     * Constructor.
     */
    @Autowired
    public WorkspaceDao(EntityManager em, DescriptorFactory descriptorFactory,
                        RepositoryConf properties) {
        super(Workspace.class, em);
        this.properties = properties;
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public Optional<Workspace> find(URI id) {
        Objects.requireNonNull(id);
        try {
            return Optional.ofNullable(
                em.find(type, id, descriptorFactory.workspaceDescriptor(id))
            );
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Optional<Workspace> getReference(URI id) {
        Objects.requireNonNull(id);
        try {
            return Optional.ofNullable(
                em.getReference(type, id, descriptorFactory.workspaceDescriptor(id))
            );
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    //@ModifiesData
    @Override
    public Workspace update(Workspace entity) {
        Objects.requireNonNull(entity);
        try {
            // Evict possibly cached instance loaded from default context
            em.getEntityManagerFactory().getCache().evict(Workspace.class, entity.getUri(), null);
            return em.merge(entity, descriptorFactory.workspaceDescriptor(entity));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    //@ModifiesData
    @Override
    public void persist(Workspace entity) {
        Objects.requireNonNull(entity);
        try {
            Connection connection = em.unwrap(Connection.class);
            URI entityUri = connection.generateIdentifier(
                em.getMetamodel().entity(Workspace.class).getIRI().toURI()
            );
            entity.setUri(entityUri);
            em.persist(entity, descriptorFactory.workspaceDescriptor(entity));
        } catch (RuntimeException | OntoDriverException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Validates workspace.
     *
     * @param workspace workspace to be validated
     * @return ValidationReport
     */
    public ValidationReport validateWorkspace(final Workspace workspace) throws IOException {
        log.info("Validating workspace {}", workspace.getUri());
        ValidationReport validationReport = validateVocabularies(workspace.getVocabularyContexts());
        log.info("- done.");
        return validationReport;
    }

    /**
     * Validates set of vocabulary contexts.
     *
     * @param vocabularyContexts set of vocabulary contexts to be validated
     * @return ValidationReport
     */
    public ValidationReport validateVocabularies(
        Set<VocabularyContext> vocabularyContexts) throws IOException {
        final Validator validator = new Validator();
        final Set<URL> rules = new HashSet<>();
        rules.addAll(validator.getGlossaryRules());
        rules.addAll(validator.getModelRules().stream()
            .filter(r -> !r.getPath().contains("m2.ttl"))
            .filter(r -> !r.getPath().contains("m3.ttl"))
            .filter(r -> !r.getPath().contains("m4.ttl"))
            .filter(r -> !r.getPath().contains("m5.ttl"))
            .filter(r -> !r.getPath().contains("m6.ttl"))
            .filter(r -> !r.getPath().contains("m7.ttl"))
            .collect(Collectors.toSet())
        );
        rules.addAll(validator.getVocabularyRules());

        boolean conforms = true;

        OntDocumentManager.getInstance().setProcessImports(false);

        final String endpoint = properties.getUrl();


        final List<ValidationResult> validationResults = new ArrayList<>();
        for (VocabularyContext c : vocabularyContexts) {
            final ValidationReport report = validateVocabulary(c.getUri().toString(),
                endpoint, validator, rules);
            conforms = conforms && report.conforms();
            validationResults.addAll(report.results());
        }
        validationResults.sort(new ValidationResultSeverityComparator());
        boolean finalConforms = conforms;

        return new ValidationReport() {
            @Override
            public boolean conforms() {
                return finalConforms;
            }

            @Override
            public List<ValidationResult> results() {
                return validationResults;
            }
        };
    }

    /**
     * Sets labels of vocabularyContexts retrieved from actual labels of vocabularies.
     *
     * @param vocabularyContexts Vocabulary context that should be extended with labels.
     * @param language           Language for which labels should be retrieved.
     */
    public void setVocabularyLabels(List<VocabularyContext> vocabularyContexts, String language) {

        if (vocabularyContexts.isEmpty()) {
            return;
        }

        String values = vocabularyContexts.stream()
            .map(vc -> vc.getUri().toString())
            .collect(Collectors.joining(">)\n  (<", "  (<", ">)\n"));

        final String uri = properties.getUrl();
        final HttpResponse<JsonObject> response =
            Unirest.post(uri).header("Content-type", "application/sparql-query")
                .header("Accept", "application/sparql-results+json")
                .body("SELECT ?vc ?label\n"
                    + "WHERE {\n"
                    + "    GRAPH ?vc { \n"
                    + "        ?s a <" + Vocabulary.s_c_slovnik + "> .\n"
                    + "        ?s <" + DCTERMS.TITLE + "> ?label .\n"
                    + "        FILTER langMatches( lang(?label), \"" + language + "\" )\n"
                    + "    }    \n"
                    + "} VALUES (?vc) {\n"
                    + values
                    + "}")
                .asObject(JsonObject.class);

        final Map<URI, String> uri2Labels = new HashMap<>();
        response.getBody().getAsJsonObject("results").getAsJsonArray("bindings")
            .forEach(b -> {
                URI vc = URI.create(b.getAsJsonObject()
                    .getAsJsonObject("vc").getAsJsonPrimitive("value").getAsString()
                );
                String label = b.getAsJsonObject()
                    .getAsJsonObject("label").getAsJsonPrimitive("value").getAsString();

                String previousValue = uri2Labels.putIfAbsent(vc, label);
                if (previousValue != null) {
                    log.warn("Found multiple labels of a vocabulary "
                        + "within vocabulary context {}. Ignoring label {}.", vc, label);
                }
            });
        vocabularyContexts.forEach(
            vc -> vc.setLabel(uri2Labels.get(vc.getUri()))
        );
    }

    /**
     * Clears the given vocabulary context.
     *
     * @param vocabularyContext vocabularyContext
     */
    public void clearVocabularyContext(final URI vocabularyContext) {
        try {
            em
                .createNativeQuery(
                    "DELETE { GRAPH ?g { ?s ?p ?o } } WHERE { GRAPH ?g { ?s ?p ?o } . }",
                    type)
                .setParameter("g", vocabularyContext)
                .executeUpdate();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Clears the given vocabulary context.
     *
     * @param workspace workspace
     */
    public List<URI> getDependentsForVocabularyInWorkspace(final Workspace workspace,
                                                           final URI vocabularyUri) {
        try {
            return em
                .createNativeQuery(
                    "SELECT ?o WHERE { ?s ?p ?o } ",
                    URI.class)
                .setParameter("p", URI.create(Vocabulary.s_p_pouziva_pojmy_ze_slovniku))
                .setParameter("s", vocabularyUri)
                .getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    public void flush() {
        em.getEntityManagerFactory().getCache().evictAll();
    }


    private ValidationReport validateVocabulary(final String v,
                                                final String endpoint,
                                                final Validator validator,
                                                final Set<URL> rules)
        throws IOException {
        final String bindings = "<" + v + ">";
        final ParameterizedSparqlString query = new ParameterizedSparqlString(
            "CONSTRUCT {?s ?p ?o} WHERE  {GRAPH ?g {?s ?p ?o}} VALUES ?g {" + bindings + "}");
        log.debug("- getting all statements for the vocabularies using query {}", query);
        final QueryExecution e = QueryExecutionFactory
            .sparqlService(endpoint, query.asQuery());
        final Model dataModel =
            ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
        e.execConstruct(dataModel);
        log.debug("- done, now validating");
        return validator.validate(dataModel, rules);
    }
}
