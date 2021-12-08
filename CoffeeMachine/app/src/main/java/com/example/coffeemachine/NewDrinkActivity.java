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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NewDrinkActivity extends AppCompatActivity {

    private int UID;
    private String token, URL;
    private EditText etName, etDesc, etAmount, etPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_drink);

        URL = getResources().getString(R.string.API_URL);

        etName = findViewById(R.id.etNewName);
        etDesc = findViewById(R.id.etNewDesc);
        etAmount = findViewById(R.id.etNewAmount);
        etPrice = findViewById(R.id.etNewPrice);

        Intent intent = getIntent();
        UID = intent.getIntExtra("UID", 1);
        token = intent.getStringExtra("Token");
    }

    @Override
    public void onBackPressed() {
        startActivityWithExtras();
    }

    @Override
    public boolean onSupportNavigateUp() {
        startActivityWithExtras();
        return false;
    }

    private void startActivityWithExtras() {
        Intent intent = new Intent(this, AdminActivity.class);
        intent.putExtra("UID", UID);
        intent.putExtra("Token", token);
        startActivity(intent);
    }

    public void onAddNewDrink(View view) {
        //input validation
        if(etName.getText().toString().isEmpty() || etDesc.getText().toString().isEmpty() || etAmount.getText().toString().isEmpty()|| etPrice.getText().toString().isEmpty()){
            Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject object = new JSONObject();
        try {
            object.put("amountTotal", Integer.parseInt(etAmount.getText().toString()));
            object.put("price", Float.parseFloat(etPrice.getText().toString()));
            object.put("name", etName.getText().toString());
            object.put("description", etDesc.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL + "Drinks", object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        startActivityWithExtras();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error", "Error: " + error.getMessage());
                Toast.makeText(NewDrinkActivity.this, "Error!!!", Toast.LENGTH_SHORT).show();
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
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonObjectRequest);
    }
}