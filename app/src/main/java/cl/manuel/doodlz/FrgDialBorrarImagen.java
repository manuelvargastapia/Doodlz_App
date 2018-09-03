package cl.manuel.doodlz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

/**
 * Permite al usuario eliminar una imagen
 */

public class FrgDialBorrarImagen extends DialogFragment{

    /*Esta clase es casi idéntica a FrgDialColor y FrgDialGrosorLinea, por lo que
    casi no tiene comentarios*/

    /*CREACIÓN Y DESPLIEGUE DE ALERTA*/
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        //Creación de alerta
        AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
        alerta.setMessage(R.string.mensaje_borrar);
        alerta.setPositiveButton(R.string.btn_borrar,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDoodleFragment().getDoodleView().limpiar();
                    }
        });

        // Agregar botón de cancelar (recurso por defecto)
        alerta.setNegativeButton(android.R.string.cancel, null);

        return alerta.create(); // return dialog
    }


    //Obtener referencia a FrgMAinActivity
    private FrgMainActivity getDoodleFragment() {
        return (FrgMainActivity) getFragmentManager().findFragmentById(R.id.doodleFragment);
    }

    //Indicar que diálogo está desplegado
    @Override
    public void onAttach(Context context) {
            super.onAttach(context);
            FrgMainActivity fragment = getDoodleFragment();
            if (fragment != null){
                fragment.setDialogoEnPantalla(true);
            }
    }

    //Indicar que diálogo no está desplegado
    @Override
    public void onDetach() {
            super.onDetach();
            FrgMainActivity fragment = getDoodleFragment();
            if (fragment != null){
                fragment.setDialogoEnPantalla(false);
            }
    }
}
