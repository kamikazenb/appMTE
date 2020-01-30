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

//42 toto je základná aktivita ktorá sa spúšťa hneď po splashscreen
public class MainActivity extends AppCompatActivity {
    //42 premenné uložené v sharedPreferences
    SharedPreferences sharedPreferences;
    //42 globalna premenna pre handler volani JSON
    private RequestQueue mQueue;
    //42 tu sú uložené všetky objekty  s hrami
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

    //42 toto sa spustí vždy keď sa načíta aktivita (napríklad po jej vypnutí), ale pozor
    //42 nie po návrate z aktivity, ktroú volala intentom startActivityForResult (napr. settingsAcitivity atd...)
    //42 a keby sa volá len  startActivity, tak po návrate by sa musela spustiť celá touto funkciou znovu
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //42 toto sú bláboly od androidu
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            //42 ked niekto klikne na tlačidlo FAV tak sa vyvolá akcia (intent)
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GameFavoritesActivity.class);
                if (favorites.size() > 0) {
                    favorites = removeDuplicates(favorites);
                    intent.putExtra("favorites", favorites);
                    startActivityForResult(intent, 888);
                } else {
                    //42 toto sú Toasty - tie malé informačné bublinky ktoré sa ti nachvíľu ukážu v okne a informujú ťa o niečom
                    Toast.makeText(getApplicationContext(), "You have 0 favorite games", Toast.LENGTH_SHORT).show();
                }

            }
        });
        //42 ked zavolám API pomcou jsonParse, tak mi vráti len 7 výsledkov! Tiež mi ale vráti
        //42 odkaz na ďalšiu stranu s výsledkami, teda keď skroluješ tak sa priebežne volajú tieto
        //42 odkazy a nie pôvodná APIna
        pages = new Pages("null", "null");
        //42 A tu je tá mágia, otvor to
        initialRecycleItems();
        //42 init handleru s tym ze tento si pripravy frontu na volania, pričom "this" je tzv. kontext, teda aktuálne okno
        mQueue = Volley.newRequestQueue(this);
        try {
            //42 pozrie sa na dáta uložené v perzistentnej pamati - sharedPreferences a povytahuje
            //42 si z tadial čo potrebuje, v tomto prípade si vytiahne arrayList v ktorom sú uložené
            //42 ID hier, ktoré sú favorites, lebo ukladanie samotných hier je priveľmi náročné
            //42 a takto sa to dalo krásne ojebať, nakoľko pri nahrávaní hier cez JsonParse
            //42 si prosto vždy skontroluje že je hra oblúbená podľa ID hry a ako áno,
            //42 tak jej nastavý favorites = true
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Set<String> set = sharedPreferences.getStringSet("key", null);
            favorites = new ArrayList<String>(set);
        } catch (Exception e) {
            favorites = new ArrayList<String>();
        }
//42 toto je kvôli tomu aby sa mohli obnovovať hry potiahnutím prsta dole na základnej obrazovke
        //42 a je to refreshLayout v ktorom je vložený náš layout
        refLayout();
        //42 HERE WE GO, konečne niečo zaujímavé. Toto je po zapnutí Appky prvé volanie APIny
        //42 treba do neho vložiť URL a druhý parameter je či chcem zmazať objekty v
        //42 ArrayListe<Game> games, lebo keby napr. refreshnes hry potiahnutím prsta
        //42 tak by ti vznikali kopie, ale n druhú stranu by si zase pri stálom mazaní
        //42 nedokázala načítať priebežne dalšie
        jsonParse(generateUrl(), true);
        //42 v prípade že sa v rycycleView scrolluje, tak kontroluje či zobrazuje predposlednú hru
        //42 a ak áno, tak si prosto zavolá APInu aby mu načítala ďalšie hry
        //42 tzv. endless scrolling
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) //check for scroll down
                {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount - 1) {
                            loading = false;
                            jsonParse(pages.getNext(), false);
                        }
                    }
                }
            }
        });
        //42 a toto bolo všetko čo bolo potrebné po začatí appky spustiť
    }

    //42 toto veľmi nerieš, sám veľmi nevime ako to funguje ale umožňuje hladanie v search bare
    //42 pričom po stlačení hľadať spustí nový jsonParse ALE POZOR, tento je iný, lebo tento vracia
    //42 jedinú hru (všimni si že má iný počet argumentov)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Toast like print
                jsonParse(query);
                if (!searchView.isIconified()) {
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

    //42 keď si zavoláš inú aktivitu intentom startActivityForResult, tak ked sa táto ukončí
    //42 a vráti odpoved, tak na tomto mieste pauznutá táo aktivita čaká na kód, ktorým jej aktivita
    //42 odpovedala, plus na dáta ktoré su v tomto spätnom intente
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        int a = 5;
        String id = "";
        boolean favorite = false;
        int no = 0;
        if (resultCode == 777) {
            try {
                //42 odpoved z gameDetail, ktorý v prípade že sa zmenil stav hry (favorite), tak
                //42 napíše odpved a podľa totho nastaví game setFavorites a
                //42 pridí/odobere z ArrayList favorites
                id = data.getStringExtra("id");
                no = data.getIntExtra("no", 0);
                favorite = data.getBooleanExtra("favorite", false);
                if (favorite) {
                    favorites.add(id);
                } else {
                    for (int i = 0; i < favorites.size(); i++) {
                        if (favorites.get(i).equals(id)) {
                            favorites.remove(i);
                            break;
                        }
                    }
                }
                games.get(no).setFavorite(favorite);
                addAndNotify();
            } catch (Exception e) {
            }
        }
        if (resultCode == 888) {
            //42 tu je odpoved z favoriteGames, či sa niečo zmenilo s favorite listom
            try {
                favorites = data.getStringArrayListExtra("favorites");
                //42 konvertujm na HashSet, lebo keď chcem zistiť či sa tam nachádza
                //42 ID, tak nemusím preiterovať cez celý ArrayList
                Set<String> set = new HashSet<String>(favorites);
                for (int i = 0; i < games.size(); i++) {
                    if (set.contains(games.get(i).getId())) {
                        games.get(i).setFavorite(true);
                    } else {
                        games.get(i).setFavorite(false);
                    }
                    addAndNotify();
                }
            } catch (Exception e) {
            }

        }
        //42 tu je odpoveď zo Settings, pričom vždy sa načítajú nové hry
        if (resultCode == 555) {
            try {
                jsonParse(generateUrl(), true);

            } catch (Exception e) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //42 vygeneruje URL ktorá bude zaslaná do APIny, pričom ak je v shared-preferences
    //42 rok hry, tak sa vložží spoločne s rodering a keď nie, tak sa pošle len požiadavka na základný
    //42 zoznam hier
    private String generateUrl() {
        StringBuilder sb1 = new StringBuilder(mainAddress);
        sb1.append("?page_size=7");
        try {
            String date = sharedPreferences.getString("release_year", "");
            String ordering = sharedPreferences.getString("ordering", "");
            if (date != null && !date.isEmpty()) {
                sb1.append("&dates=");
                sb1.append(date);
                sb1.append("-01-01,");
                sb1.append(date);
                sb1.append("-01-31");
                if (ordering != null && !ordering.isEmpty()) {
                    sb1.append("&ordering=");
                    sb1.append(ordering);
                }
            }
        } catch (Exception e) {

        }
        return sb1.toString();

    }
    //42 pri zastevení appky uloží obsah Favorites ArrayListu do sharedPreference
    //42 ale musí to urobiť cez HashSet, lebo do sharedPrefrence nemôže byť vložený ArrayList
    @Override
    protected void onStop() {
        try {
            Set<String> set = new HashSet<String>();
            set.addAll(favorites);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putStringSet("key", set);
            edit.commit();
        } catch (Exception e) {

        }

        super.onStop();
    }

    private void initialRecycleItems() {
        //42 nájde sa v základnom okne content_main recycle_view a označí sa že chceš do neho všetko
        //42 pridávať
        recyclerView = findViewById(R.id.recycle_view);
        //42 bežne má recycleView svoj adaptér, ktorý ale my nechceme, lebo neposkytuje funkcionalitu
        //42 ktorú potrebujeme, preto si musíme vytvoriť nový a pripojiť ho
        adapter = new RecyclerViewAdapter(this, games);
        recyclerView.setAdapter(adapter);
        //42 toto je kvôli tomu aby sa mohli v endless mode nahrávať hry (ani ja neviem ako to poriadne funguje)
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

    //42 takto vyzerá úplne najzákladnejšie volanie
    //42 https://rawg.io/api/games?page_size=7
    //42 takto ked bol zadaný rok hry,
    //42 https://api.rawg.io/api/games?dates=2019-01-01,2019-12-31&ordering=-added&page_size=7

    private void jsonParse(String url, final boolean deleteOld) {
        //42 vytvorenie požiadavky s priradeným Listenerom
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //42 ak je požiadavka na vymazanie starých hier
                    if (deleteOld) {
                        games.removeAll(new ArrayList<>(games));
                    }
                    //42 uložím si ďalšiu stranu s výsledkami
                    pages = new Pages(response.getString("next"), response.getString("previous"));
                    //42 samotné výsledky vo forme Array
                    JSONArray results = response.getJSONArray("results");
                    if (results.length() > 0) {
                        for (int i = 0; i < results.length(); i++) {
                            //42 URl screenshotov s hry a platforiem sú v Game ako ArrayList<String>
                            ArrayList<String> screenshots = new ArrayList<String>();
                            ArrayList<String> platforms = new ArrayList<String>();

                            JSONObject game = results.getJSONObject(i);

                            JSONArray short_screenshots = (JSONArray) results.getJSONObject(i).get("short_screenshots");
                            JSONArray parent_platforms = (JSONArray) results.getJSONObject(i).get("parent_platforms");

                            for (int j = 0; j < short_screenshots.length(); j++) {
                                JSONObject screen = short_screenshots.getJSONObject(j);
                                screenshots.add(screen.getString("image"));
                            }

                            for (int j = 0; j < parent_platforms.length(); j++) {
                                JSONObject platform = parent_platforms.getJSONObject(j);
                                platforms.add(platform.getJSONObject("platform").getString("name"));
                            }
                            //42 vytvorím novú hru
                            Game adding = new Game(
                                    game.getString("id"),
                                    game.getString("name"),
                                    game.getString("background_image"),
                                    game.getString("rating"),
                                    platforms,
                                    screenshots,
                                    game.getString("released")
                            );
                            //42 zistím podľa ID či je hra medzi obľúbenými
                            if (favorites.size() > 0) {
                                Set<String> set = new HashSet<String>(favorites);
                                if (set.contains(adding.getId())) {
                                    adding.setFavorite(true);
                                }
                            }
                            //42 pridám hru do arrayListu
                            games.add(adding);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Error at games feed", Toast.LENGTH_SHORT).show();
                    }
                    //42 vykreslím Games
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
        //42 vložím požiadavku do radu, pričom knižnica to už vybaví za mňa (asynchrónne vlákno)
        mQueue.add(request);
    }

    private void addAndNotify() {
        //42 aby mi zobrazilo načítavacie koliečko
        loading = true;
        //42 upozonenie adaptéru RecycleView že sa zmenil obsah
        adapter.notifyDataSetChanged();
        //42 zrušnie načítavacieho koliečka
        refreshLayout.setRefreshing(false);
    }
    //42 to isté ale len pre Jednu hru, URL vyzerá takto
    //42 https://rawg.io/api/games/doom
    //42 JSon vyzerá trochu inak, preto má aj inú metódu onResponse
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
                    for (int j = 0; j < parent_platforms.length(); j++) {
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
                    if (favorites.size() > 0) {
                        Set<String> set = new HashSet<String>(favorites);
                        if (set.contains(adding.getId())) {
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
    //42 ak niekto klikne v toolBare na settings, tak sa spustí nový intent
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivityForResult(intent, 555);
        }

        return super.onOptionsItemSelected(item);
    }
    //42 vymaže redudantné prvky v arrayListe
    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {

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
