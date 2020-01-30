package com.example.games;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
//42 nemôžem použiť základny RecycleView.Adapter, lebo tento nevie čo chcem ako prepojiť, preto
//42 musím po ňom dediť
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<Game> mGames = new ArrayList<Game>();

    private Context mContext;

    /**
     *
     * @param mContext
     * @param games
     */
    public RecyclerViewAdapter(Context mContext, ArrayList<Game> games) {
        this.mGames = games;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //42 puts ViewHolders in position, where should be
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        return new ViewHolder(view);
    }

    //42 method is called everytime item is added
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        //42 Glide je použitie knižnice na vykreslovanie obrázkov
        Glide.with(mContext)
                .asBitmap()
                .load(mGames.get(position).getBackground_image())
                .into(holder.articleImage);
        //42 prosto posájam layout s Stringami
        holder.gameName.setText(mGames.get(position).getName());
        String sb = "Rating: "+  mGames.get(position).getRating() + "/5";
        holder.gameRating.setText(sb);
          sb = "Released: "+  mGames.get(position).getReleased();
        holder.gameReleased.setText(sb);
        StringBuilder sb1 = new StringBuilder("Platforms: ");
        for (int i = 0; i<mGames.get(position).getPlatforms().size(); i++){
            sb1.append(" ");
            sb1.append(mGames.get(position).getPlatforms().get(i));
            sb1.append(",");
        }
        sb1.setLength(sb1.length() - 1);
        holder.gamePlatforms.setText(sb1.toString());
        holder.gamefavorite.setChecked(mGames.get(position).isFavorite());
        //42 ak niekto klikne na listItem, tak sa zavolá aktivita GameDetailActivity
        //42 pričom ale mysli na to, že je to z kontextu MainActivity ! ! !
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, GameDetailActivity.class);
                Game g = mGames.get(position);
                intent.putExtra("no", position );
                intent.putExtra("name", g.getName());
                intent.putExtra("id", g.getId());
                intent.putExtra("rating", g.getRating());
                intent.putExtra("short_screenshots", g.getShort_screenshots());
                intent.putExtra("platforms", g.getPlatforms());
                intent.putExtra("released", g.getReleased());
                intent.putExtra("background_image", g.getBackground_image());
                intent.putExtra("favorite", g.isFavorite());
                ((Activity) mContext).startActivityForResult(intent,666);
            }
        });
    }
    //this tell adapter how many items is in your list
    @Override
    public int getItemCount() {
        return mGames.size();
    }

    //42 Tu sa spojí recycleView s Layout_listitem.xml a tento sa bude zapĺňať
    public class ViewHolder extends RecyclerView.ViewHolder{
        //42 držiak pre obrázok, čo je samostatná implementovaná knižnica
        RoundedImageView articleImage;
        TextView gameName;
        TextView gameRating;
        TextView gameReleased;
        TextView gamePlatforms;
        CheckBox gamefavorite;
        RelativeLayout parentLayout;

        /**
         * hold image widgets in memory for each indivudual widget
         * thats why ViewHolder, becouse is hodling the view
         * @param itemView
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            articleImage = itemView.findViewById(R.id.image);
            gameRating = itemView.findViewById(R.id.rating);
            gameName = itemView.findViewById(R.id.title_name);
            gameReleased = itemView.findViewById(R.id.released);
            gamePlatforms = itemView.findViewById(R.id.platforms);
            gamefavorite = itemView.findViewById(R.id.favoriteBox);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
