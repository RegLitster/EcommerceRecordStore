package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }


    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart cart = new ShoppingCart();

        String sql = """
                    SELECT p.*, sc.quantity
                    FROM shopping_cart sc
                    JOIN products p ON p.product_id = sc.product_id
                    WHERE sc.user_id = ?
                """;

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);

            ResultSet row = statement.executeQuery();

            while (row.next()) {
                Product product = mapProduct(row);

                ShoppingCartItem item = new ShoppingCartItem();
                item.setProduct(product);
                item.setQuantity(row.getInt("quantity"));

                cart.add(item);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return cart;
    }


    @Override
    public Integer getItemQuantity(int userId, int productId) {
        String sql = """
                    SELECT quantity
                    FROM shopping_cart
                    WHERE user_id = ? AND product_id = ?
                """;

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setInt(2, productId);

            ResultSet row = statement.executeQuery();
            if (row.next()) {
                return row.getInt("quantity");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public void addItem(int userId, int productId, int quantity) {
        String sql = """
                    INSERT INTO shopping_cart (user_id, product_id, quantity)
                    VALUES (?, ?, ?)
                """;

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            statement.setInt(3, quantity);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateQuantity(int userId, int productId, int quantity) {
        String sql = """
                    UPDATE shopping_cart
                    SET quantity = ?
                    WHERE user_id = ? AND product_id = ?
                """;

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, quantity);
            statement.setInt(2, userId);
            statement.setInt(3, productId);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearCart(int userId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Product mapProduct(ResultSet row) throws SQLException {
        return new Product(
                row.getInt("product_id"),
                row.getString("name"),
                row.getBigDecimal("price"),
                row.getInt("category_id"),
                row.getString("description"),
                row.getString("subcategory"),
                row.getInt("stock"),
                row.getBoolean("featured"),
                row.getString("image_url")
        );
    }
}
