package com.ls.lostfound.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.ls.lostfound.R;
import java.util.List;
public class AutocompletePredictionAdapter extends RecyclerView.Adapter<AutocompletePredictionAdapter.PredictionViewHolder> {

    private List<AutocompletePrediction> predictionList;
    private EditText editTextLocation;
    private RecyclerView recyclerViewLocationSuggestions;
    public AutocompletePredictionAdapter(Context context, List<AutocompletePrediction> predictionList, EditText editTextLocation, RecyclerView recyclerViewLocationSuggestions) {
        this.predictionList = predictionList;
        this.editTextLocation = editTextLocation;
        this.recyclerViewLocationSuggestions = recyclerViewLocationSuggestions;
    }

    public class PredictionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSuggestion;

        public PredictionViewHolder(View itemView) {
            super(itemView);
            textViewSuggestion = itemView.findViewById(R.id.textViewSuggestion);
        }
    }

    @Override
    public PredictionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_autocomplete_prediction, parent, false);
        return new PredictionViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(PredictionViewHolder holder, int position) {
        AutocompletePrediction prediction = predictionList.get(position);

        holder.textViewSuggestion.setText(prediction.getFullText(null));

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                AutocompletePrediction selectedPrediction = predictionList.get(adapterPosition);
                editTextLocation.setText(selectedPrediction.getFullText(null));
                recyclerViewLocationSuggestions.setVisibility(View.GONE);
                clearPredictions(); // Clear the prediction list
            }
        });
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return predictionList.size();
    }

    public interface PredictionClickListener {
        void onPredictionClicked(AutocompletePrediction prediction);
    }

    private PredictionClickListener clickListener;

    public void setPredictionClickListener(PredictionClickListener listener) {
        this.clickListener = listener;
    }


    public void updatePredictions(List<AutocompletePrediction> newPredictions) {
        predictionList.clear();
        predictionList.addAll(newPredictions);
        notifyDataSetChanged();
    }
    public void clearPredictions() {
        predictionList.clear();
        notifyDataSetChanged();
    }




}
