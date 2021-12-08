package com.example.coffeemachine;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private String URL, token;
    private int UID;
    private ConstraintLayout mainView;
    private ListView lv;
    private JSONArray orderHistory;



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainView = findViewById(R.id.mainView);
        URL = getResources().getString(R.string.API_URL);

        lv = findViewById(R.id.lvOrders);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView txtId = (TextView) view.findViewById(R.id.id);
                String idInId = txtId.getText().toString();
                idInId = idInId.replaceAll("[^0-9]", "");

                String finalIdInId = idInId;
                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL + "Orders", null, response -> {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            int currentID = response.getJSONObject(i).getInt("id");
                            if(currentID == Integer.parseInt(finalIdInId)){
                                orderHistory = response.getJSONObject(i).getJSONArray("subOrders");
                                startActivityWithExtras();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
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
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(jsonArrayRequest);
            }
        });

        Intent intent = getIntent();
        UID = intent.getIntExtra("UID", 0);
        token = intent.getStringExtra("Token");

        ArrayList<HashMap<String, String>> orderList = new ArrayList<>();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL + "Orders", null, response -> {
            for (int i = 0; i < response.length(); i++){
                try {
                    String id = String.valueOf(response.getJSONObject(i).getInt("id"));
                    String date = response.getJSONObject(i).getString("date");

                    HashMap<String, String> order = new HashMap<>();
                    order.put("id", "Order #" + id);
                    order.put("date", "Date: " + date.substring(0, 10));

                    if(response.getJSONObject(i).getInt("userId") == UID) orderList.add(order);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // populating the list
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, orderList,
                    R.layout.order_list, new String[]{"id", "date"}, new int[]{R.id.id, R.id.date});
            lv.setAdapter(adapter);
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
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonArrayRequest);

    }

    public void startActivityWithExtras(){
        Intent intent = new Intent(this, SubOrderActivity.class);
        intent.putExtra("UID", UID);
        intent.putExtra("Token", token);
        intent.putExtra("orderHistory", orderHistory.toString());
        startActivity(intent);
    }

    // Inflating the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    // Listener for menu item options
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuAdmin:
                showDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startAdminActivity() {
        Intent intent = new Intent(this, AdminActivity.class);
        intent.putExtra("UID", UID);
        intent.putExtra("Token", token);
        startActivity(intent);
    }

    public void onNewOrder(View view) {
        Intent intent = new Intent(this, SubOrderActivity.class);
        intent.putExtra("UID", UID);
        intent.putExtra("Token", token);
        startActivity(intent);
    }

    public void showDialog()
    {

        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View promptsView = li.inflate(R.layout.password_prompt, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);


        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("Go",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String user_text = (userInput.getText()).toString();

                                if (user_text.equals("123"))
                                {
                                    startAdminActivity();
                                }
                                else{
                                    String message = "The password you have entered is incorrect." + " \n \n" + "Please try again!";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Error");
                                    builder.setMessage(message);
                                    builder.setPositiveButton("Cancel", null);
                                    builder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            showDialog();
                                        }
                                    });
                                    builder.create().show();

                                }
                            }
                        })
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.dismiss();
                            }

                        }

                );

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }
}