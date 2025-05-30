package com.jugos_jaco_app.ui.clients;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.ArrayList;
import java.util.List;

public class VisitDaysDialog extends DialogFragment {
    public interface VisitDaysDialogListener {
        void onVisitDaysSelected(List<String> selectedDays);
    }

    private static final String[] DAYS = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
    private boolean[] checkedDays;
    private ArrayList<String> preselectedDays;
    private VisitDaysDialogListener listener;

    public VisitDaysDialog(ArrayList<String> preselectedDays, VisitDaysDialogListener listener) {
        this.preselectedDays = preselectedDays;
        this.listener = listener;
        checkedDays = new boolean[DAYS.length];
        for (int i = 0; i < DAYS.length; i++) {
            checkedDays[i] = preselectedDays.contains(DAYS[i]);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Selecciona los días de visita")
                .setMultiChoiceItems(DAYS, checkedDays, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedDays[which] = isChecked;
                    }
                })
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<String> selected = new ArrayList<>();
                        for (int i = 0; i < DAYS.length; i++) {
                            if (checkedDays[i]) selected.add(DAYS[i]);
                        }
                        if (listener != null) listener.onVisitDaysSelected(selected);
                    }
                })
                .setNegativeButton("Cancelar", null);
        return builder.create();
    }


}
