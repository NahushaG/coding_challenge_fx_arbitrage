package com.estar.customcode.processors;

import com.estar.orderbook.model.CurrencyPair;
import com.estar.orderbook.model.OrderbookListener.Action;
import com.estar.orderbook.model.Price;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class CurrencyOrderBook implements OrderBook {
    private final CurrencyPair currencyPair;
    private PriorityQueue<Price> bestBuy;
    private PriorityQueue<Price> bestSell;
    private Map<Long, Price> buyOrderBook;
    private Map<Long, Price> sellOrderBook;

    private Lock lock = new ReentrantLock();


    public CurrencyOrderBook(CurrencyPair currencyPair) {
        this.currencyPair = currencyPair;
        this.buyOrderBook = new ConcurrentHashMap<>();
        this.sellOrderBook = new ConcurrentHashMap<>();
    }

    @Override
    public CurrencyPair getInstrument() {
        return currencyPair;
    }

    @Override
    public Price getBestBuy() {
        return bestBuy != null ? bestBuy.peek() : getDefaultBestBuy();
    }

    @Override
    public Price getBestSell() {
        return bestSell != null ? bestSell.peek() : getDefaultBestSell();
    }

    @Override
    public void manageOrder(Action action, Price price) {
        Map<Long, Price> orderBook;
        boolean isBuy = price.ask();
        if (isBuy) {
            orderBook = buyOrderBook;

        } else {
            orderBook = sellOrderBook;
        }
        synchronized (lock) {
            updateOrderBook(action, orderBook, price);
            updateBestValues(isBuy);
        }
    }

    public void updateOrderBook(Action action, Map<Long, Price> orderBook, Price price) {
        switch (action) {
            case INSERT -> {
                orderBook.put(price.id(), price);
            }
            case DELETE -> {
                orderBook.remove(price.id());
            }
            case MODIFY -> {
                if (orderBook.containsKey(price.id())) {
                    orderBook.put(price.id(), price);
                } else {
                    System.out.println("No price with id " + price.id() + " found for modification\n");
                }
            }
        }
        if (Action.INSERT == action || Action.MODIFY == action) {
            orderBook.put(price.id(), price);
        }

    }

    /**
     * updates the best buy or best sell based on the incoming price.
     * We have a priority queue which based on comparator keeps all sell
     * in descending order of price and buy in ascending and always pick
     * the top one as best sell  and buy
     * @param isBuy
     */
    void updateBestValues(boolean isBuy) {
        List<Price> priceList;
        Comparator<Price> comparator;
        if (isBuy) {
            priceList = this.buyOrderBook.values().stream().collect(Collectors.toList());
            comparator = Comparator.comparing(Price::price); //ascending
        } else {
            priceList = this.sellOrderBook.values().stream().collect(Collectors.toList());
            comparator = (o1, o2) -> {
                return (o1.price().compareTo(o2.price())) * -1; //descending
            };
        }
        PriorityQueue<Price> pricePriorityQueue = new PriorityQueue<>(comparator);
        pricePriorityQueue.addAll(priceList);
        if (isBuy) {
            this.bestBuy = pricePriorityQueue;
        } else {
            this.bestSell = pricePriorityQueue;
        }
    }

    /**
     * it returns the ideal buy. It is needed to compute the arbitrage if the first very transaction of a
     * specific order book is sell
     *
     * @return
     */
    Price getDefaultBestBuy() {
        Price bestSell = getBestSell();
        return bestSell != null ?
                new Price(bestSell.id(),
                        bestSell.instrument(),
                        !bestSell.ask(),
                        bestSell.quantity(),
                        new BigDecimal(1).divide(bestSell.price(), 3, RoundingMode.HALF_UP))
                : null;
    }

    /**
     * it returns the ideal buy. It is needed to compute the arbitrage if the first very transaction of a
     * specific order book is buy
     *
     * @return
     */
    Price getDefaultBestSell() {
        Price bestBuy = getBestBuy();
        return bestBuy != null ?
                new Price(bestBuy.id(),
                        bestBuy.instrument(),
                        !bestBuy.ask(),
                        bestBuy.quantity(),
                        new BigDecimal(1).divide(bestBuy.price(), 3, RoundingMode.HALF_UP))
                : null;
    }
}
