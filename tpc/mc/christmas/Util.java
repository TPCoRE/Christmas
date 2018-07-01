package tpc.mc.christmas;

import java.util.*;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import com.google.common.base.*;

import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.passive.*;
import net.minecraft.init.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

/**
 * 各种调用
 * */
public final class Util {
	
	/**
	 * 在一堆实体里获取疯猪感兴趣的实体
	 * */
	public static final Entity pigCrazy(EntityPig pig, Iterator<Entity> entities) {
		if(!christmas()) return null;
		Entity crazy = null;
		
		while(entities.hasNext()) {
			Entity e = entities.next();
			if(!pig.getEntitySenses().canSee(e) && !e.isEntityAlive()) continue;
			
			if(e instanceof EntityItem) { //是否是圣诞萝卜
				Item item = ((EntityItem) e).getEntityItem().getItem();
				
				if(item == Items.CARROT) {
					if(crazy == null) crazy = e;
					else if(crazy.getDistanceSqToEntity(pig) > e.getDistanceSqToEntity(pig)) crazy = e;
				}
			} else if(e instanceof EntityLivingBase) { //手上拿着的或其他部位
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
	 * 疯猪赶到实体边上以后，返回猪是否满足
	 * */
	public static final boolean crazyPigInter(EntityPig pig, Entity poor) {
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
		/*Calendar cal = Calendar.getInstance();
		if(cal.get(Calendar.MONTH) == 12 && cal.get(Calendar.HOUR_OF_DAY) == 25) return true;
		
		return false;*/
		
		return true; //TODO DEBUG ONLY
	}
	
	/**
	 * update a pig
	 * */
	public static final void onPigUpdate(EntityPig pig) {
		if(!christmas()) return;
		
		//prepare
		final World w = pig.worldObj;
		final boolean server = pig.isServerWorld();
		Entity stared = w.getEntityByID(pig.getDataManager().get(PIG_STARED)); //TODO
		
		if(stared == null) {
			if(server) { //server only
				stared = Util.pigCrazy(pig, w.getEntitiesWithinAABBExcludingEntity(pig, pig.getEntityBoundingBox().expandXyz(32D)).iterator());
				
				//盯
				if(stared != null) pig.getDataManager().set(PIG_STARED, stared.getEntityId());
			}
		} else { //both side
			
		}
	}
	
	/**
	 * 猪盯着的Entity的ID, Server搜寻瞪着看的
	 * */
	public static final DataParameter<Integer> PIG_STARED = EntityDataManager.<Integer>createKey(EntityPig.class, DataSerializers.VARINT);
	
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
