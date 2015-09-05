package controllers;

import forms.SubscriptionFormData;
import io.sphere.client.SphereClientException;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.commands.CartUpdateCommand;
import io.sphere.sdk.carts.commands.updateactions.AddLineItem;
import io.sphere.sdk.products.ProductVariant;
import models.ProductPageData;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.mvc.Result;
import services.CartService;
import services.PaymentService;
import views.html.index;

import java.util.Optional;

import static play.data.Form.form;

public class ProductController extends BaseController {
    private final static Form<SubscriptionFormData> ADD_TO_CART_FORM = form(SubscriptionFormData.class);

    public ProductController(final CartService cartService, final PaymentService paymentService, final Configuration configuration) {
        super(cartService, paymentService, configuration);
    }

    public Result show() {
        final Cart cart = currentCart();
        final Optional<ProductVariant> selectedVariant = getSelectedVariant(cart);
        final int selectedFrequency = frequency(cart.getId());
        final ProductPageData productPageData = new ProductPageData(productProjection(), selectedVariant, selectedFrequency);
        return ok(index.render(productPageData));
    }

    public Result submit() {
        final Form<SubscriptionFormData> boundForm = ADD_TO_CART_FORM.bindFromRequest();
        if (!boundForm.hasErrors()) {
            final Optional<ProductVariant> variant = variant(boundForm.get().variantId);
            if (variant.isPresent()) {
                try {
                    setProductToCart(variant.get(), boundForm.get().howOften);
                    return redirect(routes.OrderController.show());
                } catch (SphereClientException e) {
                    Logger.error(e.getMessage(), e);
                }
            } else {
                flash("error", "Product not found. Please try again.");
            }
        } else {
            flash("error", "Please select a box and how often you want it.");
        }
        return redirect(routes.ProductController.show());
    }

    private Optional<ProductVariant> getSelectedVariant(final io.sphere.sdk.carts.Cart cart) {
        final Optional<ProductVariant> selectedVariant =
                (cart.getLineItems().size() > 0)
                        ? Optional.ofNullable(cart.getLineItems().get(0).getVariant())
                        : Optional.empty();
        return selectedVariant;
    }

    private void setProductToCart(final ProductVariant variant, final int frequency) {
        final Cart clearedCart = clearLineItemsFromCurrentCart(currentCart());
        final AddLineItem action = AddLineItem.of(productProjection().getId(), variant.getId(), frequency);
        final Cart updatedCart = sphereClient().execute(CartUpdateCommand.of(clearedCart, action)).toCompletableFuture().join();
        sphere().customObjects().set(FREQUENCY, updatedCart.getId(), frequency).get();
    }
}
