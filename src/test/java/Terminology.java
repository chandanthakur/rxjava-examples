import org.junit.Test;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public class Terminology {

    @Test
    public void testEntry() {
        observableLocalIntervalUnsubscribe();
    }

    public void observableSubscriberSimple1(){
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>(){
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello World! 1");
                subscriber.onCompleted();
            }
        });

        Subscriber subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String out) {
                System.out.println(out);
            }
        };

        observable.subscribe(subscriber);
        Utils.printVerbose("Main code");
    }

    // compressed form
    public void observableSubscriberSimple2(){
        Observable.create(subscriber -> {
            subscriber.onNext("Hello World!");
            subscriber.onCompleted();
        }).subscribe(hello -> System.out.println(hello));
    }

    // Useful shortcuts for create observer
    public void observableSubscriberSimple3(){
        Observable.just("Hello World").
                subscribe(hello -> Utils.printVerbose("observableSubscriberSimple3", hello));
    }

    // Useful shortcuts for create observer
    public void observableSubscriberSimple4(){
        Observable.just("Hello", "Welcome", "to", "the", "world", "of", "RxJava").
                subscribe(word -> Utils.printVerbose("observableSubscriberSimple4", word));
    }

    // Useful shortcuts for create observer
    public void observableSubscriberSimple5(){
        String [] words = new String[] { "Hello", "Welcome", "to", "the", "world", "of", "RxJava" };
        Observable.from(words).
                subscribe(word -> Utils.printVerbose("observableSubscriberSimple5", word));
    }

    // Simple concept
    public void observableMultipleSubscribers() {
        String [] words = new String[] { "Hello", "Welcome", "to", "the", "world", "of", "RxJava" };
        Observable<String> observable = Observable.from(words);
        observable.subscribe(word -> Utils.printVerbose("observableMultipleSubscribers - 1", word));
        observable.subscribe(word -> Utils.printVerbose("observableMultipleSubscribers - 2", word));
    }

    // Default execution
    public void observableDefaultExecution() {
        String [] words = new String[] { "Hello", "Welcome", "to", "the", "world", "of", "RxJava" };
        Observable<String> observable = Observable.from(words);
        observable.subscribe(word -> Utils.printVerbose("observableDefaultExecution", word));
        Utils.printVerbose("Main thread exit");
    }

    // Default execution
    public void observableIntervalUnsubscribe() {
        Observable<String> observable = Observable.interval(50, TimeUnit.MILLISECONDS).map(val -> String.valueOf(val));
        Subscription subscription = observable.subscribe(word -> Utils.printVerbose("observableIntervalUnsubscribe", word));
        Utils.printVerbose("Main thread end of code");
        Utils.threadSleep(4000);
        Utils.printVerbose("Main thread, un-subscribe");
        subscription.unsubscribe(); // unsubscribe need to be implemented
        Utils.threadSleep(2000);
    }

    private Observable<Long> localInterval(Long interval) {
        Observable<Long> observable = Observable.create(new Observable.OnSubscribe<Long>(){
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                Thread thread = new Thread(() -> {
                    try {
                        Long count = 0L;
                        while (true) {
                            if(subscriber.isUnsubscribed()) {
                                break;
                            }

                            subscriber.onNext(count);
                            Thread.sleep(interval);
                            count = count + interval;
                        }
                    } catch (Exception ex) {
                        subscriber.onError(ex);
                    }
                });

                thread.start();
                subscriber.add(Subscriptions.create(thread::interrupt));
            }
        });

        return observable;
    }

    // Default execution
    public void observableLocalIntervalUnsubscribe() {
        Observable<String> observable = localInterval(50L).map(val -> String.valueOf(val));
        Subscription subscription = observable.subscribe(word -> Utils.printVerbose("observableIntervalUnsubscribe", word), exception -> Utils.printVerbose(exception.getMessage()));
        Utils.printVerbose("Main thread end of code");
        Utils.threadSleep(4000);
        Utils.printVerbose("Main thread, un-subscribe");
        subscription.unsubscribe(); // unsubscribe need to be implemented
        Utils.threadSleep(2000);
    }
}
