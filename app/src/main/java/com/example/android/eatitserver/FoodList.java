package com.example.android.eatitserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.android.eatitserver.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import com.example.android.eatitserver.Model.Food;
import com.example.android.eatitserver.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RelativeLayout rootLayout;

    FloatingActionButton fab;
    Button btnSelect,btnUpload;
    EditText edtName,edtDescription,edtPrice,edtDiscount;

    FirebaseDatabase database;
    DatabaseReference foodList;
    FirebaseStorage storage;
    StorageReference storageReference;

    String categoryId="";
    Uri saveUri;
    Food newFood;

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        database =FirebaseDatabase.getInstance();
        foodList=database.getReference("Foods");
        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReference();

        recyclerView=findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout=findViewById(R.id.rootLayout);

        fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFoodDialog();
            }
        });

        if(getIntent()!=null)
            categoryId=getIntent().getStringExtra("CategoryId");
        if(!categoryId.isEmpty()){
            loadListFood(categoryId);
        }

    }

    private void loadListFood(String categoryId) {
        Query query=foodList.orderByChild("menuId").equalTo(categoryId);
        FirebaseRecyclerOptions<Food> options=new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(query,Food.class)
                .build();

        adapter= new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);
                return new FoodViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int i, @NonNull Food model) {
                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);
                //final Food local=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                       /* //Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();
                        Intent fooddetail=new Intent(FoodList.this,FoodDetail.class);
                        fooddetail.putExtra("FoodId",adapter.getRef(position).getKey());
                        startActivity(fooddetail);
                        */

                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void showAddFoodDialog() {
        final AlertDialog.Builder myDialog =new AlertDialog.Builder(FoodList.this);
        myDialog.setTitle("Add new Food item");
        myDialog.setMessage("Please fill all information");

        LayoutInflater inflater=this.getLayoutInflater();
        View add_menu_layout= inflater.inflate(R.layout.add_new_food_layout,null);

        btnSelect=add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload=add_menu_layout.findViewById(R.id.btnUpload);
        edtName=add_menu_layout.findViewById(R.id.edtName);
        edtDescription=add_menu_layout.findViewById(R.id.edtDescription);
        edtPrice=add_menu_layout.findViewById(R.id.edtPrice);
        edtDiscount=add_menu_layout.findViewById(R.id.edtDiscount);

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

        myDialog.setView(add_menu_layout);
        myDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        myDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(newFood!=null){
                    foodList.push().setValue(newFood);
                    Snackbar.make(rootLayout,"New Food item "+newFood.getName()+" was added.",Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        myDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        myDialog.show();

    }

    private void uploadImage() {
        if(saveUri!=null){
            final ProgressDialog dialog = new ProgressDialog(FoodList.this);
            dialog.setMessage("Uploading");
            dialog.show();

            String imageName= UUID.randomUUID().toString();
            final StorageReference imageFolder= storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText(FoodList.this, "Uploaded...", Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //set value for new category if image uploaded & we can get download link
                            newFood=new Food();
                            newFood.setName(edtName.getText().toString());
                            newFood.setDescription(edtDescription.getText().toString());
                            newFood.setPrice(edtPrice.getText().toString());
                            newFood.setDiscount(edtDiscount.getText().toString());
                            newFood.setMenuId(categoryId);
                            newFood.setImage(uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(FoodList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
            btnSelect.setText("Image selected!");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateFoodDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }else if(item.getTitle().equals(Common.DELETE)){
            deleteFood(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteFood(String key) {
        foodList.child(key).removeValue();
    }

    private void showUpdateFoodDialog(final String key, final Food item) {
        //
        final AlertDialog.Builder myDialog =new AlertDialog.Builder(FoodList.this);
        myDialog.setTitle("Edit Food");
        myDialog.setMessage("Please fill all information");

        LayoutInflater inflater=this.getLayoutInflater();
        View add_menu_layout= inflater.inflate(R.layout.add_new_food_layout,null);

        btnSelect=add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload=add_menu_layout.findViewById(R.id.btnUpload);
        edtName=add_menu_layout.findViewById(R.id.edtName);
        edtDescription=add_menu_layout.findViewById(R.id.edtDescription);
        edtPrice=add_menu_layout.findViewById(R.id.edtPrice);
        edtDiscount=add_menu_layout.findViewById(R.id.edtDiscount);

        //set default value for view
        edtName.setText(item.getName());
        edtDescription.setText(item.getDescription());
        edtPrice.setText(item.getPrice());
        edtDiscount.setText(item.getDiscount());

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

        myDialog.setView(add_menu_layout);
        myDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        myDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //update information
                item.setName(edtName.getText().toString());
                item.setDescription(edtDescription.getText().toString());
                item.setPrice(edtPrice.getText().toString());
                item.setDiscount(edtDiscount.getText().toString());

                foodList.child(key).setValue(item);
                Snackbar.make(rootLayout,""+item.getName()+" was edited.",Snackbar.LENGTH_SHORT).show();
            }
        });
        myDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        myDialog.show();
    }

    private void changeImage(final Food item) {
        if(saveUri!=null){
            final ProgressDialog dialog = new ProgressDialog(FoodList.this);
            dialog.setMessage("Uploading");
            dialog.show();

            String imageName= UUID.randomUUID().toString();
            final StorageReference imageFolder= storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText(FoodList.this, "Uploaded...", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(FoodList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }
}
