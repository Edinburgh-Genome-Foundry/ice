package org.jbei.ice.client.admin.group;

import org.jbei.ice.client.common.widget.FAIconType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Hector Plahar
 */
public class CreateGroupWidget {
    private PopupPanel popup;
    private FlexTable createTable;
    private TextBox groupName;
    private TextBox groupDescription;
    private HTML closeLink;
    private Button createButton;
    private Button cancelButton;

    public CreateGroupWidget() {
        initComponents();

        int row = 0;
        createTable.setWidget(row, 0, closeLink);
        createTable.getFlexCellFormatter().setColSpan(row, 0, 2);
        row += 1;
        createTable.setHTML(row, 0, "<b>Name</b>");
        createTable.setWidget(row, 1, groupName);
        row += 1;
        createTable.setHTML(row, 0, "<b>Description</b>");
        createTable.setWidget(row, 1, groupDescription);
        row += 1;
        createTable.setWidget(row, 0, createButton);
        createTable.getFlexCellFormatter().setHorizontalAlignment(row, 0, HasAlignment.ALIGN_RIGHT);
        createTable.setWidget(row, 1, cancelButton);

        popup.setWidget(createTable);
        popup.setGlassEnabled(true);
        closeLink.addClickHandler(new CloseWidgetHandler());
        cancelButton.addClickHandler(new CloseWidgetHandler());
    }

    private void initComponents() {
        popup = new PopupPanel();
        createTable = new FlexTable();
        createTable.setStyleName("create_widget_popup");
        groupName = new TextBox();
        groupName.setStyleName("input_box");
        groupDescription = new TextBox();
        groupDescription.setStyleName("input_box");
        closeLink = new HTML("<span style=\"color: #888; font-size: 0.70em; float: right\">Close <i class=\""
                                     + FAIconType.REMOVE_SIGN.getStyleName() + "\"></i></span>");
        createButton = new Button("Create");
        cancelButton = new Button("Cancel");
    }

    public void showPopup(boolean show) {
        if (popup.isShowing() == show)
            return;

        if (show)
            popup.center();
        else
            popup.hide();
    }

    public String getGroupName() {
        return groupName.getText().trim();
    }

    public String getGroupDescription() {
        return groupDescription.getText().trim();
    }

    private class CloseWidgetHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            showPopup(false);
        }
    }
}