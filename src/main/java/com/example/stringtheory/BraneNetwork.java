package com.example.stringtheory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/** Список всех настроенных якорей по модам струн (1..5), хранится вместе с миром. */
public class BraneNetwork extends SavedData {
    private static final String NAME = "stringtheory_branes";

    private final Map<Integer, List<GlobalPos>> anchors = new HashMap<>();

    public static BraneNetwork get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(BraneNetwork::new, BraneNetwork::load, null), NAME);
    }

    public List<GlobalPos> get(int mode) {
        return anchors.getOrDefault(mode, List.of());
    }

    public void add(int mode, GlobalPos pos) {
        List<GlobalPos> list = anchors.computeIfAbsent(mode, m -> new ArrayList<>());
        if (!list.contains(pos)) {
            list.add(pos);
            setDirty();
        }
    }

    public void remove(int mode, GlobalPos pos) {
        List<GlobalPos> list = anchors.get(mode);
        if (list != null && list.remove(pos)) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag out = new ListTag();
        for (Map.Entry<Integer, List<GlobalPos>> entry : anchors.entrySet()) {
            for (GlobalPos pos : entry.getValue()) {
                CompoundTag e = new CompoundTag();
                e.putInt("mode", entry.getKey());
                e.putString("dim", pos.dimension().location().toString());
                e.putInt("x", pos.pos().getX());
                e.putInt("y", pos.pos().getY());
                e.putInt("z", pos.pos().getZ());
                out.add(e);
            }
        }
        tag.put("anchors", out);
        return tag;
    }

    public static BraneNetwork load(CompoundTag tag, HolderLookup.Provider registries) {
        BraneNetwork network = new BraneNetwork();
        ListTag list = tag.getList("anchors", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(e.getString("dim")));
            network.anchors.computeIfAbsent(e.getInt("mode"), m -> new ArrayList<>())
                    .add(GlobalPos.of(dim, new BlockPos(e.getInt("x"), e.getInt("y"), e.getInt("z"))));
        }
        return network;
    }
}
