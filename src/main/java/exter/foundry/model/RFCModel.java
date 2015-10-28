package exter.foundry.model;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import exter.foundry.item.FoundryItems;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.IPerspectiveState;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.fluids.FluidStack;

@SuppressWarnings("deprecation")
public class RFCModel implements IModel
{
  public static class BakedModel extends ItemLayerModel.BakedModel implements IFlexibleBakedModel, IPerspectiveAwareModel, ISmartItemModel
  {
    private ImmutableList<BakedQuad> bg_quads;
    private ImmutableList<BakedQuad> fg_quads;
    private Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;
    private VertexFormat format;
    private TextureAtlasSprite particle;
    private ImmutableMap<TransformType, TRSRTransformation> transforms;

    public BakedModel(ImmutableList<BakedQuad> bg_quads, ImmutableList<BakedQuad> fg_quads, TextureAtlasSprite particle, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, ImmutableMap<TransformType, TRSRTransformation> transforms)
    {
      super(ImmutableList.<BakedQuad> builder().addAll(fg_quads).addAll(bg_quads).build(), particle, format, transforms);
      this.bg_quads = bg_quads;
      this.fg_quads = fg_quads;
      this.format = format;
      this.bakedTextureGetter = bakedTextureGetter;
      this.particle = particle;
      this.transforms = transforms;
    }

    @Override
    public IBakedModel handleItemState(ItemStack stack)
    {
      FluidStack fluid = FoundryItems.item_container.getFluid(stack);
      if(fluid == null || fluid.amount == 0)
      {
        return this;
      }
      int y = fluid.amount * 10 / FoundryItems.item_container.getCapacity(stack);
      if(y <= 0)
      {
        return this;
      }
      ResourceLocation texture = fluid.getFluid().getStill();
      if(texture == null)
      {
        texture = TextureMap.LOCATION_MISSING_TEXTURE;
      }
      ImmutableList<BakedQuad> fluid_model = getQuadsForSpriteSlice(
          1, bakedTextureGetter.apply(texture), format,
          4, 3,
          12, 3 + y,
          fluid.getFluid().getColor());
      return new ItemLayerModel.BakedModel(ImmutableList.<BakedQuad> builder().addAll(bg_quads).addAll(fluid_model).addAll(fg_quads).build(), particle, format, transforms);
    }
  }

  static public class Loader implements ICustomModelLoader
  {
    static public Loader instance = new Loader();

    private Loader()
    {

    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {

    }

    public boolean accepts(ResourceLocation modelLocation)
    {
      return modelLocation.getResourceDomain().equals("foundry")
          && (modelLocation.getResourcePath().equals("container") || modelLocation.getResourcePath().endsWith("/container"));
    }

    public IModel loadModel(ResourceLocation modelLocation)
    {
      return RFCModel.instance;
    }
  }

  public static final RFCModel instance = new RFCModel();

  private final ResourceLocation texture_fg;
  private final ResourceLocation texture_bg;

  private RFCModel()
  {
    texture_fg = new ResourceLocation("foundry", "items/container_foreground");
    texture_bg = new ResourceLocation("foundry", "items/container_background");
  }

  @Override
  public Collection<ResourceLocation> getDependencies()
  {
    return ImmutableList.of();
  }

  @Override
  public Collection<ResourceLocation> getTextures()
  {
    return ImmutableList.of(texture_fg, texture_bg);
  }

  @Override
  public IModelState getDefaultState()
  {
    return DEFAULT_STATE;
  }

  private ImmutableList<BakedQuad> bakeLayer(final TRSRTransformation transform, TextureAtlasSprite sprite, int tint, final VertexFormat format)
  {
    ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
    builder.addAll(Iterables.transform(getQuadsForSprite(tint, sprite, format), new Function<BakedQuad, BakedQuad>()
    {
      public BakedQuad apply(BakedQuad input)
      {
        return Attributes.transform(transform, input, format);
      }
    }));
    return builder.build();
  }

  @Override
  public IFlexibleBakedModel bake(IModelState state, final VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
  {
    final TRSRTransformation transform = state.apply(this);

    ImmutableList<BakedQuad> model_bg = bakeLayer(transform, bakedTextureGetter.apply(texture_bg), 0, format);
    ImmutableList<BakedQuad> model_fg = bakeLayer(transform, bakedTextureGetter.apply(texture_fg), 2, format);

    TextureAtlasSprite particle = bakedTextureGetter.apply(texture_fg);
    if(state instanceof IPerspectiveState)
    {
      IPerspectiveState ps = (IPerspectiveState) state;
      Map<TransformType, TRSRTransformation> map = Maps.newHashMap();
      for(TransformType type : TransformType.values())
      {
        map.put(type, ps.forPerspective(type).apply(this));
      }
      return new BakedModel(model_bg, model_fg, particle, format, bakedTextureGetter, Maps.immutableEnumMap(map));
    }
    return new BakedModel(model_bg, model_fg, particle, format, bakedTextureGetter, ImmutableMap.<TransformType, TRSRTransformation> of());
  }


  public ImmutableList<BakedQuad> getQuadsForSprite(int tint, TextureAtlasSprite sprite, VertexFormat format)
  {
    ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

    int uMax = sprite.getIconWidth();
    int vMax = sprite.getIconHeight();

    ByteBuffer buf = BufferUtils.createByteBuffer(4 * format.getNextOffset());

    for(int f = 0; f < sprite.getFrameCount(); f++)
    {
      int[] pixels = sprite.getFrameTextureData(f)[0];
      boolean ptu;
      boolean[] ptv = new boolean[uMax];
      Arrays.fill(ptv, true);
      for(int v = 0; v < vMax; v++)
      {
        ptu = true;
        for(int u = 0; u < uMax; u++)
        {
          boolean t = isTransparent(pixels, uMax, vMax, u, v);
          if(ptu && !t) // left - transparent, right - opaque
          {
            builder.add(buildSideQuad(buf, format, EnumFacing.WEST, tint, sprite, u, v));
          }
          if(!ptu && t) // left - opaque, right - transparent
          {
            builder.add(buildSideQuad(buf, format, EnumFacing.EAST, tint, sprite, u, v));
          }
          if(ptv[u] && !t) // up - transparent, down - opaque
          {
            builder.add(buildSideQuad(buf, format, EnumFacing.UP, tint, sprite, u, v));
          }
          if(!ptv[u] && t) // up - opaque, down - transparent
          {
            builder.add(buildSideQuad(buf, format, EnumFacing.DOWN, tint, sprite, u, v));
          }
          ptu = t;
          ptv[u] = t;
        }
        if(!ptu) // last - opaque
        {
          builder.add(buildSideQuad(buf, format, EnumFacing.EAST, tint, sprite, uMax, v));
        }
      }
      // last line
      for(int u = 0; u < uMax; u++)
      {
        if(!ptv[u])
        {
          builder.add(buildSideQuad(buf, format, EnumFacing.DOWN, tint, sprite, u, vMax));
        }
      }
    }
    float z1 = 7.5f / 16f - 0.0002f * tint;
    float z2 = 8.5f / 16f + 0.0002f * tint;

    // front
    builder.add(buildQuad(buf, format, EnumFacing.SOUTH, tint,
        0, 0, z1, sprite.getMinU(), sprite.getMaxV(),
        0, 1, z1, sprite.getMinU(), sprite.getMinV(),
        1, 1, z1, sprite.getMaxU(), sprite.getMinV(),
        1, 0, z1, sprite.getMaxU(), sprite.getMaxV()));
    // back
    builder.add(buildQuad(buf, format, EnumFacing.NORTH, tint,
        0, 0, z2, sprite.getMinU(), sprite.getMaxV(),
        1, 0, z2, sprite.getMaxU(), sprite.getMaxV(),
        1, 1, z2, sprite.getMaxU(), sprite.getMinV(),
        0, 1, z2, sprite.getMinU(), sprite.getMinV()));
    return builder.build();
  }

  static public ImmutableList<BakedQuad> getQuadsForSpriteSlice(int tint, TextureAtlasSprite sprite, VertexFormat format, int min_x, int min_y, int max_x, int max_y, int color)
  {
    ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

    float min_u = sprite.getInterpolatedU(min_x);
    float min_v = sprite.getInterpolatedV(min_y);
    float max_u = sprite.getInterpolatedU(max_x);
    float max_v = sprite.getInterpolatedV(max_y);

    ByteBuffer buf = BufferUtils.createByteBuffer(4 * format.getNextOffset());

    float x1 = (float)min_x / 16;
    float y1 = (float)min_y / 16;
    float x2 = (float)max_x / 16;
    float y2 = (float)max_y / 16;
    
    float z1 = 7.5f / 16f - 0.0002f * tint;
    float z2 = 8.5f / 16f + 0.0002f * tint;

    
    builder.add(buildQuad(buf, format, EnumFacing.SOUTH, tint,
        x1, y1, z1, min_u, max_v,
        x1, y2, z1, min_u, min_v,
        x2, y2, z1, max_u, min_v,
        x2, y1, z1, max_u, max_v, color));

    builder.add(buildQuad(
        buf, format, EnumFacing.NORTH, tint,
        x1, y1, z2, min_u, max_v,
        x2, y1, z2, max_u, max_v,
        x2, y2, z2, max_u, min_v,
        x1, y2, z2, min_u, min_v, color));
    return builder.build();
  }

  protected boolean isTransparent(int[] pixels, int uMax, int vMax, int u, int v)
  {
    return (pixels[u + (vMax - 1 - v) * uMax] >> 24 & 0xFF) == 0;
  }

  private static BakedQuad buildSideQuad(ByteBuffer buf, VertexFormat format, EnumFacing side, int tint, TextureAtlasSprite sprite, int u, int v)
  {
    float x0 = (float) u / sprite.getIconWidth();
    float y0 = (float) v / sprite.getIconHeight();
    float x1 = x0, y1 = y0;
    float z1 = 7.5f / 16f;
    float z2 = 8.5f / 16f;
    switch(side)
    {
      case WEST:
        z1 = 8.5f / 16f;
        z2 = 7.5f / 16f;
      case EAST:
        y1 = (v + 1f) / sprite.getIconHeight();
        break;
      case DOWN:
        z1 = 8.5f / 16f;
        z2 = 7.5f / 16f;
      case UP:
        x1 = (u + 1f) / sprite.getIconWidth();
        break;
      default:
        throw new IllegalArgumentException("can't handle z-oriented side");
    }
    float u0 = 16f * (x0 - side.getDirectionVec().getX() * 1e-2f / sprite.getIconWidth());
    float u1 = 16f * (x1 - side.getDirectionVec().getX() * 1e-2f / sprite.getIconWidth());
    float v0 = 16f * (1f - y0 - side.getDirectionVec().getY() * 1e-2f / sprite.getIconHeight());
    float v1 = 16f * (1f - y1 - side.getDirectionVec().getY() * 1e-2f / sprite.getIconHeight());
    
    z1 += 0.0002f * tint;
    z2 -= 0.0002f * tint;
    return buildQuad(buf, format, side.getOpposite(), tint, x0, y0, z1, sprite.getInterpolatedU(u0), sprite.getInterpolatedV(v0), x1, y1, z1, sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1), x1, y1, z2, sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1), x0, y0, z2, sprite.getInterpolatedU(u0), sprite.getInterpolatedV(v0));
  }

  private static final BakedQuad buildQuad(ByteBuffer buf, VertexFormat format, EnumFacing side, int tint, float x0, float y0, float z0, float u0, float v0, float x1, float y1, float z1, float u1, float v1, float x2, float y2, float z2, float u2, float v2, float x3, float y3, float z3, float u3, float v3)
  {
    return buildQuad(buf, format, side, tint, x0, y0, z0, u0, v0, x1, y1, z1, u1, v1, x2, y2, z2, u2, v2, x3, y3, z3, u3, v3, 0xFFFFFFFF);
  }
  
  private static final BakedQuad buildQuad(ByteBuffer buf, VertexFormat format, EnumFacing side, int tint, float x0, float y0, float z0, float u0, float v0, float x1, float y1, float z1, float u1, float v1, float x2, float y2, float z2, float u2, float v2, float x3, float y3, float z3, float u3, float v3,int color)
  {
    buf.clear();
    putVertex(buf, format, side, x0, y0, z0, u0, v0, color);
    putVertex(buf, format, side, x1, y1, z1, u1, v1, color);
    putVertex(buf, format, side, x2, y2, z2, u2, v2, color);
    putVertex(buf, format, side, x3, y3, z3, u3, v3, color);
    buf.flip();
    int[] data = new int[4 * format.getNextOffset() / 4];
    buf.asIntBuffer().get(data);
    return new BakedQuad(data, tint, side);
  }

  private static void put(ByteBuffer buf, VertexFormatElement e, Float... fs)
  {
    Attributes.put(buf, e, true, 0f, fs);
  }

  @SuppressWarnings("unchecked")
  private static void putVertex(ByteBuffer buf, VertexFormat format, EnumFacing side, float x, float y, float z, float u, float v, int color)
  {
    for(VertexFormatElement e : (List<VertexFormatElement>) format.getElements())
    {
      switch(e.getUsage())
      {
        case POSITION:
          put(buf, e, x, y, z, 1f);
          break;
        case COLOR:
          {
            float red = (float) (color >>> 16 & 255) / 255.0F;
            float green = (float) (color >>> 8 & 255) / 255.0F;
            float blue = (float) (color & 255) / 255.0F;
            float alpha = (float) (color >>> 24 & 255) / 255.0F;
            put(buf, e, red, green, blue, alpha);
          }
          break;
        case UV:
          put(buf, e, u, v, 0f, 1f);
          break;
        case NORMAL:
          put(buf, e, (float) side.getFrontOffsetX(), (float) side.getFrontOffsetY(), (float) side.getFrontOffsetZ(), 0f);
          break;
        default:
          put(buf, e);
          break;
      }
    }
  }

  static private final IModelState DEFAULT_STATE;
  
  static {
    IModelState tp = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
        new Vector3f(0, 1.25f / 16, -3.5f / 16),
        TRSRTransformation.quatFromYXZDegrees(new Vector3f(90, 0, 0)),
        new Vector3f(0.55f, 0.55f, 0.55f),
        null));
    
    IModelState fp = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
        new Vector3f(0, 4f / 16, 2f / 16),
        TRSRTransformation.quatFromYXZDegrees(new Vector3f(0, -135, 25)),
        new Vector3f(1.7f, 1.7f, 1.7f),
        null));
    DEFAULT_STATE = new IPerspectiveState.Impl(TRSRTransformation.identity(), ImmutableMap
        .of(ItemCameraTransforms.TransformType.THIRD_PERSON, tp,
            ItemCameraTransforms.TransformType.FIRST_PERSON, fp));
  }
}
