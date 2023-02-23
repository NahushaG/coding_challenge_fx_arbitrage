package com.estar.customcode.algo;

import com.estar.orderbook.model.CurrencyPair;

import java.util.HashMap;
import java.util.Map;

public class UtilClass {
    /***
     * Returns map of all the possible CurrencyPair combination with their respective order book.
     * This is needed as the cycle might be in reverse so for that we need to fetch the right order book
     *
     * e.g. we had EURUSD order book but the vertex may return cycle for USDEUR which is also same
     * this map help in resolving order book for reverse node
     *
     * @return
     */
    public static Map<String, CurrencyPair> getCurrencyPair() {
        Map<String, CurrencyPair> currencyPairLookup = new HashMap<>();
        for (CurrencyPair currencyPair : CurrencyPair.values()) {
            String currencyPairStr = currencyPair.getBaseCurrency() + "_" + currencyPair.getQuoteCurrency();
            String inverseCurrencyPairStr = currencyPair.getQuoteCurrency() + "_" + currencyPair.getBaseCurrency();
            currencyPairLookup.put(currencyPairStr, currencyPair);
            currencyPairLookup.put(inverseCurrencyPairStr, currencyPair);
        }
        return currencyPairLookup;
    }
}
