package com.example.android.eatitserver.ViewHolder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.eatitserver.Model.Order;
import com.example.android.eatitserver.R;

import java.util.List;

class MyViewHolder extends RecyclerView.ViewHolder{

    public TextView pname,quantity,price,discount;

    public MyViewHolder(View itemView) {
        super(itemView);
        pname=itemView.findViewById(R.id.product_name);
        quantity=itemView.findViewById(R.id.product_quantity);
        price=itemView.findViewById(R.id.product_price);
        discount=itemView.findViewById(R.id.product_discount);
    }
}
public class OrderDetailAdapter extends RecyclerView.Adapter<MyViewHolder> {

    List<Order> myOrders;

    public OrderDetailAdapter(List<Order> myOrders) {
        this.myOrders = myOrders;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView= LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.order_detail_layout,viewGroup,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
        Order order=myOrders.get(i);
        myViewHolder.pname.setText(String.format("Name : %s",order.getProductName()));
        myViewHolder.quantity.setText(String.format("Quantity : %s",order.getQuantity()));
        myViewHolder.price.setText(String.format("Price : %s",order.getPrice()));
        myViewHolder.discount.setText(String.format("Discount : %s",order.getDiscount()));
    }

    @Override
    public int getItemCount() {
        return myOrders.size();
    }
}
