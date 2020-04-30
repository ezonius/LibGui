package io.github.cottonmc.cotton.gui.client;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

import io.github.cottonmc.cotton.gui.CottonCraftingController;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.Objects;

public class CottonInventoryScreen<T extends CottonCraftingController> extends HandledScreen<T> implements TextHoverRendererScreen {
	protected CottonCraftingController description;
	public static final int PADDING = 8;
	protected WWidget lastResponder = null;
	protected WWidget focus = null;
	
	public CottonInventoryScreen(T container, PlayerEntity player) {
		super(container, player.inventory, new LiteralText(""));
		this.description = container;
		width = 18*9;
		height = 18*9;
		this.backgroundWidth = 18*9;
		this.backgroundHeight = 18*9;
	}
	
	/*
	 * RENDERING NOTES:
	 * 
	 * * "width" and "height" are the width and height of the overall screen
	 * * "backgroundWidth" and "backgroundHeight" are the width and height of the panel to render
	 * * ~~"left" and "top" are *actually* self-explanatory~~
	 *   * "left" and "top" are now (1.15) "x" and "y". A bit less self-explanatory, I guess.
	 * * coordinates start at 0,0 at the topleft of the screen.
	 */
	
	@Override
	public void init(MinecraftClient minecraftClient_1, int screenWidth, int screenHeight) {
		super.init(minecraftClient_1, screenWidth, screenHeight);
		
		description.addPainters();
		
		reposition();
	}
	
	public void reposition() {
		WPanel basePanel = description.getRootPanel();
		if (basePanel!=null) {
			basePanel.validate(description);

			backgroundWidth = basePanel.getWidth();
			backgroundHeight = basePanel.getHeight();
			
			//DEBUG
			if (backgroundWidth<16) backgroundWidth=300;
			if (backgroundHeight<16) backgroundHeight=300;
		}
		x = (width / 2) - (backgroundWidth / 2);
		y =  (height / 2) - (backgroundHeight / 2);
	}
	
	@Override
	public void onClose() {
		super.onClose();
	}
	
	@Override
	public boolean isPauseScreen() {
		//...yeah, we're going to go ahead and override that.
		return false;
	}
	
	@Override
	public boolean charTyped(char ch, int keyCode) {
		if (description.getFocus()==null) return false;
		description.getFocus().onCharTyped(ch);
		return true;
	}
	
	@Override
	public boolean keyPressed(int ch, int keyCode, int modifiers) {
		//System.out.println("Key " + Integer.toHexString(ch)+" "+Integer.toHexString(keyCode));
		if (ch==GLFW.GLFW_KEY_ESCAPE) {
			this.client.player.closeHandledScreen();
			return true;
		} else {
			if (description.getFocus()==null) {
				if (MinecraftClient.getInstance().options.keyInventory.matchesKey(ch, keyCode)) {
					this.client.player.closeHandledScreen();
					return true;
				}
				this.handleHotbarKeyPressed(ch, keyCode);
				if (this.focusedSlot != null && this.focusedSlot.hasStack()) {
					if (Objects.requireNonNull(this.client).options.keyPickItem.matchesKey(ch, keyCode)) {
						this.onMouseClick(this.focusedSlot, this.focusedSlot.id, 0, SlotActionType.CLONE);
						return true;
					} else if (this.client.options.keyDrop.matchesKey(ch, keyCode)) {
						this.onMouseClick(this.focusedSlot, this.focusedSlot.id, hasControlDown() ? 1 : 0, SlotActionType.THROW);
						return true;
					}
				}
				return false;
			} else {
				description.getFocus().onKeyPressed(ch, keyCode, modifiers);
				return true;
			}
		}
	}
	
	@Override
	public boolean keyReleased(int ch, int keyCode, int modifiers) {
		if (description.getFocus()==null) return false;
		description.getFocus().onKeyReleased(ch, keyCode, modifiers);
		return true;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		boolean result = super.mouseClicked(mouseX, mouseY, mouseButton);
		int containerX = (int)mouseX-x;
		int containerY = (int)mouseY-y;
		if (containerX<0 || containerY<0 || containerX>=width || containerY>=height) return result;
		if (lastResponder==null) {
			lastResponder = description.doMouseDown(containerX, containerY, mouseButton);
		} else {
			//This is a drag instead
		}
		return result;
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) { //Testing shows that STATE IS ACTUALLY BUTTON
		boolean result = super.mouseReleased(mouseX, mouseY, mouseButton);
		int containerX = (int)mouseX-x;
		int containerY = (int)mouseY-y;
		
		if (lastResponder!=null) {
			lastResponder.onMouseUp(containerX-lastResponder.getAbsoluteX(), containerY-lastResponder.getAbsoluteY(), mouseButton);
			if (containerX>=0 && containerY>=0 && containerX<width && containerY<height) {
				lastResponder.onClick(containerX-lastResponder.getAbsoluteX(), containerY-lastResponder.getAbsoluteY(), mouseButton);
			}
		} else {
			description.doMouseUp(containerX, containerY, mouseButton);
		}
		
		lastResponder = null;
		return result;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double deltaX, double deltaY) {
		boolean result = super.mouseDragged(mouseX, mouseY, mouseButton, deltaX, deltaY);
		
		int containerX = (int)mouseX-x;
		int containerY = (int)mouseY-y;
		
		if (lastResponder!=null) {
			lastResponder.onMouseDrag(containerX-lastResponder.getAbsoluteX(), containerY-lastResponder.getAbsoluteY(), mouseButton, deltaX, deltaY);
			return result;
		} else {
			if (containerX<0 || containerY<0 || containerX>=width || containerY>=height) return result;
			description.doMouseDrag(containerX, containerY, mouseButton, deltaX, deltaY);
		}
		return result;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (description.getRootPanel()==null) return super.mouseScrolled(mouseX, mouseY, amount);
		
		WPanel root = description.getRootPanel();
		int containerX = (int)mouseX-x;
		int containerY = (int)mouseY-y;
		
		WWidget child = root.hit(containerX, containerY);
		child.onMouseScroll(containerX - child.getAbsoluteX(), containerY - child.getAbsoluteY(), amount);
		return true;
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		if (description.getRootPanel()==null) return;

		WPanel root = description.getRootPanel();
		int containerX = (int)mouseX-x;
		int containerY = (int)mouseY-y;

		WWidget child = root.hit(containerX, containerY);
		child.onMouseMove(containerX - child.getAbsoluteX(), containerY - child.getAbsoluteY());
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float partialTicks, int mouseX, int mouseY) {} //This is just an AbstractContainerScreen thing; most Screens don't work this way.
	
	public void paint(int mouseX, int mouseY) {
		super.renderBackground(ScreenDrawing.matrices);
		
		if (description!=null) {
			WPanel root = description.getRootPanel();
			if (root!=null) {
				root.paintBackground(x, y, mouseX-x, mouseY-y);
			}
		}
		
		if (getTitle() != null) {
			textRenderer.method_27528(ScreenDrawing.matrices, getTitle(), x, y, description.getTitleColor());
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
		ScreenDrawing.matrices = matrices;
		paint(mouseX, mouseY);
		
		super.render(matrices, mouseX, mouseY, partialTicks);
		DiffuseLighting.disable(); //Needed because super.render leaves dirty state
		
		if (description!=null) {
			WPanel root = description.getRootPanel();
			if (root!=null) {
				root.paintForeground(x, y, mouseX, mouseY);
				
				WWidget hitChild = root.hit(mouseX-x, mouseY-y);
				if (hitChild!=null) hitChild.renderTooltip(x, y, mouseX-x, mouseY-y);
			}
		}
		
		drawMouseoverTooltip(matrices, mouseX, mouseY); //Draws the itemstack tooltips
	}
	
	@Override
	public void tick() {
		super.tick();
		if (description!=null) {
			WPanel root = description.getRootPanel();
			if (root!=null) {
				root.tick();
			}
		}
	}

	@Override
	public void renderTextHover(Text text, int x, int y) {
		renderTextHoverEffect(ScreenDrawing.matrices, text, x, y);
	}
}
