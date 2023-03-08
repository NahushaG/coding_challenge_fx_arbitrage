package com.estar.customcode.processors;

import com.estar.arbitrage.ArbitragePrinter;
import com.estar.arbitrage.ArbitrageProcessor;
import com.estar.arbitrage.ResultFormatter;
import com.estar.customcode.algo.*;
import com.estar.orderbook.model.CurrencyPair;
import com.estar.orderbook.model.OrderbookListener;
import com.estar.orderbook.model.Price;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author Nahusha Ganiga
 *
 * <p>
 * Implementation of the listener class, only reason to create a new class and not implement inline
 * is just to have tidy implementation. The class runs two async task 1. updates the order book
 * 2. Calls the bellman ford algo to find the negative path and then calculate the negative path weight
 * to evaluate the arbitrage factor
 */
public class ManageOrderBook_old_code implements OrderbookListener {

    private final Map<CurrencyPair, OrderBook> currencyPairOrderBookMap;
    private final AlgoRunner algoRunner;


    private Map<CurrencyPair, BigDecimal[]> bestValueCurrencyMatrix = new HashMap<>();

    private ResultFormatter resultFormatter;

    public ManageOrderBook_old_code() {
        currencyPairOrderBookMap = buildOrderBookMapForAvailableCurrencyPair();
        AdjacentVertexMapBuilder adjacentVertexMapBuilder = new AdjacentVertexMapBuilder();
        CurrencyPairGraphBuilder currencyPairGraphBuilder = new CurrencyPairGraphBuilder(adjacentVertexMapBuilder);
        EdgeRelaxer edgeRelaxer = new EdgeRelaxer();
        ArbitrageFactorFinder arbitrageFactorFinder = new ArbitrageFactorFinder();
        Map<String, CurrencyPair> currencyStringCurrencyPairMap = UtilClass.getCurrencyPair();
        ArbitragePathFinder arbitragePathFinder = new ArbitragePathFinder(edgeRelaxer, arbitrageFactorFinder, currencyStringCurrencyPairMap);
        ArbitrageProcessor arbitrageProcessor = new ArbitragePrinter();
        this.algoRunner = new AlgoRunner(currencyPairGraphBuilder, arbitragePathFinder);
        this.resultFormatter = new ResultFormatter(currencyPairOrderBookMap, arbitrageProcessor);

    }

    /**
     * listens for update order action, runs two async task
     *  1. Updates the order book based on the action (ADD, MODIFY, INSERT). It also evaluates best buy
     *  2. Once the update task is done then it detect for Arbitrage cycle and then compute the arbitrage factor
     *     and prints instrument and factor.
     * The check will done only if there are more than 2 currency pair
     * @param action
     * @param price
     */
    @Override
    public void handlePriceUpdate(Action action, Price price) {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CompletableFuture<Void> manageOrderBook = CompletableFuture.runAsync(() -> {  // Aysnc task
            CurrencyPair currencyPair = price.instrument();
            OrderBook currencyOrderBook = currencyPairOrderBookMap.get(currencyPair);
            currencyOrderBook.manageOrder(action, price);
        }, executorService).thenRun(() -> {                                           //Since we have the object via map we only used thenRun()
            CurrencyPair currencyPair = price.instrument();
            OrderBook currencyOrderBook = currencyPairOrderBookMap.get(currencyPair);
            BigDecimal values[] = new BigDecimal[2];
            values[0] = currencyOrderBook.getBestBuy().price();
            values[1] = currencyOrderBook.getBestBuy().price();
            bestValueCurrencyMatrix.put(currencyPair, values);
            if (currencyPairOrderBookMap.size() > 2) {
                trackArbitrageOpportunity();  //Arbitrage check is called only if 3 or more currency pair
            }
        });

        executorService.shutdown();
    }

    /**
     * Map to hold all the order book, the concurrent hashmap will block only that order which needs to be
     * updated
     *
     * @return
     */
    private Map<CurrencyPair, OrderBook> buildOrderBookMapForAvailableCurrencyPair() {
        Map<CurrencyPair, OrderBook> orderBookMap = new ConcurrentHashMap<>();
        for (CurrencyPair currencyPair : CurrencyPair.values()) {
            OrderBook orderBook = new CurrencyOrderBook(currencyPair);
            orderBookMap.put(currencyPair, orderBook);
        }
        return orderBookMap;
    }

    /**
     * Call to Bellman ford algorithm and if arbitrage cycle present than print the result
     */
    private void trackArbitrageOpportunity() {
        Map<CurrencyPair, BigDecimal[]> currencyPairBestValueMap = getBestValueCurrencyMatrix();
        Map<CurrencyPair, BigDecimal> result = algoRunner.runAlgorithm(currencyPairBestValueMap);
        resultFormatter.processArbitrageRecord(result);

    }

    public Map<CurrencyPair, BigDecimal[]> getBestValueCurrencyMatrix() {
        return bestValueCurrencyMatrix;
    }

}
