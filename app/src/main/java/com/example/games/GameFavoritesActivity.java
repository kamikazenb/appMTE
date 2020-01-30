package com.example.games;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GameFavoritesActivity extends AppCompatActivity {
    private RequestQueue mQueue;

    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    LinearLayoutManager mLayoutManager;
    ArrayList<Game> games;
    ArrayList<String> favorites;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle("Favorite games");

        }catch (Exception e){

        }

        setContentView(R.layout.activity_game_favorites);
                games = new ArrayList<Game>();
        mQueue = Volley.newRequestQueue(this);

        initialRecycleItems();
        Intent mIntent = getIntent();
        favorites = mIntent.getStringArrayListExtra("favorites");


        jsonParse();


    }

    public void returnCall() {
        Intent intent = new Intent();
        setResult(888, intent);
        finish();
    }
    private void initialRecycleItems() {
        recyclerView = findViewById(R.id.recycle_view_fav);
        adapter = new RecyclerViewAdapter(this, games);
        recyclerView.setAdapter(adapter);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        int a = 5;
        String id = "";
        boolean favorite = false;
        int no = 0;
        if(resultCode==777){
            try {
                id = data.getStringExtra("id");
                no = data.getIntExtra("no",0);
                favorite = data.getBooleanExtra("favorite", false);
                if(favorite){
                    favorites.add(id);
                }else{
                    for(int i = 0; i<favorites.size(); i++){
                        if(favorites.get(i).equals(id)){
                            favorites.remove(i);
                            break;
                        }
                    }
                }
                games.get(no).setFavorite(favorite);
                adapter.notifyDataSetChanged();
            }catch (Exception e){

            }
        }else{

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        returnCall();
    }
    private void jsonParse() {

        String url;
        for(int fa = 0; fa<favorites.size(); fa++){
            StringBuilder sb1 = new
                    StringBuilder("https://rawg.io/api/games/");
            sb1.append(favorites.get(fa));
            url = sb1.toString();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray parent_platforms = (JSONArray) response.get("parent_platforms");
                        ArrayList<String> platforms = new ArrayList<String>();
                        for (int j = 0; j<parent_platforms.length(); j++){
                            JSONObject platform = parent_platforms.getJSONObject(j);
                            platforms.add(platform.getJSONObject("platform").getString("name"));
                        }
                        ArrayList<String> screenshots = new ArrayList<String>();
                        screenshots.add("null");
                        Game adding = new Game(
                                response.getString("id"),
                                response.getString("name"),
                                response.getString("background_image"),
                                response.getString("rating"),
                                platforms,
                                screenshots,
                                response.getString("released")
                        );
                        adding.setFavorite(true);
                        games.add(adding);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                        } else {

                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Error at games feed", Toast.LENGTH_SHORT).show();

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Error at games feed", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
            });

            mQueue.add(request);


        }



    }
}
