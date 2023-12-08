package models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.ls.lostfound.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import com.ls.lostfound.PostFragment;
import com.ls.lostfound.models.LostAndFoundItem;
import com.squareup.picasso.Picasso;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> implements Filterable {
        private List<LostAndFoundItem> itemList;
        private List<LostAndFoundItem> itemListFull; // This list will hold all the data, for search purposes

        // Constructor
        public SearchAdapter(List<LostAndFoundItem> itemList) {
            this.itemList = itemList;
            itemListFull = new ArrayList<>(itemList); // Create a copy for filtering
        }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lost_and_found, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LostAndFoundItem currentItem = itemList.get(position);
        holder.textViewItemName.setText(currentItem.getItemName());
        holder.textViewItemDescription.setText(currentItem.getDescription());
            Picasso.get()
                .load(currentItem.getFirebaseImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image) // if the image fails to load
                .into(holder.imageViewItem); // replace with your ImageView


    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewItemName;
        public TextView textViewItemDescription;
        public ImageView imageViewItem;
        public ViewHolder(View itemView) {
            super(itemView);
            textViewItemName = itemView.findViewById(R.id.textViewItemName);
            textViewItemDescription = itemView.findViewById(R.id.textViewItemDescription);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);

        }
    }

        // Filterable Interface
        @Override
        public Filter getFilter() {
            return itemFilter;
        }

        private Filter itemFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<LostAndFoundItem> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(itemListFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (LostAndFoundItem item : itemListFull) {
                        // Check if the item's name or description match the filter pattern
                        if (item.getItemName().toLowerCase().contains(filterPattern) ||
                                item.getDescription().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                itemList.clear();
                itemList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };

}
