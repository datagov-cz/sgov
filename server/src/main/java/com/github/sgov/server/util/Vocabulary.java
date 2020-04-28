package com.github.sgov.server.util;

/**
 * Vocabulary for the SGoV server model.
 */
@SuppressWarnings("checkstyle:ConstantName")
public final class Vocabulary {

    public static final String s_c_slovnikovy_kontext =
        "https://slovník.gov.cz/datový/pracovní-prostor/pojem/" + "slovníkovy-kontext";
    public static final String s_c_odkazuje_na_kontext =
        "https://slovník.gov.cz/datový/pracovní-prostor/pojem/" + "odkazuje-na-kontext";
    public static final String s_c_metadatový_kontextn =
        "https://slovník.gov.cz/datový/pracovní-prostor/pojem/" + "metadatový-kontext";

    public static final String s_c_administrator =
        "http://onto.fel.cvut.cz/ontologies/slovnik/agendovy/popis-dat/pojem/" + "administrátor";
    public static final String s_c_uzivatel =
        "http://onto.fel.cvut.cz/ontologies/slovnik/agendovy/popis-dat/pojem/" + "uživatel";
    public static final String s_c_omezeny_uzivatel =
        "http://onto.fel.cvut.cz/ontologies/slovnik/agendovy/popis-dat/pojem/" + "omezený-uživatel";
    public static final String s_c_uzamceny_uzivatel =
        "http://onto.fel.cvut.cz/ontologies/slovnik/agendovy/popis-dat/pojem/"
            + "uzamčený-uživatel";
    public static final String s_c_zablokovany_uzivatel =
        "http://onto.fel.cvut.cz/ontologies/slovnik/agendovy/popis-dat/pojem/"
            + "zablokovaný-uživatel";
    public static final String s_p_ma_puvodni_heslo =
        "http://onto.fel.cvut.cz/ontologies/slovnik/agendovy/popis-dat/pojem/" + "má-původní-heslo";

    public static final String s_p_ma_heslo =
        "http://onto.fel.cvut.cz/ontologies/slovnik/agendovy/popis-dat/pojem/" + "má-heslo";
    public static final String s_p_ma_krestni_jmeno =
        "http://onto.fel.cvut.cz/ontologies/slovnik/agendovy/popis-dat/pojem/" + "má-křestní-jméno";
    public static final String s_p_ma_prijmeni =
        "http://onto.fel.cvut.cz/ontologies/slovnik/agendovy/popis-dat/pojem/" + "má-příjmení";
    public static final String s_p_ma_uzivatelske_jmeno =
        "http://onto.fel.cvut.cz/ontologies/slovnik/agendovy/popis-dat/pojem/"
            + "má-uživatelské-jméno";

    private Vocabulary() {
    }
}
