import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.crypto.Data;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

public class Concurrency {

    @Test
    public void testEntry() {
        concurrencyContractViolationFix1();
    }

    public class Data {
        public Integer id;

        Data(Integer id) {
            this.id = id;
        }
    }

    Observable<Data> loadAll(Collection<Integer> ids) {
        return Observable.create(subscriber -> {
            ExecutorService pool = Executors.newFixedThreadPool(10);
            AtomicInteger countDown = new AtomicInteger(ids.size());
            //DANGER, violates Rx contract. Don't do this!
            ids.forEach(id -> pool.submit(() -> {
                final Data data = load(id);
                subscriber.onNext(data);
                if (countDown.decrementAndGet() == 0) {
                    pool.shutdownNow();
                    subscriber.onCompleted();
                }
            }));
        });
    }

    private Data load(Integer id) {
        return new Data(id);
    }

    void concurrencyContractViolation1() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        loadAll(list).subscribe(data -> {
            // this code is being called concurrently
            Utils.printVerbose("concurrencyContractViolation1:in", String.valueOf(data.id));
            Utils.threadSleep(20*(10 - data.id));
            Utils.printVerbose("concurrencyContractViolation1:out", String.valueOf(data.id));
        });

        Utils.threadSleep(5000);
    }

    void concurrencyContractViolationFix1() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        loadAll(list).serialize().subscribe(data -> {
            // this code is being called concurrently
            Utils.printVerbose("concurrencyContractViolation1:in", String.valueOf(data.id));
            Utils.threadSleep(20*(10 - data.id));
            Utils.printVerbose("concurrencyContractViolation1:out", String.valueOf(data.id));
        });

        Utils.threadSleep(5000);
    }

    //demonstrate scheduling


    // demonstrate subscribeOn, observeOn
    void schedulingCitiesAroundLatLng() {
        final ApiFactory api = new ApiFactory();
        MeetupApi meetup = api.meetup();
        double hydLat = 17.4126274;
        double hydLng = 78.2679609;
        Observable<Cities> cities = meetup.listCities(hydLat, hydLng);
        Observable<City> cityObs = cities.concatMapIterable(Cities::getResults);
        Observable<String> citiesNamesObs = cityObs.map(City::getCity);
        citiesNamesObs.
                subscribe(cityName -> {
                    Utils.printVerbose(String.valueOf(cityName));
                }, y -> {
                    Utils.printVerbose(y.toString());
                });

        Utils.printVerbose("End of main function");
        Utils.threadSleep(10000);
    }

    void temp() {
        final ApiFactory api = new ApiFactory();
        MeetupApi meetup = api.meetup();
        GeoNames geoNames = api.geoNames();

        double hydLat = 17.4126274;
        double hydLng = 78.2679609;
        Observable<Cities> cities = meetup.listCities(hydLat, hydLng);
        Observable<City> cityObs = cities.concatMapIterable(Cities::getResults);

        Observable<String> citiesNamesObs = cityObs
                .map(City::getCity);

        Observable<Long> totalPopulation = citiesNamesObs
                .flatMap(geoNames::populationOf)
                .reduce(0L, (x, y) -> x + y);

        totalPopulation.subscribe(x -> {
            Utils.printVerbose(String.valueOf(x));
        }, y -> {
            Utils.printVerbose(y.toString());
        });

        Utils.threadSleep(10000);
    }
}
