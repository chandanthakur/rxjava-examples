import org.junit.Test;
import org.slf4j.helpers.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class Scheduling {

    @Test
    public void testEntry() {
        explainSubscribeOnConcept();
    }

    private static Observable<String> heavyWork(Long units) {
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>(){
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    Long count = 0L;
                    while (count < units) {
                        Utils.printVerbose("heavyWork", String.valueOf(count));
                        subscriber.onNext(String.valueOf(count));
                        Thread.sleep(50);
                        count = count + 1;
                    }
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }
        });

        return observable;
    }


    // explain concepts of subscribeOn and observeOn, scheduling
    // Explain thread flow among all the filters
    private static void explainSubscribeOnConcept() {
        heavyWork(10L).
                map(x -> {
                    Utils.printVerbose("explainSubscribeOnConcept:MAP", String.valueOf(x));
                    return "map_" + x;
                }).observeOn(Schedulers.io()).
                doOnNext( x -> {
                    Utils.printVerbose("explainSubscribeOnConcept:doOnNext", String.valueOf(x));
                }).subscribeOn(Schedulers.io()).
                subscribe(data -> {
                    // this code is being called concurrently
                    Utils.printVerbose("explainSubscribeOnConcept:start", String.valueOf(data));
                    Utils.threadSleep(20* ThreadLocalRandom.current().nextInt(1, 5));
                    Utils.printVerbose("explainSubscribeOnConcept:end", String.valueOf(data));
                });

        Utils.printVerbose("Main code exit");
        Utils.threadSleep(5000);
    }

}
