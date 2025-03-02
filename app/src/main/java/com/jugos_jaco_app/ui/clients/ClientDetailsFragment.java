package com.jugos_jaco_app.ui.clients;

import android.content.Intent;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.button.MaterialButton;

import android.content.Context;
import android.location.LocationManager;
import android.provider.Settings;
import android.app.AlertDialog;

public class ClientDetailsFragment extends Fragment implements PhotoAdapter.OnPhotoListener {

    private static final int REQUEST_CODE_PERMISSIONS = 100;

    private String clientName;
    private String clientPhone;
    private String clientLatitude;
    private String clientLongitude;
    private RecyclerView rvPhotos;
    private PhotoAdapter photoAdapter;
    private List<String> photos = new ArrayList<>();

    private static final int REQUEST_CODE_CAMERA = 100;
    private static final int REQUEST_CODE_GALLERY = 101;
    private static final int REQUEST_CODE_GALLERY_MULTIPLE = 102;
    private Uri photoUri;
    private String adress;
    private String department;
    private String twonship;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!hasPermissions()) {
            requestPermissions();
        }
        if (getArguments() != null) {
            clientName = getArguments().getString("firstName") + " " + getArguments().getString("lastName");
            clientPhone = getArguments().getString("phoneNumber");
            adress = getArguments().getString("adress");
            department = getArguments().getString("department");
            twonship = getArguments().getString("twonship");
 
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
        Button btnAddPhoto = view.findViewById(R.id.btnAddPhoto);
        rvPhotos = view.findViewById(R.id.rvPhotos);

        tvName.setText(clientName);
        tvadress.setText(adress);

        tvPhone.setText(clientPhone);

        if (clientLatitude != null && clientLongitude != null) {
            tvCoordinates.setText("Latitud: " + clientLatitude + ", Longitud: " + clientLongitude);
        } else {
            tvCoordinates.setText("Sin coordenadas");
        }

        // Configurar el adaptador para el RecyclerView
        photoAdapter = new PhotoAdapter(photos, requireContext(), this);
        rvPhotos.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvPhotos.setAdapter(photoAdapter);

        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // Evento para el botón que agrega fotos
        btnAddPhoto.setOnClickListener(v -> showPhotoDialog());

        // Configurar el botón de llamada
        MaterialButton btnCall = view.findViewById(R.id.btnCall);
        btnCall.setOnClickListener(v -> makePhoneCall());

        // Configurar el botón de ver en mapa
        MaterialButton btnViewMap = view.findViewById(R.id.btnViewMap);
        btnViewMap.setOnClickListener(v -> checkLocationAndOpenMap());

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


    // Manejar los resultados de la cámara o la galería
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
                        photos.add(photoUri.toString()); // Agregar la foto a la lista
                        photoAdapter.updatePhotos(photos);
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
                    // Fotos seleccionadas de la galería
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                            if (imageUri != null) {
                                photos.add(imageUri.toString()); // Agregar cada foto a la lista
                            }
                        }
                        photoAdapter.updatePhotos(photos);
                    } else if (result.getData().getData() != null) {
                        // Si solo se seleccionó una foto
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            photos.add(imageUri.toString());
                            photoAdapter.updatePhotos(photos);
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
            String photoPath = photos.get(position);
            // Eliminar el archivo
            File photoFile = new File(photoPath);
            if (photoFile.exists()) {
                photoFile.delete();
            }
            // Eliminar de la lista y actualizar el adaptador
            photos.remove(position);
            photoAdapter.updatePhotos(photos);

            // Opcional: Mostrar un Toast breve para confirmar la eliminación
            Toast.makeText(requireContext(), "Foto eliminada", Toast.LENGTH_SHORT).show();
        }
    }
}
