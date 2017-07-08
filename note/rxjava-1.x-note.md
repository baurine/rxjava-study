# RxJava 1.x Note

这部分笔记的 Note 2 - Note 5 是从有道云笔记上重新整理过来的。最早的记录于 2016/2。

## Note 1

2017/7/8

先谈谈我对 RxJava 1.x 的一些理解，陆陆续续看了一两年的 ReactiveX 这种编程思想，文章看了很多，RxJava 的源码看过，也简单用过 RxSwift，因为一直没有用到实际项目中，所以只能说是初略掌握了。以下是我的一些个人理解，用词不是很精确，也不一定正确。

### Rx 与普通的流式编程的区别

首先，什么是普通的流式编程，其实这个名字是我自己想的 (来源是 Java 的 Stream API)，我也不知道该用一个什么专业名词来称呼，不过看下面的例子你就能明白我想要表达的意思。

用 JavaScript / Ruby / Swift 实现的流式编程方法：

    // JavaScript
    let r = [1,2,3,4,5,6,7,8,9].filter(i=>i>5).map(i=>i*10).reduce((a,b)=>a+b, 0)

    // Ruby
    r = [1,2,3,4,5,6,7,8,9].select {|i| i>5}.map {|i| i*10}.reduce(0) {|a,b| a+b}

    // Swift
    var r = [1,2,3,4,5,6,7,8,9].filter {$0>5}.map {$0*10}.reduce(0) {$0+$1}

比较新的现代语言，都给像数组这种可迭代的对象，赋予了 filter / map / reduce 等这些常见的操作，它们内部帮我们实现了循环，使我们只需集中在实现最关键的逻辑上。有了这些操作符以后，基本就和 for 循环 say goodbye 了。

所以，当你看到 Rx 中的 fitler / map / reduce 这些方法时，会不会有这种疑问，咦，它们和上面的 filter / map / reduce 是不是一样啊，特别是我们可以用相似的方法调用来得到相同的结果，就更加令人疑惑了。如下所示。

    Integer[] numbers = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
    Observable.from(numbers)
            .filter(integer) -> { return integer > 5; })
            .map((integer) -> { return integer * 10; })
            .reduce(0, (int1, int2) -> { return int1 + int2; })
            .subscribe((integer) -> { Log.i(TAG, "result: " + integer); });

同样得到 300 的结果。

但是，可以说，Rx 和上面的流式编程，形似而神不似，内部实现逻辑可以说是截然不同的。

举个形象但不是非常精确的例子，有 2 种做菜的方式。

第一种方式：

1. 拿到了所有做菜的材料，先全部洗干净了，剔除太小或变质的材料。(对应 filter 操作)
1. 然后把所有的材料都全部切好。(对应 map 操作)
1. 最后用这些材料一起做出菜来。(对应 reduce 操作)

这是常见的流式编程的方式。

第二种做菜的方式，以青椒炒肉为例：

1. 先取肉，洗，切，炒。(对应 肉的 filter -> map -> reduce 操作)
1. 等肉炒得差不多了，我们再取青椒，洗，切，炒，最后和肉一起出锅。(对应青椒的 filter -> map -> reduce 操作)

这是 Rx 的方式。

(你可以在这两种方式的实现代码中输出 log 来验证)。

可见，常见的流式编程法，每一步都是对数据进行整体操作的，操作后得到另外一堆数据，然后整体作为下一步的输入。

而 Rx，是在源头，将数据源中的每一个数据，单独发射出去，使之每一个数据都单独地走一遍整个流程。

因而对应到它们的内部实现上：

1. 流式编程法，每一个单独的步骤，在内部都重新对整体数据进行一次循环，因此它要做很多次小循环，但整体的流程只走一遍。
1. Rx，只在数据最源头，对整体数据进行一次循环，取出每一个单独的数据，让它去走一遍整体流程，因此，它只做一次循环，但整个的流程会走很多遍。

对于 Rx 来说，如果数据源不是固定的，那么它可以监听这个数据来源，当有数据到来时，它就可以把它发射出去把整个流程走一遍。这就是我用来理解 Rx 的观察者模式和响应式编程的方法。

Rx 比常见流式编程的抽象程度更高，它不光抽象了对数据的操作，还抽象了线程切换操作。

### Observabl(Rx) / Promise / Array / Builder 的比较

这里的 Array 指的是上面所说的支持 filter / map / reduce 操作的 Array。

以上这几种方式的共同点是它们可以实现链式调用，原因是它们的方法将返回同类型的实例引用，但返回的这个实例引用，有可能是一个全新的实例，也有可能是原来的实例。

1. 返回值的区别

   - Observabl / Promise 返回新的实例，Observable 返回新的 Observable 对象，Promise 返回新的 Promise 对象
   - Array, filter / map 返回新的数组，但 reduce 返回非数组，并且到这里后链式调用就没法继续了
   - Builder，返回自身引用

1. 是否延迟执行

   - Promise 对象，当它被 new 出来后，其中的方法体是立即执行的，不管后面有没有用 `.then()` 加上回调，执行的结果被存储在对象之中，如果后面再用 `.then()` 加上回调，之前的结果会马上传递给新的回调。
   - Rx 的 Observable 对象，当它被 new 出来后，其中的方法体，只有 `.subscribe()` 方法被执行的那一刻才会执行，否则，只要没有调用 `.subscribe()`，其中的变换就永远不会执行，这和 Promise 是完全不一样的。

### 对 flatMap 的理解

我觉得可以从两个角度来理解。

角度一：

我们上面说到，Rx 的 Observable，每个变换的返回值都应该是一个新的 Observable 对象。以 map 变换为例：

    Integer[] numbers = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
    Observable.from(numbers)
            .map((integer) -> { return integer * 10; })

此例中，map 变换中的方法体，返回值是 Integer 类型，并不是我们想要的 Observable，因此你可以理解成 map 内部要把这个返回值重新包装成 Observable (虽然实际并不完全是这样)，此例中，最终的返回值会包装成 `Observable<Integer>`。

但是如果这个方法，它的返回值就是 Observable 呢，那么是不是就可以省去这层封装了呢，我想答案是的。但这时候，就不能再用 map 变换了，必须用 flatMap 变换，flatMap 与 map 相比，其实内部就是少了一层 Observable 的再次封装，就是相当于你替 map 手动干了这个活。

另一个意思就是，filter 抽象的是 T -> boolean 的操作，map 抽象的是 T -> R 的操作，而 flatMap 抽象的是 T -> Observable 的操作。只要是返回值是 Observable 的方法，都可以用 flatMap 变换。

用 flatMap 实现上例中和 map 相同的效果：

    Integer[] numbers = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
    Observable.from(numbers)
            .flatMap((integer) -> { return Observable.just(integer * 10); })

(Promise 有着相同的实现原理，在 `Promise.then(func)` 方法中注册的方法，如果返回值是非 Promise，那么内部把返回值包装成 Promise，如果返回值已经是 Promise，那么就直接返回。)

T -> Observable 更常见的一个地方是，用 Retrofit 定义的返回值为 Observable 的 API 请求，比如

    @GET("/users/{id}")
    Observable<User> getUserInfo(@Path("id") int id);

那么我们就应该用 flatMap 去取得这个 api 请求的结果，如下所示：

    Integers[] userIDs = new Integer[]{100, 120};
    Observable.from(userIDs)
            .flatMap((userID) -> { return apiService.getUserInfo(userID); })
            .map((userInfo) -> {...})

角度二：

虽然上例中，我们用 flatMap 替代 map 实现了相同的效果，但实际这是很愚蠢的做法，没有把 flatMap 用在真正需要的地方。这些抽象的目的就是让我们集中在实现最核心逻辑上，map 帮你做了把返回值包装成 Observable 的操作，你却还非得手动去做这个与核心逻辑无关的操作。

那么什么时候才是使用 flatMap 的正确时机呢，就是不得不用，其它变换都实现不了的时候。从字面上看，flat，拍扁，怎么可以算是拍扁，把对 array 的操作转换成对其中每一个元素的操作，就是一种拍扁。

上面的代码中，第一步就是通过 `Observable.from(numbers)`，把 array 转换成 Observable，这个 Observable 把 array 中的每一个元素都单独发射出去，进行后面的各种变换。array -> Observable，这就是 flatMap 最典型，最经典的用法。

`Observable.from(array)`，我们把一个数组转换成对其中每一个元素进行操作，如果这其中的每一个元素，又有自己的一个 array 或 list，我们想对这之中的每一个元素进行操作时，那么毫无疑问，这就是 flatMap 的用武之地了。

举个例子，我们有一些作者，这些作者每个人都出版了一些书，我们想看一下中国作者们写的书中，属于计算机类的那些书的评分。来看一下伪代码：

    Integer[] authorIDs = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
    Observable.from(authorIDs)                                   // 1
              .map(id -> apiService.getAuthorInfo(id))           // 2
              .filter(author -> author.country.equals("China"))  // 3
              .flatMap(author -> Observable.from(author.books))  // 4*
              .filter(book -> book.type.equals("computer"))      // 5
              .map(book -> book.rating)                          // 6

1. 把作者 ID 列表转换成对每一个作者 ID 进行操作的 Observable
1. 使用 map 变换，通过 id 得到单个作者的信息
1. 使用 filter 变换，过滤作者，只有中国的作者才可以继续下面的流程
1. 这时候我们拿到了作者信息，每个作者都有一个 bookList，我们没有办法继续对一个 list 进行操作，我们要的是把这个 list 取出来，对其中的每一本书继续进行操作，很显然，这是一个将 list 拍扁的过程，我们可以用 Observable.from(bookList) 来实现，而变换操作符当然是 flatMap。把第 4 步和第 1 步联系着看，可以加深你的理解。
1. 我们取到了单本 book，接着就可以过滤出计算机类的书，只有计算机类的书才可以继续后面的流程
1. 取得 book 的评分

而假设如果每一本书都有多个出版商，你想对这其中的每一个出版商进行一些操作，比如知道它们的名字，哈哈，那你知道该怎么办了，继续用 flatMap 啊。

    ...
    .flatMap(author -> Observable.from(author.books))
    .flatMap(book -> Observable.from(book.publishers))
    .map(publisher -> publisher.name)

总结一下就是，只要是 T -> Observable 的变换，就应该用 flatMap。

---

## Note 2

学习资料：

1. [Awesome-RxJava](https://github.com/lzyzsd/Awesome-RxJava)
1. [NotRxJava 懒人专用指南](http://www.devtf.cn/?p=323)

[NotRxJava 懒人专用指南](http://www.devtf.cn/?p=323) 这篇文章写得很好，解释了 RxJava 内部原理，值得一看再看。

关键的几步：

1. 将所有回调抽象成 `Callback<T>`

        interface Callback<T> {
            onResult(T t);
            onError(Exception e);
        }

2. 进一步抽象出 AsnycJob 对象

        abstract class AsyncJob<T> {
            start(Callback<T> callback);
        }

   任何异步操作需要携带所需的常规参数和一个回调实例对象。

   一个函数返回的是上面这样的一个 `AsyncJob<T>` 对象。

   实际去执行的操作是调用这个返回的对象的 `start()` 方法。

        public AsyncJob<List<Cat>> queryCats(String query) {
            return new AsyncJob<List<Cat>>() {
                @Override
                public void start(Callback<List<Cat>> catsCallback) {
                    api.queryCats(query, new Api.CatsQueryCallback() {
                        @Override
                        public void onCatListReceived(List<Cat> cats) {
                            catsCallback.onResult(cats);
                        }

                        @Override
                        public void onQueryFailed(Exception e) {
                            catsCallback.onError(e);
                        }
                    });
                }
            };
        }

   `queryCats(String query)` 这个函数，当执行 `queryCats("hashiqi")` 时，实际这个函数并没有真正去查询，而是返回了另一个对象，这个对象才会真正去查询。所以要真正去查询的话应该调用：

        queryCats("hashiqi").start(new Callback<List<Cat>> {...});

   我的理解：

   - 这就是所谓的 lazy load 的一种吧。这个函数本身自己不执行相应的操作，返回一个可以执行真正操作的另外一个对象。
   - 又有点像 js 中的闭包，一个函数的结果是返回另一个函数。调用返回的结果才是去执行真正的操作。对吧。

3. 简单映射，map 函数，T -> R

        public interface Func<T, R> {
            R call(T t);
        }

   扩展以后的 `AsyncJob<T>` 精妙至极，理解了这段代码，就能理解 RxJava 的一半核心了。

        public abstract class AsyncJob<T> {
            public abstract void start(Callback<T> callback);

            public <R> AsyncJob<R> map(Func<T, R> func){
                final AsyncJob<T> source = this;
                return new AsyncJob<R>() {
                    @Override
                    public void start(Callback<R> callback) {
                        source.start(new Callback<T>() {
                            @Override
                            public void onResult(T result) {
                                R mapped = func.call(result);
                                callback.onResult(mapped);
                            }

                            @Override
                            public void onError(Exception e) {
                                callback.onError(e);
                            }
                        });
                    }
                };
            }
        }

   看到没，在这个 map 实现中，并没有真正地执行什么方法，只是简单地返回了一个新的 AsyncJob 实例，我们称原来的 AsyncJob 实例为 A，新返回的为 B，B 中含有 A 的引用 (即 source)，而 A 可能还含有更顶层的 AsyncJob 的引用 (我们称之为 Z 吧，并假设它为最顶层的 AsyncJob)，最终形成了一条 AsyncJob 引用链条。

   假设有这么一段代码：

        AsyncJob.create(...).map(mapFunc1).map(mapFunc2).start(callback);

   那么：

        AsyncJob.create(...) -> Z
        Z.map(mapFunc1) -> A  // A 含有 Z 的引用，同时保存了 mapFunc1
        A.map(mapFunc2) -> B  // B 含有 A 的引用，同时保存了 mapFunc2

   只有当 B.start() 跑起来后，整个链条才真正运转起来，B.start() 中会调用 A.start()，而 A.start() 又会调用 Z.start()，从而一直从底层一直执行到顶层。

        B.start() --> A.start() --> Z.start()

   当执行到最顶层的 AsyncJob Z 的 start() 时，Z 拥有数据源，它发射数据，它把数据通过参数中的 callback.onResutl() 通知下去。而 Z.start() 参数中的 callback 来自何处呢，它是在 A.start() 中被 new 出来的，即 Z.start(callbackFromA)，那么在 callbackFromA.onResult() 回调中，它会执行 A.mapFunc1，然后把结果传递给 A.start() 参数中的 callbackFromB，在 callbackFromB.onResult() 中，它执行 B.mapFunc2，然后将结果传递给 B.start() 参数中的 callbackFromUser。最终用户得到结果，整个流程是这样的：

        B.start(callbackFromUser)   --> A.start(callbackFromB)   --> Z.start(callbackFromA)
                                                                            |
        callbackFromUser.onResult() <-- callbackFromB.onResult() <-- callbackFromA.onReslut()

   这是一个自顶向下，再自底向上，再由上到下的调用过程。

   - 第一次自顶向下，分别构造出 Z --> A --> B AsyncJob 对象
   - 自底向上，B.start() --> A.start() --> Z.start()
   - 再由上到下，callbackA --> callbackB --> callbackUser

   每一个 AsyncJob 都是一个封装了变换操作的个体，并保存了顶层的 AsyncJob 引用，并对外只暴露 start() 方法。

   或者换另一种说法，每一种变换，以前的编程方式中，它们都是独立的函数，现在，这些函数每一个都被封装进了 AsyncJob 类中。

4. flatMap，略 

最后总结：

- `AsyncJob<T>` 就是实际上的 Observable，它不仅可以只分发一个单一的结果也可以是一个序列（可以为空）。
- `Callback<T>` 就是 Observer/Subscriber (RxJava 中 Subscriber 和 Observer 可以理解成等同)，除了 Callback 少了 `onNext(T t)` 方法。Observer 中在 `onError(Throwable t)` 方法被调用后，会继而调用 `onCompleted()`，然后 Observer 会包装好并发送出事件流（因为它能发送一个序列）。
- `abstract void start(Callback<T> callback)` 对应 `Subscription subscribe(final Observer<? super T> observer)`，这个方法也返回 Subscription，在不需要它时你可以决定取消接收事件流。

---

## Note 3

学习资料：

1. [深入浅出 RxJava](http://blog.csdn.net/lzyzsd/article/details/41833541)

### 一、基础篇

变换，操作符，map 操作符

    Observable.just("Hello, world!")
        .map(s -> s.hashCode())
        .map(i -> Integer.toString(i))
        .subscribe(s -> System.out.println(s));

1. Observable 和 Subscriber 可以做任何事情
   - Observable 可以是一个数据库查询，Subscriber 用来显示查询结果；
   - Observable 可以是屏幕上的点击事件，Subscriber 用来响应点击事件；
   - Observable 可以是一个网络请求，Subscriber 用来显示请求结果。

2. Observable 和 Subscriber 是独立于中间的变换过程的

   在 Observable 和 Subscriber 中间可以增减任何数量的 map。整个系统是高度可组合的，操作数据是一个很简单的过程。

### 二、操作符

操作符进阶，flatMap，filter，take，doOnNext

    query("Hello, world!")
        .flatMap(urls -> Observable.from(urls))
        .flatMap(url -> getTitles(url))
        .filter(title -> title != null)
        .take(5)
        .doOnNext(title -> saveTitle(title))
        .subscribe(title -> System.out.println(title));

将一系列的操作符链接起来就可以完成复杂的逻辑。代码被分解成一系列可以组合的片段。这就是响应式函数编程的魅力。用的越多，就会越多的改变你的编程思维。

### 三、响应式的好处

**错误处理**

使用 RxJava，Observable 对象根本不需要知道如何处理错误！操作符也不需要处理错误状态，一旦发生错误，就会跳过当前和后续的操作符。所有的错误处理都交给订阅者来做。

**调度**

使用RxJava，你可以使用 `subscribeOn()` 指定观察者代码运行的线程，使用 `observerOn()` 指定订阅者运行的线程：

    myObservableServices.retrieveImage(url)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(bitmap -> myImageView.setImageBitmap(bitmap));

**订阅**

当调用 `Observable.subscribe()`，会返回一个 Subscription 对象。这个对象代表了被观察者和订阅者之间的联系。

### 四、在Android中使用响应式编程

AndroidSchedulers 提供了针对 Android 的线程系统的调度器：

    retrofitService.getImage(url)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(bitmap -> myImageView.setImageBitmap(bitmap));

Retrofit 内置了对 RxJava 的支持：

    @GET ("/users/{id}/photo")
    void getUserPhoto(@Path("id") int id, Callback<Photo> cb);

    @GET("/users/{id}/photo")
    Observable<Photo> getUserPhoto(@Path("id") int id);

---

## Note 4

参考：

1. [一张图读懂 RxJava 中的线程控制](https://juejin.im/entry/575d96e16be3ff006a48941a)

2016/6/14，新的理解：

    Observable.create(
            new Observable.OnSubscribe<String>() {
                @Override
                public void call(Subscriber<? super String> subscriber) {
                    String test = "Test";
                    subscriber.onNext(test);
                    subscriber.onCompleted();
                }
            })
            .map(str -> str.toUpperCase())
            .doOnNext(System.out::println)
            .doOnSubscribe(()->System.out.print("haha"))
            .doOnError(e->e.printStackTrace())
            .subscribe(System.out::println,
                    e -> e.printStackTrace(),
                    () -> System.out.println("Done!"));

    Observable.create(...)
            .lift1(...)
            .subscribeOn(scheduler1)
            .lift2(...)
            .observerOn(scheduler2)
            .lift3(...)
            .subscribeOn(scheduler3)
            .lift4(...)
            .observerOn(scheduler4)
            .doOnSubscribe(...)
            .subscribeOn(scheduler5)
            .observerOn(scheduler6)
            .subscribe(...);

链式执行有两种，一种是像 Builder 模式，每次返回自身引用，一种是像 Promise/ReativeX，每次返回一个全新的对象，这个对象可能包含上一个对象的引用 (ReactiveX 包括而 Promise 不包括，Promise 是完全独立的)。

1. 把 map/flatMap/fileter 称为 lift 变换

1. 首先确认，`subscribe(subscriber)`，`subscribe()` 的运行 和 `subscriber.onNext() / onComplete() / onError()` 的运行并不一定在同一个线程上。`subscribe()` 相当于注册一个回调，而 `subscriber.onNext()` 则是执行回调，这样是不是好理解多了。

1. 进一步确认，`create()`，`map()`，`doOnNext()`，`subscribe()` 这些函数运行时所在的线程，与其括号中的函数运行的线程没有关系，括号中的函数相关于回调函数，`create()` `map()` `doOnNext()` 相当于注册回调函数，两者的执行线程不一定相同。

1. `create()` `map()` ... `subscribe()` 这些函数运行在相同的线程上，即这段代码刚开始执行所在的线程。而它们括号中的函数，运行的线程由 `observerOn(thread)` 和 `subscribeOn(thread)` 这两个方法来指定。

1. `create()` `map()` ... 除了最后的 `subscribe()`，都只是在简单的创建出新的 Observable 对象，并在这个对象中保存上下文，并未执行任何实质性的操作，直到 `subscribe()` 的执行，所以这是一种 lazy run。

1. 每一步的 lift 都会返回一个全新的 Observable 对象，这个新的 Observable 对象里，又会生成全新的 OnSubscribe 对象和 Operator 对象，这两个对象至关重要。也可以说主要是这两个对象起作用。

1. 新的 Operator 对象会保存 lift 变换函数，比如 `map(a->b)` 中的 `a->b` 函数，称为 transformer，同时它自身提供了一个 call 的变换函数，负责把本层的 subscriber 变换成上一层 Observable 所需要的 subscriber。call 函数会返回一个全新的 subscriber (重新实现了 onNext，onCompleted，onError) 供上层 Observable 使用，这个全新的 subscriber 在 onNext 中，会首先执行 transformer 变换函数，然后把得到的结果传递给本层的 subscriber.onNext(result) 执行。

   可以说，新的 subscriber 包含了下一级的 operator 对象的引用和下一级的 subscriber 的引用。

1. 每一层的 Observable 的触发在 `OnSubscribe.call(subscriber)` 发生，在这个 `call()` 执行体中，首先调用 operator 负责把本层的 subscriber 变换成上一层 Observable 所需要 (确切的说，是上一层 Observable 的 OnSubscribe 的 call 函数所需要的 subscriber)，然后调用上一层 Observable 的 OnSubscribe 的 call 函数。

   这说明，每一层 (除了第一层) 的 Observable 都包含了上一层 Observabel 的 OnSubscribe 引用。

1. 就是这样，每一层的 OnSubscribe 都会调用上一层的 OnSubscribe，层层调用，一直调用到第一层，这是反方向的执行，到达第一层后，第一层的 OnSubscribe.call 中会开始执行 subscriber.onNext()，每一层的 subscriber 都包含了下一层的 subscriber 引用，确切地说是，每一层的 subscriber.onNext/onComplete/onError 都会调用下一层的 subscriber。在 onNext 中会先执行保存的 transformer，再执行下一级 subscriber.onNext()，所以说，包裹了这么多，这一行代码才是核心：

        o.onNext(transformer.call(t));

   这样，层层调用，再从顶层执行回到底层。

1. 只有在执行了 subscribe() 后，所有的回调函数，transformer 们才真正工作起来。

1. 我对 flatMap 的理解，其实不需要太曲解它，只需要理解它的变换函数的返回值应该就直接是个 Observable 就行。只要变换函数返回值是 Observable，那么就应该用 flatMap 变换。

   在 Promise 的回调中，如果返回一个非 Promise 值，那么这个值会再被封装成一个 Promise 返回，而如果返回值已经是 Promise，那么就不用再封装，下一级直接使用这个 Promise。

   flatMap 应该也是差不多的道理，虽然里面的实现可能大不相同。

1. 所有的 onNext() 执行完了才会执行一次 onCompleted()。

继续看 subscribeOn() 和 observeOn() 的线程变换原理

1. 在 subscribeOn() 中，返回一个新的 Observable，新的 Observable 中有新的 OnSubscribe，新的 OnSubscribe.call() 函数中，会新建线程，并在这个新的线程中，产生新的 subscriber, 并调用上一层 OnSubscribe.call(subscriber)。

   但这个过程是由底层到顶层，只是在创建对象和保存上下文，并没有真正的执行变换函数。最终到达顶层的 OnSubscriber.call() 时，这个 call 将执行在离它最近的下游所声明的 subscribeOn() 线程上，并在这个线程上开始执行 subscriber.onNext() ...

1. observeOn()，虽然没完全看懂，好吧，实际是基本没懂，但最核心的转换理解了，这个转换是发生在 subscriber 上，因此它影响的是从顶层从底层执行的过程。因此，observeOn() 会影响下游的线程，而 subscribeOn() 是影响上游的线程。

   总结：subcribeOn() 的线程切换发生在 OnSubscribe.call() 上，影响上游，而 observeOn() 的线程切换发生在 Operator.call() 上，影响下游。

1. subscriber.onStart()，这个有点特殊，它在调用上层的 OnSubscribe.call() 之前执行，因此它和 onNext() onCompleted() onError() 并不一定在一个线程上。

   实际上它和 `.subscribe()` 的执行在一个线程上。(... 这里，有点同步回调的意思了，然而 Promise 里是不建议的)，(... 本质上 Rx 的思想就是同步回调吧，如果没有线程切换的话，一切都是同步回调)。

   而且，这里 subscriber.onStart() 的执行，虽然定义在最后，但却是最先开始执行的！在事件发送之前执行。got it~!

终于终于算是领悟了吧~！

---

## Note 5

参考：

1. [用 RxJava 实现事件总线 (Event Bus)](http://www.jianshu.com/p/ca090f6e2fe2/)

RxBus 代码解读

    public class RxBus {
        private final Subject bus;

        public RxBus() {
            this.bus = new SerializedSubject<>(PublishSubject.create());
        }

        private static class RxBusHolder {
            public static final RxBus rxbus = new RxBus();
        }

        public static RxBus getDefault() {
            return RxBusHolder.rxbus;
        }

        public void post(Object o) {
            bus.onNext(o);
        }

        public Observable<Object> toObservable() {
            return bus;
        }
    }

Subject 同时充当了 Observer 和 Observable 的角色，Subject 是非线程安全的，要避免该问题，需要将 Subject 转换为一个 SerializedSubject，上述 RxBus 类中把线程非安全的 PublishSubject 包装成线程安全的 Subject。

使用：

    // 注册
    rxSubscription = RxBus.getDefault().toObserverable().subscribe(obj -> xxx);

    // 发送
    RxBus.getDefault().post(new UserEvent(1, "yoyo"));

我的理解:

我之前很难理解 RxBus 的工作原理，是因为，我以为 subscribe() 之后，整个链条就马上工作起来，从底层到顶层，顶层开始发射事件，再从顶层执行到底层，一次性完成。中间没有间断。而 RxBus 明显不是这样，分为两个阶段，一个阶段是注册，一个阶段是由外界触发事件的发射。

所以关键点在于理解，为何 subscribe() 后，顶层是怎么处理的，为什么在顶层没有发射事件。

经过看代码，终于理解了，在 subscribe() 后，从底层传递到顶层时，顶层此时所做的工作是，仅仅把 subscriber 保存到一个集合里，并没有直接调用 subscriber 的 onNext() / onCompleted / onError 方法。

当外界向 bus post 事件时，此时，顶层的 observable 才会开始遍历这些 subscriber，依次调用 onNext() 等方法。cool~!

顶层的 observable 为何能实现将 subscriber 保存到一个集合里，而不是直接调用 subscriber 的方法呢，关键在这里：

    public SerializedSubject(final Subject<T, R> actual) {
        super(new OnSubscribe<R>() {
            @Override
            public void call(Subscriber<? super R> child) {
                actual.unsafeSubscribe(child);
            }
        });
        this.actual = actual;
        this.observer = new SerializedObserver<T>(actual);
    }

这里把实际的工作交给了 actual observable 处理，从这里来看，actual observable 实际是 PublishSubject：

    public RxBus() {
        this.bus = new SerializedSubject<>(PublishSubject.create());
    }

PublishSubject 的 OnSubscribe.call() 方法是这样的：

    @Override
    public void call(final Subscriber<? super T> child) {
        SubjectObserver<T> bo = new SubjectObserver<T>(child);
        addUnsubscriber(child, bo);
        onStart.call(bo);
        if (!child.isUnsubscribed()) {
            if (add(bo) && child.isUnsubscribed()) {
                remove(bo);
            }
        }
    }

实际工作都是由 PublishSubject 来完成的，SerializedSubject 只是一个管理类。那为什么还要用 SerializedSubject 来包一层呢，看下面的代码注释：

    // https://github.com/kaushikgopal/RxJava-Android-Samples/blob/master/app/src/main/java/com/morihacky/android/rxjava/rxbus/RxBus.java

    // private final PublishSubject<Object> _bus = PublishSubject.create();

    // If multiple threads are going to emit events to this
    // then it must be made thread-safe like this instead
    private final Subject<Object, Object> _bus = new SerializedSubject<>(PublishSubject.create());

SerializedSubject 纯粹是为了线程安全考虑的。

所以如果不考虑线程安全，可以直接用 PublishSubject，看 PublishSubject.onNext() 是怎么实现的：

    // PublishSubject
    @Override
    public void onNext(T v) {
        for (SubjectObserver<T> bo : state.observers()) {
            bo.onNext(v);
        }
    }

RxBus 在接后到事件后，调用 PublishSubject.onNext()，遍历所有的 observers，通知它们。

我叉，一下子就拨云见日了。

---

## Note 6

参考：

1. [给 Android 开发者的 RxJava 详解](http://gank.io/post/560e15be2dca930e00da1083#toc_2)

> 在前面讲 Subscriber 的时候，提到过 Subscriber 的 onStart() 可以用作流程开始前的初始化。然而 onStart() 由于在 subscribe() 发生时就被调用了，因此不能指定线程，而是只能执行在 subscribe() 被调用时的线程。这就导致如果 onStart() 中含有对线程有要求的代码（例如在界面上显示一个 ProgressBar，这必须在主线程执行），将会有线程非法的风险，因为有时你无法预测 subscribe() 将会在什么线程执行。

> 而与 Subscriber.onStart() 相对应的，有一个方法 Observable.doOnSubscribe() 。它和 Subscriber.onStart() 同样是在 subscribe() 调用后而且在事件发送前执行，但区别在于它可以指定线程。默认情况下，doOnSubscribe() 执行在 subscribe() 发生的线程；而如果在 doOnSubscribe() 之后有 subscribeOn() 的话，它将执行在离它最近的 subscribeOn() 所指定的线程。

明白了，如果你对 subscriber.onStart() 没有线程要求，那就直接用 subscriber.onStart() 执行你想要的逻辑，否则就用 doOnSubscribe() 配合 subscribeOn() 来实现你的逻辑。

剩下的疑问：doOnNext()，doOnError() 这些方法是干什么用的？这些方法中的参数方法是执行在哪个线程上？

[RxJava 里 doOnNext 的使用和线程处理](http://blog.csdn.net/wangkai0681080/article/details/50772721)

明白了，doOnNext() 和 doOnError() 指定的方法，所在线程，由 observerOn() 指定，这和 doOnSubscribe() 是不一样的，因为本质上 doOnNext 和 doOnError 是相当于 subscriber.onNext 和 subscriber.onError 的。

只不过 doOnNext 多用来执行带有副作用的操作，而且不应该改变数据流。

主要作用：

- 使用 doOnNext() 来调试，打 log
- 在 flatMap() 里使用 doOnError() 作为错误处理 (??)
- 使用 doOnNext() 去保存 / 缓存网络结果 (可以起一个新的线程去做耗时操作)
