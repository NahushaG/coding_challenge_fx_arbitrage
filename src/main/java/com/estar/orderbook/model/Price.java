package com.estar.orderbook.model;

import java.math.BigDecimal;

public record Price(
		long id,
		CurrencyPair instrument,
		boolean ask,
		int quantity, 
		BigDecimal price) 
{}
