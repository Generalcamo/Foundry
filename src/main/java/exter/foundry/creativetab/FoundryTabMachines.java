package exter.foundry.creativetab;

import exter.foundry.block.FoundryBlocks;
import exter.foundry.block.BlockFoundryMachine.EnumMachine;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class FoundryTabMachines extends CreativeTabs
{
  public static FoundryTabMachines tab = new FoundryTabMachines();

  private FoundryTabMachines()
  {
    super("foundry.machines");
  }
  
  @Override
  public ItemStack getIconItemStack()
  {
    return FoundryBlocks.block_machine.asItemStack(EnumMachine.CASTER);
  }

  @Override
  public ItemStack getTabIconItem()
  {
    return FoundryBlocks.block_machine.asItemStack(EnumMachine.CASTER);
  }
}
