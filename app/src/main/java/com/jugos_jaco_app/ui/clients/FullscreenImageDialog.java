package com.jugos_jaco_app.ui.clients;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.jugos_jaco_app.R;

public class FullscreenImageDialog extends Dialog {

    public FullscreenImageDialog(Context context, Uri imageUri) {
        super(context);

        // Quitar el título y configurar pantalla completa
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_fullscreen_image);

        // Configurar ventana a pantalla completa
        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            // Agregar banderas para pantalla completa
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        // Inicializar vistas
        PhotoView photoView = findViewById(R.id.photoView);
        ImageButton btnClose = findViewById(R.id.btnClose);

        // Cargar la imagen usando Glide
        Glide.with(context)
            .load(imageUri)
            .error(R.drawable.ic_error)
            .into(photoView);

        // Configurar zoom
        photoView.setMaximumScale(5.0f);
        photoView.setMediumScale(2.5f);

        // Configurar botón de cerrar
        btnClose.setOnClickListener(v -> dismiss());
    }
}