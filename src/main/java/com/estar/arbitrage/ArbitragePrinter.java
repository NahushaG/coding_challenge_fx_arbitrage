package com.estar.arbitrage;

public class ArbitragePrinter implements ArbitrageProcessor {

	@Override
	public void reportArbitrage(ArbitrageRecord arbitrageRecord) {
		System.out.println("Arbitrage with factor " + arbitrageRecord.factor() + " detected for:");
		for(ArbitrageRecord.Element arbitrageRouteNode : arbitrageRecord.arbitrageElements()) {
			System.out.println(arbitrageRouteNode.currencyPair() 
					+ " - Best Bid: " + arbitrageRouteNode.bestBidPrice().price()
					+ " - Best Ask: " + arbitrageRouteNode.bestAskPrice().price());
		}
		
	}

}
