package com.paulo.miaudota.Controllers;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.paulo.miaudota.Models.Pet;
import com.paulo.miaudota.MyPetsRVAdapter;
import com.paulo.miaudota.PetRVAdapter;
import com.paulo.miaudota.R;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class MyPetsFragment extends Fragment implements MyPetsRVAdapter.PetClickInterface, MyPetsRVAdapter.PetClickDeleteInterface {

    private RecyclerView petRV;
    private ProgressBar progressBar;
    private ArrayList<Pet> petArrayList;
    private MyPetsRVAdapter myPetsRVAdapter;
    private String petId;
    SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Dialog deleteDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("Warning_Activity","onCreateView -> MyPetsFragment");

        View view = inflater.inflate(R.layout.fragment_my_pets, container, false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e("Warning_Activity","onCreateView MyPetsFragment userNull-> ");
            startActivity(new Intent(getActivity(), WelcomeScreen.class));
        }

        petRV = view.findViewById(R.id.idRvMyPets);
        petArrayList = new ArrayList<>();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Pets");
        myPetsRVAdapter = new MyPetsRVAdapter(petArrayList,getContext(),this,this);
        petRV.setLayoutManager(new LinearLayoutManager(getContext()));
        petRV.setAdapter(myPetsRVAdapter);
        progressBar = view.findViewById(R.id.progressBarMyPets);
        progressBar.setVisibility(View.VISIBLE);
        deleteDialog = new Dialog(getContext());
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshMyPets);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                myPetsRVAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        getAllPets();

        return view;
    }

    @Override
    public void onPetClick(int position) {
        Fragment fragmentEditPet =  new EditPetFragment();
        Bundle bundle = new Bundle();
        Pet petModel = petArrayList.get(position);
        petId = petModel.getPetId();
        bundle.putString("PetId", petId);
        fragmentEditPet.setArguments(bundle);
        switchFragment(fragmentEditPet);
    }

    @Override
    public void onPetClickDelete(int position) {
        Pet petModel = petArrayList.get(position);
        petId = petModel.getPetId();
        Log.e("Warning_Activity","Deletando conta");
        openConfirmDeleteDialog(petId);
    }

    private void openConfirmDeleteDialog(String petId) {
        deleteDialog.setContentView(R.layout.delete_confirmation_dialog);
        deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnDeletar, btnCancelar;
        btnDeletar = deleteDialog.findViewById(R.id.btnConfirmarDelete);
        btnCancelar = deleteDialog.findViewById(R.id.btnCancelarDelete);

        btnDeletar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletarRealOficial(petId);
                deleteDialog.dismiss();
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialog.dismiss();
            }
        });

        deleteDialog.show();
    }

    private void deletarRealOficial(String petId) {
        databaseReference.child(petId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), "Pet apagado com sucesso", Toast.LENGTH_LONG).show();
                myPetsRVAdapter.notifyDataSetChanged();
            }

        });
    }

    public void switchFragment(Fragment fragment){
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    private void getAllPets() {
        petArrayList.clear();

        Query query = databaseReference.orderByChild("userId").equalTo(user.getUid().toString());
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                progressBar.setVisibility(View.GONE);
                petArrayList.add(snapshot.getValue(Pet.class));
                myPetsRVAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                progressBar.setVisibility(View.GONE);
                myPetsRVAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                myPetsRVAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                progressBar.setVisibility(View.GONE);
                myPetsRVAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(petArrayList.size() == 0){
            progressBar.setVisibility(View.GONE);
        }
    }

}