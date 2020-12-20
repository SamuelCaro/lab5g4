package pe.pucp.tel306.firebox.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

import pe.pucp.tel306.firebox.Adapters.ListaArchivosAdapter;
import pe.pucp.tel306.firebox.R;

public class PrincipalActivity extends AppCompatActivity {

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    StorageReference storageReference= FirebaseStorage.getInstance().getReference().child(currentUser.getUid());
    String TAG = "tageado";
    StorageReference referenciaCarpeta; //Esto se usa cuando se entra en las carpetas tener un registro
    String refIntent; //Esto se usa para guardar las carpetas
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        referenciaCarpeta=storageReference; //inicializacion

        //Las opciones del fab
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Se crea un popupMenu que muestra las opciones de añadir archivo y carpeta
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(),fab);
                popupMenu.getMenuInflater().inflate(R.menu.anadir_archivos,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())
                        {
                            //En caso se añada un archivo solo se selecciona
                            case R.id.añadirArchivo:
                            {
                                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("*/*");
                                startActivityForResult(intent, 2);
                                return true;
                            }

                            //En caso se añada una carpeta se debe indicar el nombre de esta y almenos un archivo dentro de esta
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
                                            Toast.makeText(PrincipalActivity.this,"Se debe indicar el nombre de la carpeta",Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                        {
                                            refIntent = nombreTv.getText().toString();
                                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); //Abrir documento
                                            intent.addCategory(Intent.CATEGORY_OPENABLE); //Indica que se puede abrir
                                            intent.setType("*/*"); //Indica que se puede seleccionar cualquier tipo de documento
                                            startActivityForResult(intent, 3);
                                        }
                                    }
                                });
                                builder.show();
                                return true;
                            }
                            case R.id.añadirArchivoPrivado:
                            {
                                //Obtengo current user ID, query a Firestorage
                                //Calculamos la cantidad de documentos en privatefiles
                                //if(cuetafree && cantfiles<6)
                                //{
                                    //añadir archivo
                                //}
                                db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                                final Boolean tipo = document.getString("tipo").equals("free");

                                                //Calculamos la cantidad de documentos en privatefiles
                                                int cant = 0;
                                                db.collection("users").document(currentUser.getUid()).collection("privatefiles")
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    int cant = 0;
                                                                    for (DocumentSnapshot document : task.getResult()) {
                                                                        cant++;
                                                                    }
                                                                    Log.d(TAG, "Cantidad de doc dentro de privatefiles: " + cant);
                                                                    if(tipo&&(cant<5)){
                                                                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                                                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                                                                        intent.setType("*/*");
                                                                        startActivityForResult(intent, 4);
                                                                    }

                                                                } else {
                                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                                }
                                                            }
                                                        });
                                                //

                                            } else {
                                                Log.d(TAG, "No such document");
                                            }
                                        } else {
                                            Log.d(TAG, "get failed with ", task.getException());
                                        }
                                    }
                                });





                                //Todo marce tu mismo eres
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

    //Cuando se inicia recien se listan los documentos
    @Override
    protected void onStart() {
        super.onStart();
        listarDocumentos();
    }

    //Para gestionar cuando se añada un elemento
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //En caso sea un archivo
        if (requestCode==2)
        {
            Uri oriol = data.getData();
            //Se usa la metadata para almacenar el nombre que se mostrará del archivo
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setCustomMetadata("displayName", getFileName(oriol))
                    .build();
            UploadTask uploadTask = referenciaCarpeta.child(oriol.getLastPathSegment()).putFile(oriol,metadata);

            //Se notifica si se subio el archivo correctamente y se lista
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

        //En caso sea una carpeta
        if (requestCode==3)
        {
            Uri oriol = data.getData();
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setCustomMetadata("displayName", getFileName(oriol))
                    .build();
            //Igual que archivo, solo que en este caso se pone el nombre creado anteriormente como child
            UploadTask uploadTask = referenciaCarpeta.child(refIntent).child(oriol.getLastPathSegment()).putFile(oriol,metadata);
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
        if (requestCode==4)
        {

            // Create a new user with a first and last name
            HashMap<String, Object> priv = new HashMap<>();
            priv.put("ruta", "Marcelo");

            // Add a new document in privatefiles
            db.collection("users").document(currentUser.getUid()).collection("privatefiles")
                    .add(priv)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Esta funcion es para listar los documentos
    public void listarDocumentos()
    {
        //Se hace a partir de la referencia de carpeta pues esta cambia entre las carpetas
        referenciaCarpeta.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                //Se crea un array list que hace referencia a todos los elementos en la carpeta
                ArrayList<StorageReference> references = new ArrayList<>();
                //Se define un umbral. Por debajo del umbral el elemento es una carpeta, por encima es un archivo.
                int umbral = 0;

                //Se obtienen las carpetas
                for (StorageReference item : listResult.getPrefixes())
                {
                    references.add(item);
                    umbral++;
                }

                //Se obtienen los archivos
                references.addAll(listResult.getItems());
                Log.d(TAG, "onSuccess:" + references.size());

                //Esto es para mostrar cuando no hay nada
                TextView hayElementos = findViewById(R.id.ceroDocumentos);
                if (references.size()==0) hayElementos.setVisibility(View.VISIBLE);
                else hayElementos.setVisibility(View.GONE);

                //Se pone en el recycler view
                ListaArchivosAdapter listaArchivosAdapter = new ListaArchivosAdapter(references, PrincipalActivity.this, umbral);
                RecyclerView rv = findViewById(R.id.rv);
                rv.setAdapter(listaArchivosAdapter);
                rv.setLayoutManager(new LinearLayoutManager(PrincipalActivity.this));
            }
        });
    }


    //Esto es para cerrar la sesion
    public void cerrarSesion()
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

    //Para validar los permisos de escritura cuando se descarga
    public boolean validadPermisosDeEscritura()
    {
        int permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permiso != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        else
        {
            return true;
        }

        return false;
    }

    //Para pedir los permisos de escritura
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Log.d("infoApp", "Permisos concedidos");

            if(requestCode == 1) { //DM
                validadPermisosDeEscritura();
            }
        } else {
            Log.d("infoApp", "no se brindaron los permisos");
        }

    }

    //Esto se usa para obtener el nombre de un archivo y su extension
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    //Se modifica el back de forma que si se encuentra dentro de una carpeta, te regrese al padre (hasta la referencia inicial)
    @Override
    public void onBackPressed() {
        if (storageReference==referenciaCarpeta) super.onBackPressed();
        else
        {
            referenciaCarpeta = referenciaCarpeta.getParent();
            listarDocumentos();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar,menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.logout: cerrarSesion();
        }
        return super.onOptionsItemSelected(item);
    }


    //Se crea el set de la referencia que se usa en el adapter
    public void setReferenciaCarpeta(StorageReference referenciaCarpeta) {
        this.referenciaCarpeta = referenciaCarpeta;
    }
}