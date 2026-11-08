package com.example.smoothhud.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Летающий глаз маньяка: парит рядом с владельцем и исчезает через 10 секунд. */
public final class FlyingEyeEntity extends Entity {

    private static final int LIFE_TICKS = 200;

    private int age;
    private Player owner;

    public FlyingEyeEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwner(Player player) {
        this.owner = player;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (age > LIFE_TICKS || owner == null || owner.isRemoved()) {
            discard();
            return;
        }
        double a = age * 0.08D;
        double ox = Math.cos(a) * 1.3D;
        double oz = Math.sin(a) * 1.3D;
        double oy = 2.4D + Math.sin(age * 0.15D) * 0.15D;
        setPos(owner.getX() + ox, owner.getY() + oy, owner.getZ() + oz);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}
