package com.example.games;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //42 globalna premenna pre handler volani JSON
    private RequestQueue mQueue;
    private ArrayList<Game> games = new ArrayList<Game>();
    private Pages pages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        pages = new Pages("null", "null");
        //42 init handleru s tym ze tento si pripravy frontu na volania
        mQueue = Volley.newRequestQueue(this);
        jsonParse();
    }
    private void jsonParse() {
       // refreshLayout.setRefreshing(true);
        StringBuilder sb1 = new
                StringBuilder("https://rawg.io/api/games");
        String url;
       // sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //String name = sharedPreferences.getString("category", "");
        sb1.append("?page_size=8");
        url = sb1.toString();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    pages = new Pages(response.getString("next"), response.getString("previous"));
                   JSONArray results = response.getJSONArray("results");
                    if(results.length()>0){
                        for (int i = 0; i < results.length(); i++) {
                            ArrayList<String> screenshots = new ArrayList<String>();
                            ArrayList<String> platforms = new ArrayList<String>();

                            JSONObject game = results.getJSONObject(i);

                            JSONArray short_screenshots = (JSONArray) results.getJSONObject(i).get("short_screenshots");
                            JSONArray parent_platforms = (JSONArray) results.getJSONObject(i).get("parent_platforms");

                            for (int j = 0; j<short_screenshots.length(); j++){
                                JSONObject screen = short_screenshots.getJSONObject(j);
                                screenshots.add(screen.getString("image"));
                            }

                            for (int j = 0; j<parent_platforms.length(); j++){
                                JSONObject platform = parent_platforms.getJSONObject(j);
                                platforms.add(platform.getJSONObject("platform").getString("name"));
                            }

                            Game adding = new Game(
                                    game.getString("id"),
                                    game.getString("name"),
                                    game.getString("background_image"),
                                    game.getString("rating"),
                                    platforms,
                                    screenshots,
                                    game.getString("released")
                            );
                           games.add(adding);
                        }
                        Toast.makeText(getApplicationContext(),"Games succesfully loaded",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"Error at games feed",Toast.LENGTH_SHORT).show();
                    }
//                    addAndNotify();

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"Error at games feed",Toast.LENGTH_SHORT).show();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Error at games feed",Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });
        mQueue.add(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
