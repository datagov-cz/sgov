package com.github.sgov.server.util;

import static com.github.sgov.server.util.Vocabulary.SLOVNIK_GOV_CZ;

import java.util.Arrays;
import java.util.regex.Pattern;


public enum VocabularyType {
    ZSGOV("základní", "z-sgov",
        ""),
    VSGOV("veřejný-sektor", "v-sgov",
        ""),
    GSGOV("generický", "g-sgov",
        "/([-ěščřžýáíéóúůďťňaa-z0-9]+)"),
    LSGOV("legislativní", "l-sgov",
        "/sbírka/([0-9]+/[0-9]+)"),
    ASGOV("agendový", "a-sgov",
        "/([-a-z0-9]+)"),
    DSGOV("datový", "d-sgov",
        "/([-ěščřžýáíéóúůďťňa-z0-9]+)");

    private final String iriLocalName;
    private final String prefix;
    private final String idRegex;

    VocabularyType(String iriLocalName, String prefix, String idRegex) {
        this.iriLocalName = iriLocalName;
        this.prefix = prefix;
        this.idRegex = idRegex;
    }

    public static VocabularyType getType(String iri) {
        return Arrays.stream(values())
            .filter(vt -> vt.getRegex().matcher(iri).matches()).findAny().orElse(null);
    }

    public String getIriLocalName() {
        return iriLocalName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getVocabularyPattern() {
        return SLOVNIK_GOV_CZ + "/" + getIriLocalName()
            + (idRegex.isEmpty() ? "" : ("(" + idRegex + ")?"));
    }

    public Pattern getRegex() {
        return Pattern.compile("^" + getVocabularyPattern() + "$");
    }
}
