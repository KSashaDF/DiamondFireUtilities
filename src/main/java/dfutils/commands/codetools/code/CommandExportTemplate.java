package dfutils.commands.codetools.code;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static diamondcore.utils.MessageUtils.actionMessage;
import static diamondcore.utils.MessageUtils.errorMessage;
import static diamondcore.utils.MessageUtils.infoMessage;

class CommandExportTemplate {

    private static final Minecraft minecraft = Minecraft.getMinecraft();

    static void executeExportTemplate(ICommandSender sender, String[] commandArgs) {

        //Checks if command arguments are valid.
        if (!checkFormat(sender, commandArgs)) {
            return;
        }

        ItemStack itemStack = minecraft.player.getHeldItemMainhand();
        File codeTemplateDirectory = new File(minecraft.gameDir, "codetemplates");
        File codeTemplateFile = new File(codeTemplateDirectory, commandArgs[1] + ".dfcode");

        if (itemStack.isEmpty()) {
            errorMessage("Invalid item!");
            return;
        }

        if (!itemStack.hasTagCompound()) {
            errorMessage("Invalid item! Item does not contain any code data.");
            return;
        }

        if (!itemStack.getTagCompound().hasKey("CodeData")) {
            errorMessage("Invalid item! Item does not contain any code data.");
            return;
        }

        try {

            //If code template folder does not exist, create the code template folder.
            if (!codeTemplateDirectory.exists() || !codeTemplateDirectory.isDirectory()) {
                codeTemplateDirectory.delete();
                codeTemplateDirectory.mkdirs();
            }

            //Creates export_codedata.nbt file and writes the currently held item's CodeData tag to the file.
            codeTemplateFile.createNewFile();
            IOUtils.write("{CodeData:" + itemStack.getTagCompound().getTagList("CodeData", 10).toString() + ",Author:\"" + minecraft.player.getName() + "\"}", new FileOutputStream(codeTemplateFile), Charsets.UTF_8);

            //Sends confirmation to the player.
            actionMessage("Saved code template \"" + commandArgs[1] + ".dfcode\" to your codetemplates folder. (in your .minecraft folder)");
            minecraft.player.playSound(SoundEvents.BLOCK_SHULKER_BOX_CLOSE, 1.0F, 1.0F);
        } catch (IOException exception) {
            errorMessage("Uh oh! An IO Exception occurred while trying to export code data.");
        }
    }

    private static boolean checkFormat(ICommandSender sender, String[] commandArgs) {
        if (commandArgs.length != 2) {
            infoMessage("Usage: \n" + new CommandCodeBase().getUsage(sender));
            return false;
        }

        return true;
    }
}
