package com.example.bestlocation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.bestlocation.Controller.PositionAdapter;
import com.example.bestlocation.Controller.SessionManager;
import com.example.bestlocation.Controller.VolleySingleton;
import com.example.bestlocation.Model.Position;
import com.example.bestlocation.Utils.Url;
import com.example.bestlocation.View.LoginActivity;
import com.example.bestlocation.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private RequestQueue queue;
    private String token;

    private Button btnLogout , btnMaps;
    private RecyclerView recyclerView;
    private PositionAdapter positionAdapter;
    private List<Position> positionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge content display
        EdgeToEdge.enable(this);

        // Set the content view
        setContentView(R.layout.activity_main);


       /* btnLogout=findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SessionManager.getInstance(MainActivity.this).UserloginOut();
                finish();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));

            }
        });


        btnMaps=findViewById(R.id.btn_maps);
        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(MainActivity.this, MapsActivity.class));

            }
        });
*/


        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the Volley Request Queue
        queue = Volley.newRequestQueue(this);

        // Check if user is logged in
        if (!SessionManager.getInstance(this).isLoggedIn()) {
            redirectToLogin();
        } else {
            token = SessionManager.getInstance(this).getToken() != null ?
                    SessionManager.getInstance(this).getToken().getToken() : null;
        }

        positionList = new ArrayList<>();


        // Configurer le RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        positionAdapter = new PositionAdapter(positionList, this);
        recyclerView.setAdapter(positionAdapter);
        // Charger les données (à implémenter selon votre logique)
        loadData();

        // Handle system window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadData() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        Log.d("MainActivity", "Fetching data from: " + Url.ALL_DATA_URL);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Url.ALL_DATA_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                        try {
                            Log.d("MainActivity", "Response received: " + response.toString());

                            JSONArray placeArray = response.getJSONArray("data");
                            Log.d("MainActivity", "placeArray: " + placeArray);

                            positionList.clear();

                            for (int i = 0; i < placeArray.length(); i++) {
                                JSONObject placeObj = placeArray.getJSONObject(i);
                                Position place = new Position();
                                place.setId(placeObj.getInt("id"));
                                place.setPseudo(placeObj.getString("pseudo"));
                                place.setNumero(placeObj.getString("numero"));
                                place.setLatitide(placeObj.getString("latitude"));
                                place.setLongitude(placeObj.getString("longitude"));
                                positionList.add(place);

                                Log.d("MainActivity", "Position added: " + place.toString());
                            }

                            positionAdapter.notifyDataSetChanged();
                            Log.d("MainActivity", "Positions fetched: " + positionList.size());
                            Toast.makeText(MainActivity.this, "Data fetched successfully!", Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Log.e("MainActivity", "JSON Exception: " + e.getMessage());
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "JSON Parsing Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        if (error.networkResponse != null) {
                            Log.e("MainActivity", "Error response code: " + error.networkResponse.statusCode);
                            Log.e("MainActivity", "Error response data: " + new String(error.networkResponse.data));
                        } else {
                            Log.e("MainActivity", "Error response: " + error.getMessage());
                        }
                        Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Accept", "application/json");
                params.put("Authorization", "Bearer " + token);
                Log.d("MainActivity", "Headers set: " + params);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }











    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_maps:
                // Ouvrir l'activité Maps
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
                return true;

            case R.id.action_logout:
                // Déconnexion
                SessionManager.getInstance(MainActivity.this).UserloginOut();
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Redirect to LoginActivity if the user is not logged in
    private void redirectToLogin() {
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }
}
