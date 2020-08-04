package com.github.sgov.server.controller;

import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.service.IdentifierResolver;
import com.github.sgov.server.service.repository.VocabularyService;
import cz.cvut.kbss.jsonld.JsonLd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vocabularies")
@Api(tags = "Vocabulary")
@SuppressWarnings("checkstyle:MissingJavadocType")
public class VocabularyController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(VocabularyController.class);

    private final VocabularyService vocabularyService;

    @Autowired
    public VocabularyController(VocabularyService vocabularyService,
                                IdentifierResolver idResolver) {
        super(idResolver);
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
        return vocabularyService.findAll(lang);
    }
}
