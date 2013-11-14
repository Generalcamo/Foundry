package exter.foundry.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IMeltingRecipe
{
  /**
   * Get the required item.
   * @return If the recipe used the Ore Dictionary, a {@link String} of it's name, an {@link ItemStack} of the required item otherwise.
   */
  public Object GetInput();

  /**
   * Get the produced fluid.
   * @return The fluid that the recipe produces.
   */
  public FluidStack GetOutput();
}