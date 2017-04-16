package exter.foundry.integration.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import exter.foundry.api.recipe.ICastingTableRecipe;
import exter.foundry.block.BlockCastingTable;
import exter.foundry.block.FoundryBlocks;
import exter.foundry.recipes.manager.CastingTableRecipeManager;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;


public class CastingTableJEI
{

  static public class Wrapper implements IRecipeWrapper
  {
    private final ICastingTableRecipe recipe;
    private final String name;

    public Wrapper(ICastingTableRecipe recipe)
    {
      this.recipe = recipe;
      this.name = recipe.getTableType().name().toLowerCase();
    }

    @Deprecated
    @Override
    public List<List<ItemStack>> getInputs()
    {
      return null;
    }

    @Deprecated
    @Override
    public List<ItemStack> getOutputs()
    {
      return null;
    }

    @Deprecated
    @Override
    public List<FluidStack> getFluidInputs()
    {
      return null;
    }

    @Deprecated
    @Override
    public List<FluidStack> getFluidOutputs()
    {
      return Collections.emptyList();
    }

    @Deprecated
    @Override
    public void drawAnimations(Minecraft minecraft, int recipeWidth, int recipeHeight)
    {

    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY)
    {
      return null;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
    {
      
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton)
    {
      return false;
    }

    @Override
    public void getIngredients(IIngredients ingredients)
    {
      ingredients.setInput(FluidStack.class, recipe.getInput());
      ingredients.setOutput(ItemStack.class, recipe.getOutput());
    }
    
    public String getName()
    {
      return name;
    }

    @Override
    public boolean equals(Object other)
    {
      return recipe == other;
    }
  }

  public class Category implements IRecipeCategory<Wrapper>
  {

    protected final ResourceLocation backgroundLocation;
    @Nonnull
    private final IDrawable background;
    @Nonnull
    private final String localizedName;
    
    private IJeiHelpers helpers;

    public Category(IJeiHelpers helpers)
    {
      this.helpers = helpers;
      IGuiHelper guiHelper = helpers.getGuiHelper();
      backgroundLocation = new ResourceLocation("foundry", "textures/gui/casting_table_jei.png");

      ResourceLocation location = new ResourceLocation("foundry", "textures/gui/casting_table_jei.png");
      background = guiHelper.createDrawable(location, 0, 0, 74, 59);
      localizedName = Translator.translateToLocal("gui.jei.casting_table." + name);
    }

    @Override
    @Nonnull
    public IDrawable getBackground()
    {
      return background;
    }

    @Override
    public void drawExtras(Minecraft minecraft)
    {

    }

    @Override
    public void drawAnimations(Minecraft minecraft)
    {

    }

    @Nonnull
    @Override
    public String getTitle()
    {
      return localizedName;
    }

    @Nonnull
    @Override
    public String getUid()
    {
      return "foundry.casting_table." + name;
    }

    @Override
    @Deprecated
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull Wrapper recipeWrapper)
    {

    }

    @Override
    public IDrawable getIcon()
    {
      return null;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, Wrapper recipeWrapper, IIngredients ingredients)
    {
      IGuiItemStackGroup gui_items = recipeLayout.getItemStacks();
      IGuiFluidStackGroup gui_fluids = recipeLayout.getFluidStacks();
      IStackHelper stack_helper = helpers.getStackHelper();
      
      List<FluidStack> input = ingredients.getInputs(FluidStack.class).get(0);

      gui_items.init(0, false, 53, 20);
      gui_items.init(1, true, 3, 39);
      gui_fluids.init(2, true, 4, 4, 16, 24, input.get(0).amount,false,null);

      gui_items.set(0, stack_helper.toItemStackList(ingredients.getOutputs(ItemStack.class).get(0)));
      gui_items.set(1, table_item);
      gui_fluids.set(2, input.get(0));
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY)
    {
      return Collections.emptyList();
    }
  }

  static public class Handler implements IRecipeHandler<Wrapper>
  {
    @Override
    @Nonnull
    public Class<Wrapper> getRecipeClass()
    {
      return Wrapper.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid()
    {
      return "foundry.casting_table";
    }

    @Override
    @Nonnull
    public IRecipeWrapper getRecipeWrapper(@Nonnull Wrapper recipe)
    {
      return recipe;
    }

    @Override
    public boolean isRecipeValid(@Nonnull Wrapper recipe)
    {
      return true;
    }

    @Override
    public String getRecipeCategoryUid(Wrapper recipe)
    {
      return "foundry.casting_table." + recipe.getName();
    }
  }
  
  private final ItemStack table_item;
  private final ICastingTableRecipe.TableType type;
  private final String name;
  
  public CastingTableJEI(BlockCastingTable.EnumTable table)
  {
    table_item = FoundryBlocks.block_casting_table.asItemStack(table);
    name = table.name;
    type = table.type;
  }

  public List<Wrapper> getRecipes()
  {
    List<Wrapper> recipes = new ArrayList<Wrapper>();

    for(ICastingTableRecipe recipe : CastingTableRecipeManager.instance.getRecipes())
    {
      if(recipe.getTableType() == type)
      {
        ItemStack output = recipe.getOutput();

        if(output != null)
        {
          recipes.add(new Wrapper(recipe));
        }
      }
    }

    return recipes;
  }
}
