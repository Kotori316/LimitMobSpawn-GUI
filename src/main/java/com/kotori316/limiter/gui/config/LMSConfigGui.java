package com.kotori316.limiter.gui.config;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.kotori316.limiter.TestSpawn;
import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.conditions.PositionLimit;
import com.kotori316.limiter.conditions.StringLimitSerializer;

public class LMSConfigGui {

    @OnlyIn(Dist.CLIENT)
    public static void openConfig(LMSHandler handler) {
        TypePage typePage = new TypePage(handler);
        typePage.openGui();
    }

    public static ConfigGroup createConfigGroup(JsonObject save, TestSpawn.Serializer<?> serializer) {
        ConfigGroup group = new ConfigGroup(serializer.getType());
        if (serializer.equals(PositionLimit.SERIALIZER)) {
            addPositionConfig(save, group, serializer);
        } else {
            addSinglePropertyConfig(save, group, serializer);
        }
        return group;
    }

    private static void addStringConfig(JsonObject save, ConfigGroup group, TestSpawn.Serializer<?> serializer) {
        for (String key : serializer.propertyKeys()) {
            group.addString(key, "", s -> save.addProperty(key, s), "", Pattern.compile("([a-z0-9._-]+)|([a-z0-9._/-]+:[a-z0-9._-]+)"));
        }
    }

    @SuppressWarnings({"ConstantConditions"})
    private static void addSinglePropertyConfig(JsonObject save, ConfigGroup group, TestSpawn.Serializer<?> serializer) {
        ClientSuggestionProvider provider = Minecraft.getInstance().player.connection.getSuggestionProvider();
        for (String key : serializer.propertyKeys()) {
            Set<String> possibleValues = serializer.possibleValues(key, true, provider);

            if (possibleValues.size() > 0) {
                if (serializer instanceof StringLimitSerializer<?, ?>) {
                    StringLimitSerializer<?, ?> sLS = (StringLimitSerializer<?, ?>) serializer;
                    List<?> list = possibleValues
                        .stream()
                        .map(sLS::fromString)
                        .collect(Collectors.toList());
                    if (list.get(0).getClass().isEnum()) {
                        List<Enum<?>> values = Arrays.asList(((Enum<?>) list.get(0)).getDeclaringClass().getEnumConstants());
                        group.addEnum(key, values.get(0), e -> save.addProperty(key, e.name().toLowerCase(Locale.ROOT)), NameMap.of(values.get(0), values).create());
                        continue;
                    }
                }
                List<String> values = possibleValues.stream().sorted().collect(Collectors.toList());
                group.addEnum(key, "", e -> save.addProperty(key, e), NameMap.of("", values).create());
            }
        }
    }

    private static void addPositionConfig(JsonObject save, ConfigGroup group, TestSpawn.Serializer<?> serializer) {
        int limit = 30_000_000;
        serializer.propertyKeys().stream().sorted(Comparator.naturalOrder())
            .forEach(s -> {
                int defaultValue = s.equals("maxY") ? 256 : 0;
                int limitMax = s.contains("Y") ? 256 : limit;
                int limitMin = s.contains("Y") ? 0 : -limit;
                group.addInt(s, defaultValue, i -> save.addProperty(s, i), defaultValue, limitMin, limitMax);
            });
    }
}
