package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.containers.vault.AccountTransactionContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionBuyContainer;
import github.pitbox46.monetamoney.containers.vault.AuctionListItemContainer;
import github.pitbox46.monetamoney.data.*;
import github.pitbox46.monetamoney.items.Coin;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SGuiStatusMessage;
import github.pitbox46.monetamoney.network.server.SSyncFeesPacket;
import github.pitbox46.monetamoney.network.server.SUpdateBalance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import java.util.function.Function;

public class CTransactionButton implements IPacket {
    public int amount;
    public Button button;

    public CTransactionButton() {}

    public CTransactionButton(int amount, Button button) {
        this.amount = amount;
        this.button = button;
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf) {
        this.amount = buf.readInt();
        this.button = buf.readEnum(Button.class);
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeInt(this.amount);
        buf.writeEnum(this.button);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        if(ctx.getSender() != null) {
            String player = ctx.getSender().getGameProfile().getName();

            if(ctx.getSender().containerMenu instanceof AccountTransactionContainer) {
                AccountTransactionContainer container = (AccountTransactionContainer) ctx.getSender().containerMenu;
                long balance = Ledger.readBalance(Ledger.jsonFile, player);
                switch(this.button) {
                    case WITHDRAW: {
                        if (this.amount > Coin.MAX_SIZE || this.amount <= 0) {
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.invalidsize")));
                        } else if (balance >= this.amount) {
                            Ledger.addBalance(Ledger.jsonFile, player, -this.amount);
                            ItemStack coins = Coin.createCoin(this.amount, Outstanding.newCoin(Outstanding.jsonFile, this.amount, player));
                            if (container.handler.getStackInSlot(0).isEmpty()) {
                                container.handler.setStackInSlot(0, coins);
                            } else {
                                ctx.getSender().getInventory().placeItemBackInInventory(coins);
                            }
                        } else {
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.nomoney")));
                        }
                    } break;
                    case DEPOSIT: {
                        if (container.handler.getStackInSlot(0).getItem() instanceof Coin) {
                            ItemStack coins = container.handler.getStackInSlot(0);
                            if (coins.getOrCreateTag().hasUUID("uuid") && Outstanding.redeemCoin(Outstanding.jsonFile, player, coins.getTag().getUUID("uuid"))) {
                                container.handler.setStackInSlot(0, ItemStack.EMPTY);
                            } else {
                                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.invalidcoin")));
                            }
                        } else {
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.invaliditem")));
                        }
                    } break;
                }
            }
            else if (ctx.getSender().containerMenu instanceof AuctionBuyContainer) {
                if(this.button == CTransactionButton.Button.PURCHASE) {
                    AuctionBuyContainer container = (AuctionBuyContainer) ctx.getSender().containerMenu;
                    CompoundTag itemNBT = container.handler.getStackInSlot(0).getOrCreateTag();
                    int price = itemNBT.getInt("price");
                    if (!Auctioned.confirmListing(Auctioned.auctionedNBT, itemNBT)) {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.listingerror")));
                    }
                    else if (Ledger.readBalance(Ledger.jsonFile, player) >= price) {
                        Ledger.addBalance(Ledger.jsonFile, player, -price);

                        CompoundTag removedTagNBT = container.handler.getStackInSlot(0).save(new CompoundTag());
                        removedTagNBT.getCompound("tag").remove("uuid");
                        removedTagNBT.getCompound("tag").remove("owner");
                        removedTagNBT.getCompound("tag").remove("price");
                        if (removedTagNBT.getCompound("tag").isEmpty()) {
                            removedTagNBT.remove("tag");
                        }
                        ctx.getSender().getInventory().placeItemBackInInventory(ItemStack.of(removedTagNBT));
                        String owner = itemNBT.getString("owner");
                        if (!owner.equals("shop listing")) {
                            Ledger.addBalance(Ledger.jsonFile, owner, price);
                            Auctioned.deleteListing(Auctioned.auctionedNBT, itemNBT.getUUID("uuid"));
                            container.handler.setStackInSlot(0, ItemStack.EMPTY);
                        }
                    }
                    else {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.nomoney")));
                    }
                }
                else if(this.button == CTransactionButton.Button.REMOVE) {
                    AuctionBuyContainer container = (AuctionBuyContainer) ctx.getSender().containerMenu;
                    CompoundTag itemNBT = container.handler.getStackInSlot(0).getOrCreateTag();
                    int price = itemNBT.getInt("price");
                    if (!Auctioned.confirmListing(Auctioned.auctionedNBT, itemNBT)) {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.listingerror")));
                    }
                    else if(!Auctioned.confirmOwner(Auctioned.auctionedNBT, itemNBT, player)) {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.notowner")));
                    }
                    else {
                        CompoundTag removedTagNBT = container.handler.getStackInSlot(0).save(new CompoundTag());
                        removedTagNBT.getCompound("tag").remove("uuid");
                        removedTagNBT.getCompound("tag").remove("owner");
                        removedTagNBT.getCompound("tag").remove("price");
                        if (removedTagNBT.getCompound("tag").isEmpty()) {
                            removedTagNBT.remove("tag");
                        }
                        ctx.getSender().getInventory().placeItemBackInInventory(ItemStack.of(removedTagNBT));
                        Auctioned.deleteListing(Auctioned.auctionedNBT, itemNBT.getUUID("uuid"));
                        container.handler.setStackInSlot(0, ItemStack.EMPTY);
                    }
                }
            }
            else if (ctx.getSender().containerMenu instanceof AuctionListItemContainer) {
                if(this.button == CTransactionButton.Button.LIST_ITEM) {
                    AuctionListItemContainer container = (AuctionListItemContainer) ctx.getSender().containerMenu;
                    if(container.handler.getStackInSlot(0).isEmpty() || container.handler.getStackInSlot(0).getItem().getClass() == Coin.class) {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.invaliditem")));
                    } else {
                        ListTag auctionList = (ListTag) Auctioned.auctionedNBT.get("auction");
                        assert auctionList != null;

                        int items = auctionList.stream().mapToInt((inbt) -> {
                            CompoundTag nbt = ((CompoundTag) inbt);
                            if(nbt.getString("owner").equals(player)) {
                                return 1;
                            }
                            return 0;
                        }).sum();

                        long price = ServerEvents.calculateListCost(items);

                        if(Ledger.readBalance(Ledger.jsonFile, player) >= price) {
                            Auctioned.addListing(Auctioned.auctionedNBT, container.handler.getStackInSlot(0), this.amount, player);
                            container.handler.setStackInSlot(0, ItemStack.EMPTY);
                            Ledger.addBalance(Ledger.jsonFile, player, -price);
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SSyncFeesPacket(ServerEvents.calculateListCost(items + 1), ServerEvents.calculateDailyListCost(items + 1) * (items + 1)));
                        } else {
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslatableComponent("message.monetamoney.nomoney")));
                        }
                    }
                }
            }
            Team team = Teams.getPlayersTeam(Teams.jsonFile, player);
            int chunks = ServerEvents.CHUNK_MAP.containsKey(team.toString()) ? (int) ServerEvents.CHUNK_MAP.get(team.toString()).stream().filter(c -> c.status == ChunkLoader.Status.ON || c.status == ChunkLoader.Status.STUCK).count() : 0;
            long dailyChunkFee = ServerEvents.calculateChunksCost(chunks) * chunks;

            ListTag auctionList = (ListTag) Auctioned.auctionedNBT.get("auction");
            assert auctionList != null;

            int listings = (int) auctionList.stream().filter(inbt -> ((CompoundTag) inbt).getString("owner").equals(player)).count();

            long dailyListingFee = ServerEvents.calculateDailyListCost(listings) * listings;

            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SUpdateBalance(Ledger.readBalance(Ledger.jsonFile, player), team.balance, dailyChunkFee, dailyListingFee));
        }
    }

    public static Function<FriendlyByteBuf, CTransactionButton> decoder() {
        return pb -> {
            CTransactionButton packet = new CTransactionButton();
            packet.readPacketData(pb);
            return packet;
        };
    }

    public enum Button {
        DEPOSIT,
        WITHDRAW,
        PURCHASE,
        REMOVE,
        LIST_ITEM,
    }
}
