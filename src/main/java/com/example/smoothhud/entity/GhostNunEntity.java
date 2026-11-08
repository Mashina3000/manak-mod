package com.example.smoothhud.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Призрачная монашка рядом с маньяком. Существует только на клиенте владельца. */
public final class GhostNunEntity extends Entity {

    private Player owner;

    public GhostNunEntity(EntityType<?> type, Level level) {
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
        if (owner == null || owner.isRemoved()) {
            discard();
            return;
        }
        // Стоит справа от маньяка на полшага, смотрит туда же
        double yaw = Math.toRadians(owner.getYRot() + 90.0D);
        setPos(owner.getX() + Math.cos(yaw) * 1.1D, owner.getY(), owner.getZ() + Math.sin(yaw) * 1.1D);
        setYRot(owner.getYRot());
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
