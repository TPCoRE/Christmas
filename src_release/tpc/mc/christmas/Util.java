package tpc.mc.christmas;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

/**
 * 各种调用，整个MOD在服务端运行，服务器的话服务器装就可以(插件式)，客户端装也行
 * */
public final class Util {
	
	/**
	 * handle blocks
	 * */
	public static final void blocks(AxisAlignedBB bb, Predicate<BlockPos> proxy) {
		int i = MathHelper.floor_double(bb.minX);
        int j = MathHelper.ceiling_double_int(bb.maxX);
        int k = MathHelper.floor_double(bb.minY);
        int l = MathHelper.ceiling_double_int(bb.maxY);
        int i1 = MathHelper.floor_double(bb.minZ);
        int j1 = MathHelper.ceiling_double_int(bb.maxZ);
        
        for(int k1 = i; k1 < j; ++k1) {
            for(int l1 = k; l1 < l; ++l1) {
                for(int i2 = i1; i2 < j1; ++i2) {
                    if(proxy.apply(new BlockPos(k1, l1, i2))) return;
                }
            }
        }
	}
	
	/**
	 * 今天是否是圣诞节
	 * */
	public static final boolean christmas() {
		Calendar cal = Calendar.getInstance();
		if(cal.get(Calendar.MONTH) == 12 && cal.get(Calendar.HOUR_OF_DAY) == 25) return true;
		
		return false;
	}
	
	/**
	 * 在一堆实体里获取疯猪感兴趣的实体
	 * */
	public static final Entity crazyfor(EntityPig pig, Iterator<Entity> entities) {
		if(!christmas()) return null;
		Entity crazy = null;
		
		while(entities.hasNext()) {
			Entity e = entities.next();
			if(!pig.getEntitySenses().canSee(e) && !e.isEntityAlive()) continue;
			
			if(e instanceof EntityItem) { //是否是圣诞萝卜
				Item item = ((EntityItem) e).getEntityItem().getItem();
				
				if(item == Items.CARROT || item == Items.CARROT_ON_A_STICK) {
					if(crazy == null) crazy = e;
					else if(crazy.getDistanceSqToEntity(pig) > e.getDistanceSqToEntity(pig)) crazy = e;
				}
			} else if(e instanceof EntityLivingBase) { //手上拿着的或其他部位
				for(EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
					ItemStack stack = ((EntityLivingBase) e).getItemStackFromSlot(slot);
					
					if(stack != null && (stack.getItem() == Items.CARROT || stack.getItem() == Items.CARROT_ON_A_STICK)) {
						if(crazy == null) crazy = e;
						else if(crazy.getDistanceSqToEntity(pig) > e.getDistanceSqToEntity(pig)) crazy = e;
					}
				}
			}
		}
		
		return crazy;
	}
	
	/**
	 * 疯猪赶到实体边上以后，返回猪是否满足
	 * */
	public static final boolean crazyinteract(EntityPig pig, Entity poor) {
		if(!christmas()) return true;
		
		if(poor instanceof EntityItem) {
			Item i = ((EntityItem) poor).getEntityItem().getItem();
			
			if(i == Items.CARROT || i == Items.CARROT_ON_A_STICK) {
				pig.onItemPickup(poor, 1);
				poor.setDead();
				
				//返还钓鱼竿
				if(i == Items.CARROT_ON_A_STICK) {
					pig.worldObj.spawnEntityInWorld(new EntityItem(pig.worldObj, poor.posX, poor.posY, poor.posZ, new ItemStack(Items.FISHING_ROD, 1)));
				}
				
				return true;
			}
		} else if(!pig.isPassenger(poor)) poor.attackEntityFrom(DamageSource.causeMobDamage(pig), ((float) pig.getEntityBoundingBox().getAverageEdgeLength()) * 1.5F);
		
		return false;
	}
	
	/**
	 * 获取pig的目标，CodeRuler内联
	 * */
	public static final Entity crazytarget(EntityPig pig) {
		throw new InternalError("Inline Method 'crazytarget(EntityPig)Entity' Failed!");
	}
	
	/**
	 * 设置pig的目标，内联
	 * */
	public static final void crazytarget(EntityPig pig, Entity target) {
		throw new InternalError("Inline Method 'crazytarget(EntityPig, Entity)Void' Failed!");
	}
	
	/**
	 * 获取pig对stared的哪里感兴趣, WORLD POS
	 * */
	public static final Vec3d crazypos(EntityPig pig, Entity stared) {
		Vec3d pos = stared.getPositionEyes(1F);
		
		//特殊处理
		if(stared instanceof EntityLivingBase) {
			ItemStack held0 = ((EntityLivingBase) stared).getHeldItemMainhand();
			ItemStack held1 = ((EntityLivingBase) stared).getHeldItemOffhand();
			
			//手持萝卜杆子，需要调整pos向前，让猪追
			if((held0 != null && held0.getItem() == Items.CARROT_ON_A_STICK) || (held1 != null && held1.getItem() == Items.CARROT_ON_A_STICK)) {
				pos = pos.add(stared.getLookVec().scale(1.5D));
			}
		}
		
		return pos;
	}
	
	/**
	 * update a pig, server side
	 * */
	public static final void onPigUpdate(final EntityPig pig) {
		if(!pig.isServerWorld() || !christmas()) return; //圣诞节&服务端only
		
		//prepare
		final WorldServer w = (WorldServer) pig.worldObj;
		final Random r = pig.getRNG();
		Entity stared = crazytarget(pig);
		
		//stared拾取
		if(stared == null) { //空闲状态
			stared = Util.crazyfor(pig, w.getEntitiesWithinAABBExcludingEntity(pig, pig.getEntityBoundingBox().expandXyz(32D)).iterator());
			
			//有东西了
			if(stared != null) {
				crazytarget(pig, stared);
				
				//生成粒子效果
				w.spawnParticle(EnumParticleTypes.HEART, pig.posX + (double) (r.nextFloat() * pig.width * 2.0F) - (double) pig.width, pig.posY + 0.5D + (double) (r.nextFloat() * pig.height), pig.posZ + (double) (r.nextFloat() * pig.width * 2.0F) - (double) pig.width, 3 + r.nextInt(2), 0D, 0D, 0D, 0);
				
				//播放音效
				pig.playSound(SoundEvents.ENTITY_PIG_AMBIENT, 0.5F, 2F);
				pig.playSound(SoundEvents.ENTITY_ZOMBIE_PIG_AMBIENT, 2F, 16F);
			}
		} else { //stared切换
			Entity crazy = Util.crazyfor(pig, w.getEntitiesWithinAABBExcludingEntity(pig, pig.getEntityBoundingBox().expandXyz(8D)).iterator());
			
			//更近的
			if(crazy != null && crazy != stared) {
				crazytarget(pig, stared = crazy);
				
				//播放音效
				pig.playSound(SoundEvents.ENTITY_ZOMBIE_PIG_AMBIENT, 1F, 4F);
			}
			
			//check alive, 诺死掉
			if(!stared.isEntityAlive()) {
				crazytarget(pig, stared = null);
				pig.attackEntityFrom(DamageSource.causeMobDamage(pig), r.nextFloat() + pig.getHealth() * r.nextFloat());
				pig.func_189654_d(false);
				
				//粒子效果
				w.spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, pig.posX + (double) (r.nextFloat() * pig.width * 2.0F) - (double) pig.width, pig.posY + 0.5D + (double) (r.nextFloat() * pig.height), pig.posZ + (double) (r.nextFloat() * pig.width * 2.0F) - (double) pig.width, 10 + r.nextInt(5), 0D, 0D, 0D, 0);
				
				//播放音效
				pig.playSound(SoundEvents.ENTITY_PIG_DEATH, 2.7F, 32F);
			}
		}
		
		//追逐
		if(stared != null) {
			Vec3d toward = crazypos(pig, stared);
			Vec3d v = toward.subtract(pig.getPositionEyes(1F)).normalize().scale(0.23D);
			
			//失重
			pig.func_189654_d(true);
			pig.fallDistance = 0F;
			
			//让猪飞，调整猪的旋转
			pig.setVelocity(-v.xCoord, v.yCoord, -v.zCoord);
			ProjectileHelper.rotateTowardsMovement(pig, 1F);
			pig.rotationYawHead = pig.prevRotationYawHead = pig.rotationYaw;
			pig.setVelocity(v.xCoord, v.yCoord, v.zCoord);
			pig.moveEntity(v.xCoord, v.yCoord, v.zCoord);
			
			//拱！
			Util.blocks(pig.getEntityBoundingBox().expandXyz(0.25D), new Predicate<BlockPos>() {
				
				@Override
				public boolean apply(BlockPos pos) {
					IBlockState state = w.getBlockState(pos);
					
					//可碰撞的
					if(state.getCollisionBoundingBox(w, pos) != Block.NULL_AABB) {
						float hard = state.getBlockHardness(w, pos);
						
						//怼掉
						if(0 <= hard && hard <= 2.5F) w.destroyBlock(pos, true);
						
						//受伤
						hard = Math.abs(hard) * 0.15F + r.nextFloat() * 0.12F;
						if(pig.getHealth() > hard) pig.setHealth(pig.getHealth() - hard);
						else pig.attackEntityFrom(DamageSource.flyIntoWall, hard);
					}
					
					return false;
				}
			});
			
			//追到互交
			if(w.getEntitiesWithinAABBExcludingEntity(pig, pig.getEntityBoundingBox().expandXyz(0.25D)).contains(stared)) {
				if(Util.crazyinteract(pig, stared)) {
					crazytarget(pig, null);
					pig.heal(pig.getMaxHealth());
					pig.func_189654_d(false);
					
					//粒子效果
					w.spawnParticle(EnumParticleTypes.HEART, pig.posX + (double) (r.nextFloat() * pig.width * 2.0F) - (double) pig.width, pig.posY + 0.5D + (double) (r.nextFloat() * pig.height), pig.posZ + (double) (r.nextFloat() * pig.width * 2.0F) - (double) pig.width, 10 + r.nextInt(5), 0D, 0D, 0D, 0);
					
					//播放音效
					pig.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.4F, 2F);
				}
			}
		}
	}
	
	/**
	 * Internal
	 * */
	public static final class CodeRuler {
		
		/**
		 * 将classbuf以christmas的标准规范化
		 * */
		public static final byte[] codefix(byte[] classbuf) {
			ClassReader cr = new ClassReader(classbuf);
			ClassNode cn = new ClassNode();
			cr.accept(cn, 0);
			
			//CL: net/minecraft/entity/item/EntityBoat -> aap
			//CL: net/minecraft/entity/Entity -> rw
			//CL: net/minecraft/entity/passive/EntityPig -> wi
			//CL: net/minecraft/entity/passive/EntityAnimal -> wc
			//MD: net/minecraft/entity/Entity/onUpdate -> m
			//MD: net/minecraft/entity/Entity/func_189654_d -> d
			//MD: net/minecraft/entity/EntityLivingBase/onLivingUpdate -> n
			
			//special class found
			if(cn.name.equals("aap")) {
				MethodNode onUpdate = null;
				boolean onUpdate_exist = false;
				
				//roll all methods
				Iterator<MethodNode> mns = cn.methods.iterator();
				while(mns.hasNext()) {
					MethodNode mn = mns.next();
					
					//Found ?!
					if(mn.name.equals("m") && mn.desc.equals("()V")) {
						onUpdate = mn;
						onUpdate_exist = true;
						
						//Only one, so break
						break;
					}
				}
				
				//prepare
				if(onUpdate == null) onUpdate = (MethodNode) cn.visitMethod(Opcodes.ACC_PUBLIC, "m", "()V", null, null);
				InsnList ns;
				
				//coding onUpdate
				ns = new InsnList();
				ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
				ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/christmas/Util", "christmas", "()Z"));
				ns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "aap", "d", "(Z)V"));
				onUpdate.instructions.insert(copy(ns));
				if(!onUpdate_exist) {
					onUpdate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
					onUpdate.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "rw", "m", "()V"));
					onUpdate.instructions.add(new InsnNode(Opcodes.RETURN));
				} else;
			} else if(cn.name.equals("wi")) {
				MethodNode onLivingUpdate = null;
				boolean onLivingUpdate_exist = false;
				
				//roll all methods
				Iterator<MethodNode> mns = cn.methods.iterator();
				while(mns.hasNext()) {
					MethodNode mn = mns.next();
					
					//Found ?!
					if(mn.name.equals("n") && mn.desc.equals("()V")) {
						onLivingUpdate = mn;
						onLivingUpdate_exist = true;
						
						//Only one, so break
						break;
					}
				}
				
				//prepare
				if(onLivingUpdate == null) onLivingUpdate = (MethodNode) cn.visitMethod(Opcodes.ACC_PUBLIC, "n", "()V", null, null);
				InsnList ns;
				
				//coding onLivingUpdate
				ns = new InsnList();
				ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
				if(!onLivingUpdate_exist) { //special process
					ns.add(new InsnNode(Opcodes.DUP));
					ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "wc", "n", "()V"));
					
					//prepare sub inject
					LabelNode l0 = new LabelNode();
					LabelNode l1 = new LabelNode();
					onLivingUpdate.visitLocalVariable("this", "Lwi;", null, l0.getLabel(), l1.getLabel(), 0);
					
					//inject
					onLivingUpdate.instructions.add(l0);
					onLivingUpdate.instructions.add(new InsnNode(Opcodes.RETURN));
					onLivingUpdate.instructions.add(l1);
				} else;
				ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/christmas/Util", "onPigUpdate", "(Lwi;)V"));
				inject(ns, onLivingUpdate.instructions, new $P0(Opcodes.RETURN));
				
				//create fields
				cn.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "_internal_christmas_pig_crazytarget", "Lrw;", null, null));
			} else if(cn.name.equals("tpc/mc/christmas/Util")) { //内联
				//roll all methods
				Iterator<MethodNode> mns = cn.methods.iterator();
				while(mns.hasNext()) {
					MethodNode mn = mns.next();
					
					if(mn.name.equals("crazytarget")) { //find
						InsnList ns = mn.instructions;
						ns.clear(); //防止喷错
						
						if(mn.desc.equals("(Lwi;)Lrw;")) { //getter
							ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
							ns.add(new FieldInsnNode(Opcodes.GETFIELD, "wi", "_internal_christmas_pig_crazytarget", "Lrw;"));
							ns.add(new InsnNode(Opcodes.ARETURN));
						} else { //setter
							ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
							ns.add(new VarInsnNode(Opcodes.ALOAD, 1));
							ns.add(new FieldInsnNode(Opcodes.PUTFIELD, "wi", "_internal_christmas_pig_crazytarget", "Lrw;"));
							ns.add(new InsnNode(Opcodes.RETURN));
						}
					}
				}
			}
			
			//write down
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			cn.accept(cw);
			return cw.toByteArray();
		}
		
		/**
		 * 在dest中的特殊位置插入injected
		 * */
		public static final void inject(InsnList injected, InsnList dest, Predicate<AbstractInsnNode> proxy) {
			
			//roll all codes in dest
			for(int i = 0, l = dest.size(); i < l; ++i) {
				AbstractInsnNode n = dest.get(i);
				
				if(proxy.apply(n)) dest.insertBefore(n, copy(injected)); //find, then inject
			}
		}
		
		/**
		 * 复制一个InsnList
		 * */
		public static final InsnList copy(InsnList src) {
			InsnList r = new InsnList();
			Map<LabelNode, LabelNode> map = new HashMap<>();
			Iterator<AbstractInsnNode> iter;
			
			//roll codes and find labels
			iter = src.iterator();
			while(iter.hasNext()) {
				AbstractInsnNode n = iter.next();
				
				if(n.getType() == AbstractInsnNode.LABEL && !map.containsKey(n)) map.put((LabelNode) n, new LabelNode());
			}
			
			//clone
			iter = src.iterator();
			while(iter.hasNext()) r.add(iter.next().clone(map));
			
			return r;
		}
		
		/**
		 * Internal Only
		 * */
		private static class $P0 implements Predicate<AbstractInsnNode> {
			
			private final int OP;
			private $P0(int op) { OP = op; }
			
			@Override
			public boolean apply(AbstractInsnNode n) {
				return n.getOpcode() == OP;
			}
		}
	}
}