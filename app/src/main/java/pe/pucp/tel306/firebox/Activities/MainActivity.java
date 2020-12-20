package pe.pucp.tel306.firebox.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import pe.pucp.tel306.firebox.Fragments.LoginFragment;
import pe.pucp.tel306.firebox.R;

public class MainActivity extends AppCompatActivity {

    StorageReference storage = FirebaseStorage.getInstance().getReference();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "infoAPP";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LoginFragment loginFragment = LoginFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.loggingContainer, loginFragment);
        fragmentTransaction.commit();
    }




    public void login(View view)
    {
        List<AuthUI.IdpConfig> proveedores = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
        AuthUI instance = AuthUI.getInstance();
        Intent intent = instance.createSignInIntentBuilder().setLogo(R.mipmap.ic_launcher).setAvailableProviders(proveedores).build();
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            validarUsuario();
        }
    }

    public void validarUsuario() {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                // StorageReference reference= storage.child(currentUser.getUid());  //Aquí se crea la carpeta del usuario creado


                final HashMap<String, Object> user = new HashMap<>();
                user.put("nombre", currentUser.getDisplayName());
                user.put("tipo", "free");
                user.put("capacidad", "250MB");

                db.collection("users").document(currentUser.getUid())
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });

                CollectionReference usersCollectionRef = db.collection("users").document(currentUser.getUid()).collection("privatefiles");
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
                                    if(user.get("tipo").equals("free")&&(cant<5)){

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

                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
                //

                //La carpeta se creara a penas se añada un archivo
                goToMainScreen();
            } else {
                currentUser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (currentUser.isEmailVerified()) {
                            goToMainScreen();
                        } else {
                            Toast.makeText(MainActivity.this, "Se ha enviado un correo para que verifique la cuenta", Toast.LENGTH_SHORT).show();
                            currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d("InfoApp", "correo enviado");
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    public void goToMainScreen() {
        startActivity(new Intent(MainActivity.this, PrincipalActivity.class));
        finish();
    }



}