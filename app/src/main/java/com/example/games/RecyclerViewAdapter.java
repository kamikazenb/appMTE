package com.example.games;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

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
        //puts ViewHolders in position, where should be
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        return new ViewHolder(view);
    }

    //method is called everytime item is added
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called");

        Glide.with(mContext)
                .asBitmap()
                .load(mGames.get(position).getBackground_image())
                .into(holder.articleImage);

        holder.articleTitle.setText(mGames.get(position).getName());
        holder.articleSource.setText(mGames.get(position).getRating());
//        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String urlString = mArticles.get(position).getArticleAddress();
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.setPackage("com.android.chrome");
//                try {
//                    mContext.startActivity(intent);
//                } catch (ActivityNotFoundException ex) {
//                    intent.setPackage(null);
//                    mContext.startActivity(intent);
//                }
//
//            }
//        });
    }
    //this tell adapter how many items is in your list
    @Override
    public int getItemCount() {
        return mGames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        RoundedImageView articleImage;
        TextView articleTitle;
        TextView articleSource;
        RelativeLayout parentLayout;

        /**
         * hold image widgets in memory for each indivudual widget
         * thats why ViewHolder, becouse is hodling the view
         * @param itemView
         */

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            articleImage = itemView.findViewById(R.id.image);
            articleSource = itemView.findViewById(R.id.source);
            articleTitle = itemView.findViewById(R.id.title_name);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
