package org.jbei.ice.client.common.header;

import org.jbei.ice.client.common.header.HeaderView.Resources;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class SearchCompositeBox extends Composite {

    private final TextBox box;
    private final HTMLPanel imagePanel;
    private Image image;
    private final FlexTable widgetHolder;

    public SearchCompositeBox() {
        Grid grid = new Grid(1, 3);
        grid.setCellPadding(0);
        grid.setCellSpacing(0);
        grid.setStyleName("quick_search");
        initWidget(grid);

        widgetHolder = new FlexTable();
        widgetHolder.setCellPadding(4);
        widgetHolder.setCellSpacing(0);

        // widget holder
        grid.setWidget(0, 0, widgetHolder);

        // text box
        box = new TextBox();
        box.setWidth("300px");
        box.setStyleName("quick_search_input");
        grid.setWidget(0, 1, box);

        // search arrow
        imagePanel = new HTMLPanel(
                "<span style=\"position: relative; left: 7px\" id=\"search_arrow\"></span>");
        image = new Image(Resources.INSTANCE.arrowDown());
        imagePanel.setStyleName("quick_search_arrow_panel");
        imagePanel.add(image, "search_arrow");
        grid.setWidget(0, 2, imagePanel);
        grid.getCellFormatter().setStyleName(0, 2, "search_arrow_td");
    }

    public void addSearchWidget(Widget widget) { // TODO : this is set not add
        widgetHolder.setWidget(0, 0, widget);
    }

    public TextBox getTextBox() {
        return this.box;
    }

    public Image getImage() {
        return this.image;
    }

    public void appendFilter(String filter) {
        if (box.getText().isEmpty())
            box.setText(filter);
        else
            box.setText(box.getText() + " " + filter);
    }
}
