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
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<String> selectedDay = new MutableLiveData<>(null);
    private List<Client> cachedClients = new ArrayList<>();
    private boolean isDataLoaded = false;
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<List<Client>> clients = new MutableLiveData<>();

    public LiveData<List<Client>> getClients() {
        return clientList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getSelectedDay() {
        return selectedDay;
    }

    public void setSelectedDay(String day) {
        this.selectedDay.setValue(day);
    }

    public boolean hasLoadedData() {
        return isDataLoaded;
    }

    public void forceLoadClients(Context context) {
        isDataLoaded = false;
        loadClients(context);
    }

    public void loadClients(Context context) {
        if (isDataLoaded && clientList.getValue() != null) {
            return; // Si ya hay datos cargados, no hacer nada
        }

        isLoading.setValue(true);
        String url = Utilities.URL + "clients";
        
        // Solo agregar el parámetro day si hay un día seleccionado
        String currentDay = selectedDay.getValue();
        if (currentDay != null && !currentDay.isEmpty()) {
            url += "?day=" + currentDay;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        List<Client> clients = new ArrayList<>();
                        JSONArray clientsJson = response.getJSONArray("data");
                        for (int i = 0; i < clientsJson.length(); i++) {
                            JSONObject clientJson = clientsJson.getJSONObject(i);
                            JSONObject locationJson = clientJson.getJSONObject("location");
                            String typePrice = "";
                            if (clientJson.has("type_price") && !clientJson.isNull("type_price")) {
                                typePrice = clientJson.getString("type_price");
                            }

                            String position = clientJson.optString("position", "");
                            String visitDay = clientJson.optString("visit_day", "");
                            String profileImage = clientJson.optString("profile_image", "");

                            Client client = new Client(
                                    String.valueOf(clientJson.getInt("id")),
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
                                    clientJson.getString("business_name"),
                                    position,
                                    visitDay,
                                    profileImage
                            );
                            clients.add(client);
                        }

                        cachedClients = new ArrayList<>(clients);
                        clientList.setValue(clients);
                        isDataLoaded = true;
                        isLoading.setValue(false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("TAGASIEMPRE", e.toString());
                        errorMessage.setValue("Error al procesar los datos");
                        isLoading.setValue(false);
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
                    isLoading.setValue(false);
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