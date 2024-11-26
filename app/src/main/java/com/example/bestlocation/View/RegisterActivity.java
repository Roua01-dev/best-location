package com.example.bestlocation.View;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.bestlocation.Controller.SessionManager;
import com.example.bestlocation.Controller.VolleySingleton;
import com.example.bestlocation.MainActivity;
import com.example.bestlocation.Model.User;
import com.example.bestlocation.R;
import com.example.bestlocation.Utils.Url;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText fullname, email, password;
    private Button buttonRegister, buttonLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Adjust the padding for system UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        fullname = findViewById(R.id.edfullname);
        email = findViewById(R.id.edemail);
        password = findViewById(R.id.edpassword);
        buttonRegister = findViewById(R.id.btnRegister);
        buttonLogin = findViewById(R.id.btn_Login_register);
        progressBar = findViewById(R.id.progressbarregister);

        // Register button click listener
        buttonRegister.setOnClickListener(view -> registerUser());

        // Login button click listener
        buttonLogin.setOnClickListener(view -> {
            finish();
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });
    }

    // Method to register a user
    public void registerUser() {
        final String myName = fullname.getText().toString().trim();
        final String myEmail = email.getText().toString().trim();
        final String myPassword = password.getText().toString().trim();

        // Validation checks
        if (TextUtils.isEmpty(myName)) {
            fullname.setError("Enter your name please");
            fullname.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(myEmail)) {
            email.setError("Enter your email please");
            email.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(myPassword)) {
            password.setError("Enter your password please");
            password.requestFocus();
            return;
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Create StringRequest for the network call
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Url.REGISTER_URL,
                response -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            String userJson = obj.getString("data");
                            User user = new User(userJson);
                            SessionManager.getInstance(getApplicationContext()).userLogin(user);
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Registration failed: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("RegisterActivity", "JSON Parsing error: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Error parsing server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Log.e("RegisterActivity", "Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(), "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", myName);
                params.put("email", myEmail);
                params.put("password", myPassword);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        // Add the request to the request queue
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}
