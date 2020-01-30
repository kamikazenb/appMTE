package com.example.games;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.makeramen.roundedimageview.RoundedImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GameDetailActivity extends AppCompatActivity {
    private RequestQueue mQueue;


    private int no;
    private String id;
    private TextView rating;
    private ArrayList<String> platforms;

    private ArrayList<String> short_screenshots;
    private String background_image;
    private boolean favorite;
    //dalsie
    private String description;
    RoundedImageView articleImage;
    private TextView descriptionTextView;
    private TextView platformsnTextView;
    private TextView releasedTextView;
    FloatingActionButton fab;
    boolean changed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changed = true;
                if(favorite){
                    favorite = false;
                    Snackbar.make(view, "Removed from favorite", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    fab.setImageResource(android.R.drawable.btn_star_big_off);
                }else {
                    favorite = true;
                    Snackbar.make(view, "Added to favorite", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    fab.setImageResource(android.R.drawable.btn_star_big_on);
                }

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mQueue = Volley.newRequestQueue(this);

        descriptionTextView  = findViewById(R.id.gameText);
        articleImage = findViewById(R.id.gameBackgroundImage);
        platformsnTextView = findViewById(R.id.gamePlatforms);
        releasedTextView = findViewById(R.id.gameReleased);
        rating = findViewById(R.id.gameRating);

        Intent mIntent = getIntent();
        no = mIntent.getIntExtra("no", 0);
        String dummy;
        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbarLayout.setTitle(mIntent.getStringExtra("name"));

        id = mIntent.getStringExtra("id");
        short_screenshots = mIntent.getStringArrayListExtra("short_screenshots");

        dummy = "Rating: " +mIntent.getStringExtra("rating")+"/5";
        rating.setText(dummy);

        platforms = mIntent.getStringArrayListExtra("platforms");
        StringBuilder sb1 = new StringBuilder("Platforms: ");
        for (int i = 0; i<platforms.size(); i++){
            sb1.append(" ");
            sb1.append(platforms.get(i));
            sb1.append(",");
        }
        sb1.setLength(sb1.length() - 1);
        platformsnTextView.setText(sb1.toString());

        releasedTextView.setText(mIntent.getStringExtra("released"));

        favorite = mIntent.getBooleanExtra("favorite", false);
        if(favorite){
            fab.setImageResource(android.R.drawable.btn_star_big_on);
        }else {
            fab.setImageResource(android.R.drawable.btn_star_big_off);
        }


        background_image = mIntent.getStringExtra("background_image");
        Glide.with(this)
                .asBitmap()
                .load(background_image)
                .into(articleImage);

        jsonParse(id);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                returnCall();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void returnCall(){
        Intent intent = new Intent();
        intent.putExtra("id", id);
        intent.putExtra("no", no);
        intent.putExtra("favorite", favorite);
        if(changed){
            setResult(777, intent);
        }else {
            setResult(666, intent);
        }

        finish();
    }
    @Override
    public void onBackPressed() {
       returnCall();
    }
    private void jsonParse(String id) {
        StringBuilder sb1 = new
                StringBuilder("https://rawg.io/api/games/");
        String url;
        sb1.append(id);
        url = sb1.toString();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    description = response.getString("description");
                    Toast.makeText(getApplicationContext(), "Games succesfully loaded", Toast.LENGTH_SHORT).show();
                    descriptionTextView.setText(description);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        descriptionTextView.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        descriptionTextView.setText(Html.fromHtml(description));
                    }

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
