package com.example.bestlocation.Controller;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.bestlocation.MainActivity;
import com.example.bestlocation.Model.Position;
import com.example.bestlocation.R;
import com.example.bestlocation.Utils.Url;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

        holder.ivDelete.setOnClickListener(v -> {
            deletePosition(currentPosition, position);
        });
    }

    // Méthode pour supprimer un élément
    private void deletePosition(Position position, int adapterPosition) {
        // URL de suppression
        String url = Url.ALL_DATA_URL + "/" + position.getId();

        // Récupération du token
        final String token = SessionManager.getInstance(context).getToken().getToken();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            // Suppression réussie
                            positionList.remove(adapterPosition);
                            notifyItemRemoved(adapterPosition);
                            Toast.makeText(context, "Position supprimée avec succès", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Erreur: " + response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Erreur lors de la réponse JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Affiche un toast en cas d'erreur réseau ou serveur
                    Toast.makeText(context, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        // Ajout de la requête à la file d'attente
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }



    @Override
    public int getItemCount() {
        return positionList != null ? positionList.size() : 0;
    }

    public static class PositionViewHolder extends RecyclerView.ViewHolder {
        TextView tvPseudo, tvNumero, tvAddress;
        ImageView ivDelete;

        public PositionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPseudo = itemView.findViewById(R.id.tvPseudo);
            tvNumero = itemView.findViewById(R.id.tvNumero);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            ivDelete = itemView.findViewById(R.id.ivDelete);

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

