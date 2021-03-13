package com.kotori316.limiter.gui.config;

import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import com.google.gson.JsonObject;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.SpawnReason;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.kotori316.limiter.TestSpawn;
import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.conditions.DimensionLimit;
import com.kotori316.limiter.conditions.EntityClassificationLimit;
import com.kotori316.limiter.conditions.EntityLimit;
import com.kotori316.limiter.conditions.PositionLimit;
import com.kotori316.limiter.conditions.SpawnReasonLimit;

public class LMSConfigGui {

    @OnlyIn(Dist.CLIENT)
    public static void openConfig(LMSHandler handler) {
        TypePage typePage = new TypePage(handler);
        typePage.openGui();
    }

    public static ConfigGroup createConfigGroup(JsonObject save, TestSpawn.Serializer<?> serializer) {
        ConfigGroup group = new ConfigGroup(serializer.getType());
        if (serializer.equals(DimensionLimit.SERIALIZER) || serializer.equals(EntityLimit.SERIALIZER)) {
            addStringConfig(save, group, serializer);
        } else if (serializer.equals(EntityClassificationLimit.SERIALIZER)) {
            addClassificationConfig(save, group, serializer);
        } else if (serializer.equals(SpawnReasonLimit.SERIALIZER)) {
            addReasonConfig(save, group, serializer);
        } else if (serializer.equals(PositionLimit.SERIALIZER)) {
            addPositionConfig(save, group, serializer);
        }
        return group;
    }

    private static void addStringConfig(JsonObject save, ConfigGroup group, TestSpawn.Serializer<?> serializer) {
        for (String key : serializer.propertyKeys()) {
            group.addString(key, "", s -> save.addProperty(key, s), "", Pattern.compile("([a-z0-9._-]+)|([a-z0-9._/-]+:[a-z0-9._-]+)"));
        }
    }

    private static void addClassificationConfig(JsonObject save, ConfigGroup group, TestSpawn.Serializer<?> serializer) {
        for (String key : serializer.propertyKeys()) {
            EntityClassification value = EntityClassification.values()[0];
            group.addEnum(key, value, e -> save.addProperty(key, e.getString()), NameMap.of(value, EntityClassification.values()).create());
        }
    }

    private static void addReasonConfig(JsonObject save, ConfigGroup group, TestSpawn.Serializer<?> serializer) {
        for (String key : serializer.propertyKeys()) {
            SpawnReason value = SpawnReason.values()[0];
            group.addEnum(key, value, e -> save.addProperty(key, e.name().toLowerCase(Locale.ROOT)), NameMap.of(value, SpawnReason.values()).create());
        }
    }

    private static void addPositionConfig(JsonObject save, ConfigGroup group, TestSpawn.Serializer<?> serializer) {
        int limit = 3_000_000;
        serializer.propertyKeys().stream().sorted(Comparator.naturalOrder())
            .forEach(s -> {
                int defaultValue = s.equals("maxY") ? 256 : 0;
                group.addInt(s, defaultValue, i -> save.addProperty(s, i), defaultValue, -limit, limit);
            });
    }
}
