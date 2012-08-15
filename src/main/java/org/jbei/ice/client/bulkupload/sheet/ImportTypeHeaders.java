package org.jbei.ice.client.bulkupload.sheet;

import org.jbei.ice.shared.EntryAddType;

// utility class for returning the headers for specific import type
public class ImportTypeHeaders {

    public static Header[] getHeadersForType(EntryAddType type) {
        Header[] headers;

        switch (type) {
            case STRAIN:
                headers = getStrainHeaders();
                break;

            case PLASMID:
                headers = getPlasmidHeaders();
                break;

            case PART:
                headers = getPartHeaders();
                break;

            case ARABIDOPSIS:
                headers = getSeedHeaders();
                break;

            case STRAIN_WITH_PLASMID:
                headers = getStrainWithPlasmidHeaders();
                break;

            default:
                headers = null;
        }

        if (headers == null)
            return headers;

        // this is a bit of a hack due to using an enum
        for (Header header : headers)
            header.getCell().reset();

        return headers;
    }

    public static Header[] getStrainHeaders() {
        Header[] headers = new Header[]{Header.PI, Header.FUNDING_SOURCE, Header.IP,
                Header.BIOSAFETY, Header.NAME, Header.ALIAS, Header.KEYWORDS, Header.SUMMARY,
                Header.NOTES, Header.REFERENCES, Header.LINKS, Header.STATUS, Header.SEQ_FILENAME,
                Header.ATT_FILENAME, Header.SELECTION_MARKERS, Header.PARENTAL_STRAIN,
                Header.GEN_PHEN, Header.PLASMIDS
        };
        return headers;
    }

    public static Header[] getPlasmidHeaders() {
        Header[] headers = new Header[]{Header.PI, Header.FUNDING_SOURCE, Header.IP,
                Header.BIOSAFETY, Header.NAME, Header.ALIAS, Header.KEYWORDS, Header.SUMMARY,
                Header.NOTES, Header.REFERENCES, Header.LINKS, Header.STATUS, Header.SEQ_FILENAME,
                Header.ATT_FILENAME, Header.SELECTION_MARKERS, Header.CIRCULAR, Header.BACKBONE,
                Header.PROMOTERS, Header.ORIGIN_OF_REPLICATION
        };
        return headers;
    }

    public static Header[] getPartHeaders() {
        Header[] headers = new Header[]{Header.PI, Header.FUNDING_SOURCE, Header.IP,
                Header.BIOSAFETY, Header.NAME, Header.ALIAS, Header.KEYWORDS, Header.SUMMARY,
                Header.NOTES, Header.REFERENCES, Header.LINKS, Header.STATUS, Header.SEQ_FILENAME,
                Header.ATT_FILENAME
        };
        return headers;
    }

    public static Header[] getSeedHeaders() {
        Header[] headers = new Header[]{Header.PI, Header.FUNDING_SOURCE, Header.IP,
                Header.BIOSAFETY, Header.NAME, Header.ALIAS, Header.KEYWORDS, Header.SUMMARY,
                Header.NOTES, Header.REFERENCES, Header.LINKS, Header.STATUS, Header.HOMOZYGOSITY,
                Header.ECOTYPE, Header.HARVEST_DATE, Header.GENERATION, Header.PLANT_TYPE,
                Header.PARENTS, Header.SELECTION_MARKERS
        };
        return headers;
    }

    public static Header[] getStrainWithPlasmidHeaders() {
        Header[] headers = new Header[]{Header.PI, Header.FUNDING_SOURCE, Header.IP,
                Header.BIOSAFETY, Header.STRAIN_NAME, Header.STRAIN_ALIAS, Header.STRAIN_KEYWORDS,
                Header.STRAIN_SUMMARY, Header.STRAIN_NOTES, Header.STRAIN_REFERENCES,
                Header.STRAIN_LINKS, Header.STRAIN_STATUS, Header.STRAIN_SEQ_FILENAME,
                Header.STRAIN_ATT_FILENAME, Header.STRAIN_SELECTION_MARKERS,
                Header.STRAIN_PARENTAL_STRAIN, Header.STRAIN_GEN_PHEN, Header.STRAIN_PLASMIDS,
                Header.PLASMID_NAME, Header.PLASMID_ALIAS, Header.PLASMID_KEYWORDS,
                Header.PLASMID_SUMMARY, Header.PLASMID_NOTES, Header.PLASMID_REFERENCES,
                Header.PLASMID_LINKS, Header.PLASMID_STATUS, Header.PLASMID_SEQ_FILENAME,
                Header.PLASMID_ATT_FILENAME, Header.PLASMID_SELECTION_MARKERS, Header.CIRCULAR,
                Header.PLASMID_BACKBONE, Header.PLASMID_PROMOTERS,
                Header.PLASMID_ORIGIN_OF_REPLICATION
        };
        return headers;
    }
}
