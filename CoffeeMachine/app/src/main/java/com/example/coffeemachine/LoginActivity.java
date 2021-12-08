package com.example.coffeemachine;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private String URL, txtEmail, txtPassword, token;
    private int UID;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        URL = getResources().getString(R.string.API_URL);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    public void onRegister(View view) {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }

    public void onLogin(View view) {
        txtEmail = etEmail.getText().toString();
        txtPassword = etPassword.getText().toString();

        /*======================*/

        JSONObject object = new JSONObject();
        try {
            //input your API parameters
            object.put("email", txtEmail);
            object.put("password", txtPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Enter the correct url for your api service site
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL + "AuthManagement/Login", object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            loginSequence(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error", "Error: " + error.getMessage());
                Toast.makeText(LoginActivity.this, "Error!!!", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);

        /*======================*/
    }

    private void loginSequence(JSONObject res) throws JSONException {
        token = res.getString("token");
        if(token == "null"){
            Toast.makeText(LoginActivity.this, "ERROR: No token!", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL + "Accounts", null, response -> {

            for (int i = 0; i < response.length(); i++){
                try {
                    if(response.getJSONObject(i).getString("email").equals(txtEmail)){
                        UID = response.getJSONObject(i).getInt("id");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("UID", UID);
            intent.putExtra("Token", token);
            startActivity(intent);
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("JSON ERROR", URL);
                Log.e("JSON ERROR", String.valueOf(error));
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        requestQueue.add(jsonArrayRequest);
    }
}