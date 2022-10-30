package com.github.vfyjxf.nee.asm;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class NEEClassTransformer implements IClassTransformer {

    private final static String TARGET_CLASS_NAME = "mezz/jei/gui/recipes/RecipeTransferButton";
    private final static String METHOD_NAME = "init";
    private final static String METHOD_TARGET = "(Lnet/minecraft/inventory/Container;Lnet/minecraft/entity/player/EntityPlayer;)V";

    private final static String HELPER_CLASS_NAME = "com/github/vfyjxf/nee/asm/JeiHooks";
    private final static String HELPER_METHOD_NAME = "setButtonEnable";
    private final static String HELPER_METHOD_TARGET = "(Lnet/minecraft/client/gui/GuiButton;Lmezz/jei/api/recipe/transfer/IRecipeTransferError;)V";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        String internalName = transformedName.replace('.', '/');
        if (internalName.equals(TARGET_CLASS_NAME)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals(METHOD_NAME) && methodNode.desc.equals(METHOD_TARGET)) {
                    NotEnoughEnergistics.logger.info("Transforming : " + internalName + methodNode.name + methodNode.desc);
                    /*
                     *Add: JEIHelper.setButtonEnable(this, error);
                     */
                    InsnList insnList = new InsnList();
                    for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
                        int opcode = instruction.getOpcode();
                        if (opcode == RETURN) {
                            insnList.add(new VarInsnNode(ALOAD, 0));
                            insnList.add(new VarInsnNode(ALOAD, 0));
                            insnList.add(new FieldInsnNode(GETFIELD,
                                    "mezz/jei/gui/recipes/RecipeTransferButton",
                                    "recipeTransferError",
                                    "Lmezz/jei/api/recipe/transfer/IRecipeTransferError;"));
                            insnList.add(new MethodInsnNode(INVOKESTATIC, HELPER_CLASS_NAME, HELPER_METHOD_NAME, HELPER_METHOD_TARGET, false));
                            methodNode.instructions.insertBefore(instruction, insnList);
                        }
                    }
                }
            }
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        }
        return basicClass;
    }
}
