package me.kmathers.quill.interpreter;

import me.kmathers.quill.Quill;
import me.kmathers.quill.interpreter.QuillValue.*;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Built-in inventory functions for Quill.
 */
public class BuiltInInventoryFuncs {
    private static Quill plugin = Quill.getPlugin(Quill.class);

    public static class ShowInventoryFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "show_inventory()", "2", "show_inventory(player, inventory)"));
            }
            
            Player player = args.get(0).asPlayer();
            InventoryValue inventoryValue = args.get(1).asInventory();

            String name = inventoryValue.getName();
            int size = inventoryValue.isLarge() ? 54 : 27;
            List<ItemStack> items = inventoryValue.getInventory();

            Inventory inventory = Bukkit.createInventory(null, size, Component.text(name));
            
            for (int i = 0; i < items.size(); i++) {
                inventory.setItem(i, items.get(i));
            }

            if (player.getOpenInventory().getTopInventory() != null) {
                player.closeInventory(Reason.PLUGIN);
            }
            player.openInventory(inventory);

            return new BooleanValue(true);
        }
    }

    public static class CloseInventoryFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "close_inventory()", "close_inventory(player)"));
            }
            
            Player player = args.get(0).asPlayer();

            if (player.getOpenInventory().getTopInventory() != null) {
                player.closeInventory(Reason.PLUGIN);
            }

            return new BooleanValue(true);
        }
    }

    public static class GetSlotFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "get_slot()", "2", "get_slot(inventory, slot)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();
            int slot = (int) args.get(1).asNumber();

            return new ItemValue(inventoryValue.getSlot(slot));
        }
    }

    public static class SetSlotFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 3) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "set_slot()", "3", "set_slot(inventory, slot, item)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();
            int slot = (int) args.get(1).asNumber();
            ItemStack item = args.get(2).asItem();

            inventoryValue.setSlot(slot, item);

            return new BooleanValue(true);
        }
    }

    public static class GetInventoryNameFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "get_inventory_name()", "get_inventory_name(inventory)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();

            return new StringValue(inventoryValue.getName());
        }
    }

    public static class SetInventoryNameFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "set_inventory_name()", "2", "set_inventory_name(inventory, name)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();
            String name = args.get(1).asString();

            inventoryValue.setName(name);

            return new BooleanValue(true);
        }
    }

    public static class GetSizeFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "get_size()", "get_size(inventory)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();

            return new NumberValue(inventoryValue.getSize());
        }
    }

    public static class IsLargeFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "is_large()", "is_large(inventory)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();

            return new BooleanValue(inventoryValue.isLarge());
        }
    }

    public static class ClearInventoryFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "clear_inventory()", "clear_inventory(inventory)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();

            inventoryValue.clear();

            return new BooleanValue(true);
        }
    }

    public static class ClearInventorySlotFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "clear_inventory_slot()", "2",  "clear_inventory_slot(inventory, slot)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();
            int slot = (int) args.get(1).asNumber();

            inventoryValue.clearSlot(slot);

            return new BooleanValue(true);
        }
    }

    public static class AddInventoryItemFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "add_inventory_item()", "2", "add_inventory_item(inventory, item)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();
            ItemStack item = args.get(1).asItem();

            inventoryValue.addItem(item);

            return new BooleanValue(true);
        }
    }

    public static class RemoveInventoryItemFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "remove_inventory_item()", "2", "remove_inventory_item(inventory, item)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();
            ItemStack item = args.get(1).asItem();

            inventoryValue.removeItem(item);

            return new BooleanValue(true);
        }
    }

    public static class ContainsItemFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "contains_item()", "2", "contains_item(inventory, item)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();
            ItemStack item = args.get(1).asItem();

            return new BooleanValue(inventoryValue.contains(item));
        }
    }

    public static class ContainsAtLeastFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 3) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "contains_at_least()", "2", "contains_at_least(inventory, item, amount)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();
            ItemStack item = args.get(1).asItem();
            int amount = (int) args.get(2).asNumber();

            return new BooleanValue(inventoryValue.containsAtLeast(item, amount));
        }
    }

    public static class GetAmountFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "get_amount()", "2", "get_amount(inventory, item)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();
            ItemStack item = args.get(1).asItem();

            return new NumberValue(inventoryValue.getAmount(item));
        }
    }

    public static class GetAllItemsFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "get_all_items()", "get_all_items(inventory)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();
            List<ItemStack> items = inventoryValue.getAllItems();
            List<QuillValue> itemValues = items.stream().map(item -> (QuillValue) new ItemValue(item)).toList();

            return new ListValue(itemValues);
        }
    }

    public static class SetInventoryFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "set_inventory()", "2", "set_inventory(inventory, items)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();
            List<ItemStack> items = args.get(1).asList().stream().map(item -> item.asItem()).toList();

            inventoryValue.setInventory(items);

            return new BooleanValue(true);
        }
    }

    public static class IsEmptyFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "is_empty()", "is_empty(inventory)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();

            return new BooleanValue(inventoryValue.isEmpty());
        }
    }

    public static class IsFullFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "is_full()", "is_full(inventory)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();

            return new BooleanValue(inventoryValue.isFull());
        }
    }

    public static class FirstEmptyFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 1) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-single", "first_empty()", "first_empty(inventory)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();

            return new NumberValue(inventoryValue.firstEmpty());
        }
    }

    public static class AllFunction implements QuillInterpreter.BuiltInFunction {
        @Override
        public QuillValue call(List<QuillValue> args, ScopeContext scope, QuillInterpreter interpreter) {
            if (args.size() != 2) {
                throw new RuntimeException(plugin.translate("quill.error.developer.arguments.requires-multiple", "all()", "2", "all(inventory, item)"));
            }
            
            InventoryValue inventoryValue = args.get(0).asInventory();
            List<Integer> slots = inventoryValue.all(args.get(1).asItem());
            List<QuillValue> slotValues = slots.stream().map(slot -> (QuillValue) new NumberValue(slot)).toList();
            
            return new ListValue(slotValues);
        }
    }
}