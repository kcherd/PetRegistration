package com.example.petreg;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class RegisterFragment extends Fragment {

    private EditText idTV;
    private EditText nameTV;
    private Button recordButton;

    public interface RegisterEventListener {
        public void registerEvent(String id);
    }

    RegisterEventListener eventListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        eventListener = (RegisterEventListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.register_fragment, container, false);
        idTV = v.findViewById(R.id.id_reg_tv);
        nameTV = v.findViewById(R.id.name_reg_tv);
        recordButton = v.findViewById(R.id.record_button);


        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventListener.registerEvent(idTV.getText().toString());
            }
        });
        return v;
    }

}
