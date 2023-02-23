package com.estar.arbitrage;

import com.estar.customcode.processors.ManageOrderBook;
import com.estar.orderbook.generator.PriceGenerator;
import com.estar.orderbook.model.OrderbookListener;
import com.estar.orderbook.model.Price;

public class Application {
	
	public static void main(String[] args) throws InterruptedException {
		PriceGenerator priceGenerator = new PriceGenerator(100);
		OrderbookListener orderbookListener = new ManageOrderBook();
		priceGenerator.subscribe(orderbookListener);
		Thread.sleep(1000000000);
	}
}
