package com.example.games;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SearchView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    //42 globalna premenna pre handler volani JSON
    private RequestQueue mQueue;
    private ArrayList<Game> games = new ArrayList<Game>();
    private Pages pages;
    private String mainAddress = "https://rawg.io/api/games";
    private ArrayList<String> favorites;
    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    private SwipeRefreshLayout refreshLayout;
    int visibleItemCount;
    int totalItemCount;
    int pastVisiblesItems;
    LinearLayoutManager mLayoutManager;
    boolean loading = true;
    SearchView searchView;

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
                Intent intent = new Intent(getApplicationContext(), GameFavoritesActivity.class);
                if(favorites.size()>0){
                    favorites = removeDuplicates(favorites);
                    intent.putExtra("favorites", favorites);
                    startActivityForResult(intent,888);
                }else{
                    Toast.makeText(getApplicationContext(),"You have 0 favorite games",Toast.LENGTH_SHORT).show();
                }

            }
        });



        pages = new Pages("null", "null");
        initialRecycleItems();
        //42 init handleru s tym ze tento si pripravy frontu na volania
        mQueue = Volley.newRequestQueue(this);
        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Set<String> set = sharedPreferences.getStringSet("key", null);
            favorites = new ArrayList<String>(set);
        }catch (Exception e){
            favorites = new ArrayList<String>();
        }

        refLayout();
        jsonParse(generateUrl(), true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) //check for scroll down
                {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (loading)
                    {
                        if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount-1)
                        {
                            loading = false;
                            jsonParse(pages.getNext(), false);
                        }
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu) {
        getMenuInflater().inflate( R.menu.menu_main, menu);

       final MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
         searchView= (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Toast like print
                Toast.makeText(getApplicationContext(), query, Toast.LENGTH_SHORT).show();
                jsonParse(query);
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;
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
                addAndNotify();
            }catch (Exception e){  }
        } if (resultCode==888){
            try {
                favorites = data.getStringArrayListExtra("favorites");
                Set<String> set = new HashSet<String>(favorites);
                for (int i = 0; i<games.size(); i++){
                    if(set.contains(games.get(i).getId())){
                        games.get(i).setFavorite(true);
                    }else{
                        games.get(i).setFavorite(false);
                    }
                    addAndNotify();
                }
            }catch (Exception e){ }

        }
        if (resultCode==555) {
            try {

                jsonParse(generateUrl(), true);

            } catch (Exception e) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String generateUrl(){
        StringBuilder sb1 = new StringBuilder(mainAddress);
        sb1.append("?page_size=7");
        try {
            String date = sharedPreferences.getString("release_year", "");
            String ordering = sharedPreferences.getString("ordering", "");
            if(date != null && !date.isEmpty()){
                sb1.append("&dates=");
                sb1.append(date);
                sb1.append("-01-01,");
                sb1.append(date);
                sb1.append("-01-31");
                if(ordering != null && !ordering.isEmpty()){
                    sb1.append("&ordering=");
                    sb1.append(ordering);
                }
            }
        }catch (Exception e){

        }
        return sb1.toString();

    }
    @Override
    protected void onStop() {
        try {
            Set<String> set = new HashSet<String>();
            set.addAll(favorites);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putStringSet("key", set);
            edit.commit();
        }catch (Exception e){

        }

        super.onStop();
    }

    private void initialRecycleItems() {
        recyclerView = findViewById(R.id.recycle_view);
        adapter = new RecyclerViewAdapter(this, games);
        recyclerView.setAdapter(adapter);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

    }

    private void refLayout() {
        refreshLayout = findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                jsonParse(generateUrl(), true);
            }
        });
    }

    private void jsonParse(String url, final boolean deleteOld){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(deleteOld){
                        games.removeAll(new ArrayList<>(games));
                    }
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
                            if(favorites.size()>0){
                                Set<String> set = new HashSet<String>(favorites);
                                if(set.contains(adding.getId())){
                                    adding.setFavorite(true);
                                }
                            }
                            games.add(adding);
                        }
                        Toast.makeText(getApplicationContext(),"Games succesfully loaded",Toast.LENGTH_SHORT).show();

                    }
                    else {
                       Toast.makeText(getApplicationContext(),"Error at games feed",Toast.LENGTH_SHORT).show();
                    }
                    addAndNotify();

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

    private void addAndNotify(){
        loading = true;
        adapter.notifyDataSetChanged();
        refreshLayout.setRefreshing(false);
    }

    private void jsonParse(final String game) {
        StringBuilder sb1 = new
                StringBuilder("https://rawg.io/api/games/");
        String url;
        sb1.append(game);
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
                   screenshots.add("err");
                    Game adding = new Game(
                            response.getString("id"),
                            response.getString("name"),
                            response.getString("background_image"),
                            response.getString("rating"),
                            platforms,
                            screenshots,
                            response.getString("released")
                    );
                    if(favorites.size()>0){
                        Set<String> set = new HashSet<String>(favorites);
                        if(set.contains(adding.getId())){
                            adding.setFavorite(true);
                        }
                    }
                    games.removeAll(new ArrayList<>(games));
                    games.add(adding);
                    addAndNotify();

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivityForResult(intent,555);
        }
        if (id == R.id.favorites) {


        }

        return super.onOptionsItemSelected(item);
    }

    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {

        // Create a new ArrayList
        ArrayList<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }

        // return the new list
        return newList;
    }
}
