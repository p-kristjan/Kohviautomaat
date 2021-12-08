package com.example.coffeemachine;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SubOrderActivity extends AppCompatActivity {

    SharedPreferences mPreferences;
    public static final String sharedPrefFile = "com.example.coffeemachine";
    private final String jsonArrayKey = "drinksJSONArray";

    private Spinner dropdown;
    private String[] spinnerItems;
    private int UID, selectedId, orderId;
    private double price;
    private String token, drinksStr, URL;
    private TextView txtTotalPrice;
    private JSONObject drink, json;
    private JSONArray drinks;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_order);

        selectedId = 10000;
        URL = getResources().getString(R.string.API_URL);

        lv = findViewById(R.id.lvDrinks);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedId = position;
            }
        });

        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        drinksStr = mPreferences.getString(jsonArrayKey, "[]");
        try {
            drinks = new JSONArray(drinksStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        dropdown = findViewById(R.id.spinnerPaymentType);
        spinnerItems = new String[]{"Card", "Cash"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, spinnerItems);
        dropdown.setAdapter(adapter);

        price = 0;
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        txtTotalPrice.setText(String.format(getResources().getString(R.string.total_price), String.valueOf(price)));

        Intent intent = getIntent();
        UID = intent.getIntExtra("UID", 0);
        token = intent.getStringExtra("Token");
        if(getIntent().hasExtra("ordersRaw")){
            try {
                drink = new JSONObject(intent.getStringExtra("ordersRaw"));
                drinks.put(drink);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(getIntent().hasExtra("orderHistory")){
            try {
                drinks = new JSONArray(intent.getStringExtra("orderHistory"));
                Log.e("debuggy", String.valueOf(drinks));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        updateListView();

        for(int i = 0; i < drinks.length(); i++){
            try {
                price = drinks.getJSONObject(i).getDouble("price") + price;
                txtTotalPrice.setText(String.format(getResources().getString(R.string.total_suborders), price));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateListView() {
        ArrayList<HashMap<String, String>> drinkList = new ArrayList<>();

        for (int i = 0; i < drinks.length(); i++){
            try {
                String name = drinks.getJSONObject(i).getString("drinkName");
                String amount = String.valueOf(drinks.getJSONObject(i).getInt("amount"));
                String size = String.valueOf(drinks.getJSONObject(i).getInt("drinkSize"));
                String price = String.valueOf(drinks.getJSONObject(i).getDouble("price"));

                HashMap<String, String> drink = new HashMap<>();
                drink.put("name", name);

                drink.put("amount", "Volume: " + Integer.parseInt(amount) * Integer.parseInt(size) + " liters");
                drink.put("price", String.format(getString(R.string.price_for_list_item), Double.parseDouble(price)));

                drinkList.add(drink);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // populating the list
        ListAdapter adapter = new SimpleAdapter(
                SubOrderActivity.this, drinkList,
                R.layout.suborder_list, new String[]{"name", "amount", "price"}, new int[]{R.id.name, R.id.amount, R.id.price});
        lv.setAdapter(adapter);
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
        try {
            drinks = new JSONArray("[]");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("UID", UID);
        intent.putExtra("Token", token);
        startActivity(intent);
    }

    public void onAddDrink(View view) {
        Intent intent = new Intent(this, DrinkActivity.class);
        intent.putExtra("UID", UID);
        intent.putExtra("Token", token);
        startActivity(intent);
    }

    public void onRemoveDrink(View view) {
        if(selectedId == 10000) Toast.makeText(this, "Nothing selected!", Toast.LENGTH_SHORT).show();
        else{
            drinks.remove(selectedId);
            updateListView();
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCheckout(View view) {
        //input validation
        if(drinks.toString().equals("[]")){
            Toast.makeText(this, "No drinks!", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject object = new JSONObject();
        try {
            object.put("userId", UID);
            object.put("paymentType", dropdown.getSelectedItem().toString());
            object.put("date", String.valueOf(LocalDate.now()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL + "Orders", object,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        orderId = response.getInt("id");

                        for(int i = 0; i < drinks.length(); i++){
                            drinks.getJSONObject(i).put("orderId", orderId);
                        }

                        for(int i = 0; i < drinks.length(); i++){
                            JSONObject drinkObject = drinks.getJSONObject(i);
                            drinkObject.remove("id");
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL + "SubOrders", drinkObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    startActivityWithExtras();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    VolleyLog.d("Error", "Error: " + error.getMessage());
                                    Toast.makeText(SubOrderActivity.this, "Error!!!", Toast.LENGTH_SHORT).show();
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

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error", "Error: " + error.getMessage());
                Toast.makeText(SubOrderActivity.this, "Error!!!", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onPause() {
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putString(jsonArrayKey, drinks.toString());
        preferencesEditor.apply();
        super.onPause();
    }
}