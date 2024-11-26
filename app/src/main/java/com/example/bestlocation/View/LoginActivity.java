package com.example.bestlocation.View;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private Button buttonLogin, buttonRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Adjust the padding for system UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        email = findViewById(R.id.email_login);
        password = findViewById(R.id.passwordLogin);
        buttonLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        buttonRegister = findViewById(R.id.btn_register_login);

        // Register button click listener
        buttonRegister.setOnClickListener(view -> {
            finish();
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Login button click listener
        buttonLogin.setOnClickListener(view -> userLogin());
    }

    // Method to log in the user
    private void userLogin() {
        final String myEmail = email.getText().toString().trim();
        final String myPassword = password.getText().toString().trim();

        // Validation checks
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
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Url.LOGIN_URL, response -> {
            progressBar.setVisibility(View.INVISIBLE);
            try {
                JSONObject obj = new JSONObject(response);
                if (obj.getBoolean("success")) {
                    Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                    String userJson = obj.getString("data");

                    User user = new User(userJson);
                    SessionManager.getInstance(getApplicationContext()).userLogin(user);
                    finish();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        },
                error -> {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", myEmail);
                params.put("password", myPassword);
                return params;
            }
        };

        // Add the request to the request queue
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}
