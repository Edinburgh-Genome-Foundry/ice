package org.jbei.ice.lib.dto;

import org.jbei.ice.lib.dao.IDataTransferModel;
import org.jbei.ice.lib.dto.sample.SampleType;

/**
 * Storage location information that specifies display (which can be a unique identifier)
 * and the location type (e.g. well in a plate)
 *
 * @author Hector Plahar
 */
public class StorageLocation implements IDataTransferModel {

    private long id;
    private String display;
    private SampleType type;
    private StorageLocation child;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public SampleType getType() {
        return type;
    }

    public void setType(SampleType type) {
        this.type = type;
    }

    public StorageLocation getChild() {
        return child;
    }

    public void setChild(StorageLocation child) {
        this.child = child;
    }
}
