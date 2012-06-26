package org.jbei.ice.client.bulkimport;

import com.google.gwt.user.client.Window;
import org.jbei.ice.client.bulkimport.model.ModelFactory;
import org.jbei.ice.client.bulkimport.model.SheetFieldData;
import org.jbei.ice.client.bulkimport.model.SheetModel;
import org.jbei.ice.client.bulkimport.sheet.Header;
import org.jbei.ice.client.bulkimport.sheet.ImportTypeHeaders;
import org.jbei.ice.client.bulkimport.sheet.InfoValueExtractorFactory;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class SheetPresenter {

    /**
     * interface that the equivalent view
     * for this presenter must interface
     */
    public static interface View {

        void highlightHeaders(int row, int col);

        void clear();

        boolean isEmptyRow(int row);

        void setRow(int row);

        int getRow();

        HashMap<Integer, String> getAttachmentRowFileIds();

        HashMap<Integer, String> getSequenceRowFileIds();

        void setCellWidgetForCurrentRow(Header header, String display, String title, int col);

        int getSheetRowCount();

        String getCellText(int row, int col);
    }

    private final View view;
    private HashMap<AutoCompleteField, ArrayList<String>> data;
    private final EntryAddType type;
    private BulkImportDraftInfo currentInfo;

    public SheetPresenter(View view, EntryAddType type) {
        this.view = view;
        this.type = type;
    }

    public SheetPresenter(View view, EntryAddType type, BulkImportDraftInfo info) {
        this(view, type);
        this.currentInfo = info;
    }

    public void reset() {
        if (Window.confirm("Clear all data?"))
            view.clear();
    }

    public void setAutoCompleteData(HashMap<AutoCompleteField, ArrayList<String>> data) {
        this.data = data;
    }

    public ArrayList<String> getAutoCompleteData(AutoCompleteField field) {
        if (data == null)
            return null;

        return data.get(field);
    }

    public EntryAddType getType() {
        return this.type;
    }

    public Header[] getTypeHeaders() {
        return ImportTypeHeaders.getHeadersForType(type);
    }

    public ArrayList<EntryInfo> getCellEntryList() {

        Header[] headers = getTypeHeaders();
        int rowCount = view.getSheetRowCount();
        ArrayList<EntryInfo> toRemove = new ArrayList<EntryInfo>();
        SheetModel model = ModelFactory.getModelForType(type);
        if (model == null)
            return null;
        ArrayList<EntryInfo> infoList = new ArrayList<EntryInfo>();

        // for each row
        for (int i = 0; i < rowCount; i += 1) {

            if (view.isEmptyRow(i)) {
                toRemove.add(currentInfo.getEntryList().remove(i));
                continue;
            }

            // is row associated with a saved entry?
            EntryInfo existing = currentInfo.getEntryList().get(i);

//            for each header
            int y = 0;
            for (Header header : headers) {

                String id = "";
                switch (header) {
                    case ATT_FILENAME:
                        id = view.getAttachmentRowFileIds().get(i);
                        break;

                    case SEQ_FILENAME:
                        id = view.getSequenceRowFileIds().get(i);
                        break;
                }

                String text = view.getCellText(i, y);
                model.setInfoField(new SheetFieldData(header, id, text), existing);
                y += 1;
            }

            infoList.add(existing);
        }

        currentInfo.getEntryList().clear();
        currentInfo.getEntryList().addAll(infoList);
        return infoList;
    }

    /**
     * @return size of the field, which also equates to
     *         the number of columns displayed in the sheet. This is based on the number of
     *         headers for the entry type
     */
    public int getFieldSize() {
        return getTypeHeaders().length;
    }

    public void addRow() {
        int index = view.getRow() - 1; // row includes the headers but this is 0-indexed

        // type is already set in the constructor
        Header[] headers = getTypeHeaders();
        int headersSize = headers.length;

        for (int i = 0; i < headersSize; i += 1) {

            String display = "";
            String title = "";
            if (currentInfo != null && currentInfo.getCount() >= view.getRow()) {

                EntryInfo primaryInfo = currentInfo.getEntryList().get(index);
                String value = InfoValueExtractorFactory.extractValue(getType(),
                                                                      headers[i],
                                                                      primaryInfo,
                                                                      primaryInfo.getInfo(),
                                                                      index,
                                                                      view.getAttachmentRowFileIds(),
                                                                      view.getSequenceRowFileIds());
                if (value == null)
                    value = "";

                display = value;
                title = value;
                if (value.length() > 15)
                    display = (value.substring(0, 13) + "...");
            }

            view.setCellWidgetForCurrentRow(headers[i], display, title, i);
        }
        view.setRow(view.getRow() + 1);
    }
}
