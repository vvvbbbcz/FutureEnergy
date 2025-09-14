package net.industrybase.api.electric;

import net.industrybase.api.client.renderer.blockentity.WireConnectableRenderer;
import net.minecraft.core.BlockPos;

import java.util.Set;

public interface IWireConnectable {
	/**
	 * When the wire connector start to render,
	 * the renderer will check if the block is subscribed,
	 * if not, the renderer will send the packet to the server,
	 * tell the server that the player can see the wire connector,
	 * the server should send the wires that connected to the connector,
	 * and if the wires changed, the server should sync these data to client.
	 * <br>
	 * After the renderer send the packet to the server,
	 * the block will be labeled as subscribed
	 * by calling {@link #setSubscribed()}.
	 * <br>
	 * You can see {@link WireConnectableRenderer#render} for more details.
	 *
	 * @return if the block is subscribed
	 */
	boolean isSubscribed();

	/**
	 * Label the block as subscribed.
	 */
	void setSubscribed();

	Set<BlockPos> getWires();
}
