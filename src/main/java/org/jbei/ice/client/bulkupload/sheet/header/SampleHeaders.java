package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.ArrayList;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.shared.dto.EntryInfo;

/**
 * @author Hector Plahar
 */
public abstract class SampleHeaders {

    protected ArrayList<CellColumnHeader> headers = new ArrayList<CellColumnHeader>();

    public SampleHeaders(ArrayList<String> locationList) {
        for (String location : locationList) {
            try {
                Header header = Header.valueOf("SAMPLE_" + location.replaceAll(" ", "_").toUpperCase());
                headers.add(new CellColumnHeader(header));
            } catch (IllegalArgumentException ila) {
                headers.clear();
                return;
            }
        }
    }

    public CellColumnHeader getHeaderForIndex(int index) {
        return headers.get(index);
    }

    public ArrayList<CellColumnHeader> getHeaders() {
        return headers;
    }

    public int getHeaderSize() {
        return this.headers.size();
    }

    public abstract SheetCellData extractValue(Header headerType, EntryInfo info);

    SheetCellData extractCommon(Header headerType, EntryInfo info) {

        if (!info.isHasSample())
            return null;

        switch (headerType) {
            case SAMPLE_NAME:
                SampleStorage sampleStorage = info.getOneSampleStorage();
                String value = sampleStorage.getSample().getLabel();
                return new SheetCellData(headerType, value, value);

            default:
                return null;
        }
    }
}