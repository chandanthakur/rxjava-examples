import org.junit.Test;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.observables.SyncOnSubscribe;
import rx.schedulers.Schedulers;

public class FlowControl {

    @Test
    public void testEntry() {
        simpleBackpressureSample2();
    }


    Observable<Integer> myRange(int from, int count) {
        return Observable.create(subscriber -> {
            int i = from;
            while (i < from + count) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(i++);
                } else {
                    return;
                }
            }
            subscriber.onCompleted();
        });
    }

    public void simpleBackpressureSample1() {
       myRange(1, 500)
                .map(Dish::new)
                .subscribe(x -> {
                    Utils.printVerbose("simpleBackpressureSample1", "Washing: " + x);
                    Utils.threadSleep(10);
                });
    }


    public void simpleBackpressureSample2() {
        Observable.range(1, 500)
                .map(Dish::new)
                .observeOn(Schedulers.io())
                .subscribe(x -> {
                    Utils.printVerbose("simpleBackpressureSample1", "Washing:begin: " + x);
                    Utils.threadSleep(10);
                    Utils.printVerbose("simpleBackpressureSample1", "Washing:end: " + x);
                }, error -> Utils.printVerbose("simpleBackpressureSample1", error.toString()));

        Utils.threadSleep(10000);
    }


    // SyncOnSubscribe
    public void syncOnSubscribeSample1() {
        Observable.OnSubscribe<Long> onSubscribe =
                SyncOnSubscribe.createStateful(
                        () -> 0L,
                        (cur, observer) -> {
                            observer.onNext(cur);
                            return cur + 1;
                        }
                );

        Observable<Long> naturals = Observable.create(onSubscribe);

        naturals.map(Dish::new)
                .observeOn(Schedulers.io())
                .subscribe(x -> {
                    Utils.printVerbose("simpleBackpressureSample1", "Washing:begin: " + x);
                    Utils.threadSleep(10);
                    Utils.printVerbose("simpleBackpressureSample1", "Washing:end: " + x);
                }, error -> Utils.printVerbose("simpleBackpressureSample1", error.toString()));

        Utils.threadSleep(10000);
    }
}
