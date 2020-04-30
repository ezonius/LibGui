package io.github.cottonmc.cotton.gui.widget;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.ValidatedSlot;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.Inventory;

public class WItemSlot extends WWidget {
	private final List<ValidatedSlot> peers = Lists.newArrayList();
	private BackgroundPainter backgroundPainter;
	private Inventory inventory;
	private int startIndex = 0;
	private int slotsWide = 1;
	private int slotsHigh = 1;
	private boolean big = false;
	private boolean modifiable = true;
	private boolean validated;
	
	public WItemSlot(Inventory inventory, int startIndex, int slotsWide, int slotsHigh, boolean big, boolean ltr) {
		this.inventory = inventory;
		this.startIndex = startIndex;
		this.slotsWide = slotsWide;
		this.slotsHigh = slotsHigh;
		this.big = big;
		//this.ltr = ltr;
	}
	
	private WItemSlot() {}
	
	public static WItemSlot of(Inventory inventory, int index) {
		WItemSlot w = new WItemSlot();
		w.inventory = inventory;
		w.startIndex = index;
		
		return w;
	}
	
	public static WItemSlot of(Inventory inventory, int startIndex, int slotsWide, int slotsHigh) {
		WItemSlot w = new WItemSlot();
		w.inventory = inventory;
		w.startIndex = startIndex;
		w.slotsWide = slotsWide;
		w.slotsHigh = slotsHigh;
		
		return w;
	}
	
	public static WItemSlot outputOf(Inventory inventory, int index) {
		WItemSlot w = new WItemSlot();
		w.inventory = inventory;
		w.startIndex = index;
		w.big = true;
		
		return w;
	}
	
	public static WItemSlot ofPlayerStorage(Inventory inventory) {
		WItemSlot w = new WItemSlot();
		w.inventory = inventory;
		w.startIndex = 9;
		w.slotsWide = 9;
		w.slotsHigh = 3;
		//w.ltr = false;
		
		return w;
	}
	
	@Override
	public int getWidth() {
		return slotsWide * 18;
	}
	
	@Override
	public int getHeight() {
		return slotsHigh * 18;
	}

	public boolean isBigSlot() {
		return big;
	}

	/**
	 * Returns true if the contents of this {@code WItemSlot} can be modified by players.
	 *
	 * @return true if this slot is modifiable
	 * @since 1.8.0
	 */
	public boolean isModifiable() {
		return modifiable;
	}

	public WItemSlot setModifiable(boolean modifiable) {
		this.modifiable = modifiable;
		for (ValidatedSlot peer : peers) {
			peer.setModifiable(modifiable);
		}
		return this;
	}

	@Override
	public void createPeers(GuiDescription c) {
		if (isValidated()) return;
		super.createPeers(c);
		peers.clear();
		int index = startIndex;
		
		for (int y = 0; y < slotsHigh; y++) {
			for (int x = 0; x < slotsWide; x++) {
				ValidatedSlot slot = new ValidatedSlot(inventory, index, this.getAbsoluteX() + (x * 18), this.getAbsoluteY() + (y * 18));
				slot.setModifiable(modifiable);
				peers.add(slot);
				c.addSlotPeer(slot);
				index++;
			}
		}
		setValidated(true);
	}
	
	@Environment(EnvType.CLIENT)
	public void setBackgroundPainter(BackgroundPainter painter) {
		this.backgroundPainter = painter;
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void paintBackground(int x, int y) {
		if (backgroundPainter!=null) {
			backgroundPainter.paintBackground(x, y, this);
		} else {
			for(int ix = 0; ix < getWidth()/18; ++ix) {
				for(int iy = 0; iy < getHeight()/18; ++iy) {
					int lo = 0xB8000000;
					int bg = 0x4C000000;
					int hi = 0xB8FFFFFF;
					if (isBigSlot()) {
						ScreenDrawing.drawBeveledPanel((ix * 18) + x - 4, (iy * 18) + y - 4, 24, 24,
								lo, bg, hi);
					} else {
						ScreenDrawing.drawBeveledPanel((ix * 18) + x - 1, (iy * 18) + y - 1, 18, 18,
								lo, bg, hi);
					}
				}
			}
		}
	}
	
	public boolean isValidated() {
		return validated;
	}
	
	public WItemSlot setValidated(boolean validated) {
		this.validated = validated;
		return this;
	}
}
