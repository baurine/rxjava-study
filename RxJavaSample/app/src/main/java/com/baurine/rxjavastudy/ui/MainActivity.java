package com.baurine.rxjavastudy.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.baurine.rxjavastudy.R;
import com.baurine.rxjavastudy.presenter.RxJavaPresenter;

public class MainActivity extends AppCompatActivity
        implements ResultView {

    private TextView tvResult;
    private RxJavaPresenter rxJavaPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rxJavaPresenter = new RxJavaPresenter(this);
        initViews();
    }

    private void initViews() {
        tvResult = (TextView) findViewById(R.id.tv_result);
        showResult("");
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_test_basic:
                rxJavaPresenter.testBasic();
                break;
        }
    }

    @Override
    public void showResult(String result) {
        tvResult.setText(getString(R.string.result, result));
    }
}
