package net.wikicraft;

import net.dimaskama.mcef.api.MCEFApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class WikicraftSpotlightClient implements ClientModInitializer {

	public static KeyMapping openSpotlightKey;

	private static final KeyMapping.Category WIKICRAFT_CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath("wikicraft-spotlight", "general"));

	@Override
	public void onInitializeClient() {
		MCEFApi.initialize();
		SpotlightScreen.loadConfig();
		openSpotlightKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.wikicraft-spotlight.open",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_G,
				WIKICRAFT_CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openSpotlightKey.consumeClick()) {
				if (client.screen == null) {
					if (WikiBrowserScreen.stayOpen && WikiBrowserScreen.lastUrl != null) {
						client.setScreen(new WikiBrowserScreen(
								WikiBrowserScreen.lastUrl,
								WikiBrowserScreen.lastTitle,
								null
						));
					} else {
						client.setScreen(new SpotlightScreen());
					}
				}
			}
		});
	}
}
