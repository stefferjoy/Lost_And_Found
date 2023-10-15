package models;

import android.content.Context;
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

    public ItemAdapter(List<LostAndFoundItem> itemList) {
        this.itemList = itemList;
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

        holder.textViewName.setText(currentItem.getItemName());
        holder.textViewDescription.setText(currentItem.getDescription());
        holder.textViewLocation.setText(currentItem.getLocation());
        holder.textViewDate.setText(currentItem.getDate());

        // Display the userId
        holder.textViewUserId.setText("User ID: " + currentItem.getUserId());


        if (currentItem.getImagePath() != null) {
            Picasso.get().load(currentItem.getImagePath()).into(holder.imageViewItem);
        }
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
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            textViewUserId = itemView.findViewById(R.id.textViewUserId);
        }
    }
}
