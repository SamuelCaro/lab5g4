package pe.pucp.tel306.firebox.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import pe.pucp.tel306.firebox.Adapters.ListaArchivosAdapter;
import pe.pucp.tel306.firebox.R;

public class PrincipalActivity extends AppCompatActivity {

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    StorageReference storageReference= FirebaseStorage.getInstance().getReference().child(currentUser.getUid());
    String TAG = "tageado";
    StorageReference referenciaCarpeta=storageReference;
    String refIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        listarDocumentos();
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(),fab);
                popupMenu.getMenuInflater().inflate(R.menu.anadir_archivos,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())
                        {
                            case R.id.añadirArchivo:
                            {
                                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("*/*");
                                startActivityForResult(intent, 2);
                                return true;
                            }
                            case R.id.añadirCarpeta:
                            {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(PrincipalActivity.this);
                                builder.setTitle("Ingrese el nombre de la carpeta");
                                builder.setMessage("Deberá ingresar almenos un archivo a la carpeta");
                                final EditText nombreTv = new EditText(getApplicationContext());
                                builder.setView(nombreTv);
                                builder.setPositiveButton("confirmar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (nombreTv.getText().toString().equals(""))
                                        {
                                            nombreTv.setError("Se debe indicar el nombre de la carpeta");
                                        }
                                        else
                                        {
                                            refIntent = nombreTv.getText().toString();
                                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                                            intent.setType("*/*");
                                            startActivityForResult(intent, 3);
                                        }
                                    }
                                });
                                builder.show();

                                return true;
                            }
                            default:return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode==2)
        {
            Uri oriol = data.getData();
            UploadTask uploadTask = referenciaCarpeta.child(oriol.getLastPathSegment()).putFile(oriol);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(PrincipalActivity.this,"Se subio el documento exitosamente",Toast.LENGTH_SHORT).show();
                    listarDocumentos();
                }
            });

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PrincipalActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        if (requestCode==3)
        {
            Uri oriol = data.getData();
            UploadTask uploadTask = referenciaCarpeta.child(refIntent).child(oriol.getLastPathSegment()).putFile(oriol);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(PrincipalActivity.this,"Se subio el documento exitosamente",Toast.LENGTH_SHORT).show();
                    listarDocumentos();
                }
            });

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PrincipalActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void listarDocumentos()
    {
        storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                ArrayList<StorageReference> references = new ArrayList<>();
                int umbral = 0;
                for (StorageReference item : listResult.getPrefixes())
                {
                    references.add(item);
                    umbral++;
                }
                for (StorageReference item : listResult.getItems()) references.add(item);
                Log.d(TAG, "onSuccess:" + String.valueOf(references.size()));

                //Esto es para mostrar que no hay nada
                TextView hayElementos = findViewById(R.id.ceroDocumentos);
                if (references.size()==0) hayElementos.setVisibility(View.VISIBLE);
                else hayElementos.setVisibility(View.GONE);

                ListaArchivosAdapter listaArchivosAdapter = new ListaArchivosAdapter(references, PrincipalActivity.this, umbral);
                RecyclerView rv = findViewById(R.id.rv);
                rv.setAdapter(listaArchivosAdapter);
                rv.setLayoutManager(new LinearLayoutManager(PrincipalActivity.this));
            }
        });
    }

    public void cerrarSesion(View view)
    {
        AuthUI.getInstance().signOut(this).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}