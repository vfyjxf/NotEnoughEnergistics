package com.github.vfyjxf.nee.asm;

import com.github.vfyjxf.nee.NotEnoughEnergistics;
import com.github.vfyjxf.nee.helper.ModChecker;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class NEEClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        String internalName = transformedName.replace('.', '/');
        if ("mezz/jei/gui/recipes/RecipeTransferButton".equals(internalName)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);
            for (MethodNode methodNode : classNode.methods) {
                if ("init".equals(methodNode.name) && "(Lnet/minecraft/inventory/Container;Lnet/minecraft/entity/player/EntityPlayer;)V".equals(methodNode.desc)) {
                    NotEnoughEnergistics.logger.info("Transforming : " + internalName + methodNode.name + methodNode.desc);
                    /*
                     *Add: JEIHooks.setButtonEnable(this, error);
                     */
                    for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
                        int opcode = instruction.getOpcode();
                        if (opcode == RETURN) {
                            InsnList insnList = new InsnList();
                            insnList.add(new VarInsnNode(ALOAD, 0));
                            insnList.add(new VarInsnNode(ALOAD, 0));
                            insnList.add(new FieldInsnNode(GETFIELD,
                                    "mezz/jei/gui/recipes/RecipeTransferButton",
                                    "recipeTransferError",
                                    "Lmezz/jei/api/recipe/transfer/IRecipeTransferError;"));
                            insnList.add(new MethodInsnNode(INVOKESTATIC,
                                    "com/github/vfyjxf/nee/asm/JeiHooks",
                                    "setButtonEnable",
                                    "(Lnet/minecraft/client/gui/GuiButton;Lmezz/jei/api/recipe/transfer/IRecipeTransferError;)V",
                                    false));
                            methodNode.instructions.insertBefore(instruction, insnList);
                        }
                    }
                }
            }
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        }
        if ("appeng/core/sync/packets/PacketMEInventoryUpdate".equals(internalName)) {
            if (ModChecker.isUnofficialAppeng) return basicClass;

            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);
            for (MethodNode methodNode : classNode.methods) {
                if ("clientPacketData".equals(methodNode.name) && "(Lappeng/core/sync/network/INetworkInfo;Lappeng/core/sync/AppEngPacket;Lnet/minecraft/entity/player/EntityPlayer;)V".equals(methodNode.desc)) {
                    NotEnoughEnergistics.logger.info("Transforming : " + internalName + methodNode.name + methodNode.desc);
                    /*
                     *Add: AppengHooks.updateMeInventory(gs, this.list);
                     */
                    for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
                        int opcode = instruction.getOpcode();
                        if (opcode == RETURN) {
                            InsnList insnList = new InsnList();
                            insnList.add(new VarInsnNode(ALOAD, 4));
                            insnList.add(new VarInsnNode(ALOAD, 0));
                            insnList.add(new FieldInsnNode(GETFIELD,
                                    "appeng/core/sync/packets/PacketMEInventoryUpdate",
                                    "list",
                                    "Ljava/util/List;"));
                            insnList.add(new MethodInsnNode(INVOKESTATIC,
                                    "com/github/vfyjxf/nee/asm/AppengHooks",
                                    "updateMeInventory",
                                    "(Ljava/lang/Object;Ljava/util/List;)V",
                                    false));
                            methodNode.instructions.insertBefore(instruction, insnList);
                        }
                    }
                }
            }
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        }
        if ("p455w0rd/wct/sync/packets/PacketMEInventoryUpdate".equals(internalName)){
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);
            for (MethodNode methodNode : classNode.methods) {
                if ("clientPacketData".equals(methodNode.name) && "(Lp455w0rd/ae2wtlib/api/networking/INetworkInfo;Lp455w0rd/wct/sync/WCTPacket;Lnet/minecraft/entity/player/EntityPlayer;)V".equals(methodNode.desc)) {
                    NotEnoughEnergistics.logger.info("Transforming : " + internalName + methodNode.name + methodNode.desc);
                    /*
                     *Add: AppengHooks.updateWirelessInventory(gs, this.list);
                     */
                    for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
                        int opcode = instruction.getOpcode();
                        if (opcode == RETURN) {
                            InsnList insnList = new InsnList();
                            insnList.add(new VarInsnNode(ALOAD, 4));
                            insnList.add(new VarInsnNode(ALOAD, 0));
                            insnList.add(new FieldInsnNode(GETFIELD,
                                    "p455w0rd/wct/sync/packets/PacketMEInventoryUpdate",
                                    "list",
                                    "Ljava/util/List;"));
                            insnList.add(new MethodInsnNode(INVOKESTATIC,
                                    "com/github/vfyjxf/nee/asm/AppengHooks",
                                    "updateWirelessInventory",
                                    "(Lnet/minecraft/client/gui/GuiScreen;Ljava/util/List;)V",
                                    false));
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
