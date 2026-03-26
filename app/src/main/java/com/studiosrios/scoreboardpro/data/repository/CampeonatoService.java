package com.studiosrios.scoreboardpro.data.repository;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.studiosrios.scoreboardpro.data.model.Campeonato;

import java.util.ArrayList;
import java.util.List;

public class CampeonatoService {

    private final DatabaseReference mDatabase;
    private final String currentUserId;

    public CampeonatoService() {
        this.mDatabase = FirebaseDatabase.getInstance().getReference("campeonatos");
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                             FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    public void salvarCampeonato(String nome) {
        if (currentUserId == null) return;

        String key = mDatabase.push().getKey();
        if (key != null) {
            // Ajustado para usar o construtor do Kotlin (que pode exigir todos os argumentos dependendo de como foi definido)
            Campeonato novoCampeonato = new Campeonato();
            novoCampeonato.setId(key);
            novoCampeonato.setNome(nome);
            novoCampeonato.setOwnerId(currentUserId);
            
            mDatabase.child(key).setValue(novoCampeonato);
        }
    }

    public void listarTodos(final OnCampeonatosDataChanged listener) {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Campeonato> lista = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Campeonato camp = postSnapshot.getValue(Campeonato.class);
                    if (camp != null) {
                        lista.add(camp);
                    }
                }
                listener.onSuccess(lista);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    public void editarCampeonato(Campeonato campeonato, String novoNome) {
        // No Java, acessamos propriedades Kotlin via getters/setters
        if (currentUserId != null && currentUserId.equals(campeonato.getOwnerId())) {
            campeonato.setNome(novoNome);
            mDatabase.child(campeonato.getId()).setValue(campeonato);
        }
    }

    public void excluirCampeonato(Campeonato campeonato) {
        if (currentUserId != null && currentUserId.equals(campeonato.getOwnerId())) {
            mDatabase.child(campeonato.getId()).removeValue();
        }
    }

    public interface OnCampeonatosDataChanged {
        void onSuccess(List<Campeonato> lista);
        void onError(Exception e);
    }
}
