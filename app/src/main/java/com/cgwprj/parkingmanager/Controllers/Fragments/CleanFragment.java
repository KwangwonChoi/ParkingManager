package com.cgwprj.parkingmanager.Controllers.Fragments;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cgwprj.parkingmanager.Controllers.Acitivity.MainActivity;
import com.cgwprj.parkingmanager.Data.UserData;
import com.cgwprj.parkingmanager.Models.CarInfo;
import com.cgwprj.parkingmanager.Models.CarInquiryInfo;
import com.cgwprj.parkingmanager.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CleanFragment extends android.support.v4.app.Fragment{

    private MainActivity mainActivity;
    FirebaseFirestore db;
    List<CarInfo> carInfos;
    SimpleAdapter simpleAdapter;
    ArrayAdapter<String> selectedAdapter;
    Map<String, CarInfo> clickedNumber;

    List<HashMap<String,String>> DataList;
    TextView carNumberView;

    public CleanFragment() {

    }

    public void setCarInfos(List<CarInfo> carInfos){
        this.carInfos = carInfos;
    }

    public static CleanFragment newInstance(MainActivity mainActivity) {
        CleanFragment fragment = new CleanFragment();
        fragment.setActivity(mainActivity);
        return fragment;
    }

    public void setActivity(MainActivity activity){
        this.mainActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final Fragment fragment = this;
        clickedNumber = new HashMap<>();
        db = FirebaseFirestore.getInstance();
        DataList = new ArrayList<>();

        final View view = inflater.inflate(R.layout.fragment_clean, container, false);

        carNumberView = view.findViewById(R.id.clean_selected_car_number);
        final ListView listView = view.findViewById(R.id.clean_car_list_view);
        final ListView selectedListView = view.findViewById(R.id.clean_car_selected_list_view);
        listView.clearAnimation();

        Button cleanBtn = view.findViewById(R.id.clean_btn);

        setCarInfos(((MainActivity) getActivity()).getCarInfos());

        if(carInfos != null)
            for (CarInfo c : carInfos) {
                HashMap<String,String> map = new HashMap<>();
                map.put("CarNumber", c.getCarNumber());
                map.put("Register", c.getRegisterTime());
                DataList.add(map);
            }
        else
            carInfos = new ArrayList<>();

        simpleAdapter = new SimpleAdapter(getContext(),DataList,android.R.layout.simple_list_item_2,new String[]{"CarNumber","Register"},new int[]{android.R.id.text1,android.R.id.text2});
        selectedAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);

        listView.setAdapter(simpleAdapter);
        selectedListView.setAdapter(selectedAdapter);
        simpleAdapter.notifyDataSetChanged();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CarInfo curInfo = carInfos.get(position);

                if(clickedNumber.get(curInfo.getCarNumber()) != null){
                    selectedAdapter.remove(curInfo.getCarNumber());
                    clickedNumber.remove(curInfo.getCarNumber());
                }
                else{
                    selectedAdapter.add(curInfo.getCarNumber());
                    clickedNumber.put(curInfo.getCarNumber(), curInfo);
                }

                String carNumberViewText = "선택된 차 갯수 : " + Integer.toString(clickedNumber.size());
                carNumberView.setText(carNumberViewText);
                selectedAdapter.notifyDataSetChanged();
            }
        });

        cleanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder oDialog = new AlertDialog.Builder(getContext(),
                        android.R.style.Theme_DeviceDefault_Light_Dialog);

                String message = Integer.toString(clickedNumber.size()) + "대를 제외한 모든 차량을 삭제하시겠습니까?";
                oDialog.setMessage(message)
                        .setPositiveButton("아니오", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                            }
                        })
                        .setNeutralButton("예", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                for (CarInfo carInfo : carInfos) {
                                    if (clickedNumber.get(carInfo.getCarNumber()) == null) {
                                        CarInquiryInfo carInquiryInfo = new CarInquiryInfo(carInfo);

                                        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference()
                                                .child(UserData.getInstance().getParkingLot())
                                                .child(Integer.toString(carInfo.hashCode()));
                                        myRef.setValue(null);

                                        db.collection("PARKINGLOT")
                                                .document(UserData.getInstance().getParkingLot())
                                                .collection(Integer.toHexString(carInfo.getCarNumber().hashCode()))
                                                .document(Integer.toString(carInquiryInfo.hashCode()))
                                                .set(carInquiryInfo);

                                        Toast.makeText(view.getContext(), carInfo.getCarNumber() + " 출차 하였습니다.", Toast.LENGTH_SHORT).show();

                                        mainActivity.ChangeFragmentToMain();
                                    }
                                }
                            }
                        })
                        .show();




            }
        });

        String carNumberViewText = "선택된 차 갯수 : " + Integer.toString(clickedNumber.size());
        carNumberView.setText(carNumberViewText);

        return view;
    }



}
