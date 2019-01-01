package com.example.wnsvy.kakaocalorie.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.wnsvy.kakaocalorie.R;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by Oclemmy on 4/23/2016 for ProgrammingWizards Channel.
 */
public class RankHolder extends RecyclerView.ViewHolder {

    TextView nameTxt;
    TextView footstepTxt;
    CircleImageView friendProfile;

    public RankHolder(View itemView) {
        super(itemView);
        nameTxt= (TextView) itemView.findViewById(R.id.nameTxt);
        footstepTxt = (TextView) itemView.findViewById(R.id.footStepCount);
        friendProfile = (CircleImageView)itemView.findViewById(R.id.friend_profile);
    }
}
