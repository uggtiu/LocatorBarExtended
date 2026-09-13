package ru.uggtiu.extendedlocatorbar.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import ru.uggtiu.extendedlocatorbar.config.*;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ElbCommand {

    private static final SuggestionProvider<FabricClientCommandSource> PLAYER_SUGGESTIONS = (context, builder) -> {
        var networkHandler = context.getSource().getClient().getNetworkHandler();
        if (networkHandler != null) {
            for (PlayerListEntry entry : networkHandler.getPlayerList()) {
                builder.suggest(entry.getProfile().name());
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<FabricClientCommandSource> SCOPE_SUGGESTIONS = (context, builder) -> {
        builder.suggest("server");
        builder.suggest("global");
        return builder.buildFuture();
    };

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerCommands(dispatcher, "elb");
            registerCommands(dispatcher, "extendedlocatorbar");
        });
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, String rootName) {
        dispatcher.register(literal(rootName)
            .then(literal("reload").executes(ElbCommand::executeReload))
            .then(literal("gui").executes(ctx -> {
                var client = ctx.getSource().getClient();
                client.send(() -> client.setScreen(ru.uggtiu.extendedlocatorbar.gui.ElbConfigScreen.createScreen(client.currentScreen)));
                return 1;
            }))
            // Global branch
            .then(literal("global")
                .then(literal("mode")
                    .then(literal("head").executes(ctx -> setGlobalMode(ctx, RenderMode.HEAD)))
                    .then(literal("icon").executes(ctx -> setGlobalMode(ctx, RenderMode.ICON))))
                .then(literal("icon_color")
                    .then(argument("color", StringArgumentType.string())
                        .executes(ElbCommand::setGlobalIconColor)))
                .then(literal("clean_nicknames")
                    .then(argument("enabled", BoolArgumentType.bool())
                        .executes(ElbCommand::setGlobalCleanNicknames)))
                .then(literal("head_size")
                    .then(argument("size", FloatArgumentType.floatArg(4.0f, 32.0f))
                        .executes(ElbCommand::setGlobalHeadSize)))
                .then(literal("rounding")
                    .then(argument("percent", IntegerArgumentType.integer(0, 100))
                        .executes(ElbCommand::setGlobalRounding)))
                .then(literal("nicknames")
                    .then(argument("enabled", BoolArgumentType.bool())
                        .executes(ElbCommand::setGlobalNicknames)))
                .then(literal("enhanced")
                    .then(argument("enabled", BoolArgumentType.bool())
                        .executes(ElbCommand::setGlobalEnhanced)))
                .then(literal("scale")
                    .then(argument("scale", FloatArgumentType.floatArg(0.2f, 3.0f))
                        .executes(ElbCommand::setGlobalScale)))
                .then(literal("highlight")
                    .then(argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> setGlobalHighlight(ctx, null))
                        .then(argument("color", StringArgumentType.string())
                            .executes(ctx -> setGlobalHighlight(ctx, StringArgumentType.getString(ctx, "color"))))))
            )
            // Player-specific branch
            .then(literal("player")
                .then(argument("target", StringArgumentType.string())
                    .suggests(PLAYER_SUGGESTIONS)
                    .then(literal("mode")
                        .then(literal("head")
                            .executes(ctx -> setPlayerMode(ctx, RenderMode.HEAD, "server"))
                            .then(argument("scope", StringArgumentType.word())
                                .suggests(SCOPE_SUGGESTIONS)
                                .executes(ctx -> setPlayerMode(ctx, RenderMode.HEAD, StringArgumentType.getString(ctx, "scope")))))
                        .then(literal("icon")
                            .executes(ctx -> setPlayerMode(ctx, RenderMode.ICON, "server"))
                            .then(argument("scope", StringArgumentType.word())
                                .suggests(SCOPE_SUGGESTIONS)
                                .executes(ctx -> setPlayerMode(ctx, RenderMode.ICON, StringArgumentType.getString(ctx, "scope"))))))
                    .then(literal("icon_color")
                        .then(argument("color", StringArgumentType.string())
                            .executes(ctx -> setPlayerIconColor(ctx, "server"))
                            .then(argument("scope", StringArgumentType.word())
                                .suggests(SCOPE_SUGGESTIONS)
                                .executes(ctx -> setPlayerIconColor(ctx, StringArgumentType.getString(ctx, "scope"))))))
                    .then(literal("clean_nicknames")
                        .then(argument("enabled", BoolArgumentType.bool())
                            .executes(ctx -> setPlayerCleanNicknames(ctx, "server"))
                            .then(argument("scope", StringArgumentType.word())
                                .suggests(SCOPE_SUGGESTIONS)
                                .executes(ctx -> setPlayerCleanNicknames(ctx, StringArgumentType.getString(ctx, "scope"))))))
                    .then(literal("highlight")
                        .then(argument("enabled", BoolArgumentType.bool())
                            .executes(ctx -> setPlayerHighlight(ctx, null, "server"))
                            .then(argument("color", StringArgumentType.string())
                                .executes(ctx -> setPlayerHighlight(ctx, StringArgumentType.getString(ctx, "color"), "server"))
                                .then(argument("scope", StringArgumentType.word())
                                    .suggests(SCOPE_SUGGESTIONS)
                                    .executes(ctx -> setPlayerHighlight(ctx, StringArgumentType.getString(ctx, "color"), StringArgumentType.getString(ctx, "scope")))))))
                    .then(literal("rounding")
                        .then(argument("percent", IntegerArgumentType.integer(0, 100))
                            .executes(ctx -> setPlayerRounding(ctx, "server"))
                            .then(argument("scope", StringArgumentType.word())
                                .suggests(SCOPE_SUGGESTIONS)
                                .executes(ctx -> setPlayerRounding(ctx, StringArgumentType.getString(ctx, "scope"))))))
                    .then(literal("nickname_color")
                        .then(argument("color", StringArgumentType.string())
                            .executes(ctx -> setPlayerNickColor(ctx, "server"))
                            .then(argument("scope", StringArgumentType.word())
                                .suggests(SCOPE_SUGGESTIONS)
                                .executes(ctx -> setPlayerNickColor(ctx, StringArgumentType.getString(ctx, "scope"))))))
                    .then(literal("nickname_scale")
                        .then(argument("scale", FloatArgumentType.floatArg(0.2f, 3.0f))
                            .executes(ctx -> setPlayerNickScale(ctx, "server"))
                            .then(argument("scope", StringArgumentType.word())
                                .suggests(SCOPE_SUGGESTIONS)
                                .executes(ctx -> setPlayerNickScale(ctx, StringArgumentType.getString(ctx, "scope"))))))
                    .then(literal("reset")
                        .executes(ctx -> resetPlayer(ctx, "server"))
                        .then(argument("scope", StringArgumentType.word())
                            .suggests(SCOPE_SUGGESTIONS)
                            .executes(ctx -> resetPlayer(ctx, StringArgumentType.getString(ctx, "scope")))))
                )
            )
        );
    }

    private static int executeReload(CommandContext<FabricClientCommandSource> ctx) {
        ConfigManager.load();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.reload"));
        return 1;
    }

    // --- Global Actions ---

    private static int setGlobalMode(CommandContext<FabricClientCommandSource> ctx, RenderMode mode) {
        ConfigManager.get().global.renderMode = mode;
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.global.mode", Text.translatable(mode.getTranslationKey())));
        return 1;
    }

    private static int setGlobalCleanNicknames(CommandContext<FabricClientCommandSource> ctx) {
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        ConfigManager.get().global.cleanNicknames = enabled;
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.global.clean_nicknames", enabled));
        return 1;
    }

    private static int setGlobalIconColor(CommandContext<FabricClientCommandSource> ctx) {
        String colorStr = StringArgumentType.getString(ctx, "color");
        ConfigManager.get().global.defaultIconColor = parseColor(colorStr, 0xFF00FFCC);
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.global.icon_color"));
        return 1;
    }

    private static int setGlobalHeadSize(CommandContext<FabricClientCommandSource> ctx) {
        float size = FloatArgumentType.getFloat(ctx, "size");
        ConfigManager.get().global.headSize = size;
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.global.head_size", size));
        return 1;
    }

    private static int setGlobalRounding(CommandContext<FabricClientCommandSource> ctx) {
        int rounding = IntegerArgumentType.getInteger(ctx, "percent");
        ConfigManager.get().global.headRounding = rounding;
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.global.rounding", rounding));
        return 1;
    }

    private static int setGlobalNicknames(CommandContext<FabricClientCommandSource> ctx) {
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        ConfigManager.get().global.showNicknames = enabled;
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.global.nicknames", enabled));
        return 1;
    }

    private static int setGlobalEnhanced(CommandContext<FabricClientCommandSource> ctx) {
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        ConfigManager.get().global.enhancedNickname = enabled;
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.global.enhanced", enabled));
        return 1;
    }

    private static int setGlobalScale(CommandContext<FabricClientCommandSource> ctx) {
        float scale = FloatArgumentType.getFloat(ctx, "scale");
        ConfigManager.get().global.nicknameScale = scale;
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.global.scale", scale));
        return 1;
    }

    private static int setGlobalHighlight(CommandContext<FabricClientCommandSource> ctx, String colorStr) {
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        ConfigManager.get().global.highlight = enabled;
        if (colorStr != null) {
            ConfigManager.get().global.defaultHighlightColor = parseColor(colorStr, 0xFFFF0000);
        }
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.global.highlight", enabled));
        return 1;
    }

    // --- Per-Player Actions ---

    private static int setPlayerMode(CommandContext<FabricClientCommandSource> ctx, RenderMode mode, String scope) {
        String target = StringArgumentType.getString(ctx, "target");
        PlayerConfig cfg = getOrCreatePlayerConfig(target, scope);
        cfg.renderMode = mode;
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.player.mode", scope, target, Text.translatable(mode.getTranslationKey())));
        return 1;
    }

    private static int setPlayerCleanNicknames(CommandContext<FabricClientCommandSource> ctx, String scope) {
        String target = StringArgumentType.getString(ctx, "target");
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        PlayerConfig cfg = getOrCreatePlayerConfig(target, scope);
        cfg.cleanNicknames = enabled;
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.player.clean_nicknames", scope, target, enabled));
        return 1;
    }

    private static int setPlayerIconColor(CommandContext<FabricClientCommandSource> ctx, String scope) {
        String target = StringArgumentType.getString(ctx, "target");
        String colorStr = StringArgumentType.getString(ctx, "color");
        PlayerConfig cfg = getOrCreatePlayerConfig(target, scope);
        cfg.iconColor = parseColor(colorStr, 0xFF00FFCC);
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.player.icon_color", scope, target));
        return 1;
    }

    private static int setPlayerHighlight(CommandContext<FabricClientCommandSource> ctx, String colorStr, String scope) {
        String target = StringArgumentType.getString(ctx, "target");
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        PlayerConfig cfg = getOrCreatePlayerConfig(target, scope);
        cfg.highlight = enabled;
        if (colorStr != null) {
            cfg.highlightColor = parseColor(colorStr, 0xFFFF0000);
        }
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.player.highlight", scope, target, enabled));
        return 1;
    }

    private static int setPlayerRounding(CommandContext<FabricClientCommandSource> ctx, String scope) {
        String target = StringArgumentType.getString(ctx, "target");
        int rounding = IntegerArgumentType.getInteger(ctx, "percent");
        PlayerConfig cfg = getOrCreatePlayerConfig(target, scope);
        cfg.headRounding = rounding;
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.player.rounding", scope, target, rounding));
        return 1;
    }

    private static int setPlayerNickColor(CommandContext<FabricClientCommandSource> ctx, String scope) {
        String target = StringArgumentType.getString(ctx, "target");
        String colorStr = StringArgumentType.getString(ctx, "color");
        PlayerConfig cfg = getOrCreatePlayerConfig(target, scope);
        cfg.nicknameColor = parseColor(colorStr, 0xFFFFFFFF);
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.player.nickname_color", scope, target));
        return 1;
    }

    private static int setPlayerNickScale(CommandContext<FabricClientCommandSource> ctx, String scope) {
        String target = StringArgumentType.getString(ctx, "target");
        float scale = FloatArgumentType.getFloat(ctx, "scale");
        PlayerConfig cfg = getOrCreatePlayerConfig(target, scope);
        cfg.nicknameScale = scale;
        ConfigManager.save();
        ctx.getSource().sendFeedback(Text.translatable("elb.command.player.nickname_scale", scope, target, scale));
        return 1;
    }

    private static int resetPlayer(CommandContext<FabricClientCommandSource> ctx, String scope) {
        String target = StringArgumentType.getString(ctx, "target");
        String lowerTarget = target.toLowerCase();
        ModConfig cfg = ConfigManager.get();

        if ("global".equalsIgnoreCase(scope)) {
            PlayerConfig p = findInMap(cfg.players, lowerTarget);
            if (p != null) {
                p.renderMode = null;
                p.iconColor = null;
                p.headRounding = null;
                p.highlight = null;
                p.highlightColor = null;
                p.nicknameColor = null;
                p.nicknameScale = null;
                p.cleanNicknames = null;
                ConfigManager.save();
                ctx.getSource().sendFeedback(Text.translatable("elb.command.player.reset.global", target));
                return 1;
            }
        } else {
            String serverKey = ConfigManager.getCurrentServerKey();
            Map<String, PlayerConfig> serverMap = cfg.servers.get(serverKey);
            if (serverMap != null) {
                PlayerConfig p = removeFromMap(serverMap, lowerTarget);
                if (p != null) {
                    ConfigManager.save();
                    ctx.getSource().sendFeedback(Text.translatable("elb.command.player.reset.server", target));
                    return 1;
                }
            }
        }

        ctx.getSource().sendFeedback(Text.translatable("elb.command.player.reset.not_found", target, scope));
        return 1;
    }

    private static PlayerConfig findInMap(Map<String, PlayerConfig> map, String lowerTarget) {
        if (map == null) return null;
        if (map.containsKey(lowerTarget)) return map.get(lowerTarget);
        for (PlayerConfig p : map.values()) {
            if ((p.name != null && p.name.equalsIgnoreCase(lowerTarget)) ||
                (p.uuid != null && p.uuid.equalsIgnoreCase(lowerTarget))) {
                return p;
            }
        }
        return null;
    }

    private static PlayerConfig removeFromMap(Map<String, PlayerConfig> map, String lowerTarget) {
        if (map == null) return null;
        PlayerConfig p = map.remove(lowerTarget);
        if (p != null) return p;
        for (var it = map.entrySet().iterator(); it.hasNext(); ) {
            var entry = it.next();
            PlayerConfig val = entry.getValue();
            if ((val.name != null && val.name.equalsIgnoreCase(lowerTarget)) ||
                (val.uuid != null && val.uuid.equalsIgnoreCase(lowerTarget))) {
                it.remove();
                return val;
            }
        }
        return null;
    }

    // --- Helpers ---

    private static Map<String, PlayerConfig> getScopeMap(String scope) {
        ModConfig cfg = ConfigManager.get();
        if ("global".equalsIgnoreCase(scope)) {
            return cfg.players;
        } else {
            String serverKey = ConfigManager.getCurrentServerKey();
            return cfg.servers.computeIfAbsent(serverKey, k -> new LinkedHashMap<>());
        }
    }

    private static PlayerConfig getOrCreatePlayerConfig(String target, String scope) {
        String key = target.toLowerCase();
        Map<String, PlayerConfig> map = getScopeMap(scope);
        return map.computeIfAbsent(key, k -> new PlayerConfig(target, null));
    }

    private static int parseColor(String input, int defaultColor) {
        if (input == null || input.isBlank()) return defaultColor;
        String s = input.trim().toLowerCase();

        switch (s) {
            case "red": return 0xFFFF0000;
            case "green": return 0xFF00FF00;
            case "blue": return 0xFF0000FF;
            case "yellow": return 0xFFFFFF00;
            case "gold": return 0xFFFFAA00;
            case "aqua": case "cyan": return 0xFF00FFFF;
            case "purple": return 0xFFAA00AA;
            case "white": return 0xFFFFFFFF;
            case "black": return 0xFF000000;
        }

        if (s.startsWith("#")) s = s.substring(1);
        if (s.startsWith("0x")) s = s.substring(2);

        try {
            if (s.length() == 6) {
                return 0xFF000000 | (int) Long.parseLong(s, 16);
            } else if (s.length() == 8) {
                return (int) Long.parseLong(s, 16);
            }
        } catch (NumberFormatException ignored) {}

        return defaultColor;
    }
}