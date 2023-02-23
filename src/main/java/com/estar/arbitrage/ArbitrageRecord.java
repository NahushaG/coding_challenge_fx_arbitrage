package com.estar.arbitrage;

import java.math.BigDecimal;
import java.util.List;

import com.estar.orderbook.model.CurrencyPair;
import com.estar.orderbook.model.Price;

/**
 * An {@link ArbitrageRecord} describes an arbitrage situation based on current orderbooks with a factor and a list of {@link Element}
 * 
 * The factor describes how much profit can be made by using the arbitrage opportunity without factoring in the quantity.
 * E.g. If you can turn 1€ into 1.2€, the factor would be 1.2.
 * 
 * The elements contain snapshots of the orderbooks used for the arbitrage.
 * For each CurrencyPair used, both orderbook sides are contained, even though only one side is used for the arbitrage.
 */
public record ArbitrageRecord(BigDecimal factor,
		List<Element> arbitrageElements) {
	
	public static record Element(
			CurrencyPair currencyPair,
			Price bestBidPrice,
			Price bestAskPrice) {}
}
