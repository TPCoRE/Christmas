package tpc.mc.christmas;

import java.util.Iterator;
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
import org.objectweb.asm.tree.LabelNode;
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
import net.minecraft.init.Blocks;
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
 * ���ֵ��ã�����MOD�ڷ�������У��������Ļ�������װ�Ϳ���(���ʽ)���ͻ���װҲ��
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
	 * �����Ƿ���ʥ����
	 * */
	public static final boolean christmas() {
		/*Calendar cal = Calendar.getInstance();
		if(cal.get(Calendar.MONTH) == 12 && cal.get(Calendar.HOUR_OF_DAY) == 25) return true;
		
		return false;*/
		
		return true; //TODO DEBUG ONLY
	}
	
	/**
	 * ��һ��ʵ�����ȡ�������Ȥ��ʵ��
	 * */
	public static final Entity crazyfor(EntityPig pig, Iterator<Entity> entities) {
		if(!christmas()) return null;
		Entity crazy = null;
		
		while(entities.hasNext()) {
			Entity e = entities.next();
			if(!pig.getEntitySenses().canSee(e) && !e.isEntityAlive()) continue;
			
			if(e instanceof EntityItem) { //�Ƿ���ʥ���ܲ�
				Item item = ((EntityItem) e).getEntityItem().getItem();
				
				if(item == Items.CARROT) {
					if(crazy == null) crazy = e;
					else if(crazy.getDistanceSqToEntity(pig) > e.getDistanceSqToEntity(pig)) crazy = e;
				}
			} else if(e instanceof EntityLivingBase) { //�������ŵĻ�������λ
				for(EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
					ItemStack stack = ((EntityLivingBase) e).getItemStackFromSlot(slot);
					
					if(stack != null && (stack.getItem() == Items.CARROT)) {
						if(crazy == null) crazy = e;
						else if(crazy.getDistanceSqToEntity(pig) > e.getDistanceSqToEntity(pig)) crazy = e;
					}
				}
			}
		}
		
		return crazy;
	}
	
	/**
	 * ����ϵ�ʵ������Ժ󣬷������Ƿ�����
	 * */
	public static final boolean crazyinteract(EntityPig pig, Entity poor) {
		if(!christmas()) return true;
		
		if(poor instanceof EntityItem) {
			Item i = ((EntityItem) poor).getEntityItem().getItem();
			
			if(i == Items.CARROT) {
				pig.onItemPickup(poor, 1);
				poor.setDead();
				return true;
			}
		} else if(!pig.isPassenger(poor)) poor.attackEntityFrom(DamageSource.causeMobDamage(pig), ((float) pig.getEntityBoundingBox().getAverageEdgeLength()) * 1.5F);
		
		return false;
	}
	
	/**
	 * ��ȡpig��Ŀ�꣬CodeRuler����
	 * */
	public static final Entity crazytarget(EntityPig pig) {
		throw new InternalError("Inline Method 'crazytarget(EntityPig)Entity' Failed!");
	}
	
	/**
	 * ����pig��Ŀ�꣬����
	 * */
	public static final void crazytarget(EntityPig pig, Entity target) {
		throw new InternalError("Inline Method 'crazytarget(EntityPig, Entity)Void' Failed!");
	}
	
	/**
	 * ��ȡpig��stared���������Ȥ, WORLD POS
	 * */
	public static final Vec3d crazypos(EntityPig pig, Entity stared) {
		return stared.getPositionVector(); //TODO & ���BUG
	}
	
	/**
	 * update a pig, server side
	 * */
	public static final void onPigUpdate(final EntityPig pig) {
		if(!pig.isServerWorld() || !christmas()) return; //ʥ����&�����only
		
		//prepare
		final WorldServer w = (WorldServer) pig.worldObj;
		final Random r = pig.getRNG();
		Entity stared = crazytarget(pig);
		
		//staredʰȡ
		if(stared == null) { //����״̬
			stared = Util.crazyfor(pig, w.getEntitiesWithinAABBExcludingEntity(pig, pig.getEntityBoundingBox().expandXyz(32D)).iterator());
			
			//�ж�����
			if(stared != null) {
				crazytarget(pig, stared);
				
				//��������Ч��
				w.spawnParticle(EnumParticleTypes.HEART, pig.posX + (double) (r.nextFloat() * pig.width * 2.0F) - (double) pig.width, pig.posY + 0.5D + (double) (r.nextFloat() * pig.height), pig.posZ + (double) (r.nextFloat() * pig.width * 2.0F) - (double) pig.width, 3 + r.nextInt(2), 0D, 0D, 0D, 0);
				
				//������Ч
				pig.playSound(SoundEvents.ENTITY_PIG_AMBIENT, 0.5F, 2F);
				pig.playSound(SoundEvents.ENTITY_ZOMBIE_PIG_AMBIENT, 2F, 16F);
			}
		} else { //stared�л�
			Entity crazy = Util.crazyfor(pig, w.getEntitiesWithinAABBExcludingEntity(pig, pig.getEntityBoundingBox().expandXyz(8D)).iterator());
			
			//������
			if(crazy != null && crazy != stared) {
				crazytarget(pig, stared = crazy);
				
				//������Ч
				pig.playSound(SoundEvents.ENTITY_ZOMBIE_PIG_AMBIENT, 1F, 4F);
			}
			
			//check alive, ŵ����
			if(!stared.isEntityAlive()) {
				crazytarget(pig, stared = null);
				pig.attackEntityFrom(DamageSource.causeMobDamage(pig), r.nextFloat() + pig.getHealth() * r.nextFloat());
				pig.func_189654_d(false);
				
				//����Ч��
				w.spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, pig.posX + (double) (r.nextFloat() * pig.width * 2.0F) - (double) pig.width, pig.posY + 0.5D + (double) (r.nextFloat() * pig.height), pig.posZ + (double) (r.nextFloat() * pig.width * 2.0F) - (double) pig.width, 10 + r.nextInt(5), 0D, 0D, 0D, 0);
				
				//������Ч
				pig.playSound(SoundEvents.ENTITY_PIG_DEATH, 2.7F, 32F);
			}
		}
		
		//׷��
		if(stared != null) {
			Vec3d toward = crazypos(pig, stared);
			Vec3d dir = toward.subtract(pig.getPositionVector()).normalize();
			
			//����
			Util.blocks(pig.getEntityBoundingBox().expandXyz(0.25D), new Predicate<BlockPos>() {
				
				@Override
				public boolean apply(BlockPos pos) {
					IBlockState state = w.getBlockState(pos);
					
					//����ײ��
					if(state.getCollisionBoundingBox(w, pos) != Block.NULL_AABB) {
						float hard = state.getBlockHardness(w, pos);
						
						//��
						if(0 <= hard && hard <= 2.5F) w.destroyBlock(pos, true);
						
						//����
						hard = Math.abs(hard) * 0.15F + r.nextFloat() * 0.1F;
						if(pig.getMaxHealth() > hard) pig.setHealth(pig.getMaxHealth() - hard);
						else pig.attackEntityFrom(DamageSource.flyIntoWall, hard);
					}
					
					return false;
				}
			});
			
			//׷������
			if(w.getEntitiesWithinAABBExcludingEntity(pig, pig.getEntityBoundingBox().expandXyz(0.25D)).contains(stared)) {
				if(Util.crazyinteract(pig, stared)) {
					crazytarget(pig, null);
					pig.heal(pig.getMaxHealth());
					pig.func_189654_d(false);
					
					//����Ч��
					w.spawnParticle(EnumParticleTypes.HEART, pig.posX + (double) (r.nextFloat() * pig.width * 2.0F) - (double) pig.width, pig.posY + 0.5D + (double) (r.nextFloat() * pig.height), pig.posZ + (double) (r.nextFloat() * pig.width * 2.0F) - (double) pig.width, 10 + r.nextInt(5), 0D, 0D, 0D, 0);
					
					//������Ч
					pig.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.4F, 2F);
				}
			}
			
			//ʧ��
			pig.func_189654_d(true);
			pig.fallDistance = 0F;
			
			//����ɣ����������ת
			pig.setVelocity(-dir.xCoord, dir.yCoord, -dir.zCoord);
			ProjectileHelper.rotateTowardsMovement(pig, 1F);
			pig.rotationYawHead = pig.prevRotationYawHead = pig.rotationYaw;
			pig.setVelocity(dir.xCoord, dir.yCoord, dir.zCoord);
		}
	}
	
	/**
	 * Internal
	 * */
	public static final class CodeRuler {
		
		/**
		 * ��classbuf��christmas�ı�׼�淶��
		 * */
		public static final byte[] codefix(byte[] classbuf) {
			ClassReader cr = new ClassReader(classbuf);
			ClassNode cn = new ClassNode();
			cr.accept(cn, 0);
			
			//special class found
			if(cn.name.equals("net/minecraft/entity/passive/EntityPig")) {
				MethodNode onLivingUpdate = null;
				boolean onLivingUpdate_exist = false;
				
				//roll all methods
				Iterator<MethodNode> mns = cn.methods.iterator();
				while(mns.hasNext()) {
					MethodNode mn = mns.next();
					
					//Found ?!
					if(mn.name.equals("onLivingUpdate") && mn.desc.equals("()V")) {
						onLivingUpdate = mn;
						onLivingUpdate_exist = true;
						
						//Only one, so break
						break;
					}
				}
				
				//prepare
				if(onLivingUpdate == null) onLivingUpdate = (MethodNode) cn.visitMethod(Opcodes.ACC_PUBLIC, "onLivingUpdate", "()V", null, null);
				InsnList ns;
				
				//coding onLivingUpdate
				ns = new InsnList();
				ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
				if(!onLivingUpdate_exist) { //special process
					ns.add(new InsnNode(Opcodes.DUP));
					ns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/entity/passive/EntityAnimal", "onLivingUpdate", "()V"));
					
					//prepare sub inject
					LabelNode l0 = new LabelNode();
					LabelNode l1 = new LabelNode();
					onLivingUpdate.visitLocalVariable("this", "Lnet/minecraft/entity/passive/EntityPig;", null, l0.getLabel(), l1.getLabel(), 0);
					
					//inject
					onLivingUpdate.instructions.add(l0);
					onLivingUpdate.instructions.add(new InsnNode(Opcodes.RETURN));
					onLivingUpdate.instructions.add(l1);
				} else;
				ns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tpc/mc/christmas/Util", "onPigUpdate", "(Lnet/minecraft/entity/passive/EntityPig;)V"));
				inject(ns, onLivingUpdate.instructions, new $P0(Opcodes.RETURN));
				
				//create fields
				cn.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "_internal_christmas_pig_crazytarget", "Lnet/minecraft/entity/Entity;", null, null));
			} else if(cn.name.equals("tpc/mc/christmas/Util")) { //����
				//roll all methods
				Iterator<MethodNode> mns = cn.methods.iterator();
				while(mns.hasNext()) {
					MethodNode mn = mns.next();
					
					if(mn.name.equals("crazytarget")) { //find
						InsnList ns = mn.instructions;
						ns.clear(); //��ֹ���
						
						if(mn.desc.equals("(Lnet/minecraft/entity/passive/EntityPig;)Lnet/minecraft/entity/Entity;")) { //getter
							ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
							ns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/passive/EntityPig", "_internal_christmas_pig_crazytarget", "Lnet/minecraft/entity/Entity;"));
							ns.add(new InsnNode(Opcodes.ARETURN));
						} else { //setter
							ns.add(new VarInsnNode(Opcodes.ALOAD, 0));
							ns.add(new VarInsnNode(Opcodes.ALOAD, 1));
							ns.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/passive/EntityPig", "_internal_christmas_pig_crazytarget", "Lnet/minecraft/entity/Entity;"));
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
		 * ��dest�е�����λ�ò���injected
		 * */
		public static final void inject(InsnList injected, InsnList dest, Predicate<AbstractInsnNode> proxy) {
			
			//roll all codes in dest
			for(int i = 0, l = dest.size(); i < l; ++i) {
				AbstractInsnNode n = dest.get(i);
				
				if(proxy.apply(n)) dest.insertBefore(n, copy(injected)); //find, then inject
			}
		}
		
		/**
		 * ����һ��InsnList
		 * */
		public static final InsnList copy(InsnList src) {
			InsnList r = new InsnList();
			Iterator<AbstractInsnNode> iter = src.iterator();
			
			while(iter.hasNext()) r.add(iter.next().clone(null));
			
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