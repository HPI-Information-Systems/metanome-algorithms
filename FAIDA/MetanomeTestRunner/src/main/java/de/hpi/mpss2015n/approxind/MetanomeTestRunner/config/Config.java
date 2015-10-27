package de.hpi.mpss2015n.approxind.MetanomeTestRunner.config;

import java.io.File;

public class Config {

  public enum Algorithm {
    IND
  }

  public enum Dataset {
    PLANETS, TPC_H_1, TPC_H_10, TPC_H_10_partial, PLISTA, LOD, ENSEMBLE, WIKIRANK, CATH, SAP, ATOM, PDB
  }

  public Config.Algorithm algorithm = Config.Algorithm.IND;

  public String databaseName = null;
  public String[] tableNames = null;

  public String inputFolderPath = "data" + File.separator;
  public String inputFileEnding = ".csv";
  public char inputFileSeparator = ';';
  public char inputFileQuotechar = '\"';
  public char inputFileEscape = '\\';
  public int inputFileSkipLines = 0;
  public boolean inputFileStrictQuotes = false;
  public boolean inputFileIgnoreLeadingWhiteSpace = true;
  public boolean inputFileHasHeader = false; // !
  public boolean inputFileSkipDifferingLines = true; // Skip lines that differ from the dataset's
  // schema

  public String measurementsFolderPath = "io" + File.separator + "measurements" + File.separator;

  public String statisticsFileName = "statistics.txt";
  public String resultFileName = "results.txt";

  public boolean writeResults = true;

  public Config(Config.Algorithm algorithm, Config.Dataset dataset) {
    this.algorithm = algorithm;
    this.setDataset(dataset);
  }

  private void setDataset(Config.Dataset dataset) {
    switch (dataset) {
      case PLANETS:
        this.databaseName = "planets";
        this.tableNames =
            new String[] {"WDC_astrology", "WDC_age", "WDC_astronomical", "WDC_game",
                          "WDC_kepler", "WDC_planets","WDC_planetz", "WDC_satellites",
                          "WDC_science", "WDC_symbols", "WDC_appearances"};
        this.inputFileHasHeader = true;
        this.inputFileSeparator = ',';
        break;
      case TPC_H_10_partial:
        this.databaseName = "tpc_h_10";
        this.tableNames = new String[] {"customer", "nation",
                                        // "orders",
                                        "part",
                                        // "partsupp",
                                        "region", "supplier",
                                        // "lineitem",
        };
        this.inputFileHasHeader = false;
        this.inputFileSeparator = ';';
        break;
      case TPC_H_10:
        this.databaseName = "tpc_h_10";
        this.tableNames =
            new String[] {"customer", "nation", "orders", "part", "partsupp", "region",
                          "supplier", "lineitem"};
        this.inputFileHasHeader = false;
        this.inputFileSeparator = ';';
        break;
      case TPC_H_1:
        this.databaseName = "tpc_h_1";
        this.tableNames =
            new String[] {"customer", "nation", "orders", "part", "partsupp", "region",
                          "supplier", "lineitem"};
        this.inputFileHasHeader = false;
        this.inputFileSeparator = '|';
        break;
      case ATOM:
        this.databaseName = "atom";
        this.tableNames =
            new String[] {"atom"};
        this.inputFileHasHeader = false;
        this.inputFileSeparator = ';';
        break;
      case SAP:
        this.databaseName = "sap";
        this.tableNames =
            new String[] {"CE4HI01_utf16", "ILOA_utf16", "VTTS_utf16", "ZBC00DT_COCM_utf16"};
        this.inputFileHasHeader = false;
        this.inputFileSeparator = ',';
        break;
      case PLISTA:
        this.databaseName = "plista";
        this.tableNames =
            new String[] {"error", "items", "request", "statistic"};
        this.inputFileHasHeader = false;
        this.inputFileSeparator = ';';
        break;
      case LOD:
        this.databaseName = "lod";
        this.tableNames =
            new String[] {"EXPERMENTS_THRESHOLDS", "EXPERMENTS_THRESHOLDS_PLI", "PERSON_DE", "PERSON_EN"};
        this.inputFileHasHeader = false;
        this.inputFileSeparator = ';';
        break;
      case PDB:
        this.databaseName = "pdb";
        this.tableNames =
            new String[] {"ATOM_SITES", "ATOM_SITES_ALT", "ATOM_SITES_FOOTNOTE", "ATOM_TYPE", "CELL",
                          "CHEM_COMP", "CITATION", "CITATION_AUTHOR", "CITATION_EDITOR", "COMPUTING",
                          "DATABASE_2", "DATABASE_PDB_CAVEAT", "DATABASE_PDB_MATRIX", "DATABASE_PDB_REV",
                          "DATABASE_PDB_REV_RECORD", "DATABASE_PDB_TVECT", "DIFFRN", "DIFFRN_DETECTOR",
                          "DIFFRN_RADIATION", "DIFFRN_RADIATION_WAVELENGTH", "DIFFRN_REFLNS",
                          "DIFFRN_SOURCE", "ENTITY", "ENTITY_KEYWORDS", "ENTITY_LINK", "ENTITY_NAME_COM",
                          "ENTITY_NAME_SYS", "ENTITY_POLY", "ENTITY_SRC_GEN", "ENTITY_SRC_NAT",
                          "EXPTL", "EXPTL_CRYSTAL", "EXPTL_CRYSTAL_GROW", "EXPTL_CRYSTAL_GROW_COMP",
                          "MMS_CATEGORY", "MMS_ENTRY", "MMS_ENTRY_CATEGORIES", "MMS_ITEM", "PDBX_DATABASE_PDB_OBS_SPR",
                          "PDBX_DATABASE_RELATED", "PDBX_DATABASE_STATUS", "PDBX_ENTITY_NAME",
                          "PDBX_ENTITY_SRC_SYN", "PDBX_NMR_CONSTRAINTS", "PDBX_NMR_DETAILS", "PDBX_NMR_ENSEMBLE",
                          "PDBX_NMR_ENSEMBLE_RMS", "PDBX_NMR_EXPTL", "PDBX_NMR_EXPTL_SAMPLE", "PDBX_NMR_REFINE",
                          "PDBX_NMR_REPRESENTATIVE", "PDBX_NMR_SAMPLE_DETAILS", "PDBX_NMR_SOFTWARE",
                          "PDBX_NMR_SPECTROMETER", "PDBX_PRERELEASE_SEQ", "PDBX_REFINE", "PDBX_REFINE_TLS",
                          "PDBX_REFINE_TLS_GROUP", "PDBX_STRUCT_SHEET_HBOND", "PDBX_XPLOR_FILE",
                          "PHASING", "PHASING_MAD", "PHASING_MAD_SET", "PHASING_MIR",
                          "PHASING_MIR_DER", "PHASING_MIR_DER_SHELL", "PHASING_MIR_DER_SITE",
                          "PHASING_MIR_SHELL", "REFINE", "REFINE_ANALYZE", "REFINE_B_ISO", "REFINE_HIST",
                          "REFINE_LS_RESTR", "REFINE_LS_RESTR_NCS", "REFINE_LS_SHELL", "REFINE_OCCUPANCY",
                          "REFLN", "REFLNS", "REFLNS_SHELL", "SOFTWARE", "STRUCT", "STRUCT_ASYM", "STRUCT_BIOL",
                          "STRUCT_BIOL_GEN", "STRUCT_BIOL_KEYWORDS", "STRUCT_CONF_TYPE", "STRUCT_CONN_TYPE",
                          "STRUCT_KEYWORDS", "STRUCT_MON_PROT", "STRUCT_MON_PROT_CIS", "STRUCT_NCS_DOM",
                          "STRUCT_NCS_DOM_LIM", "STRUCT_NCS_OPER", "STRUCT_REF", "STRUCT_REF_SEQ",
                          "STRUCT_REF_SEQ_DIF", "STRUCT_SHEET", "STRUCT_SHEET_ORDER", "STRUCT_SHEET_RANGE",
                          "STRUCT_SITE", "STRUCT_SITE_GEN", "STRUCT_SITE_KEYWORDS", "SYMMETRY"};
        this.inputFileHasHeader = false;
        this.inputFileSeparator = ';';
        break;
      default:
        break;
    }
  }

  @Override
  public String toString() {
    return "Config:\n\t" + "databaseName: " + this.databaseName + "\n\t" + "tableNames: "
           + this.tableNames;
  }
}
