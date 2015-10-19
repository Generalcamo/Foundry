package exter.foundry.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class ColoredFluid extends Fluid
{
  
  private int color;

  public ColoredFluid(String fluidName,ResourceLocation still,ResourceLocation flowing)
  {
    super(fluidName,still, flowing);
    color = 0xFFFFFFFF;
  }

  public ColoredFluid setColor(int fluid_color)
  {
    color = fluid_color | 0xFF000000;
    return this;
  }
  
  @Override
  public int getColor()
  {
    return color;
  }
}
