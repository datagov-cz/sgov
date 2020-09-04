package com.github.sgov.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.progress.SimpleProgressMonitor;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.rules.RuleUtil;
import org.topbraid.shacl.validation.ResourceValidationReport;
import org.topbraid.shacl.validation.ValidationReport;
import org.topbraid.shacl.validation.ValidationUtil;

@Slf4j
public class Validator {

    private final Set<File> glossaryRules = new HashSet<>();
    private final Set<File> modelRules = new HashSet<>();
    private final Set<File> vocabularyRules = new HashSet<>();

    /**
     * Validator constructor.
     */
    public Validator() {
        final URL url = getClass().getClassLoader().getResource("./rules");
        for (final File f : Objects.requireNonNull(new File(url.getPath()).listFiles(f ->
            f.getName().matches("([gsm])[0-9]+.ttl")))) {
            final String name = f.getName();
            if (name.startsWith("g")) {
                glossaryRules.add(f);
            } else if (name.startsWith("m")) {
                glossaryRules.add(f);
            } else {
                vocabularyRules.add(f);
            }
        }
    }

    public Set<File> getModelRules() {
        return modelRules;
    }

    public Set<File> getGlossaryRules() {
        return glossaryRules;
    }

    public Set<File> getVocabularyRules() {
        return vocabularyRules;
    }

    private Model getRulesModel(final Collection<File> rules) {
        final Model shapesModel = JenaUtil.createMemoryModel();
        try {
            for (File r : rules) {
                shapesModel
                    .read(new FileReader(r), null, FileUtils.langTurtle);
            }
        } catch (FileNotFoundException e) {
            log.error("An error occurred during rule model construction.", e);
        }
        return shapesModel;
    }

    /**
     * Validates the given model with vocabulary data (glossaries, models) against the given
     * ruleset.
     *
     * @param dataModel model with data to validate
     * @param ruleSet   set of rules (see 'resources') used for validation
     * @return validation report
     */
    public ValidationReport validate(final Model dataModel, final Set<File> ruleSet) {
        log.info("Validating model of size {}", dataModel.size());
        final Model shapesModel = getRulesModel(ruleSet);

        shapesModel.read(Validator.class.getResourceAsStream("/inference-rules.ttl"), null,
            FileUtils.langTurtle);

        final Model inferredModel = RuleUtil
            .executeRules(dataModel, shapesModel, null, new SimpleProgressMonitor("inference"));
        dataModel.add(inferredModel);

        final Resource report = ValidationUtil.validateModel(dataModel, shapesModel, true);

        return new ResourceValidationReport(report);
    }
}
