package com.example.bestlocation;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.bestlocation.Controller.SessionManager;
import com.example.bestlocation.Controller.VolleySingleton;
import com.example.bestlocation.Model.Position;
import com.example.bestlocation.Model.User;
import com.example.bestlocation.Utils.Url;
import com.example.bestlocation.View.LoginActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.bestlocation.databinding.ActivityMapsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import android.location.Location;

import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;


//OnMapReadyCallback: pour configurer la carte une fois qu'elle est prête
//OnMarkerClickListener: pour gérer les clics sur les marqueurs.
//OnMapLongClickListener: pour gérer les clics prolongés sur la carte
//OnCameraIdleListener: pour détecter quand la caméra est immobile après un déplacement
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMapClickListener {


    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private Handler handler = new Handler();
    private Runnable locationUpdateTask;
    private static final long LOCATION_UPDATE_INTERVAL = 60000; // 1 minute (en millisecondes)

    private String userName;

    private GoogleMap mMap;
    private String token;
    private List<Position> placeList = new ArrayList<>();
    private List<Marker> markerList = new ArrayList<>();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        token = getTokenFromSession();
        if (token == null) {
            return; // Exit if token is null
        }

        userName = getUsernameFromSession();


        // Initialisation du client de localisation
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Vérifiez les permissions avant d'activer la localisation actuelle
        checkLocationPermission();
        // Initialisation de la carte
        initializeMap();

        // Lancer les mises à jour de la localisation
        startLocationUpdates();
    }


    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableCurrentLocation();
        }
    }


    private void enableCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && mMap != null) {
            mMap.setMyLocationEnabled(true);

            // Obtenez la dernière localisation connue
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                                Toast.makeText(MapsActivity.this, "Vous êtes ici!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    private String getUsernameFromSession() {
        sessionManager = SessionManager.getInstance(this);
        if (sessionManager.isLoggedIn()) {
            // Récupérer le nom de l'utilisateur
            User user = sessionManager.getUser(new User());
            return user.getName(); // Retourner le nom de l'utilisateur
        } else {
            // Si l'utilisateur n'est pas connecté, rediriger vers la page de connexion
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return null; // Retourner null si l'utilisateur n'est pas connecté
        }
    }

    // Récupération du token de session
    private String getTokenFromSession() {
        sessionManager = SessionManager.getInstance(this);
        if (sessionManager.isLoggedIn()) {
            return sessionManager.getToken() != null ? sessionManager.getToken().getToken() : null;
        } else {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return null;
        }
    }

    // Initialisation de la carte
    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("MapsActivity", "Map fragment is null");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Activer les paramètres de la carte
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Vérifiez et activez la localisation
        checkLocationPermission();
        fetchData(); // Charger les données et ajouter des marqueurs
        mMap.setOnMapClickListener(this);


    }


    //effectuer une requête HTTP GET pour récupérer des données JSON
    public void fetchData() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Url.ALL_DATA_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                        handleResponse(response); // Traiter la réponse
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        handleError(error); // Traiter l'erreur
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                //client (l'application) souhaite recevoir les données au format JSON
                params.put("Accept", "application/json");
                /*Ajoute un token d'authentification à la requête pour
                s'assurer que seules les requêtes
                 autorisées accèdent à l'API**/
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    // Gérer la réponse JSON et extraire les positions
    private void handleResponse(JSONObject response) {
        try {
            Log.d("DataFetcher", "Response received: " + response.toString());

            JSONArray placeArray = response.getJSONArray("data");
            Log.d("DataFetcher", "Response received placeArray: " + placeArray);

            placeList.clear();

            for (int i = 0; i < placeArray.length(); i++) {
                JSONObject placeObj = placeArray.getJSONObject(i);
                Position place = new Position();
                place.setId(placeObj.getInt("id"));
                place.setPseudo(placeObj.getString("pseudo"));
                place.setNumero(placeObj.getString("numero"));
                place.setLatitide(placeObj.getString("latitude"));
                place.setLongitude(placeObj.getString("longitude"));
                placeList.add(place);
            }
            // Vérifier si la liste de positions est bien remplie
            Log.d("DataFetcher", "Positions fetched: " + placeList.size());


            addMarkersToMap();
            Toast.makeText(this, "Data fetched successfully!", Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            Log.e("DataFetcher", "JSON Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Gérer les erreurs lors de la requête
    private void handleError(VolleyError error) {
        Log.e("DataFetcher", "Error response: " + error.getMessage());
        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
    }

    // Ajouter les marqueurs sur la carte
    private void addMarkersToMap() {
        if (mMap == null || placeList.isEmpty()) return;

        mMap.clear(); // Effacer les anciens marqueurs

        Log.d("DataFetcher", "Positions marker: " + placeList.size());

        for (Position place : placeList) {
            LatLng position = new LatLng(Double.parseDouble(place.getLatitide()), Double.parseDouble(place.getLongitude()));
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(place.getPseudo())
                    .snippet("Numéro: " + place.getNumero())
            );
            markerList.add(marker);
        }

        // Déplacer la caméra vers le premier point
        LatLng firstPosition = new LatLng(Double.parseDouble(placeList.get(0).getLatitide()), Double.parseDouble(placeList.get(0).getLongitude()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPosition, 10));
    }

    @Override
    public void onCameraIdle() {

    }


    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        return false; // Return false to allow the default behavior of the marker (e.g., showing info window)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableCurrentLocation();
            } else {
                Toast.makeText(this, "Permission refusée pour la localisation!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        locationUpdateTask = new Runnable() {
            @Override
            public void run() {
                updateCurrentLocation();
                handler.postDelayed(this, LOCATION_UPDATE_INTERVAL); // Relancer après l'intervalle
            }
        };
        handler.post(locationUpdateTask); // Démarrer immédiatement
    }

    private void stopLocationUpdates() {
        if (locationUpdateTask != null) {
            handler.removeCallbacks(locationUpdateTask); // Arrêter les mises à jour
        }
    }

    private void updateCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                // Déplacer la caméra vers la nouvelle position
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                                Toast.makeText(MapsActivity.this, "Position mise à jour!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }


    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        // Ajoutez un marqueur à l'endroit où l'utilisateur a cliqué
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(userName)
                .snippet("Latitude: " + latLng.latitude + ", Longitude: " + latLng.longitude));
        marker.setTag("Some data related to the marker");

        // Vous pouvez aussi déplacer la caméra pour centrer le marqueur
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

    }
}


//ajouter manuelment une position depuis le maps
//vous pouvez supprimer  position marker