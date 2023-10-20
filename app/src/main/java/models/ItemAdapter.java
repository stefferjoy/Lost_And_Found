package models;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ls.lostfound.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private List<LostAndFoundItem> itemList;
    private Context context;


    public ItemAdapter(List<LostAndFoundItem> itemList, Context context) {
        this.itemList = itemList;
        this.context = context; // Assign the provided context
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

        Log.d("ImageURL", "Image URL: " + currentItem.getFirebaseImageUrl());

        // Load and display the image using Picasso
        if(currentItem.getFirebaseImageUrl() != null) {
            Log.d("LostAndFoundAdapter", "Loading image: " + currentItem.getFirebaseImageUrl());
            Picasso.get()
                    .load(currentItem.getFirebaseImageUrl())
                    .error(R.drawable.error_image)
                    .into(holder.imageViewItem);
            Log.d("ImageURL", "Image URL: " + currentItem.getFirebaseImageUrl());
        } else {
            Log.e("LostAndFoundAdapter", "FirebaseImageUrl is null");
        }


        // Set the values of your views
        holder.textViewName.setText(currentItem.getItemName());
        holder.textViewDescription.setText(currentItem.getDescription());
        holder.textViewLocation.setText(currentItem.getLocation());
        holder.textViewDate.setText(currentItem.getDate());
        holder.textViewUserId.setText("User ID: " + currentItem.getUserId());
        Picasso.get().load(currentItem.getFirebaseImageUrl()).into(holder.imageViewItem);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView  textViewName, textViewDescription, textViewLocation, textViewDate, textViewUserId;
        ImageView imageViewItem;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewItemName);
            textViewDescription = itemView.findViewById(R.id.textViewItemDescription);
            textViewLocation = itemView.findViewById(R.id.textViewItemLocation);
            textViewDate = itemView.findViewById(R.id.textViewItemDate);
            textViewUserId = itemView.findViewById(R.id.textViewUserId);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
        }
    }
}
