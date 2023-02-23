package com.estar.customcode.processors;

import com.estar.orderbook.model.CurrencyPair;
import com.estar.orderbook.model.Price;

import java.math.BigDecimal;

import static com.estar.orderbook.model.OrderbookListener.Action;


public interface OrderBook {
    public static enum TRANSACTION_ACTION{
        BUY,
        SELL,
    }

    /**
     * returns currency pair for the instrument
     * @return
     */
    public CurrencyPair getInstrument();

    /**
     * returns best buy price for that currency pair
     * @return
     */
    public Price getBestBuy();

    /**
     * return best sell price sell
     * @return
     */
    public Price getBestSell();

    /**
     * Add, remove modify price. Also update the bestbuy and bestSell value based on the operation
     *
     * @param action
     * @param price
     *
     */
    public void manageOrder(Action action, Price price);

}
