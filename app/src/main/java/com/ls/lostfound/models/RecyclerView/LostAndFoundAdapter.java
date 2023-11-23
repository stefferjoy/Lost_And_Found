package com.ls.lostfound.models.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.ls.lostfound.R;
import com.ls.lostfound.models.LostAndFoundItem;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LostAndFoundAdapter extends RecyclerView.Adapter<LostAndFoundAdapter.LostAndFoundItemViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private List<LostAndFoundItem> itemList;

    public LostAndFoundAdapter(List<LostAndFoundItem> itemList) {
        this.itemList = itemList;
    }

    private OnChatButtonClickListener chatButtonListener;

    public void setOnChatButtonClickListener(OnChatButtonClickListener listener) {
        this.chatButtonListener = listener;
    }


    @NonNull
    @Override
    public LostAndFoundItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lost_and_found, parent, false);
        return new LostAndFoundItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LostAndFoundItemViewHolder holder, int position) {
        LostAndFoundItem currentItem = itemList.get(position);

        if (currentItem != null) {
            holder.textViewUser.setText("User: " + currentItem.getUserId());
            holder.textViewName.setText("Item Name: " + currentItem.getItemName());
            holder.textViewDescription.setText("Description: " + currentItem.getDescription());
            holder.textViewLocation.setText("Location: " + currentItem.getLocation());
            holder.textViewDate.setText("Date: " + currentItem.getDate());

            if (currentItem.getFirebaseImageUrl() != null) {
                // Load the image from Firebase Storage
                Picasso.get().load(currentItem.getFirebaseImageUrl()).into(holder.imageViewItem);
            } else {
                // You can set a placeholder image here if needed
                holder.imageViewItem.setImageResource(R.drawable.placeholder_image);
            }
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class LostAndFoundItemViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUser, textViewName, textViewDescription, textViewLocation, textViewDate;
        ImageView imageViewItem;

        public LostAndFoundItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUser = itemView.findViewById(R.id.textViewUserName);
            textViewName = itemView.findViewById(R.id.textViewItemName);
            textViewDescription = itemView.findViewById(R.id.textViewItemDescription);
            textViewLocation = itemView.findViewById(R.id.textViewItemLocation);
            textViewDate = itemView.findViewById(R.id.textViewItemDate);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
        }
    }
    public interface OnChatButtonClickListener {
        void onChatButtonClick(String userId);
    }

}
