package Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.ImageDictionarySyncronized;
import DataModel.FCPublication;
import DataModel.FNotification;
import FooDoNetServerClasses.ImageDownloader;
import UIUtil.RoundedImageView;
import upp.foodonet.material.R;

/**
 * Created by Asher on 22.07.2016.
 */
public class NotificationsRecyclerViewAdapter extends RecyclerView.Adapter<NotificationsRecyclerViewAdapter.NotificationViewHolder> {
    private static final String MY_TAG = "food_notifAdapter";

    ArrayList<FNotification> notifications;
    Context context;

    ImageDictionarySyncronized imageDictionary;
    ImageDownloader imageDownloader;

    public NotificationsRecyclerViewAdapter(Context context){
        this.context = context;
        imageDictionary = new ImageDictionarySyncronized();
        imageDownloader = new ImageDownloader(context, imageDictionary);
    }

    public void UpdateNotificationsList(ArrayList<FNotification> notifications){
        if(this.notifications == null)
            this.notifications = new ArrayList<>();
        this.notifications.clear();
        if(notifications != null && notifications.size() != 0)
            this.notifications.addAll(notifications);
        notifyDataSetChanged();
    }

    public void RemoveNotificationItem(int position){
        notifications.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card_item, parent, false);
        return new NotificationViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int position) {
        holder.SetNotificationData(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder{
        Context context;
        public int notificationID;
        RoundedImageView riv_image;
        ImageView iv_icon;
        TextView tv_title;
        TextView tv_subtitle;
        TextView tv_when_arrived;

        public NotificationViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            riv_image = (RoundedImageView)itemView.findViewById(R.id.riv_notification_image);
            iv_icon = (ImageView)itemView.findViewById(R.id.iv_notification_type_icon);
            tv_title = (TextView)itemView.findViewById(R.id.tv_notification_title);
            tv_subtitle = (TextView)itemView.findViewById(R.id.tv_notification_subtitle);
            tv_when_arrived = (TextView)itemView.findViewById(R.id.tv_notification_when_arrived);
        }

        public void SetNotificationData(FNotification notification){
            notificationID = notification.get_id();
            tv_subtitle.setText(notification.get_publication_or_group_title());
            tv_when_arrived.setText(CommonUtil.GetTimeLeftString(context, notification.get_date_arrived(), new Date()));
            riv_image.setImageDrawable(context.getResources().getDrawable(R.drawable.notification_no_photo));
            switch (notification.get_type()){
                case FNotification.FNOTIFICATION_TYPE_NEW_PUBLICATION:
                    tv_title.setText(context.getString(R.string.notification_title_new_event_near_you));
                    tv_title.setText(context.getString(R.string.notification_title_new_event_near_you));
                    break;
                case FNotification.FNOTIFICATION_TYPE_EDITED_PUBLICATION:
                    iv_icon.setImageDrawable(context.getResources().getDrawable(R.drawable.notification_icon_new_event));
                    tv_title.setText(context.getString(R.string.notification_title_event_changed));
                    break;
                case FNotification.FNOTIFICATION_TYPE_DELETED_PUBLICATION:
                    tv_title.setText(context.getString(R.string.notification_title_event_ended));
                    iv_icon.setImageDrawable(context.getResources().getDrawable(R.drawable.notification_icon_ended));
                    break;
                case FNotification.FNOTIFICATION_TYPE_GROUP_MEMBER_DELETE:
                    tv_title.setText(context.getString(R.string.notification_title_you_were_removed_from_group));
                    iv_icon.setImageDrawable(context.getResources().getDrawable(R.drawable.notification_icon_ended));
                    break;
                case FNotification.FNOTIFICATION_TYPE_NEW_REPORT:
                    iv_icon.setImageDrawable(context.getResources().getDrawable(R.drawable.notification_icon_report));
                    tv_title.setText(context.getString(R.string.notification_title_new_report_for_event));
                    break;
                case FNotification.FNOTIFICATION_TYPE_GROUP_MEMBER_ADD:
                    iv_icon.setImageDrawable(context.getResources().getDrawable(R.drawable.notification_icon_group));
                    tv_title.setText(context.getString(R.string.notification_title_user_added_you_to_group));
                    break;
                case FNotification.FNOTIFICATION_TYPE_NEW_REGISTRATION:
                    iv_icon.setImageDrawable(context.getResources().getDrawable(R.drawable.notification_icon_joined));
                    tv_title.setText(context.getString(R.string.notification_title_someone_registered_for_your_pub));
                    break;
            }
            if(notification.get_type() != FNotification.FNOTIFICATION_TYPE_GROUP_MEMBER_ADD
               && notification.get_type() != FNotification.FNOTIFICATION_TYPE_GROUP_MEMBER_DELETE)
                SetPublicationImage(notification.get_publication_or_group_id(), notification.get_publication_version(), riv_image);
        }

        private void SetPublicationImage(int pubID, int pubVersion, ImageView publicationImage) {
            Drawable imageDrawable;
            imageDrawable = imageDictionary.Get(pubID);
            if (imageDrawable == null) {
                imageDownloader.Download(pubID, pubVersion, publicationImage);
            } else
                publicationImage.setImageDrawable(imageDrawable);
        }
    }
}
