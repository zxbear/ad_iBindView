package com.zxbear.ibindview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.zxbear.ibvannot.IBindView;
import com.zxbear.ibvannot.IBindViews;
import com.zxbear.ibvapi.IBinds;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @IBindView(R.id.bind_tv)
    public TextView tv;

    @IBindView(R.id.bind_btn)
    public Button btn;

    @IBindViews({R.id.bind_tv,R.id.bind_tv2})
    public List<TextView> tvs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IBinds.bind(this);
        tv.setText("test tv");
        btn.setOnClickListener(v -> {
            tvs.get(1).setText("test tv2");
        });
    }

}