package com.ls.lostfound.models;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ls.lostfound.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private List<LostAndFoundItem> itemList;
    private Context context;
    private boolean isMyPosts;

    private OnEditListener onEditListener;
    private OnDeleteListener onDeleteListener;

    public ItemAdapter(List<LostAndFoundItem> itemList, Context context, boolean isMyPosts,
                       OnEditListener onEditListener, OnDeleteListener onDeleteListener) {
        this.itemList = itemList;
        this.context = context;
        this.isMyPosts = isMyPosts;
        this.onEditListener = onEditListener;
        this.onDeleteListener = onDeleteListener;
    }

    public void setItems(List<LostAndFoundItem> items) {
        this.itemList = items;
        notifyDataSetChanged();
    }

    public void removeItem(LostAndFoundItem item) {
        int position = itemList.indexOf(item);
        if (position > -1) {
            itemList.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lost_and_found, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        LostAndFoundItem currentItem = itemList.get(position);


        // Load and display the image using Picasso
        if (currentItem.getFirebaseImageUrl() != null) {
            Log.d("LostAndFoundAdapter", "Loading image: " + currentItem.getFirebaseImageUrl());
            Picasso.get()
                    .load(currentItem.getFirebaseImageUrl())
                    .error(R.drawable.error_image)
                    .into(holder.imageViewItem);
        } else {
            Log.e("ItemAdapter", "FirebaseImageUrl is null");
        }





        // Set visibility and click listeners for edit and delete buttons
        if (isMyPosts == true) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);

            holder.editButton.setOnClickListener(v -> {
                Log.d("ItemAdapter", "Edit button clicked for position: " + position);
                onEditListener.onEditClicked(currentItem);
            });

            holder.deleteButton.setOnClickListener(v -> {
                Log.d("ItemAdapter", "Delete button clicked for position: " + position);
                onDeleteListener.onDeleteClicked(currentItem);
            });

        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }

        // Set the values of your views
        holder.textViewName.setText(currentItem.getItemName());
        holder.textViewDescription.setText(currentItem.getDescription());
        holder.textViewLocation.setText(currentItem.getLocation());
        holder.textViewDate.setText(currentItem.getDate());
        holder.textViewUserName.setText("User Name: " + currentItem.getUserName());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewDescription, textViewLocation, textViewDate, textViewUserName;
        ImageView imageViewItem, editButton, deleteButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewItemName);
            textViewDescription = itemView.findViewById(R.id.textViewItemDescription);
            textViewLocation = itemView.findViewById(R.id.textViewItemLocation);
            textViewDate = itemView.findViewById(R.id.textViewItemDate);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }


    // Interfaces for the listeners
    public interface OnEditListener {
        void onEditClicked(LostAndFoundItem item);
    }

    public interface OnDeleteListener {
        void onDeleteClicked(LostAndFoundItem item);
    }
}
