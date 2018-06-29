package dfutils.commands.itemcontrol.attributes;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;

import static dfutils.commands.MessageUtils.*;

class CommandAttributeClear {
    
    private static Minecraft minecraft = Minecraft.getMinecraft();
    
    static void executeClearAttributes(ICommandSender sender, String[] commandArgs) {
    
        //Checks if command format is valid.
        if (!checkFormat(sender, commandArgs)) return;
    
        ItemStack itemStack = minecraft.player.getHeldItemMainhand();
    
        //Checks if item stack is not air.
        if (itemStack.isEmpty()) {
            commandError("Invalid item!");
            return;
        }
        
        //Checks if item has attributes.
        if (!itemStack.hasTagCompound()) {
            commandError("This item does not contain any attributes!");
            return;
        }
    
        //Checks if item has attributes.
        if (!itemStack.getTagCompound().hasKey("AttributeModifiers", 9)) {
            commandError("This item does not contain any attributes!");
            return;
        }
        
        itemStack.getTagCompound().removeTag("AttributeModifiers");
        
        //Sends updated item to the server.
        minecraft.playerController.sendSlotPacket(itemStack, minecraft.player.inventoryContainer.inventorySlots.size() - 10 + minecraft.player.inventory.currentItem);
    
        commandAction("Cleared attributes from item.");
    }
    
    private static boolean checkFormat(ICommandSender sender, String[] commandArgs) {
        if (commandArgs.length == 1) {
            return true;
            
        } else {
            commandInfo("Usage:\n" + new CommandAttributeBase().getUsage(sender));
            return false;
        }
    }
}
