package com.github.sgov.server.util;

import java.nio.file.Paths;
import java.util.function.Function;


public enum VocabularyType {
    ZSGOV("základní", (String vocabularyId) -> "z-sgov"),
    VSGOV("veřejný-sektor", (String vocabularyId) -> "v-sgov"),
    GSGOV("generický",
        (String vocabularyId) -> Paths.get("g-sgov", "g-sgov-" + vocabularyId).toString()),
    LSGOV("legislativní",
        (String vocabularyId) -> Paths.get("l-sgov", "l-sgov-" + vocabularyId).toString()),
    ASGOV("agendový",
        (String vocabularyId) -> Paths.get("a-sgov", "a-sgov-" + vocabularyId).toString()),
    DSGOV("datový",
        (String vocabularyId) -> Paths.get("d-sgov", "d-sgov-" + vocabularyId).toString());

    String fragment;

    private Function<String, String> getVocabularyFolder;

    VocabularyType(String fragment, Function<String, String> getVocabularyFolder) {
        this.fragment = fragment;
        this.getVocabularyFolder = getVocabularyFolder;
    }

    public String getVocabularyFolder(String vocabularyId) {
        return getVocabularyFolder.apply(vocabularyId);
    }

    public String getFragment() {
        return fragment;
    }
}
