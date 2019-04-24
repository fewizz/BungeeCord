package net.md_5.bungee.modern;

import java.util.Arrays;
import java.util.Queue;
import java.util.Set;

import com.google.common.base.Preconditions;

import lombok.Getter;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.forge.ForgeConstants;
import net.md_5.bungee.forge.ForgeServerHandler;
import net.md_5.bungee.forge.ForgeUtils;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.NetworkState;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.SetCompression;
import net.md_5.bungee.util.QuietException;

public class ModernServerConnector extends ServerConnector<ModernUserConnection> {
	private State thisState = State.UNDEF;
	@Getter
	private ForgeServerHandler handshakeHandler;
	@Getter
	private Login login;
	
	public ModernServerConnector(ChannelWrapper ch, ModernUserConnection user, BungeeServerInfo target) {
		super(ch, user, target);
	}

	private enum State {
		UNDEF, LOGIN_REQUEST, LOGIN_SUCCESS, ENCRYPT_RESPONSE, LOGIN, FINISHED;
		
		void shouldBe(State s) {
			Preconditions.checkState(s == this);
		}
	}

	@Override
	public void connected() throws Exception {
		this.handshakeHandler = new ForgeServerHandler(user, channel, target);
		Handshake originalHandshake = user.pendingConnection.getHandshake();
		Handshake copiedHandshake = new Handshake(originalHandshake);

		if (target.ipForward()) {
			String newHost = copiedHandshake.getHost() + "\00" + user.getAddress().getHostString() + "\00" + user.getUUID();

			// Handle properties.
			LoginResult.Property[] properties = new LoginResult.Property[0];

			LoginResult profile = user.pendingConnection.getLoginProfile();
			if (profile != null && profile.getProperties() != null && profile.getProperties().length > 0) {
				properties = profile.getProperties();
			}

			if (user.forgeClientHandler.isFmlTokenInHandshake()) {
				// Get the current properties and copy them into a slightly bigger array.
				LoginResult.Property[] newp = Arrays.copyOf(properties, properties.length + 2);
				// Add a new profile property that specifies that this user is a Forge user.
				newp[newp.length - 2] = new LoginResult.Property(ForgeConstants.FML_LOGIN_PROFILE, "true", null);
				// If we do not perform the replacement, then the IP Forwarding code in Spigot
				// et. al. will try to split on this prematurely.
				newp[newp.length - 1] = new LoginResult.Property(ForgeConstants.EXTRA_DATA, user.getExtraDataInHandshake().replaceAll("\0", "\1"), "");
				// All done.
				properties = newp;
			}
			// If we touched any properties, then append them
			if (properties.length > 0) {
				newHost += "\00" + bungee.gson.toJson(properties);
			}

			copiedHandshake.setHost(newHost);
		} else if (!user.getExtraDataInHandshake().isEmpty()) {
			// Restore the extra data
			copiedHandshake.setHost(copiedHandshake.getHost() + user.getExtraDataInHandshake());
		}

		channel.write(copiedHandshake);
		channel.setNetworkState(NetworkState.LOGIN);
		thisState = State.LOGIN_SUCCESS;
		channel.write(new LoginRequest(user.getName()));
	}

	@Override
	public void handle(LoginSuccess loginSuccess) throws Exception {
		thisState.shouldBe(State.LOGIN_SUCCESS);
		
		channel.setNetworkState(NetworkState.GAME);
		thisState = State.LOGIN;

		// Only reset the Forge client when:
		// 1) The user is switching servers (so has a current server)
		// 2) The handshake is complete
		// 3) The user is currently on a modded server (if we are on a vanilla server,
		// we may be heading for another vanilla server, so we don't need to reset.)
		//
		// user.getServer() gets the user's CURRENT server, not the one we are trying
		// to connect to.
		//
		// We will reset the connection later if the current server is vanilla, and
		// we need to switch to a modded connection. However, we always need to reset
		// the
		// connection when we have a modded server regardless of where we go - doing it
		// here makes sense.
		if (user.getServer() != null && user.forgeClientHandler.isHandshakeComplete() && user.getServer().isForgeServer())
			user.forgeClientHandler.resetHandshake();

		throw CancelSendSignal.INSTANCE;
	}

	@Override
	public void handle(SetCompression setCompression) throws Exception {
		channel.setCompressionThreshold(setCompression.getThreshold());
	}

	@Override
	public void handle(Login login) throws Exception {	
		thisState.shouldBe(State.LOGIN);
		
		this.login = login;
		channel.write(bungee.registerChannels(user.pendingConnection.getProtocol()));
		
		Queue<DefinedPacket> packetQueue = target.getPacketQueue();
		synchronized (packetQueue) {
			while (!packetQueue.isEmpty())
				channel.write(packetQueue.poll());
		}

		for (PluginMessage message : user.pendingConnection.getRelayMessages()) {
			channel.write(message);
		}

		if (user.getSettings() != null) 
			channel.write(user.getSettings());

		loginFuture.setSuccess(null);
		
		thisState = State.FINISHED;

		throw CancelSendSignal.INSTANCE;
	}

	@Override
	public void handle(EncryptionRequest encryptionRequest) throws Exception {
		throw new QuietException( "Server in online mode!" );
	}

	@Override
	public void handle(PluginMessage pluginMessage) throws Exception {
		if (target.forgeSupport()) {
			if (pluginMessage.getTag().equals(ForgeConstants.FML_REGISTER)) {
				Set<String> channels = ForgeUtils.readRegisteredChannels(pluginMessage);
				boolean isForgeServer = false;
				for (String channel : channels) {
					if (channel.equals(ForgeConstants.FML_HANDSHAKE_TAG)) {
						// If we have a completed handshake and we have been asked to register a FML|HS
						// packet, let's send the reset packet now. Then, we can continue the message
						// sending.
						// The handshake will not be complete if we reset this earlier.
						if (user.getServer() != null && user.forgeClientHandler.isHandshakeComplete()) {
							user.forgeClientHandler.resetHandshake();
						}

						isForgeServer = true;
						break;
					}
				}

				if (isForgeServer && !this.handshakeHandler.isServerForge()) {
					// We now set the server-side handshake handler for the client to this.
					handshakeHandler.setServerAsForgeServer();
					user.setForgeServerHandler(handshakeHandler);
				}
			}

			if (pluginMessage.getTag().equals(ForgeConstants.FML_HANDSHAKE_TAG) || pluginMessage.getTag().equals(ForgeConstants.FORGE_REGISTER)) {
				this.handshakeHandler.handle(pluginMessage);

				// We send the message as part of the handler, so don't send it here.
				throw CancelSendSignal.INSTANCE;
			}
		}

		// We have to forward these to the user, especially with Forge as stuff might
		// break
		// This includes any REGISTER messages we intercepted earlier.
		user.unsafe().sendPacket(pluginMessage);
	}
}
