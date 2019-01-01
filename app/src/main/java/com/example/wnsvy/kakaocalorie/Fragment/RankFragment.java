package com.example.wnsvy.kakaocalorie.Fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wnsvy.kakaocalorie.Adapter.RnakAdapter;
import com.example.wnsvy.kakaocalorie.Model.RankModel;
import com.example.wnsvy.kakaocalorie.R;

import java.util.ArrayList;


/**
 * Created by Oclemmy on 4/23/2016 for ProgrammingWizards Channel.
 */
public class RankFragment extends DialogFragment {

    public ArrayList<RankModel> rankModelArrayList;
    RecyclerView rv;
    RnakAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView=inflater.inflate(R.layout.fragment_rank,container);

        //RECYCER
        rv= (RecyclerView) rootView.findViewById(R.id.mRecyerID);
        rv.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        rankModelArrayList = getArguments().getParcelableArrayList("friendProfile");

        //ADAPTER
        adapter=new RnakAdapter(this.getActivity(),rankModelArrayList);
        rv.setAdapter(adapter);

        getDialog().setTitle("오늘의 걷기왕");


        return rootView;
    }
}
