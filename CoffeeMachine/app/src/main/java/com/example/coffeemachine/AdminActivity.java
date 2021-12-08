package com.example.coffeemachine;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private int UID;
    private String token, URL, selectedId;
    private ListView lv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        URL = getResources().getString(R.string.API_URL);
        selectedId = "";

        lv = findViewById(R.id.lvDrinksAdmin);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedId = saveId(view);
            }
        });

        Intent intent = getIntent();
        UID = intent.getIntExtra("UID", 1);
        token = intent.getStringExtra("Token");

        updateListView();
    }

    private void updateListView() {
        ArrayList<HashMap<String, String>> drinkList = new ArrayList<>();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL + "Drinks", null, response -> {
            for (int i = 0; i < response.length(); i++){
                try {
                    String id = String.valueOf(response.getJSONObject(i).getInt("id"));
                    String name = response.getJSONObject(i).getString("name");
                    String amount = String.valueOf(response.getJSONObject(i).getInt("amountTotal"));

                    HashMap<String, String> drink = new HashMap<>();
                    drink.put("id", id);
                    drink.put("name", name + " (ID: " + id + ")");
                    drink.put("amount", "Amount: " + amount);

                    drinkList.add(drink);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // populating the list
            ListAdapter adapter = new SimpleAdapter(
                    AdminActivity.this, drinkList,
                    R.layout.drink_list_item, new String[]{"name", "amount"}, new int[]{R.id.name, R.id.amount});
            lv.setAdapter(adapter);
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("JSON ERROR", URL);
                Log.e("JSON ERROR", String.valueOf(error));
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonArrayRequest);
    }

    private String saveId(View view) {
        // extract id from name
        TextView txtName = (TextView) view.findViewById(R.id.name);
        String idInName = txtName.getText().toString();
        idInName = idInName.substring(idInName.indexOf("(")+1, idInName.indexOf(")"));
        idInName = idInName.replaceAll("[^0-9]", "");
        return idInName;
    }

    private void deleteById(String id) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, URL + "Drinks/" + id, null,
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
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("UID", UID);
        intent.putExtra("Token", token);
        startActivity(intent);
    }

    public void onNewDrinkAdmin(View view) {
        Intent intent = new Intent(this, NewDrinkActivity.class);
        intent.putExtra("UID", UID);
        intent.putExtra("Token", token);
        startActivity(intent);
    }

    public void onRemoveDrinkAdmin(View view) {
        if(selectedId.isEmpty()) Toast.makeText(this, "Nothing selected!", Toast.LENGTH_SHORT).show();
        else {
            deleteById(selectedId);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            updateListView();
        }
    }
}