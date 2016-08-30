package CommonUtilPackage;

import java.io.Serializable;

import DataModel.GroupMember;

/**
 * Created by Asher on 26.05.2016.
 */
public class ContactItem implements Serializable, Comparable<ContactItem>{
    private String name;
    private String phoneNumber;
    private boolean isSelected;
    private GroupMember groupMember;

    public ContactItem(String name, String phoneNumber){
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public ContactItem(String name, String phoneNumber, GroupMember groupMember){
        this(name,phoneNumber);
        this.groupMember = groupMember;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getName() { return name; }

    public boolean getIsSelected() { return isSelected; }

    public GroupMember getGroupMember() {
        return groupMember;
    }

    public void setGroupMember(GroupMember groupMember) {
        this.groupMember = groupMember;
    }

    @Override
    public int compareTo(ContactItem contactItem) {
        return getName().compareToIgnoreCase(contactItem.getName());
    }
}
