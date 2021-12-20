package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.containers.vault.AuctionBuyContainer;
import github.pitbox46.monetamoney.data.Auctioned;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SSyncAuctionNBT;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import java.util.function.Function;

public class COpenBuyPage implements IPacket {
    public CompoundTag nbt;

    public COpenBuyPage() {}

    public COpenBuyPage(CompoundTag nbt) {
        this.nbt = nbt;
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf) {
        this.nbt = buf.readNbt();
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeNbt(this.nbt);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        if(ctx.getSender() == null) return;
        CompoundTag nbt = this.nbt;
        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncAuctionNBT(Auctioned.auctionedNBT));
        NetworkHooks.openGui(ctx.getSender(), new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return new TranslatableComponent("screen.monetamoney.auctionbuy");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                return new AuctionBuyContainer(id, inv, nbt);
            }
        }, buf -> buf.writeNbt(nbt));
    }

    public static Function<FriendlyByteBuf, COpenBuyPage> decoder() {
        return pb -> {
            COpenBuyPage packet = new COpenBuyPage();
            packet.readPacketData(pb);
            return packet;
        };
    }
}
