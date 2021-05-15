package com.example.android.eatitserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.android.eatitserver.Common.Common;
import com.example.android.eatitserver.Interface.ItemClickListener;
import com.example.android.eatitserver.Model.Banner;
import com.example.android.eatitserver.Model.Food;
import com.example.android.eatitserver.ViewHolder.BannerViewHolder;
import com.example.android.eatitserver.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class BannerManagement extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RelativeLayout rootLayout;

    FloatingActionButton fab;
    Button btnSelect,btnUpload;
    EditText edtName,edtId;

    FirebaseDatabase database;
    DatabaseReference bannerList;
    FirebaseStorage storage;
    StorageReference storageReference;

    String foodId="";
    Uri saveUri;

    Banner newBanner;

    FirebaseRecyclerAdapter<Banner, BannerViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_management);

        database =FirebaseDatabase.getInstance();
        bannerList=database.getReference("Banner");
        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReference();

        recyclerView=findViewById(R.id.recycler_banner);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout=findViewById(R.id.rootLayout);

        fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddBannerDialog();
            }
        });

        loadListBanner();
    }

    private void loadListBanner() {
        Query query=bannerList;
        FirebaseRecyclerOptions<Banner> options=new FirebaseRecyclerOptions.Builder<Banner>()
                .setQuery(query,Banner.class)
                .build();

        adapter= new FirebaseRecyclerAdapter<Banner, BannerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BannerViewHolder viewHolder, int i, @NonNull Banner model) {
                viewHolder.banner_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.banner_image);
                //final Food local=model;
            }

            @NonNull
            @Override
            public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.banner_layout, parent, false);
                return new BannerViewHolder(view);
            }


        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void showAddBannerDialog() {
        final AlertDialog.Builder myDialog =new AlertDialog.Builder(BannerManagement.this);
        myDialog.setTitle("Add new Banner item");
        myDialog.setMessage("Please fill all information");

        LayoutInflater inflater=this.getLayoutInflater();
        View add_banner_layout= inflater.inflate(R.layout.add_new_banner_layout,null);

        edtId=add_banner_layout.findViewById(R.id.edtFoodId);
        edtName=add_banner_layout.findViewById(R.id.edtFoodName);
        btnSelect=add_banner_layout.findViewById(R.id.btnSelect);
        btnUpload=add_banner_layout.findViewById(R.id.btnUpload);


        //event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();//Let user select the image from gallery and save the url or path
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        myDialog.setView(add_banner_layout);
        myDialog.setIcon(R.drawable.ic_laptop_black_24dp);

        myDialog.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(newBanner!=null){
                    bannerList.push().setValue(newBanner);
                    Snackbar.make(rootLayout,"New banner "+newBanner.getName()+" was added.",Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        myDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        myDialog.show();
    }

    private void uploadImage() {
        if(saveUri!=null){
            final ProgressDialog dialog = new ProgressDialog(BannerManagement.this);
            dialog.setMessage("Uploading");
            dialog.show();

            String imageName= UUID.randomUUID().toString();
            final StorageReference imageFolder= storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText(BannerManagement.this, "Uploaded...", Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //set value for new category if image uploaded & we can get download link
                            newBanner=new Banner();
                            newBanner.setName(edtName.getText().toString());
                            newBanner.setId(edtId.getText().toString());
                            newBanner.setImage(uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(BannerManagement.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress=(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    dialog.setMessage("Uploaded "+progress+"%");
                }
            });
        }
    }

    private void chooseImage() {
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        //intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image"), Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            saveUri=data.getData();
            btnSelect.setText("Image Selected!");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateBannerDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }else if(item.getTitle().equals(Common.DELETE)){
            deleteBanner(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteBanner(String key) {
        bannerList.child(key).removeValue();
    }

    private void showUpdateBannerDialog(final String key, final Banner item) {
        final AlertDialog.Builder myDialog =new AlertDialog.Builder(BannerManagement.this);
        myDialog.setTitle("Edit Banner");
        myDialog.setMessage("Please fill all information");

        LayoutInflater inflater=this.getLayoutInflater();
        View add_banner_layout= inflater.inflate(R.layout.add_new_banner_layout,null);

        btnSelect=add_banner_layout.findViewById(R.id.btnSelect);
        btnUpload=add_banner_layout.findViewById(R.id.btnUpload);
        edtName=add_banner_layout.findViewById(R.id.edtFoodName);
        edtId=add_banner_layout.findViewById(R.id.edtFoodId);

        //set default value for view
        edtName.setText(item.getName());
        edtId.setText(item.getId());


        //event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();//Let user select the image from gallery and save the url or path
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });

        myDialog.setView(add_banner_layout);
        myDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        myDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //update information
                item.setName(edtName.getText().toString());
                item.setId(edtId.getText().toString());

                bannerList.child(key).setValue(item);
                Snackbar.make(rootLayout,""+item.getName()+" was edited.",Snackbar.LENGTH_SHORT).show();
            }
        });
        myDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        myDialog.show();
    }

    private void changeImage(final Banner item) {
        if(saveUri!=null){
            final ProgressDialog dialog = new ProgressDialog(BannerManagement.this);
            dialog.setMessage("Uploading");
            dialog.show();

            String imageName= UUID.randomUUID().toString();
            final StorageReference imageFolder= storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText(BannerManagement.this, "Uploaded...", Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //set value for new category if image uploaded & we can get download link
                            item.setImage(uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(BannerManagement.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress=(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    dialog.setMessage("Uploaded "+progress+"%");
                }
            });
        }
    }
}
