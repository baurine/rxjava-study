package com.baurine.rxjavastudy.presenter;

import android.util.Log;

import com.baurine.rxjavastudy.ui.ResultView;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * Created by baurine on 2/25/17.
 */

public class RxJava2Presenter {
    private static final String TAG = "RxJava2Presenter";

    private ResultView resultView;

    ////////////////////////////////////////
    public RxJava2Presenter(ResultView resultView) {
        this.resultView = resultView;
    }

    ////////////////////////////////////////
    public void testBasic() {
        Integer[] numbers = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        Observable.fromArray(numbers)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(@NonNull Integer integer) throws Exception {
                        return integer > 5;
                    }
                })
                .map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(@NonNull Integer integer) throws Exception {
                        return integer * 10;
                    }
                })
                .flatMap(new Function<Integer, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(@NonNull Integer integer) throws Exception {
                        return Observable.just(integer);
                    }
                })
                .reduce(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(@NonNull Integer integer, @NonNull Integer integer2) throws Exception {
                        return integer + integer2;
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        Log.i(TAG, "Final result: " + integer);
                        resultView.showResult(String.valueOf(integer));
                    }
                });
    }
}
