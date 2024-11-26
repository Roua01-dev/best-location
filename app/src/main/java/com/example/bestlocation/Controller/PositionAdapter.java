package com.example.bestlocation.Controller;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bestlocation.MainActivity;
import com.example.bestlocation.Model.Position;
import com.example.bestlocation.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.PositionViewHolder> {
    private List<Position> positionList;
    private Context context;

    public PositionAdapter(List<Position> positionList, Context context) {
        this.positionList = positionList;
        this.context = context;
    }

    @NonNull
    @Override
    public PositionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_position, parent, false);
        return new PositionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PositionViewHolder holder, int position) {
        Position currentPosition = positionList.get(position);
        holder.tvPseudo.setText(currentPosition.getPseudo());
        holder.tvNumero.setText(currentPosition.getNumero());

        // Convertir latitude et longitude en adresse
        String address = getAddressFromLatLng(currentPosition.getLatitide(), currentPosition.getLongitude());
        holder.tvAddress.setText(address);
    }

    @Override
    public int getItemCount() {
        return positionList != null ? positionList.size() : 0;
    }

    public static class PositionViewHolder extends RecyclerView.ViewHolder {
        TextView tvPseudo, tvNumero, tvAddress;

        public PositionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPseudo = itemView.findViewById(R.id.tvPseudo);
            tvNumero = itemView.findViewById(R.id.tvNumero);
            tvAddress = itemView.findViewById(R.id.tvAddress);
        }
    }

    private String getAddressFromLatLng(String lat, String lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            double latitude = Double.parseDouble(lat);
            double longitude = Double.parseDouble(lng);
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getAddressLine(0);
            } else {
                return "Adresse non trouvée";
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return "Erreur lors de la conversion";
        }
    }
}

