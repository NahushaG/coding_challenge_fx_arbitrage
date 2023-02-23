package com.estar.orderbook.model;

public interface OrderbookListener {
	
	public enum Action {
		INSERT,
		MODIFY,
		DELETE;
	}
	
	/**
	 * Called on an orderbook update. Prices can be added, modified or deleted from the orderbook.
	 */
	public void handlePriceUpdate(Action action, Price price);

}
