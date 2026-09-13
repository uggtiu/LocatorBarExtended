package ru.uggtiu.locatorbarextended.gui;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import ru.uggtiu.locatorbarextended.config.*;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

public class BleConfigScreen {

    public static Screen createScreen(Screen parent) {
        ModConfig config = ConfigManager.get();
        GlobalConfig global = config.global;
        String currentServerKey = ConfigManager.getCurrentServerKey();

        // 1. General Settings
        ConfigCategory.Builder generalCategory = ConfigCategory.createBuilder()
                .name(Text.translatable("ble.category.general"))
                .tooltip(Text.translatable("ble.category.general.tooltip"));

        generalCategory.option(Option.<RenderMode>createBuilder()
                .name(Text.translatable("ble.mode"))
                .description(OptionDescription.of(Text.translatable("ble.mode.desc")))
                .binding(RenderMode.HEAD,
                        () -> global.renderMode == RenderMode.DEFAULT ? RenderMode.HEAD : global.renderMode,
                        val -> global.renderMode = (val == RenderMode.DEFAULT ? RenderMode.HEAD : val))
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(RenderMode.class)
                        .formatValue(val -> Text.translatable(val.getTranslationKey())))
                .build());

        generalCategory.option(Option.<Boolean>createBuilder()
                .name(Text.translatable("ble.clean_nicknames"))
                .description(OptionDescription.of(Text.translatable("ble.clean_nicknames.desc")))
                .binding(false, () -> global.cleanNicknames, val -> global.cleanNicknames = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        generalCategory.option(Option.<Float>createBuilder()
                .name(Text.translatable("ble.head_size"))
                .binding(9.0f, () -> global.headSize, val -> global.headSize = val)
                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(4.0f, 24.0f).step(0.5f))
                .build());

        generalCategory.option(Option.<Integer>createBuilder()
                .name(Text.translatable("ble.head_rounding"))
                .binding(0, () -> global.headRounding, val -> global.headRounding = val)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(0, 100).step(5))
                .build());

        generalCategory.option(Option.<Boolean>createBuilder()
                .name(Text.translatable("ble.show_nicknames"))
                .binding(true, () -> global.showNicknames, val -> global.showNicknames = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        generalCategory.option(Option.<Boolean>createBuilder()
                .name(Text.translatable("ble.enhanced_nickname"))
                .description(OptionDescription.of(Text.translatable("ble.enhanced_nickname.desc")))
                .binding(true, () -> global.enhancedNickname, val -> global.enhancedNickname = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        generalCategory.option(Option.<Float>createBuilder()
                .name(Text.translatable("ble.nickname_scale"))
                .binding(1.0f, () -> global.nicknameScale, val -> global.nicknameScale = val)
                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.5f, 2.0f).step(0.1f))
                .build());

        generalCategory.option(Option.<Boolean>createBuilder()
                .name(Text.translatable("ble.highlight.global"))
                .description(OptionDescription.of(Text.translatable("ble.highlight.global.desc")))
                .binding(false, () -> global.highlight, val -> global.highlight = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        generalCategory.option(Option.<Color>createBuilder()
                .name(Text.translatable("ble.highlight_color.default"))
                .binding(new Color(global.defaultHighlightColor, true),
                        () -> new Color(global.defaultHighlightColor, true),
                        color -> global.defaultHighlightColor = color.getRGB())
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                .build());

        generalCategory.option(Option.<Boolean>createBuilder()
                .name(Text.translatable("ble.auto_remember"))
                .description(OptionDescription.of(Text.translatable("ble.auto_remember.desc")))
                .binding(true, () -> global.autoRememberPlayers, val -> global.autoRememberPlayers = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        // 2. Global Players Category
        ConfigCategory.Builder globalPlayersCategory = ConfigCategory.createBuilder()
                .name(Text.translatable("ble.category.players.global"))
                .tooltip(Text.translatable("ble.category.players.global.tooltip"));

        if (config.players.isEmpty()) {
            globalPlayersCategory.option(LabelOption.create(Text.translatable("ble.empty_list.global")));
        } else {
            for (Map.Entry<String, PlayerConfig> entry : config.players.entrySet()) {
                String key = entry.getKey();
                PlayerConfig p = entry.getValue();
                String displayName = p.name != null ? p.name : key;
                globalPlayersCategory.group(createPlayerGroup(displayName, p, global, parent));
            }
        }

        // 3. Server-Specific Players Category
        ConfigCategory.Builder serverPlayersCategory = ConfigCategory.createBuilder()
                .name(Text.translatable("ble.category.players.server"))
                .tooltip(Text.translatable("ble.category.players.server.tooltip", currentServerKey));

        Map<String, PlayerConfig> serverPlayers = config.servers.computeIfAbsent(currentServerKey, k -> new LinkedHashMap<>());

        if (serverPlayers.isEmpty()) {
            serverPlayersCategory.option(LabelOption.create(Text.translatable("ble.empty_list.server", currentServerKey)));
        } else {
            for (Map.Entry<String, PlayerConfig> entry : serverPlayers.entrySet()) {
                String key = entry.getKey();
                PlayerConfig p = entry.getValue();
                String displayName = p.name != null ? p.name : key;
                serverPlayersCategory.group(createPlayerGroup(displayName, p, global, parent));
            }
        }

        // 4. Danger / Fun Category
        ConfigCategory.Builder dangerCategory = ConfigCategory.createBuilder()
                .name(Text.translatable("ble.category.danger"))
                .tooltip(Text.translatable("ble.category.danger.tooltip"));

        // Universal color for all icons
        dangerCategory.option(Option.<Boolean>createBuilder()
                .name(Text.translatable("ble.danger.universal_color"))
                .description(OptionDescription.of(Text.translatable("ble.danger.universal_color.desc")))
                .binding(false, () -> global.universalIconColorEnabled, val -> global.universalIconColorEnabled = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        dangerCategory.option(Option.<Color>createBuilder()
                .name(Text.translatable("ble.danger.universal_color_picker"))
                .binding(new Color(global.universalIconColor, true),
                        () -> new Color(global.universalIconColor, true),
                        col -> global.universalIconColor = col.getRGB())
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                .build());

        // Rainbow icons
        dangerCategory.option(Option.<Boolean>createBuilder()
                .name(Text.translatable("ble.danger.rainbow"))
                .description(OptionDescription.of(Text.translatable("ble.danger.rainbow.desc")))
                .binding(false, () -> global.rainbowIcons, val -> global.rainbowIcons = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        dangerCategory.option(Option.<Float>createBuilder()
                .name(Text.translatable("ble.danger.rainbow_speed"))
                .binding(1.0f, () -> global.rainbowSpeed, val -> global.rainbowSpeed = val)
                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.2f, 5.0f).step(0.2f))
                .build());

        // In-world Overlay (Locator Bar but not locator bar)
        dangerCategory.option(Option.<Boolean>createBuilder()
                .name(Text.translatable("ble.danger.overlay"))
                .description(OptionDescription.of(Text.translatable("ble.danger.overlay.desc")))
                .binding(false, () -> global.inWorldOverlayEnabled, val -> global.inWorldOverlayEnabled = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        dangerCategory.option(Option.<Double>createBuilder()
                .name(Text.translatable("ble.danger.overlay_distance"))
                .description(OptionDescription.of(Text.translatable("ble.danger.overlay_distance.desc")))
                .binding(16.0, () -> global.inWorldDistanceThreshold, val -> global.inWorldDistanceThreshold = val)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(4.0, 128.0).step(2.0))
                .build());

        dangerCategory.option(Option.<Boolean>createBuilder()
                .name(Text.translatable("ble.danger.overlay_hide_bar"))
                .description(OptionDescription.of(Text.translatable("ble.danger.overlay_hide_bar.desc")))
                .binding(true, () -> global.hideFromBarWhenOverlayed, val -> global.hideFromBarWhenOverlayed = val)
                .controller(TickBoxControllerBuilder::create)
                .build());

        dangerCategory.option(Option.<Float>createBuilder()
                .name(Text.translatable("ble.danger.overlay_head_size"))
                .binding(14.0f, () -> global.inWorldHeadSize, val -> global.inWorldHeadSize = val)
                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(8.0f, 32.0f).step(1.0f))
                .build());

        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("ble.title"))
                .category(generalCategory.build())
                .category(globalPlayersCategory.build())
                .category(serverPlayersCategory.build())
                .category(dangerCategory.build())
                .save(ConfigManager::save)
                .build()
                .generateScreen(parent);
    }

    private static OptionGroup createPlayerGroup(String displayName, PlayerConfig p, GlobalConfig global, Screen parentScreen) {
        OptionGroup.Builder playerGroup = OptionGroup.createBuilder()
                .name(Text.translatable("ble.player.group_title", displayName))
                .description(OptionDescription.of(Text.literal(displayName + (p.uuid != null ? " (" + p.uuid + ")" : ""))));

        playerGroup.option(Option.<RenderMode>createBuilder()
                .name(Text.translatable("ble.mode"))
                .binding(RenderMode.DEFAULT,
                        () -> p.renderMode != null ? p.renderMode : RenderMode.DEFAULT,
                        val -> p.renderMode = (val == RenderMode.DEFAULT ? null : val))
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(RenderMode.class)
                        .formatValue(val -> Text.translatable(val.getTranslationKey())))
                .build());

        playerGroup.option(Option.<TriState>createBuilder()
                .name(Text.translatable("ble.clean_nicknames"))
                .binding(TriState.DEFAULT,
                        () -> p.cleanNicknames == null ? TriState.DEFAULT : (p.cleanNicknames ? TriState.ENABLED : TriState.DISABLED),
                        val -> p.cleanNicknames = (val == TriState.DEFAULT ? null : (val == TriState.ENABLED)))
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(TriState.class)
                        .formatValue(val -> Text.translatable(val.getTranslationKey())))
                .build());

        playerGroup.option(Option.<TriState>createBuilder()
                .name(Text.translatable("ble.highlight"))
                .binding(TriState.DEFAULT,
                        () -> p.highlight == null ? TriState.DEFAULT : (p.highlight ? TriState.ENABLED : TriState.DISABLED),
                        val -> p.highlight = (val == TriState.DEFAULT ? null : (val == TriState.ENABLED)))
                .controller(opt -> EnumControllerBuilder.create(opt)
                        .enumClass(TriState.class)
                        .formatValue(val -> Text.translatable(val.getTranslationKey())))
                .build());

        playerGroup.option(Option.<Color>createBuilder()
                .name(Text.translatable("ble.highlight_color"))
                .binding(new Color(p.highlightColor != null ? p.highlightColor : global.defaultHighlightColor, true),
                        () -> new Color(p.highlightColor != null ? p.highlightColor : global.defaultHighlightColor, true),
                        col -> p.highlightColor = col.getRGB())
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                .build());

        playerGroup.option(Option.<Color>createBuilder()
                .name(Text.translatable("ble.icon_color"))
                .binding(new Color(p.iconColor != null ? p.iconColor : 0xFF00FFCC, true),
                        () -> new Color(p.iconColor != null ? p.iconColor : 0xFF00FFCC, true),
                        col -> p.iconColor = col.getRGB())
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                .build());

        playerGroup.option(Option.<Color>createBuilder()
                .name(Text.translatable("ble.nickname_color"))
                .binding(new Color(p.nicknameColor != null ? p.nicknameColor : 0xFFFFFFFF, true),
                        () -> new Color(p.nicknameColor != null ? p.nicknameColor : 0xFFFFFFFF, true),
                        col -> p.nicknameColor = col.getRGB())
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                .build());

        playerGroup.option(Option.<Integer>createBuilder()
                .name(Text.translatable("ble.head_rounding"))
                .description(OptionDescription.of(Text.translatable("ble.head_rounding.desc_player", String.valueOf(global.headRounding))))
                .binding(-1,
                        () -> p.headRounding != null ? p.headRounding : -1,
                        val -> p.headRounding = (val < 0 ? null : val))
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(-1, 100).step(5))
                .build());

        playerGroup.option(Option.<Float>createBuilder()
                .name(Text.translatable("ble.nickname_scale"))
                .description(OptionDescription.of(Text.translatable("ble.nickname_scale.desc_player", String.format("%.1f", global.nicknameScale))))
                .binding(0.0f,
                        () -> p.nicknameScale != null ? p.nicknameScale : 0.0f,
                        val -> p.nicknameScale = (val <= 0.05f ? null : val))
                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.0f, 2.0f).step(0.1f))
                .build());

        playerGroup.option(ButtonOption.createBuilder()
                .name(Text.translatable("ble.player.reset_button"))
                .description(OptionDescription.of(Text.translatable("ble.player.reset_button.desc")))
                .action((screen, btn) -> {
                    p.renderMode = null;
                    p.iconColor = null;
                    p.headRounding = null;
                    p.highlight = null;
                    p.highlightColor = null;
                    p.nicknameColor = null;
                    p.nicknameScale = null;
                    p.cleanNicknames = null;
                    ConfigManager.save();
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(createScreen(parentScreen));
                })
                .build());

        return playerGroup.build();
    }
}