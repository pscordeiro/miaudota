package com.paulo.miaudota.Controllers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paulo.miaudota.CandidaturaRVAdapter;
import com.paulo.miaudota.Models.Candidatura;
import com.paulo.miaudota.Models.Pet;
import com.paulo.miaudota.Models.User;
import com.paulo.miaudota.PetRVAdapter;
import com.paulo.miaudota.R;

import java.util.ArrayList;


public class Candidaturas extends AppCompatActivity implements CandidaturaRVAdapter.CandidaturaClickInterface{

    private RecyclerView candidaturaRV;
    private ProgressBar progressBar;
    private ArrayList<Candidatura> candidaturaArrayList;
    private CandidaturaRVAdapter candidaturaRVAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    private String userId, nomeUsuario, emailUsuario, localizacaoUsuario, profilePicUsuario, idPetCandidatura, celularUsuario;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidaturas);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e("Warning_Activity","onCreateView HomeFragment userNull-> ");
            startActivity(new Intent(Candidaturas.this, WelcomeScreen.class));
        }

        candidaturaRV = findViewById(R.id.idRvCandidaturas);
        candidaturaArrayList = new ArrayList<>();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Candidaturas");
        candidaturaRVAdapter = new CandidaturaRVAdapter(candidaturaArrayList,Candidaturas.this,this);
        candidaturaRV.setLayoutManager(new LinearLayoutManager(Candidaturas.this));
        candidaturaRV.setAdapter(candidaturaRVAdapter);
        progressBar = findViewById(R.id.progressBarCandidaturas);
        progressBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshCandidaturas);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                candidaturaRVAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        userId = mAuth.getCurrentUser().getUid();
        if (getIntent().hasExtra("petId")) {
            idPetCandidatura = getIntent().getStringExtra("petId");
        }

        getAllCandidaturas(idPetCandidatura);

    }

    private void getAllCandidaturas(String idPet) {
        candidaturaArrayList.clear();

        Query query = databaseReference.orderByChild("idPet").equalTo(idPet);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                progressBar.setVisibility(View.GONE);
                Log.e("PET", "pet nao adotado-> ");
                candidaturaArrayList.add(snapshot.getValue(Candidatura.class));
                candidaturaRVAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                candidaturaArrayList.add(snapshot.getValue(Candidatura.class));
                candidaturaRVAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                candidaturaArrayList.add(snapshot.getValue(Candidatura.class));
                candidaturaRVAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                candidaturaArrayList.add(snapshot.getValue(Candidatura.class));
                candidaturaRVAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(candidaturaArrayList.size() == 0){
            progressBar.setVisibility(View.GONE);
        }

    }

    @Override
    public void onCandidaturaClick(int position) {
        Candidatura candidaturaModel = candidaturaArrayList.get(position);
        String texto = "Olá, vi no Miaudota que você tem interesse em adotar o meu pet";
        String numCelular = "55" +  candidaturaModel.getCelularUsuario();
        String url="https://api.whatsapp.com/send?phone="+ numCelular + "&text=" + texto;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        try {
            startActivity(i);
        }catch (Exception ex){
            Toast.makeText(Candidaturas.this, "Ocorreu um erro ao tentar abrir o whastsapp !!", Toast.LENGTH_LONG).show();
        }
    }


}