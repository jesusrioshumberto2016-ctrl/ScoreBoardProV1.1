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

    /**
     * 1. CRIAR: Qualquer usuário logado pode criar um campeonato.
     */
    public void salvarCampeonato(String nome) {
        if (currentUserId == null) return;

        String key = mDatabase.push().getKey();
        if (key != null) {
            Campeonato novoCampeonato = new Campeonato(key, nome, currentUserId);
            mDatabase.child(key).setValue(novoCampeonato);
        }
    }

    /**
     * 2. LER: Todos os usuários logados podem ler todos os campeonatos.
     */
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

    /**
     * 3. EDITAR: Somente o usuário cujo uid seja igual ao ownerId pode editar.
     */
    public void editarCampeonato(Campeonato campeonato, String novoNome) {
        if (currentUserId != null && currentUserId.equals(campeonato.ownerId)) {
            campeonato.nome = novoNome;
            mDatabase.child(campeonato.id).setValue(campeonato);
        }
    }

    /**
     * 4. EXCLUIR: Somente o usuário cujo uid seja igual ao ownerId pode excluir.
     */
    public void excluirCampeonato(Campeonato campeonato) {
        if (currentUserId != null && currentUserId.equals(campeonato.ownerId)) {
            mDatabase.child(campeonato.id).removeValue();
        }
    }

    public interface OnCampeonatosDataChanged {
        void onSuccess(List<Campeonato> lista);
        void onError(Exception e);
    }
}
