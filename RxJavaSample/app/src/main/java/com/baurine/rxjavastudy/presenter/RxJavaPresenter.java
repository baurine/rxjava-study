package com.baurine.rxjavastudy.presenter;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by baurine on 2/25/17.
 */

public class RxJavaPresenter {

    public interface ResultView {
        void showResult(String result);
    }

    private ResultView resultView;

    ////////////////////////////////////////
    public RxJavaPresenter(ResultView resultView) {
        this.resultView = resultView;
    }

    ////////////////////////////////////////
    public void test1() {
        Integer[] numbers = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        Observable.from(numbers)
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return integer > 5;
                    }
                })
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        return integer * 10;
                    }
                })
                .reduce(0, new Func2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer, Integer integer2) {
                        return integer + integer2;
                    }
                })
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        resultView.showResult(String.valueOf(integer));
                    }
                });
    }
}
