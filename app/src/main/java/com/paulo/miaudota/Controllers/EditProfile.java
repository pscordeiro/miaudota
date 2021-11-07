package com.paulo.miaudota.Controllers;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.paulo.miaudota.R;
import com.paulo.miaudota.Models.User;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity implements View.OnClickListener  {

    private EditText editTextFullName,editTextCPF,editTextEmail,editTextCidade,editTextUf,editTextDataNascimento;
    private CircleImageView profilePicture;
    private ProgressBar progressBar;
    private String userID;
    private String profilePicUrl;
    private String dataN;
    private String dataAtual;
    final Calendar myCalendar = Calendar.getInstance();
    private Uri cropImageUri;
    private Bitmap bitmap = null;
    DatePickerDialog.OnDateSetListener date;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference reference;

    @Override
    protected void onStart() {
        Log.e("Warning_Activity","onStart EditProfile -> ");
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e("Warning_Activity","onStart EditProfile userNull-> ");
            startActivity(new Intent(EditProfile.this, WelcomeScreen.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("Warning_Activity","onCreate EditProfile -> ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //carregando informações do usuário
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();
        profilePicture =  findViewById(R.id.profilePictureEdit);
        cropImageUri = user.getPhotoUrl();
        editTextFullName = findViewById(R.id.nomeProfileEdit);
        editTextCPF = findViewById(R.id.cpfProfileEdit);
        editTextEmail = findViewById(R.id.emailProfileEdit);
        editTextCidade = findViewById(R.id.cidadeProfileEdit);
        editTextUf = findViewById(R.id.ufProfileEdit);
        editTextDataNascimento = findViewById(R.id.btnDataNascimentoProfileEdit);
        editTextDataNascimento.setOnClickListener(this);
        progressBar = findViewById(R.id.progressBarPRofileEdit);
        progressBar.setVisibility(View.VISIBLE);

        if (user == null) {
            Log.e("Warning_Activity","onCreate EditProfile userNull-> ");
            startActivity(new Intent(EditProfile.this, WelcomeScreen.class));
        }

        Button updateProfile = findViewById(R.id.btnUpdate);
        updateProfile.setOnClickListener(this);

        ImageButton updateProfilePicture = findViewById(R.id.btnProfilePictureEdit);
        updateProfilePicture.setOnClickListener(this);
        profilePicture.setOnClickListener(this);

        date = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        //Carregar informações do usuário completo
        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User userProfile = snapshot.getValue(User.class);
                String nomeCompleto;
                String email;

                try {
                    profilePicUrl = user.getPhotoUrl().toString();
                }
                catch (Exception ex){
                    Log.e("Warning_Activity","profilePicUrl -> Erro ao buscar foto: " + ex);
                }

                if(userProfile.Cpf != null){
                    //se for completo carrega tudo
                    nomeCompleto = userProfile.NomeCompleto;
                    email = userProfile.Email;
                    String cpf = userProfile.Cpf;
                    String cidade = userProfile.Cidade;
                    String uf = userProfile.UF;
                    String dataNascimento = userProfile.DataNascimento;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date dataFormatada = null;
                    try {
                        dataFormatada = dateFormat.parse(dataNascimento);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    myCalendar.setTime(dataFormatada);

                    editTextFullName.setText(nomeCompleto);
                    editTextEmail.setText(email);
                    editTextCPF.setText(cpf);
                    editTextCidade.setText(cidade);
                    editTextUf.setText(uf);
                    editTextDataNascimento.setText(dataNascimento);

                    Glide.with(EditProfile.this)
                            .load(profilePicUrl) // image url
                            .placeholder(R.drawable.profile_placeholder) // any placeholder to load at start
                            .error(R.drawable.profile_placeholder)  // any image in case of error
                            .override(200, 200) // resizing
                            .centerCrop()
                            .into(profilePicture);  // imageview object

                    progressBar.setVisibility(View.INVISIBLE);
                }
                else{
                    //se for incompleto carrega email e nome e a foto
                    nomeCompleto = userProfile.NomeCompleto;
                    email = userProfile.Email;

                    editTextFullName.setText(nomeCompleto);
                    editTextEmail.setText(email);

                    Glide.with(EditProfile.this)
                            .load(profilePicUrl) // image url
                            .placeholder(R.drawable.profile_placeholder) // any placeholder to load at start
                            .error(R.drawable.profile_placeholder)  // any image in case of error
                            .override(200, 200) // resizing
                            .centerCrop()
                            .into(profilePicture);  // imageview object

                    progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Warning_Activity","loadUser -> erro onCancelled.");
                Toast.makeText(EditProfile.this,"Algo deu errado !",Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnUpdate:
                updateProfile();
                break;
            case R.id.btnDataNascimentoProfileEdit:
                new DatePickerDialog(EditProfile.this, R.style.my_dialog_theme ,date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.btnProfilePictureEdit:
            case R.id.profilePictureEdit:
                updateProfilePicture();
                break;
        }
    }

    ActivityResultLauncher<Intent> profilePicActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Intent data = result.getData();
                        cropImageUri = data.getData();

                        CropImage.activity(cropImageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropMenuCropButtonTitle("Meter o corte")
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setAspectRatio(1,1)
                        .start(EditProfile.this);
                    }
                }
    });

    public void updateProfilePicture() {
        Intent openGalleryIntent = new Intent();
        openGalleryIntent.setType("image/*");
        openGalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        profilePicActivityResultLauncher.launch(openGalleryIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                cropImageUri = result.getUri();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(cropImageUri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                }
                catch (Exception ex){
                    Log.e("TAG","falhou bitmap: ", ex);
                }
                Glide.with(EditProfile.this)
                        .load(cropImageUri.toString()) // image url
                        .placeholder(R.drawable.profile_placeholder) // any placeholder to load at start
                        .error(R.drawable.profile_placeholder)  // any image in case of error
                        .override(200, 200) // resizing
                        .centerCrop()
                        .into(profilePicture);  // imageview object
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("Warning_Activity","profilePicUrl -> Erro ao cortar foto: " + error);
                Glide.with(EditProfile.this)
                        .load(profilePicUrl) // image url
                        .placeholder(R.drawable.profile_placeholder) // any placeholder to load at start
                        .error(R.drawable.profile_placeholder)  // any image in case of error
                        .override(200, 200) // resizing
                        .centerCrop()
                        .into(profilePicture);  // imageview object
            }
        }
    }

    private void updateProfile() {
        updateLabel();

        String email = editTextEmail.getText().toString().trim();
        String nomeCompleto = editTextFullName.getText().toString().trim();
        String cpf = editTextCPF.getText().toString().trim();
        String cidade = editTextCidade.getText().toString().trim();
        String uf = editTextUf.getText().toString().toUpperCase(Locale.ROOT).trim();
        String dataNascimento = dataN.toString().trim();

        if(nomeCompleto.isEmpty()){
            editTextFullName.setError("Campo obrigatório !!");
            editTextFullName.requestFocus();
            return;
        }

        if(cpf.isEmpty()){
            editTextCPF.setError("Campo obrigatório !!");
            editTextCPF.requestFocus();
            return;
        }

        validateCPF();

        if(email.isEmpty()){
            editTextEmail.setError("Campo obrigatório !!");
            editTextEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Por favor digite um email válido!");
            editTextEmail.requestFocus();
            return;
        }

        if(cidade.isEmpty()){
            editTextCidade.setError("Campo obrigatório !!");
            editTextCidade.requestFocus();
            return;
        }

        if(uf.length() > 2){
            editTextUf.setError("A unidade federativa não pode possuir mais que 2 caracteres");
            editTextUf.requestFocus();
            return;
        }

        if(dataNascimento.equals(dataAtual)){
            Toast.makeText(EditProfile.this,"Você não nasceu hoje porra",Toast.LENGTH_LONG).show();
            editTextDataNascimento.setError("Campo obrigatório !!");
            editTextDataNascimento.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        User usuario = new User(nomeCompleto, cpf , email, cidade, uf, dataNascimento);
        reference.child(userID).setValue(usuario);
        if(bitmap != null){
            handleUpload(bitmap);
        }
        else{
            Intent intent = new Intent(EditProfile.this,ProfileFragment.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); finish();
            Toast.makeText(EditProfile.this,"Perfil atualizado com sucesso !!",Toast.LENGTH_LONG).show();
        }
    }

    private void handleUpload(Bitmap bitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("profileImages")
                .child(userID + ".jpeg");

        ref.putBytes(baos.toByteArray())
                .addOnSuccessListener(taskSnapshot -> getDownloadUrl(ref))
                .addOnFailureListener(e -> Log.e("TAG","onFailure: ", e.getCause() ));
    }

    private void getDownloadUrl(StorageReference sReference){
        sReference.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Log.e("TAG","onSuccess: " + uri );
                    setUserProfileUrl(uri);
                });
    }

    private void setUserProfileUrl(Uri uri){
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        user.updateProfile(request)
                .addOnSuccessListener(unused -> {
                    Log.d("Profile_Photo", "User profile updated.");
                    Intent intent = new Intent(EditProfile.this,ProfileFragment.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); finish();
                    Toast.makeText(EditProfile.this,"Perfil atualizado com sucesso !!",Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("TAG","onFailure: ", e.getCause() );
                    Toast.makeText(EditProfile.this,"Falha ao enviar imagem do perfil...",Toast.LENGTH_LONG).show();
                });
    }

    private void validateCPF() {

    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dataN = sdf.format(myCalendar.getTime());
        dataAtual = sdf.format(Calendar.getInstance().getTime());
        if(!dataN.toString().equals(dataAtual)){
            editTextDataNascimento.setText(dataN);
        }
    }

}