package com.jugos_jaco_app.ui.clients;

import static com.jugos_jaco_app.Login.KEY_TOKEN;
import static com.jugos_jaco_app.Login.PREFS_NAME;
import static com.jugos_jaco_app.Login.TOKEN_TYPE;
 
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jugos_jaco_app.Login;
import com.jugos_jaco_app.R;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.button.MaterialButton;

import android.content.Context;
import android.location.LocationManager;
import android.provider.Settings;
import android.app.AlertDialog;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;

import com.jugos_jaco_app.ui.adapters.PhotoAdapter;
import com.jugos_jaco_app.ui.api.RetrofitClient;
import com.jugos_jaco_app.ui.api.PhotoResponse;
import com.jugos_jaco_app.ui.api.ServerPhotosResponse;
import com.jugos_jaco_app.ui.adapters.ServerPhotoAdapter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

import android.media.ExifInterface;
import android.graphics.Matrix;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.jugos_jaco_app.ui.api.MessageResponse;
import android.content.pm.ResolveInfo;

import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.jugos_jaco_app.ui.utilities.Utilities;

import android.content.pm.ActivityInfo;
import android.widget.ProgressBar;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.graphics.drawable.ColorDrawable;

import androidx.lifecycle.ViewModelProvider;

public class ClientDetailsFragment extends Fragment implements PhotoAdapter.OnPhotoListener, VisitDaysDialog.VisitDaysDialogListener {

    // ... (resto de campos)
    @Override
    public void onVisitDaysSelected(List<String> selectedDays) {
        if (selectedDays == null || selectedDays.isEmpty()) {
            Toast.makeText(getContext(), "Debe seleccionar al menos un día de visita", Toast.LENGTH_LONG).show();
            return;
        }
        // Detectar días eliminados
        ArrayList<String> daysToRemove = new ArrayList<>();
        for (String oldDay : currentVisitDays) {
            if (!selectedDays.contains(oldDay)) {
                daysToRemove.add(oldDay);
            }
        }
        // Eliminar días desmarcados en el servidor
        for (String day : daysToRemove) {
            String idVisitDay = visitDayIdMap.get(day);
            if (idVisitDay != null && !idVisitDay.isEmpty()) {
                deleteVisitDayFromServer(idVisitDay, day);
            }
        }
        // Agregar días nuevos
        for (String day : selectedDays) {
            if (!currentVisitDays.contains(day)) {
                postVisitDayToServer(day);
            }
        }
        // Actualiza la lista local para reflejar los cambios
        currentVisitDays.clear();
        currentVisitDays.addAll(selectedDays);
    }

    private void deleteVisitDayFromServer(String idVisitDay, String day) {
        String url = Utilities.URL + "clients/" + id + "/visit-days/" + idVisitDay;
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest deleteRequest = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Día eliminado: " + day, Toast.LENGTH_SHORT).show();
                    loadVisitDays(tvVisitDayGlobal);
                },
                error -> {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Error al eliminar día: " + day, Toast.LENGTH_SHORT).show();
                    loadVisitDays(tvVisitDayGlobal);
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", ClientDetailsFragment.getAuthorizationHeader(getContext()));
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        queue.add(deleteRequest);
    }

    private void postVisitDayToServer(String day) {
        String urlVisitDays = Utilities.URL + "clients/" + id + "/visit-days";
        int position = (int) (10000 + Math.random() * 9000); // número arriba de 10000
        org.json.JSONObject params = new org.json.JSONObject();
        try {
            params.put("position", position);
            params.put("visit_day", day);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al preparar datos", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.POST,
                urlVisitDays,
                params,
                response -> {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Día agregado: " + day, Toast.LENGTH_SHORT).show();
                    loadVisitDays(tvVisitDayGlobal);
                },
                error -> {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Error al agregar día: " + day, Toast.LENGTH_SHORT).show();
                    loadVisitDays(tvVisitDayGlobal);
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", ClientDetailsFragment.getAuthorizationHeader(getContext()));
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(postRequest);
    }


    private static final int REQUEST_CODE_PERMISSIONS = 100;

    private String clientName;
    private String clientPhone;
    private String clientLatitude;
    private String clientLongitude;
    private RecyclerView rvPhotos;
    private PhotoAdapter photoAdapter;
    private List<PhotoAdapter.PhotoItem> photos = new ArrayList<>();

    private static final int REQUEST_CODE_CAMERA = 100;
    private static final int REQUEST_CODE_GALLERY = 101;
    private static final int REQUEST_CODE_GALLERY_MULTIPLE = 102;
    private Uri photoUri;
    private String adress;
    private String department;
    private String township;
    private String clientFirstName;
    private String clientLastName;
    private String id;
    private String typePrice;
    private String businessName;
    private String position;
    private String visitDay;

    private RecyclerView rvLocalPhotos;
    private RecyclerView rvServerPhotos;
    private PhotoAdapter localPhotoAdapter;
    private ServerPhotoAdapter serverPhotoAdapter;
    private List<PhotoAdapter.PhotoItem> localPhotos = new ArrayList<>();
    private List<ServerPhotosResponse.ServerPhoto> serverPhotos = new ArrayList<>();

    // Agregar variable para guardar el path de la foto actual
    private String currentPhotoPath;

    private FloatingActionButton fabDeletePhotos;

    private MenuItem deleteMenuItem;
    private String plus_code;

    private ImageView clientHeaderImage;
    private Uri headerPhotoUri;
    private String currentHeaderPhotoPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setHasOptionsMenu(true);

        if (!hasPermissions()) {
            requestPermissions();
        }
        if (getArguments() != null) {
            clientName = getArguments().getString("firstName") + " " + getArguments().getString("lastName");
            clientFirstName = getArguments().getString("firstName");
            clientLastName = getArguments().getString("lastName");
            id = getArguments().getString("id");
            typePrice = getArguments().getString("typePrice");
            businessName = getArguments().getString("businessName");
            clientPhone = getArguments().getString("phoneNumber");
            adress = getArguments().getString("adress");
            department = getArguments().getString("department");
            township = getArguments().getString("township");
            position = getArguments().getString("position", "Sin posición");
            visitDay = getArguments().getString("visit_day", "Sin día asignado");

            // Recuperar las coordenadas como String
            clientLatitude = getArguments().getString("latitude", "0.0");
            clientLongitude = getArguments().getString("longitude", "0.0");
            plus_code = getArguments().getString("plus_code", "");

            // Aquí puedes convertir las coordenadas a double si lo necesitas
            try {
                double latitude = Double.parseDouble(clientLatitude);
                double longitude = Double.parseDouble(clientLongitude);
            } catch (NumberFormatException e) {
                // Maneja el error en caso de que las coordenadas no sean válidas
                clientLatitude = "0.0";
                clientLongitude = "0.0";
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_details, container, false);

        // Mostrar los detalles del cliente
        TextView tvClientName = view.findViewById(R.id.tvClientName);
        TextView tvClientPhone = view.findViewById(R.id.tvClientPhone);
        TextView tvAddress = view.findViewById(R.id.tvAddress);
        TextView tvTypePrice = view.findViewById(R.id.tvTypePrice);
        TextView tvBusinessName = view.findViewById(R.id.tvBusinessName);
        TextView tvCoordinates = view.findViewById(R.id.tvCoordinates);
        TextView tvdepartment = view.findViewById(R.id.tvDepartment);
        TextView tvtownship = view.findViewById(R.id.tvTownship);
        TextView tvPosition = view.findViewById(R.id.tvPosition);
        TextView tvVisitDay = view.findViewById(R.id.tvVisitDay);
        btnEditVisitDays = view.findViewById(R.id.btnEditVisitDays);
        btnEditVisitDays.setEnabled(false); // Deshabilitado hasta cargar días
        tvVisitDayGlobal = tvVisitDay;

        Button btnAddPhoto = view.findViewById(R.id.btnAddPhoto);
        Button btnUploadPhotos = view.findViewById(R.id.btnUploadPhotos);
        rvPhotos = view.findViewById(R.id.rvPhotos);

        tvClientName.setText(clientName);
        tvtownship.setText(township);
        tvAddress.setText(adress);
        tvdepartment.setText(department);
        tvClientPhone.setText(clientPhone);
        tvTypePrice.setText(typePrice);
        tvPosition.setText("Posición: " + (position != null ? position : "Sin posición"));
        // Cargar los días de visita usando un método separado
        loadVisitDays(tvVisitDay);

        // Inicializa la lista de días actuales si es null
        if (currentVisitDays == null) currentVisitDays = new ArrayList<>();
        btnEditVisitDays.setOnClickListener(v -> {
            VisitDaysDialog dialog = new VisitDaysDialog(new ArrayList<>(currentVisitDays), this);
            dialog.show(getParentFragmentManager(), "VisitDaysDialog");
        });

        // Obtener los argumentos
        Bundle args = getArguments();
        if (args != null) {
            String firstName = args.getString("firstName", "");
            String lastName = args.getString("lastName", "");
            String phoneNumber = args.getString("phoneNumber", "");
            String address = args.getString("adress", "");
            String typePrice = args.getString("typePrice", "");
            String businessName = args.getString("businessName", "Sin nombre de negocio");

            tvClientName.setText(firstName + " " + lastName);
            tvClientPhone.setText(phoneNumber);
            tvAddress.setText(address);
            tvTypePrice.setText(typePrice);
            tvBusinessName.setText(businessName);
        }

        // Configurar el adaptador para el RecyclerView
        photoAdapter = new PhotoAdapter(new ArrayList<>(), requireContext(), this);
        rvPhotos.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvPhotos.setAdapter(photoAdapter);

        FloatingActionButton fabEdit = view.findViewById(R.id.fabEdit);
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("first_name", clientFirstName);
                bundle.putString("last_name", clientLastName);
                bundle.putString("phone_number", clientPhone);
                bundle.putString("address", adress);
                bundle.putString("department", department);
                bundle.putString("township", township);
                bundle.putString("latitude", clientLatitude);
                bundle.putString("longitude", clientLongitude);
                bundle.putString("client_id", id);
                bundle.putString("business_name", businessName);
                bundle.putString("position", position);
                bundle.putString("visit_day", visitDay);

                try {
                    NavController navController = Navigation.findNavController(v);
                    navController.navigate(R.id.action_clientDetailsFragment_to_editClientFragment, bundle);
                } catch (Exception e) {
                    Log.e("Navigation", "Error navigating to NewClientFragment: " + e.getMessage());
                    Toast.makeText(requireContext(), "Error al navegar", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Evento para el botón que agrega fotos
        btnAddPhoto.setOnClickListener(v -> showPhotoDialog());
        btnUploadPhotos.setOnClickListener(v -> uploadPendingPhotos());

        // Configurar el botón de llamada
        MaterialButton btnCall = view.findViewById(R.id.btnCall);
        btnCall.setOnClickListener(v -> makePhoneCall());

        // Configurar el botón de ver en mapa
        MaterialButton btnViewMap = view.findViewById(R.id.btnViewMap);
        btnViewMap.setOnClickListener(v -> checkLocationAndOpenMap());

        // Dentro del método onCreateView, después de btnViewMap
        MaterialButton btnNewSale = view.findViewById(R.id.fabNewSale);
        btnNewSale.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("clientId", id);
            bundle.putString("clientName", clientFirstName + " " + clientLastName);

            Log.i("TAGASIEMPRE",id +" "+clientFirstName );

            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_clientDetailsFragment_to_newSaleFragment, bundle);
        });

        // Inicializar RecyclerViews
        rvLocalPhotos = view.findViewById(R.id.rvLocalPhotos);
        rvServerPhotos = view.findViewById(R.id.rvServerPhotos);

        // Configurar adapters
        localPhotoAdapter = new PhotoAdapter(localPhotos, requireContext(), this);
        serverPhotoAdapter = new ServerPhotoAdapter(serverPhotos, requireContext());

        // Configurar layouts
        rvLocalPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvServerPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        rvLocalPhotos.setAdapter(localPhotoAdapter);
        rvServerPhotos.setAdapter(serverPhotoAdapter);

        fabDeletePhotos = view.findViewById(R.id.fabDeletePhotos);
        fabDeletePhotos.setOnClickListener(v -> deleteSelectedPhotos());

        // Configurar el adaptador con listener para modo selección
        serverPhotoAdapter.setSelectionModeListener(isSelectionMode -> {
            if (deleteMenuItem != null) {
                // Mostrar el botón solo si hay fotos seleccionadas
                boolean hasSelectedPhotos = !serverPhotoAdapter.getSelectedPhotos().isEmpty();
                deleteMenuItem.setVisible(isSelectionMode && hasSelectedPhotos);
            }
        });

        // Configurar la imagen de cabecera
        clientHeaderImage = view.findViewById(R.id.clientHeaderImage);
        clientHeaderImage.setOnClickListener(v -> showHeaderPhotoDialog());

        return view;
    }

    /**
     * Carga los días de visita del cliente y los muestra en el TextView proporcionado.
     */
    // Mapa para guardar el id_visit_day asociado a cada día
    private HashMap<String, String> visitDayIdMap = new HashMap<>();
    private ArrayList<String> currentVisitDays = new ArrayList<>();
    private MaterialButton btnEditVisitDays;
    private TextView tvVisitDayGlobal;

    private void loadVisitDays(TextView tvVisitDay) {
        // Habilitar el botón solo cuando termine de cargar
        String urlVisitDays = Utilities.URL + "clients/" + id + "/visit-days";
        Log.d("ClientDetailsFragment", "URL días de visita: " + urlVisitDays);
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest visitDaysRequest = new JsonObjectRequest(
                Request.Method.GET,
                urlVisitDays,
                null,
                response -> { // (chequeo se agrega dentro del callback, ya corregido arriba)

                    try {
                        if (!isAdded() || getContext() == null) return;
                        JSONArray visitDaysArray = response.getJSONArray("data");
                        StringBuilder visitDaysBuilder = new StringBuilder();
                        currentVisitDays.clear();
                        visitDayIdMap.clear(); // Limpiar el mapa antes de llenarlo
                        for (int i = 0; i < visitDaysArray.length(); i++) {
                            JSONObject visitDayObj = visitDaysArray.getJSONObject(i);
                            String visitDayStr = visitDayObj.optString("visit_day", "");
                            String positionStr = visitDayObj.has("position") && !visitDayObj.isNull("position") ? visitDayObj.optString("position", "Sin posición") : "Sin posición";
                            String idVisitDay = visitDayObj.optString("id", "");
                            if (!visitDayStr.isEmpty()) {
                                if (visitDaysBuilder.length() > 0) visitDaysBuilder.append(", ");
                                visitDaysBuilder.append("Día: ").append(visitDayStr).append(" (Posición: ").append(positionStr).append(")");
                                currentVisitDays.add(visitDayStr);
                                if (!idVisitDay.isEmpty()) visitDayIdMap.put(visitDayStr, idVisitDay);
                            }
                        }
                        String visitDaysConcat = visitDaysBuilder.toString();
                        tvVisitDay.setText("Días de visita: " + (visitDaysConcat.isEmpty() ? "No asignados" : visitDaysConcat));
                        if (btnEditVisitDays != null) btnEditVisitDays.setEnabled(true);
                    } catch (Exception e) {
                        if (!isAdded() || getContext() == null) return;
                        tvVisitDay.setText("Días de visita: Error al cargar");
                        Log.e("ClientDetailsFragment", "Error procesando días de visita: " + e.getMessage());
                        if (btnEditVisitDays != null) btnEditVisitDays.setEnabled(false);
                    }
                },
                error -> {
                    if (!isAdded() || getContext() == null) return;
                    tvVisitDay.setText("Días de visita: Error de red"+id);
                    String errorMsg = (error.getMessage() != null) ? error.getMessage() : "Sin mensaje";
                    int statusCode = 0;
                    if (error.networkResponse != null) {
                        statusCode = error.networkResponse.statusCode;
                        Log.e("ClientDetailsFragment", "Error en petición de días de visita: statusCode=" + statusCode + ", mensaje=" + errorMsg);
                    } else {
                        Log.e("ClientDetailsFragment", "Error en petición de días de visita: mensaje=" + errorMsg);
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", ClientDetailsFragment.getAuthorizationHeader(getContext()));
                headers.put("Accept", "application/json");
                return headers;
            }
        };;
        queue.add(visitDaysRequest);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ahora llamamos a loadServerPhotos() aquí, después de que la vista está creada
        loadServerPhotos();
        loadProfileImage();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Restaurar la orientación automática cuando se destruye el fragmento
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Limpiar el menú anterior si existe
        menu.clear();
        // Inflar el nuevo menú
        inflater.inflate(R.menu.menu_client_details, menu);
        deleteMenuItem = menu.findItem(R.id.action_delete_photos);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_photos) {
            deleteSelectedPhotos();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Mostrar cuadro de diálogo con opciones para tomar foto o seleccionar de la galería
    private void showPhotoDialog() {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Agregar Foto")
                .setItems(new String[]{"Tomar Foto", "Seleccionar de Galería"}, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else if (which == 1) {
                        openGallery();
                    }
                })
                .show();
    }

    // Abrir la cámara para tomar una foto
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(requireContext(),
                            requireContext().getPackageName() + ".fileprovider",
                            photoFile);

                    // Agregar flags para dar permisos de lectura/escritura
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                    cameraLauncher.launch(takePictureIntent);
                }
            } catch (IOException ex) {
                Toast.makeText(requireContext(),
                        "Error al crear el archivo de imagen",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    // Abrir la galería para seleccionar una o varias fotos
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.putExtra("android.intent.extra.GALLERY", true);

        try {
            galleryLauncher.launch(Intent.createChooser(intent, "Seleccionar fotos de la galería"));
        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    "No se pudo abrir la galería",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Nueva API para manejar la actividad de la cámara
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK) {
                    if (currentPhotoPath != null) {
                        // Usar directamente el path guardado
                        File file = new File(currentPhotoPath);
                        if (file.exists()) {
                            addPhotoToAdapter(currentPhotoPath);
                        } else {
                            Toast.makeText(getContext(),
                                    "No se pudo acceder a la imagen",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Usar el directorio de caché interno de la app
        File storageDir = new File(requireContext().getCacheDir(), "camera_photos");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Manejo de la galería
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        // Múltiples fotos seleccionadas
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                            if (imageUri != null) {
                                String realPath = getRealPathFromURI(imageUri);
                                if (realPath != null) {
                                    addPhotoToAdapter(realPath);
                                }
                            }
                        }
                    } else if (result.getData().getData() != null) {
                        // Una sola foto seleccionada
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            String realPath = getRealPathFromURI(imageUri);
                            if (realPath != null) {
                                addPhotoToAdapter(realPath);
                            }
                        }
                    }
                }
            });


    private boolean hasPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private ActivityResultLauncher<String[]> multiplePermissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean allGranted = true;
                for (Boolean isGranted : permissions.values()) {
                    if (!isGranted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    Toast.makeText(getContext(), "Permisos concedidos", Toast.LENGTH_SHORT).show();
                } else {
                }
            });

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            String[] permissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES
            };
            multiplePermissionsLauncher.launch(permissions);
        } else {
            String[] permissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            multiplePermissionsLauncher.launch(permissions);
        }
    }



    private ActivityResultLauncher<String> callPhonePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startCall();
                } else {
                    Toast.makeText(getContext(),
                            "Se necesita permiso para realizar llamadas",
                            Toast.LENGTH_SHORT).show();
                }
            });



    private void makePhoneCall() {
        if (clientPhone != null && !clientPhone.isEmpty()) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                callPhonePermissionLauncher.launch(Manifest.permission.CALL_PHONE);
            } else {
                startCall();
            }
        } else {
            Toast.makeText(getContext(), "No hay número de teléfono disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCall() {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + clientPhone));
            startActivity(intent);
        } catch (SecurityException e) {
            Toast.makeText(getContext(), "Error al realizar la llamada", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Permiso de ubicación")
                        .setMessage("Para un mejor seguimiento de rutas, necesitamos acceder a tu ubicación todo el tiempo. ¿Deseas permitirlo?")
                        .setPositiveButton("Configurar", (dialog, which) -> {
                            requestBackgroundLocationPermission();
                        })
                        .setNegativeButton("No", null)
                        .create()
                        .show();
            }
        }
    }

    private ActivityResultLauncher<String> backgroundLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    Toast.makeText(getContext(), "Permiso de ubicación en segundo plano concedido", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Permiso de ubicación en segundo plano denegado", Toast.LENGTH_SHORT).show();
                }
            });

    private void requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
    }

    // Agregar un nuevo launcher para los permisos de ubicación
    private ActivityResultLauncher<String[]> locationPermissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean allGranted = true;
                for (Boolean isGranted : permissions.values()) {
                    if (!isGranted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    // Si se conceden los permisos, verificar GPS y continuar
                    checkGPSAndProceed();
                } else {
                    Toast.makeText(getContext(), "Se necesitan permisos de ubicación", Toast.LENGTH_SHORT).show();
                }
            });

    // Modificar el método checkLocationAndOpenMap
    private void checkLocationAndOpenMap() {
        if (clientLatitude == null || clientLongitude == null ||
                clientLatitude.equals("0.0") || clientLongitude.equals("0.0")) {
            Toast.makeText(getContext(), "No hay coordenadas disponibles para este cliente", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Si el GPS está apagado, mostrar diálogo para activarlo
            new AlertDialog.Builder(requireContext())
                    .setTitle("GPS Desactivado")
                    .setMessage("El GPS está desactivado. ¿Desea activarlo?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        locationSettingsLauncher.launch(intent);
                    })
                    .setNegativeButton("No", (dialog, which) ->
                            Toast.makeText(getContext(), "Se requiere GPS para ver la ubicación", Toast.LENGTH_SHORT).show())
                    .create()
                    .show();
        } else {
            // Si el GPS está encendido, abrir directamente Google Maps
            openGoogleMaps();
        }
    }

    // Nuevo método para verificar GPS
    private void checkGPSAndProceed() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Si el GPS está apagado, mostrar diálogo para activarlo
            new AlertDialog.Builder(requireContext())
                    .setTitle("GPS Desactivado")
                    .setMessage("El GPS está desactivado. ¿Desea activarlo?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        locationSettingsLauncher.launch(intent);
                    })
                    .setNegativeButton("No", (dialog, which) ->
                            Toast.makeText(getContext(), "Se requiere GPS para ver la ubicación", Toast.LENGTH_SHORT).show())
                    .create()
                    .show();
        } else {
            // Si el GPS está encendido, verificar permiso de ubicación en segundo plano
            checkBackgroundLocationPermission();
            openGoogleMaps();
        }
    }

    private final ActivityResultLauncher<Intent> locationSettingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    openGoogleMaps();
                } else {
                    Toast.makeText(getContext(), "Se requiere GPS para ver la ubicación", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void openGoogleMaps() {
        try {
            // Codificar el Plus Code para asegurarse de que el signo '+' se maneje correctamente
            String encodedPlusCode = URLEncoder.encode(plus_code, "UTF-8");

            // Crear URI para Google Maps con el Plus Code codificado
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + encodedPlusCode);;

            // Crear intent para abrir Google Maps
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);

        } catch (UnsupportedEncodingException e) {
            // Si ocurre un error en la codificación, mostrar mensaje de error
            Toast.makeText(getContext(), "Error al codificar el Plus Code", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Si ocurre cualquier otro error, mostrar mensaje genérico
            Toast.makeText(getContext(), "Error al abrir el mapa", Toast.LENGTH_SHORT).show();
        }
    }    @Override
    public void onPhotoClick(int position) {
        if (position >= 0 && position < photos.size()) {
            PhotoAdapter.PhotoItem photoItem = photos.get(position);
            String photoPath = photoItem.getPath();

            // Eliminar el archivo si es local
            if (!photoPath.startsWith("http")) {
                File photoFile = new File(photoPath);
                if (photoFile.exists()) {
                    photoFile.delete();
                }
            }

            // Eliminar de la lista y actualizar el adaptador
            photos.remove(position);
            photoAdapter.updatePhotos(photos);

            // Mostrar mensaje de confirmación
        }
    }

    /**
     * Maneja la subida de una foto al servidor
     * @param imagePath Ruta de la imagen a subir
     */

    /**
     * Maneja los errores durante la subida de fotos
     */
    private void handleUploadError() {
        requireActivity().runOnUiThread(() -> {
            // Remover la última foto agregada si hubo error
            int position = photos.size() - 1;
            if (position >= 0) {
                photos.remove(position);
                photoAdapter.updatePhotos(photos);
            }
        });
    }

    /**
     * Obtiene el token de autenticación almacenado
     * @return Token de autenticación
     */


    /**
     * Convierte una URI de contenido a una ruta de archivo real
     * @param uri URI a convertir
     * @return Ruta real del archivo o null si no se puede obtener
     */
    private String getRealPathFromURI(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    if (columnIndex > -1) {
                        result = cursor.getString(columnIndex);
                    }
                }
            } finally {
                cursor.close();
            }
        } else if (uri.getScheme().equals("file")) {
            result = uri.getPath();
        }
        return result;
    }

    /**
     * Sube todas las fotos pendientes al servidor
     */
    private void uploadPendingPhotos() {
        List<PhotoAdapter.PhotoItem> pendingPhotos = photoAdapter.getPendingPhotos();
        if (pendingPhotos.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No hay fotos pendientes de subir",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Contador para rastrear las fotos subidas
        final int[] uploadedCount = {0};
        final int totalPhotos = pendingPhotos.size();

        // Subir fotos secuencialmente
        uploadNextPhoto(pendingPhotos, 0, uploadedCount, totalPhotos);
    }

    private void uploadNextPhoto(List<PhotoAdapter.PhotoItem> pendingPhotos, int currentIndex,
                                 final int[] uploadedCount, final int totalPhotos) {
        // Verificar si hemos terminado de procesar todas las fotos
        if (currentIndex >= pendingPhotos.size()) {
            requireActivity().runOnUiThread(() -> {
                photoAdapter.updatePhotos(photos);
            });
            return;
        }

        PhotoAdapter.PhotoItem photo = pendingPhotos.get(currentIndex);

        // Encontrar el índice correcto en la lista principal
        int mainListIndex = -1;
        for (int i = 0; i < photos.size(); i++) {
            if (photos.get(i).getPath().equals(photo.getPath())) {
                mainListIndex = i;
                break;
            }
        }

        final int photoIndex = mainListIndex;

        // Actualizar UI para mostrar que la foto está en proceso de subida
        requireActivity().runOnUiThread(() -> {
            if (photoIndex != -1) {
                photos.get(photoIndex).setUploading(true);
                photoAdapter.notifyItemChanged(photoIndex);
            }
        });

        // Verificar si el archivo existe
        File imageFile = new File(photo.getPath());
        if (!imageFile.exists()) {
            uploadNextPhoto(pendingPhotos, currentIndex + 1, uploadedCount, totalPhotos);
            return;
        }

        // Preparar el archivo para la subida
        RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/jpeg"),
                imageFile
        );

        MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                "image",
                "photo.jpg",
                requestFile
        );

        String token = Login.getAuthorizationHeader(requireContext());

        // Realizar la petición al servidor
        RetrofitClient.getInstance().getApiService()
                .uploadBusinessImage(id, imagePart, token)
                .enqueue(new Callback<PhotoResponse>() {
                    @Override
                    public void onResponse(Call<PhotoResponse> call, Response<PhotoResponse> response) {
                        // Actualizar UI para mostrar que la foto ya no está en proceso de subida
                        requireActivity().runOnUiThread(() -> {
                            if (photoIndex != -1) {
                                photos.get(photoIndex).setUploading(false);
                                photoAdapter.notifyItemChanged(photoIndex);
                            }
                        });

                        // Verificar si la respuesta del servidor fue exitosa
                        if (response.isSuccessful() && response.body() != null) {
                            PhotoResponse photoResponse = response.body();
                            uploadedCount[0]++;

                            requireActivity().runOnUiThread(() -> {
                                try {
                                    if (photoIndex != -1) {
                                        // Obtener la foto antes de removerla
                                        PhotoAdapter.PhotoItem localPhoto = photos.get(photoIndex);

                                        // Crear nueva foto para el servidor usando el ID de la respuesta y manteniendo el path local
                                        ServerPhotosResponse.ServerPhoto newServerPhoto = new ServerPhotosResponse.ServerPhoto(
                                                photoResponse.getData().getId(), // Usar el ID de la respuesta
                                                "business",
                                                "file://" + localPhoto.getPath() // Mantener el path local
                                        );

                                        // Agregar al adapter del servidor
                                        serverPhotos.add(newServerPhoto);
                                        serverPhotoAdapter.notifyItemInserted(serverPhotos.size() - 1);

                                        // Remover del adapter local
                                        photos.remove(photoIndex);
                                        photoAdapter.updatePhotos(photos);
                                    }
                                } catch (Exception e) {
                                    Log.e("PhotoUpload", "Error al mover foto entre adapters: " + e.getMessage());
                                }
                            });

                            // Continuar con la siguiente foto
                            uploadNextPhoto(pendingPhotos, currentIndex + 1, uploadedCount, totalPhotos);
                        } else {
                            // Manejar error en la respuesta
                            handleUploadError();
                            // Continuar con la siguiente foto a pesar del error
                            uploadNextPhoto(pendingPhotos, currentIndex + 1, uploadedCount, totalPhotos);
                        }
                    }

                    @Override
                    public void onFailure(Call<PhotoResponse> call, Throwable t) {
                        // Manejar error de conexión
                        requireActivity().runOnUiThread(() -> {
                            if (photoIndex != -1) {
                                photos.get(photoIndex).setUploading(false);
                                photoAdapter.notifyItemChanged(photoIndex);
                            }
                            handleUploadError();
                        });
                        // Continuar con la siguiente foto a pesar del error
                        uploadNextPhoto(pendingPhotos, currentIndex + 1, uploadedCount, totalPhotos);
                    }
                });
    }

    private void handleUploadError(int position) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(),
                    "Error al subir la foto " + (position + 1),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void addPhotoToAdapter(String photoPath) {
        if (photoPath != null) {
            File imageFile = new File(photoPath);
            if (imageFile.exists()) {
                try {
                    // Obtener la orientación EXIF de la imagen
                    int rotation = getImageRotation(photoPath);

                    // Obtener dimensiones de la imagen original
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(photoPath, options);
                    int imageWidth = options.outWidth;
                    int imageHeight = options.outHeight;

                    float originalSizeKB = imageFile.length() / 1024f;
                    Log.d("PhotoCompression", "Tamaño original: " + originalSizeKB + " KB");

                    if (originalSizeKB <= 200 && rotation == 0) {
                        // Si la imagen ya es pequeña y no necesita rotación, usarla directamente
                        Log.d("PhotoCompression", "La imagen ya es suficientemente pequeña y no necesita rotación");
                        PhotoAdapter.PhotoItem newPhoto = new PhotoAdapter.PhotoItem(photoPath, false);
                        photos.add(newPhoto);
                        photoAdapter.updatePhotos(photos);
                        return;
                    }

                    // Calcular el factor de escala óptimo manteniendo el aspect ratio
                    int targetWidth = 1280; // Ancho objetivo
                    float ratio = (float) imageWidth / imageHeight;
                    int targetHeight = (int) (targetWidth / ratio);

                    // Ajustar dimensiones si la altura es muy grande
                    if (targetHeight > 1280) {
                        targetHeight = 1280;
                        targetWidth = (int) (targetHeight * ratio);
                    }

                    // Configurar opciones de decodificación
                    options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                    // Decodificar la imagen original
                    Bitmap originalBitmap = BitmapFactory.decodeFile(photoPath, options);
                    Bitmap workingBitmap = originalBitmap;

                    // Escalar la imagen manteniendo la calidad
                    if (imageWidth > targetWidth || imageHeight > targetHeight) {
                        workingBitmap = Bitmap.createScaledBitmap(
                            originalBitmap,
                            targetWidth,
                            targetHeight,
                            true
                        );
                        originalBitmap.recycle(); // Liberar memoria del bitmap original
                    }

                    // Rotar si es necesario
                    if (rotation != 0) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(rotation);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(
                            workingBitmap, 0, 0,
                            workingBitmap.getWidth(), workingBitmap.getHeight(),
                            matrix, true
                        );
                        if (workingBitmap != originalBitmap) {
                            workingBitmap.recycle();
                        }
                        workingBitmap = rotatedBitmap;
                    }

                    // Comprimir con calidad progresiva
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int quality = 100;
                    workingBitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);

                    // Reducir calidad gradualmente si es necesario
                    while (bos.size() > 200 * 1024 && quality > 60) {
                        bos.reset();
                        quality -= 5;
                        workingBitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                    }

                    // Si aún es muy grande, intentar reducir más la calidad pero no tanto
                    if (bos.size() > 200 * 1024) {
                        while (bos.size() > 200 * 1024 && quality > 40) {
                            bos.reset();
                            quality -= 2;
                            workingBitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                        }
                    }

                    float finalSizeKB = bos.size() / 1024f;
                    Log.d("PhotoCompression", String.format(
                        "Tamaño final: %.2f KB, Calidad: %d%%, Dimensiones: %dx%d, Rotación: %d°",
                        finalSizeKB, quality, workingBitmap.getWidth(), workingBitmap.getHeight(), rotation
                    ));

                    File optimizedFile = new File(requireContext().getCacheDir(),
                        "optimized_" + imageFile.getName());
                    FileOutputStream fos = new FileOutputStream(optimizedFile);
                    fos.write(bos.toByteArray());
                    fos.close();

                    // Liberar memoria del último bitmap
                    if (workingBitmap != originalBitmap) {
                        workingBitmap.recycle();
                    }

                    PhotoAdapter.PhotoItem newPhoto = new PhotoAdapter.PhotoItem(
                        optimizedFile.getAbsolutePath(),
                        false
                    );
                    photos.add(newPhoto);
                    photoAdapter.updatePhotos(photos);

                } catch (Exception e) {
                    Log.e("TAGASIEMPRE", e.toString());
                    Toast.makeText(requireContext(),
                        "Error al procesar la imagen",
                        Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }
    private int getImageRotation(String photoPath) {
        try {
            ExifInterface exif = new ExifInterface(photoPath);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void loadServerPhotos() {
        if (!isAdded() || getContext() == null) {
            return;
        }

        ProgressBar headerProgress = requireView().findViewById(R.id.headerImageProgress);
        headerProgress.setVisibility(View.VISIBLE);

        String token = Login.getAuthorizationHeader(requireContext());
        RetrofitClient.getInstance().getApiService()
                .getClientImages(id, token)
                .enqueue(new Callback<ServerPhotosResponse>() {
                    @Override
                    public void onResponse(Call<ServerPhotosResponse> call, Response<ServerPhotosResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            serverPhotos.clear();
                            serverPhotos.addAll(response.body().getPhotos());
                            serverPhotoAdapter.notifyDataSetChanged();

                            // Imprimir en el log cuando la respuesta es exitosa
                            Log.d("LoadServerPhotos", "Fotos cargadas exitosamente. Total: " + serverPhotos.size());

                            // Construir una cadena de texto para representar el arreglo
                            StringBuilder photosInfo = new StringBuilder("Fotos: [");
                            for (ServerPhotosResponse.ServerPhoto photo : serverPhotos) {
                                photosInfo.append("{ID: ").append(photo.getId())
                                        .append(", Path: ").append(photo.getPath())
                                        .append("}, ");
                            }
                            if (!serverPhotos.isEmpty()) {
                                photosInfo.setLength(photosInfo.length() - 2); // Eliminar la última coma y espacio
                            }
                            photosInfo.append("]");

                            // Imprimir el arreglo en el log
                            Log.d("LoadServerPhotos", photosInfo.toString());
                        } else {
                            // Imprimir en el log si la respuesta no es exitosa
                            Log.e("LoadServerPhotos", "Error al cargar fotos: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ServerPhotosResponse> call, Throwable t) {
                        // Imprimir en el log si hay un fallo en la solicitud
                        Log.e("LoadServerPhotos", "Fallo al cargar fotos del servidor: " + t.getMessage());
                        Toast.makeText(requireContext(),
                                "Error al cargar las fotos del servidor",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteSelectedPhotos() {
        Set<Integer> selectedIds = serverPhotoAdapter.getSelectedPhotos();
        if (selectedIds.isEmpty()) {
            Toast.makeText(requireContext(), "No hay fotos seleccionadas", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar fotos")
                .setMessage("¿Está seguro que desea eliminar las " + selectedIds.size() + " fotos seleccionadas?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    String token = Login.getAuthorizationHeader(requireContext());

                    for (Integer photoId : selectedIds) {
                        serverPhotoAdapter.setPhotoDeleting(photoId, true);

                        RetrofitClient.getInstance().getApiService()
                                .deleteMedia(photoId, token)
                                .enqueue(new Callback<MessageResponse>() {
                                    @Override
                                    public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                                        requireActivity().runOnUiThread(() -> {
                                            if (response.isSuccessful()) {
                                                serverPhotoAdapter.removePhoto(photoId);

                                            } else {
                                                serverPhotoAdapter.setPhotoDeleting(photoId, false);
                                                Toast.makeText(requireContext(),
                                                        "Error al eliminar la foto",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Call<MessageResponse> call, Throwable t) {
                                        requireActivity().runOnUiThread(() -> {
                                            serverPhotoAdapter.setPhotoDeleting(photoId, false);
                                            Toast.makeText(requireContext(),
                                                    "Error de conexión al eliminar la foto",
                                                    Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showHeaderPhotoDialog() {
        // Crear array de opciones dependiendo si hay imagen o no
        String[] options;
        if (clientHeaderImage.getDrawable() != null && 
            !(clientHeaderImage.getDrawable() instanceof ColorDrawable)) {
            options = new String[]{"Ver Foto", "Tomar Foto", "Seleccionar de Galería"};
        } else {
            options = new String[]{"Tomar Foto", "Seleccionar de Galería"};
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Foto de perfil")
                .setItems(options, (dialog, which) -> {
                    if (options.length == 3) {
                        // Si hay 3 opciones, "Ver Foto" es la primera
                        switch (which) {
                            case 0:
                                showFullscreenImage();
                                break;
                            case 1:
                                openHeaderCamera();
                                break;
                            case 2:
                                openHeaderGallery();
                                break;
                        }
                    } else {
                        // Si hay 2 opciones, no hay "Ver Foto"
                        if (which == 0) {
                            openHeaderCamera();
                        } else {
                            openHeaderGallery();
                        }
                    }
                })
                .show();
    }

    private void showFullscreenImage() {
        if (!isAdded() || getContext() == null) {
            return;
        }

        ProgressBar headerProgress = requireView().findViewById(R.id.headerImageProgress);
        headerProgress.setVisibility(View.VISIBLE);

        String token = Login.getAuthorizationHeader(requireContext());
        RetrofitClient.getInstance().getApiService()
                .getProfileImage(id, token)
                .enqueue(new Callback<PhotoResponse>() {
                    @Override
                    public void onResponse(Call<PhotoResponse> call, Response<PhotoResponse> response) {
                        if (response.isSuccessful() && response.body() != null && 
                            response.body().getData() != null && 
                            response.body().getData().getPath() != null) {
                            headerProgress.setVisibility(View.GONE);

                            String imagePath = response.body().getData().getPath();
                            String imageUrl = Utilities.URL_FOTOS + "storage/" + imagePath;
                            
                            // Mostrar la imagen en pantalla completa
                            requireActivity().runOnUiThread(() -> {
                                Uri imageUri = Uri.parse(imageUrl);
                                FullscreenImageDialog dialog = new FullscreenImageDialog(requireContext(), imageUri);
                                dialog.show();
                            });
                        } else {
                            Toast.makeText(requireContext(), 
                                "No se pudo cargar la imagen", 
                                Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PhotoResponse> call, Throwable t) {
                        headerProgress.setVisibility(View.GONE);

                        Toast.makeText(requireContext(), 
                            "Error al cargar la imagen",

                            Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openHeaderCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createHeaderImageFile();
                if (photoFile != null) {
                    headerPhotoUri = FileProvider.getUriForFile(requireContext(),
                            requireContext().getPackageName() + ".fileprovider",
                            photoFile);

                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, headerPhotoUri);

                    headerCameraLauncher.launch(takePictureIntent);
                }
            } catch (IOException ex) {
                Toast.makeText(requireContext(),
                        "Error al crear el archivo de imagen",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openHeaderGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        headerGalleryLauncher.launch(intent);
    }

    private File createHeaderImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "HEADER_" + timeStamp + "_";

        File storageDir = new File(requireContext().getCacheDir(), "profile_photos");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentHeaderPhotoPath = image.getAbsolutePath();
        return image;
    }

    private final ActivityResultLauncher<Intent> headerCameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK) {
                    if (currentHeaderPhotoPath != null) {
                        processAndUploadHeaderPhoto(currentHeaderPhotoPath);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> headerGalleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        String realPath = getRealPathFromURI(selectedImageUri);
                        if (realPath != null) {
                            processAndUploadHeaderPhoto(realPath);
                        }
                    }
                }
            }
    );

    private void processAndUploadHeaderPhoto(String photoPath) {
        if (photoPath != null) {
            File imageFile = new File(photoPath);
            if (imageFile.exists()) {
                try {
                    // Obtener la orientación EXIF de la imagen
                    int rotation = getImageRotation(photoPath);

                    // Obtener dimensiones de la imagen original
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(photoPath, options);
                    int imageWidth = options.outWidth;
                    int imageHeight = options.outHeight;

                    float originalSizeKB = imageFile.length() / 1024f;
                    Log.d("PhotoCompression", "Tamaño original: " + originalSizeKB + " KB");

                    if (originalSizeKB <= 200 && rotation == 0) {
                        // Si la imagen ya es pequeña y no necesita rotación, usarla directamente
                        Log.d("PhotoCompression", "La imagen ya es suficientemente pequeña y no necesita rotación");
                        uploadHeaderPhoto(imageFile);
                        return;
                    }

                    // Calcular el factor de escala óptimo manteniendo el aspect ratio
                    int targetWidth = 1280; // Ancho objetivo
                    float ratio = (float) imageWidth / imageHeight;
                    int targetHeight = (int) (targetWidth / ratio);

                    // Ajustar dimensiones si la altura es muy grande
                    if (targetHeight > 1280) {
                        targetHeight = 1280;
                        targetWidth = (int) (targetHeight * ratio);
                    }

                    // Configurar opciones de decodificación
                    options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                    // Decodificar la imagen original
                    Bitmap originalBitmap = BitmapFactory.decodeFile(photoPath, options);
                    Bitmap workingBitmap = originalBitmap;

                    // Escalar la imagen manteniendo la calidad
                    if (imageWidth > targetWidth || imageHeight > targetHeight) {
                        workingBitmap = Bitmap.createScaledBitmap(
                            originalBitmap,
                            targetWidth,
                            targetHeight,
                            true
                        );
                        originalBitmap.recycle(); // Liberar memoria del bitmap original
                    }

                    // Rotar si es necesario
                    if (rotation != 0) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(rotation);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(
                            workingBitmap, 0, 0,
                            workingBitmap.getWidth(), workingBitmap.getHeight(),
                            matrix, true
                        );
                        if (workingBitmap != originalBitmap) {
                            workingBitmap.recycle();
                        }
                        workingBitmap = rotatedBitmap;
                    }

                    // Comprimir con calidad progresiva
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int quality = 100;
                    workingBitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);

                    // Reducir calidad gradualmente si es necesario
                    while (bos.size() > 200 * 1024 && quality > 60) {
                        bos.reset();
                        quality -= 5;
                        workingBitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                    }

                    // Si aún es muy grande, intentar reducir más la calidad pero no tanto
                    if (bos.size() > 200 * 1024) {
                        while (bos.size() > 200 * 1024 && quality > 40) {
                            bos.reset();
                            quality -= 2;
                            workingBitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                        }
                    }

                    float finalSizeKB = bos.size() / 1024f;
                    Log.d("PhotoCompression", String.format(
                        "Tamaño final: %.2f KB, Calidad: %d%%, Dimensiones: %dx%d, Rotación: %d°",
                        finalSizeKB, quality, workingBitmap.getWidth(), workingBitmap.getHeight(), rotation
                    ));

                    File optimizedFile = new File(requireContext().getCacheDir(),
                        "profile_" + imageFile.getName());
                    FileOutputStream fos = new FileOutputStream(optimizedFile);
                    fos.write(bos.toByteArray());
                    fos.close();

                    // Subir la imagen al servidor
                    uploadHeaderPhoto(optimizedFile);

                } catch (Exception e) {
                    resetHeaderImage();
                    Log.e("TAGASIEMPRE", e.toString());
                    Toast.makeText(requireContext(),
                        "Error al procesar la imagen",
                        Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadHeaderPhoto(File imageFile) {
        // Mostrar el ProgressBar
        ProgressBar headerProgress = clientHeaderImage.getRootView().findViewById(R.id.headerImageProgress);
        headerProgress.setVisibility(View.VISIBLE);

        RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/jpeg"),
                imageFile
        );

        MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                "image",
                imageFile.getName(),
                requestFile
        );

        String token = Login.getAuthorizationHeader(requireContext());

        RetrofitClient.getInstance().getApiService()
                .uploadProfileImage(id, imagePart, token)
                .enqueue(new Callback<PhotoResponse>() {
                    @Override
                    public void onResponse(Call<PhotoResponse> call, Response<PhotoResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // La subida fue exitosa, obtener la URL del servidor
                            if (response.body().getData() != null && response.body().getData().getPath() != null) {
                                String serverImagePath = response.body().getData().getPath();
                                String imageUrl = Utilities.URL_FOTOS + "storage/" + serverImagePath;

                                // Cargar la imagen desde el servidor
                                requireActivity().runOnUiThread(() -> {
                                    Glide.with(requireContext())
                                            .load(imageUrl)
                                            .placeholder(R.drawable.product_placeholder)
                                            .error(R.drawable.product_placeholder)
                                            .listener(new RequestListener<Drawable>() {
                                                @Override
                                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                    headerProgress.setVisibility(View.GONE);
                                                    return false;
                                                }

                                                @Override
                                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                    headerProgress.setVisibility(View.GONE);
                                                    return false;
                                                }
                                            })
                                            .into(clientHeaderImage);

                                    // Actualizar el item en la lista de ClientsFragment
                                    ClientsViewModel clientsViewModel = new ViewModelProvider(requireActivity()).get(ClientsViewModel.class);
                                    clientsViewModel.updateClientProfileImage(id, serverImagePath);
                                });
                            }

                            // Mostrar mensaje de éxito
                            String message = response.body().getMessage();
                            if (message != null && !message.isEmpty()) {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            resetHeaderImage();
                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    Log.e("UploadError", "Error response: " + errorBody);
                                    JSONObject errorJson = new JSONObject(errorBody);

                                    String errorMessage;
                                    if (errorJson.has("errors") && errorJson.getJSONObject("errors").has("image")) {
                                        JSONArray imageErrors = errorJson.getJSONObject("errors").getJSONArray("image");
                                        errorMessage = imageErrors.getString(0);
                                    } else {
                                        errorMessage = errorJson.optString("message", "Error al subir la imagen de perfil");
                                    }

                                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(),
                                            "Error al subir la imagen de perfil",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e("UploadError", "Error parsing error response: " + e.getMessage());
                                Toast.makeText(requireContext(),
                                        "Error al subir la imagen de perfil",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<PhotoResponse> call, Throwable t) {
                        // Ocultar el ProgressBar
                        headerProgress.setVisibility(View.GONE);
                        
                        resetHeaderImage();
                        Log.e("UploadError", "Network error: " + t.getMessage());
                        Toast.makeText(requireContext(),
                                "Error de conexión al subir la imagen: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetHeaderImage() {
        requireActivity().runOnUiThread(() -> {
            clientHeaderImage.setImageResource(R.drawable.cliente_icon);
        });
    }

    private void loadProfileImage() {
        if (!isAdded() || getContext() == null) {
            return;
        }

        String token = Login.getAuthorizationHeader(requireContext());
        
        // Ahora es seguro obtener el ProgressBar porque la vista ya existe
        ProgressBar headerProgress = requireView().findViewById(R.id.headerImageProgress);
        headerProgress.setVisibility(View.VISIBLE);

        RetrofitClient.getInstance().getApiService()
                .getProfileImage(id, token)
                .enqueue(new Callback<PhotoResponse>() {
                    @Override
                    public void onResponse(Call<PhotoResponse> call, Response<PhotoResponse> response) {
                        if (!isAdded() || getContext() == null) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            String imagePath = response.body().getData().getPath();
                            if (imagePath != null && !imagePath.isEmpty()) {
                                String imageUrl = Utilities.URL_FOTOS + "storage/" + imagePath;

                                // Cargar la imagen usando Glide
                                Glide.with(requireContext())
                                        .load(imageUrl)
                                        .placeholder(R.drawable.product_placeholder)
                                        .error(R.drawable.product_placeholder)
                                        .listener(new RequestListener<Drawable>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                if (isAdded() && getContext() != null) {
                                                    headerProgress.setVisibility(View.GONE);
                                                }
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                if (isAdded() && getContext() != null) {
                                                    headerProgress.setVisibility(View.GONE);
                                                }
                                                return false;
                                            }
                                        })
                                        .into(clientHeaderImage);
                            } else {
                                if (isAdded() && getContext() != null) {
                                    headerProgress.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            if (isAdded() && getContext() != null) {
                                headerProgress.setVisibility(View.GONE);
                                Log.d("ProfileImage", "No profile image found or error in response");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<PhotoResponse> call, Throwable t) {
                        if (isAdded() && getContext() != null) {
                            headerProgress.setVisibility(View.GONE);
                            Log.e("ProfileImage", "Error loading profile image: " + t.getMessage());
                        }
                    }
                });
    }

    public static String getAuthorizationHeader(Context context) {


        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        String tokenType = sharedPreferences.getString(TOKEN_TYPE, "Bearer");

        if (token != null) {
            return tokenType + " " + token;
        }
        return null;
    }


}