package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import java.security.Principal;

@RestController
@RequestMapping("/cart")
@PreAuthorize("isAuthenticated()")
public class ShoppingCartController {

    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;
    private final ProductDao productDao;

    public ShoppingCartController(ShoppingCartDao shoppingCartDao,
                                  UserDao userDao,
                                  ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    @GetMapping
    public ShoppingCart getCart(Principal principal) {

        String username = principal.getName();
        User user = userDao.getByUserName(username);

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not found."
            );
        }

        return shoppingCartDao.getByUserId(user.getId());
    }

    @PostMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addProductToCart(@PathVariable int productId,
                                 Principal principal) {

        String username = principal.getName();
        User user = userDao.getByUserName(username);

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not found."
            );
        }


        if (productDao.getById(productId) == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found."
            );
        }

        int userId = user.getId();

        Integer quantity =
                shoppingCartDao.getItemQuantity(userId, productId);

        if (quantity == null) {
            shoppingCartDao.addItem(userId, productId, 1);
        } else {
            shoppingCartDao.updateQuantity(userId, productId, quantity + 1);
        }
    }

    @PutMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProductQuantity(@PathVariable int productId,
                                      @RequestBody int quantity,
                                      Principal principal) {

        if (quantity < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be at least 1."
            );
        }

        String username = principal.getName();
        User user = userDao.getByUserName(username);

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not found."
            );
        }

        int userId = user.getId();

        Integer existingQuantity =
                shoppingCartDao.getItemQuantity(userId, productId);

        if (existingQuantity == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found in cart."
            );
        }

        shoppingCartDao.updateQuantity(userId, productId, quantity);
    }

    @DeleteMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeProduct(@PathVariable int productId,
                              Principal principal) {

        String username = principal.getName();
        User user = userDao.getByUserName(username);

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not found."
            );
        }

        int userId = user.getId();

        Integer quantity =
                shoppingCartDao.getItemQuantity(userId, productId);

        if (quantity == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found in cart."
            );
        }

        shoppingCartDao.updateQuantity(userId, productId, 0);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(Principal principal) {

        String username = principal.getName();
        User user = userDao.getByUserName(username);

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not found."
            );
        }

        shoppingCartDao.clearCart(user.getId());
    }
}
