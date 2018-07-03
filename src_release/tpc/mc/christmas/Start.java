package tpc.mc.christmas;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

/**
 * 启动CodeRuler
 * */
public final class Start {
	
	/**
	 * 启动
	 * */
	public static final void premian(String arg, Instrumentation inst) throws Throwable {
		System.out.println("Christmas Mod -> Starts!");
		
		final Method m = Class.forName("tpc.mc.christmas.Util$CodeRuler").getMethod("codefix", byte[].class);
		inst.addTransformer(new ClassFileTransformer() {
			
			@Override
			public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
				try {
					classfileBuffer = (byte[]) m.invoke(null, classfileBuffer);
				} catch(Throwable e) {
					e.printStackTrace();
					System.exit(0);
				}
				
				return classfileBuffer;
			}
		}, true);
		
		System.out.println("Christmas Mod -> Retransform!");
		
		//回滚加载过的classes
		Class[] cs = inst.getAllLoadedClasses();
		for(Class c : cs) {
			if(inst.isModifiableClass(c)) inst.retransformClasses(c);
		}
		
		System.out.println("Christmas Mod -> Listening!");
	}
}
