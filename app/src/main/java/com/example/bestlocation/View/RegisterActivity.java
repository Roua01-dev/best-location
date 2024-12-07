package com.example.bestlocation.View;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText fullname, email, password;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullname = findViewById(R.id.edfullname);
        email = findViewById(R.id.edemail);
        password = findViewById(R.id.edpassword);
        progressBar = findViewById(R.id.progressbarregister);

        findViewById(R.id.btnRegister).setOnClickListener(view -> registerUser());
        findViewById(R.id.btn_Login_register).setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String myName = fullname.getText().toString().trim();
        String myEmail = email.getText().toString().trim();
        String myPassword = password.getText().toString().trim();

        if (TextUtils.isEmpty(myName)) {
            fullname.setError("Enter your full name");
            return;
        }
        if (TextUtils.isEmpty(myEmail)) {
            email.setError("Enter your email");
            return;
        }
        if (TextUtils.isEmpty(myPassword)) {
            password.setError("Enter your password");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Url.REGISTER_URL,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                            SessionManager.getInstance(this).userLogin(new User(obj.getString("data")));
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("RegisterActivity", "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("RegisterActivity", "Network error: " + error.getMessage());
                    Toast.makeText(this, "Network error. Try again.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", myName);
                params.put("email", myEmail);
                params.put("password", myPassword);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}
