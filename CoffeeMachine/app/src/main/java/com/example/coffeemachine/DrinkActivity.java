package com.example.coffeemachine;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DrinkActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner dropdownSize, dropdownDrinks;
    private String[] spinnerSizeItems;
    private ArrayList<String> spinnerDrinkItems;
    private String URL, token;
    private int UID, size, pos, amount;
    private TextView txtDesc, txtPrice, txtAvailable, txtUnit, txtLiter;
    private EditText etAmount;
    private JSONArray json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink);

        URL = getResources().getString(R.string.API_URL);
        spinnerDrinkItems = new ArrayList<String>();
        size = 1;

        dropdownDrinks = findViewById(R.id.spinnerDrink);
        txtDesc = findViewById(R.id.txtDrinkActualDescription);
        txtPrice = findViewById(R.id.txtPrice);
        txtAvailable = findViewById(R.id.txtAvailable);
        txtUnit = findViewById(R.id.txtPriceUnit);
        txtLiter = findViewById(R.id.txtLiters);
        etAmount = findViewById(R.id.etAmount);

        txtLiter.setText(String.format(getResources().getString(R.string.total_volume), 0));

        dropdownSize = findViewById(R.id.spinnerSize);
        spinnerSizeItems = new String[]{"Regular (1 liter)", "Large (2 liters)", "Extra large (3 liters)"};
        ArrayAdapter<String> adapterSize = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, spinnerSizeItems);
        dropdownSize.setAdapter(adapterSize);
        dropdownSize.setOnItemSelectedListener(this);

        Intent intent = getIntent();
        UID = intent.getIntExtra("UID", 1);
        token = intent.getStringExtra("Token");

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL + "Drinks", null, response -> {
            json = response;
            for(int i = 0; i < response.length(); i++){
                HashMap<String, String> drink = new HashMap<>();
                try {
                    spinnerDrinkItems.add(response.getJSONObject(i).getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            ArrayAdapter<String> adapterDrink = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, spinnerDrinkItems);
            dropdownDrinks.setAdapter(adapterDrink);
            dropdownDrinks.setOnItemSelectedListener(this);

            try {
                txtDesc.setText(response.getJSONObject(0).getString("description"));
                txtPrice.setText(String.format(getResources().getString(R.string.price_of_drink), 0.0));
                txtAvailable.setText(String.format(getResources().getString(R.string.available), Integer.parseInt(response.getJSONObject(0).getString("amountTotal"))));
                txtUnit.setText(String.format(getResources().getString(R.string.unit_price), Double.parseDouble(response.getJSONObject(0).getString("price"))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("JSON ERROR", URL);
                Log.e("JSON ERROR", String.valueOf(error));
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonArrayRequest);

        etAmount.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                pos = dropdownDrinks.getSelectedItemPosition();

                //anticrash
                if(etAmount.getText().toString().isEmpty()){
                    amount = 0;
                } else {
                    amount = Integer.parseInt(etAmount.getText().toString());
                }

                try {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    txtPrice.setText(String.format(getResources().getString(R.string.price_of_drink), Double.parseDouble(json.getJSONObject(pos).getString("price")) * amount * size));
                    txtLiter.setText(String.format(getResources().getString(R.string.total_volume), size * amount));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
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
        Intent intent = new Intent(this, SubOrderActivity.class);
        intent.putExtra("UID", UID);
        intent.putExtra("Token", token);
        startActivity(intent);
    }

    public void onAddDrinkToSubOrder(View view) {
        pos = dropdownDrinks.getSelectedItemPosition();
        try {
            if(json.getJSONObject(pos).getInt("amountTotal") < (size * amount)){
                Toast.makeText(this, "Not enough available!", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(amount == 0){
            Toast.makeText(this, "Enter amount!", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject finishedDrink = new JSONObject();
        JSONObject updateDrink = new JSONObject();
        try {
            updateDrink = json.getJSONObject(pos);
            updateDrink.put("amountTotal", json.getJSONObject(pos).getInt("amountTotal") - amount * size);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, URL + "Drinks/" + json.getJSONObject(pos).getInt("id"), updateDrink,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {}
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {}
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

            finishedDrink.put("orderId", 0);
            finishedDrink.put("drinkName", json.getJSONObject(pos).getString("name"));
            finishedDrink.put("amount", amount);
            finishedDrink.put("drinkSize", size);
            finishedDrink.put("price", json.getJSONObject(pos).getDouble("price") * size * amount);

            Intent intent = new Intent(this, SubOrderActivity.class);
            intent.putExtra("UID", UID);
            intent.putExtra("Token", token);
            intent.putExtra("ordersRaw", finishedDrink.toString());
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.spinnerDrink)
        {
            JSONObject selectedObject = null;
            try {
                selectedObject = json.getJSONObject(position);
                txtDesc.setText(selectedObject.getString("description"));
                txtPrice.setText(String.format(getResources().getString(R.string.price_of_drink), Double.parseDouble(selectedObject.getString("price")) * amount * size));
                txtAvailable.setText(String.format(getResources().getString(R.string.available), Integer.parseInt(selectedObject.getString("amountTotal"))));
                txtUnit.setText(String.format(getResources().getString(R.string.unit_price), Double.parseDouble(selectedObject.getString("price"))));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else if(parent.getId() == R.id.spinnerSize)
        {
            pos = dropdownDrinks.getSelectedItemPosition();
            switch(position){
                case 0:
                    size = 1;
                break;
                case 1:
                    size = 2;
                break;
                case 2:
                    size = 3;
                break;
            }
            try {
                txtPrice.setText(String.format(getResources().getString(R.string.price_of_drink), Double.parseDouble(json.getJSONObject(pos).getString("price")) * amount * size));
                txtLiter.setText(String.format(getResources().getString(R.string.total_volume), size * amount));
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "ERROR!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}