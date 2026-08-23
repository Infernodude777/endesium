package com.infernodude777.endesium.item;

import com.infernodude777.endesium.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The Void Sword. Applies Slowness on hit; hold use for five seconds while
 * wearing the full Void set to tear a singularity out of the End sky.
 *
 * <p>Charge lifecycle notes (these matter): vanilla calls {@code onUseTick}
 * with the remaining count BEFORE decrementing it and expires the use when it
 * reaches zero without ever calling back into the item, so a threshold of
 * {@code >= CHARGE_TICKS} is unreachable by exactly one tick. The trigger here
 * therefore fires at {@code CHARGE_TICKS - 1}, and {@link #finishUsingItem}
 * backs up the vanilla auto-complete path. Releasing early simply cancels.
 */
public final class VoidSwordItem extends SwordItem {
    public static final int CHARGE_TICKS = 60;
    public static final int COOLDOWN_TICKS = 6000;

    public VoidSwordItem(Tier t, Properties p){ super(t,p); }

    /** 0..1 charge fraction for the client HUD; clamps past-full overhang. */
    public static float chargeProgress(int remainingTicks){
        return Math.max(0.0F, Math.min(1.0F, (CHARGE_TICKS - remainingTicks) / (float) CHARGE_TICKS));
    }

    @Override public UseAnim getUseAnimation(ItemStack s){ return UseAnim.BLOCK; }

    @Override public int getUseDuration(ItemStack s, LivingEntity e){ return CHARGE_TICKS; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level l, net.minecraft.world.entity.player.Player p, InteractionHand h){
        ItemStack s=p.getItemInHand(h);
        if(h!=InteractionHand.MAIN_HAND||!p.getMainHandItem().is(ModItems.VOID_SWORD))
            return InteractionResultHolder.fail(s);
        if(!VoidEquipmentAbilities.isFullVoidArmor(p)){
            if(!l.isClientSide()) p.displayClientMessage(Component.literal("The full Void set must answer before the sword can charge").withStyle(ChatFormatting.DARK_GRAY), true);
            return InteractionResultHolder.fail(s);
        }
        if(!l.isClientSide()) p.displayClientMessage(Component.literal("The sword drinks from the deep...").withStyle(ChatFormatting.DARK_PURPLE), true);
        p.startUsingItem(h);
        return InteractionResultHolder.consume(s);
    }

    @Override
    public void onUseTick(Level l, LivingEntity e, ItemStack s, int rem){
        if(l.isClientSide()||!(e instanceof ServerPlayer p)) return;
        if(!VoidEquipmentAbilities.isFullVoidArmor(p)||!p.getMainHandItem().is(ModItems.VOID_SWORD)){
            p.stopUsingItem();
            return;
        }
        int ch=getUseDuration(s,p)-rem;
        // Charge feedback: motes spiral in as completion approaches.
        if(ch>0&&ch%10==0) VoidBlackHoleManager.emitChargeParticles(p, ch/(float)CHARGE_TICKS);
        // One tick before vanilla expiry: the only tick this can actually fire.
        if(ch>=CHARGE_TICKS-1){
            fire(p);
            p.stopUsingItem();
        }
    }

    /** Late release: a nearly-finished charge still fires instead of wasting. */
    @Override
    public void releaseUsing(ItemStack s, Level l, LivingEntity e, int timeLeft){
        if(l.isClientSide()||!(e instanceof ServerPlayer p)) return;
        int used=getUseDuration(s,p)-timeLeft;
        if(!VoidEquipmentAbilities.isFullVoidArmor(p)||!p.getMainHandItem().is(ModItems.VOID_SWORD)) return;
        if(used>=CHARGE_TICKS*4/5){
            fire(p);
        } else if(used>8&&!p.getCooldowns().isOnCooldown(ModItems.VOID_SWORD)){
            p.displayClientMessage(Component.literal("The singularity dissipates.").withStyle(ChatFormatting.DARK_GRAY), true);
        }
    }

    /** Safety net for the server-side auto-complete path at duration end. */
    @Override
    public ItemStack finishUsingItem(ItemStack s, Level l, LivingEntity e){
        if(!l.isClientSide()&&e instanceof ServerPlayer p
                &&VoidEquipmentAbilities.isFullVoidArmor(p)
                &&p.getMainHandItem().is(ModItems.VOID_SWORD)){
            fire(p);
        }
        return s;
    }

    /** Spawns the singularity once, applying the correct cooldown either way. */
    private static void fire(ServerPlayer p){
        if(p.getCooldowns().isOnCooldown(ModItems.VOID_SWORD)) return;
        boolean ok=VoidBlackHoleManager.trySpawn(p);
        p.getCooldowns().addCooldown(ModItems.VOID_SWORD,
                ok ? COOLDOWN_TICKS : 60);
        if(ok){
            ServerLevel level=p.serverLevel();
            level.playSound(null, p.blockPosition(), SoundEvents.PORTAL_TRIGGER,
                    SoundSource.PLAYERS, 0.9F, 0.55F);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack s, LivingEntity t, LivingEntity a){
        boolean d=super.hurtEnemy(s,t,a);
        if(!t.level().isClientSide()&&d&&!t.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN)) t.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,30,0,false,true,true));
        return d;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag){
        tooltip.add(Component.literal("Hold use in the full Void set to charge")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("a singularity that drags everything near.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
