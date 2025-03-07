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
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Manejar el error
                ex.printStackTrace();
            }
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(getActivity(),
                        getActivity().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(takePictureIntent);
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
                    if (photoUri != null) {
                        // Solo agregar la foto al adapter sin subirla
                        addPhotoToAdapter(photoUri.toString());
                    } else {
                        Toast.makeText(getContext(), "No se pudo obtener la foto", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    private File createImageFile() throws IOException {
        // Crear un nombre único para la imagen
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefijo */
                ".jpg",         /* sufijo */
                storageDir      /* directorio */
        );
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
            Toast.makeText(requireContext(), "Foto eliminada", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Maneja la subida de una foto al servidor
     * @param imagePath Ruta de la imagen a subir
     */
    private void uploadPhoto(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                handleUploadError();
                return;
            }

            // Convertir URI de contenido a ruta real si es necesario
            if (imagePath.startsWith("content://")) {
                String realPath = getRealPathFromURI(Uri.parse(imagePath));
                if (realPath != null) {
                    imageFile = new File(realPath);
                }
            }

            // Comprimir la imagen
            Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            if (originalBitmap == null) {
                Toast.makeText(requireContext(), "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
                handleUploadError();
                return;
            }

            // Comprimir imagen
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
            
            // Guardar imagen comprimida
            File compressedFile = new File(requireContext().getCacheDir(), "compressed_" + imageFile.getName());
            FileOutputStream fos = new FileOutputStream(compressedFile);
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();

            // Crear MultipartBody.Part
            RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/*"),
                compressedFile
            );

            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                "image",
                compressedFile.getName(),
                requestFile
            );

            // Obtener token
            String token = Login.getAuthorizationHeader(requireContext());

            // Hacer la petición
            RetrofitClient.getApiService()
                .uploadBusinessImage(id, imagePart, token)
                .enqueue(new Callback<PhotoResponse>() {
                    @Override
                    public void onResponse(Call<PhotoResponse> call, Response<PhotoResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), 
                                    "Foto subida exitosamente",
                                    Toast.LENGTH_SHORT).show();
                                int position = photos.size() - 1;
                                String photoUrl = response.body().getUrl();
                                photoAdapter.setPhotoUploaded(position, photoUrl);
                            });
                        } else {
                            // Manejar errores de la API
                            try {
                                String errorBody = response.errorBody().string();
                                JSONObject errorJson = new JSONObject(errorBody);
                                String errorMessage = errorJson.getString("message");
                                
                                // Si hay errores específicos, mostrarlos
                                if (errorJson.has("errors")) {
                                    JSONObject errors = errorJson.getJSONObject("errors");
                                    if (errors.has("image")) {
                                        JSONArray imageErrors = errors.getJSONArray("image");
                                        if (imageErrors.length() > 0) {
                                            errorMessage = imageErrors.getString(0);
                                        }
                                    }
                                }
                                
                                final String finalErrorMessage = errorMessage;
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), 
                                        finalErrorMessage, 
                                        Toast.LENGTH_LONG).show();
                                });
                            } catch (Exception e) {
                                handleUploadError();
                            }
                            handleUploadError();
                        }
                    }

                    @Override
                    public void onFailure(Call<PhotoResponse> call, Throwable t) {
                        handleUploadError();
                    }
                });

        } catch (Exception e) {
            e.printStackTrace();
            handleUploadError();
        }
    }

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
                Toast.makeText(requireContext(), 
                    "Se subieron " + uploadedCount[0] + " de " + totalPhotos + " fotos", 
                    Toast.LENGTH_LONG).show();
            });
            return;
        }

        PhotoAdapter.PhotoItem photo = pendingPhotos.get(currentIndex);
        
        // Mostrar progreso de subida
        requireActivity().runOnUiThread(() -> {
            photoAdapter.setPhotoUploading(currentIndex, true);
            Toast.makeText(requireContext(), 
                "Subiendo foto " + (currentIndex + 1) + " de " + totalPhotos, 
                Toast.LENGTH_SHORT).show();
        });

        File imageFile = new File(photo.getPath());

        if (!imageFile.exists()) {
            uploadNextPhoto(pendingPhotos, currentIndex + 1, uploadedCount, totalPhotos);
            return;
        }

        // Comprimir imagen
        try {
            Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            if (originalBitmap == null) {
                uploadNextPhoto(pendingPhotos, currentIndex + 1, uploadedCount, totalPhotos);
                return;
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
            
            File compressedFile = new File(requireContext().getCacheDir(), 
                "compressed_" + imageFile.getName());
            FileOutputStream fos = new FileOutputStream(compressedFile);
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();

            // Crear MultipartBody.Part
            RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/jpeg"),
                compressedFile
            );

            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                "image",
                "photo.jpg",
                requestFile
            );

            // Obtener token
            String token = Login.getAuthorizationHeader(requireContext());

            // Hacer la petición
            RetrofitClient.getApiService()
                .uploadBusinessImage(id, imagePart, token)
                .enqueue(new Callback<PhotoResponse>() {
                    @Override
                    public void onResponse(Call<PhotoResponse> call, Response<PhotoResponse> response) {
                        requireActivity().runOnUiThread(() -> {
                            photoAdapter.setPhotoUploading(currentIndex, false);
                        });
                        
                        if (response.isSuccessful() && response.body() != null) {
                            uploadedCount[0]++;
                            requireActivity().runOnUiThread(() -> {
                                photoAdapter.setPhotoUploaded(currentIndex, response.body().getUrl());
                            });
                        } else {
                            handleUploadError(currentIndex);
                        }
                        uploadNextPhoto(pendingPhotos, currentIndex + 1, uploadedCount, totalPhotos);
                    }

                    @Override
                    public void onFailure(Call<PhotoResponse> call, Throwable t) {
                        requireActivity().runOnUiThread(() -> {
                            photoAdapter.setPhotoUploading(currentIndex, false);
                        });
                        handleUploadError(currentIndex);
                        uploadNextPhoto(pendingPhotos, currentIndex + 1, uploadedCount, totalPhotos);
                    }
                });

        } catch (Exception e) {
            handleUploadError(currentIndex);
            uploadNextPhoto(pendingPhotos, currentIndex + 1, uploadedCount, totalPhotos);
        }
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
            PhotoAdapter.PhotoItem newPhoto = new PhotoAdapter.PhotoItem(photoPath, false);
            photos.add(newPhoto);
            photoAdapter.updatePhotos(photos);
        }
    }
}
