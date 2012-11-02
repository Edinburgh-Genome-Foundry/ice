package org.jbei.ice.client.admin.group;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.GroupInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Panel for managing groups. Also acts as the view
 *
 * @author Hector Plahar
 */
public class GroupsPanel extends Composite implements AdminPanel {

    private VerticalPanel vPanel;
    private Button createGroup;
    private GroupsWidget widget;
    private HandlerRegistration createGroupRegistration;

    public GroupsPanel(DeleteActionCell.Delegate<AccountInfo> deleteDelegate) {
        ScrollPanel scrollPanel = new ScrollPanel();
        initWidget(scrollPanel);
        initComponents(deleteDelegate);

        vPanel.add(createGroup);
        scrollPanel.add(vPanel);
        vPanel.setStyleName("margin-top-20");
    }

    protected void initComponents(DeleteActionCell.Delegate<AccountInfo> deleteDelegate) {
        widget = new GroupsWidget(deleteDelegate);
        vPanel = new VerticalPanel();
        vPanel.setWidth("100%");

        createGroup = new Button("<i class=\"" + FAIconType.PLUS.getStyleName() + "\"></i>&nbsp; Create Group");
    }

    public void setCreateGroupHandler(ClickHandler clickHandler) {
        if (createGroupRegistration != null)
            createGroupRegistration.removeHandler();
        createGroupRegistration = createGroup.addClickHandler(clickHandler);
    }

    public void setGroupMembers(ArrayList<AccountInfo> list) {
        widget.setGroupMembers(list);
    }

    public void setGroups(ArrayList<GroupInfo> groups) {
        widget.setGroups(groups);
        widget.showDisplay();
        vPanel.add(widget);
    }

    public void setGroupSelectionHandler(ClickHandler handler) {
        widget.setClickHandler(handler);
    }

    public GroupInfo getGroupSelection(ClickEvent event) {
        return widget.getGroupSelection(event);
    }

    //
    // inner classes
    //
    private class GroupsWidget extends Composite {
        private ArrayList<GroupInfo> groups;
        private FlexTable groupList;
        private HorizontalPanel layout;
        private int row;
        private HashMap<Integer, GroupInfo> mapping;
        private final GroupMembersWidget groupMembers;

        public GroupsWidget(DeleteActionCell.Delegate<AccountInfo> delegate) {
            groupList = new FlexTable();
            groupList.setCellPadding(0);
            groupList.setCellSpacing(0);
            groupList.getFlexCellFormatter().setWidth(0, 0, "250px");
            mapping = new HashMap<Integer, GroupInfo>();
            groupMembers = new GroupMembersWidget(delegate);
            groupMembers.setVisible(false);
            layout = new HorizontalPanel();
            initWidget(layout);
            layout.setStyleName("margin-top-10");

            layout.add(groupList);
            layout.add(groupMembers);
        }

        public GroupInfo getGroupSelection(ClickEvent event) {
            HTMLTable.Cell cell = groupList.getCellForEvent(event);
            if (cell == null || cell.getCellIndex() > 0)
                return null;

            setSelected(cell.getRowIndex());
            return mapping.get(cell.getRowIndex());
        }

        public void setGroupMembers(ArrayList<AccountInfo> members) {
            groupMembers.setMemberList(members);
            groupMembers.setVisible(true);
        }

        public void setClickHandler(ClickHandler handler) {
            groupList.addClickHandler(handler);
        }

        public void setGroups(ArrayList<GroupInfo> list) {
            this.groups = list;
            mapping.clear();
            groupMembers.setMemberList(new ArrayList<AccountInfo>());  // reset
            groupMembers.setVisible(false);
            row = 0;
        }

        public void showDisplay() {
            displayGroup(groups.get(0), 0);
        }

        private void displayGroup(GroupInfo info, int level) {
            if (info == null)
                return;

            draw(info, level);
            GroupInfo child = getChild(info.getId());
            level += 5;
            displayGroup(child, level);
        }

        public void setSelected(int row) {
            for (int i = 0; i < groupList.getRowCount(); i += 1) {
                if (i == row)
                    groupList.getFlexCellFormatter().addStyleName(i, 0, "group_info_td_selected");
                else
                    groupList.getFlexCellFormatter().removeStyleName(i, 0, "group_info_td_selected");
            }
        }

        private void draw(GroupInfo info, int level) {
            groupList.setWidget(row, 0, new HTML("<div style=\"margin-left: " + (level * 3)
                                                         + "px\"><b>" + info.getLabel() + "</b><i class=\""
                                                         + FAIconType.CHEVRON_RIGHT.getStyleName() + "\"></i>"
                                                         + "<br><span style=\"color: #888; font-size: 0.65em; top: "
                                                         + "-5px; position: relative\">"
                                                         + info.getDescription() + "</span></div>"));
            groupList.getFlexCellFormatter().setStyleName(row, 0, "group_info_td");
            mapping.put(row, info);
            row += 1;
        }

        private GroupInfo getChild(long id) {
            for (GroupInfo info : groups) {
                if (info.getParentId() == id)
                    return info;
            }

            return null;
        }
    }
}