package com.example.wnsvy.kakaocalorie.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wnsvy.kakaocalorie.Model.RankModel;
import com.example.wnsvy.kakaocalorie.R;

import java.util.ArrayList;


/**
 * Created by Oclemmy on 4/23/2016 for ProgrammingWizards Channel.
 */
public class RnakAdapter extends RecyclerView.Adapter<RankHolder> {

    Context context;
    ArrayList<RankModel> rankList;

    public RnakAdapter(Context context, ArrayList<RankModel> rankList) {
        this.context = context;
        this.rankList = rankList;
    }

    //INITIALIE VH
    @Override
    public RankHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.view_friend_rank_item,parent,false);
        RankHolder holder=new RankHolder(v);
        return holder;
    }

    //BIND DATA
    @Override
    public void onBindViewHolder(RankHolder holder, int position) {
        holder.nameTxt.setText(rankList.get(position).getUserID());
        holder.footstepTxt.setText(rankList.get(position).getFootstep());
        Glide.with(context).load(rankList.get(position).getUserPhoto()).apply(new RequestOptions().override(200,200)).into(holder.friendProfile);
    }

    @Override
    public int getItemCount() {
        return rankList.size();
    }
}

