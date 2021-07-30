package github.pitbox46.monetamoney.network;

import github.pitbox46.monetamoney.data.Auctioned;
import github.pitbox46.monetamoney.network.server.*;
import github.pitbox46.monetamoney.screen.AnchorScreen;
import github.pitbox46.monetamoney.screen.IStatusable;
import github.pitbox46.monetamoney.screen.vault.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientProxy extends CommonProxy {
    public static long personalBalance = 0;
    public static long teamBalance = 0;

    public ClientProxy() {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void handleSOpenMainPage(NetworkEvent.Context ctx) {
        Minecraft.getInstance().displayGuiScreen(new MainPage());
    }

    @Override
    public void handleSOpenBalancePage(NetworkEvent.Context ctx, SOpenBalancePage packet) {
        if(Minecraft.getInstance().world != null) {
            Minecraft.getInstance().displayGuiScreen(new BalancePage());
            personalBalance = packet.personalBal;
            teamBalance = packet.teamBal;
        }
    }

    @Override
    public void handleSUpdateBalance(NetworkEvent.Context ctx, SUpdateBalance packet) {
        personalBalance = packet.personalBal;
        teamBalance = packet.teamBal;
    }

    @Override
    public void handleSSyncAuctionNBT(NetworkEvent.Context ctx, SSyncAuctionNBT packet) {
        Auctioned.auctionedNBT = packet.nbt;
    }

    @Override
    public void handleSDenyItemUse(NetworkEvent.Context ctx, SDenyUseItem packet) {
        if(Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.setHeldItem(packet.hand, packet.item);
        }
    }

    @Override
    public void handleSOpenTeamsPage(NetworkEvent.Context ctx, SOpenTeamsPage packet) {
        if(Minecraft.getInstance().world != null) {
            Minecraft.getInstance().displayGuiScreen(new TeamsPage(packet.team, packet.type));
        }
    }

    @Override
    public void handleSOpenChunksPage(NetworkEvent.Context ctx, SOpenChunksPage packet) {
        if(Minecraft.getInstance().world != null) {
            Minecraft.getInstance().displayGuiScreen(new ChunksPage(packet.team, packet.chunks));
        }
    }

    @Override
    public void handleSOpenAnchorPage(NetworkEvent.Context ctx, SOpenAnchorPage packet) {
        if(Minecraft.getInstance().world != null) {
            Minecraft.getInstance().displayGuiScreen(new AnchorScreen(packet.active));
        }
    }

    @Override
    public void handleSGuiStatusMessage(NetworkEvent.Context ctx, SGuiStatusMessage packet) {
        if(Minecraft.getInstance().currentScreen instanceof IStatusable) {
            ((IStatusable) Minecraft.getInstance().currentScreen).setStatus(packet.message);
        }
    }
}
