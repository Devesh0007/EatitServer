package com.example.android.eatitserver.ViewHolder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.example.android.eatitserver.R;

import info.hoang8f.widget.FButton;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    public TextView txtOrderId,txtOrderStatus,txtOrderAddress,txtOrderPhone;
    public FButton btnEdit,btnRemove,btnDetail,btnDirection;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        txtOrderId=itemView.findViewById(R.id.order_id);
        txtOrderAddress=itemView.findViewById(R.id.order_address);
        txtOrderPhone=itemView.findViewById(R.id.order_phone);
        txtOrderStatus=itemView.findViewById(R.id.order_status);
        btnEdit=itemView.findViewById(R.id.btnEdit);
        btnRemove=itemView.findViewById(R.id.btnRemove);
        btnDetail=itemView.findViewById(R.id.btnDetails);
        btnDirection=itemView.findViewById(R.id.btnDirection);
    }

}
