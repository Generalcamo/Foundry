package exter.foundry.integration.minetweaker;


import exter.foundry.api.recipe.IAlloyMixerRecipe;
import exter.foundry.integration.jei.AlloyMixerJEI;
import exter.foundry.recipes.AlloyMixerRecipe;
import exter.foundry.recipes.manager.AlloyMixerRecipeManager;
import minetweaker.MineTweakerAPI;
import minetweaker.api.liquid.ILiquidStack;
import minetweaker.api.minecraft.MineTweakerMC;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.foundry.AlloyMixer")
public class MTAlloyMixerHandler
{
  public static class AlloyMixerAction extends AddRemoveAction
  {
    
    IAlloyMixerRecipe recipe;
    
    public AlloyMixerAction(IAlloyMixerRecipe recipe)
    {
      this.recipe = recipe;
    }
    
    @Override
    protected void add()
    {
      AlloyMixerRecipeManager.instance.recipes.add(recipe);
      MineTweakerAPI.getIjeiRecipeRegistry().addRecipe(new AlloyMixerJEI.Wrapper(recipe));
    }

    @Override
    protected void remove()
    {
      AlloyMixerRecipeManager.instance.recipes.remove(recipe);
      MineTweakerAPI.getIjeiRecipeRegistry().removeRecipe(new AlloyMixerJEI.Wrapper(recipe));
    }

    @Override
    public String getRecipeType()
    {
      return "alloy mixer";
    }

    @Override
    public String getDescription()
    {
      StringBuilder builder = new StringBuilder();
      builder.append("(");
      boolean comma = false;
      for(FluidStack input:recipe.getInputs())
      {
        if(comma)
        {
          builder.append(',');
        }
        builder.append(' ');
        builder.append(MTHelper.getFluidDescription(input));
        comma = true;
      }
      builder.append(String.format(" ) -> %s", MTHelper.getFluidDescription(recipe.getOutput())));
      return builder.toString();
    }
  }

  
  @ZenMethod
  static public void addRecipe(ILiquidStack output,ILiquidStack[] inputs)
  {
    
    FluidStack out = (FluidStack)output.getInternal();
    FluidStack[] in = new FluidStack[inputs.length];
    
    int i;
    for(i = 0; i < inputs.length; i++)
    {
      in[i] = MineTweakerMC.getLiquidStack(inputs[i]);
    }

    IAlloyMixerRecipe recipe = null;
    try
    {
      recipe = new AlloyMixerRecipe(out, in);
    } catch(IllegalArgumentException e)
    {
      MineTweakerAPI.logError("Invalid alloy mixer recipe: " + e.getMessage());
      return;
    }
    MineTweakerAPI.apply((new AlloyMixerAction(recipe).action_add));
  }

  @ZenMethod
  static public void removeRecipe(ILiquidStack[] inputs)
  {
    FluidStack[] in = new FluidStack[inputs.length];
    
    int i;
    for(i = 0; i < inputs.length; i++)
    {
      in[i] = MineTweakerMC.getLiquidStack(inputs[i]);
    }

    
    IAlloyMixerRecipe recipe = AlloyMixerRecipeManager.instance.findRecipe(in,null);
    if(recipe == null)
    {
      MineTweakerAPI.logWarning("Alloy mixer recipe not found.");
      return;
    }
    MineTweakerAPI.apply((new AlloyMixerAction(recipe)).action_remove);
  }
}
