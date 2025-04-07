package com.jugos_jaco_app.ui.clients;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jugos_jaco_app.ui.models.Client;
import com.jugos_jaco_app.ui.utilities.VolleySingleton;
import com.jugos_jaco_app.ui.utilities.Utilities;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientsViewModel extends ViewModel {
    private MutableLiveData<List<Client>> clientList = new MutableLiveData<>();
    private List<Client> cachedClients = new ArrayList<>();
    private boolean isDataLoaded = false;
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<Client>> getClients() {
        return clientList;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadClientsIfNeeded(Context context) {
        if (!isDataLoaded) {
            loadClients(context);
        }
    }

    public void forceLoadClients(Context context) {
        loadClients(context);
    }

    private void loadClients(Context context) {
        String url = Utilities.URL + "clients/";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        List<Client> clients = new ArrayList<>();
                        // Obtener el array "data" directamente del objeto response
                        JSONArray clientsJson = response.getJSONArray("data");
                        for (int i = 0; i < clientsJson.length(); i++) {
                            JSONObject clientJson = clientsJson.getJSONObject(i);
                            JSONObject locationJson = clientJson.getJSONObject("location");
                            String typePrice = "";
                            if (clientJson.has("type_price") && !clientJson.isNull("type_price")) {
                                typePrice = clientJson.getString("type_price");
                            }

                            Client client = new Client(
                                    String.valueOf(clientJson.getInt("id")), // Convertir id a String
                                    clientJson.getString("first_name"),
                                    clientJson.getString("last_name"),
                                    clientJson.getString("phone_number"),
                                    clientJson.getString("address"),
                                    clientJson.getString("department"),
                                    clientJson.getString("township"),
                                    locationJson.getString("latitude"),
                                    locationJson.getString("longitude"),
                                    locationJson.getString("plus_code"),
                                    typePrice,
                                    clientJson.getString("business_name")
                            );

                            clients.add(client);
                        }

                        cachedClients = new ArrayList<>(clients);
                        clientList.setValue(clients);
                        isDataLoaded = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("TAGASIEMPRE", e.toString());
                        errorMessage.setValue("Error al procesar los datos");
                    }
                },
                error -> {
                    String message = "Error al cargar los clientes";
                    if (error.networkResponse != null) {
                        try {
                            String errorBody = new String(error.networkResponse.data);
                            JSONObject errorJson = new JSONObject(errorBody);
                            if(errorJson.has("message")){
                                message = errorJson.getString("message");
                            }else{
                                message = "Error inesperado";
                            }
                        } catch (JSONException e) {
                            if (error.networkResponse.statusCode == 401) {
                                message = "Sesión expirada";
                            }
                        }
                    }
                    errorMessage.setValue(message);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", ClientsFragment.getAuthorizationHeader(context));
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }
    public void filterClients(String query) {
        if (query.isEmpty()) {
            clientList.setValue(cachedClients);
            return;
        }

        List<Client> filteredList = new ArrayList<>();
        for (Client client : cachedClients) {
            if (client.getFirstName().toLowerCase().contains(query.toLowerCase()) ||
                    client.getLastName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(client);
            }
        }
        clientList.setValue(filteredList);
    }

    public void addNewClient(Client newClient) {
        List<Client> currentClients = clientList.getValue();
        if (currentClients != null) {
            currentClients.add(newClient);
            cachedClients.add(newClient);
            clientList.setValue(currentClients);
        }
    }

    public void updateClient(Client updatedClient) {
        List<Client> currentClients = clientList.getValue();
        if (currentClients != null) {
            // Encontrar y actualizar el cliente en la lista
            for (int i = 0; i < currentClients.size(); i++) {
                if (currentClients.get(i).getId().equals(updatedClient.getId())) {
                    currentClients.set(i, updatedClient);
                    break;
                }
            }
            // Actualizar también la lista cacheada
            for (int i = 0; i < cachedClients.size(); i++) {
                if (cachedClients.get(i).getId().equals(updatedClient.getId())) {
                    cachedClients.set(i, updatedClient);
                    break;
                }
            }
            clientList.setValue(currentClients);
        }
    }
}