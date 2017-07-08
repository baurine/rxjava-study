# RxJava 2.x Note

## Note 1

2017/7/9

资料：

1. [RxJava 2 浅析](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2016/0907/6604.html)
1. [RxJava 源码解读 (1.x)](http://blog.csdn.net/maplejaw_/article/details/52381395)

和 RxJava 1.x 相比，RxJava 2 的主要变化：

1. Subscriber 消失了，取而代之的是 ObservableEmitter 和新的 Observer。前者用于创建 Observable 时使用，名字更形象了，本来顶层的 Observable 就是用来发射数据的。
1. 新的 Observer，取消了 Subscriber.onStart() 回调，增加了 onSubscribe(Disposable d) 回调。
1. 增加了 Flowable 用来处理背压。

其它变化不是很大，对理解没有太大影响，具体看上面的链接文章。

RxJava 三步曲：

1. 初始化 Observable，ObservableEmitter 替代了 Subscriber，ObservableOnSubscribe 替代了 Obserable.OnSubscribe

        Observable<Integer> observable=Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onComplete();
            }
        });

1. 初始化 Observer，增加了 onSubscribe() 回调

        Observer<Integer> observer= new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d)
            }

            @Override
            public void onNext(Integer value) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        }

1. 建立订阅关系

        observable.subscribe(observer);

增加 Observer.onSubscribe(Disposable d) 带来的好处是，当 Observer 在 onNext 中接收到异常数据时，可以很方便地提前取消订阅，调用 Disposable.dispose() 方法。

    Observer<Integer> observer = new Observer<Integer>() {
        private Disposable disposable;

        @Override
        public void onSubscribe(Disposable d) {
            disposable = d;
        }

        @Override
        public void onNext(Integer value) {
            Log.d("JG", value.toString());
            // >3 时为异常数据，解除订阅
            if (value > 3) {
                disposable.dispose();
            }
        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onComplete() {
        }
    };
