package com.jugos_jaco_app.ui.clients;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jugos_jaco_app.Login;
import com.jugos_jaco_app.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

public class ClientDetailsFragment extends Fragment implements PhotoAdapter.OnPhotoListener {

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

    private RecyclerView rvLocalPhotos;
    private RecyclerView rvServerPhotos;
    private PhotoAdapter localPhotoAdapter;
    private ServerPhotoAdapter serverPhotoAdapter;
    private List<PhotoAdapter.PhotoItem> localPhotos = new ArrayList<>();
    private List<ServerPhotosResponse.ServerPhoto> serverPhotos = new ArrayList<>();

    // Agregar variable para guardar el path de la foto actual
    private String currentPhotoPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!hasPermissions()) {
            requestPermissions();
        }
        if (getArguments() != null) {
            clientName = getArguments().getString("firstName") + " " + getArguments().getString("lastName");
            clientFirstName = getArguments().getString("firstName") ;
             clientLastName = getArguments().getString("lastName");
            id = getArguments().getString("id");
            typePrice = getArguments().getString("typePrice");

            clientPhone = getArguments().getString("phoneNumber");
            adress = getArguments().getString("adress");
            department = getArguments().getString("department");
            township = getArguments().getString("township");


            // Recuperar las coordenadas como String
            clientLatitude = getArguments().getString("latitude", "0.0");
            clientLongitude = getArguments().getString("longitude", "0.0");
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
        TextView tvName = view.findViewById(R.id.tvClientName);
        TextView tvadress = view.findViewById(R.id.tvAddress);
        FloatingActionButton fabEdit = view.findViewById(R.id.fabEdit);

        TextView tvPhone = view.findViewById(R.id.tvClientPhone);
        TextView tvCoordinates = view.findViewById(R.id.tvCoordinates);
        TextView tvdepartment = view.findViewById(R.id.tvDepartment);
        TextView tvtownship = view.findViewById(R.id.tvTownship);
        TextView tvTypePrice = view.findViewById(R.id.tvTypePrice);

        Button btnAddPhoto = view.findViewById(R.id.btnAddPhoto);
        Button btnUploadPhotos = view.findViewById(R.id.btnUploadPhotos);
        rvPhotos = view.findViewById(R.id.rvPhotos);

        tvName.setText(clientName);
        tvtownship.setText(township);
        tvadress.setText(adress);
         tvdepartment.setText(department);
        tvPhone.setText(clientPhone);
        tvTypePrice.setText(typePrice);

        if (clientLatitude != null && clientLongitude != null) {
            tvCoordinates.setText("Latitud: " + clientLatitude + ", Longitud: " + clientLongitude);
        } else {
            tvCoordinates.setText("Sin coordenadas");
        }

        // Configurar el adaptador para el RecyclerView
        photoAdapter = new PhotoAdapter(new ArrayList<>(), requireContext(), this);
        rvPhotos.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvPhotos.setAdapter(photoAdapter);

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
               // bundle.putString("type_price", typePrice);

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

        // Cargar fotos del servidor
        loadServerPhotos();

        return view;
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

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Permitir selección múltiple
        galleryLauncher.launch(galleryIntent);
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
            // Crear URI para Google Maps con las coordenadas del cliente
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + clientLatitude + "," + clientLongitude);

            // Crear intent para abrir Google Maps
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Verificar si Google Maps está instalado
            if (mapIntent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Si Google Maps no está instalado, abrir en el navegador
                Uri browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" +
                        clientLatitude + "," + clientLongitude);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                startActivity(browserIntent);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al abrir el mapa", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
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
        
        requireActivity().runOnUiThread(() -> {
            if (photoIndex != -1) {
                photos.get(photoIndex).setUploading(true);
                photoAdapter.notifyItemChanged(photoIndex);
            }
        });

        File imageFile = new File(photo.getPath());
        if (!imageFile.exists()) {
            uploadNextPhoto(pendingPhotos, currentIndex + 1, uploadedCount, totalPhotos);
            return;
        }

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

        RetrofitClient.getApiService()
            .uploadBusinessImage(id, imagePart, token)
            .enqueue(new Callback<PhotoResponse>() {
                @Override
                public void onResponse(Call<PhotoResponse> call, Response<PhotoResponse> response) {
                    requireActivity().runOnUiThread(() -> {
                        if (photoIndex != -1) {
                            photos.get(photoIndex).setUploading(false);
                            photoAdapter.notifyItemChanged(photoIndex);
                        }
                    });
                    
                    if (response.isSuccessful() && response.body() != null) {
                        PhotoResponse photoResponse = response.body();
                        uploadedCount[0]++;
                        
                        requireActivity().runOnUiThread(() -> {
                            try {
                                if (photoIndex != -1) {
                                    photos.remove(photoIndex);
                                    photoAdapter.updatePhotos(photos);
                                }
                                
                                loadServerPhotos();

                                String message = photoResponse.getMessage();
                                if (message != null && !message.isEmpty()) {
                                 }
                            } catch (Exception e) {
                                Log.e("PhotoUpload", "Error al remover foto: " + e.getMessage());
                            }
                        });

                        uploadNextPhoto(pendingPhotos, currentIndex + 1, uploadedCount, totalPhotos);
                    } else {
                        handleUploadError(currentIndex);
                        uploadNextPhoto(pendingPhotos, currentIndex + 1, uploadedCount, totalPhotos);
                    }
                }

                @Override
                public void onFailure(Call<PhotoResponse> call, Throwable t) {
                    requireActivity().runOnUiThread(() -> {
                        if (photoIndex != -1) {
                            photos.get(photoIndex).setUploading(false);
                            photoAdapter.notifyItemChanged(photoIndex);
                        }
                        handleUploadError(currentIndex);
                    });
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
                    
                    // Escalar la imagen manteniendo la calidad
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                        originalBitmap, 
                        targetWidth, 
                        targetHeight, 
                        true
                    );
                    originalBitmap.recycle(); // Liberar memoria

                    // Rotar si es necesario
                    if (rotation != 0) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(rotation);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(
                            scaledBitmap, 0, 0,
                            scaledBitmap.getWidth(), scaledBitmap.getHeight(),
                            matrix, true
                        );
                        scaledBitmap.recycle();
                        scaledBitmap = rotatedBitmap;
                    }

                    // Comprimir con calidad progresiva
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int quality = 100;
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);

                    // Reducir calidad gradualmente si es necesario
                    while (bos.size() > 200 * 1024 && quality > 60) { // Mantener calidad mínima de 60%
                        bos.reset();
                        quality -= 5;
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                    }

                    // Si aún es muy grande, intentar reducir más la calidad pero no tanto
                    if (bos.size() > 200 * 1024) {
                        while (bos.size() > 200 * 1024 && quality > 40) {
                            bos.reset();
                            quality -= 2; // Reducción más gradual
                            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                        }
                    }

                    float finalSizeKB = bos.size() / 1024f;
                    Log.d("PhotoCompression", String.format(
                        "Tamaño final: %.2f KB, Calidad: %d%%, Dimensiones: %dx%d, Rotación: %d°", 
                        finalSizeKB, quality, scaledBitmap.getWidth(), scaledBitmap.getHeight(), rotation
                    ));

                    File optimizedFile = new File(requireContext().getCacheDir(), 
                        "optimized_" + imageFile.getName());
                    FileOutputStream fos = new FileOutputStream(optimizedFile);
                    fos.write(bos.toByteArray());
                    fos.close();

                    scaledBitmap.recycle(); // Liberar memoria

                    PhotoAdapter.PhotoItem newPhoto = new PhotoAdapter.PhotoItem(
                        optimizedFile.getAbsolutePath(), 
                        false
                    );
                    photos.add(newPhoto);
                    photoAdapter.updatePhotos(photos);
                } catch (Exception e) {
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
        String token = Login.getAuthorizationHeader(requireContext());

        RetrofitClient.getApiService()
            .getClientImages(id, token)
            .enqueue(new Callback<ServerPhotosResponse>() {
                @Override
                public void onResponse(Call<ServerPhotosResponse> call, Response<ServerPhotosResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        serverPhotos.clear();
                        serverPhotos.addAll(response.body().getPhotos());
                        serverPhotoAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<ServerPhotosResponse> call, Throwable t) {
                    Toast.makeText(requireContext(), 
                        "Error al cargar las fotos del servidor", 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

  
}
