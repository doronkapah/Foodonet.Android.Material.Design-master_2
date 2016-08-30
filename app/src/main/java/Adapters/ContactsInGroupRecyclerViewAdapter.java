package Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import CommonUtilPackage.ContactItem;
import CommonUtilPackage.IContactItemRemoved;
import CommonUtilPackage.IGroupMemberRemoved;
import upp.foodonet.material.R;

/**
 * Created by Asher on 28.05.2016.
 */
public class ContactsInGroupRecyclerViewAdapter extends RecyclerView.Adapter<ContactsInGroupRecyclerViewAdapter.ContactInGroupViewHolder> implements IContactItemRemoved {
    ArrayList<ContactItem> contacts;
    IGroupMemberRemoved callback;

    public ContactsInGroupRecyclerViewAdapter(IGroupMemberRemoved parentCallback) {
        this.contacts = new ArrayList<>();
        this.callback = parentCallback;
    }

    public void setContacts(ArrayList<ContactItem> contacts) {
        this.contacts.clear();
        this.contacts.addAll(contacts);
        this.notifyDataSetChanged();
    }

    @Override
    public ContactInGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_in_group_item, parent, false);
        return new ContactInGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactInGroupViewHolder holder, int position) {
        holder.setContactItem(contacts.get(position));
        holder.setCallback(this);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    @Override
    public void OnContactItemRemoved(ContactItem contactItem) {
        if(contactItem.getGroupMember() != null)
            callback.OnGroupMemberRemoved(contactItem.getGroupMember());
        contacts.remove(contactItem);
        notifyDataSetChanged();
    }

    public class ContactInGroupViewHolder extends RecyclerView.ViewHolder {

        ContactItem item;
        IContactItemRemoved contactItemRemovedCallback;

        TextView tv_title;
        ImageView icon_user;
        ImageView icon_not_user;
        ImageButton btn_delete_member;

        public ContactInGroupViewHolder(final View itemView) {
            super(itemView);
            tv_title = (TextView) itemView.findViewById(R.id.tv_contact_in_group_title);
            icon_user = (ImageView) itemView.findViewById(R.id.iv_group_member_icon_user);
            icon_not_user = (ImageView) itemView.findViewById(R.id.iv_group_member_icon_not_user);
            btn_delete_member = (ImageButton) itemView.findViewById(R.id.ib_leave_group);
            btn_delete_member.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(contactItemRemovedCallback != null && item != null)
                        contactItemRemovedCallback.OnContactItemRemoved(item);
                }
            });
        }

        public void setContactItem(ContactItem item){
            this.item = item;
            this.tv_title.setText(item.getName());
            icon_user.setVisibility(item.getGroupMember() == null
                    || item.getGroupMember().get_user_id() == 0
                    ? View.GONE : View.VISIBLE);
            icon_not_user.setVisibility(item.getGroupMember() == null
                    || item.getGroupMember().get_user_id() == 0
                    ? View.VISIBLE : View.GONE);
        }

        public void setCallback(IContactItemRemoved callback){
            contactItemRemovedCallback = callback;
        }
    }
}
