package com.jugos_jaco_app.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jugos_jaco_app.R;
import com.jugos_jaco_app.ui.clients.NewClientFragment;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ClientAdapter clientAdapter;
    private List<Client> clients;
    private List<Client> clientsFull; // Copia de la lista completa de clientes

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);



        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        clients = new ArrayList<>();
        clientAdapter = new ClientAdapter(clients, requireContext());
        recyclerView.setAdapter(clientAdapter);

        FloatingActionButton fabCrearCliente = root.findViewById(R.id.fab_crear_cliente);
        fabCrearCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nuevoClienteFragment);
            }
        });

        fillClientsData();
        return root;
    }

    private void fillClientsData() {
        if (clients == null) {
            clients = new ArrayList<>();
        }

        clients.add(new Client("Juan", "Danli Colonia cofradia", "Pérez", "555-1234", "14.6349", "-90.5069"));
        clients.add(new Client("María", "Danli Colonia cofradia", "López", "555-5678", null, null));
        clients.add(new Client("Carlos", "Danli Colonia cofradia", "Gómez", "555-9876", "13.9670", "-89.2064"));
        clients.add(new Client("Ana", "Danli Colonia cofradia", "Ramírez", "555-6543", null, null));

        clientsFull = new ArrayList<>(clients); // Guardar copia completa de la lista

        if (clientAdapter != null) {
            clientAdapter.notifyDataSetChanged();
        }
    }

    public void filterClients(String query) {
        List<Client> filteredList = new ArrayList<>();

        for (Client client : clientsFull) {
            if (client.getFirstName().toLowerCase().contains(query.toLowerCase()) ||
                    client.getLastName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(client);
            }
        }

        clientAdapter.updateList(filteredList); // Llamamos al método en el adaptador
    }
}

