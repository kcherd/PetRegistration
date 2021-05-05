package com.example.petreg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VaccineAdapter extends RecyclerView.Adapter<VaccineAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<Pet.Vaccination> vaccinations;

    public VaccineAdapter(Context context, List<Pet.Vaccination> vaccinations) {
        this.vaccinations = vaccinations;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public VaccineAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VaccineAdapter.ViewHolder holder, int position) {
        Pet.Vaccination vaccination = vaccinations.get(position);
        holder.nameVacTV.setText(vaccination.getName());
        holder.dateVacTV.setText((CharSequence) vaccination.getDate());
    }

    @Override
    public int getItemCount() {
        return vaccinations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        final TextView nameVacTV, dateVacTV;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.nameVacTV = (TextView) view.findViewById(R.id.name_vac_list_item);
            this.dateVacTV = (TextView) view.findViewById(R.id.date_vac_list_item);
        }
    }
}
