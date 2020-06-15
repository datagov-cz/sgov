package com.github.sgov.server.dao;

import com.github.sgov.server.Validator;
import com.github.sgov.server.config.conf.PersistenceConf;
import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.exception.PersistenceException;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.model.util.DescriptorFactory;
import com.github.sgov.server.util.Vocabulary;
import com.google.gson.JsonObject;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.ontodriver.Connection;
import cz.cvut.kbss.ontodriver.exception.OntoDriverException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.topbraid.shacl.validation.ValidationReport;

/**
 * DAO for accessing workspace.
 */
@Slf4j
@Repository
public class WorkspaceDao extends BaseDao<Workspace> {

    private final PersistenceConf config;
    private final RepositoryConf properties;

    /**
     * Constructor.
     */
    @Autowired
    public WorkspaceDao(EntityManager em, PersistenceConf config, RepositoryConf properties) {
        super(Workspace.class, em);
        this.config = config;
        this.properties = properties;
    }

    @Override
    public Optional<Workspace> find(URI id) {
        Objects.requireNonNull(id);
        try {
            return Optional.ofNullable(
                em.find(type, id, DescriptorFactory.workspaceDescriptor(id))
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
                em.getReference(type, id, DescriptorFactory.workspaceDescriptor(id))
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
            return em.merge(entity, DescriptorFactory.workspaceDescriptor(entity));
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
            em.persist(entity, DescriptorFactory.workspaceDescriptor(entity));
        } catch (RuntimeException | OntoDriverException e) {
            throw new PersistenceException(e);
        }
    }


    /**
     * Returns all workspace IRIs.
     *
     * @return list of workspace IRIs.
     */
    public List<String> getAllWorkspaceIris() {
        final String uri = properties.getUrl();
        final HttpResponse<JsonObject> response =
                Unirest.post(uri).header("Content-type", "application/sparql-query")
                        .header("Accept", "application/sparql-results+json")
                        .body("SELECT ?iri WHERE { ?iri  a <" + Vocabulary.s_c_metadatovy_kontext
                                + "> }")
                        .asObject(JsonObject.class);

        final List<String> list = new ArrayList<>();
        response.getBody().getAsJsonObject("results").getAsJsonArray("bindings")
                .forEach(b -> list.add(
                        ((JsonObject) b).getAsJsonObject("iri").getAsJsonPrimitive("value")
                                .getAsString()));
        return list;
    }

    private List<String> getVocabularySnapshotContextsForWorkspace(final String workspace) {
        final String endpointUlozistePracovnichProstoru = properties.getUrl();
        final QuerySolutionMap map = new QuerySolutionMap();
        map.add("workspace", ResourceFactory.createResource(workspace));
        map.add("odkazujeNaKontext",
                ResourceFactory.createResource(Vocabulary.s_p_odkazuje_na_kontext));
        map.add("slovnikovyKontext",
                ResourceFactory.createResource(Vocabulary.s_c_slovnikovy_kontext));
        final ParameterizedSparqlString query = new ParameterizedSparqlString(
                "SELECT ?kontext WHERE { ?workspace ?odkazujeNaKontext ?kontext . ?kontext a "
                        + "?slovnikovyKontext }", map);
        final ResultSet rs = QueryExecutionFactory
                        .sparqlService(endpointUlozistePracovnichProstoru, query.asQuery())
                        .execSelect();

        final List<String> list = new ArrayList<>();
        while (rs.hasNext()) {
            list.add(rs.nextSolution().getResource("kontext").getURI());
        }
        return list;
    }

    /**
     * Validates workspace.
     *
     * @param workspaceIri workspace IRI
     * @return ValidationReport
     */
    public ValidationReport validateWorkspace(final String workspaceIri) {
        log.info("Validating workspace {}", workspaceIri);
        final String endpointUlozistePracovnichProstoru = properties.getUrl();
        final List<String> vocabulariesForWorkspace =
                getVocabularySnapshotContextsForWorkspace(workspaceIri);
        log.debug("- found vocabularies {}", vocabulariesForWorkspace);
        final String bindings = vocabulariesForWorkspace.stream().map(v -> "<" + v + ">")
                .collect(Collectors.joining(" "));
        final ParameterizedSparqlString query = new ParameterizedSparqlString(
                "CONSTRUCT {?s ?p ?o} WHERE  {GRAPH ?g {?s ?p ?o}} VALUES ?g {" + bindings + "}");
        log.debug("- getting all statements for the vocabularies using query {}", query.toString());
        final QueryExecution e = QueryExecutionFactory
                .sparqlService(endpointUlozistePracovnichProstoru, query.asQuery());
        final Model m = e.execConstruct();
        log.info("- found {} statements. Now validating", m.listStatements().toSet().size());
        final Validator validator = new Validator();
        OntDocumentManager.getInstance().setProcessImports(false);
        final Model dataModel =
                ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, m);
        final Set<String> rules = new HashSet<>();
        rules.addAll(Validator.getGlossaryRules());
        rules.addAll(Validator.getModelRules());
        rules.addAll(Validator.getVocabularyRules());
        final ValidationReport r = validator.validate(dataModel, rules);
        log.info("- done.");
        log.debug("- validation results:");
        r.results().forEach(result -> {
            if (log.isDebugEnabled()) {
                log.debug(MessageFormat
                    .format("    - [{0}] Node {1} failing for value {2} with message: {3} ",
                        result.getSeverity().getLocalName(), result.getFocusNode(),
                        result.getValue(),
                        result.getMessage()));
            }
        });
        return r;
    }
}
