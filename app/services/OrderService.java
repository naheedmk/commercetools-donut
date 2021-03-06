package services;

import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.orders.Order;
import play.libs.F;

/**
 * Business service that provides access to the SPHERE.IO Order API.
 */
public interface OrderService {

    /**
     * Creates a new {@code Order} from an existing {@code Cart}.
     *
     * @param cart the {@code Cart} object to create the {@code Order} from, must not be null
     * @return the newly created {@code Order}, must not be null
     */
    F.Promise<Order> createOrder(Cart cart);
}