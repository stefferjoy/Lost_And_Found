package com.ls.lostfound.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ls.lostfound.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationItem> notificationList;

    public NotificationAdapter(List<NotificationItem> notificationList) {
        this.notificationList = notificationList;
    }

    public void updateNotifications(List<NotificationItem> newNotifications) {
        this.notificationList = newNotifications;
        notifyDataSetChanged();
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem notification = notificationList.get(position);
        holder.textViewTitle.setText(notification.getTitle());
        holder.textViewMessage.setText(notification.getMessage());
        // Set other fields if needed
    }

    @Override
    public int getItemCount() {

        return notificationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewMessage;
        ImageView imageViewIcon;

        ViewHolder(View view) {
            super(view);
            textViewTitle = view.findViewById(R.id.textViewNotificationTitle);
            textViewMessage = view.findViewById(R.id.textViewNotificationMessage);
            imageViewIcon = view.findViewById(R.id.imageViewNotificationIcon);
        }
    }
}
