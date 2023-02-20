package pt.nobrehd.autopay.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import pt.nobrehd.autopay.Utils;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

@Environment(EnvType.CLIENT)
public class AutopayClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Utils.init();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(
            literal("autopay").then(
                literal("add").then(
                    argument("pattern", StringArgumentType.string()).then(
                        argument("delay", IntegerArgumentType.integer()).executes(ctx -> {
                            ClientPlayerEntity player = ctx.getSource().getPlayer();
                            if (MinecraftClient.getInstance().getCurrentServerEntry() != null){
                                String server = MinecraftClient.getInstance().getCurrentServerEntry().address;
                                String pattern = StringArgumentType.getString(ctx, "pattern");
                                int delay = IntegerArgumentType.getInteger(ctx, "delay");
                                Utils.addToDict(server, pattern, delay);
                                player.sendMessage(Utils.format("Config Added to " + server), false);
                            } else {
                                player.sendMessage(Utils.format("This command can only be used in a server"), false);
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
            ).then(
                literal("pay").then(
                    argument("amount", IntegerArgumentType.integer()).executes(ctx -> {
                        ClientPlayerEntity player = ctx.getSource().getPlayer();
                        if (MinecraftClient.getInstance().getCurrentServerEntry() != null){
                            String server = MinecraftClient.getInstance().getCurrentServerEntry().address;
                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                            new Thread(() -> {
                                String pattern = Utils.getPattern(server);
                                int delay = Utils.getDelay(server);
                                if (pattern != null && delay != 0) {
                                    for (String name: player.getServer().getPlayerNames()){
                                        String command = pattern.replace("%player%", name).replace("%amount%", String.valueOf(amount));
                                        player.sendChatMessage(command, null);
                                        try {
                                            Thread.sleep(delay);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    player.sendMessage(Utils.format("No config found for " + server), false);
                                }
                            }).start();
                        } else {
                            player.sendMessage(Utils.format("This command can only be used in a server"), false);
                        }
                        return Command.SINGLE_SUCCESS;
                    })
                )
            ).then(
                literal("reload").executes(ctx -> {
                    ClientPlayerEntity player = ctx.getSource().getPlayer();
                    Utils.reloadDict();
                    player.sendMessage(Utils.format("Config reloaded"), false);
                    return Command.SINGLE_SUCCESS;
                })
            ).then(
                literal("download").then(
                    argument("url", StringArgumentType.string()).then(
                        argument("overwrite", BoolArgumentType.bool()).executes(ctx -> {
                            ClientPlayerEntity player = ctx.getSource().getPlayer();
                            String url = StringArgumentType.getString(ctx, "url");
                            boolean overwrite = BoolArgumentType.getBool(ctx, "overwrite");
                            Utils.downloadDict(url, overwrite);
                            player.sendMessage(Utils.format("Config downloaded"), false);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
            ).then(
                    literal("open").executes(ctx -> {
                        ClientPlayerEntity player = ctx.getSource().getPlayer();
                        Utils.openConfig();
                        player.sendMessage(Utils.format("Config opened"), false);
                        return Command.SINGLE_SUCCESS;
                    }
                )
            ).then(
                literal("help").executes(ctx -> {
                    ClientPlayerEntity player = ctx.getSource().getPlayer();
                    player.sendMessage(Text.literal("§6====== Autopay Help ======"), false);
                    player.sendMessage(Text.literal("§6/autopay §radd §4<pattern> §9<delay>"), false);
                    player.sendMessage(Text.literal("§6/autopay §rpay §9<amount>"), false);
                    player.sendMessage(Text.literal("§6/autopay §rreload"), false);
                    player.sendMessage(Text.literal("§6/autopay §rdownload §4<url> §a<overwrite>"), false);
                    player.sendMessage(Text.literal("§6/autopay §ropen"), false);
                    player.sendMessage(Text.empty(), false);
                    return Command.SINGLE_SUCCESS;
                })
            )
        ));
    }
}
