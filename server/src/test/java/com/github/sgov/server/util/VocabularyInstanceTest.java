package com.github.sgov.server.util;

import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class VocabularyInstanceTest {

    @ParameterizedTest
    @MethodSource("provideValidVocabularyIris")
    public void allVocabularyTypesResolveVocabularyFoldersCorrectly(final String expectedFolder,
                                                                    final String vocabularyIri) {
        Assertions.assertEquals(expectedFolder, new VocabularyInstance(vocabularyIri).getFolder());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidVocabularyIris")
    public void invalidVocabularyIrisResultInNullFolderfinal(String vocabularyIri) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new VocabularyInstance(vocabularyIri).getFolder());
    }

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
            Arguments.of("content/z-sgov", "https://slovník.gov.cz/základní"),
            Arguments.of("content/v-sgov", "https://slovník.gov.cz/veřejný-sektor"),
            Arguments.of("content/g-sgov/g-sgov-a", "https://slovník.gov.cz/generický/a"),
            Arguments.of("content/l-sgov/l-sgov-sbírka-1-2", "https://slovník.gov.cz/legislativní/sbírka/1/2"),
            Arguments.of("content/a-sgov/a-sgov-a", "https://slovník.gov.cz/agendový/a"),
            Arguments.of("content/d-sgov/d-sgov-a", "https://slovník.gov.cz/datový/a")
        );
    }
}
