package Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import DataModel.Group;
import upp.foodonet.material.R;

/**
 * Created by Asher on 30.04.2016.
 */
public class GroupsListRecyclerViewAdapter extends RecyclerView.Adapter<GroupsListRecyclerViewAdapter.GroupsListViewHolder> {
    private ArrayList<Group> groupsList;
    private IOnGroupSelecterFromListListener groupSelectedListener;
    private int userID;
    private String admin_subtitle;

    public GroupsListRecyclerViewAdapter(ArrayList<Group> groups, int userID, String admin_subtitle, IOnGroupSelecterFromListListener listener) {
        groupsList = new ArrayList<>();
        groupsList.addAll(groups);
        groupSelectedListener = listener;
        this.userID = userID;
        this.admin_subtitle = admin_subtitle;
    }

    public void UpdateGroupsList(ArrayList<Group> groups){
        groupsList = new ArrayList<>();
        groupsList.addAll(groups);
        this.notifyDataSetChanged();
    }

    @Override
    public GroupsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_list_item, parent, false);
        return new GroupsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GroupsListViewHolder holder, int position) {
        Group group = groupsList.get(position);
        holder.setGroupID(group.Get_id());
        holder.setGroupMembersCount(group.get_members_count());
        holder.setGroupTitle(group.Get_name());
        if(group.Get_admin_id() == userID) holder.setGroupIsAdmin(admin_subtitle);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupSelectedListener.OnGroupSelected(holder.getGroupID());
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    public class GroupsListViewHolder extends RecyclerView.ViewHolder {
        private TextView groupTitle;
        private TextView groupMembersCount;
        private TextView groupIsAdmin;
        private int groupID;
        public View view;

        public void setGroupIsAdmin(String isAdmin) {
            this.groupIsAdmin.setText(isAdmin);
        }

        public GroupsListViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            groupTitle = (TextView) view.findViewById(R.id.tv_groups_list_item_title);
            groupMembersCount = (TextView) view.findViewById(R.id.tv_groups_list_item_members_count);
            groupIsAdmin = (TextView)view.findViewById(R.id.tv_is_admin);
        }

        public void setGroupTitle(String title) {
            groupTitle.setText(title);
        }

        public void setGroupMembersCount(int count) {
            groupMembersCount.setText(String.valueOf(count));
        }

        public void setGroupID(int id) {
            groupID = id;
        }

        public int getGroupID() {
            return groupID;
        }
    }
}
