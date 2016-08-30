package Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import DataModel.RegisteredUserForPublication;
import upp.foodonet.material.R;

/**
 * Created by Asher on 15.07.2016.
 */
public class RegisteredUsersForCallOrSmsRecyclerViewAdapter extends RecyclerView.Adapter<RegisteredUsersForCallOrSmsRecyclerViewAdapter.RegisteredUserViewHolder> {
    ArrayList<RegisteredUserForPublication> regList;
    IRegisteredUserSelectedCallback callback;

    public RegisteredUsersForCallOrSmsRecyclerViewAdapter(ArrayList<RegisteredUserForPublication> regListNew, IRegisteredUserSelectedCallback callback){
        this.callback = callback;
        this.regList = new ArrayList<>();
        if(regList != null)
            for(RegisteredUserForPublication regUser : regListNew)
                this.regList.add(regUser);
    }

    public void UpdateList(ArrayList<RegisteredUserForPublication> regListNew){
        this.regList.clear();
        if(regList != null)
            for(RegisteredUserForPublication regUser : regListNew)
                this.regList.add(regUser);
        notifyDataSetChanged();
    }

    @Override
    public RegisteredUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.registered_user_item, parent, false);
        return new RegisteredUserViewHolder(view, callback);
    }

    @Override
    public void onBindViewHolder(RegisteredUserViewHolder holder, int position) {
        holder.InitItem(regList.get(position));
    }

    @Override
    public int getItemCount() {
        return regList.size();
    }

    public class RegisteredUserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        IRegisteredUserSelectedCallback callback;
        Button btn_reg_user_select;
        TextView tv_reg_user_name;
        String phoneNumber;

        public RegisteredUserViewHolder(View itemView, IRegisteredUserSelectedCallback callback) {
            super(itemView);
            this.callback = callback;
            btn_reg_user_select = (Button)itemView.findViewById(R.id.btn_reg_user_select);
            btn_reg_user_select.setOnClickListener(this);
            tv_reg_user_name = (TextView)itemView.findViewById(R.id.tv_reg_user_name);
        }

        public void InitItem(RegisteredUserForPublication regUser){
            tv_reg_user_name.setText(regUser.getCollectorName());
            phoneNumber = regUser.getCollectorphone();
        }

        @Override
        public void onClick(View view) {
            callback.RegisteredUserSelected(phoneNumber);
        }
    }
}
