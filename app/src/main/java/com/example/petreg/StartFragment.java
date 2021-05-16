package com.example.petreg;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class StartFragment extends Fragment {
    private static final String TAG = "StartFragment";
    private Button regButton;
    private Button infoButton;
    private Button mapButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.start_fragment, container, false);

        regButton = v.findViewById(R.id.register);
        infoButton = v.findViewById(R.id.info);
        mapButton = v.findViewById(R.id.map);

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                RegisterDialogFragment registerDialogFragment = new RegisterDialogFragment();
                registerDialogFragment.show(fragmentManager, "RegisterDialog");
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                InfoDialogFragment infoDialogFragment = new InfoDialogFragment();
                infoDialogFragment.show(fragmentManager, "InfoDialog");
            }
        });

        return v;
    }
}