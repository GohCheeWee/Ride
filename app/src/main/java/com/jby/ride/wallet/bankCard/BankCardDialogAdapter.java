package com.jby.ride.wallet.bankCard;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jby.ride.R;
import java.util.ArrayList;
import java.util.Objects;

public class BankCardDialogAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<BankCardDialogObject> bankCardDialogObjectArrayList;
    private String fromDialog;

    public BankCardDialogAdapter(Context context, ArrayList<BankCardDialogObject> bankCardDialogObjectArrayList, String fromDialog){
        this.context = context;
        this.bankCardDialogObjectArrayList = bankCardDialogObjectArrayList;
        this.fromDialog = fromDialog;
    }
    @Override
    public int getCount() {
        return bankCardDialogObjectArrayList.size();
    }

    @Override
    public BankCardDialogObject getItem(int i) {
        return bankCardDialogObjectArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.bank_card_dialog_list_view_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        BankCardDialogObject object = getItem(i);

        String card_num = object.getCardNumber();
        card_num = card_num.substring(card_num.length()-4);
        card_num = "**** **** **** " + card_num;

        String cardType = object.getType();
        String provider = object.getProvider();
        String status = object.getStatus();

        //if opening from top up or withdraw dialog then hide the filter view
        if(fromDialog.equals("")){
            if(status.equals("1"))
                viewHolder.defaultCardLayout.setVisibility(View.GONE);
            else
                viewHolder.defaultCardLayout.setVisibility(View.VISIBLE);
        }
        else
            viewHolder.defaultCardLayout.setVisibility(View.GONE);

        if(cardType.equals("1")){
            cardType = "Debit Card";
        }
        else
            cardType = "Credit Card";

        viewHolder.cardNumber.setText(card_num);
        viewHolder.cardType.setText(cardType);
        viewHolder.cardProvider.setText(provider);
        viewHolder.cardProviderIcon.setImageDrawable(cardProvider(provider));
        return view;
    }


    private Drawable cardProvider(String provider){
        Drawable drawable;
        switch (provider){
            case "Master Card":
                drawable = Objects.requireNonNull(context).getResources().getDrawable(R.drawable.fragment_pending_ride_detail_payment_method);
                break;
            case "Discover Network":
                drawable = Objects.requireNonNull(context).getResources().getDrawable(R.drawable.bank_card_dialog_discover_network_icon);
                break;
            case "American Express":
                drawable = Objects.requireNonNull(context).getResources().getDrawable(R.drawable.bank_card_dialog_american_icon);
                break;
            case "VISA":
                drawable = Objects.requireNonNull(context).getResources().getDrawable(R.drawable.bank_card_dialog_visa_icon);
                break;
                default:
                    drawable = Objects.requireNonNull(context).getResources().getDrawable(R.drawable.bank_card_dialog_default_card_icon);
        }
        return drawable;
    }


    private static class ViewHolder{
        private TextView cardProvider, cardType, cardNumber;
        private ImageView cardProviderIcon;
        private View defaultCardLayout;

        ViewHolder (View view){
            cardProvider = view.findViewById(R.id.bank_card_dialog_list_view_item_card_provider);
            cardType = view.findViewById(R.id.bank_card_dialog_list_view_item_card_type);
            cardNumber = view.findViewById(R.id.bank_card_dialog_list_view_item_card_number);
            cardProviderIcon = view.findViewById(R.id.bank_card_dialog_list_view_item_card_type_icon);
            defaultCardLayout = view.findViewById(R.id.bank_card_dialog_list_view_item_default_card_layout);
        }
    }
}
