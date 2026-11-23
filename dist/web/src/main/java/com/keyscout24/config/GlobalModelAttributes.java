package com.keyscout24.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.keyscout24.auth.CurrentUser;
import com.keyscout24.auth.UserPrincipal;
import com.keyscout24.db.CartItemRepository;

/** Port of the Flask context processors: current_user and cart_count in every template. */
@ControllerAdvice
public class GlobalModelAttributes {

    private final CartItemRepository cartItems;

    public GlobalModelAttributes(CartItemRepository cartItems) {
        this.cartItems = cartItems;
    }

    /** null when nobody is logged in (Flask: current_user.is_authenticated == False). */
    @ModelAttribute("currentUser")
    public UserPrincipal currentUser() {
        return CurrentUser.get().orElse(null);
    }

    @ModelAttribute("cartCount")
    public long cartCount() {
        return CurrentUser.get().map(user -> cartItems.countByUser_Id(user.id())).orElse(0L);
    }
}
