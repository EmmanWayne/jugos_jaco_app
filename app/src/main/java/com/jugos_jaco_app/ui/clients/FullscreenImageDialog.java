package com.jugos_jaco_app.ui.clients;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.jugos_jaco_app.R;

public class FullscreenImageDialog extends Dialog {
    private Uri imageUri;
    private Context context;

    public FullscreenImageDialog(Context context, Uri imageUri) {
        super(context);
        this.context = context;
        this.imageUri = imageUri;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_fullscreen_image);

        // Configurar ventana a pantalla completa
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        PhotoView photoView = findViewById(R.id.ivFullscreen);
        ImageButton btnClose = findViewById(R.id.btnClose);

        // Cargar imagen con Glide
        Glide.with(context)
                .load(imageUri)
                .into(photoView);

        // Configurar botón de cerrar
        btnClose.setOnClickListener(v -> dismiss());

        // Permitir zoom y gestos en la imagen
        photoView.setMaximumScale(5.0f);
        photoView.setMediumScale(2.5f);
    }
}