package com.example.bestlocation.View;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bestlocation.MainActivity;
import com.example.bestlocation.MapsActivity;
import com.example.bestlocation.R;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bestlocation.R;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.bestlocation.R;
import com.example.bestlocation.Utils.Url;
import com.example.bestlocation.Controller.SessionManager;
import com.example.bestlocation.Controller.VolleySingleton;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddPositionActivity extends AppCompatActivity {



    private TextView tvLatitde,tvLongitude;
    private EditText edpseudo,edtelephone,edaddress;
    private Button btnSave;
    double latitude,longitude;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_position);


       // tvLatitde=findViewById(R.id.latitude);
      //  tvLongitude=findViewById(R.id.longitude);
        edaddress=findViewById(R.id.edaddress);
        edpseudo=findViewById(R.id.edpseudo_add);
        edtelephone=findViewById(R.id.ednumero_add);
        btnSave=findViewById(R.id.Savebtn);




             latitude = getIntent().getDoubleExtra("latitude", 0.0);
             longitude = getIntent().getDoubleExtra("longitude", 0.0);


        String address = getAddressFromLatLng(latitude, longitude);
        edaddress.setText(address);
        extras = getIntent().getExtras();



        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveData();
                //redirection
                Intent intent = new Intent(AddPositionActivity.this, MainActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                finish();
            }
        });








        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }






    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getAddressLine(0); // Return the first address line
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Address not found";
    }
    private void   saveData() {

        final String token = SessionManager.getInstance(this).getToken().getToken();


        final String pseudo = edpseudo.getText().toString().trim();
        final String numero = edtelephone.getText().toString().trim();






        if (TextUtils.isEmpty(pseudo)) {
            edpseudo.setError("Please enter your pseudo");
            edpseudo.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(numero)) {
            edtelephone.setError("Please enter your numero");
            edtelephone.requestFocus();
            return;
        }




        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();


        JSONObject postparams = new JSONObject();
        try {

            postparams.put("pseudo", pseudo);
            postparams.put("numero", numero);
            postparams.put("latitude", latitude);
            postparams.put("longitude", longitude);
        } catch (JSONException e) {

            e.getMessage();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest( Request.Method.POST
                , Url.ALL_DATA_URL, postparams, new Response.Listener<JSONObject>() {


            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getBoolean("success") ) {
                        Toast.makeText(AddPositionActivity.this,
                                response.getString("message"), Toast.LENGTH_SHORT).show();

                        finish();
                        startActivity(new Intent(AddPositionActivity.this, MapsActivity.class));
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            @Override
            public Map<String, String> getHeaders()  {
                Map<String, String> params = new HashMap<>();
                params.put("Accept", "application/json");
                params.put("Authorization", "Bearer  "+token);
                return params;
            }
        };


        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

    }




}
