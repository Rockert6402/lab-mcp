package com.tdea.mcp_server_tdea;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import com.tdea.mcp_server_tdea.model.ShoppingItem;

@Service
public class ShoppingCart {

    // Almacenamiento en memoria, seguro para múltiples hilos
    private final Map<String, ShoppingItem> shoppingList = new ConcurrentHashMap<>();

    @Tool(name = "addItem",
          description = "Add an item to the shopping list. Specify name, quantity, category, unit price, brand and description.")
    public String addItem(
            @ToolParam(description = "Item name") String name,
            @ToolParam(description = "Quantity to add, greater than zero") int quantity,
            @ToolParam(description = "Category, e.g. dairy, fruit", required = false) String category,
            @ToolParam(description = "Unit price, zero or greater") double price,
            @ToolParam(description = "Brand", required = false) String brand,
            @ToolParam(description = "Short description", required = false) String description) {

        if (name == null || name.trim().isEmpty() || quantity <= 0 || price < 0) {
            return "Error: Invalid item name, quantity or price.";
        }

        String key = name.trim().toLowerCase();
        // compute es atómico: suma la cantidad si el ítem ya existe
        shoppingList.compute(key, (k, existingItem) -> {
            if (existingItem == null) {
                return new ShoppingItem(name.trim(), quantity, category, price, brand, description);
            }
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            return existingItem;
        });

        return "Added " + quantity + " of " + name + " to the shopping list.";
    }

    @Tool(name = "getItems",
          description = "Get all shopping items, including their name, quantity, category, price, brand and description.")
    public List<ShoppingItem> getItems() {
        return new ArrayList<>(shoppingList.values());
    }

    @Tool(name = "removeItem",
          description = "Remove a specified quantity of an item. If quantity is zero, negative, or greater than or equal to the available quantity, remove the entire item.")
    public String removeItem(
            @ToolParam(description = "Item name") String name,
            @ToolParam(description = "Quantity to remove") int quantity) {

        if (name == null || name.trim().isEmpty()) {
            return "Error: Invalid item name.";
        }

        String key = name.trim().toLowerCase();
        ShoppingItem item = shoppingList.get(key);
        if (item == null) {
            return "Error: Item '" + name + "' not found in the shopping list.";
        }

        if (quantity <= 0 || quantity >= item.getQuantity()) {
            shoppingList.remove(key);
            return "Removed '" + name + "' from the shopping list.";
        }

        item.setQuantity(item.getQuantity() - quantity);
        return "Removed " + quantity + " of '" + name + "'. Remaining quantity: " + item.getQuantity() + ".";
    }
}
