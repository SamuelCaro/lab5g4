package pe.pucp.tel306.firebox.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import pe.pucp.tel306.firebox.Activities.PrincipalActivity;
import pe.pucp.tel306.firebox.R;

public class ListaArchivosAdapter extends RecyclerView.Adapter<ListaArchivosAdapter.ViewHolder> {
    ArrayList<StorageReference> references;
    Context context;
    int umbral;

    //Se inicializan los atributos
    public ListaArchivosAdapter(ArrayList<StorageReference> references, Context context, int umbral)
    {
        this.context = context;
        this.references= references;
        this.umbral = umbral;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.documento,parent,false);
        ListaArchivosAdapter.ViewHolder viewHolder = new ListaArchivosAdapter.ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final StorageReference reference = references.get(position);

        holder.context = context;
        holder.reference = reference;
        if (position<umbral)
        {
            holder.tipo = "Carpeta";
            holder.tipoDocumento.setImageResource(R.drawable.folder); //Si es carpeta se pone la imagen de carpeta
            holder.nombreDocumento.setText(reference.getName());

            //Si es carpeta, al hacer click te debe ingresar a esta
            holder.nombreDocumento.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PrincipalActivity principalActivity = (PrincipalActivity) context;
                    principalActivity.setReferenciaCarpeta(reference);
                    principalActivity.listarDocumentos();
                }
            });
        }
        else
        {
            holder.tipo= "Documento";
            holder.tipoDocumento.setImageResource(R.drawable.file); //Si es documento se pone la imagen de un documento
            holder.nombreDocumento.setOnCreateContextMenuListener(holder);

            //De la metadata se obtiene el nombre a mostrar
            reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    holder.nombreDocumento.setText(storageMetadata.getCustomMetadata("displayName"));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return references.size();
    }

    //implementa el context menu porque los documentos tienen acciones
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener
    {
        TextView nombreDocumento;
        ImageView tipoDocumento;
        String tipo;
        Context context;
        StorageReference reference;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreDocumento = itemView.findViewById(R.id.nombreDocumento);
            tipoDocumento= itemView.findViewById(R.id.tipoImagenDocumento);
        }

        //Esto representa el menu y cada una de las acciones
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem renombrar = menu.add(Menu.NONE,1 , 1, "Renombrar");
            MenuItem descargar = menu.add(Menu.NONE, 2, 2, "Descargar");
            MenuItem eliminar = menu.add(Menu.NONE, 3, 3, "Eliminar");
            renombrar.setOnMenuItemClickListener(onEditMenu);
            descargar.setOnMenuItemClickListener(onEditMenu);
            eliminar.setOnMenuItemClickListener(onEditMenu);
        }

        //De acuerdo a lo seleccionado se realiza la accion correspondiente
        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case 1:
                        //Para renombrar
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                        builder2.setTitle("Ingrese el nombre nuevo del archivo");
                        final EditText nuevoNombreTv = new EditText(context);
                        builder2.setView(nuevoNombreTv);
                        builder2.setPositiveButton("confirmar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (nuevoNombreTv.getText().toString().equals(""))
                                {
                                    nuevoNombreTv.setError("Se debe indicar un nombre");
                                    Toast.makeText(context,"Se debe indicar un nombre",Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    //Si selecciona el nombre se actualiza la metadata
                                    String nuevoNombre = nuevoNombreTv.getText().toString();
                                    StorageMetadata metadata = new StorageMetadata.Builder()
                                            .setCustomMetadata("displayName", nuevoNombre)
                                            .build();
                                    reference.updateMetadata(metadata).addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                        @Override
                                        public void onSuccess(StorageMetadata storageMetadata) {
                                            PrincipalActivity principalActivity = (PrincipalActivity) context;
                                            principalActivity.listarDocumentos();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                            });
                        builder2.show();
                        return true;


                    case 2:
                        //Descarga del archivo
                        if (tipo.equalsIgnoreCase("carpeta")) Toast.makeText(context,"Comming soon", Toast.LENGTH_SHORT).show();
                        else
                        {
                            //
                            reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                @Override
                                public void onSuccess(StorageMetadata storageMetadata) {
                                    // Metadata now contains the metadata for 'images/forest.jpg'
                                    final String pswd = storageMetadata.getCustomMetadata("password");
                                    Log.d("infoAPP", "el psswd es "+ pswd)    ;
                                    if(pswd!=null)
                                    {

                                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setTitle("Ingrese la contraseña");
                                        builder.setMessage("Deberá ingresar su contraseña");

                                        final EditText passwd = new EditText(context.getApplicationContext());
                                        builder.setView(passwd);
                                        builder.setPositiveButton("confirmar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (passwd.getText().toString().equals(""))
                                                {
                                                    passwd.setError("Tienes que inidcar una contraseña");
                                                    Toast.makeText(context,"Tienes que inidcar una contraseña",Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                {
                                                    if(passwd.getText().toString().equals(pswd)){
                                                        //Se piden los permisos
                                                        PrincipalActivity principalActivity = (PrincipalActivity) context;
                                                        principalActivity.validadPermisosDeEscritura();
                                                        if(principalActivity.validadPermisosDeEscritura())
                                                        {
                                                            //Se descarga en downloads
                                                            File directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                                            File localFile = new File(directorio, nombreDocumento.getText().toString());
                                                            reference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                    Toast.makeText(context, "Archivo descargado",Toast.LENGTH_SHORT).show();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    }

                                                }
                                            }
                                        });
                                        builder.show();
                                    }else{
                                        //Se piden los permisos
                                        PrincipalActivity principalActivity = (PrincipalActivity) context;
                                        principalActivity.validadPermisosDeEscritura();
                                        if(principalActivity.validadPermisosDeEscritura())
                                        {
                                            //Se descarga en downloads
                                            File directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                            File localFile = new File(directorio, nombreDocumento.getText().toString());
                                            reference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    Toast.makeText(context, "Archivo descargado",Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                }
                            });
                            /*
                            //Se piden los permisos
                            PrincipalActivity principalActivity = (PrincipalActivity) context;
                            principalActivity.validadPermisosDeEscritura();
                            if(principalActivity.validadPermisosDeEscritura())
                            {
                                //Se descarga en downloads
                                File directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                File localFile = new File(directorio, nombreDocumento.getText().toString());
                                reference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        Toast.makeText(context, "Archivo descargado",Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            */
                        }
                        return true;

                    case 3:
                        //Aqui se elimina el archivo seleccionado
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Eliminar archivo");
                        builder.setMessage("¿Esta seguro que desea eliminar el archivo "+ nombreDocumento.getText().toString() + "?" );
                        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() { //Se pide confirmar
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Archivo eliminado exitosamente",Toast.LENGTH_SHORT).show();
                                        PrincipalActivity principalActivity = (PrincipalActivity) context;
                                        principalActivity.listarDocumentos();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                        builder.show();
                        return true;

                        //Esto no debería ocurrir
                    default:
                        Toast.makeText(context, "Ocurrio un error",Toast.LENGTH_SHORT).show();
                        return false;
                }
            }
        };
    }
}
