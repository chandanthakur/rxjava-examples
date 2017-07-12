import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.schedulers.Schedulers;

public class Composable {

    @Test
    public void testEntry() {
        composeFunctionsExample2();
    }

    public static Observable<Integer> populationOf(String query) {
        final ApiFactory api = new ApiFactory();
        GeoNames geoNames = api.geoNames();
        return geoNames.search(query)
                .doOnNext(X -> Utils.printVerbose("populationOf", query))
                .concatMapIterable(SearchResult::getGeonames)
                .map(Geoname::getPopulation)
                .filter(p -> p != null)
                .singleOrDefault(0)
                .onErrorReturn(th -> 0);
    }

    Observable<String> citiesAroundLatLng(double lat, double lng) {
        final ApiFactory api = new ApiFactory();
        MeetupApi meetup = api.meetup();
        return meetup.listCities(lat, lng).
                doOnNext(X -> Utils.printVerbose("citiesAroundLatLng", lat + "," + lng)).
                concatMapIterable(Cities::getResults).
                map(City::getCity);
    }

    Observable<String> cityAroundLatLng(double lat, double lng) {
        final ApiFactory api = new ApiFactory();
        MeetupApi meetup = api.meetup();
        return meetup.listCities(lat, lng).
                concatMapIterable(Cities::getResults).
                map(City::getCity).take(1).subscribeOn(Schedulers.io());
    }

    void findPopulationSample1(){
        populationOf("hyderabad").subscribe(population ->  {
            Utils.printVerbose("findPopulationSample1", String.valueOf(population));
        });
        Utils.threadSleep(5000);
    }

    void citiesAroundLatLngSample1() {
        double hydLat = 17.4126274;
        double hydLng = 78.2679609;
        citiesAroundLatLng(hydLat, hydLng).
                subscribe(cityName -> {
                    Utils.printVerbose(String.valueOf(cityName));
                }, y -> {
                    Utils.printVerbose(y.toString());
                });

        Utils.printVerbose("End of main function");
        Utils.threadSleep(10000);
    }

    void cityAroundLatLngSample1() {
        double hydLat = 17.4126274;
        double hydLng = 78.2679609;
        cityAroundLatLng(hydLat, hydLng).
                subscribe(cityName -> {
                    Utils.printVerbose(String.valueOf(cityName));
                }, y -> {
                    Utils.printVerbose(y.toString());
                });

        Utils.printVerbose("End of main function");
        Utils.threadSleep(10000);
    }

    void composeFunctionsExample1() {
        double hydLat = 17.4126274;
        double hydLng = 78.2679609;
        cityAroundLatLng(hydLat, hydLng).
                flatMap(city -> populationOf(city)).
                subscribe(population -> Utils.printVerbose("composeFunctionsExample1", String.valueOf(population)));
        Utils.printVerbose("End of main function");
        Utils.threadSleep(10000);
    }

    // Discuss map variants here with different parameters, parallelization
    void composeFunctionsExample2() {
        double hydLat = 17.4126274;
        double hydLng = 78.2679609;
        citiesAroundLatLng(hydLat, hydLng).
                flatMap(city -> populationOf(city).subscribeOn(Schedulers.io()), 1).
                subscribeOn(Schedulers.io()).
                subscribe(population -> Utils.printVerbose("composeFunctionsExample1", String.valueOf(population)));
        Utils.printVerbose("End of main function");
        Utils.threadSleep(25000);
    }

}
