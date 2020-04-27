package com.github.sgov.server.model.util;

/**
 * Vocabulary for workspace management.
 */
public final class WorkspaceVocabulary {
  public static final String NS = "https://slovník.gov.cz/datový/pracovní-prostor/pojem/";
  public static final String C_PRACOVNI_PROSTOR = NS + "metadatový-kontext";
  public static final String C_ODKAZUJE_NA_KONTEXT = NS + "odkazuje-na-kontext";
  public static final String C_SLOVNIKOVY_KONTEXT = NS + "slovníkovy-kontext";

  private WorkspaceVocabulary() {
  }
}
