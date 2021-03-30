package com.github.sgov.server.util;

/**
 * Vocabulary for the SGoV server model.
 */
@SuppressWarnings("checkstyle:ConstantName")
public final class Vocabulary {

    public static final String ONTOGRAPHER_NAMESPACE = "http://onto.fel.cvut.cz/ontologies/application/ontoGrapher/";
    public static final String TERMIT_NAMESPACE = "http://onto.fel.cvut.cz/ontologies/application/termit/";

    public static final String SLOVNIK_GOV_CZ = "https://slovník.gov.cz";
    public static final String WORKSPACE_NAMESPACE = "https://slovník.gov.cz/datový/pracovní-prostor/pojem/";
    public static final String DATA_DESCRIPTION_NAMESPACE = "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/";

    public static final String s_c_metadatovy_kontext = WORKSPACE_NAMESPACE + "metadatový-kontext";
    public static final String s_c_slovnikovy_kontext = WORKSPACE_NAMESPACE + "slovníkový-kontext";
    public static final String s_c_slovnikovy_kontext_pouze_pro_cteni =
            WORKSPACE_NAMESPACE + "slovníkový-kontext-pouze-pro-čtení";
    public static final String s_c_kontext_sledovani_zmen = WORKSPACE_NAMESPACE
            + "kontext-sledování-změn";

    public static final String s_p_odkazuje_na_kontext = WORKSPACE_NAMESPACE
            + "odkazuje-na-kontext";
    public static final String s_p_vychazi_z_verze = WORKSPACE_NAMESPACE + "vychází-z-verze";
    public static final String s_p_ma_pracovni_metadatovy_kontext =
            WORKSPACE_NAMESPACE + "má-pracovní-metadatový-kontext";
    public static final String s_p_obsahuje_slovnik = WORKSPACE_NAMESPACE
            + "obsahuje-slovník";
    public static final String s_p_meni_verzi = WORKSPACE_NAMESPACE + "mění-verzi";
    public static final String s_p_je_technickym_kontextem_slovniku =
            WORKSPACE_NAMESPACE + "je-technickým-kontextem-slovníku";
    public static final String s_p_ma_kontext_sledovani_zmen = WORKSPACE_NAMESPACE
            + "má-kontext-sledování-změn";

    public static final String s_c_uzivatel = DATA_DESCRIPTION_NAMESPACE
            + "uživatel";
    public static final String s_c_administrator = WORKSPACE_NAMESPACE
        + "administrátor";
    public static final String s_c_omezeny_uzivatel = WORKSPACE_NAMESPACE
        + "omezený-uživatel";
    public static final String s_c_uzamceny_uzivatel = WORKSPACE_NAMESPACE
        + "uzamčený-uživatel";
    public static final String s_c_zablokovany_uzivatel = WORKSPACE_NAMESPACE
        + "zablokovaný-uživatel";
    public static final String s_p_ma_puvodni_heslo = WORKSPACE_NAMESPACE
        + "má-původní-heslo";
    public static final String s_p_pouziva_pojmy_ze_slovniku = TERMIT_NAMESPACE
        + "pojem/používá-pojmy-ze-slovníku";
    public static final String s_p_ma_heslo = DATA_DESCRIPTION_NAMESPACE
            + "má-heslo";
    public static final String s_p_ma_krestni_jmeno = DATA_DESCRIPTION_NAMESPACE
            + "má-křestní-jméno";
    public static final String s_p_ma_prijmeni = DATA_DESCRIPTION_NAMESPACE
            + "má-příjmení";
    public static final String s_p_ma_uzivatelske_jmeno = DATA_DESCRIPTION_NAMESPACE
            + "má-uživatelské-jméno";

    public static final String s_p_ma_datum_a_cas_posledni_modifikace =
            DATA_DESCRIPTION_NAMESPACE + "má-datum-a-čas-poslední-modifikace";
    public static final String s_p_ma_posledniho_editora = DATA_DESCRIPTION_NAMESPACE
            + "má-posledního-editora";
    public static final String s_p_ma_datum_a_cas_vytvoreni = DATA_DESCRIPTION_NAMESPACE
            + "má-datum-a-čas-vytvoření";
    public static final String s_p_ma_autora = DATA_DESCRIPTION_NAMESPACE + "má-autora";

    public static final String s_c_slovnik = DATA_DESCRIPTION_NAMESPACE + "slovník";
    public static final String s_p_ma_glosar = DATA_DESCRIPTION_NAMESPACE + "má-glosář";
    public static final String s_p_ma_model = DATA_DESCRIPTION_NAMESPACE + "má-model";
    public static final String s_c_glosar = DATA_DESCRIPTION_NAMESPACE + "glosář";
    public static final String s_c_model = DATA_DESCRIPTION_NAMESPACE + "model";

    private Vocabulary() {
    }
}
