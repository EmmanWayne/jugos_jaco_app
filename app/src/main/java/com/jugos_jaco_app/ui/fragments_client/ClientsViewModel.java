package com.jugos_jaco_app.ui.fragments_client;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jugos_jaco_app.VolleySingleton;
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
                        JSONObject responseJson = new JSONObject(response.toString());
                        JSONArray clientsJson = responseJson.getJSONArray("clients");

                        for (int i = 0; i < clientsJson.length(); i++) {
                            JSONObject clientJson = clientsJson.getJSONObject(i);
                            Client client = new Client(
                                    clientJson.getString("first_name"),
                                    clientJson.getString("last_name"),
                                    clientJson.getString("phone_number"),
                                    clientJson.getString("address"),
                                    clientJson.getString("department"),
                                    clientJson.getString("township"),
                                    clientJson.getJSONObject("location").getString("latitude"),
                                    clientJson.getJSONObject("location").getString("longitude")
                            );
                            clients.add(client);
                        }
                        cachedClients = new ArrayList<>(clients);
                        clientList.setValue(clients);
                        isDataLoaded = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        errorMessage.setValue("Error al procesar los datos");
                    }
                },
                error -> {
                    String message = "Error al cargar los clientes";
                    if (error.networkResponse != null) {
                        try {
                            String errorBody = new String(error.networkResponse.data);
                            JSONObject errorJson = new JSONObject(errorBody);
                            message = errorJson.getString("message");
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
}