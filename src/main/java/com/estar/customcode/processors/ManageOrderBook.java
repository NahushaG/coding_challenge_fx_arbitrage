package com.estar.customcode.processors;

import com.estar.arbitrage.ArbitragePrinter;
import com.estar.arbitrage.ArbitrageProcessor;
import com.estar.arbitrage.ResultFormatter;
import com.estar.customcode.algo.*;
import com.estar.customcode.exceptions.ProcessException;
import com.estar.orderbook.model.CurrencyPair;
import com.estar.orderbook.model.OrderbookListener;
import com.estar.orderbook.model.Price;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * @Author Nahusha Ganiga
 *
 * <p>
 * Implementation of the listener class, only reason to create a new class and not implement inline
 * is just to have tidy implementation. The class runs two async task 1. updates the order book
 * 2. Calls the bellman ford algo to find the negative path and then calculate the negative path weight
 * to evaluate the arbitrage factor
 */
public class ManageOrderBook implements OrderbookListener {

    private final Map<CurrencyPair, OrderBook> currencyPairOrderBookMap;
    private final AlgoRunner algoRunner;

    private ResultFormatter resultFormatter;
    private static Logger LOG = LoggerFactory.getLogger(ManageOrderBook.class);

    public ManageOrderBook() {
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
     * 1. Updates the order book based on the action (ADD, MODIFY, INSERT). It also evaluates best buy
     * 2. Once the update task is done then it detect for Arbitrage cycle and then compute the arbitrage factor
     * and prints instrument and factor.
     * The check will done only if there are more than 2 currency pair
     *
     * @param action
     * @param price
     */
    @Override
    public void handlePriceUpdate(Action action, Price price) {
        int core_count = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(core_count);
        updateOrderBook(action, price);
        CompletableFuture.supplyAsync(updateOrderBook(action, price), executorService)
                .thenApplyAsync(orderBooks -> updateBestBuyMatrix(orderBooks))
                .thenAccept(bestPriceMatrix -> trackArbitrageOpportunity(bestPriceMatrix))
                .exceptionally(throwable -> {
                    LOG.info("Can not get build bestBuy");
                    return null;
                });
        executorService.shutdown();
    }

    private Supplier<Map<CurrencyPair, OrderBook>> updateOrderBook(Action action, Price price) {
        CurrencyPair currencyPair = price.instrument();
        OrderBook currencyOrderBook = currencyPairOrderBookMap.get(currencyPair);
        currencyOrderBook.manageOrder(action, price);
        return () -> currencyPairOrderBookMap;
    }


    private Map<CurrencyPair, BigDecimal[]> updateBestBuyMatrix(Map<CurrencyPair, OrderBook> orderBooks) throws CompletionException {
        Map<CurrencyPair, BigDecimal[]> bestBuyMatrix = new HashMap<>();
        if (orderBooks.size() > 2) {
            for (Map.Entry<CurrencyPair, OrderBook> entry : orderBooks.entrySet()) {
                OrderBook orderBook = entry.getValue();
                BigDecimal bestBuy[] = new BigDecimal[]{orderBook.getBestBuy().price(), orderBook.getBestSell().price()};
                bestBuyMatrix.put(entry.getKey(), bestBuy);
            }

            return bestBuyMatrix;
        } else {
            throw new ProcessException("Best Buy Matrix can be generated");
        }
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
    private void trackArbitrageOpportunity(Map<CurrencyPair, BigDecimal[]> bestMatrixPrice) {
        Map<CurrencyPair, BigDecimal> result = algoRunner.runAlgorithm(bestMatrixPrice);
        resultFormatter.processArbitrageRecord(result);
    }
}
