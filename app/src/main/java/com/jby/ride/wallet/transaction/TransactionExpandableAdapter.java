package com.jby.ride.wallet.transaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.jby.ride.R;
import com.jby.ride.ride.past.PastRideAdapter;

import java.util.ArrayList;

public class TransactionExpandableAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<TransactionParentObject> transactionParentObjectArrayList;
    private  TransactionExpandableAdapterCallBack transactionExpandableAdapterCallBack;

    public TransactionExpandableAdapter(Context context, ArrayList<TransactionParentObject> transactionParentObjectArrayList, TransactionExpandableAdapterCallBack transactionExpandableAdapterCallBack){
        this.context = context;
        this.transactionParentObjectArrayList = transactionParentObjectArrayList;
        this.transactionExpandableAdapterCallBack = transactionExpandableAdapterCallBack;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
/*-----------------------------------------------------------------------------PARENT VIEW-------------------------------------------------------------*/
    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            convertView = layoutInflater.inflate(R.layout.transaction_dialog_parent_list_view_item, null);
            groupViewHolder = new GroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);

        }
        else
            groupViewHolder = (GroupViewHolder)convertView.getTag();

        TransactionParentObject object = getGroup(groupPosition);
        groupViewHolder.date.setText(object.getDate());

        return convertView;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public TransactionParentObject getGroup(int i) {
        return transactionParentObjectArrayList.get(i);
    }

    private static class GroupViewHolder{
        TextView date;
        GroupViewHolder (View view){
            date = view.findViewById(R.id.transaction_dialog_parent_list_view_item_date);
        }
    }

    @Override
    public int getGroupCount() {
        return transactionParentObjectArrayList.size();
    }


    /*-----------------------------------------------------------------------END OF PARENT VIEW-------------------------------------------------------------*/
/*---------------------------------------------------------------------------CHILD VIEW-------------------------------------------------------------------*/
    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        ChildViewHolder viewHolder;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            view = layoutInflater.inflate(R.layout.transaction_dialog_child_list_view_item, null);
                viewHolder = new ChildViewHolder(view);
                view.setTag(viewHolder);
        }
         else
            viewHolder = (ChildViewHolder) view.getTag();

            final TransactionChildObject object = getChild(groupPosition, childPosition);

            viewHolder.label.setText(object.getTitle());
            viewHolder.status.setText(object.getStatus());
            viewHolder.amount.setText(object.getAmount());
            viewHolder.amount.setTextColor(setAmountColor(object.getTitle()));

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickEffect(view);
                    transactionExpandableAdapterCallBack.openTransactionDetail(object.getId());
                }
            });
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public int getChildrenCount(int i) {
        return transactionParentObjectArrayList.get(i).getTransactionChildObjectArrayList().size();
    }

    @Override
    public TransactionChildObject getChild(int groupPosition, int childPosition) {
        return transactionParentObjectArrayList.get(groupPosition).getTransactionChildObjectArrayList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private int setAmountColor(String label){
        if(label.equals("Top-Up"))
            return  context.getResources().getColor(R.color.green);
        else
            return context.getResources().getColor(R.color.red);
    }

    private static class ChildViewHolder{
        final TextView label, status, amount;

        ChildViewHolder (View view){
            label = view.findViewById(R.id.transaction_dialog_child_list_view_item_label);
            status = view.findViewById(R.id.transaction_dialog_child_list_view_item_status);
            amount = view.findViewById(R.id.transaction_dialog_child_list_view_item_amount);
        }
    }
/*-----------------------------------------------------------------------------------END OF CHILD VIEW---------------------------------------------------------*/

    public void clickEffect(View view){
        Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
        animation1.setDuration(500);
        view.startAnimation(animation1);
    }

    public interface TransactionExpandableAdapterCallBack{
        void openTransactionDetail(String id);
    }
}
