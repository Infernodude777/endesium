package com.infernodude777.endesium.item;
import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import java.util.ArrayList; import java.util.Iterator; import java.util.List; import java.util.UUID;
public final class VoidBlackHoleManager {
    public static final int LIFETIME_TICKS = 160;
    private static final double PULL_RADIUS = 14.0D;
    private static final double CORE_RADIUS = 1.4D;
    private static final double BLAST_RADIUS = 12.0D;
    private static final float EXPLOSION_DAMAGE = 18.0F;
    private static final float CORE_DAMAGE = 5.0F;
    private static final List<ActiveBlackHole> ACTIVE = new ArrayList<>();
    private VoidBlackHoleManager() {}
    public static boolean trySpawn(ServerPlayer p) {
        if (!VoidEquipmentAbilities.isFullVoidArmor(p) || !p.getMainHandItem().is(com.infernodude777.endesium.registry.ModItems.VOID_SWORD)) return false;
        if (p.getCooldowns().isOnCooldown(com.infernodude777.endesium.registry.ModItems.VOID_SWORD)) return false;
        for (ActiveBlackHole h : ACTIVE) if (h.owner.equals(p.getUUID()) && h.level == p.serverLevel()) return false;
        Vec3 eye = p.getEyePosition(); Vec3 look = p.getLookAngle().normalize(); Vec3 center = eye.add(look.scale(6.5D));
        ServerLevel l = p.serverLevel(); BlockPos probe = BlockPos.containing(center);
        if (!l.getBlockState(probe).isAir() && !l.getBlockState(probe).canBeReplaced()) center = eye.add(look.scale(4.0D));
        ActiveBlackHole hole = new ActiveBlackHole(l, p.getUUID(), center); ACTIVE.add(hole);
        try { Display.BlockDisplay d = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, l); d.setPos(center.x, center.y, center.z); d.setBlockState(Blocks.BLACK_CONCRETE.defaultBlockState()); d.setBillboardConstraints(Display.BillboardConstraints.CENTER); d.setTransformation(new com.mojang.math.Transformation(new Vector3f(-0.75f,-0.75f,-0.75f), null, new Vector3f(1.5f,1.5f,1.5f), null)); d.setInvulnerable(true); l.addFreshEntity(d); hole.display = d; } catch(Exception e){ Endesium.LOGGER.warn("Black hole display failed: {}", e.getMessage()); }
        l.playSound(null, BlockPos.containing(center), ModSounds.RESONANCE_STRIKE, SoundSource.PLAYERS, 1.2F, 0.3F);
        l.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 48, 0.6D,0.6D,0.6D,0.18D);
        l.sendParticles(ParticleTypes.PORTAL, center.x, center.y, center.z, 32, 0.8D,0.8D,0.8D,0.1D);
        return true;
    }
    public static void tick(MinecraftServer s){ Iterator<ActiveBlackHole> it=ACTIVE.iterator(); while(it.hasNext()){ ActiveBlackHole h=it.next(); if(h.level.getServer()!=s){it.remove(); continue;} if(h.age>=LIFETIME_TICKS){h.detonate(); it.remove(); continue;} h.tick(); } }
    public static void emitChargeParticles(ServerPlayer p,float prog){ ServerLevel l=p.serverLevel(); Vec3 c=p.getEyePosition().add(p.getLookAngle().scale(0.9D)); int cnt=2+(int)(prog*6.0F); double sp=0.1D+prog*0.18D; l.sendParticles(ParticleTypes.REVERSE_PORTAL,c.x,c.y,c.z,cnt,sp,sp,sp,0.05D); if(prog>0.7F) l.sendParticles(ParticleTypes.PORTAL,c.x,c.y,c.z,3,0.15D,0.15D,0.15D,0.06D); }
    public static int getActiveCount(){ return ACTIVE.size(); }
    public static void clear(){ for(ActiveBlackHole h:ACTIVE) if(h.display!=null&&h.display.isAlive()) h.display.discard(); ACTIVE.clear(); }
    private static final class ActiveBlackHole {
        final ServerLevel level; final UUID owner; final Vec3 center; int age; Display.BlockDisplay display;
        ActiveBlackHole(ServerLevel l, UUID o, Vec3 c){ level=l; owner=o; center=c; }
        void tick(){ age++; if(display!=null&&display.isAlive()){ display.setPos(center.x,center.y,center.z); float s=1.5f+(float)Math.sin(age*(0.2f+age*0.0025f))*0.15f; display.setTransformation(new com.mojang.math.Transformation(new Vector3f(-s/2,-s/2,-s/2),null,new Vector3f(s,s,s),null)); }
            AABB area=new AABB(center,center).inflate(PULL_RADIUS); for(Entity e:level.getEntities((Entity)null,area,c->c.isAlive())){ if(e instanceof LivingEntity le && VoidEquipmentAbilities.isProtectedFromBlackHole(le)) continue; Vec3 off=center.subtract(e.position().add(0,e.getBbHeight()*0.5D,0)); double d=off.length(); if(d<0.45D||d>PULL_RADIUS) continue; double t=1.0D-d/PULL_RADIUS; double str=0.07D+0.55D*t*t; Vec3 pull=off.normalize().scale(str); if(d<5.0D){ Vec3 tan=new Vec3(-off.z,0,off.x).normalize().scale(str*0.3D); pull=pull.add(tan); } e.setDeltaMovement(e.getDeltaMovement().add(pull)); e.hurtMarked=true; }
            if(age%12==0){ AABB core=new AABB(center,center).inflate(CORE_RADIUS); for(Entity e:level.getEntities((Entity)null,core,c->c instanceof LivingEntity le&&le.isAlive())){ if(e instanceof LivingEntity le&&!VoidEquipmentAbilities.isProtectedFromBlackHole(le)){ le.hurt(level.damageSources().magic(), CORE_DAMAGE); le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,40,2)); } } }
            if(age%2==0){ double rad=1.8D+Math.sin(age*0.25D)*0.15D; for(int i=0;i<6;i++){ double ang=age*0.35D+i*Math.PI*2/6; double x=center.x+Math.cos(ang)*rad; double z=center.z+Math.sin(ang)*rad; double y=center.y+Math.sin(age*0.18D+i)*0.25D; level.sendParticles(ParticleTypes.PORTAL,x,y,z,1,0,0,0,0.02D); } level.sendParticles(ModParticles.VOID_SKIRT_MOTE,center.x,center.y,center.z,6,0.7D,0.7D,0.7D,0.03D); }
            if(age%40==0) level.playSound(null, BlockPos.containing(center), ModSounds.RESONANCE_STRIKE, SoundSource.HOSTILE, 0.5F, 0.2F+age*0.002F);
        }
        void detonate(){ if(display!=null&&display.isAlive()) display.discard(); level.sendParticles(ParticleTypes.REVERSE_PORTAL,center.x,center.y,center.z,64,1.2D,1.2D,1.2D,0.22D); level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,center.x,center.y,center.z,1,0,0,0,0); level.playSound(null,BlockPos.containing(center),ModSounds.RESONANCE_STRIKE,SoundSource.HOSTILE,1.4F,0.5F); AABB blast=new AABB(center,center).inflate(BLAST_RADIUS); for(Entity e:level.getEntities((Entity)null,blast,c->c instanceof LivingEntity le&&le.isAlive())){ if(e instanceof LivingEntity le&&!VoidEquipmentAbilities.isProtectedFromBlackHole(le)){ double dist=Math.sqrt(le.distanceToSqr(center.x,center.y,center.z)); float dmg=(float)(EXPLOSION_DAMAGE*(1.0D-dist/BLAST_RADIUS)); if(dmg>1.0F) le.hurt(level.damageSources().explosion((LivingEntity)null,null),dmg); le.setDeltaMovement(le.getDeltaMovement().add(0.0D,0.4D,0.0D)); le.hurtMarked=true; } } }
    }
}
