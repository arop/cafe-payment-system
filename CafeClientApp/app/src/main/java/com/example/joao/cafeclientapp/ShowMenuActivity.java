package com.example.joao.cafeclientapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ShowMenuActivity extends AppCompatActivity {

    private Context context;
    final ArrayList<JSONObject> list = new ArrayList<JSONObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        context = getApplicationContext();

        final ListView listview = (ListView) findViewById(R.id.menu_list_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });



        ServerRestClient.get("menu", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("success", "got menu");
                try{
                    JSONArray menu = (JSONArray) response.get("menu");
                    for (int i = 0; i < menu.length(); ++i) {
                        list.add(menu.getJSONObject(i));
                    }
                    Log.i("list size", list.size()+"#");
                    MenuItemAdapter adapter = new MenuItemAdapter(context, list);
                    listview.setAdapter(adapter);

                    /*listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, final View view,
                                                int position, long id) {
                            final String item = (String) parent.getItemAtPosition(position);
                            view.animate().setDuration(2000).alpha(0)
                                    .withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            list.remove(item);
                                            adapter.notifyDataSetChanged();
                                            view.setAlpha(1);
                                        }
                                    });
                        }

                    });*/

                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable){
                Log.e("FAILURE:", error);
                Toast.makeText(context, "Server not available...", Toast.LENGTH_SHORT).show();
            }

        });


    }
}
