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

public class ClientDetailsFragment extends Fragment {

    private static final int REQUEST_CODE_PERMISSIONS = 100;

    private String clientName;
    private String clientPhone;
    private String clientLatitude;
    private String clientLongitude;
    private RecyclerView rvPhotos;
    private PhotoAdapter photoAdapter;
    private List<String> photoPaths = new ArrayList<>();

    private static final int REQUEST_CODE_CAMERA = 100;
    private static final int REQUEST_CODE_GALLERY = 101;
    private static final int REQUEST_CODE_GALLERY_MULTIPLE = 102;
    private Uri photoUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!hasPermissions()) {
            requestPermissions();
        }
        if (getArguments() != null) {
            clientName = getArguments().getString("firstName") + " " + getArguments().getString("lastName");
            clientPhone = getArguments().getString("phoneNumber");

            // Recuperar las coordenadas como String
            clientLatitude = getArguments().getString("latitude", "0.0");
            clientLongitude = getArguments().getString("longitude", "0.0");
            Toast.makeText(getContext(), "" + clientLatitude, Toast.LENGTH_SHORT).show();
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
        TextView tvPhone = view.findViewById(R.id.tvClientPhone);
        TextView tvCoordinates = view.findViewById(R.id.tvCoordinates);
        Button btnAddPhoto = view.findViewById(R.id.btnAddPhoto);
        rvPhotos = view.findViewById(R.id.rvPhotos);

        tvName.setText(clientName);
        tvPhone.setText(clientPhone);

        if (clientLatitude != null && clientLongitude != null) {
            tvCoordinates.setText("Latitud: " + clientLatitude + ", Longitud: " + clientLongitude);
        } else {
            tvCoordinates.setText("Sin coordenadas");
        }

        // Configurar el adaptador para el RecyclerView
        photoAdapter = new PhotoAdapter(photoPaths, position -> {
            photoPaths.remove(position);
            photoAdapter.notifyItemRemoved(position);
        });
        rvPhotos.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvPhotos.setAdapter(photoAdapter);

        // Evento para el botón que agrega fotos
        btnAddPhoto.setOnClickListener(v -> showPhotoDialog());

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
                        photoPaths.add(photoUri.toString()); // Agregar la foto a la lista
                        photoAdapter.notifyItemInserted(photoPaths.size() - 1);
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
                                photoPaths.add(imageUri.toString()); // Agregar cada foto a la lista
                            }
                        }
                        photoAdapter.notifyDataSetChanged();
                    } else if (result.getData().getData() != null) {
                        // Si solo se seleccionó una foto
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            photoPaths.add(imageUri.toString());
                            photoAdapter.notifyItemInserted(photoPaths.size() - 1);
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

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permiso concedido
                    Toast.makeText(getContext(), "Permiso concedido", Toast.LENGTH_SHORT).show();
                } else {
                    // Permiso denegado
                    Toast.makeText(getContext(), "Permiso necesario no concedido", Toast.LENGTH_SHORT).show();
                }
            });

    private void showFullscreenImage(Uri imageUri) {
        FullscreenImageDialog dialog = new FullscreenImageDialog(requireContext(), imageUri);
        dialog.show();
    }
}
