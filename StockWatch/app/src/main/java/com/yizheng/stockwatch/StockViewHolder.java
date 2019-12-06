package com.yizheng.stockwatch;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StockViewHolder extends RecyclerView.ViewHolder {

    TextView symbolText, priceText, changeText, nameText;
    private ArrayList<TextView> textViews = new ArrayList<>();

    public StockViewHolder(@NonNull View itemView) {
        super(itemView);

        symbolText = itemView.findViewById(R.id.symbolText);
        priceText = itemView.findViewById(R.id.priceText);
        changeText = itemView.findViewById(R.id.changeText);
        nameText = itemView.findViewById(R.id.nameText);
        textViews.add(symbolText); textViews.add(priceText); textViews.add(changeText); textViews.add(nameText);
    }

    void setColor(int color){
        for (TextView tv: textViews){
            tv.setTextColor(color);
        }
    }
}
