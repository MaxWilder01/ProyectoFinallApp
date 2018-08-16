package com.ufro.appfinanzas.appfianzas;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class AgregarIngreso extends AppCompatDialogFragment implements View.OnClickListener {

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseTotales;
    private EditText txtCantidadIngreso;
    private EditText txtComentarioIngreso;
    private int cantidad;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("usuarios").child((mAuth.getCurrentUser()).getUid()).child("transacciones");
        mDatabaseTotales = FirebaseDatabase.getInstance().getReference("usuarios").child((mAuth.getCurrentUser()).getUid()).child("totales");
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.agregar_ingreso_layout, null);

        txtCantidadIngreso = view.findViewById(R.id.txtCantidadIngreso);
        txtComentarioIngreso = view.findViewById(R.id.txtComentarioIngreso);
        Button btnAgregarIngreso = view.findViewById(R.id.btnAgregarIngreso);

        btnAgregarIngreso.setOnClickListener(this);

        builder.setView(view)
                .setTitle("Agregar Ingreso");

        return builder.create();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();

        switch (i) {
            case R.id.btnAgregarIngreso: {
                agregarIngreso();
            }
        }
    }

    private void agregarIngreso() {
        cantidad = Integer.parseInt(txtCantidadIngreso.getText().toString());
        String comentario = txtComentarioIngreso.getText().toString();

        if (!Integer.toString(cantidad).equals("") && !comentario.equals("")) {

            String id = mDatabase.push().getKey();

            Transaccion ingreso = new Transaccion(id, cantidad, comentario, "ingreso", getFecha());

            mDatabaseTotales.child("total_ingresos").addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int valorActual = snapshot.getValue(Integer.class);
                        mDatabaseTotales.child("total_ingresos").setValue(valorActual + cantidad);

                    } else {
                        mDatabaseTotales.child("total_ingresos").setValue(cantidad);
                        mDatabaseTotales.child("total_gastos").setValue(0);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mDatabase.child(id).setValue(ingreso);

            this.dismiss();

        } else {
            Toast.makeText(getActivity(), "Todos los campos son obligatorios", Toast.LENGTH_LONG).show();
        }
    }

    public String getFecha() {
        Calendar now = Calendar.getInstance();
        String year = Integer.toString(now.get(Calendar.YEAR));
        String month = Integer.toString(now.get(Calendar.MONTH));

        return year + month;
    }
}
