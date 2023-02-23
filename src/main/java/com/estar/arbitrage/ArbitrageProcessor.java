package com.estar.arbitrage;

public interface ArbitrageProcessor {
	
	/**
	 * Notified on detection of arbitrage situation.
	 */
	void reportArbitrage(ArbitrageRecord arbitrageRecord);
}
