package patchi.distomc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.lwjgl.glfw.GLFW;
import patchi.distomc.item.DistoItem;

public class DistoMCClient implements ClientModInitializer {

    private static KeyBinding modeKeyBind;

    @Override
    public void onInitializeClient() {

        modeKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.distomc.mode", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_P, // The keycode of the key
                "category.distomc.disto" // The translation key of the keybinding's category.
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (modeKeyBind.wasPressed()) {
                Item heldItem = client.player.getMainHandStack().getItem();

                if(heldItem instanceof DistoItem) {
                    ((DistoItem) heldItem).changeMode();
                }

            }
        });
    }
}
