package exter.foundry.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import exter.foundry.container.ContainerMetalCaster;
import exter.foundry.gui.button.GuiButtonFoundry;
import exter.foundry.tileentity.TileEntityMetalCaster;
import exter.foundry.tileentity.TileEntityFoundry.RedstoneMode;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class GuiMetalCaster extends GuiFoundry
{

  private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("foundry:textures/gui/caster.png");

  public static final int TANK_HEIGHT = 47;
  private static final int TANK_X = 39;
  private static final int TANK_Y = 21;
  
  private static final int PROGRESS_X = 60;
  private static final int PROGRESS_Y = 51;
  private static final int PROGRESS_WIDTH = 22;
  private static final int PROGRESS_HEIGHT = 15;

  private static final int POWER_X = 59;
  private static final int POWER_Y = 21;
  private static final int POWER_WIDTH = 3;
  private static final int POWER_HEIGHT = 16;
  
  
  private static final int TANK_OVERLAY_X = 176;
  private static final int TANK_OVERLAY_Y = 0;

  private static final int PROGRESS_OVERLAY_X = 176;
  private static final int PROGRESS_OVERLAY_Y = 53;

  private static final int POWER_OVERLAY_X = 176;
  private static final int POWER_OVERLAY_Y = 71;
  
  private static final int RSMODE_X = 176 - 16 - 4;
  private static final int RSMODE_Y = 4;
  private static final int RSMODE_TEXTURE_X = 176;
  private static final int RSMODE_TEXTURE_Y = 90;

  
  
  private TileEntityMetalCaster te_caster;
  private GuiButtonFoundry button_mode;

  public GuiMetalCaster(TileEntityMetalCaster cs, EntityPlayer player)
  {
    super(new ContainerMetalCaster(cs, player));
    allowUserInput = false;
    ySize = 166;
    te_caster = cs;
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouse_x, int mouse_y)
  {
    super.drawGuiContainerForegroundLayer(mouse_x, mouse_y);

    fontRendererObj.drawString("Metal Caster", 5, 6, 0x404040);
    fontRendererObj.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
  {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    mc.renderEngine.bindTexture(GUI_TEXTURE);
    int window_x = (width - xSize) / 2;
    int window_y = (height - ySize) / 2;
    drawTexturedModalRect(window_x, window_y, 0, 0, xSize, ySize);

    //Draw progress bar.
    int progress = te_caster.getProgress() * PROGRESS_WIDTH / TileEntityMetalCaster.CAST_TIME;
    if(progress > 0)
    {
      drawTexturedModalRect(window_x + PROGRESS_X, window_y + PROGRESS_Y, PROGRESS_OVERLAY_X, PROGRESS_OVERLAY_Y, progress, PROGRESS_HEIGHT);
    }

    //Draw stored power bar.
    int power = (int)(te_caster.getStoredFoundryEnergy() * POWER_HEIGHT / te_caster.getFoundryEnergyCapacity());
    if(power > 0)
    {
      drawTexturedModalRect(window_x + POWER_X, window_y + POWER_Y + POWER_HEIGHT - power, POWER_OVERLAY_X, POWER_OVERLAY_Y + POWER_HEIGHT - power, POWER_WIDTH, power);
    }
    displayTank(window_x, window_y, TANK_X, TANK_Y, TANK_HEIGHT, TANK_OVERLAY_X, TANK_OVERLAY_Y, te_caster.getTank(0));
  }

  @Override
  public void drawScreen(int mousex, int mousey, float par3)
  {
    super.drawScreen(mousex, mousey, par3);

    if(isPointInRegion(TANK_X,TANK_Y,16,TANK_HEIGHT,mousex,mousey))
    {
      List<String> currenttip = new ArrayList<String>();
      addTankTooltip(currenttip, mousex, mousey, te_caster.getTank(0));
      drawHoveringText(currenttip, mousex, mousey, fontRendererObj);
    }
    
    if(isPointInRegion(POWER_X,POWER_Y,POWER_WIDTH,POWER_HEIGHT,mousex,mousey))
    {
      List<String> currenttip = new ArrayList<String>();
      long power = te_caster.getStoredFoundryEnergy();
      long max_power = te_caster.getFoundryEnergyCapacity();
      currenttip.add("Energy: " + String.valueOf(power) + "/" + String.valueOf(max_power));
      drawHoveringText(currenttip, mousex, mousey, fontRendererObj);
    }

    if(isPointInRegion(RSMODE_X,RSMODE_Y,button_mode.getWidth(),button_mode.getHeight(),mousex,mousey))
    {
      List<String> currenttip = new ArrayList<String>();
      currenttip.add(getRedstoenModeText(te_caster.getRedstoneMode()));
      drawHoveringText(currenttip, mousex, mousey, fontRendererObj);
    }
  }
  
  @Override
  protected ResourceLocation getGUITexture()
  {
    return GUI_TEXTURE;
  }

  @Override 
  public void initGui()
  {
    super.initGui();
    int window_x = (width - xSize) / 2;
    int window_y = (height - ySize) / 2;
    button_mode = new GuiButtonFoundry(
            1,
            RSMODE_X + window_x, RSMODE_Y + window_y,
            16, 15,
            GUI_TEXTURE,RSMODE_TEXTURE_X,RSMODE_TEXTURE_Y,
            RSMODE_TEXTURE_X + 16, RSMODE_TEXTURE_Y);
    buttonList.add(button_mode);
  }

  @Override
  protected void actionPerformed(GuiButton button)
  {
    if(button.id == button_mode.id)
    {
      switch(te_caster.getRedstoneMode())
      {
        case RSMODE_IGNORE:
          te_caster.setRedstoneMode(RedstoneMode.RSMODE_OFF);
          break;
        case RSMODE_OFF:
          te_caster.setRedstoneMode(RedstoneMode.RSMODE_ON);
          break;
        case RSMODE_ON:
          te_caster.setRedstoneMode(RedstoneMode.RSMODE_PULSE);
          break;
        case RSMODE_PULSE:
          te_caster.setRedstoneMode(RedstoneMode.RSMODE_IGNORE);
          break;
      }
    }
  }
}
