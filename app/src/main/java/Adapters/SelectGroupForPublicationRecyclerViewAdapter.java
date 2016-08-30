package Adapters;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import DataModel.Group;
import upp.foodonet.material.R;

/**
 * Created by Asher on 09.06.2016.
 */
public class SelectGroupForPublicationRecyclerViewAdapter extends RecyclerView.Adapter<SelectGroupForPublicationRecyclerViewAdapter.SelectGroupForPublicationViewHolder> {

    private static final String MY_TAG = "food_rv_groupsAdapter";

    private ArrayList<Group> groupsList;
    public IOnGroupForPublicationSelectedListener groupSelectedCallback;
    private Drawable publicGroupIcon;
    private Drawable privateGroupIcon;

    public SelectGroupForPublicationRecyclerViewAdapter(ArrayList<Group> groups, IOnGroupForPublicationSelectedListener callback, Drawable publicGroupIcon, Drawable privateGroupIcon, String publicGroupName){
        groupSelectedCallback = callback;
        this.publicGroupIcon = publicGroupIcon;
        this.privateGroupIcon = privateGroupIcon;
        Group groupPublic = new Group();
        groupPublic.Set_name(publicGroupName);
        groupPublic.Set_id(0);
        groupsList = new ArrayList<>();
        groupsList.add(groupPublic);
        groupsList.addAll(groups);
    }

    public void UpdateGroupsList(ArrayList<Group> groups){
        if(groups == null || groups.size() < 1)
            return;
        if(groupsList == null)
            groupsList = new ArrayList<>();
        groupsList.addAll(groups);
        this.notifyDataSetChanged();
    }

    @Override
    public SelectGroupForPublicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_for_publication_list_item, parent, false);
        return new SelectGroupForPublicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SelectGroupForPublicationViewHolder holder, int position) {
        Group group = groupsList.get(position);
        final String groupName = group.Get_name();
        holder.setGroupID(group.Get_id());
        holder.SetTitle(groupName);
        holder.SetIcon(group.Get_id() == 0 ? publicGroupIcon : privateGroupIcon);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupSelectedCallback.OnGroupForPublicationSelected(holder.getGroupID(), groupName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    public class SelectGroupForPublicationViewHolder extends RecyclerView.ViewHolder{

        private TextView groupTitle;
        private ImageView groupIcon;
        private int groupID;
        public View view;

        public SelectGroupForPublicationViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            groupTitle = (TextView)view.findViewById(R.id.tv_group_for_pub_title);
            groupIcon = (ImageView)view.findViewById(R.id.iv_group_for_pub_icon);
        }

        public void SetTitle(String title){
            if(groupTitle != null) groupTitle.setText(title);
        }

        public void SetIcon(Drawable icon){
            if(groupIcon != null) groupIcon.setImageDrawable(icon);
        }

        public void setGroupID(int groupID) {
            this.groupID = groupID;
        }

        public int getGroupID() {
            return groupID;
        }
    }
}
