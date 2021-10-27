package com.github.sgov.server.util;

import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class UtilsTest {

    public static Stream<Arguments> provideInvalidVocabularyIris() {
        return Stream.of(
            Arguments.of("https://slovník.gov.cz/základní/"),
            Arguments.of("https://slovník.gov.cz/základníx"),
            Arguments.of("https://slovníkx.gov.cz/základní"),
            Arguments.of("http://slovník.gov.cz/základní")
        );
    }

    public static Stream<Arguments> provideValidVocabularyIris() {
        return Stream.of(
            Arguments.of("z-sgov", "https://slovník.gov.cz/základní"),
            Arguments.of("v-sgov", "https://slovník.gov.cz/veřejný-sektor"),
            Arguments.of("g-sgov-a", "https://slovník.gov.cz/generický/a"),
            Arguments.of("l-sgov-sbírka-1-2",
                "https://slovník.gov.cz/legislativní/sbírka/1/2"),
            Arguments.of("a-sgov-a", "https://slovník.gov.cz/agendový/a"),
            Arguments.of("d-sgov-a", "https://slovník.gov.cz/datový/a")
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidVocabularyIris")
    public void allVocabularyTypesResolveVocabularyIdentifiersCorrectly(final String expectedFolder,
                                                                        final String vocabularyIri) {
        Assertions.assertEquals(expectedFolder, Utils.getVocabularyId(vocabularyIri));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidVocabularyIris")
    public void invalidVocabularyIrisResultInNullVocabularyId(String vocabularyIri) {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> Utils.getVocabularyId(vocabularyIri));
    }
}
