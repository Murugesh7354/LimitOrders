package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
 import java.util.Iterator;

public class LimitOrderAgent implements PriceListener {
    private final ExecutionClient executionClient;
    private final List<Order> orders;

    public LimitOrderAgent(final ExecutionClient executionClient) {
        this.executionClient = executionClient;
        this.orders = new ArrayList<>();
    }

    @Override
public void priceTick(String productId, BigDecimal price) {
    Iterator<Order> iterator = orders.iterator();
    while (iterator.hasNext()) {
        Order order = iterator.next();
        if (order.getProductId().equals(productId) && order.isExecutable(price)) {
            try {
                executeOrder(order);
                iterator.remove(); 
            } catch (ExecutionException e) {
                System.err.println("Order execution failed for product " + productId + ": " + e.getMessage());
            }
        }
    }
}
    public void addOrder(boolean isBuy, String productId, int amount, BigDecimal limitPrice) {
        orders.add(new Order(isBuy, productId, amount, limitPrice));
    }

    private void executeOrder(Order order) {
        try {
            if (order.isBuy()) {
                executionClient.buy(order.getProductId(), order.getAmount());
            } else {
                executionClient.sell(order.getProductId(), order.getAmount());
            }
           
            orders.remove(order);
        } catch (ExecutionException e) {
           
            System.err.println("Order execution failed: " + e.getMessage());
        }
    }

    // Inner class to represent an order
    private static class Order {
        private final boolean isBuy;
        private final String productId;
        private final int amount;
        private final BigDecimal limitPrice;

        public Order(boolean isBuy, String productId, int amount, BigDecimal limitPrice) {
            this.isBuy = isBuy;
            this.productId = productId;
            this.amount = amount;
            this.limitPrice = limitPrice;
        }

        public boolean isBuy() {
            return isBuy;
        }

        public String getProductId() {
            return productId;
        }

        public int getAmount() {
            return amount;
        }

        public boolean isExecutable(BigDecimal price) {
            return isBuy ? price.compareTo(limitPrice) <= 0 : price.compareTo(limitPrice) >= 0;
        }
    }
}
