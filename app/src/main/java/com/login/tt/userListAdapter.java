package com.login.tt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class userListAdapter extends RecyclerView.Adapter<userListAdapter.ViewHolder>{
    //ArrayList<currentJoinUser> mDataList  역활 :
    // userList 클래스에서 AsyncTask를 사용하여 데이터 베이스에서 받은  Json 파일을 Gson으로 파싱하여 adapter 생성자에 넣는다
    // onCreateViewHolder 역활 : ViewHolder 클래스 파일에서  연결한 파일을 onCreateViewHolder 에서 view에서 어떤 Item을 연결할지 성정하고 반환한다
    //onBindViewHolder 역활 :  Item에 넣을 값을 설정한다. 해당 설정에 listener을 달아서 클릭햇을때 어떤 행동을 할지도 설정할 수 있다.
    ArrayList<currentJoinUser> mDataList;

    private r_Click_Listener mListener;

    public interface r_Click_Listener{
        void onItemClicked(int position);
//        void deleteClicked(int position);
    }

    public void setOnclickListener(r_Click_Listener listener){
        mListener=listener;
    }

    public userListAdapter(ArrayList<currentJoinUser> DataList){
        this.mDataList=DataList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_list_item,viewGroup,false);
        return new ViewHolder(view);
    }
    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        currentJoinUser item=mDataList.get(position);
        holder.nikName.setText(item.nikName); //뷰홀더의 텍스트뷰 값을 설정


        if(mListener!=null){
            final int pos=position;
            holder.sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onItemClicked(pos);
                }
            });
        }
    }



    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView nikName;
        Button sendBtn;

        public ViewHolder(View itemview){
            super(itemview);
            nikName=itemview.findViewById(R.id.nikName);
            sendBtn=itemview.findViewById(R.id.sendBtn);

        }
    }

}
