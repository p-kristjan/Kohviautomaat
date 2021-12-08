package com.example.coffeemachine;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPw1, etPw2;
    private String URL, txtEmail, txtPassword, token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        URL = getResources().getString(R.string.API_URL);
        etEmail = findViewById(R.id.etEmail);
        etPw1 = findViewById(R.id.etPassword1);
        etPw2 = findViewById(R.id.etPassword2);
    }

    public void onRegister(View view) {
        // Input validation
        if(etEmail.getText().toString().isEmpty() ||
                etPw1.getText().toString().isEmpty() ||
                etPw2.getText().toString().isEmpty() ||
                !etPw1.getText().toString().equals(etPw2.getText().toString())){
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            return;
        }

        txtEmail = etEmail.getText().toString();
        txtPassword = etPw1.getText().toString();

        /*======================*/

        JSONObject object = new JSONObject();
        try {
            object.put("email", txtEmail);
            object.put("password", txtPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL + "AuthManagement/Register", object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error", "Error: " + error.getMessage());
                Toast.makeText(RegisterActivity.this, "Error!!!", Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonObjectRequest);

        /*======================*/
    }
}