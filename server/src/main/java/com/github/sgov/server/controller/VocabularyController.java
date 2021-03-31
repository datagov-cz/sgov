package com.github.sgov.server.controller;

import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.service.repository.VocabularyService;
import com.github.sgov.server.util.Constants;
import cz.cvut.kbss.jsonld.JsonLd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vocabularies")
@Api(tags = "Vocabulary")
@SuppressWarnings("checkstyle:MissingJavadocType")
public class VocabularyController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(VocabularyController.class);

    private final VocabularyService vocabularyService;

    @Autowired
    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    /**
     * Retrieves all vocabularies.
     *
     * @param headers Request headers to fetch the Accept-language header
     * @return a list of vocabulary contexts
     */
    @GetMapping(produces = {
        MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    @ApiOperation(value = "Retrieve all vocabularies.")
    public List<VocabularyContext> findAll(@RequestHeader
                                                   Map<String, String> headers) {
        final String lang;
        if (headers.containsKey("Accept-Language")) {
            lang = headers.get("Accept-Language");
        } else {
            lang = "cs";
        }
        return vocabularyService.getVocabulariesAsContextDtos(lang);
    }

    /**
     * Retrieve existing workspace.
     *
     * @param vocabularyFragment Localname of vocabulary id.
     * @param namespace         Namespace used for resource identifier resolution. Optional, if not
     *                          specified, the configured namespace is used.
     * @return Workspace specified by workspaceFragment and optionally namespace.
     */
    @GetMapping(value = "/{vocabularyFragment}/imports",
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            JsonLd.MEDIA_TYPE})
    @ApiOperation(value = "Get vocabulary imports.")
    @Deprecated
    public Set<URI> getVocabularyImports(
        @PathVariable String vocabularyFragment,
        @RequestParam(name = Constants.QueryParams.NAMESPACE) String namespace) {
        final URI identifier = resolveIdentifier(
            namespace, vocabularyFragment, null);
        return vocabularyService.getTransitiveImports(identifier);
    }
}
